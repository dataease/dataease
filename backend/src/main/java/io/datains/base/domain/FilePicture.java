package io.datains.base.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.List;

/**
 * <p>
 * 图片表
 * </p>
 *
 * @author Mr.zhang
 * @since 2022-07-11
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class FilePicture implements Serializable {

    private static final long serialVersionUID=1L;

    private Long id;

    /**
     * 图片地址
     */
    private String url;

    /**
     * 图片类型
     */
    private Integer type;

    /**
     * 图片详细信息
     */
    private String imgDetailed;

    /**
     * 图片详细信息
     */
    private String name;


    /**
     *图片地址
     */
    private List<FilePicture> str;

}
