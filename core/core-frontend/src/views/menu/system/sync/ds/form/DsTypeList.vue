<script lang="ts" setup>
import {computed, PropType, shallowRef} from "vue";
import mysqlDs from "@/assets/svg/mysql-ds.svg";
import oracleDs from "@/assets/svg/oracle-ds.svg";
import sqlServerDs from "@/assets/svg/sqlServer-ds.svg";
import TiDBDs from "@/assets/svg/TiDB-ds.svg";
import impalaDs from "@/assets/svg/impala-ds.svg";
import mariadbDs from "@/assets/svg/mariadb-ds.svg";
import StarRocksDs from "@/assets/svg/StarRocks-ds.svg";
import pgDs from "@/assets/svg/pg-ds.svg";
import mongoDs from "@/assets/svg/mongo-ds.svg";
import ckDs from "@/assets/svg/ck-ds.svg";
import db2Ds from "@/assets/svg/db2-ds.svg";
import redshiftDs from "@/assets/svg/redshift-ds.svg";
import APIDs from "@/assets/svg/API-ds.svg";
import ExcelDs from "@/assets/svg/Excel-ds.svg";
import dorisDs from "@/assets/svg/doris-ds.svg";
import {DsType, dsTypes, nameMap, typeList} from "./option";
import {useI18n} from "@/hooks/web/useI18n";
import Elasticsearch from "@/assets/svg/Elasticsearch.svg"
import {loadSyncPlugin} from "@/api/sync/syncDatasource";

const {t} = useI18n();

const props = withDefaults(
    defineProps<{
      currentType: string | PropType<DsType>;
      filterText: string;
      latestUseTypes: [];
    }>(),
    {
      currentType: "OLAP",
      filterText: "",
    }
);

const iconMap = {
  mysql: mysqlDs,
  oracle: oracleDs,
  sqlServer: sqlServerDs,
  TiDB: TiDBDs,
  impala: impalaDs,
  mariadb: mariadbDs,
  StarRocks: StarRocksDs,
  postgresql: pgDs,
  mongo: mongoDs,
  ck: ckDs,
  db2: db2Ds,
  redshift: redshiftDs,
  API: APIDs,
  Excel: ExcelDs,
  doris: dorisDs,
  elasticsearch: Elasticsearch,
};

const databaseList = shallowRef();
const currentTypeList = computed(() => {
  if (props.currentType == "all") {
    return typeList.map((ele, index) => {
      return {
        name: nameMap[ele],
        dbList: databaseList.value[index].filter((ele) =>
            ele.name.toLowerCase().includes(props.filterText.trim())
        ),
      };
    });
  }
  if (props.currentType === "latestUse") {
    let catalogList = [] as string[];
    let dstypes = [] as any[];
    props.latestUseTypes.forEach((type) => {
      dsTypes.forEach((item) => {
        if (item.type === type && catalogList.indexOf(item.catalog) === -1) {
          catalogList.push(item.catalog);
        }
      });
    });
    let dbList = [] as any[];
    catalogList.forEach((catalog) => {
      props.latestUseTypes.forEach((type) => {
        dsTypes.forEach((item) => {
          if (item.type === type && item.catalog === catalog) {
            dbList.push(item);
          }
        });
      });
    });
    dbList = dbList.filter((ele) =>
        ele.name.toLowerCase().includes(props.filterText.trim())
    );
    dstypes.push({name: t("sync_datasource.recently_created"), dbList});
    return dstypes;
  }
  const index = typeList.findIndex((ele) => props.currentType === ele);
  return (
      [
        {
          name: nameMap[props.currentType as string],
          dbList: databaseList.value[index].filter((ele) =>
              ele.name.toLowerCase().includes(props.filterText.trim())
          ),
        },
      ] || []
  );
});
const getDatasourceTypes = () => {
  const arr = [[], [], [], [], []] as any[];
  dsTypes.forEach((item) => {
    const index = typeList.findIndex((ele) => ele === item.catalog);
    if (index !== -1) {
      arr[index].push(item);
    }
  });
  databaseList.value = arr.map((ele) => {
    return ele.sort((a: any, b: any) => {
      return (
          a.name.toLowerCase().charCodeAt(0) - b.name.toLowerCase().charCodeAt(0)
      );
    });
  });
};
getDatasourceTypes();
const listSyncPlugin = () => {
  loadSyncPlugin().then(res => {
    res.data?.forEach(item => {
      const {name, category, type, icon, extraParams, staticMap, datasourceRole} = item
      const node = {
        name,
        catalog: category,
        type,
        icon,
        extraParams,
        isPlugin: true,
        staticMap,
        datasourceRole
      }
      const index = typeList.findIndex(ele => ele === node.catalog)
      if (index !== -1) {
        let copiedArr = JSON.parse(JSON.stringify(databaseList.value))
        copiedArr[index].push(node)
        databaseList.value = copiedArr
      }
    })
  })
}
listSyncPlugin()
const emits = defineEmits(["selectDsType"]);
const selectDs = dbInfo => {
  emits("selectDsType", dbInfo);
};
</script>

<template>
  <div class="ds-type-list">
    <template v-for="ele in currentTypeList" :key="ele.name">
      <div class="title-form_primary">
        {{ ele.name }}
      </div>
      <div class="item-container">
        <div
            v-for="db in ele.dbList"
            :key="db.type"
            class="db-card"
            @click="selectDs(db)"
        >
          <el-icon class="icon-border">
            <Icon v-if="db['isPlugin']" :static-content="db.icon"></Icon>
            <Icon v-else>
              <component :is="iconMap[db.type]" class="svg-icon"></component>
            </Icon>
          </el-icon>
          <p class="db-name">{{ db.name }}</p>
        </div>
      </div>
    </template>
  </div>
</template>

<style lang="less" scoped>
.ds-type-list {
  width: 100%;
  position: relative;
  display: flex;
  width: 100%;
  flex-wrap: wrap;

  .title-form_primary {
    margin-bottom: 16px;
  }

  .item-container {
    display: flex;
    width: calc(100% + 16px);
    flex-wrap: wrap;
    margin-left: -16px;
  }

  .db-card {
    height: 64px;
    width: 266px;
    display: flex;
    align-items: center;
    background: #ffffff;
    border: 1px solid #dee0e3;
    border-radius: 4px;
    margin-bottom: 16px;
    margin-left: 16px;
    padding: 16px;
    cursor: pointer;

    .icon-border {
      margin-right: 12px;
      font-size: 32px;
    }

    &:hover {
      box-shadow: 0px 6px 24px rgba(31, 35, 41, 0.08);
    }
  }

  .marLeft {
    margin-left: 0;
  }
}
</style>
