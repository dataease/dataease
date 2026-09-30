package io.dataease.font.manage;

import io.dataease.api.font.dto.FontUploadSettings;
import io.dataease.api.menu.MenuApi;
import io.dataease.api.menu.vo.MenuVO;
import io.dataease.api.font.dto.FontUploadSettingsVO;
import io.dataease.exception.DEException;
import io.dataease.i18n.Translator;
import io.dataease.permission.util.V3UserUtil;
import io.dataease.system.dao.auto.entity.CoreSysSetting;
import io.dataease.system.dao.auto.mapper.CoreSysSettingRepository;
import io.dataease.utils.IDUtils;
import io.dataease.utils.JsonUtil;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.unit.DataSize;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@Component
public class FontSettingsManage {
    public static final String KEY = "font.upload.limits";
    private static final long MB = 1024L * 1024;
    private static final long MAX_STORAGE_MB = 1024L * 1024;

    @Resource
    private CoreSysSettingRepository repository;
    @Resource
    private MenuApi menuApi;
    @Value("#{T(org.springframework.util.unit.DataSize).parse('${dataease.font.max-upload-size:20MB}')}")
    private DataSize maxUploadSize = DataSize.ofMegabytes(20);
    @Value("#{T(org.springframework.util.unit.DataSize).parse('${dataease.font.max-storage-size:512MB}')}")
    private DataSize maxStorageSize = DataSize.ofMegabytes(512);
    @Value("#{T(org.springframework.util.unit.DataSize).parse('${spring.servlet.multipart.max-file-size:500MB}')}")
    private DataSize multipartFileSize = DataSize.ofMegabytes(500);
    @Value("#{T(org.springframework.util.unit.DataSize).parse('${spring.servlet.multipart.max-request-size:500MB}')}")
    private DataSize multipartRequestSize = DataSize.ofMegabytes(500);
    @Value("${dataease.path.font:/opt/dataease3.0/data/font/}")
    private String path;

    public record Limits(long uploadBytes, long storageBytes) { }

    // 不缓存：每次上传读取同一条设置，其他实例在提交后立即看到完整的新配置。
    public Limits limits() {
        var stored = repository.findByPkey(KEY);
        if (stored.isEmpty()) return new Limits(maxUploadSize.toBytes(), maxStorageSize.toBytes());
        FontUploadSettings settings = JsonUtil.parseObject(stored.get().getPval(), FontUploadSettings.class);
        validate(settings, false);
        return new Limits(settings.maxUploadMb() * MB, settings.maxStorageMb() * MB);
    }

    public FontUploadSettingsVO query() {
        checkMenuPermission();
        Limits limits = limits();
        long used = 0;
        Path directory = Path.of(path);
        if (Files.exists(directory)) {
            try (var files = Files.newDirectoryStream(directory)) {
                for (Path file : files) if (Files.isRegularFile(file)) used += Files.size(file);
            } catch (IOException e) {
                DEException.throwException(Translator.get("i18n_font_upload_failed"));
            }
        }
        return new FontUploadSettingsVO(limits.uploadBytes() / MB, limits.storageBytes() / MB,
                used, maxUploadAllowedMb());
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public void save(FontUploadSettings settings) {
        checkMenuPermission();
        validate(settings, true);
        // 单条 JSON 保证两项设置一起生效；串行化事务保护首次并发创建。
        CoreSysSetting entity = repository.findByPkey(KEY)
                .orElseGet(() -> new CoreSysSetting(IDUtils.snowID(), KEY, "", "font", 0));
        entity.setPval(JsonUtil.toJSONString(settings).toString());
        repository.saveAndFlush(entity);
    }

    private void validate(FontUploadSettings settings, boolean checkRequestLimit) {
        if (settings == null || settings.maxUploadMb() == null || settings.maxStorageMb() == null
                || settings.maxUploadMb() < 1 || settings.maxStorageMb() < 1
                || settings.maxStorageMb() > MAX_STORAGE_MB
                || settings.maxUploadMb() > settings.maxStorageMb()
                || (checkRequestLimit && settings.maxUploadMb() > maxUploadAllowedMb())) {
            DEException.throwException(Translator.get("i18n_font_upload_limits_invalid"));
        }
    }

    private long maxUploadAllowedMb() {
        // 为 multipart 边界和文件头预留 64KB；无限制配置仍以安全整数范围封顶。
        long file = multipartFileSize.toBytes() < 0 ? Long.MAX_VALUE : multipartFileSize.toBytes();
        long request = multipartRequestSize.toBytes() < 0 ? Long.MAX_VALUE : multipartRequestSize.toBytes();
        return Math.max(0, Math.min(MAX_STORAGE_MB, Math.min(file, Math.max(0, request - 65536)) / MB));
    }

    // 字体设置和字体写操作共用菜单权限，必须在任何数据库或文件写入前调用。
    public void checkMenuPermission() {
        if (V3UserUtil.getUid() == null || V3UserUtil.getLink() != null
                || !hasFontMenu(menuApi.query(), "")) {
            DEException.throwException(403, Translator.get("i18n_font_settings_forbidden"));
        }
    }

    private boolean hasFontMenu(List<MenuVO> menus, String parent) {
        if (menus == null) return false;
        for (MenuVO menu : menus) {
            String path = menu.getPath();
            if (path == null) continue;
            String fullPath = path.startsWith("/") ? path : parent + "/" + path;
            if ("/sys-setting/font".equals(fullPath) || hasFontMenu(menu.getChildren(), fullPath)) {
                return true;
            }
        }
        return false;
    }
}
