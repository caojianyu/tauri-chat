package cn.zyx.chat.service;

import cn.zyx.chat.entity.UcAccount;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author caojianyu
 * @since 2023-01-04
 */
public interface IUcAccountService extends IService<UcAccount> {

    List<UcAccount> queryFriends(int accountId);
}
