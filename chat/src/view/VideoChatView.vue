<script setup lang="ts">
import { ref, reactive, onBeforeMount } from "vue";
import { minimizeWindow, closeWindow } from "../common/WindowEvent";
import { emit, listen } from "@tauri-apps/api/event";
import { EventType, MsgType } from "../common/Constans";

/** webrtc连接对象 */
let pc: RTCPeerConnection;
/** 媒体流对象 */
let webcamStream: MediaStream;

/** 本地视频框 */
const local = ref<HTMLVideoElement>();
/** 远程视频框 */
const remote = ref<HTMLVideoElement>();

/** 消息对象 */
const msgObj = reactive<Record<string, any>>({
  msg: "",
  msgType: "",
  receiver: "",
  sender: "",
});

onBeforeMount(async () => {
  msgObj.msgType = MsgType.READY;
  // 页面完成构建，通知主页面
  emit(EventType.VIDEO_CHAT_READY, msgObj);

  // TODO 监听解除？
  listen<Record<string, any>>(EventType.VIDEO_CHAT, (event) => {
    const payload = event.payload;
    switch (payload.msgType) {
      case MsgType.READY:
        videoCall(payload.msg);
        break;
      case MsgType.VIDEO_OFFDER:
        handleVideoOfferMsg(payload);
        break;
      case MsgType.VIDEO_ANSWER:
        handleVideoAnswerMsg(payload);
        break;
      case MsgType.NEW_ICE_CANDIDATE:
        handleNewICECandidateMsg(payload);
        break;
      case MsgType.HANG_UP:
        handleHangUpMsg();
        break;
    }
  });
});

/**
 * 处理offer消息
 *
 * @param data offer信息
 */
const handleVideoOfferMsg = async (data: Record<string, any>) => {
  // 发起者作为接收者，接收应答
  msgObj.receiver = data.sender;

  // 不是作为发起人打开视频通话
  await videoCall(false);

  const desc = new RTCSessionDescription(data.msg);
  await pc.setRemoteDescription(desc);
  await pc.setLocalDescription(await pc.createAnswer());

  msgObj.msgType = MsgType.VIDEO_ANSWER;
  msgObj.msg = pc.localDescription;
  sendToServer();
};

/**
 * 处理应答消息
 *
 * @param data 应答消息
 */
const handleVideoAnswerMsg = async (data: Record<string, any>) => {
  const desc = new RTCSessionDescription(data.msg);
  await pc.setRemoteDescription(desc).catch(reportError);
};

const handleNewICECandidateMsg = async (data: Record<string, any>) => {
  const candidate = new RTCIceCandidate(data.msg);
  try {
    await pc.addIceCandidate(candidate);
  } catch (err) {
    reportError(err);
  }
};

const handleICEConnectionStateChangeEvent = (event: Event) => {
  console.log("*** ICE连接状态变为" + pc.iceConnectionState);
};

const handleICEGatheringStateChangeEvent = (event: Event) => {
  console.log("*** ICE聚集状态变为" + pc.iceGatheringState);
};

const handleSignalingStateChangeEvent = (event: Event) => {
  console.log("*** WebRTC信令状态变为: " + pc.signalingState);
};

const handleICECandidateEvent = (event: RTCPeerConnectionIceEvent) => {
  if (event.candidate) {
    msgObj.msgType = MsgType.NEW_ICE_CANDIDATE;
    msgObj.msg = event.candidate;
    sendToServer();
  }
};

/**
 * 获取远程媒体流赋值到video标签
 *
 * @param event
 */
const handleTrackEvent = (event: RTCTrackEvent) => {
  remote.value!.srcObject = event.streams[0];
};

/** 挂断视频（主动、被动） */
const handleHangUpMsg = () => {
  /**
   * TODO 目前版本关闭窗口代码有问题，wry 0.24.1中修复。
   * 但是tauri最新版本中，wry 版本是0.23.4。尝试过使用
   * wry 0.24.1版本。
   * 但源代码../core/tauri-runtime-wry/src/lib.rs
   * 3026行for循环中，闭包返回值添加了智能指针Cow类型。
   * 导致原返回值类型出现错误。为最小化影响程序，等待tauri
   * 后续版本升级依赖版本。
   */
  closeWindow();
};

/** 初始化rtc */
const initRTCPeerConnection = () => {
  // 可使用开源程序自建
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

  // 协商过程处理程序
  pc.onicecandidate = handleICECandidateEvent;
  pc.oniceconnectionstatechange = handleICEConnectionStateChangeEvent;
  pc.onicegatheringstatechange = handleICEGatheringStateChangeEvent;
  pc.onsignalingstatechange = handleSignalingStateChangeEvent;
  // pc.onnegotiationneeded = handleNegotiationNeededEvent;
  pc.ontrack = handleTrackEvent;
  // pc.ondatachannel = handleDataChannel;
};

/** 发送offer */
const sendOffer = async () => {
  const offer = await pc.createOffer();

  // 描述
  await pc.setLocalDescription(offer);

  // 发送给远程计算机
  msgObj.msgType = MsgType.VIDEO_OFFDER;
  msgObj.msg = pc.localDescription;
  sendToServer();
};

/**
 * 视频通话
 *
 * @param isSender 是否是发起人
 */
const videoCall = async (isSender: boolean) => {
  initRTCPeerConnection();
  // 获取本地相机
  webcamStream = await navigator.mediaDevices.getUserMedia({
    video: true,
    audio: false, // 打包环境应开启捕获音频
  });

  // 媒体流赋值给本地video标签
  local.value!.srcObject = webcamStream;
  // 将本地媒体流添加到通道传输
  webcamStream.getTracks().forEach((track: MediaStreamTrack) =>
    // pc.addTransceiver(track, { streams: [webcamStream] })
    pc.addTrack(track, webcamStream)
  );
  // 是发起人就发送offer
  if (isSender) {
    sendOffer();
  }
};

/** 挂断 */
const hangUp = () => {
  msgObj.msgType = MsgType.HANG_UP;
  emit(EventType.CHAT_A, msgObj);
  handleHangUpMsg();
};

/** 发送消息到主界面，由主界面发送至服务器，总线型传输 */
const sendToServer = () => {
  emit(EventType.CHAT_A, msgObj);
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
    <div class="video-box">
      <video class="remote" ref="remote" autoplay></video>
      <video class="local" ref="local" autoplay></video>
    </div>
    <div>
      <button @click="hangUp()" class="hang-up">挂断</button>
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
  color: #fff;
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
  background: rgba(255, 255, 255, 0.2);
}
.close-btn:hover {
  background: #fb7373;
  color: #fff;
}

.main {
  background: rgba(0, 0, 0, 0.8);
  padding-top: 28px;
  height: 100%;
  box-sizing: border-box;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  > div {
    width: 100%;
    display: flex;
    justify-content: center;
    .hang-up {
      width: 80px;
      height: 32px;
      color: #fff;
      background-color: #d84b4b;
      margin: 15px 0;
    }
  }

  .video-box {
    height: 100%;
    video {
      width: 50%;
      height: 100%;
    }
  }
}
</style>
