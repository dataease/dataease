package io.dataease.plugins.server;

import io.dataease.auth.annotation.DeLog;
import io.dataease.commons.constants.SysLogConstants;
import io.dataease.commons.utils.AuthUtils;
import io.dataease.plugins.config.SpringContextUtil;
import io.dataease.plugins.xpack.ukey.dto.request.XpackUkeyDto;
import io.dataease.plugins.xpack.ukey.service.UkeyXpackService;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.servlet.ServletRequest;
import java.util.List;

@ApiIgnore
@RequestMapping("/plugin/ukey")
@RestController
public class XUserKeysServer {


    @PostMapping("info")
    public List<XpackUkeyDto> getUserKeysInfo() {
        UkeyXpackService ukeyXpackService = SpringContextUtil.getBean(UkeyXpackService.class);
        Long userId = AuthUtils.getUser().getUserId();
        return ukeyXpackService.getUserKeysInfo(userId);
    }

    @PostMapping("validate")
    public String validate(ServletRequest request) {
        return null;
    }

    @DeLog(
            operatetype = SysLogConstants.OPERATE_TYPE.CREATE,
            sourcetype = SysLogConstants.SOURCE_TYPE.APIKEY,
            value = "id"
    )
    @PostMapping("generate")
    public void generateUserKey(@RequestBody XpackUkeyDto ukeyDto) {
        UkeyXpackService ukeyXpackService = SpringContextUtil.getBean(UkeyXpackService.class);
        Long userId = AuthUtils.getUser().getUserId();
        XpackUkeyDto xpackUkeyDto = ukeyXpackService.generateUserKey(userId);
        ukeyDto.setId(xpackUkeyDto.getId());
    }

    @DeLog(
            operatetype = SysLogConstants.OPERATE_TYPE.DELETE,
            sourcetype = SysLogConstants.SOURCE_TYPE.APIKEY
    )
    @PostMapping("delete/{id}")
    public void deleteUserKey(@PathVariable Long id) {
        UkeyXpackService ukeyXpackService = SpringContextUtil.getBean(UkeyXpackService.class);
        ukeyXpackService.deleteUserKey(id);
    }

    @DeLog(
            operatetype = SysLogConstants.OPERATE_TYPE.MODIFY,
            sourcetype = SysLogConstants.SOURCE_TYPE.APIKEY
    )
    @PostMapping("changeStatus/{id}")
    public void changeStatus(@PathVariable Long id) {
        UkeyXpackService ukeyXpackService = SpringContextUtil.getBean(UkeyXpackService.class);
        ukeyXpackService.switchStatus(id);
    }


}
