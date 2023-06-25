<script lang="ts" setup>
import { shallowRef, PropType, computed } from 'vue'

export type DsType = 'OLTP' | 'OLAP' | 'DL' | 'OTHER' | 'LOCAL'
const props = defineProps({
  currentType: {
    type: String as PropType<DsType>,
    default: 'OLTP'
  }
})
let dsTypes = []
const nameMap = ['OLTP', 'OLAP', 'DL', 'OTHER', 'LOCAL']
const typeList = ['OLTP', 'OLAP', 'DL', 'OTHER', 'LOCAL']
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
      catalog: 'OLTP',
      extraParams:
        'characterEncoding=UTF-8&connectTimeout=5000&useSSL=false&allowPublicKeyRetrieval=true'
    },
    {
      type: 'TiDB',
      name: 'TiDB',
      catalog: 'OLTP',
      extraParams:
        'characterEncoding=UTF-8&connectTimeout=5000&useSSL=false&allowPublicKeyRetrieval=true'
    },
    {
      type: 'hive',
      name: 'Apache Hive',
      catalog: 'DL',
      extraParams: ''
    },
    {
      type: 'impala',
      name: 'Apache Impala',
      catalog: 'OLAP',
      extraParams: 'AuthMech=0'
    },
    {
      type: 'mariadb',
      name: 'MariaDB',
      catalog: 'OLTP',
      extraParams:
        'characterEncoding=UTF-8&connectTimeout=5000&useSSL=false&allowPublicKeyRetrieval=true'
    },
    {
      type: 'StarRocks',
      name: 'StarRocks',
      catalog: 'OLAP',
      extraParams:
        'characterEncoding=UTF-8&connectTimeout=5000&useSSL=false&allowPublicKeyRetrieval=true'
    },
    {
      type: 'pg',
      name: 'PostgreSQL',
      catalog: 'OLTP',
      extraParams: ''
    },
    {
      type: 'sqlServer',
      name: 'SQL Server',
      catalog: 'OLTP',
      extraParams: ''
    },
    {
      type: 'oracle',
      name: 'Oracle',
      catalog: 'OLTP',
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
      targetCharset: ['Default', 'GBK', 'UTF-8']
    },
    {
      type: 'mongo',
      name: 'MongoDB',
      catalog: 'OLTP',
      extraParams: 'rebuildschema=true&authSource=admin'
    },
    {
      type: 'ck',
      name: 'ClickHouse',
      catalog: 'OLAP',
      extraParams: ''
    },
    {
      type: 'db2',
      name: 'Db2',
      catalog: 'OLTP',
      extraParams: ''
    },
    {
      type: 'redshift',
      name: 'AWS Redshift',
      catalog: 'DL',
      extraParams: ''
    },
    {
      type: 'es',
      name: 'Elasticsearch',
      catalog: 'OLAP',
      extraParams: ''
    },
    {
      type: 'API',
      name: 'API',
      catalog: 'OTHER',
      extraParams: ''
    },
    {
      type: 'Excel',
      name: 'Excel',
      catalog: 'LOCAL',
      extraParams: ''
    }
  ]
  const arr = [[], [], [], [], []]
  dsTypes.forEach(item => {
    const index = typeList.findIndex(ele => ele === item.catalog)
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
const selectDs = ({ type }) => {
  emits('selectDsType', type)
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
