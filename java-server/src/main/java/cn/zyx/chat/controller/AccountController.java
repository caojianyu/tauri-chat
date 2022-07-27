package cn.zyx.chat.controller;

import cn.zyx.chat.common.annotation.Account;
import cn.zyx.chat.common.annotation.JwtToken;
import cn.zyx.chat.entity.UcAccount;
import cn.zyx.chat.utils.JwtUtil;
import cn.zyx.chat.utils.R;
import com.alibaba.fastjson.JSONObject;
import io.netty.util.internal.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * @author caojianyu
 * @version 1.0.0
 * @date 2022-07-14 13:17
 * @email jieni_cao@foxmail.com
 */
@RestController
@RequestMapping("/app")
public class AccountController {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;


    @PostMapping("/register")
    public R register(@RequestBody UcAccount account) {
        account.setId(UUID.randomUUID().toString());
        account.setSalt("zyx");
        account.setPassword(DigestUtils.md5DigestAsHex((account.getPassword()+account.getSalt()).getBytes()));
        ValueOperations<String, String> valueOperations = stringRedisTemplate.opsForValue();
        valueOperations.set(account.getAccount(), JSONObject.toJSONString(account));
        return R.ok();
    }

    @PostMapping("/login")
    public R login(@RequestBody UcAccount account) {
        ValueOperations<String, String> valueOperations = stringRedisTemplate.opsForValue();
        String accountStr = valueOperations.get(account.getAccount());
        if (StringUtil.isNullOrEmpty(accountStr)) {
            return R.error("用户名不存在");
        }

        UcAccount ucAccount = JSONObject.parseObject(accountStr, UcAccount.class);
        if (ucAccount.getPassword().equals(DigestUtils.md5DigestAsHex((account.getPassword()+ucAccount.getSalt()).getBytes()))) {
            String token = jwtUtil.generateToken(ucAccount.getAccount());
            return R.ok().put("data", token);
        }
        return R.error("账号或密码错误");
    }

    @JwtToken
    @GetMapping("/getAccountInfo")
    public R getAccountInfo(@Account UcAccount account) {
        account.setSalt(null);
        account.setPassword(null);
        return R.ok().put("data", account);
    }
}
