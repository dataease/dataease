package io.dataease.font.manage;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import io.dataease.api.font.dto.FontDto;
import io.dataease.exception.DEException;
import io.dataease.font.dao.auto.entity.CoreFont;
import io.dataease.font.dao.auto.mapper.CoreFontMapper;
import io.dataease.utils.BeanUtils;
import io.dataease.utils.FileUtils;
import io.dataease.utils.IDUtils;
import io.dataease.utils.LogUtil;
import jakarta.annotation.Resource;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.awt.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

@Component
public class FontManage {

    private static final Pattern SAFE_FONT_FILE_NAME = Pattern.compile("^[A-Za-z0-9._-]+\\.ttf$", Pattern.CASE_INSENSITIVE);
    private static final String TTF_EXTENSION = ".ttf";

    @Value("${dataease.path.font:/opt/dataease2.0/data/font/}")
    private String path;

    @Resource
    private CoreFontMapper coreFontMapper;
    @Autowired
    private ResourceLoader resourceLoader;

    public List<FontDto> list() {
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
        validateUploadedFont(fontDto.getFileTransName());
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
        validateUploadedFont(fontDto.getFileTransName());
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
                deleteManagedFontFile(coreFont.getFileTransName());
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
        validateFontFileName(file);

        QueryWrapper<CoreFont> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("file_trans_name", file);
        List<CoreFont> coreFonts = coreFontMapper.selectList(queryWrapper);
        if (CollectionUtils.isEmpty(coreFonts)) {
            DEException.throwException("不存在的字库文件");
        }

        Path filePath = resolveFontPath(coreFonts.get(0).getFileTransName());
        try {
            response.setContentType("application/x-download");
            response.setHeader("Content-Disposition", "attachment;filename=" + coreFonts.get(0).getFileTransName());
            try (ServletOutputStream out = response.getOutputStream();
                 InputStream stream = Files.newInputStream(filePath)) {
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

    private FontDto saveFile(MultipartFile file, String fileNameUUID) throws DEException {
        FontDto fontDto = new FontDto();
        try {
            validateUploadedFontFilename(file.getOriginalFilename());
            byte[] fileBytes = file.getBytes();
            UploadedFont uploadedFont = validateAndReadUploadedFont(fileBytes);
            String fileTransName = fileNameUUID + TTF_EXTENSION;
            Path filePath = resolveFontPath(fileTransName);
            Files.write(filePath, uploadedFont.bytes(), StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE);
            fontDto.setFileTransName(fileTransName);

            long length = uploadedFont.bytes().length;
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
            fontDto.setName(uploadedFont.font().getFontName());
        } catch (Exception e) {
            DEException.throwException(e);
        }
        return fontDto;
    }

    private void validateUploadedFontFilename(String filename) {
        FileUtils.validateUploadFilename(filename);
        if (StringUtils.isEmpty(filename) || !filename.toLowerCase().endsWith(TTF_EXTENSION)) {
            DEException.throwException("非法格式的文件！");
        }
    }

    private UploadedFont validateAndReadUploadedFont(byte[] fileBytes) throws Exception {
        if (fileBytes.length == 0) {
            DEException.throwException("非法格式的文件！");
        }
        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(fileBytes)) {
            Font font = Font.createFont(Font.TRUETYPE_FONT, inputStream);
            return new UploadedFont(fileBytes, font);
        } catch (FontFormatException e) {
            DEException.throwException("非法格式的文件！");
            return null;
        }
    }

    private void validateUploadedFont(String fileTransName) {
        if (StringUtils.isEmpty(fileTransName)) {
            return;
        }
        Path filePath = resolveFontPath(fileTransName);
        if (!Files.isRegularFile(filePath)) {
            DEException.throwException("不存在的字库文件");
        }
    }

    private void deleteManagedFontFile(String fileTransName) {
        if (!isSafeFontFileName(fileTransName)) {
            LogUtil.warn("Skip deleting unmanaged font file: " + fileTransName);
            return;
        }
        Path filePath = resolveFontPath(fileTransName);
        try {
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            DEException.throwException(e);
        }
    }

    private Path resolveFontPath(String fileTransName) {
        validateFontFileName(fileTransName);
        Path fontBasePath = getFontBasePath();
        Path targetPath = fontBasePath.resolve(fileTransName).normalize();
        if (!targetPath.startsWith(fontBasePath)) {
            DEException.throwException("非法字体文件路径");
        }
        return targetPath;
    }

    private Path getFontBasePath() {
        try {
            Path fontBasePath = Paths.get(path).toAbsolutePath().normalize();
            Files.createDirectories(fontBasePath);
            return fontBasePath.toRealPath();
        } catch (IOException e) {
            DEException.throwException(e);
            return null;
        }
    }

    private void validateFontFileName(String fileTransName) {
        if (!isSafeFontFileName(fileTransName)) {
            DEException.throwException("非法字体文件名");
        }
    }

    private boolean isSafeFontFileName(String fileTransName) {
        return StringUtils.isNotBlank(fileTransName) && SAFE_FONT_FILE_NAME.matcher(fileTransName).matches();
    }

    private record UploadedFont(byte[] bytes, Font font) {
    }
}
