<script setup lang="ts">
import { ref, reactive, onBeforeMount } from "vue";
import { appWindow } from "@tauri-apps/api/window";
import { invoke } from "@tauri-apps/api/tauri";
import { fetch } from "@tauri-apps/api/http";

const data = reactive({
  account: "",
  avatar: "",
  userList: [] as any[],
  currentSelectUserIndex: -1,
  ws: null,
  msg: "",
  records: [] as any[],
  receiver: "",
  isOpenVideo: false,
});

const minimizeWindow = () => {
  appWindow.minimize();
};

const closeWindow = () => {
  appWindow.close();
};

// dom
const record: any = ref(null);
const local: any = ref(null);
const remote: any = ref(null);

let ws: any = null;
let pc: any = null;
let webcamStream: any = null;

onBeforeMount(() => {
  getAccountInfo();
});

// methods
const getAccountInfo = async () => {
  // request login api.
  const {
    data: { account, avatar },
  }: any = (
    await fetch("http://127.0.0.1:6503/app/get_account_info", {
      method: "GET",
      headers: {
        token: localStorage.getItem("token"),
      },
    })
  ).data;
  data.account = account;
  data.avatar = avatar;

  initWebSocket();
};

// init websocket
const initWebSocket = () => {
  ws = new WebSocket(`ws://127.0.0.1:6503/chat/${data.account}`);

  ws.onopen = () => {
    setInterval(() => {
      sendToServer({
        msg_type: "heartbeat",
        receiver: "",
        sender: "",
        msg: "",
        sender_avatar: "",
      });
    }, 1000 * 60);
  };

  ws.onmessage = async (e: any) => {
    let msg = JSON.parse(e.data);
    switch (msg.msg_type) {
      case "user-list":
        data.userList = msg.user_list;
        break;
      case "message":
        // 1.sender 2.receiver
        msg.isSend = 2;
        // save record to local file.
        await invoke("save_chat_record", {
          directory: data.account,
          firstAccount: msg.sender,
          secondAccount: msg.receiver,
          jsonStr: JSON.stringify(msg),
        });
        // query record.
        await getChatRecord();
        break;
      case "video-offer": // Invitation and offer to chat
        handleVideoOfferMsg(msg);
        break;
      case "video-answer": // Callee has answered our offer
        handleVideoAnswerMsg(msg);
        break;
      case "new-ice-candidate": // A new ICE candidate has been received
        handleNewICECandidateMsg(msg);
        break;
    }
  };

  ws.onerror = () => {
    initWebSocket();
  };
};

// click event, send message
const sendMsg = async () => {
  // the meassge and receiver can not be empty.
  if (!data.msg || !data.receiver) {
    return;
  }

  let msg: any = {
    msg_type: "message",
    msg: data.msg,
    receiver: data.receiver,
    sender: data.account,
    sender_avatar: data.avatar,
  };
  sendToServer(msg);

  msg.isSend = 1;
  data.msg = "";

  await invoke("save_chat_record", {
    directory: data.account,
    firstAccount: msg.sender,
    secondAccount: msg.receiver,
    jsonStr: JSON.stringify(msg),
  });

  await getChatRecord();
};

const scrollBar = () => {
  setTimeout(() => {
    record.value.scrollTo({
      top: record.value.scrollHeight,
      left: 0,
      behavior: "smooth",
    });
  }, 100);
};

const handleVideoOfferMsg = async (msg: any) => {
  data.receiver = msg.sender;

  await videoCommunication(false);

  let desc = new RTCSessionDescription(JSON.parse(msg.msg));
  await pc.setRemoteDescription(desc);

  await pc.setLocalDescription(await pc.createAnswer());
  sendToServer({
    msg_type: "video-answer",
    receiver: data.receiver,
    msg: JSON.stringify(pc.localDescription),
    sender: data.account,
    sender_avatar: "",
  });
};

const handleVideoAnswerMsg = async (msg: any) => {
  let desc = new RTCSessionDescription(JSON.parse(msg.msg));
  await pc.setRemoteDescription(desc).catch(reportError);
};

const handleNewICECandidateMsg = async (msg: any) => {
  let candidate = new RTCIceCandidate(JSON.parse(msg.msg));
  try {
    await pc.addIceCandidate(candidate);
  } catch (err) {
    reportError(err);
  }
};

const selectUser = async (index: number) => {
  data.currentSelectUserIndex = index;
  data.receiver = data.userList[index].account;
  await getChatRecord();
};

const getChatRecord = async () => {
  await invoke("get_chat_record", {
    directory: data.account,
    firstAccount: data.account,
    secondAccount: data.receiver,
  }).then((res: any) => {
    if (res) {
      data.records = JSON.parse(res);
    }
  });

  // record panel
  scrollBar();
};

const handleICEConnectionStateChangeEvent = (event: any) => {
  console.log("*** ICE连接状态变为" + pc.iceConnectionState);
};

const handleICEGatheringStateChangeEvent = (event: any) => {
  console.log("*** ICE聚集状态变为" + pc.iceGatheringState);
};

const handleSignalingStateChangeEvent = (event: any) => {
  console.log("*** WebRTC信令状态变为: " + pc.signalingState);
};

const handleICECandidateEvent = (event: any) => {
  if (event.candidate) {
    sendToServer({
      msg_type: "new-ice-candidate",
      receiver: data.receiver,
      msg: JSON.stringify(event.candidate),
      sender: data.account,
      sender_avatar: "",
    });
  }
};

const handleTrackEvent = (event: any) => {
  remote.value.srcObject = event.streams[0];
};

const sendToServer = (msg: any) => {
  let msgJSON = JSON.stringify(msg);
  ws.send(msgJSON);
};

const videoCommunication = async (isSender: boolean) => {
  // open panel
  data.isOpenVideo = true;

  // Create an RTCPeerConnection which knows to use our chosen
  // STUN server.
  const iceServer: object = {
    iceServers: [
      {
        url: "stun:stun.l.google.com:19302",
      },
      {
        url: "turn:numb.viagenie.ca",
        username: "webrtc@live.com",
        credential: "muazkh",
      },
    ],
  };

  pc = new RTCPeerConnection(iceServer);

  // Set up event handlers for the ICE negotiation process.
  pc.onicecandidate = handleICECandidateEvent;
  pc.oniceconnectionstatechange = handleICEConnectionStateChangeEvent;
  pc.onicegatheringstatechange = handleICEGatheringStateChangeEvent;
  pc.onsignalingstatechange = handleSignalingStateChangeEvent;
  // pc.onnegotiationneeded = handleNegotiationNeededEvent;
  pc.ontrack = handleTrackEvent;

  // get local camera
  webcamStream = await navigator.mediaDevices.getUserMedia({
    video: true,
    audio: false,
  });

  local.value.srcObject = webcamStream;

  webcamStream.getTracks().forEach((track: any) =>
    // pc.addTransceiver(track, { streams: [webcamStream] })
    pc.addTrack(track, webcamStream)
  );

  // send offer
  if (isSender) {
    const offer = await pc.createOffer();

    // Establish the offer as the local peer's current
    // description.
    await pc.setLocalDescription(offer);

    // Send the offer to the remote peer.
    sendToServer({
      msg_type: "video-offer",
      receiver: data.receiver,
      msg: JSON.stringify(pc.localDescription),
      sender: data.account,
      sender_avatar: "",
    });
  }
};
</script>

<template>
  <div data-tauri-drag-region class="titlebar">
    <div @click="minimizeWindow()" class="titlebar-button">
      <i class="iconfont icon-jianhao"></i>
    </div>
    <div @click="closeWindow()" class="titlebar-button close-btn">
      <i class="iconfont icon-guanbi"></i>
    </div>
  </div>
  <div class="main">
    <div class="sidebar">
      <img class="self-avatar" :src="data.avatar" alt="" />
      <div class="icon-box">
        <i class="iconfont icon-liaotian"></i>
      </div>
    </div>
    <div class="user-list">
      <div class="search-box">
        <input placeholder="搜索" type="text" />
      </div>
      <div class="list">
        <div
          @click="selectUser(index)"
          class="item"
          :class="data.currentSelectUserIndex == index ? 'active' : ''"
          v-for="(item, index) in data.userList"
          :key="index"
        >
          <img class="avatar" :src="item.avatar" alt="" />
          <div>
            <h5>{{ item.account }}</h5>
            <span class="time">1分钟前</span>
          </div>
        </div>
      </div>
    </div>
    <div class="content">
      <div class="title">
        <span>{{
          data.currentSelectUserIndex != -1 ? data.receiver : ""
        }}</span>
      </div>
      <div ref="record" class="record">
        <div
          :class="item.isSend == 1 ? 'self' : 'opposite'"
          v-for="(item, index) in data.records"
          :key="index"
        >
          <div v-if="item.isSend == 1" class="bubble-box">
            {{ item.msg }}
          </div>
          <img
            class="avatar"
            :src="item.isSend == 1 ? data.avatar : item.sender_avatar"
            alt=""
          />
          <div v-if="item.isSend == 2" class="bubble-box">
            {{ item.msg }}
          </div>
        </div>
      </div>
      <div class="input">
        <i @click="videoCommunication(true)" class="iconfont icon-shipin"></i>
        <textarea v-model="data.msg" cols="30" rows="4"></textarea>
        <button @click="sendMsg()" class="send-btn">发送</button>
      </div>
    </div>
    <div v-if="data.isOpenVideo" class="video-box">
      <video class="remote" ref="remote" autoplay></video>
      <video class="local" ref="local" autoplay></video>
    </div>
  </div>
</template>

<style lang="less" scoped>
.titlebar {
  height: 28px;
  user-select: none;
  display: flex;
  justify-content: flex-end;
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  color: #494949;
}
.titlebar-button {
  display: inline-flex;
  justify-content: center;
  align-items: center;
  width: 44px;
  height: 26px;
  cursor: pointer;
  i {
    font-size: 16px;
  }
}
.titlebar-button:hover {
  background: #e2e2e2;
}
.close-btn:hover {
  background: #fb7373;
  color: #fff;
}
.main {
  height: 100%;
  display: flex;
  background: #f8f8f8;
}
.sidebar {
  width: 60px;
  height: 100%;
  background: #665dfe;
  color: #fff;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  position: relative;
  .icon-box {
    width: 40px;
    height: 40px;
    background: rgba(0, 0, 0, 0.2);
    display: flex;
    align-items: center;
    justify-content: center;
    border-radius: 4px;
    .iconfont {
      font-size: 22px;
    }
  }
}

.user-list {
  display: flex;
  flex-shrink: 0;
  flex-direction: column;
}

.search-box {
  height: 60px;
  border-right: 1px solid #e5e9f2;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  padding: 0 10px;
  input {
    width: 100%;
    height: 34px;
    border-radius: 40px;
    outline: none;
    padding: 0 15px;
    border: 1px solid #ced4da;
  }
}

.list {
  width: 260px;
  height: 100%;
  overflow-y: auto;
  padding: 10px;
  border-top: 1px solid #e5e9f2;
  border-right: 1px solid #e5e9f2;
  border-bottom: 1px solid #e5e9f2;
  .item {
    display: flex;
    height: 80px;
    align-items: center;
    padding: 0 20px;
    color: #705057;
    > div {
      margin-left: 10px;
      .time {
        font-size: 12px;
        color: #49556c;
      }
    }
    img {
      flex-shrink: 0;
    }
  }
  .active {
    background: #665dfe;
    border-radius: 4px;
    color: #fff;
    > div {
      .time {
        color: #fff;
      }
    }
  }
}

.content {
  display: flex;
  flex-direction: column;
  width: 100%;
  .title {
    flex-shrink: 0;
    height: 60px;
    display: flex;
    align-items: center;
    border-bottom: 1px solid #e5e9f2;
    padding: 0 20px;
    font-size: 20px;
  }
  .record {
    height: 100%;
    overflow-y: auto;
    > div {
      width: 60%;
      padding: 5px 30px;
      display: flex;
      justify-content: flex-start;
      margin: 5px 0;
      > div {
        padding: 10px;
        border-bottom-left-radius: 4px;
        border-bottom-right-radius: 4px;
        font-size: 14px;
        line-height: 22px;
      }
      img {
        width: 40px;
        height: 40px;
      }
    }
    .self {
      justify-content: flex-end;
      margin-left: auto;
      .bubble-box {
        border-top-left-radius: 4px;
        margin-right: 10px;
        background: #665dfe;
        color: #fff;
      }
    }
    .opposite {
      justify-content: flex-start;
      margin-right: auto;
      .bubble-box {
        border-top-right-radius: 4px;
        margin-left: 10px;
        background: #fff;
      }
    }
  }
  .input {
    width: 100%;
    flex-shrink: 0;
    border-top: 1px solid #e5e9f2;
    position: relative;
    line-height: 0;
    .iconfont {
      position: absolute;
      top: 20px;
      right: 20px;
      cursor: pointer;
      color: #665dfe;
      font-size: 18px;
    }
    .send-btn {
      background: #665dfe;
      color: #fff;
      outline: none;
      border: none;
      padding: 6px 20px;
      border-radius: 4px;
      position: absolute;
      right: 20px;
      bottom: 20px;
      cursor: pointer;
      &:hover {
        background: #584ff8;
      }
    }
  }
}

.video-box {
  width: 80%;
  height: 80%;
  background: #1a1a1a;
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  position: absolute;
  bottom: 0;
  video {
    vertical-align: middle;
  }
  .remote {
    height: 100%;
    width: 70%;
    // background: #fb7373;
  }
  .local {
    width: 30%;
  }
}

// common
.self-avatar {
  position: absolute;
  width: 40px;
  height: 40px;
  border-radius: 50%;
  top: 10px;
}

.avatar {
  width: 48px;
  height: 48px;
  border-radius: 50%;
}

textarea {
  width: 100%;
  resize: none;
  outline: 0;
  border: none;
  padding: 40px 30px;
  font-size: 16px;
  box-sizing: border-box;
  background: #f8f8f8;
}

::-webkit-scrollbar {
  width: 7px;
}
::-webkit-scrollbar-thumb {
  background: #665dfe;
  border-radius: 7px;
}
</style>
