package io.dataease.dataset.dao.ext.mapper;

import com.querydsl.core.Tuple;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import io.dataease.constant.BusiResourceEnum;
import io.dataease.dao.auto.entity.*;
import io.dataease.utils.CommunityUtils;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class DataSetAssistantMapper {

    @Resource
    private JPAQueryFactory queryFactory;

    private static final QCoreDatasetGroup cdg = QCoreDatasetGroup.coreDatasetGroup;
    private static final QCoreDatasetTable cdt = QCoreDatasetTable.coreDatasetTable;
    private static final QCoreDatasetTableField cdtf = QCoreDatasetTableField.coreDatasetTableField;
    private static final QCoreDatasource cd = QCoreDatasource.coreDatasource;

    /**
     * Mode 1: model=null, community standalone — no permission filtering at all.
     */
    public List<Map<String, Object>> queryAll(Long dsId, Long datasetId) {
        var query = buildBaseQuery();
        applyOptionalFilters(query, dsId, datasetId);
        return executeQuery(query);
    }

    /**
     * Mode 2: model=false, community with xpack loaded but license invalid.
     * Excludes resources that have been registered in per_busi_resource
     * (i.e., already assigned permissions).
     */
    public List<Map<String, Object>> queryCommunity(Long dsId, Long datasetId) {
        var query = buildBaseQuery()
                .where(CommunityUtils.buildNotExistsCondition(cdg, BusiResourceEnum.DATASET.getFlag()))
                .where(CommunityUtils.buildNotExistsCondition(cd, BusiResourceEnum.DATASOURCE.getFlag()));
        applyOptionalFilters(query, dsId, datasetId);
        return executeQuery(query);
    }

    public void applyOptionalFilters(JPQLQuery<?> query, Long dsId, Long datasetId) {
        if (ObjectUtils.isNotEmpty(datasetId)) {
            query.where(cdg.id.eq(datasetId));
        }
        if (ObjectUtils.isNotEmpty(dsId)) {
            query.where(cd.id.eq(dsId));
        }
    }

    JPQLQuery<Tuple> buildBaseQuery() {
        return queryFactory
                .select(
                        cd.id, cd.name, cd.description, cd.type, cd.configuration,
                        cdg.id, cdg.name, cdg.type, cdg.mode, cdg.info, cdg.unionSql, cdg.isCross,
                        cdt.id, cdt.tableName, cdt.type, cdt.info, cdt.sqlVariableDetails,
                        cdtf.id, cdtf.originName, cdtf.name, cdtf.description, cdtf.dataeaseName,
                        cdtf.fieldShortName, cdtf.groupList, cdtf.otherGroup, cdtf.groupType,
                        cdtf.type, cdtf.deType, cdtf.deExtractType, cdtf.extField,
                        cdtf.checked, cdtf.accuracy, cdtf.dateFormat, cdtf.dateFormatType, cdtf.params
                )
                .from(cdg)
                .leftJoin(cdt).on(cdg.id.eq(cdt.datasetGroupId))
                .leftJoin(cdtf).on(cdtf.datasetGroupId.eq(cdg.id)
                        .and(cdtf.datasetTableId.isNull().or(cdtf.datasetTableId.eq(cdt.id))))
                .innerJoin(cd).on(cdt.datasourceId.eq(cd.id))
                .where(cdg.isCross.isFalse()
                        .and(cd.status.isNull().or(cd.status.ne("Error"))));
    }

    List<Map<String, Object>> executeQuery(JPQLQuery<Tuple> query) {
        List<Tuple> tuples = query.fetch();
        List<Map<String, Object>> result = new ArrayList<>();
        for (Tuple tuple : tuples) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("cd_id", tuple.get(cd.id));
            map.put("cd_name", tuple.get(cd.name));
            map.put("cd_description", tuple.get(cd.description));
            map.put("cd_type", tuple.get(cd.type));
            map.put("cd_configuration", tuple.get(cd.configuration));
            map.put("cdg_id", tuple.get(cdg.id));
            map.put("cdg_name", tuple.get(cdg.name));
            map.put("cdg_type", tuple.get(cdg.type));
            map.put("cdg_model", tuple.get(cdg.mode));
            map.put("cdg_info", tuple.get(cdg.info));
            map.put("cdg_union_sql", tuple.get(cdg.unionSql));
            map.put("cdg_is_cross", tuple.get(cdg.isCross));
            map.put("cdt_id", tuple.get(cdt.id));
            map.put("cdt_table_name", tuple.get(cdt.tableName));
            map.put("cdt_type", tuple.get(cdt.type));
            map.put("cdt_info", tuple.get(cdt.info));
            map.put("cdt_sql_variable_details", tuple.get(cdt.sqlVariableDetails));
            map.put("cdtf_id", tuple.get(cdtf.id));
            map.put("cdtf_origin_name", tuple.get(cdtf.originName));
            map.put("cdtf_name", tuple.get(cdtf.name));
            map.put("cdtf_description", tuple.get(cdtf.description));
            map.put("cdtf_dataease_name", tuple.get(cdtf.dataeaseName));
            map.put("cdtf_field_short_name", tuple.get(cdtf.fieldShortName));
            map.put("cdtf_group_list", tuple.get(cdtf.groupList));
            map.put("cdtf_other_group", tuple.get(cdtf.otherGroup));
            map.put("cdtf_group_type", tuple.get(cdtf.groupType));
            map.put("cdtf_type", tuple.get(cdtf.type));
            map.put("cdtf_de_type", tuple.get(cdtf.deType));
            map.put("cdtf_de_extract_type", tuple.get(cdtf.deExtractType));
            map.put("cdtf_ext_field", tuple.get(cdtf.extField));
            map.put("cdtf_checked", tuple.get(cdtf.checked));
            map.put("cdtf_accuracy", tuple.get(cdtf.accuracy));
            map.put("cdtf_date_format", tuple.get(cdtf.dateFormat));
            map.put("cdtf_date_format_type", tuple.get(cdtf.dateFormatType));
            map.put("cdtf_params", tuple.get(cdtf.params));
            result.add(map);
        }
        return result;
    }
}
