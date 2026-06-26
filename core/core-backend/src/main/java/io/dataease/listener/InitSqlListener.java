package io.dataease.listener;

import io.dataease.dao.auto.entity.DeStandaloneVersion;
import io.dataease.dao.auto.repo.DeStandaloneVersionRepository;

import io.dataease.extensions.datasource.utils.SpringContextUtil;
import io.dataease.initSql.SqlBlock;
import io.dataease.initSql.Version;
import io.dataease.utils.LogUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.*;


@Component
@Order(value = 1)
public class InitSqlListener implements ApplicationRunner {
    @Autowired
    private DeStandaloneVersionRepository deStandaloneVersionRepository;


    @Override
    public void run(ApplicationArguments args) {
        List<SqlBlock> sqlBlocks = new ArrayList<>();
        Map<String, SqlBlock> beansOfType = SpringContextUtil.getApplicationContext().getBeansOfType(SqlBlock.class);
        sqlBlocks.addAll(beansOfType.entrySet().stream().map(Map.Entry::getValue).toList());

        Map<String, List<SqlBlock>> groupedSqlBlocks = new HashMap<>();
        for (SqlBlock block : sqlBlocks) {
            String versionGroup = block.getVersionGroup();
            groupedSqlBlocks.computeIfAbsent(versionGroup, k -> new ArrayList<>()).add(block);
        }

        executeGroups(groupedSqlBlocks, "1", "2", "3");
    }

    private void executeGroups(Map<String, List<SqlBlock>> groupedSqlBlocks, String... groups) {
        for (String group : groups) {
            List<SqlBlock> toMigrateSqlBlocks = groupedSqlBlocks.get(group);
            if (toMigrateSqlBlocks == null || toMigrateSqlBlocks.isEmpty()) {
                LogUtil.warn("No SqlBlock found for version group: " + group + ". Available groups: " + groupedSqlBlocks.keySet());
                continue;
            }
            LogUtil.info("Found " + toMigrateSqlBlocks.size() + " SqlBlock(s) for group: " + group);
            toMigrateSqlBlocks.sort(versionComparator);
            DeStandaloneVersion lastVersion = getLastVersion(group);
            if (lastVersion == null) {
                for (SqlBlock sqlBlock : toMigrateSqlBlocks) {
                    int versionRank = findLastRank() + 1;
                    executeSql(sqlBlock, versionRank);
                }
            } else {
                Version version = new Version(lastVersion.getVersion());
                // 上一版本执行失败（success=false），需要重新执行相同版本
                boolean retry = lastVersion.getSuccess() != null && !lastVersion.getSuccess();
                for (SqlBlock sqlBlock : toMigrateSqlBlocks) {
                    int cmp = sqlBlock.getVersion().compareTo(version);
                    if (cmp > 0 || (retry && cmp >= 0)) {
                        int versionRank = findLastRank() + 1;
                        executeSql(sqlBlock, versionRank);
                        retry = false; // 只重试失败的那个版本
                    }
                }
            }
        }
    }

    private void executeSql(SqlBlock sqlBlock, int versionRank) {
        LogUtil.info("Begin to migrate sql: " + sqlBlock.getVersion().getVersion());
        long time = System.currentTimeMillis();

        // 先保存一条 success=false 的记录，执行失败时该记录会留在表中
        DeStandaloneVersion deStandaloneVersion = new DeStandaloneVersion();
        deStandaloneVersion.setId(versionRank);
        deStandaloneVersion.setVersion(sqlBlock.getVersion().getVersion());
        deStandaloneVersion.setDescription("ddl");
        deStandaloneVersion.setType("SQL");
        deStandaloneVersion.setScript("V" + sqlBlock.getVersion().getVersion() + "__ddl.sql");
        deStandaloneVersion.setChecksum(0);
        deStandaloneVersion.setInstalledBy("system");
        deStandaloneVersion.setInstalledOn(LocalDateTime.now());
        deStandaloneVersion.setExecutionTime(0);
        deStandaloneVersion.setSuccess(false);
        deStandaloneVersionRepository.saveAndFlush(deStandaloneVersion);

        try {
            sqlBlock.execute();
        } catch (Exception e) {
            LogUtil.error("Failed to migrate sql: " + sqlBlock.getVersion().getVersion() + ", error: " + e.getMessage(), e);
            LogUtil.error("Application startup blocked due to sql migration failure. Fix the issue and restart to retry.", e);
            throw new RuntimeException("SqlBlock[" + sqlBlock.getVersion().getVersion() + "] execution failed, application startup blocked", e);
        }

        // 执行成功后更新 success=true
        deStandaloneVersion.setExecutionTime(Integer.valueOf((int) (System.currentTimeMillis() - time)));
        deStandaloneVersion.setSuccess(true);
        deStandaloneVersionRepository.saveAndFlush(deStandaloneVersion);
        LogUtil.info("Success to migrate sql : " + sqlBlock.getVersion().getVersion());
    }

    private int findLastRank() {
        List<DeStandaloneVersion> deStandaloneVersions = deStandaloneVersionRepository.findRecords();
        if (CollectionUtils.isEmpty(deStandaloneVersions)) {
            return 0;
        } else {
            return deStandaloneVersions.getFirst().getId();
        }
    }

    private DeStandaloneVersion getLastVersion(String versionGroup) {
        List<DeStandaloneVersion> migratedVersions = deStandaloneVersionRepository.findRecords();
        for (DeStandaloneVersion migratedVersion : migratedVersions) {
            if (migratedVersion.getVersion().startsWith(versionGroup)) {
                return migratedVersion;
            }
        }
        return null;
    }

    private Comparator<SqlBlock> versionComparator = (sb1, sb2) -> sb1.getVersion().compareTo(sb2.getVersion());

}
