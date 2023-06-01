<script lang="ts" setup>
import { reactive, ref } from 'vue'
import { ElIcon, ElMessage } from 'element-plus-secondary'
import { Icon } from '@/components/icon-custom'
import type { DsType } from './DsTypeList.vue'
import DsTypeList from './DsTypeList.vue'
import { useI18n } from '@/hooks/web/useI18n'
import EditorDetail from './EditorDetail.vue'
import ExcelDetail from './ExcelDetail.vue'
import { validate, save } from '../../../../../api/datasource.ts'
import { Base64 } from 'js-base64'

interface Node {
  name: string
  id: string
  type: DsType
}

const { t } = useI18n()

const dsType = ['OLTP', 'OLAP', 'dataWarehouseLake', 'OTHER']

const nameMap = {
  OLTP: 'OLTP',
  OLAP: 'OLAP',
  dataWarehouseLake: t('datasource.dl'),
  OTHER: t('datasource.other'),
  LOCAL: t('datasource.local_file'),
  API: 'API:'
}

const state = reactive({
  addeddatasourceList: [],
  datasourceTree: [],
  datasourceTypeList: [],
  dsTableData: [],
  paginationConfig: {
    currentPage: 1,
    pageSize: 10,
    total: 0
  },
  filterTable: []
})
state.datasourceTree = dsType.map(ele => {
  return {
    name: nameMap[ele],
    type: ele
  }
})

export interface Configuration {
  dataBase: string
  extraParams: string
  username: string
  password: string
  host: string
  authMethod: string
  port: string
}

export interface ApiConfiguration {
  id: string
  name: string
  method: string
  url: string
  status: string
  useJsonPath: boolean
  serialNumber: number
}

export interface SyncSetting {
  id: number
  updateType: string
  syncRate: string
  simpleCronValue: number
  simpleCronType: string
  startTime: number
  endTime: number
  endLimit: string
  cron: string
}

const activeStep = ref(0)
const detail = ref()
const currentType = ref<DsType>('OLTP')
const nickName = ref('')
const currentDsType = ref('')
const selectDsType = (type: string) => {
  currentDsType.value = type
  activeStep.value = activeStep.value + 1
  detail.value.initForm(type)
}
const handleNodeClick = (data: Node) => {
  currentType.value = data.type
}

const next = () => {
  if (currentDsType.value === '') {
    ElMessage.warning(t('datasource.select_type'))
    return
  }
  activeStep.value = activeStep.value + 1
}
const prev = () => {
  activeStep.value = activeStep.value - 1
}

const validateDS = () => {
  const request = JSON.parse(JSON.stringify(form))
  if (form.type === 'API') {
    if (form.apiConfiguration.length == 0) {
      return
    }
    request.configuration = Base64.encode(JSON.stringify(request.apiConfiguration))
    request.syncSetting.startTime = new Date(request.syncSetting.startTime).getTime()
    request.syncSetting.endTime = new Date(request.syncSetting.endTime).getTime()
  } else {
    request.configuration = Base64.encode(JSON.stringify(request.configuration))
  }
  validate(request).then(res => {
    ElMessage.success(t('datasource.validate_success'))
  })
}

const saveDS = () => {
  const request = JSON.parse(JSON.stringify(form))
  if (form.type === 'API') {
    if (form.apiConfiguration.length == 0) {
      return
    }
    request.configuration = Base64.encode(JSON.stringify(request.apiConfiguration))
    request.syncSetting.startTime = new Date(request.syncSetting.startTime).getTime()
    request.syncSetting.endTime = new Date(request.syncSetting.endTime).getTime()
  } else {
    request.configuration = Base64.encode(JSON.stringify(request.configuration))
  }

  save(request).then(res => {
    ElMessage.success(t('common.save_success'))
  })
}

// const save = (data: Node) => {
//   currentType.value = data.type
// }
const form = reactive<{
  name: string
  description: string
  type: string
  configuration?: Configuration
  apiConfiguration?: ApiConfiguration[]
  syncSetting?: SyncSetting
}>({
  name: '',
  description: '',
  type: 'API',
  apiConfiguration: []
})
</script>

<template>
  <div class="datasource">
    <div class="ds-type-select">
      <div class="title">
        {{ t('datasource.create') }}
        <el-input class="m24 w100" v-model="nickName" clearable>
          <template #prefix>
            <el-icon>
              <Icon name="icon_search-outline_outlined"></Icon>
            </el-icon>
          </template>
        </el-input>
      </div>
      <div
        :key="ele.name"
        @click="handleNodeClick(ele)"
        v-for="ele in state.datasourceTree"
        class="list-item_primary"
      >
        <span :title="ele.name" class="label">{{ ele.name }}</span>
      </div>
    </div>
    <div class="ds-editor">
      <div class="editor-step flex-center">
        <el-steps :active="activeStep" align-center>
          <el-step :title="t('datasource.select_ds_type')" />
          <el-step :title="t('datasource.ds_info')" />
          <el-step v-if="currentDsType === 'API'" :title="t('datasource.sync_info')" />
        </el-steps>
      </div>
      <div class="editor-content">
        <ds-type-list
          v-show="!currentDsType"
          @select-ds-type="selectDsType"
          :current-type="currentType"
        ></ds-type-list>
        <editor-detail
          ref="detail"
          :form="form"
          :active-step="activeStep"
          v-show="currentDsType && currentDsType !== 'Db2'"
        ></editor-detail>
        <template v-if="currentDsType == 'Db2'">
          <excel-detail></excel-detail>
        </template>
      </div>
      <div class="editor-footer">
        <el-button secondary> {{ t('common.cancel') }}</el-button>
        <el-button
          v-show="
            (activeStep === 0 && currentDsType !== 'API') ||
            (activeStep !== 2 && currentDsType === 'API')
          "
          type="primary"
          @click="next"
        >
          {{ t('common.next') }}</el-button
        >
        <el-button v-show="activeStep !== 0" type="primary" @click="prev">
          {{ t('common.prev') }}</el-button
        >
        <el-button v-show="activeStep === 1" type="primary" @click="validateDS">
          {{ t('datasource.validate') }}</el-button
        >
        <el-button
          v-show="
            (activeStep === 1 && currentDsType !== 'API') ||
            (activeStep === 2 && currentDsType === 'API')
          "
          type="primary"
          @click="saveDS"
        >
          {{ t('common.sure') }}</el-button
        >
      </div>
    </div>
  </div>
</template>

<style lang="less" scoped>
.datasource {
  display: flex;
  width: 100%;
  height: 100%;
  background: #fff;
  .ds-type-select {
    width: 269px;
    padding: 24px;
    border-right: 1px solid #ccc;
    .title {
      display: flex;
      justify-content: space-between;
      font-family: PingFang SC;
      font-size: 20px;
      font-weight: 500;
      color: var(--TextPrimary, #1f2329);
      box-sizing: border-box;
      flex-wrap: wrap;
      position: sticky;
      top: 0;
      left: 24px;
      z-index: 5;
      background: white;
      &::before {
        content: '';
        width: 100%;
        height: 24px;
        top: -24px;
        position: absolute;
        z-index: 5;
        left: 0;
        background: white;
      }
    }

    .m24 {
      margin: 24px 0;
    }
    .w100 {
      width: 100%;
    }
  }
  .ds-editor {
    flex: 1;
    .editor-step {
      height: 100px;
      border-bottom: 1px solid #cccc;
      padding: 24px;
    }

    .editor-content {
      padding: 24px;
      height: calc(100vh - 180px);
      overflow-y: auto;
    }

    .editor-footer {
      height: 80px;
      padding-right: 24px;
      text-align: right;
    }
  }
}
</style>
<style lang="less">
.custom-tree-node {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding-right: 8px;
  box-sizing: content-box;

  .label-tooltip {
    width: 60%;
    overflow: hidden;
    white-space: nowrap;
    text-overflow: ellipsis;
  }
}
</style>
