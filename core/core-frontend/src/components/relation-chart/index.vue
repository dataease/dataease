<script lang="ts" setup>
import { ref, reactive, nextTick } from 'vue'
import { XpackComponent } from '@/components/plugin'
import { cloneDeep } from 'lodash-es'
import {
  getDatasourceRelationship as getDatasourceRelation,
  getDatasetRelationship as getDatasetRelation
} from '@/api/relation/index'
const relationDrawer = ref(false)
const chartSize = reactive({
  height: 0,
  width: 0
})
const getChartSize = () => {
  const dom = document.querySelector('.relation-drawer_content')
  if (!dom) return
  Object.assign(chartSize, {
    height: dom.offsetHeight + 'px',
    width: dom.offsetWidth + 'px'
  })
}

const consanguinity = ref()
let resRef = null
const getDatasourceRelationship = id => {
  getDatasourceRelation(id)
    .then(res => {
      resRef = cloneDeep(res || {})
    })
    .finally(() => {
      tableLoading.value = false
      nextTick(() => {
        consanguinity.value.invokeMethod({
          methodName: 'getChartData',
          args: {
            info: current,
            res: resRef
          }
        })
      })
    })
}
const getDatasetRelationship = id => {
  getDatasetRelation(id)
    .then(res => {
      resRef = cloneDeep(res || {})
    })
    .finally(() => {
      tableLoading.value = false
      nextTick(() => {
        consanguinity.value.invokeMethod({
          methodName: 'getChartData',
          args: {
            info: current,
            res: resRef
          }
        })
      })
    })
}

const current = {
  queryType: '',
  num: '',
  label: ''
}
const tableLoading = ref(false)
const getChartData = obj => {
  Object.assign(current, obj || {})
  const { queryType, num } = current
  tableLoading.value = true
  relationDrawer.value = true
  nextTick(() => {
    getChartSize()
    switch (queryType) {
      case 'datasource':
        getDatasourceRelationship(num)
        break
      case 'dataset':
        getDatasetRelationship(num)
        break
      default:
        break
    }
  })
}

defineExpose({
  getChartData
})
</script>

<template>
  <el-drawer
    title="血缘关系图"
    v-model="relationDrawer"
    custom-class="de-relation-drawer"
    size="1200px"
    direction="rtl"
  >
    <div v-loading="tableLoading" class="relation-drawer_content">
      <XpackComponent
        ref="consanguinity"
        :chart-size="chartSize"
        :current="current"
        detailDisabled
        jsname="L21lbnUvc3lzdGVtL2Fzc29jaWF0aW9uL0NoYXJ0"
      />
    </div>
  </el-drawer>
</template>

<style lang="less">
.de-relation-drawer {
  .ed-drawer__body {
    padding-bottom: 24px;
  }
  .relation-drawer_content {
    border: 1px solid #dee0e3;
    width: 100%;
    height: 100%;
    background: #f5f6f7;
    border-radius: 4px;
    position: relative;
  }
}
</style>
