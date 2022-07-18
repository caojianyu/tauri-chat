package cn.zyx.chat.entity;

import lombok.Data;

/**
 * @author caojianyu
 * @version 1.0.0
 * @date 2022-07-14 13:15
 * @email jieni_cao@foxmail.com
 */
@Data
public class UcAccount {

    private String id;
    private String account;
    private String avatar;
    private String password;
    private String salt;
}
