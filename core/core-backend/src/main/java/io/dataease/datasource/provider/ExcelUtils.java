package io.dataease.datasource.provider;


import io.dataease.api.dataset.dto.DatasetTableDTO;
import io.dataease.api.ds.vo.TableField;
import io.dataease.datasource.dto.ExcelSheetData;
import io.dataease.datasource.request.DatasourceRequest;
import io.dataease.exception.DEException;
import io.dataease.utils.AuthUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;


public class ExcelUtils {

    private static String path = "/opt/dataease/data/excel/";

    public static List<DatasetTableDTO> getTables(DatasourceRequest datasourceRequest) throws DEException {
        List<DatasetTableDTO> tableDescs = new ArrayList<>();
        return tableDescs;
    }

    public static List<ExcelSheetData> excelSaveAndParse(MultipartFile file, String datasourceId) throws DEException {
        String filename = file.getOriginalFilename();
        List<ExcelSheetData> excelSheetDataList = null;
        try {
            parseExcel(filename, file.getInputStream(), true);
        }catch (Exception e) {
            DEException.throwException(e);
        }
        List<ExcelSheetData> returnSheetDataList = new ArrayList<>();

        if (StringUtils.isNotEmpty(datasourceId)) {
        } else {
            returnSheetDataList = excelSheetDataList;
        }
        returnSheetDataList = returnSheetDataList.stream()
                .filter(excelSheetData -> !CollectionUtils.isEmpty(excelSheetData.getFields()))
                .collect(Collectors.toList());
        // save file
        String excelId = UUID.randomUUID().toString();
        String filePath = saveFile(file, excelId);

        filename = filename.substring(0, filename.lastIndexOf('.'));
        if (returnSheetDataList.size() == 1) {
            returnSheetDataList.get(0).setTableName(filename);
            returnSheetDataList.get(0).setPath(filePath);
        } else {
            for (ExcelSheetData excelSheetData : returnSheetDataList) {
                excelSheetData.setTableName(filename + "-" + excelSheetData.getExcelLabel());
                excelSheetData.setPath(filePath);
            }
        }
        return returnSheetDataList;
    }

    private static String saveFile(MultipartFile file, String fileNameUUID) throws DEException {
        String filePath = null;
        try {
            String filename = file.getOriginalFilename();
            String suffix = filename.substring(filename.lastIndexOf(".") + 1);
            String dirPath = path + AuthUtils.getUser().getUserId() + "/";
            File p = new File(dirPath);
            if (!p.exists()) {
                p.mkdirs();
            }
            filePath = dirPath + fileNameUUID + "." + suffix;
            File f = new File(filePath);
            FileOutputStream fileOutputStream = new FileOutputStream(f);
            fileOutputStream.write(file.getBytes());
            fileOutputStream.flush();
            fileOutputStream.close();
        } catch (Exception e) {
            DEException.throwException(e);
        }
        return filePath;
    }

    private static List<ExcelSheetData> parseExcel(String filename, InputStream inputStream, boolean isPreview) throws DEException {
        List<ExcelSheetData> excelSheetDataList = new ArrayList<>();
        try {
            String suffix = filename.substring(filename.lastIndexOf(".") + 1);
            if (StringUtils.equalsIgnoreCase(suffix, "csv")) {
                List<TableField> fields = new ArrayList<>();
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
                String s = reader.readLine();// first line
                String[] split = s.split(",");
                for (String s1 : split) {
                    TableField tableFiled = new TableField();
                    tableFiled.setName(s1);
                    tableFiled.setOriginName(s1);
                    tableFiled.setFieldType("TEXT");
                    fields.add(tableFiled);
                }
                List<List<String>> data = new ArrayList<>();
                int num = 1;
                String line;
                while ((line = reader.readLine()) != null) {
                    if (isPreview && num > 1000) {
                        break;
                    }
                    data.add(Arrays.asList(line.split(",")));
                    num++;
                }
                ExcelSheetData excelSheetData = new ExcelSheetData();
                String[] fieldArray = fields.stream().map(TableField::getName).toArray(String[]::new);
                excelSheetData.setFields(fields);
                excelSheetData.setData(data);
                excelSheetData.setExcelLabel(filename);
                excelSheetDataList.add(excelSheetData);
            }
            inputStream.close();
            excelSheetDataList.forEach(excelSheetData -> {
                List<List<String>> data = excelSheetData.getData();
                String[] fieldArray = excelSheetData.getFields().stream().map(TableField::getName).toArray(String[]::new);
                List<Map<String, Object>> jsonArray = new ArrayList<>();
                if (!CollectionUtils.isEmpty(data)) {
                    jsonArray = data.stream().map(ele -> {
                        Map<String, Object> map = new HashMap<>();
                        for (int i = 0; i < fieldArray.length; i++) {
                            map.put(fieldArray[i], i < ele.size() ? ele.get(i) : "");
                        }
                        return map;
                    }).collect(Collectors.toList());
                }
            });
        } catch (Exception e) {
            DEException.throwException(e);
        }


        return excelSheetDataList;
    }


}
