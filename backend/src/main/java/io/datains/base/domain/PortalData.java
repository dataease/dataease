package io.datains.base.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 数据门户
 * </p>
 *
 * @author Mr.zhang
 * @since 2022-06-02
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class PortalData implements Serializable {

    private static final long serialVersionUID=1L;

    private Long id;

    /**
     * 创建人id
     */
    private Long userId;

    /**
     * 创建人名称
     */
    private String userName;

    /**
     * 更新人
     */
    private String updateBy;

    /**
     * 修改时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime updateTime;

    private String positionJson;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;


}
