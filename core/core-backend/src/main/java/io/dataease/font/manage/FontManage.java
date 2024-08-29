package io.dataease.font.manage;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import io.dataease.api.font.dto.FontDto;
import io.dataease.chart.dao.auto.entity.CoreChartView;
import io.dataease.exception.DEException;
import io.dataease.font.dao.auto.entity.CoreFont;
import io.dataease.font.dao.auto.mapper.CoreFontMapper;
import io.dataease.utils.BeanUtils;
import io.dataease.utils.FileUtils;
import io.dataease.utils.IDUtils;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.ServletOutputStream;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.core.io.ResourceLoader;

import java.io.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Component
public class FontManage {

    private static String path = "/opt/dataease2.0/data/font/";
    @Resource
    private CoreFontMapper coreFontMapper;
    @Autowired
    private ResourceLoader resourceLoader;

    public List<FontDto> list(FontDto fontDto) {
        QueryWrapper<CoreFont> queryWrapper = new QueryWrapper<>();
        List<CoreFont> coreFonts = coreFontMapper.selectList(queryWrapper);
        List<FontDto> fontDtos = new ArrayList<>();
        for (CoreFont coreFont : coreFonts) {
            FontDto dto = new FontDto();
            BeanUtils.copyBean(dto, coreFont);
            fontDtos.add(dto);
        }

        return fontDtos;
    }

    public FontDto create(FontDto fontDto) {
        QueryWrapper<CoreFont> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("name", fontDto.getName());
        if (CollectionUtils.isNotEmpty(coreFontMapper.selectList(queryWrapper))) {
            DEException.throwException("存在重名字库");
        }
        fontDto.setId(IDUtils.snowID());
        CoreFont coreFont = new CoreFont();
        BeanUtils.copyBean(coreFont, fontDto);
        coreFont.setUpdateTime(System.currentTimeMillis());
        coreFontMapper.insert(coreFont);
        return fontDto;
    }


    public FontDto edit(FontDto fontDto) {
        if (ObjectUtils.isEmpty(fontDto.getId())) {
            return create(fontDto);
        }
        if (fontDto.getIsDefault()) {
            UpdateWrapper<CoreFont> updateWrapper = new UpdateWrapper<>();
            updateWrapper.ne("id", fontDto.getId());
            CoreFont record = new CoreFont();
            record.setIsDefault(false);
            coreFontMapper.update(record, updateWrapper);
        }
        CoreFont coreFont = new CoreFont();
        BeanUtils.copyBean(coreFont, fontDto);
        coreFont.setUpdateTime(System.currentTimeMillis());
        coreFontMapper.updateById(coreFont);
        return fontDto;
    }

    public void delete(Long id) {
        CoreFont coreFont = coreFontMapper.selectById(id);
        if (coreFont != null) {
            coreFontMapper.deleteById(id);
            if (StringUtils.isNotEmpty(coreFont.getFileTransName())) {
                FileUtils.deleteFile(path + coreFont.getFileTransName());
            }
        }

    }

    public void changeDefault(FontDto fontDto) {
        QueryWrapper<CoreFont> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("id", fontDto.getId());
        CoreFont record = new CoreFont();
        record.setIsDefault(fontDto.getIsDefault());
        coreFontMapper.update(record, queryWrapper);
    }

    public FontDto upload(MultipartFile file) {
        String fileUuid = UUID.randomUUID().toString();
        return saveFile(file, fileUuid);
    }

    public void download(String file, HttpServletResponse response) {

        QueryWrapper<CoreFont> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("file_trans_name", file);
        List<CoreFont> coreFonts = coreFontMapper.selectList(queryWrapper);
        if (CollectionUtils.isEmpty(coreFonts)) {
            DEException.throwException("不存在的字库文件");
        }

        try {
            response.setContentType("application/x-download");
            response.setHeader("Content-Disposition", "attachment;filename=" + coreFonts.get(0).getFileTransName());
            try (ServletOutputStream out = response.getOutputStream();
                 InputStream stream = new FileInputStream(path + coreFonts.get(0).getFileTransName())) {
                byte buff[] = new byte[1024];
                int length;
                while ((length = stream.read(buff)) > 0) {
                    out.write(buff, 0, length);
                }
                out.flush();
            }
        } catch (IOException e) {
            DEException.throwException(e.getMessage());
        }
    }

    public List<FontDto> defaultFont() {
        QueryWrapper<CoreFont> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("is_default", 1);
        List<CoreFont> coreFonts = coreFontMapper.selectList(queryWrapper);
        List<FontDto> fontDtos = new ArrayList<>();
        for (CoreFont coreFont : coreFonts) {
            FontDto dto = new FontDto();
            BeanUtils.copyBean(dto, coreFont);
            fontDtos.add(dto);
        }
        return fontDtos;
    }

    private static FontDto saveFile(MultipartFile file, String fileNameUUID) throws DEException {
        FontDto fontDto = new FontDto();
        try {
            String filename = file.getOriginalFilename();
            if (StringUtils.isEmpty(filename) || !filename.endsWith(".ttf")) {
                DEException.throwException("非法格式的文件！");
            }
            String suffix = filename.substring(filename.lastIndexOf(".") + 1);
            String filePath = path + fileNameUUID + "." + suffix;
            File f = new File(filePath);
            FileOutputStream fileOutputStream = new FileOutputStream(f);
            fileOutputStream.write(file.getBytes());
            fileOutputStream.flush();
            fileOutputStream.close();
            fontDto.setFileTransName(fileNameUUID + "." + suffix);

            long length = file.getSize();
            String unit = "MB";
            Double size = 0.0;
            if ((double) length / 1024 / 1024 > 1) {
                if ((double) length / 1024 / 1024 / 1024 > 1) {
                    unit = "GB";
                    size = Double.valueOf(String.format("%.2f", (double) length / 1024 / 1024 / 1024));
                } else {
                    size = Double.valueOf(String.format("%.2f", (double) length / 1024 / 1024));
                }
            } else {
                unit = "KB";
                size = Double.valueOf(String.format("%.2f", (double) length / 1024));
            }
            fontDto.setSize(size);
            fontDto.setSizeType(unit);

        } catch (Exception e) {
            DEException.throwException(e);
        }
        return fontDto;
    }

}
