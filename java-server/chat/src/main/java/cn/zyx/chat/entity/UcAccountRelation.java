package cn.zyx.chat.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.time.LocalDateTime;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * <p>
 * 
 * </p>
 *
 * @author caojianyu
 * @since 2023-01-05
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class UcAccountRelation implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 用户id（发起人）
     */
    private Integer accountId;

    /**
     * 好友id（被接收人）
     */
    private Integer friendId;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;


}
