package cn.zyx.chat.mapper;

import cn.zyx.chat.entity.UcAccount;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import java.util.List;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author caojianyu
 * @since 2023-01-04
 */
public interface UcAccountMapper extends BaseMapper<UcAccount> {

    List<UcAccount> queryFriends(int accountId);

}
