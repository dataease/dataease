package io.dataease.font.manage;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.dataease.api.font.dto.FontDto;
import io.dataease.exception.DEException;
import io.dataease.font.dao.auto.entity.CoreFont;
import io.dataease.font.dao.auto.mapper.CoreFontMapper;
import io.dataease.utils.BeanUtils;
import io.dataease.utils.FileUtils;
import io.dataease.utils.IDUtils;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.ObjectUtils;
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
            FileUtils.deleteFile(path + coreFont.getFileTransName());
        }

    }

    public void changeDefault(FontDto fontDto) {
        QueryWrapper<CoreFont> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("id", fontDto.getId());
        CoreFont record = new CoreFont();
        record.setIsDefault(fontDto.getIsDefault());
        coreFontMapper.update(record, queryWrapper);
    }

    public String upload(MultipartFile file) {
        String fileUuid = UUID.randomUUID().toString();
        return saveFile(file, fileUuid);
    }

    public void download(Long id, HttpServletResponse response) {
        CoreFont coreFont = coreFontMapper.selectById(id);
        try {
            response.setContentType("application/x-download");
            response.setHeader("Content-Disposition", "attachment;filename=" + coreFont.getFileTransName());
            try (ServletOutputStream out = response.getOutputStream();
                 InputStream stream = new FileInputStream(path + coreFont.getFileTransName())) {
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

    private static String saveFile(MultipartFile file, String fileNameUUID) throws DEException {
        String fileTransName = "";
        try {
            String filename = file.getOriginalFilename();
            String suffix = filename.substring(filename.lastIndexOf(".") + 1);
            String filePath = path + fileNameUUID + "." + suffix;
            File f = new File(filePath);
            FileOutputStream fileOutputStream = new FileOutputStream(f);
            fileOutputStream.write(file.getBytes());
            fileOutputStream.flush();
            fileOutputStream.close();
            fileTransName = fileNameUUID + "." + suffix;
        } catch (Exception e) {
            DEException.throwException(e);
        }
        return fileTransName;
    }

}
