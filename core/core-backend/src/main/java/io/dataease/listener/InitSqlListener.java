package io.dataease.listener;

import io.dataease.datasource.dao.auto.entity.DeStandaloneVersion;
import io.dataease.datasource.dao.auto.repository.DeStandaloneVersionRepository;

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

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.*;


@Component
@Order(value = 1)
public class InitSqlListener implements ApplicationRunner {
    @Autowired
    private DeStandaloneVersionRepository deStandaloneVersionRepository;


    @Override
    public void run(ApplicationArguments args) {
        List<DeStandaloneVersion> versionRecords = deStandaloneVersionRepository.findRecords();
        boolean isUpgrade = !CollectionUtils.isEmpty(versionRecords);

        List<SqlBlock> sqlBlocks = new ArrayList<>();
        Map<String, SqlBlock> beansOfType = SpringContextUtil.getApplicationContext().getBeansOfType(SqlBlock.class);
        sqlBlocks.addAll(beansOfType.entrySet().stream().map(Map.Entry::getValue).toList());

        Map<String, List<SqlBlock>> groupedSqlBlocks = new HashMap<>();
        for (SqlBlock block : sqlBlocks) {
            String versionGroup = block.getVersionGroup();
            groupedSqlBlocks.computeIfAbsent(versionGroup, k -> new ArrayList<>()).add(block);
        }

        if (isUpgrade) {
            executeGroups(groupedSqlBlocks, "upgrade");
        } else {
            executeGroups(groupedSqlBlocks, "1", "2", "3");
        }
    }

    private void executeGroups(Map<String, List<SqlBlock>> groupedSqlBlocks, String... groups) {
        for (String group : groups) {
            List<SqlBlock> toMigrateSqlBlocks = groupedSqlBlocks.get(group);
            if (toMigrateSqlBlocks == null) continue;
            toMigrateSqlBlocks.sort(versionComparator);
            int versionRank = findLastRank();
            DeStandaloneVersion lastVersion = getLastVersion(group);
            if (lastVersion == null) {
                for (SqlBlock sqlBlock : toMigrateSqlBlocks) {
                    versionRank++;
                    executeSql(sqlBlock, versionRank);
                }
            } else {
                Version version = new Version(lastVersion.getVersion());
                for (SqlBlock sqlBlock : toMigrateSqlBlocks) {
                    if (sqlBlock.getVersion().compareTo(version) > 0) {
                        versionRank++;
                        executeSql(sqlBlock, versionRank);
                    }
                }
            }
        }
    }

    private void executeSql(SqlBlock sqlBlock, int versionRank) {
        LogUtil.info("Begin to migrate sql: " + sqlBlock.getVersion().getVersion());
        long time = System.currentTimeMillis();
        sqlBlock.execute();
        DeStandaloneVersion deStandaloneVersion = new DeStandaloneVersion();
        deStandaloneVersion.setId(versionRank);
        deStandaloneVersion.setVersion(sqlBlock.getVersion().getVersion());
        deStandaloneVersion.setDescription("ddl");
        deStandaloneVersion.setType("SQL");
        deStandaloneVersion.setScript("V" + sqlBlock.getVersion().getVersion() + "__ddl.sql");
        deStandaloneVersion.setChecksum(0);
        deStandaloneVersion.setInstalledBy("system");
        deStandaloneVersion.setInstalledOn(LocalDateTime.now());
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
