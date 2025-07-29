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
        for (String versionGroup : groupedSqlBlocks.keySet()) {
            List<SqlBlock> toMigrateSqlBlocks = groupedSqlBlocks.get(versionGroup);
            toMigrateSqlBlocks.sort(versionComparator);
            int versionRank = findLastRank();
            DeStandaloneVersion lastVersion = getLastVersion(versionGroup);
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
        deStandaloneVersion.setInstalledOn(Instant.now());
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

    private Comparator<SqlBlock> versionComparator = new Comparator<SqlBlock>() {
        @Override
        public int compare(SqlBlock sb1, SqlBlock sb2) {
            Version v1 = sb1.getVersion();
            Version v2 = sb2.getVersion();

            int i = 0;
            while (i < v1.getParts().size() || i < v2.getParts().size()) {
                if (i < v1.getParts().size() && i < v2.getParts().size()) {
                    int thisPart = Integer.parseInt(v1.getParts().get(i));
                    int otherPart = Integer.parseInt(v2.getParts().get(i));
                    if (thisPart != otherPart) {
                        return thisPart - otherPart;
                    }
                } else if (i < v1.getParts().size()) {
                    if (Integer.parseInt(v1.getParts().get(i)) != 0) {
                        return 1;
                    }
                } else {
                    if (Integer.parseInt(v2.getParts().get(i)) != 0) {
                        return -1;
                    }
                }
                i++;
            }
            return 0;
        }
    };

}
