package io.dataease.map.service;


import com.google.gson.Gson;
import io.dataease.plugins.common.base.domain.ChartView;
import io.dataease.plugins.common.base.domain.ChartViewExample;
import io.dataease.plugins.common.base.domain.ChartViewWithBLOBs;
import io.dataease.plugins.common.base.mapper.ChartViewMapper;
import io.dataease.plugins.common.util.FileUtil;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class MapTransferService {

    @Value("${geo.rootpath:/opt/dataease/data/feature/}")
    private String geoPath;


    @Value("${geo.custom.rootpath:/opt/dataease/data/custom/}")
    private String customGeoPath;

    private static final List<String> MATCH_TYPES = new ArrayList<>();
    private static final Gson gson = new Gson();
    private static final String AREA_CODE_KEY = "areaCode";
    private static final String GLOBAL_CHINA_PREFIX = "156";

    private static final String FILE_SEPARATOR = "/";
    private static final String FULL_KEY = "full";
    private static final String BORDER_KEY = "border";

    private static final String FULL_FILE_SUFFIX = "_full.json";

    private static final List<String> coverFileNameList = new ArrayList<>();


    @PostConstruct
    public void init() {
        MATCH_TYPES.add("map");
        MATCH_TYPES.add("buddle-map");
        coverFileNameList.add("350200_full.json");
    }

    @Resource
    private ChartViewMapper chartViewMapper;


    public void execute() {
        ChartViewExample example = new ChartViewExample();
        List<ChartViewWithBLOBs> chartViews = chartViewMapper.selectByExampleWithBLOBs(example);
        chartViews.forEach(view -> {
            if (typeMatch(view) && StringUtils.isNotBlank(view.getCustomAttr())) {
                Map<String, Object> customAttrMap = convert(view.getCustomAttr());
                if (customMatch(customAttrMap)) {
                    view.setCustomAttr(gson.toJson(customAttrMap));
                    chartViewMapper.updateByPrimaryKeyWithBLOBs(view);
                }
            }
        });
        moveMapFiles();
    }

    public void moveMapFiles() {
        String chinaRootPath = geoPath + FULL_KEY + FILE_SEPARATOR;
        File chinaRootDir = new File(chinaRootPath);
        File[] files = chinaRootDir.listFiles();
        if (ArrayUtils.isEmpty(files)) return;
        Map<String, List<File>> listMap = Arrays.stream(files).filter(FileUtil::isFile).collect(Collectors.groupingBy(this::fileType));
        if (ObjectUtils.isEmpty(listMap)) return;
        moveFiles(listMap, BORDER_KEY);
        moveFiles(listMap, FULL_KEY);
        moveGlobalFile();

    }

    private void moveFiles(Map<String, List<File>> listMap, String fileType) {
        String dirPath = customGeoPath + fileType + FILE_SEPARATOR;
        Optional.ofNullable(listMap.get(fileType)).ifPresent(files -> {
            files.forEach(file -> {
                String fileName = file.getName();
                String newFilePath = dirPath + GLOBAL_CHINA_PREFIX + FILE_SEPARATOR + GLOBAL_CHINA_PREFIX + fileName;
                File target = new File(newFilePath);
                if (coverFileNameList.contains(fileName) || !target.exists()) {
                    FileUtil.move(file, target, true);
                }
            });
        });
    }

    private void moveGlobalFile() {
        String fileName = "000000000" + FULL_FILE_SUFFIX;
        String sourcePath = geoPath + FULL_KEY + FILE_SEPARATOR + "000" + FILE_SEPARATOR + fileName;
        File sourceFile = new File(sourcePath);
        if (!sourceFile.exists()) return;

        String targetDirPath = customGeoPath + FULL_KEY + FILE_SEPARATOR + "000" + FILE_SEPARATOR;
        File targetDir = new File(targetDirPath);
        if (!targetDir.exists()) {
            targetDir.mkdirs();
        }
        String targetPath = targetDirPath + fileName;

        File targetFile = new File(targetPath);
        if (!targetFile.exists()) {
            FileUtil.move(sourceFile, targetFile, true);
        }


    }

    private String fileType(File file) {
        return file.getName().endsWith(FULL_FILE_SUFFIX) ? FULL_KEY : BORDER_KEY;
    }

    private Boolean typeMatch(ChartView chartView) {
        return MATCH_TYPES.stream().anyMatch(type -> StringUtils.equals(type, chartView.getType()));
    }

    private Map<String, Object> convert(String customJson) {
        return gson.fromJson(customJson, Map.class);
    }

    private Boolean customMatch(Map<String, Object> customAttrMap) {
        Object codeObj = null;
        if ((codeObj = customAttrMap.get(AREA_CODE_KEY)) != null) {
            String code = codeObj.toString();
            boolean matych = code.length() == 6;
            customAttrMap.put(AREA_CODE_KEY, GLOBAL_CHINA_PREFIX + code);
            return matych;
        }
        return false;
    }
}
