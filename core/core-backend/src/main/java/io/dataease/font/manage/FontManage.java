package io.dataease.font.manage;

import io.dataease.api.font.dto.FontDto;
import io.dataease.exception.DEException;
import io.dataease.font.dao.auto.entity.CoreFont;
import io.dataease.font.dao.auto.mapper.CoreFontRepository;
import io.dataease.utils.BeanUtils;
import io.dataease.utils.FileUtils;
import io.dataease.utils.IDUtils;
import jakarta.annotation.Resource;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.awt.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
public class FontManage {

    @Value("${dataease.path.font:/opt/dataease2.0/data/font/}")
    private String path;

    @Resource
    private CoreFontRepository coreFontRepository;

    public List<FontDto> list(FontDto fontDto) {
        List<CoreFont> coreFonts = coreFontRepository.findAll();
        List<FontDto> fontDtos = new ArrayList<>();
        for (CoreFont coreFont : coreFonts) {
            FontDto dto = new FontDto();
            BeanUtils.copyBean(dto, coreFont);
            fontDtos.add(dto);
        }

        return fontDtos;
    }

    public FontDto create(FontDto fontDto) {
        if (CollectionUtils.isNotEmpty(coreFontRepository.findByName(fontDto.getName()))) {
            DEException.throwException("存在重名字库");
        }
        fontDto.setId(IDUtils.snowID());
        CoreFont coreFont = new CoreFont();
        BeanUtils.copyBean(coreFont, fontDto);
        coreFont.setUpdateTime(System.currentTimeMillis());
        coreFontRepository.saveAndFlush(coreFont);
        return fontDto;
    }


    public FontDto edit(FontDto fontDto) {
        if (ObjectUtils.isEmpty(fontDto.getId())) {
            return create(fontDto);
        }
        if (fontDto.getIsDefault()) {
            coreFontRepository.resetDefaultById(fontDto.getId());
        }
        CoreFont coreFont = new CoreFont();
        BeanUtils.copyBean(coreFont, fontDto);
        coreFont.setUpdateTime(System.currentTimeMillis());
        coreFontRepository.saveAndFlush(coreFont);
        return fontDto;
    }

    public void delete(Long id) {
        CoreFont coreFont = coreFontRepository.findById(id).orElse(null);
        if (coreFont != null) {
            coreFontRepository.deleteById(id);
            if (StringUtils.isNotEmpty(coreFont.getFileTransName())) {
                FileUtils.deleteFile(path + coreFont.getFileTransName());
            }
        }

    }

    public void changeDefault(FontDto fontDto) {
        coreFontRepository.updateIsDefaultById(fontDto.getId(), fontDto.getIsDefault());
    }

    public FontDto upload(MultipartFile file) {
        String fileUuid = UUID.randomUUID().toString();
        return saveFile(file, fileUuid);
    }

    public void download(String file, HttpServletResponse response) {

        List<CoreFont> coreFonts = coreFontRepository.findByFileTransName(file);
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
        List<CoreFont> coreFonts = coreFontRepository.findByisDefault(true);
        List<FontDto> fontDtos = new ArrayList<>();
        for (CoreFont coreFont : coreFonts) {
            FontDto dto = new FontDto();
            BeanUtils.copyBean(dto, coreFont);
            fontDtos.add(dto);
        }
        return fontDtos;
    }

    private FontDto saveFile(MultipartFile file, String fileNameUUID) throws DEException {
        FontDto fontDto = new FontDto();
        try {
            String filename = file.getOriginalFilename();
            if (StringUtils.isEmpty(filename) || !filename.toLowerCase().endsWith(".ttf")) {
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
            Font font = Font.createFont(Font.TRUETYPE_FONT, new File(filePath));
            fontDto.setSize(size);
            fontDto.setSizeType(unit);
            fontDto.setName(font.getFontName());
        } catch (Exception e) {
            DEException.throwException(e);
        }
        return fontDto;
    }

}
