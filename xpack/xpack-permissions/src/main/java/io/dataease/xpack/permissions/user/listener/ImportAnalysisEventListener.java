package io.dataease.xpack.permissions.user.listener;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.alibaba.excel.exception.ExcelAnalysisException;
import io.dataease.api.permissions.user.vo.UserImportVO;
import io.dataease.exception.DEException;
import io.dataease.utils.*;
import io.dataease.xpack.permissions.user.entity.ExcelCheckResult;
import io.dataease.xpack.permissions.user.entity.ExcelImportErrDto;
import io.dataease.xpack.permissions.user.entity.ExcelUserDTO;
import io.dataease.xpack.permissions.user.helper.ExcelValiHelper;
import io.dataease.xpack.permissions.user.manage.ExcelCheckManage;
import org.apache.commons.collections4.CollectionUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ImportAnalysisEventListener extends AnalysisEventListener<ExcelUserDTO> {
    private final List<ExcelUserDTO> successList = new ArrayList<>();

    private final List<ExcelImportErrDto> errList = new ArrayList<>();

    private static final int BATCH_COUNT = 1000;

    private final List<ExcelUserDTO> list = new ArrayList<>();

    private final Class<ExcelUserDTO> clazz;


    private final ExcelCheckManage excelCheckManage;

    private final UserImportVO vo;

    public ImportAnalysisEventListener(ExcelCheckManage excelCheckManage, Class<ExcelUserDTO> clazz, UserImportVO vo) {
        this.clazz = clazz;
        this.excelCheckManage = excelCheckManage;
        this.vo = vo;
    }

    @Override
    public void invoke(ExcelUserDTO data, AnalysisContext context) {
        if (context.readRowHolder().getRowIndex() == 0) {
            validateHead(data);
            return;
        }
        Map<Integer, String> resultMap = null;
        try {
            resultMap = ExcelValiHelper.validateEntity(data);
        } catch (NoSuchFieldException e) {
            String msg = "第" + context.readRowHolder().getRowIndex() + "行数据解析错误";
            LogUtil.error(msg, new ExcelAnalysisException(msg));
            DEException.throwException(e);
        }
        if (resultMap != null) {
            ExcelImportErrDto excelImportErrDto = new ExcelImportErrDto(data, resultMap);
            errList.add(excelImportErrDto);
        } else {
            list.add(data);
        }

        if (list.size() >= BATCH_COUNT) {
            ExcelCheckResult<ExcelUserDTO> excelCheckResult = excelCheckManage.checkImportExcel(list);
            dealResult(excelCheckResult);
        }
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext analysisContext) {
        ExcelCheckResult<ExcelUserDTO> checkResult = excelCheckManage.checkImportExcel(list);
        dealResult(checkResult);
    }

    private void dealResult(ExcelCheckResult<ExcelUserDTO> checkResult) {
        successList.addAll(checkResult.getSuccessDtos());
        errList.addAll(checkResult.getErrDtos());
        list.clear();
        vo.setSuccessCount(successList.size());
        vo.setErrorCount(errList.size());
        if (errList.size() > 0) {
            CacheUtils.put("import-user-record", vo.getDataKey(), errList);
        }
    }

    private void validateHead(ExcelUserDTO head) {
        if (this.excelCheckManage != null) {
            excelCheckManage.validateHead(head);
        }
    }

    private void exportErrorExcel() throws IOException {
        List<Object> userResultList = errList.stream().map(ExcelImportErrDto::getObject).collect(Collectors.toList());
        List<Map<Integer, String>> errMsgList = errList.stream().map(ExcelImportErrDto::getCellMap).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(userResultList)) {
            CommonExcelUtils.writeExcel(ServletUtils.response(), userResultList, clazz, errMsgList, "error");
        }
    }
}
