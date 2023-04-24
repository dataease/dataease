<script lang="ts" setup>
import { shallowRef, PropType, computed } from 'vue'

export type DsType = 'OLTP' | 'OLAP' | 'dataWarehouseLake' | 'OTHER'
const props = defineProps({
  currentType: {
    type: String as PropType<DsType>,
    default: 'OLTP'
  }
})
let dsTypes = []
const nameMap = ['OLTP', 'OLAP', 'dataWarehouseLake', 'OTHER']
const typeList = ['OLTP', 'OLAP', 'DL', 'OTHER']
const databaseList = shallowRef([])
const currentTypeList = computed(() => {
  const index = nameMap.findIndex(ele => props.currentType === ele)
  return databaseList.value[index] || []
})
const getDatasourceTypes = () => {
  dsTypes = [
    {
      type: 'mysql',
      name: 'MySQL',
      isPlugin: false,
      calculationMode: 'DIRECT_AND_SYNC',
      databaseClassification: 'OLTP',
      extraParams:
        'characterEncoding=UTF-8&connectTimeout=5000&useSSL=false&allowPublicKeyRetrieval=true',
      charset: null,
      targetCharset: null,
      isJdbc: true,
      keywordPrefix: '`',
      keywordSuffix: '`',
      aliasPrefix: '',
      aliasSuffix: '',
      jdbc: true,
      plugin: false
    },
    {
      type: 'TiDB',
      name: 'TiDB',
      isPlugin: false,
      calculationMode: 'DIRECT_AND_SYNC',
      databaseClassification: 'OLTP',
      extraParams:
        'characterEncoding=UTF-8&connectTimeout=5000&useSSL=false&allowPublicKeyRetrieval=true',
      charset: null,
      targetCharset: null,
      isJdbc: true,
      keywordPrefix: '`',
      keywordSuffix: '`',
      aliasPrefix: '',
      aliasSuffix: '',
      jdbc: true,
      plugin: false
    },
    {
      type: 'hive',
      name: 'Apache Hive',
      isPlugin: false,
      calculationMode: 'DIRECT',
      databaseClassification: 'DL',
      extraParams: '',
      charset: null,
      targetCharset: null,
      isJdbc: true,
      keywordPrefix: '`',
      keywordSuffix: '`',
      aliasPrefix: '',
      aliasSuffix: '',
      jdbc: true,
      plugin: false
    },
    {
      type: 'impala',
      name: 'Apache Impala',
      isPlugin: false,
      calculationMode: 'DIRECT',
      databaseClassification: 'OLAP',
      extraParams: 'AuthMech=0',
      charset: null,
      targetCharset: null,
      isJdbc: true,
      keywordPrefix: '`',
      keywordSuffix: '`',
      aliasPrefix: "'",
      aliasSuffix: "'",
      jdbc: true,
      plugin: false
    },
    {
      type: 'mariadb',
      name: 'MariaDB',
      isPlugin: false,
      calculationMode: 'DIRECT_AND_SYNC',
      databaseClassification: 'OLTP',
      extraParams:
        'characterEncoding=UTF-8&connectTimeout=5000&useSSL=false&allowPublicKeyRetrieval=true',
      charset: null,
      targetCharset: null,
      isJdbc: true,
      keywordPrefix: '`',
      keywordSuffix: '`',
      aliasPrefix: "'",
      aliasSuffix: "'",
      jdbc: true,
      plugin: false
    },
    {
      type: 'StarRocks',
      name: 'StarRocks',
      isPlugin: false,
      calculationMode: 'DIRECT_AND_SYNC',
      databaseClassification: 'OLAP',
      extraParams:
        'characterEncoding=UTF-8&connectTimeout=5000&useSSL=false&allowPublicKeyRetrieval=true',
      charset: null,
      targetCharset: null,
      isJdbc: true,
      keywordPrefix: '`',
      keywordSuffix: '`',
      aliasPrefix: "'",
      aliasSuffix: "'",
      jdbc: true,
      plugin: false
    },
    {
      type: 'ds_doris',
      name: 'Doris',
      isPlugin: false,
      calculationMode: 'DIRECT_AND_SYNC',
      databaseClassification: 'OLAP',
      extraParams:
        'characterEncoding=UTF-8&connectTimeout=5000&useSSL=false&allowPublicKeyRetrieval=true',
      charset: null,
      targetCharset: null,
      isJdbc: true,
      keywordPrefix: '`',
      keywordSuffix: '`',
      aliasPrefix: "'",
      aliasSuffix: "'",
      jdbc: true,
      plugin: false
    },
    {
      type: 'pg',
      name: 'PostgreSQL',
      isPlugin: false,
      calculationMode: 'DIRECT_AND_SYNC',
      databaseClassification: 'OLTP',
      extraParams: '',
      charset: null,
      targetCharset: null,
      isJdbc: true,
      keywordPrefix: '"',
      keywordSuffix: '"',
      aliasPrefix: '"',
      aliasSuffix: '"',
      jdbc: true,
      plugin: false
    },
    {
      type: 'sqlServer',
      name: 'SQL Server',
      isPlugin: false,
      calculationMode: 'DIRECT_AND_SYNC',
      databaseClassification: 'OLTP',
      extraParams: '',
      charset: null,
      targetCharset: null,
      isJdbc: true,
      keywordPrefix: '"',
      keywordSuffix: '"',
      aliasPrefix: '"',
      aliasSuffix: '"',
      jdbc: true,
      plugin: false
    },
    {
      type: 'oracle',
      name: 'Oracle',
      isPlugin: false,
      calculationMode: 'DIRECT_AND_SYNC',
      databaseClassification: 'OLTP',
      extraParams: '',
      charset: [
        'Default',
        'GBK',
        'BIG5',
        'ISO-8859-1',
        'UTF-8',
        'UTF-16',
        'CP850',
        'EUC_JP',
        'EUC_KR'
      ],
      targetCharset: ['Default', 'GBK', 'UTF-8'],
      isJdbc: true,
      keywordPrefix: '"',
      keywordSuffix: '"',
      aliasPrefix: '"',
      aliasSuffix: '"',
      jdbc: true,
      plugin: false
    },
    {
      type: 'mongo',
      name: 'MongoDB',
      isPlugin: false,
      calculationMode: 'DIRECT',
      databaseClassification: 'OLTP',
      extraParams: 'rebuildschema=true&authSource=admin',
      charset: null,
      targetCharset: null,
      isJdbc: true,
      keywordPrefix: '`',
      keywordSuffix: '`',
      aliasPrefix: '"',
      aliasSuffix: '"',
      jdbc: true,
      plugin: false
    },
    {
      type: 'ck',
      name: 'ClickHouse',
      isPlugin: false,
      calculationMode: 'DIRECT',
      databaseClassification: 'OLAP',
      extraParams: '',
      charset: null,
      targetCharset: null,
      isJdbc: true,
      keywordPrefix: '`',
      keywordSuffix: '`',
      aliasPrefix: '',
      aliasSuffix: '',
      jdbc: true,
      plugin: false
    },
    {
      type: 'db2',
      name: 'Db2',
      isPlugin: false,
      calculationMode: 'DIRECT_AND_SYNC',
      databaseClassification: 'OLTP',
      extraParams: '',
      charset: null,
      targetCharset: null,
      isJdbc: true,
      keywordPrefix: '"',
      keywordSuffix: '"',
      aliasPrefix: '"',
      aliasSuffix: '"',
      jdbc: true,
      plugin: false
    },
    {
      type: 'redshift',
      name: 'AWS Redshift',
      isPlugin: false,
      calculationMode: 'DIRECT',
      databaseClassification: 'DL',
      extraParams: '',
      charset: null,
      targetCharset: null,
      isJdbc: true,
      keywordPrefix: '"',
      keywordSuffix: '"',
      aliasPrefix: '"',
      aliasSuffix: '"',
      jdbc: true,
      plugin: false
    },
    {
      type: 'es',
      name: 'Elasticsearch',
      isPlugin: false,
      calculationMode: 'DIRECT',
      databaseClassification: 'OLAP',
      extraParams: '',
      charset: null,
      targetCharset: null,
      isJdbc: false,
      keywordPrefix: '"',
      keywordSuffix: '"',
      aliasPrefix: '"',
      aliasSuffix: '"',
      jdbc: false,
      plugin: false
    },
    {
      type: 'api',
      name: 'API',
      isPlugin: false,
      calculationMode: 'SYNC',
      databaseClassification: 'OTHER',
      extraParams: 'rebuildschema=true&authSource=admin',
      charset: null,
      targetCharset: null,
      isJdbc: false,
      keywordPrefix: '"',
      keywordSuffix: '"',
      aliasPrefix: '"',
      aliasSuffix: '"',
      jdbc: false,
      plugin: false
    },
    {
      type: 'influxdb',
      name: 'InfluxDB',
      isPlugin: true,
      calculationMode: 'DIRECT',
      databaseClassification: 'OTHER',
      extraParams: '',
      charset: null,
      targetCharset: null,
      isJdbc: true,
      keywordPrefix: '"',
      keywordSuffix: '"',
      aliasPrefix: '"',
      aliasSuffix: '"',
      jdbc: true,
      plugin: true
    },
    {
      type: 'sls',
      name: 'SLS',
      isPlugin: true,
      calculationMode: 'DIRECT',
      databaseClassification: 'OTHER',
      extraParams: '',
      charset: null,
      targetCharset: null,
      isJdbc: true,
      keywordPrefix: '"',
      keywordSuffix: '"',
      aliasPrefix: '"',
      aliasSuffix: '"',
      jdbc: true,
      plugin: true
    },
    {
      type: 'kingbase',
      name: 'KingBase',
      isPlugin: true,
      calculationMode: 'DIRECT_AND_SYNC',
      databaseClassification: 'OLTP',
      extraParams: '',
      charset: null,
      targetCharset: null,
      isJdbc: true,
      keywordPrefix: '"',
      keywordSuffix: '"',
      aliasPrefix: '"',
      aliasSuffix: '"',
      jdbc: true,
      plugin: true
    },
    {
      type: 'mongobi',
      name: 'Mongodb-BI',
      isPlugin: true,
      calculationMode: 'DIRECT',
      databaseClassification: 'OLTP',
      extraParams:
        'characterEncoding=UTF-8&connectTimeout=5000&useSSL=true&allowPublicKeyRetrieval=true&verifyServerCertificate=false',
      charset: null,
      targetCharset: null,
      isJdbc: true,
      keywordPrefix: '`',
      keywordSuffix: '`',
      aliasPrefix: '',
      aliasSuffix: '',
      jdbc: true,
      plugin: true
    },
    {
      type: 'maxcompute',
      name: 'Maxcompute',
      isPlugin: true,
      calculationMode: 'DIRECT',
      databaseClassification: 'DL',
      extraParams: '',
      charset: null,
      targetCharset: null,
      isJdbc: true,
      keywordPrefix: '"',
      keywordSuffix: '"',
      aliasPrefix: '"',
      aliasSuffix: '"',
      jdbc: true,
      plugin: true
    },
    {
      type: 'presto',
      name: 'Presto',
      isPlugin: true,
      calculationMode: 'DIRECT',
      databaseClassification: 'OLAP',
      extraParams: '',
      charset: null,
      targetCharset: null,
      isJdbc: true,
      keywordPrefix: '',
      keywordSuffix: '',
      aliasPrefix: '',
      aliasSuffix: '',
      jdbc: true,
      plugin: true
    },
    {
      type: 'dm',
      name: 'DM',
      isPlugin: true,
      calculationMode: 'DIRECT',
      databaseClassification: 'OLTP',
      extraParams: '',
      charset: null,
      targetCharset: null,
      isJdbc: true,
      keywordPrefix: '"',
      keywordSuffix: '"',
      aliasPrefix: '"',
      aliasSuffix: '"',
      jdbc: true,
      plugin: true
    },
    {
      type: 'kylin',
      name: 'Kylin',
      isPlugin: true,
      calculationMode: 'DIRECT',
      databaseClassification: 'DL',
      extraParams: '',
      charset: null,
      targetCharset: null,
      isJdbc: true,
      keywordPrefix: '',
      keywordSuffix: '',
      aliasPrefix: '',
      aliasSuffix: '',
      jdbc: true,
      plugin: true
    }
  ]
  const arr = [[], [], [], []]
  dsTypes.forEach(item => {
    const index = typeList.findIndex(ele => ele === item.databaseClassification)
    if (index !== -1) {
      arr[index].push(item)
    }
  })
  databaseList.value = arr.map(ele => {
    return ele.sort((a, b) => {
      return a.name.toLowerCase().charCodeAt(0) - b.name.toLowerCase().charCodeAt(0)
    })
  })
}
getDatasourceTypes()
const emits = defineEmits(['selectDsType'])
const selectDs = ({ name }) => {
  emits('selectDsType', name)
}
</script>

<template>
  <div class="ds-type-list">
    <div class="item-container">
      <div v-for="db in currentTypeList" :key="db.type" class="db-card" @click="selectDs(db)">
        <img src="https://de.fit2cloud.com/img/db2.bbbf4043.jpg" alt="" />
        <p class="db-name">{{ db.name }}</p>
      </div>
    </div>
  </div>
</template>

<style lang="less" scoped>
.ds-type-list {
  width: 100%;
  position: relative;
  display: flex;
  width: 100%;
  flex-wrap: wrap;

  .item-container {
    display: flex;
    width: 100%;
    flex-wrap: wrap;
  }

  .db-card {
    height: 141px;
    width: 177.6px;
    display: flex;
    flex-wrap: wrap;
    background: #ffffff;
    border: 1px solid #dee0e3;
    border-radius: 4px;
    margin-bottom: 16px;
    margin-right: 16px;
    cursor: pointer;
    img {
      width: 100%;
      height: 102px;
      border-top-left-radius: 4px;
      border-top-right-radius: 4px;
    }
    .db-name {
      width: 100%;
      margin: 0;
      display: flex;
      align-items: center;
      padding: 8px 12px;
      border-top: 1px solid rgba(#1f2329, 0.15);
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
