<script setup lang="ts">
import { ref, reactive } from "vue";
import { appWindow } from "@tauri-apps/api/window";
import { fetch, Body } from "@tauri-apps/api/http";
import { invoke } from "@tauri-apps/api/tauri";
import { WebviewWindow } from "@tauri-apps/api/window";

const data = reactive({
  form: {
    account: "cjy11",
    password: "123456",
  },
});

const minimizeWindow = () => {
  appWindow.minimize();
};

const closeWindow = () => {
  appWindow.close();
};

const login = async () => {
  // request login api.
  let { data: token }: any = (
    await fetch("http://localhost:6503/app/login", {
      method: "POST",
      body: Body.json(data.form),
    })
  ).data;

  // save the token to locally.
  localStorage.setItem("token", token);

  // window hide.
  const loginWindow = WebviewWindow.getByLabel("login");
  loginWindow?.hide();

  // loading embedded asset:
  const webview = new WebviewWindow("chat", {
    url: "#/chatView",
    center: true,
    minWidth: 800,
    minHeight: 600,
    decorations: false,
  });

  webview.once("tauri://created", function () {
    // webview window successfully created
    loginWindow?.close();
  });
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
    <div class="back">
      <span>Chat</span>
    </div>
    <div class="input-box">
      <div>
        <span>账户</span>
        <input v-model="data.form.account" type="text" />
      </div>
      <div>
        <span>密码</span>
        <input v-model="data.form.password" type="password" />
      </div>
      <button @click="login()" class="login-btn">登录</button>
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
  align-items: center;
  height: 100%;
  .back {
    width: 100%;
    height: 125px;
    background: #665dfe;
    span {
      display: inline-block;
      color: #fff;
      margin: 10px;
      font-size: 20px;
      font-size: 600;
    }
  }
  .input-box {
    width: 240px;
    margin: 0 auto;
    > div {
      height: 40px;
      display: flex;
      align-items: center;
      justify-content: space-between;
      border-bottom: 1px solid #e5e5e5;
      margin-top: 15px;
      span {
        flex-shrink: 0;
        color: #808080;
        font-size: 13px;
      }
      input {
        margin-left: 10px;
        outline: none;
        border: none;
        width: 100%;
        height: 40px;
        font-size: 16px;
      }
    }
  }
}

.login-btn {
  width: 100%;
  height: 40px;
  background: #665dfe;
  color: #fff;
  outline: none;
  border: none;
  margin-top: 20px;
  border-radius: 4px;
  font-size: 15px;
}
</style>
