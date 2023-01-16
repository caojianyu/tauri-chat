package cn.zyx.chat.controller;

import cn.zyx.chat.common.annotation.Account;
import cn.zyx.chat.common.annotation.JwtToken;
import cn.zyx.chat.entity.UcAccount;
import cn.zyx.chat.service.IUcAccountService;
import cn.zyx.chat.utils.JwtUtil;
import cn.zyx.chat.utils.R;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author caojianyu
 * @version 1.0.0
 * @date 2022-07-14 13:17
 * @email jieni_cao@foxmail.com
 */
@RestController
@RequestMapping("/app")
public class UcAccountController {

    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private IUcAccountService accountService;

    @PostMapping("/register")
    public R register(@RequestBody UcAccount account) {
        String salt = RandomStringUtils.randomAlphanumeric(20);
        account.setSalt(salt);
        account.setPassword(DigestUtils.md5DigestAsHex((account.getPassword() + account.getSalt()).getBytes()));
        account.setCreateTime(LocalDateTime.now());
        accountService.save(account);
        return R.ok();
    }

    @PostMapping("/login")
    public R login(@RequestBody UcAccount account) {
        UcAccount ucAccount = accountService.getOne(new LambdaQueryWrapper<UcAccount>()
                .eq(UcAccount::getAccount, account.getAccount()));
        if (ucAccount == null) {
            return R.error("用户不存在");
        }
        String password = DigestUtils.md5DigestAsHex((account.getPassword() + ucAccount.getSalt()).getBytes());
        if (ucAccount.getPassword().equals(password)) {
            String token = jwtUtil.generateToken(ucAccount.getId());
            return R.ok().put("data", token);
        }
        return R.error("账号或密码错误");
    }

    @JwtToken
    @GetMapping("/getUserInfo")
    public R getUserInfo(@Account UcAccount account) {
        account.setSalt(null);
        account.setPassword(null);
        return R.ok().put("data", account);
    }

    @JwtToken
    @GetMapping("/getUserList")
    public R getUserList(@Account UcAccount account) {
        List<UcAccount> userList = accountService.queryFriends(account.getId());
        return R.ok().put("data", userList);
    }
}
