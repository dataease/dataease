package io.dataease.service.dataset;

import io.dataease.commons.constants.ParamConstants;
import io.dataease.commons.utils.AuthUtils;
import io.dataease.commons.utils.ServletUtils;
import io.dataease.controller.dataset.request.DataSetTaskInstanceGridRequest;
import io.dataease.dto.dataset.DataSetTaskDTO;
import io.dataease.dto.dataset.DataSetTaskLogDTO;
import io.dataease.ext.ExtDataSetTaskMapper;
import io.dataease.i18n.Translator;
import io.dataease.plugins.common.base.domain.DatasetTableTaskLog;
import io.dataease.plugins.common.base.domain.DatasetTableTaskLogExample;
import io.dataease.plugins.common.base.mapper.DatasetTableTaskLogMapper;
import io.dataease.plugins.common.base.mapper.DatasetTableTaskMapper;
import io.dataease.plugins.common.exception.DataEaseException;
import io.dataease.plugins.common.util.GlobalDateUtils;
import io.dataease.service.system.SystemParameterService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * @Author gin
 * @Date 2021/3/4 1:26 下午
 */
@Service
public class DataSetTableTaskLogService {
    @Resource
    private DatasetTableTaskLogMapper datasetTableTaskLogMapper;
    @Resource
    private ExtDataSetTaskMapper extDataSetTaskMapper;
    @Resource
    private DatasetTableTaskMapper datasetTableTaskMapper;
    @Resource
    private SystemParameterService systemParameterService;

    public DatasetTableTaskLog save(DatasetTableTaskLog datasetTableTaskLog, Boolean hasTask) {
        if (hasTask && datasetTableTaskMapper.selectByPrimaryKey(datasetTableTaskLog.getTaskId()) == null) {
            return datasetTableTaskLog;
        }
        if (StringUtils.isEmpty(datasetTableTaskLog.getId())) {
            datasetTableTaskLog.setId(UUID.randomUUID().toString());
            datasetTableTaskLog.setCreateTime(System.currentTimeMillis());
            datasetTableTaskLogMapper.insert(datasetTableTaskLog);
        } else {
            datasetTableTaskLogMapper.updateByPrimaryKeySelective(datasetTableTaskLog);
        }
        return datasetTableTaskLog;
    }

    public void delete(String id) {
        datasetTableTaskLogMapper.deleteByPrimaryKey(id);
    }

    public void exportExcel(DataSetTaskInstanceGridRequest request) throws Exception {
        HttpServletResponse response = ServletUtils.response();
        OutputStream outputStream = response.getOutputStream();
        try {
            List<DataSetTaskLogDTO> taskLogDTOS = listTaskLog(request, "notexcel");
            List<String[]> details = taskLogDTOS.stream().map(item -> {
                String[] row = new String[5];
                row[0] = item.getName();
                row[1] = item.getDatasetName();
                try {
                    row[2] = GlobalDateUtils.getTimeString(item.getStartTime());
                    row[3] = item.getEndTime() != null ? GlobalDateUtils.getTimeString(item.getEndTime()) : "";
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                row[4] = Translator.get("I18N_TASK_LOG_" + item.getStatus().toUpperCase());
                return row;
            }).collect(Collectors.toList());
            String[] headArr = {Translator.get("I18N_TASK_NAME"), Translator.get("I18N_DATASET"), Translator.get("I18N_START_TIME"), Translator.get("I18N_END_TIME"), Translator.get("I18N_STATUS")};
            details.add(0, headArr);

            HSSFWorkbook wb = new HSSFWorkbook();
            //明细sheet
            HSSFSheet detailsSheet = wb.createSheet(Translator.get("I18N_DATA"));

            //给单元格设置样式
            CellStyle cellStyle = wb.createCellStyle();
            Font font = wb.createFont();
            //设置字体大小
            font.setFontHeightInPoints((short) 12);
            //设置字体加粗
            font.setBold(true);
            //给字体设置样式
            cellStyle.setFont(font);
            //设置单元格背景颜色
            cellStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            //设置单元格填充样式(使用纯色背景颜色填充)
            cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            if (CollectionUtils.isNotEmpty(details)) {
                for (int i = 0; i < details.size(); i++) {
                    HSSFRow row = detailsSheet.createRow(i);
                    String[] rowData = details.get(i);
                    if (rowData != null) {
                        for (int j = 0; j < rowData.length; j++) {
                            HSSFCell cell = row.createCell(j);
                            cell.setCellValue(rowData[j]);
                            if (i == 0) {// 头部
                                cell.setCellStyle(cellStyle);
                                //设置列的宽度
                                detailsSheet.setColumnWidth(j, 255 * 20);
                            }
                        }
                    }
                }
            }

            response.setContentType("application/vnd.ms-excel");
            //文件名称
            String fileName = "DataEase " + Translator.get("I18N_SYNC_LOG");
            String encodeFileName = URLEncoder.encode(fileName, "UTF-8");
            response.setHeader("Content-disposition", "attachment;filename=" + encodeFileName + ".xls");
            wb.write(outputStream);
            outputStream.flush();
            outputStream.close();
        } catch (Exception e) {
            DataEaseException.throwException(e);
        }
    }


    public List<DataSetTaskLogDTO> listTaskLog(DataSetTaskInstanceGridRequest request, String type) {
        if (!type.equalsIgnoreCase("excel")) {
            request.setExcludedIdList(List.of("初始导入", "替换", "追加"));
        }
        if (AuthUtils.getUser().getIsAdmin()) {
            List<DataSetTaskLogDTO> dataSetTaskLogDTOS = extDataSetTaskMapper.listTaskLog(request);
            dataSetTaskLogDTOS.forEach(dataSetTaskLogDTO -> {
                if (StringUtils.isEmpty(dataSetTaskLogDTO.getName())) {
                    dataSetTaskLogDTO.setName(dataSetTaskLogDTO.getTaskId());
                }
            });
            return dataSetTaskLogDTOS;
        } else {
            List<DataSetTaskLogDTO> dataSetTaskLogDTOS = extDataSetTaskMapper.listUserTaskLog(request);
            dataSetTaskLogDTOS.forEach(dataSetTaskLogDTO -> {
                if (StringUtils.isEmpty(dataSetTaskLogDTO.getName())) {
                    dataSetTaskLogDTO.setName(dataSetTaskLogDTO.getTaskId());
                }
            });
            return dataSetTaskLogDTOS;
        }

    }

    public void deleteByTaskId(String taskId) {
        DatasetTableTaskLogExample datasetTableTaskLogExample = new DatasetTableTaskLogExample();
        DatasetTableTaskLogExample.Criteria criteria = datasetTableTaskLogExample.createCriteria();
        criteria.andTaskIdEqualTo(taskId);
        datasetTableTaskLogMapper.deleteByExample(datasetTableTaskLogExample);
    }

    public List<DatasetTableTaskLog> getByTableId(String datasetId) {
        DatasetTableTaskLogExample datasetTableTaskLogExample = new DatasetTableTaskLogExample();
        DatasetTableTaskLogExample.Criteria criteria = datasetTableTaskLogExample.createCriteria();
        criteria.andTableIdEqualTo(datasetId);
        return datasetTableTaskLogMapper.selectByExampleWithBLOBs(datasetTableTaskLogExample);
    }

    public List<DatasetTableTaskLog> select(DatasetTableTaskLog datasetTableTaskLog) {
        DatasetTableTaskLogExample example = getDatasetTableTaskLogExample(datasetTableTaskLog);
        example.setOrderByClause("create_time desc");
        return datasetTableTaskLogMapper.selectByExampleWithBLOBs(example);
    }

    public DataSetTaskDTO lastExecStatus(DataSetTaskDTO dataSetTaskDTO) {
        DatasetTableTaskLogExample example = new DatasetTableTaskLogExample();
        DatasetTableTaskLogExample.Criteria criteria = example.createCriteria();
        if (StringUtils.isNotEmpty(dataSetTaskDTO.getTableId())) {
            criteria.andTableIdEqualTo(dataSetTaskDTO.getTableId());
        }
        if (StringUtils.isNotEmpty(dataSetTaskDTO.getId())) {
            criteria.andTaskIdEqualTo(dataSetTaskDTO.getId());
        }
        example.setOrderByClause("create_time desc");
        List<DatasetTableTaskLog> datasetTableTaskLogs = datasetTableTaskLogMapper.selectByExampleWithBLOBs(example);
        if (CollectionUtils.isNotEmpty(datasetTableTaskLogs)) {
            dataSetTaskDTO.setLastExecStatus(datasetTableTaskLogs.get(0).getStatus());
            dataSetTaskDTO.setLastExecTime(datasetTableTaskLogs.get(0).getCreateTime());
            dataSetTaskDTO.setMsg(datasetTableTaskLogs.get(0).getInfo());
        }
        return dataSetTaskDTO;
    }

    private DatasetTableTaskLogExample getDatasetTableTaskLogExample(DatasetTableTaskLog datasetTableTaskLog) {
        DatasetTableTaskLogExample example = new DatasetTableTaskLogExample();
        DatasetTableTaskLogExample.Criteria criteria = example.createCriteria();
        if (StringUtils.isNotEmpty(datasetTableTaskLog.getStatus())) {
            criteria.andStatusEqualTo(datasetTableTaskLog.getStatus());
        }
        if (StringUtils.isNotEmpty(datasetTableTaskLog.getTableId())) {
            criteria.andTableIdEqualTo(datasetTableTaskLog.getTableId());
        }
        if (StringUtils.isNotEmpty(datasetTableTaskLog.getTaskId())) {
            criteria.andTaskIdEqualTo(datasetTableTaskLog.getTaskId());
        }
        return example;
    }

    private static final String LOG_RETENTION = "30";

    public void cleanLog() {
        String value = systemParameterService.getValue(ParamConstants.BASIC.DS_SYNC_LOG_TIME_OUT.getValue());
        value = StringUtils.isBlank(value) ? LOG_RETENTION : value;
        int logRetention = Integer.parseInt(value);
        Calendar instance = Calendar.getInstance();
        Calendar startInstance = (Calendar) instance.clone();
        startInstance.add(Calendar.DATE, -logRetention);
        startInstance.set(Calendar.HOUR_OF_DAY, 0);
        startInstance.set(Calendar.MINUTE, 0);
        startInstance.set(Calendar.SECOND, 0);
        startInstance.set(Calendar.MILLISECOND, -1);
        long timeInMillis = startInstance.getTimeInMillis();
        DatasetTableTaskLogExample example = new DatasetTableTaskLogExample();
        example.createCriteria().andCreateTimeLessThan(timeInMillis);
        datasetTableTaskLogMapper.deleteByExample(example);
    }

}
