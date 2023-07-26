<script lang="ts" setup>
import { reactive, ref, computed, watch, nextTick } from 'vue'
import { ElIcon, ElMessage } from 'element-plus-secondary'
import CreatDsGroup from './CreatDsGroup.vue'
import { Icon } from '@/components/icon-custom'
import type { DsType } from './DsTypeList.vue'
import DsTypeList from './DsTypeList.vue'
import { useI18n } from '@/hooks/web/useI18n'
import EditorDetail from './EditorDetail.vue'
import ExcelDetail from './ExcelDetail.vue'
import { save, validate } from '@/api/datasource'
import { Base64 } from 'js-base64'
import type { Param } from './ExcelDetail.vue'
import { dsTypes, typeList, nameMap } from './option'
import { uuid } from 'vue-uuid'
import { cloneDeep } from 'lodash-es'
interface Node {
  name: string
  id: string
  type: DsType
}

interface Tree {
  [key: string]: any
}
interface Form {
  name: string
  pid?: string
  description: string
  type: string
  configuration?: Configuration
  apiConfiguration?: ApiConfiguration[]
  syncSetting?: SyncSetting
}

const { t } = useI18n()
const creatDsFolder = ref()

const state = reactive({
  datasourceTree: []
})
state.datasourceTree = typeList.map(ele => {
  return {
    name: nameMap[ele],
    type: ele
  }
})

export interface Configuration {
  dataBase: string
  connectionType: string
  schema: string
  extraParams: string
  username: string
  password: string
  host: string
  authMethod: string
  port: string
  initialPoolSize: string
  minPoolSize: string
  maxPoolSize: string
  queryTimeout: string
}

export interface ApiConfiguration {
  id: string
  name: string
  deTableName: string
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
const excel = ref()
const currentType = ref<DsType>('OLTP')
const filterText = ref('')
const currentDsType = ref('')
const selectDsType = (type: string) => {
  currentDsType.value = type
  activeStep.value = 1
  detail.value.initForm(type)
}

const handleDsNodeClick = data => {
  if (!data.type) return
  selectDsType(data.type)
}
const handleNodeClick = (data: Node) => {
  currentType.value = data.type
}

watch(filterText, val => {
  if (activeStep.value === 1) {
    dsTree.value.filter(val)
  }
})

const dsTree = ref()
const defaultProps = {
  children: 'dbList',
  label: 'name'
}
const filterNode = (value: string, data: Tree) => {
  if (!value) return true
  return data.name.toLowerCase().includes(value)
}
const getDsIconName = data => {
  if (!data.catalog) return 'dv-folder'
  return 'mysql-frame'
}
const databaseList = ref([])
const currentTypeList = computed(() => {
  return typeList.map((ele, index) => {
    return { name: nameMap[ele], dbList: databaseList.value[index] }
  })
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

const next = () => {
  if (currentDsType.value === '') {
    ElMessage.error(t('datasource.select_type'))
    return
  }
  activeStep.value = activeStep.value + 1
}

const complete = (params, successCb, finallyCb) => {
  excel.value.saveExcelDs(params, successCb, finallyCb)
  return
}

const prev = () => {
  if (activeStep.value === 1) {
    currentDsType.value = ''
  }
  activeStep.value = activeStep.value - 1
}

const validateDS = () => {
  const request = JSON.parse(JSON.stringify(form)) as unknown as Omit<
    Form,
    'configuration' | 'apiConfiguration'
  > & {
    configuration: string
    apiConfiguration: string
  }
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
  validate(request).then(() => {
    ElMessage.success(t('datasource.validate_success'))
  })
}

const saveDS = () => {
  const request = JSON.parse(JSON.stringify(form)) as unknown as Omit<
    Form,
    'configuration' | 'apiConfiguration'
  > & {
    configuration: string
    apiConfiguration: string
  }
  if (form.type === 'Excel') {
    if (editDs.value) {
      complete(null, null, null)
    } else {
      creatDsFolder.value.createInit('datasource', { id: pid.value }, '', form2.name)
    }
    return
  } else if (form.type === 'API') {
    if (form.apiConfiguration.length == 0) {
      return
    }
    for (var i = 0; i < request.apiConfiguration.length; i++) {
      if (
        request.apiConfiguration[i].deTableName === '' ||
        request.apiConfiguration[i].deTableName === undefined ||
        request.apiConfiguration[i].deTableName === null
      ) {
        request.apiConfiguration[i].deTableName =
          'api_' + request.apiConfiguration[i].name + '_' + uuid.v1()
      }
    }
    request.configuration = Base64.encode(JSON.stringify(request.apiConfiguration))
    request.syncSetting.startTime = new Date(request.syncSetting.startTime).getTime()
    request.syncSetting.endTime = new Date(request.syncSetting.endTime).getTime()
  } else {
    request.configuration = Base64.encode(JSON.stringify(request.configuration))
  }
  if (editDs.value) {
    save(request).then(() => {
      ElMessage.success('保存数据源成功')
    })
  } else {
    creatDsFolder.value.createInit('datasource', { id: pid.value, request }, '', form.name)
  }
}
const form = reactive<Form>({
  name: '',
  description: '',
  type: 'API',
  apiConfiguration: []
})
const form2 = reactive<Param>({
  type: '',
  editType: 0,
  name: '',
  id: '0'
})
const visible = ref(false)
const editDs = ref(false)
const pid = ref('0')

const init = (nodeInfo: Form | Param, id?: string) => {
  editDs.value = !!nodeInfo
  if (!!nodeInfo) {
    if (nodeInfo.type == 'Excel') {
      Object.assign(form2, cloneDeep(nodeInfo))
    } else {
      Object.assign(form, cloneDeep(nodeInfo))
    }
    pid.value = nodeInfo.pid || '0'
  } else {
    pid.value = id || '0'
  }
  activeStep.value = Number(editDs.value)
  visible.value = true
  if (!!nodeInfo) {
    nextTick(() => {
      selectDsType(nodeInfo.type)
    })
  }
}

defineExpose({
  init
})
</script>

<template>
  <el-drawer
    :close-on-click-modal="false"
    size="calc(100% - 100px)"
    modal-class="datasource-drawer-fullscreen"
    direction="btt"
    :show-close="false"
    v-model="visible"
  >
    <template #header="{ close }">
      <span>{{ t('datasource.create') }}</span>
      <div class="editor-step flex-center">
        <el-steps space="150px" :active="activeStep" align-center>
          <el-step>
            <template #icon>
              <div class="step-icon">
                <span class="icon">
                  {{ activeStep <= 0 ? '1' : '' }}
                </span>
                <span class="title">{{ t('datasource.select_ds_type') }}</span>
              </div>
            </template>
          </el-step>
          <el-step>
            <template #icon>
              <div class="step-icon">
                <span class="icon">
                  {{ activeStep <= 1 ? '2' : '' }}
                </span>
                <span class="title">{{ t('datasource.ds_info') }}</span>
              </div>
            </template>
          </el-step>
          <el-step v-if="currentDsType === 'API'">
            <template #icon>
              <div class="step-icon">
                <span class="icon">
                  {{ activeStep <= 2 ? '3' : '' }}
                </span>
                <span class="title">{{ t('datasource.sync_info') }}</span>
              </div>
            </template>
          </el-step>
        </el-steps>
      </div>
      <el-icon @click="close" class="datasource-close">
        <Icon name="icon_close_outlined"></Icon>
      </el-icon>
    </template>
    <div class="datasource">
      <div class="ds-type-select">
        <div class="title">
          <el-input class="m24 w100" v-model="filterText" clearable>
            <template #prefix>
              <el-icon>
                <Icon name="icon_search-outline_outlined"></Icon>
              </el-icon>
            </template>
          </el-input>
        </div>
        <template v-if="activeStep === 0">
          <p
            :class="currentType === 'latestUse' && 'active'"
            @click="handleNodeClick({ type: 'latestUse', name: 'latestUse', id: 'latestUse' })"
            class="list-item_primary"
          >
            最近使用
            <el-divider />
          </p>
          <p
            :class="currentType === 'all' && 'active'"
            @click="handleNodeClick({ type: 'all', name: 'all', id: 'all' })"
            class="list-item_primary"
          >
            全部
          </p>
          <div
            :key="ele.name"
            @click="handleNodeClick(ele)"
            v-for="ele in state.datasourceTree"
            class="list-item_primary"
            :class="currentType === ele.type && 'active'"
          >
            <span :title="ele.name" class="label">{{ ele.name }}</span>
          </div>
        </template>
        <el-tree
          :expand-on-click-node="false"
          menu
          v-if="activeStep > 0"
          ref="dsTree"
          :data="currentTypeList"
          :props="defaultProps"
          :filter-node-method="filterNode"
          @node-click="handleDsNodeClick"
        >
          <template #default="{ node, data }">
            <span class="custom-tree-node">
              <el-icon v-if="!!data.catalog" class="icon-border" style="width: 18px; height: 18px">
                <Icon :name="getDsIconName(data)"></Icon>
              </el-icon>
              <span :title="node.label" class="label-tooltip">{{ node.label }}</span>
            </span>
          </template>
        </el-tree>
      </div>
      <div class="ds-editor">
        <div class="editor-content">
          <ds-type-list
            v-show="activeStep === 0"
            @select-ds-type="selectDsType"
            :current-type="currentType"
          ></ds-type-list>
          <editor-detail
            ref="detail"
            :form="form"
            :editDs="editDs"
            :active-step="activeStep"
            v-show="activeStep !== 0 && currentDsType && currentDsType !== 'Excel'"
          ></editor-detail>
          <template v-if="activeStep !== 0 && currentDsType == 'Excel'">
            <excel-detail :editDs="editDs" ref="excel" :param="form2"></excel-detail>
          </template>
        </div>
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
  </el-drawer>
  <creat-ds-group @finish="complete" ref="creatDsFolder"></creat-ds-group>
</template>

<style lang="less">
.datasource-drawer-fullscreen {
  .ed-drawer__body {
    padding: 0;
  }

  .ed-drawer__header > :first-child {
    flex: none;
    width: auto;
  }

  .ed-drawer__header {
    justify-content: space-between;
  }

  .datasource-close {
    cursor: pointer;
  }
  .editor-step {
    position: relative;
    .ed-steps {
      width: 500px;
    }
    .ed-step.is-center .ed-step__line {
      width: 80px;
      right: 40px;
      z-index: 5;
      left: calc(100% - 40px);
    }

    .ed-step__icon.is-icon {
      width: auto;
      position: relative;
      z-index: 0;
    }

    .ed-step__head.is-finish::after {
      right: calc(100% - 66px);
    }

    .ed-step__head.is-process .ed-step__icon {
      background-color: transparent;
      .step-icon {
        .icon {
          background: #3370ff;
        }
      }
    }

    .ed-step__head.is-finish .ed-step__icon {
      background-color: transparent;
      .step-icon {
        .icon {
          border: 1px solid #3370ff;
        }
      }
    }

    .ed-step__head.is-wait .ed-step__icon {
      background-color: transparent;
      .step-icon {
        .icon {
          color: #8f959e;
          border: 1px solid #8f959e;
        }
      }
    }

    .step-icon {
      display: flex;
      padding: 0 48px;
      align-items: center;
      .icon {
        width: 28px;
        height: 28px;
        border-radius: 50%;
        line-height: 28px;
      }
      .title {
        margin-left: 8px;
        color: #1f2329;
        font-size: 14px;
        font-weight: 400;
        line-height: 22px;
      }
    }
  }
  .datasource {
    width: 100%;
    height: 100%;
    background: #fff;
    .ds-type-select {
      width: 279px;
      height: calc(100% - 64px);
      padding: 16px 7px;
      border-right: 1px solid #ccc;
      float: left;

      .icon-border {
        padding: 3px;
        border: 1px solid #dee0e3;
        border-radius: 3px;
        width: 24px;
        margin-right: 8px;
        height: 24px;
      }
      .title {
        display: flex;
        justify-content: space-between;
        font-family: PingFang SC;
        font-size: 14px;
        font-weight: 500;
        color: var(--TextPrimary, #1f2329);
        box-sizing: border-box;
        flex-wrap: wrap;
        position: sticky;
        top: 0;
        left: 24px;
        z-index: 5;
        background: white;
        padding: 0 17px;
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
      .ed-divider--horizontal {
        margin: 4px 0;
        position: absolute;
        bottom: -5px;
        width: calc(100% - 34px);
      }

      .m24 {
        margin-bottom: 8px;
      }
      .w100 {
        width: 100%;
      }
      .list-item_primary {
        position: relative;
        padding: 8px 17px;
        font-weight: 500;
        font-size: 14px;
      }
    }
    .ds-editor {
      float: left;
      width: calc(100% - 279px);
      height: calc(100% - 64px);

      .editor-content {
        padding: 8px 24px;
        height: calc(100vh - 221px);
        overflow-y: auto;
      }
    }
    .editor-footer {
      height: 64px;
      display: flex;
      align-items: center;
      justify-content: flex-end;
      width: 100%;
      padding-right: 24px;
      float: left;
      border-top: 1px solid rgba(31, 35, 41, 0.15);
    }
  }
}
</style>
