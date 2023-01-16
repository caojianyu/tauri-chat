<script setup lang="ts">
import { ref, onBeforeMount } from "vue";

import { minimizeWindow, closeWindow } from "../common/WindowEvent";

import { emit, listen, once } from "@tauri-apps/api/event";
import { EventType, MsgType } from "../common/Constans";

/** 视频发起者 */
const sender = ref("");

onBeforeMount(async () => {
  emit(EventType.CHAT_B, {
    msgType: MsgType.READY,
  });
  
  listen<Record<string, any>>(EventType.VIDEO_SELECTION, async (event) => {
    const payload = event.payload;
    switch (payload.msgType) {
      case MsgType.HANG_UP:
        // 发起者主动挂断
        closeWindow();
        break;
      default:
        sender.value = event.payload.msg;
    }
  });
});

/**
 * 发送接受状态到总线
 *
 * @param status 接受状态
 */
const acceptStatus = (status: boolean) => {
  emit(EventType.CHAT_B, {
    msgType: MsgType.ACCEPT_STATUS,
    msg: status,
  });
  closeWindow();
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
    <p>{{ sender }}邀请你视频</p>
    <div>
      <button @click="acceptStatus(false)">挂断</button>
      <button @click="acceptStatus(true)">接受</button>
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
  align-items: center;
  justify-content: space-evenly;
  color: #fff;
  > div {
    width: 100%;
    display: flex;
    justify-content: space-evenly;
    align-items: center;
    button {
      width: 80px;
      height: 32px;
      color: #fff;
      &:first-of-type {
        background-color: #d84b4b;
      }
      &:last-of-type {
        background-color: #0cb309;
      }
    }
  }
}
</style>
