<script lang="ts" setup>
import { shallowRef, PropType, computed } from 'vue'
import { dsTypes, typeList, nameMap } from './option'

export type DsType = 'OLTP' | 'OLAP' | 'DL' | 'OTHER' | 'LOCAL' | 'latestUse' | 'all'
const props = defineProps({
  currentType: {
    type: String as PropType<DsType>,
    default: 'OLTP'
  },
  latestUseTypes: {
    type: Array
  }
})

const databaseList = shallowRef([])
const currentTypeList = computed(() => {
  if (props.currentType == 'all') {
    return typeList.map((ele, index) => {
      return { name: nameMap[ele], dbList: databaseList.value[index] }
    })
  }
  if (props.currentType === 'latestUse') {
    let catalogList = []
    let dstypes = []
    props.latestUseTypes.forEach(type => {
      dsTypes.forEach(item => {
        if (item.type === type && catalogList.indexOf(item.catalog) === -1) {
          catalogList.push(item.catalog)
        }
      })
    })
    let dbList = []
    catalogList.forEach(catalog => {
      props.latestUseTypes.forEach(type => {
        dsTypes.forEach(item => {
          if (item.type === type && item.catalog === catalog) {
            dbList.push(item)
          }
        })
      })
    })
    dstypes.push({ name: '最近创建', dbList })
    return dstypes
  }
  const index = typeList.findIndex(ele => props.currentType === ele)
  return [{ name: nameMap[props.currentType], dbList: databaseList.value[index] }] || []
})
const getDatasourceTypes = () => {
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
    <template v-for="ele in currentTypeList" :key="ele.name">
      <div class="title-form_primary">
        {{ ele.name }}
      </div>
      <div class="item-container">
        <div v-for="db in ele.dbList" :key="db.type" class="db-card" @click="selectDs(db)">
          <el-icon class="icon-border">
            <Icon :name="`${db.type}-ds`"></Icon>
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
