<script setup lang="ts">
import { findDvSqlBotDataset } from '@/api/aiSqlBot'
import { onMounted, reactive } from 'vue'
import { storeToRefs } from 'pinia'
import { dvMainStoreWithOut } from '@/store/modules/data-visualization/dvMain'
import { useI18n } from '@/hooks/web/useI18n'
import { Refresh } from '@element-plus/icons-vue'

const { t } = useI18n()

const dvMainStore = dvMainStoreWithOut()
const { dvInfo } = storeToRefs(dvMainStore)

const props = defineProps({
  assistantId: {
    type: String,
    required: false
  }
})

const newChat = (param?: any) => {
  const handler = window['sqlbot_assistant_handler']
  if (handler && handler[props.assistantId]) {
    handler[props.assistantId].createConversation(param)
  }
}

const init = () => {
  if (dvInfo.value.id) {
    findDvSqlBotDataset(dvInfo.value.id).then(res => {
      state.baseDatasetInfo = res.data
      state.curDatasetInfo = state.baseDatasetInfo[0]
      state.curDatasetId = state.curDatasetInfo.tableId
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
  state.baseDatasetInfo.forEach(datasetInfo => {
    if (datasetInfo.tableId === state.curDatasetId) {
      localStorage.setItem('dsId', state.curDatasetInfo.dsId)
      localStorage.setItem('tableId', state.curDatasetInfo.tableId)
      newChat()
    }
  })
}

const refresh = () => {
  init()
  newChat()
}
</script>

<template>
  <el-row class="de-sq-assistant">
    <span class="de-sq-tips">{{ t('visualization.cur_sq_dataset') }}</span>
    <el-select
      v-model="state.curDatasetId"
      class="de-sq-select"
      :teleported="false"
      size="small"
      @change="datasetSelect"
    >
      <el-option
        v-for="option in state.baseDatasetInfo"
        size="mini"
        :key="option.tableId"
        :value="option.tableId"
        :label="option.tableName"
      ></el-option>
    </el-select>
    <div class="de-sq-button">
      <el-button
        size="small"
        :title="t('visualization.refresh')"
        :icon="Refresh"
        text
        @click="refresh"
      />
    </div>
  </el-row>
</template>

<style lang="less">
.assistant-chat-main {
  padding: 0 0 40px;
}
</style>

<style scoped lang="less">
.de-sq-assistant {
  position: absolute;
  display: flex;
  bottom: 140px;
  left: 20px;
  width: 100%;
  z-index: 10;
  color: #646a73;
  background-color: #fff;
  font-size: 14px;
}

.de-sq-tips {
  font-size: 14px;
  line-height: 28px;
}

.de-sq-icon {
  font-size: 14px;
  line-height: 28px;
}

.de-sq-select {
  width: auto;
  min-width: 150px;
  max-width: 400px;
}

.de-sq-button {
  margin-left: 4px;
}
</style>
