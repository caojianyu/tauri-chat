
/** websocket消息类型 */
enum MsgType {
    /** 心跳 */
    HEARTBEAT = "heartbeat",
    /** 普通消息 */
    MESSAGE = "message",
    /** 视频发起消息，视频接收者获取消息 */
    VIDEO_OFFDER = "video-offer",
    /** 视频应答消息，视频发起者获取消息 */
    VIDEO_ANSWER = "video-answer",
    /**  ICE协商 */
    NEW_ICE_CANDIDATE = "new-ice-candidate",
    /** 界面已准备好 */
    READY = "ready",
    /** 视频接受状态 */
    ACCEPT_STATUS = "accept-status",
    /** 挂断 */
    HANG_UP = "hang-up"
}

/** 事件消息类型 */
enum EventType {
    /** 聊天界面消息接收事件a */
    CHAT_A = "chat-a",
    /** 聊天界面消息接收事件b */
    CHAT_B = "chat-b",
    /** 视频聊天界面消息接收 */
    VIDEO_CHAT = "video-chat",
    /** 视频聊天界面已准备 */
    VIDEO_CHAT_READY = "video-chat-ready",
    /** 视频聊天选择事件 */
    VIDEO_SELECTION = "video-selection"
}

/** rust方法 */
class RustFn {
    /** 保存聊天记录 */
    public static SAVE_CHAT_HISTORY = "save_chat_history";
}

/** 页面路径 */
class PagePath {
    /** 主界面，即聊天界面 */
    public static CHAT_VIEW = "#/chat";
    /** 视频接受、挂断选择界面 */
    public static VIDEO_SELECTION_VIEW = "#/videoSelection";
    /** 视频聊天界面 */
    public static VIDEO_CHAT_VIEW = "#/videoChat";
}

/** 聊天软件相关配置 */
class TauriChat {
    /** 软件目录 */
    public static DIR = "TauriChat Files";
    /** 聊天记录文件后缀 */
    public static SUFFIX = ".txt";
}

export {
    MsgType,
    EventType,
    RustFn,
    PagePath,
    TauriChat
}