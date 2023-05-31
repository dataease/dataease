package io.dataease.license.dao.po;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@TableName("license")
@AllArgsConstructor
@NoArgsConstructor
public class LicensePO {

    @TableId
    private Long id;

    private Long updateTime;

    private String license;

    private String f2cLicense;
}
