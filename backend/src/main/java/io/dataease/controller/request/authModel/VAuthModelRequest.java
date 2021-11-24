package io.dataease.controller.request.authModel;

import io.dataease.dto.authModel.VAuthModelDTO;
import lombok.Data;

/**
 * Author: wangjiahao
 * Date: 2021/11/24
 * Description:
 */
@Data
public class VAuthModelRequest extends VAuthModelDTO {

    private String userId;

}
