package io.dataease.plugins.server;

import io.dataease.commons.constants.AuthConstants;
import io.dataease.commons.utils.LogUtil;
import io.dataease.plugins.config.SpringContextUtil;
import io.dataease.plugins.xpack.user.service.UserXpackService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;


@Api(tags = "xpack：用户导入")
@RequestMapping("/plugin/user")
@RestController
public class XUserServer {

    private static final Long MAXSIZE = 10485760L;
    @ApiOperation("下载模版")
    @PostMapping("/template")
    public void template(HttpServletResponse response) {
        UserXpackService userXpackService = SpringContextUtil.getBean(UserXpackService.class);
        userXpackService.templateDown(response);
    }

    @ApiOperation("导入")
    @PostMapping("/upload")
    public void upload(@RequestPart(value = "file", required = true) MultipartFile file, HttpServletResponse response) {
        if (file.getSize() > MAXSIZE) {
            String msgKey = "i18n_max_user_import_size";
            response.addHeader("Access-Control-Expose-Headers", AuthConstants.DE_DOWN_ERROR_KEY);
            response.setHeader(AuthConstants.DE_DOWN_ERROR_KEY, msgKey);
            return;
        }
        UserXpackService userXpackService = SpringContextUtil.getBean(UserXpackService.class);
        try{
            userXpackService.upload(file, response);
        }catch (Exception e) {
            response.addHeader("Access-Control-Expose-Headers", AuthConstants.DE_DOWN_ERROR_KEY);
            if (StringUtils.contains(e.getMessage(), "template file error")) {
                response.setHeader(AuthConstants.DE_DOWN_ERROR_KEY, "I18N_USER_TEMPLATE_ERROR");
            }else {
                response.setHeader(AuthConstants.DE_DOWN_ERROR_KEY, e.getMessage());
            }
            LogUtil.error(e.getMessage());
        }
    }
}
