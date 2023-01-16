package cn.zyx.chat.service.impl;

import cn.zyx.chat.entity.UcAccount;
import cn.zyx.chat.mapper.UcAccountMapper;
import cn.zyx.chat.service.IUcAccountService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author caojianyu
 * @since 2023-01-04
 */
@Service
public class UcAccountServiceImpl extends ServiceImpl<UcAccountMapper, UcAccount> implements IUcAccountService {

    @Override
    public List<UcAccount> queryFriends(int accountId) {
        return baseMapper.queryFriends(accountId);
    }
}
