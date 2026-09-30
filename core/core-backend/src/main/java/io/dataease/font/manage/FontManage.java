package io.dataease.font.manage;

import io.dataease.api.font.dto.FontDto;
import io.dataease.exception.DEException;
import io.dataease.i18n.Translator;
import io.dataease.font.dao.auto.entity.CoreFont;
import io.dataease.font.dao.auto.mapper.CoreFontRepository;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.awt.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.Locale;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.util.concurrent.Semaphore;

@Component
public class FontManage {

    @Value("${dataease.path.font:/opt/dataease3.0/data/font/}")
    private String path;

    @Resource
    private FontSettingsManage fontSettingsManage;

    private final Semaphore uploadPermit = new Semaphore(1);

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
                String targetPath = path + coreFont.getFileTransName();
                try {
                    File targetFile = new File(targetPath).getCanonicalFile();
                    if (targetFile.getAbsolutePath().startsWith(new File(path).getCanonicalPath())) {
                        FileUtils.deleteFile(targetFile.getAbsolutePath());
                    }
                } catch (IOException e) {
                    // skip invalid paths
                }
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
        if (StringUtils.isBlank(file) || file.contains("..") || file.contains("/") || file.contains("\\")) {
            DEException.throwException("非法的文件路径");
        }

        List<CoreFont> coreFonts = coreFontRepository.findByFileTransName(file);
        if (CollectionUtils.isEmpty(coreFonts)) {
            DEException.throwException("不存在的字库文件");
        }

        try {
            String targetPath = path + coreFonts.get(0).getFileTransName();
            File targetFile = new File(targetPath).getCanonicalFile();
            if (!targetFile.getAbsolutePath().startsWith(new File(path).getCanonicalPath())) {
                DEException.throwException("非法的文件路径");
            }

            response.setContentType("application/x-download");
            response.setHeader("Content-Disposition", "attachment;filename=" + coreFonts.get(0).getFileTransName());
            try (ServletOutputStream out = response.getOutputStream();
                 InputStream stream = new FileInputStream(targetFile)) {
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
        FontSettingsManage.Limits limits = fontSettingsManage.limits();
        long fileLimit = limits.uploadBytes();
        long storageLimit = limits.storageBytes();
        if (fileLimit <= 0 || storageLimit <= 0) {
            DEException.throwException(Translator.get("i18n_font_upload_limits_invalid"));
        }
        if (file == null || file.getSize() <= 0 || file.getSize() > fileLimit) {
            DEException.throwException(Translator.get("i18n_font_upload_size_limit"));
        }
        String filename = file.getOriginalFilename();
        if (StringUtils.isBlank(filename) || !filename.toLowerCase(Locale.ROOT).endsWith(".ttf")) {
            DEException.throwException(Translator.get("i18n_font_upload_invalid"));
        }
        FileUtils.validateUploadFilename(filename);
        if (!uploadPermit.tryAcquire()) {
            DEException.throwException(Translator.get("i18n_font_upload_busy"));
        }
        try {
            Path directory = Path.of(path);
            Files.createDirectories(directory);
            // 同一字体目录可能被多个实例共享，配额检查和写入必须持有同一把锁。
            try (FileChannel channel = FileChannel.open(directory.resolve(".upload.lock"),
                    StandardOpenOption.CREATE, StandardOpenOption.WRITE);
                 FileLock lock = channel.tryLock()) {
                if (lock == null) {
                    DEException.throwException(Translator.get("i18n_font_upload_busy"));
                }
                long remaining = storageLimit;
                try (var files = Files.newDirectoryStream(directory)) {
                    for (Path existing : files) {
                        if (Files.isRegularFile(existing)) {
                            remaining -= Files.size(existing);
                            if (remaining < file.getSize()) {
                                DEException.throwException(Translator.get("i18n_font_storage_limit"));
                            }
                        }
                    }
                }
                return storeValidatedFont(file, directory, fileNameUUID, fileLimit, remaining);
            }
        } catch (DEException e) {
            throw e;
        } catch (java.nio.channels.OverlappingFileLockException e) {
            DEException.throwException(Translator.get("i18n_font_upload_busy"));
        } catch (IOException e) {
            LogUtil.error("Font upload failed", e);
            DEException.throwException(Translator.get("i18n_font_upload_failed"));
        } finally {
            uploadPermit.release();
        }
        return null;
    }

    private FontDto storeValidatedFont(MultipartFile file, Path directory, String uuid,
                                      long fileLimit, long remaining) throws IOException {
        Path temporary = Files.createTempFile(directory, ".font-upload-", ".tmp");
        try {
            long length = 0;
            try (InputStream input = file.getInputStream();
                 OutputStream output = Files.newOutputStream(temporary)) {
                byte[] buffer = new byte[8192];
                int count;
                while ((count = input.read(buffer)) != -1) {
                    length += count;
                    if (length > fileLimit) {
                        DEException.throwException(Translator.get("i18n_font_upload_size_limit"));
                    }
                    if (length > remaining) {
                        DEException.throwException(Translator.get("i18n_font_storage_limit"));
                    }
                    output.write(buffer, 0, count);
                }
            }
            Font font;
            try {
                font = Font.createFont(Font.TRUETYPE_FONT, temporary.toFile());
            } catch (FontFormatException e) {
                DEException.throwException(Translator.get("i18n_font_upload_invalid"));
                return null;
            }
            FontDto dto = new FontDto();
            dto.setFileTransName(uuid + ".ttf");
            dto.setName(font.getFontName());
            boolean megabytes = length > 1024 * 1024;
            double size = (double) length / (megabytes ? 1024 * 1024 : 1024);
            dto.setSize(Math.round(size * 100.0) / 100.0);
            dto.setSizeType(megabytes ? "MB" : "KB");
            Files.move(temporary, directory.resolve(dto.getFileTransName()), StandardCopyOption.ATOMIC_MOVE);
            return dto;
        } finally {
            // 包括读取/写盘/解析/移动失败，均不遗留本次上传的随机文件。
            Files.deleteIfExists(temporary);
        }
    }

}
