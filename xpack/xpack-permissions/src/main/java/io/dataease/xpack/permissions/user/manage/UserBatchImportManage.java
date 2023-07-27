package io.dataease.xpack.permissions.user.manage;

import io.dataease.api.permissions.user.vo.UserImportVO;
import io.dataease.exception.DEException;
import io.dataease.utils.CommonExcelUtils;
import io.dataease.utils.IDUtils;
import io.dataease.utils.LogUtil;
import io.dataease.utils.ServletUtils;
import io.dataease.xpack.permissions.user.entity.ExcelUserDTO;
import io.dataease.xpack.permissions.user.listener.ImportAnalysisEventListener;
import jakarta.annotation.Resource;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Component
public class UserBatchImportManage {

    protected final Logger logger = LoggerFactory.getLogger(getClass());
    private static final String templatePath = "excel/user.xlsx";

    @Resource
    private ResourceLoader resourceLoader;

    @Resource
    private ExcelCheckManage excelCheckManage;

    public void templateDown() {
        HttpServletResponse response = ServletUtils.response();
        org.springframework.core.io.Resource resource = resourceLoader.getResource("classpath:" + templatePath);
        try {
            downFile(resource.getInputStream(), response, resource.contentLength());
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            DEException.throwException(e.getMessage());
        }
    }

    private void downFile(InputStream inputStream, HttpServletResponse response, Long len) throws Exception {
        ServletOutputStream outputStream = null;
        BufferedInputStream bis = null;
        String filename = "user.xlsx";
        response.reset();
        response.addHeader("responseType", "blob");
        response.addHeader("Access-Control-Allow-Origin", "*");
        response.setContentType("application/vnd.ms-excel;charset=UTF-8");
        response.addHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(filename, StandardCharsets.UTF_8));
        response.addHeader("Content-Length", String.valueOf(len));
        outputStream = response.getOutputStream();
        byte[] buff = new byte[1024];

        // 读取文件
        bis = new BufferedInputStream(inputStream);
        int i = bis.read(buff);
        while (i != -1) {
            outputStream.write(buff, 0, buff.length);
            outputStream.flush();
            i = bis.read(buff);
        }
        bis.close();
        if (outputStream != null) {
            outputStream.close();
        }
        inputStream.close();
    }

    private static final Long MAXSIZE = 10485760L;

    public UserImportVO upload(MultipartFile file) {

        if (file.getSize() > MAXSIZE) {
            String msgKey = "i18n_max_user_import_size";
            DEException.throwException(msgKey);
        }
        byte[] byteArr = null;
        try {
            UserImportVO vo = new UserImportVO(IDUtils.randomID(16));
            byteArr = file.getBytes();
            InputStream inputStream = new ByteArrayInputStream(byteArr);
            CommonExcelUtils.importExcel(inputStream, ExcelUserDTO.class, new ImportAnalysisEventListener(excelCheckManage, ExcelUserDTO.class, vo));
            return vo;
        } catch (Exception e) {
            LogUtil.error(e.getMessage(), e);
            if (StringUtils.contains(e.getMessage(), "template file error")) {
                DEException.throwException("I18N_USER_TEMPLATE_ERROR");
            } else {
                DEException.throwException(e.getMessage());
            }
        }
        return null;
    }
}
