package io.dataease.listener.sql;

import io.dataease.dao.auto.entity.CoreDatasource;
import io.dataease.datasource.dao.auto.entity.CoreDeEngine;
import io.dataease.datasource.dao.auto.repository.CoreDatasourceRepository;
import io.dataease.datasource.dao.auto.repository.CoreDeEngineRepository;
import io.dataease.initSql.Version;
import io.dataease.utils.LogUtil;
import jakarta.annotation.Resource;
import org.apache.commons.codec.binary.Base64;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * 一次性迁移：把历史版本用公开 legacy 密钥写入的加密配置，重新用每实例随机密钥加密。
 * 仅在此迁移窗口内保留 legacy 解密逻辑；运行时 aesDecrypt 不再回退 legacy 密钥。
 */
@Component
public class CoreDataVersion5 implements CoreSqlBlock {

    private static final String LEGACY_SECRET_KEY = "www.fit2cloud.co";
    private static final String LEGACY_IV = "1234567890123456";

    @Resource
    private CoreDatasourceRepository coreDatasourceRepository;

    @Resource
    private CoreDeEngineRepository coreDeEngineRepository;

    @Override
    public Version getVersion() {
        return new Version("2.44");
    }

    @Override
    @Transactional
    public void execute() {
        migrateDatasources();
        migrateEngines();
    }

    private void migrateDatasources() {
        List<CoreDatasource> rows = coreDatasourceRepository.findAll();
        List<CoreDatasource> toSave = new ArrayList<>();
        int skipped = 0;
        for (CoreDatasource row : rows) {
            String stored = row.getConfiguration();
            if (stored == null || stored.isEmpty()) {
                continue;
            }
            String legacyPlain = decryptLegacy(stored);
            if (legacyPlain == null) {
                // 已是每实例密钥密文（读取时已解密为明文）、历史明文或损坏数据，保持原值
                skipped++;
                continue;
            }
            row.setConfiguration(legacyPlain);
            toSave.add(row);
        }
        if (!toSave.isEmpty()) {
            coreDatasourceRepository.saveAll(toSave);
        }
        LogUtil.info("Storage-key migration finished for core_datasource: re-encrypted=" + toSave.size() + ", skipped=" + skipped);
    }

    private void migrateEngines() {
        List<CoreDeEngine> rows = coreDeEngineRepository.findAll();
        List<CoreDeEngine> toSave = new ArrayList<>();
        int skipped = 0;
        for (CoreDeEngine row : rows) {
            String stored = row.getConfiguration();
            if (stored == null || stored.isEmpty()) {
                continue;
            }
            String legacyPlain = decryptLegacy(stored);
            if (legacyPlain == null) {
                skipped++;
                continue;
            }
            row.setConfiguration(legacyPlain);
            toSave.add(row);
        }
        if (!toSave.isEmpty()) {
            coreDeEngineRepository.saveAll(toSave);
        }
        LogUtil.info("Storage-key migration finished for core_de_engine: re-encrypted=" + toSave.size() + ", skipped=" + skipped);
    }

    private static String decryptLegacy(String src) {
        try {
            byte[] raw = LEGACY_SECRET_KEY.getBytes(StandardCharsets.UTF_8);
            SecretKeySpec keySpec = new SecretKeySpec(raw, "AES");
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            IvParameterSpec ivSpec = new IvParameterSpec(LEGACY_IV.getBytes(StandardCharsets.UTF_8));
            cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);
            byte[] encrypted = Base64.decodeBase64(src);
            byte[] original = cipher.doFinal(encrypted);
            return new String(original, StandardCharsets.UTF_8);
        } catch (Exception e) {
            return null;
        }
    }
}
