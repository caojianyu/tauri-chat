package cn.zyx.chat.websocket;

import com.alibaba.fastjson.JSONObject;
import io.netty.util.internal.StringUtil;
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
import java.util.HashMap;
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
    public void onMessage(String message, Session session) throws IOException {
        JSONObject jsonObject = JSONObject.parseObject(message);
        System.out.println(message);
        String type = jsonObject.getString("msg_type");
        // 接收消息类型1心跳、2发送消息
        switch (type) {
            case "heartbeat":
                System.out.println("heartbeat.");
                break;
            default:
                String receiver = jsonObject.getString("receiver");
                Session receiverSession = clients.get(receiver);
                receiverSession.getBasicRemote().sendText(JSONObject.toJSONString(jsonObject));
                break;
        }
    }

    @OnOpen
    public void onOpen(@PathParam("account") String account, Session session) {
        ValueOperations<String, String> valueOperations = stringRedisTemplate.opsForValue();
        String accountStr = valueOperations.get(account);
        if (StringUtil.isNullOrEmpty(accountStr)) {
            return;
        }
        clients.put(account, session);

        broadcast();
        System.out.println("当前已有" + clients.size());
    }

    @OnClose
    public void onClose(Session session) {
        String key = "";
        for (Map.Entry<String, Session> k : clients.entrySet()) {
            if (k.getValue().getId().equals(session.getId())) {
                key = k.getKey();
                break;
            }
        }
        clients.remove(key);

        broadcast();
        System.out.println("退出一个用户");
        System.out.println("当前还有用户" + clients.size());
    }

    /**
     * 广播用户列表
     */
    private void broadcast() {
        Map<String, Object> map = new HashMap<>();
        map.put("msg_type", "user-list");
        ValueOperations<String, String> valueOperations = stringRedisTemplate.opsForValue();
        List<Map<String, Object>> userList = new ArrayList<>();
        clients.forEach((k, v) -> {
            JSONObject jsonObject = JSONObject.parseObject(valueOperations.get(k));
            jsonObject.remove("password");
            jsonObject.remove("salt");
            userList.add(jsonObject);
        });

        map.put("user_list", userList);

        clients.forEach((k, v) -> {
            try {
                v.getBasicRemote().sendText(JSONObject.toJSONString(map));
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

}