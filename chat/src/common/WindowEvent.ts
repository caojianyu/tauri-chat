import { appWindow } from "@tauri-apps/api/window";

/** 最小化 */
const minimizeWindow = () => {
    appWindow.minimize();
};
/** 关闭 */
const closeWindow = () => {
    appWindow.close();
};
/** 隐藏 */
const hideWindow = () => {
    appWindow.hide();
};

export {
    minimizeWindow,
    closeWindow,
    hideWindow
}