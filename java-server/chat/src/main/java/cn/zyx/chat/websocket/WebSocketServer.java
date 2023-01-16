package cn.zyx.chat.websocket;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Component;

import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author caojianyu
 * @version 1.0.0
 * @date 2022-07-12 15:33
 * @email jieni_cao@foxmail.com
 */

@Component
@ServerEndpoint(value = "/chat/{account}")
public class WebSocketServer {

    private static StringRedisTemplate stringRedisTemplate;

    @Autowired
    public void setStringRedisTemplate(StringRedisTemplate stringRedisTemplate) {
        WebSocketServer.stringRedisTemplate = stringRedisTemplate;
    }

    private static final Map<String, Session> clients = new ConcurrentHashMap<>();

    @OnMessage
    public void onMessage(String msg, Session session) throws IOException {
        // 收到消息
        JSONObject jsonObject = JSONObject.parseObject(msg);
        String msgType = jsonObject.getString("msgType");
        // 接收消息类型1心跳、2发送消息
        switch (msgType) {
            case "heartbeat":
                System.out.println("heartbeat.");
                break;
            default:
                // 获取接收者
                String receiver = jsonObject.getString("receiver");
                if (StringUtils.isNotEmpty(receiver)) {
                    // 查询到session
                    Session receiverSession = clients.get(receiver);
                    // 如果用户在线则发送消息，不在线则缓存消息
                    if (receiverSession != null) {
                        // 发送消息
                        receiverSession.getBasicRemote().sendText(jsonObject.toJSONString());
                    } else {
                        ValueOperations<String, String> valueOperations = stringRedisTemplate.opsForValue();
                        String key = String.format("%s_unread_msg", receiver);
                        // 堆积消息
                        String contents = valueOperations.get(key);
                        List<JSONObject> list;
                        if (StringUtils.isEmpty(contents)) {
                            list = new ArrayList<>();
                            list.add(jsonObject);
                        } else {
                            list = JSONArray.parseArray(contents, JSONObject.class);
                            list.add(jsonObject);
                        }
                        valueOperations.set(key, JSONObject.toJSONString(list));
                    }

                }
        }
    }

    @OnOpen
    public void onOpen(@PathParam("account") String account, Session session) throws IOException {
        clients.put(account, session);

        // 查询当前用户有无未读消息，有则发送消息并删除未读消息缓存
        ValueOperations<String, String> valueOperations = stringRedisTemplate.opsForValue();
        String key = String.format("%s_unread_msg", account);
        String contents = valueOperations.get(key);
        if (StringUtils.isNotEmpty(contents)) {
            List<String> list = JSONArray.parseArray(contents, String.class);
            for (String msg : list) {
                session.getBasicRemote().sendText(msg);
            }
            stringRedisTemplate.delete(key);
        }

        System.out.println("当前已有" + clients.size());
    }

    @OnClose
    public void onClose(Session session) {
        // 查询退出用户
        String key = "";
        for (Map.Entry<String, Session> k : clients.entrySet()) {
            if (k.getValue().getId().equals(session.getId())) {
                key = k.getKey();
                break;
            }
        }

        clients.remove(key);

        System.out.println("退出一个用户");
        System.out.println("当前还有用户" + clients.size());
    }

}