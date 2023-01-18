import { ref, reactive, getCurrentInstance, nextTick } from "vue";
import { invoke } from "@tauri-apps/api/tauri";
import { WebviewWindow } from "@tauri-apps/api/window";
import { emit, listen, once } from '@tauri-apps/api/event';
import { createDir, readTextFile, BaseDirectory } from '@tauri-apps/api/fs';
import { exists } from '@tauri-apps/api/fs';
import { documentDir } from '@tauri-apps/api/path';

// 接口
import { getUserInfo, getUserList } from "../common/Api";
import { MsgType, EventType, RustFn, PagePath, TauriChat } from "../common/Constans";

/*******************======数据定义-start======*******************/
/** 用户信息 */
const user = reactive({
  account: "",
  nickname: "",
  avatar: "",
});
/** 消息对象 */
const msgObj = reactive<Record<string, any>>({
  msg: "",
  msgType: "",
  receiver: "",
  sender: "",
  senderAvatar: ""
});
/** 好友列表 */
const userList = ref<Record<string, any>[]>([]);
/** 聊天历史 */
const chatHistory = ref<Record<string, any>[]>([]);
/** 当前选择用户索引 */
const currentUserIndex = ref(-1);
/** 视频接受状态 */
const acceptStatus = ref(false);
/*******************======数据定义-end======*******************/

/** 聊天记录框dom */
let record: HTMLDivElement;
/** websocket连接对象 */
let ws: WebSocket;

// 初始化
const init = async () => {
  // 消息面板dom
  const instance = getCurrentInstance();
  record = instance?.refs.record as HTMLDivElement

  // 获取用户信息
  const { account, nickname, avatar } = (await getUserInfo<typeof user>()).data;
  // 赋值用户信息
  // 初始化消息发送者
  user.account = msgObj.sender = account;
  user.nickname = nickname;
  user.avatar = msgObj.senderAvatar = avatar;

  // 查询朋友列表
  userList.value = (await getUserList()).data;

  initWebSocket();
};

/** 初始化websocket */
const initWebSocket = () => {
  ws = new WebSocket(`ws://127.0.0.1:6503/chat/${user.account}`);
  // ws = new WebSocket(`wss://www.xxx.cn/chat/${user.account}`);
  ws.onopen = () => {
    // 发送心跳
    setInterval(() => {
      sendToServer({
        msgType: MsgType.HEARTBEAT
      });
    }, 1000 * 60);
  };

  ws.onmessage = (event: MessageEvent) => {
    const data: Record<string, any> = JSON.parse(event.data);
    switch (data.msgType) {
      case MsgType.MESSAGE:
        // 聊天消息
        handleChatMsg(data);
        break;
      case MsgType.VIDEO_OFFDER:
        // 接收到offer，处理视频聊天的请求
        handleVideoOfferMsg(data);
        break;
      case MsgType.VIDEO_ANSWER:
        // 接收到answer 
        emit(EventType.VIDEO_CHAT, data);
        break;
      case MsgType.NEW_ICE_CANDIDATE:
        // 协商
        emit(EventType.VIDEO_CHAT, data);
        break;
      case MsgType.HANG_UP:
        // 挂断，通过接收状态来关闭某个窗口
        if (data.msg) {
          emit(EventType.VIDEO_CHAT, data);
        } else {
          emit(EventType.VIDEO_SELECTION, data);
        }
        break;
    }
  };

  ws.onerror = (event: Event) => {
    // websocket出错重连
    initWebSocket();
  };

}

/**
 * 处理聊天消息
 * 
 * @param data 聊天消息
 */
const handleChatMsg = async (data: Record<string, any>) => {
  // 收到聊天消息，创建目录以保存聊天记录
  await createUserDir();
  // 1.表示发送的消息 2.表示接收的消息
  data.isSend = 2;

  // 聊天记录文件路径
  const filePath = `${await documentDir()}\\${TauriChat.DIR}\\${user.account}\\${data.sender}${TauriChat.SUFFIX}`;
  // 保存聊天记录到本地（由于不清楚有没有持续写入的api，所以采用rust写入）
  await invoke(RustFn.SAVE_CHAT_HISTORY, {
    filePath,
    data: JSON.stringify(data),
  });

  // 当前选中好友账户
  let selectAccount;
  if (currentUserIndex.value != -1) {
    // 已选中好友账户
    selectAccount = userList.value[currentUserIndex.value].account;
  }

  // 判断消息发送者是否是当前选中好友账户，是则查询添加的聊天记录，不是则添加未读消息标识
  if (data.sender == selectAccount) {
    await getChatHistory(filePath);
  } else {
    // 找到好友，添加未读标识。表示好友发送消息过来了
    const sender = userList.value.find(item => item.account == data.sender);
    sender!.unread = true
  }
}

/** 创建用户目录，用于保存聊天信息 */
const createUserDir = async () => {
  // 以账户作为目录名称
  const dir = `${TauriChat.DIR}\\${user.account}`;
  // 不存在则创建目录
  const isExists = await exists(dir, { dir: BaseDirectory.Document });
  if (!isExists) {
    await createDir(dir, { dir: BaseDirectory.Document, recursive: true });
  }
}

/**
 * 处理对方发送offer的事件
 * 
 * @param data 收到offer消息
 */
const handleVideoOfferMsg = async (data: Record<string, any>) => {
  // 打开接受与否的窗口
  new WebviewWindow("video-selection", {
    url: PagePath.VIDEO_SELECTION_VIEW,
    width: 300,
    height: 200,
    decorations: false,
    resizable: false,
    x: 0,
    y: 200
  });

  // 接收视频界面已准备
  const unlisten = await listen<Record<string, any>>(EventType.CHAT_B, async (event) => {
    const payload = event.payload;
    switch (payload.msgType) {
      case MsgType.READY:
        emit(EventType.VIDEO_SELECTION, {
          msg: data.sender
        });
        break;
      case MsgType.ACCEPT_STATUS:
        if (payload.msg) {
          acceptStatus.value = payload.msg;
          // 接受则打开视频界面
          videoCall(false, data);
        } else {
          // 向请求方发送挂断的消息
          sendToServer({
            msgType: MsgType.HANG_UP,
            receiver: data.sender,
            msg: acceptStatus.value
          });
        }

        // 不管接受还是挂断都解除监听并关闭界面，因为此次会话已经结束
        await unlisten();
        break;
    }
  })
}

/**
 * 发送json数据至服务器
 * 
 * @param data 传输的json数据对象
 */
const sendToServer = (data: Record<string, any>) => {
  const json = JSON.stringify(data);
  ws.send(json);
}

/** 发送消息 */
const sendMsg = async () => {
  // 消息和接收者不能为空
  if (!msgObj.msg || !msgObj.receiver) {
    return;
  }

  msgObj.msgType = MsgType.MESSAGE;

  sendToServer(msgObj);
  await createUserDir();

  msgObj.isSend = 1;
  const filePath = `${await documentDir()}\\${TauriChat.DIR}\\${user.account}\\${msgObj.receiver}${TauriChat.SUFFIX}`;
  // 保存聊天记录
  await invoke(RustFn.SAVE_CHAT_HISTORY, {
    filePath,
    data: JSON.stringify(msgObj),
  });

  // 消息发送完成，置空消息
  msgObj.msg = "";

  await getChatHistory(filePath);
};

/**
 * 选择用户
 * 
 * @param index 用户当前索引
 */
const selectUser = async (index: number) => {
  currentUserIndex.value = index;
  // 未读消息标签不再显示
  userList.value[index].unread = false;
  // 选中用户则是消息接收者
  msgObj.receiver = userList.value[index].account;

  const filePath = `${TauriChat.DIR}\\${user.account}\\${msgObj.receiver}${TauriChat.SUFFIX}`;
  // 判断聊天记录文件是否存在，不存在则不查询
  const isExists = await exists(filePath, { dir: BaseDirectory.Document });
  if (isExists) {
    await getChatHistory(filePath);
    // 滚动条置于底部
    record.scrollTop = record.scrollHeight;
  } else {
    chatHistory.value = [];
  }
};

/**
 * 获取与相关好友的聊天记录
 * 
 * @param filePath 聊天记录文件路径
 */
const getChatHistory = async (filePath: string) => {
  // TODO 在发布版本下，不应直接读取全部，而且根据每天产生的聊天记录来读取
  // 读取到聊天记录
  const contents = await readTextFile(filePath, { dir: BaseDirectory.Document });
  const history: Record<string, any>[] = [];
  // 聊天记录是以行来记录的，所以直接分割成为数组，并且由于每行聊天记录保存的是json字符串，所以还需要解析成对象
  const data = contents.split("\n");
  data.forEach(item => {
    if (item) {
      history.push(JSON.parse(item))
    }
  })
  // 数据处理完成，进行赋值
  chatHistory.value = history;

  // 数组渲染完成后将dom滚动条位置固定在底部
  nextTick(() => {
    scrollBar();
  });
};

/** 滑动滚动条 */
const scrollBar = () => {
  record?.scrollTo({
    top: record.scrollHeight,
    left: 0,
    behavior: "smooth",
  });
};

/**
 * 打开视频界面
 * 
 * @param isSend 是否是发送人
 * @param data 接收发起者的数据 | 主动发起则忽略
 */
const videoCall = async (isSend: boolean, data?: Record<string, any>) => {
  // 新窗口-视频界面
  new WebviewWindow(EventType.VIDEO_CHAT, {
    url: PagePath.VIDEO_CHAT_VIEW,
    minWidth: 800,
    minHeight: 600,
    decorations: false,
  });

  // 接收视频界面已准备好事件
  const unlisten = await once(EventType.VIDEO_CHAT_READY, async (event) => {
    if (isSend) {
      // 发起者发起offer
      await emit(EventType.VIDEO_CHAT, {
        msgType: MsgType.READY, // 主页面收到，表示可以向视频界面发送消息了
        msg: isSend // 被动接收还是主动发起
      });
    } else {
      // 接收者处理offer
      await emit(EventType.VIDEO_CHAT, data);
    }
    // 解除监听
    unlisten();
  })

  // TODO 监听解除？
  listen<Record<string, any>>(EventType.CHAT_A, (event) => {
    const payload = event.payload;
    // 为不影响信息对象数据，直接传输
    sendToServer({
      msgType: payload.msgType,
      msg: payload.msg,
      receiver: payload.receiver || msgObj.receiver,
      sender: msgObj.sender
    });
  });
};

export {
  selectUser,
  videoCall,
  sendMsg,
  init,
}

export {
  user,
  msgObj,
  currentUserIndex,
  chatHistory,
  userList
}