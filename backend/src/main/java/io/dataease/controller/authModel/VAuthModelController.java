package io.dataease.controller.authModel;

import com.github.xiaoymin.knife4j.annotations.ApiSupport;
import io.swagger.annotations.Api;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Author: wangjiahao
 * Date: 2021/11/5
 * Description:
 */
@Api(tags = "授权树：授权树模型")
@ApiSupport(order = 80)
@RestController
@RequestMapping("authModel")
public class VAuthModelController {

}
