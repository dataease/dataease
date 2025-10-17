<script setup lang="ts">
import { findDvSqlBotDataset } from '@/api/aiSqlBot'
import { onMounted, reactive } from 'vue'
import { storeToRefs } from 'pinia'
import { dvMainStoreWithOut } from '@/store/modules/data-visualization/dvMain'
import { useI18n } from '@/hooks/web/useI18n'
const { t } = useI18n()

const dvMainStore = dvMainStoreWithOut()
const { dvInfo } = storeToRefs(dvMainStore)
const init = () => {
  if (dvInfo.value.id) {
    findDvSqlBotDataset(dvInfo.value.id).then(res => {
      state.baseDatasetInfo = res.data
      state.curDatasetInfo = state.baseDatasetInfo[0]
      datasetSelect()
    })
  }
}
onMounted(() => {
  init()
})

const state = reactive({
  baseDatasetInfo: [],
  curDatasetInfo: null,
  curDatasetId: null
})

const datasetSelect = () => {
  localStorage.setItem('dsId', state.curDatasetInfo.dsId)
  localStorage.setItem('tableId', state.curDatasetInfo.tableId)
}
</script>

<template>
  <el-row class="de-sq-assistant">
    <span class="de-sq-tips">{{ t('visualization.cur_sq_dataset') }}</span>
    <el-select
      v-model="state.curDatasetInfo"
      class="de-sq-select"
      :teleported="false"
      size="small"
      @change="datasetSelect"
    >
      <el-option
        v-for="option in state.baseDatasetInfo"
        size="mini"
        :key="option.tableId"
        :value="option"
        :label="option.tableName"
      ></el-option>
    </el-select>
  </el-row>
</template>

<style scoped lang="less">
.de-sq-assistant {
  position: absolute;
  display: flex;
  bottom: 140px;
  left: 20px;
  width: 100%;
  z-index: 10;
  color: #646a73;
  font-size: 14px;
}

.de-sq-tips {
  font-size: 14px;
  line-height: 28px;
}

.de-sq-select {
  width: 150px;
}
</style>
