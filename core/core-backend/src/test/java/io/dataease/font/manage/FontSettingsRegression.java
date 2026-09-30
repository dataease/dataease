package io.dataease.font.manage;

import io.dataease.api.font.dto.FontUploadSettings;
import io.dataease.api.system.vo.SettingItemVO;
import io.dataease.exception.DEException;
import io.dataease.permission.util.V3UserUtil;
import io.dataease.system.dao.auto.mapper.CoreSysSettingRepository;
import io.dataease.system.manage.SysParameterManage;
import org.springframework.context.annotation.*;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.orm.jpa.*;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.multipart.MultipartFile;

import javax.sql.DataSource;
import jakarta.persistence.EntityManagerFactory;
import java.io.*;
import java.lang.reflect.Field;
import java.nio.file.*;
import java.util.*;

/** Real isolated H2/JPA storage; authentication contexts are set explicitly, not real login. */
public class FontSettingsRegression {
    static Path directory;
    static int assertions;
    static boolean fontMenuGranted;
    @Configuration
    @EnableTransactionManagement
    @EnableJpaRepositories(basePackageClasses = {CoreSysSettingRepository.class, io.dataease.font.dao.auto.mapper.CoreFontRepository.class})
    static class Config {
        @Bean io.dataease.api.menu.MenuApi menuApi() {
            return () -> {
                var root = new io.dataease.api.menu.vo.MenuVO(); root.setPath("/sys-setting");
                var font = new io.dataease.api.menu.vo.MenuVO(); font.setPath(fontMenuGranted ? "font" : "appearance");
                root.setChildren(List.of(font));
                return List.of(root);
            };
        }
        @Bean DataSource dataSource() {
            return new DriverManagerDataSource("jdbc:h2:mem:fontsettings;DB_CLOSE_DELAY=-1", "sa", "");
        }
        @Bean LocalContainerEntityManagerFactoryBean entityManagerFactory(DataSource ds) {
            var bean = new LocalContainerEntityManagerFactoryBean();
            bean.setDataSource(ds);
            bean.setPackagesToScan("io.dataease.system.dao.auto.entity", "io.dataease.font.dao.auto.entity");
            bean.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
            bean.setJpaPropertyMap(Map.of("hibernate.hbm2ddl.auto", "update"));
            return bean;
        }
        @Bean JpaTransactionManager transactionManager(EntityManagerFactory emf) {
            return new JpaTransactionManager(emf);
        }
        @Bean FontManage fonts() { return new FontManage(); }
        @Bean FontSettingsManage settings() throws Exception {
            var result = new FontSettingsManage();
            field(result, "path", directory.toString());
            return result;
        }
    }
    static void field(Object target, String key, Object value) throws Exception {
        Field f = target.getClass().getDeclaredField(key); f.setAccessible(true); f.set(target, value);
    }
    static void check(boolean ok) { assertions++; if (!ok) throw new AssertionError("Assertion " + assertions); }
    static void deny(Runnable action, int code) {
        try { action.run(); throw new AssertionError("Expected denial"); }
        catch (DEException e) { check(e.getCode() == code); }
    }
    public static void main(String[] args) throws Exception {
        new io.dataease.i18n.Translator().setMessageSource(new org.springframework.context.support.StaticMessageSource());
        new io.dataease.utils.IDUtils().setSnowFlake(new io.dataease.utils.SnowFlake(0, 1));
        directory = Files.createTempDirectory("de-font-settings-");
        System.setProperty("dataease.path.font", directory.toString());
        try {
            try (var context = new AnnotationConfigApplicationContext(Config.class)) {
                var settings = context.getBean(FontSettingsManage.class);
                var repository = context.getBean(CoreSysSettingRepository.class);
                V3UserUtil.clear(); deny(settings::query, 403);
                V3UserUtil.setUid(99L);
                deny(settings::query, 403);
                deny(() -> settings.save(new FontUploadSettings(1L, 2L)), 403);
                check(repository.count() == 0);
                var fonts = context.getBean(FontManage.class);
                var fontRepository = context.getBean(io.dataease.font.dao.auto.mapper.CoreFontRepository.class);
                field(fonts, "path", directory + File.separator);
                var original = new io.dataease.font.dao.auto.entity.CoreFont();
                original.setId(20L); original.setName("existing"); original.setIsDefault(true);
                original.setFileTransName("protected.ttf"); original.setUpdateTime(1L);
                fontRepository.saveAndFlush(original);
                Path protectedFile = directory.resolve("protected.ttf");
                Files.writeString(protectedFile, "unchanged");
                var input = new io.dataease.api.font.dto.FontDto();
                input.setId(20L); input.setName("unauthorized"); input.setIsDefault(false);
                for (int identity = 0; identity < 4; identity++) {
                    V3UserUtil.clear();
                    if (identity > 0) V3UserUtil.setUid(99L);
                    if (identity == 2) V3UserUtil.setProxy(10L);
                    if (identity == 3) {
                        fontMenuGranted = true;
                        V3UserUtil.setLink(new io.dataease.permission.model.LinkIdentity(1L, 99L, 1L, 1L));
                    }
                    deny(() -> fonts.create(input), 403);
                    deny(() -> fonts.edit(input), 403);
                    deny(() -> fonts.delete(20L), 403);
                    deny(() -> fonts.changeDefault(input), 403);
                    deny(() -> fonts.upload(null), 403);
                    var unchanged = fontRepository.findById(20L).orElseThrow();
                    check(fontRepository.count() == 1 && unchanged.getName().equals("existing") && unchanged.getIsDefault());
                    check(Files.readString(protectedFile).equals("unchanged"));
                }
                V3UserUtil.clear(); V3UserUtil.setUid(99L); fontMenuGranted = true;
                input.setId(null); input.setName("allowed");
                fonts.create(input);
                check(fontRepository.count() == 2);
                input.setName("edited"); fonts.edit(input);
                check(fontRepository.findById(input.getId()).orElseThrow().getName().equals("edited"));
                input.setIsDefault(true); fonts.changeDefault(input);
                check(fontRepository.findById(input.getId()).orElseThrow().getIsDefault());
                fonts.delete(input.getId()); check(fontRepository.count() == 1);
                fonts.delete(20L); check(fontRepository.count() == 0 && !Files.exists(protectedFile));

                fontMenuGranted = true;
                check(settings.query().maxUploadMb() == 20 && settings.query().maxStorageMb() == 512);
                settings.save(new FontUploadSettings(1L, 10L));
                check(repository.count() == 1);
                var second = new FontSettingsManage(); field(second, "repository", repository);
                check(second.limits().uploadBytes() == 1048576L);
                FontManage upload = new FontManage(); field(upload, "path", directory.toString()); field(upload, "fontSettingsManage", settings);
                MultipartFile file = new MultipartFile() {
                    public String getName() { return "file"; }
                    public String getOriginalFilename() { return "test.ttf"; }
                    public String getContentType() { return "application/octet-stream"; }
                    public boolean isEmpty() { return false; }
                    public long getSize() { return 2L * 1048576; }
                    public byte[] getBytes() { throw new AssertionError(); }
                    public InputStream getInputStream() { return new ByteArrayInputStream(new byte[100]); }
                    public void transferTo(File f) { throw new AssertionError(); }
                };
                try { upload.upload(file); throw new AssertionError(); }
                catch (DEException e) { check(e.getMessage().equals("i18n_font_upload_size_limit")); }
                settings.save(new FontUploadSettings(3L, 10L));
                try { upload.upload(file); throw new AssertionError(); }
                catch (DEException e) { check(e.getMessage().equals("i18n_font_upload_invalid")); }
                check(second.limits().uploadBytes() == 3L * 1048576);
                check(repository.count() == 1);
                for (var bad : List.of(new FontUploadSettings(0L, 10L), new FontUploadSettings(11L, 10L),
                        new FontUploadSettings(null, 10L), new FontUploadSettings(500L, 512L),
                        new FontUploadSettings(1L, Long.MAX_VALUE))) {
                    try { settings.save(bad); throw new AssertionError(); }
                    catch (DEException e) { check(e.getMessage().equals("i18n_font_upload_limits_invalid")); }
                    check(second.limits().uploadBytes() == 3L * 1048576);
                }
                Path existing = directory.resolve("existing.ttf");
                try (var f = new RandomAccessFile(existing.toFile(), "rw")) { f.setLength(2L * 1048576); }
                settings.save(new FontUploadSettings(1L, 1L));
                check(settings.query().usedBytes() == 2L * 1048576 && Files.exists(existing));
                V3UserUtil.setProxy(10L);
                check(settings.query().maxUploadMb() == 1);
                fontMenuGranted = false;
                deny(settings::query, 403);
                deny(() -> settings.save(new FontUploadSettings(2L, 3L)), 403);
                V3UserUtil.clear(); V3UserUtil.setUid(1L);
                fontMenuGranted = true;
                V3UserUtil.setLink(new io.dataease.permission.model.LinkIdentity(1L, 1L, 1L, 1L));
                deny(settings::query, 403);
                deny(() -> settings.save(new FontUploadSettings(2L, 3L)), 403);
                V3UserUtil.clear(); V3UserUtil.setUid(99L); V3UserUtil.setProxy(-1L);
                check(settings.query().maxUploadMb() == 1);
                V3UserUtil.clear(); V3UserUtil.setUid(1L);
                SettingItemVO setting = new SettingItemVO(); setting.setPkey(FontSettingsManage.KEY);
                deny(() -> new SysParameterManage().saveGroup(List.of(setting), "basic"), 403);
                check(second.limits().uploadBytes() == 1048576L);
            }
            // 新上下文模拟实例重启，仍然使用同一个隔离数据库。
            try (var context = new AnnotationConfigApplicationContext(Config.class)) {
                check(context.getBean(FontSettingsManage.class).query().maxStorageMb() == 1);
            }
            System.out.println("PASS: " + assertions + " assertions; real H2/JPA persistence, permission, bypass denial, live upload limits, second reader, restart");
        } finally {
            V3UserUtil.clear();
            System.clearProperty("dataease.path.font");
            try (var files = Files.walk(directory)) {
                for (Path p : files.sorted(Comparator.reverseOrder()).toList()) Files.deleteIfExists(p);
            }
        }
    }
}
