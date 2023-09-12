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
import { save, validate, latestUse } from '@/api/datasource'
import { Base64 } from 'js-base64'
import type { Param } from './ExcelDetail.vue'
import { dsTypes, typeList, nameMap } from './option'
import { useRouter } from 'vue-router'
import { uuid } from 'vue-uuid'
import { useEmitt } from '@/hooks/web/useEmitt'
import FinishPage from '../FinishPage.vue'
import { cloneDeep } from 'lodash-es'
import { useCache } from '@/hooks/web/useCache'
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
  id?: string
  description: string
  type: string
  configuration?: Configuration
  apiConfiguration?: ApiConfiguration[]
  syncSetting?: SyncSetting
}

const { t } = useI18n()
const creatDsFolder = ref()
const router = useRouter()
const { wsCache } = useCache()

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
const latestUseTypes = ref([])
const currentType = ref<DsType>('OLTP')
const filterText = ref('')
const currentDsType = ref('')
const emits = defineEmits(['refresh'])
const { emitter } = useEmitt()
const selectDsType = (type: string) => {
  currentDsType.value = type
  activeStep.value = 1
  activeApiStep.value = 1
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
    dsTree.value.filter(val.toLocaleLowerCase())
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

const getLatestUseTypes = () => {
  latestUse({}).then(res => {
    latestUseTypes.value = res.data
  })
}
getLatestUseTypes()

const activeApiStep = ref(0)

const next = () => {
  if (currentDsType.value === '') {
    ElMessage.error(t('datasource.select_type'))
    return
  }

  if (
    form.apiConfiguration?.length === 0 &&
    currentDsType.value === 'API' &&
    activeStep.value !== 2
  ) {
    ElMessage.error('数据表不能为空')
    return
  }

  activeApiStep.value = activeStep.value + 1

  if (currentDsType.value === 'API' && activeStep.value === 1) return
  activeStep.value = activeStep.value + 1
}

const complete = (params, successCb, finallyCb) => {
  excel.value.saveExcelDs(params, successCb, finallyCb)
  return
}

const showFinishPage = ref(false)
const dsInfo = reactive({
  name: '',
  id: ''
})

const createDataset = () => {
  router.push({
    path: '/dataset-form',
    query: {
      datasourceId: dsInfo.id
    }
  })
}
const backToDatasourceList = () => {
  visible.value = false
  emits('refresh')
  showFinishPage.value = false
}
const continueCreating = () => {
  showFinishPage.value = false
  init(null)
}

const handleShowFinishPage = ({ id, name }) => {
  if (editDs.value || wsCache.get('ds-create-success')) {
    emits('refresh')
    visible.value = false
    return
  }
  showFinishPage.value = true
  Object.assign(dsInfo, { id, name })
}
emitter.on('showFinishPage', handleShowFinishPage)

const prev = () => {
  if (currentDsType.value === 'API' && activeApiStep.value === 2) {
    activeApiStep.value = activeStep.value = 1
    return
  }

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
  if (currentDsType.value === 'API') {
    if (form.apiConfiguration.length === 0) {
      ElMessage.error('需要添加数据表')
      return
    }
    request.configuration = Base64.encode(JSON.stringify(request.apiConfiguration))
    request.syncSetting.startTime = new Date(request.syncSetting.startTime).getTime()
    request.syncSetting.endTime = new Date(request.syncSetting.endTime).getTime()
  } else {
    request.configuration = Base64.encode(JSON.stringify(request.configuration))
  }
  const validateFrom = detail.value.submitForm()
  validateFrom(val => {
    if (val) {
      validate(request).then(() => {
        ElMessage.success(t('datasource.validate_success'))
      })
    }
  })
}

const typeTitle = computed(() => {
  if (!currentDsType.value) {
    return ''
  }
  let str = ''
  databaseList.value.some(ele => {
    return ele.some(itx => {
      if (itx.type === currentDsType.value) {
        str = itx.name
        return true
      }
      return false
    })
  })
  return str
})

const saveDS = () => {
  const request = JSON.parse(JSON.stringify(form)) as unknown as Omit<
    Form,
    'configuration' | 'apiConfiguration'
  > & {
    configuration: string
    apiConfiguration: string
  }
  if (currentDsType.value === 'Excel') {
    excel.value.uploadStatus(false)
    if (!excel.value.sheetFile?.name) {
      excel.value.uploadStatus(true)
      return
    }

    const validate = excel.value.submitForm()
    validate(val => {
      if (val) {
        if (editDs.value) {
          complete(null, null, null)
        } else {
          creatDsFolder.value.createInit('datasource', { id: pid.value }, '', form2.name)
        }
      }
    })

    return
  } else if (currentDsType.value === 'API') {
    for (var i = 0; i < request.apiConfiguration.length; i++) {
      if (
        request.apiConfiguration[i].deTableName === '' ||
        request.apiConfiguration[i].deTableName === undefined ||
        request.apiConfiguration[i].deTableName === null
      ) {
        request.apiConfiguration[i].deTableName =
          'api_' +
          request.apiConfiguration[i].name +
          '_' +
          uuid.v1().replaceAll('-', '').substring(0, 10)
      }
      request.apiConfiguration[i].jsonFields = []
      for (var j = 0; j < request.apiConfiguration[i].fields.length; j++) {
        request.apiConfiguration[i].fields[j].value = []
      }
    }
    request.configuration = Base64.encode(JSON.stringify(request.apiConfiguration))
    request.syncSetting.startTime = new Date(request.syncSetting.startTime).getTime()
    request.syncSetting.endTime = new Date(request.syncSetting.endTime).getTime()
  } else {
    request.configuration = Base64.encode(JSON.stringify(request.configuration))
  }
  const validate = detail.value.submitForm()
  validate(val => {
    if (val) {
      if (editDs.value) {
        save(request).then(res => {
          if (res !== undefined) {
            handleShowFinishPage({ id: res.id, name: res.name })
            ElMessage.success('保存数据源成功')
          }
        })
      } else {
        creatDsFolder.value.createInit('datasource', { id: pid.value, request }, '', form.name)
      }
    }
  })
}

const defaultForm = {
  name: '',
  description: '',
  type: 'API',
  apiConfiguration: []
}
const form = reactive<Form>(cloneDeep(defaultForm))
const defaultForm2 = {
  type: '',
  id: '0',
  editType: 0,
  name: '',
  creator: ''
}
const form2 = reactive<Param>(cloneDeep(defaultForm2))
const visible = ref(false)
const editDs = ref(false)
const pid = ref('0')

const init = (nodeInfo: Form | Param, id?: string, res?: object) => {
  editDs.value = !!nodeInfo

  if (!!nodeInfo) {
    if (nodeInfo.type == 'Excel') {
      Object.assign(form2, cloneDeep(nodeInfo))
    } else {
      Object.assign(form, cloneDeep(nodeInfo))
    }
    pid.value = nodeInfo.pid || '0'
  } else {
    Object.assign(form2, cloneDeep(defaultForm2))
    Object.assign(form, cloneDeep(defaultForm))
    pid.value = id || '0'
  }

  activeStep.value = Number(editDs.value)
  activeApiStep.value = activeStep.value

  visible.value = true
  if (!!nodeInfo) {
    nextTick(() => {
      currentDsType.value = nodeInfo.type
      activeStep.value = 1
      activeApiStep.value = activeStep.value
      if (!!res) {
        nextTick(() => {
          excel.value.appendReplaceExcel(res)
        })
      }
    })
  }
}

const drawTitle = computed(() => {
  const { id, editType, creator } = form2
  if (creator && id && currentDsType.value == 'Excel') {
    return editType === 1 ? '追加数据' : '替换数据'
  }
  return editDs.value ? t('datasource.modify') : '创建数据源'
})

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
      <span>{{ drawTitle }}</span>
      <div v-if="!editDs" class="editor-step flex-center">
        <el-steps space="150px" :active="activeStep" align-center>
          <el-step>
            <template #icon>
              <div class="step-icon">
                <span class="icon">
                  {{ activeStep <= 0 ? '1' : '' }}
                </span>
                <span class="title">{{ t('deDataset.select_data_source') }}</span>
              </div>
            </template>
          </el-step>
          <el-step>
            <template #icon>
              <div class="step-icon">
                <span class="icon">
                  {{ activeStep <= 1 ? '2' : '' }}
                </span>
                <span class="title">配置信息</span>
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
      <div class="ds-type-select" v-if="!editDs">
        <div class="title">
          <el-input
            :placeholder="t('chart.search')"
            class="m24 w100"
            v-model="filterText"
            clearable
          >
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
            最近创建
          </p>
          <el-divider />
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
            <span class="custom-tree-node flex-align-center">
              <el-icon v-if="!!data.catalog" class="icon-border" style="width: 18px; height: 18px">
                <Icon :name="`${data.type}-ds`"></Icon>
              </el-icon>
              <span :title="node.label" class="label-tooltip">{{ node.label }}</span>
            </span>
          </template>
        </el-tree>
      </div>
      <div class="ds-editor" :class="editDs && 'edit-ds'">
        <div v-show="activeStep !== 0 && !editDs" class="ds-type-title">
          {{ typeTitle }}
        </div>
        <div class="editor-content" :class="(activeStep === 0 || editDs) && 'type-title'">
          <ds-type-list
            v-show="activeStep === 0"
            :filter-text="filterText.toLocaleLowerCase()"
            @select-ds-type="selectDsType"
            :current-type="currentType"
            :latest-use-types="latestUseTypes"
          ></ds-type-list>
          <editor-detail
            ref="detail"
            :form="form"
            :editDs="editDs"
            :active-step="activeApiStep"
            v-show="activeStep !== 0 && currentDsType && currentDsType !== 'Excel'"
          ></editor-detail>
          <template v-if="activeStep !== 0 && currentDsType == 'Excel'">
            <excel-detail :editDs="editDs" ref="excel" :param="form2"></excel-detail>
          </template>
        </div>
      </div>
      <div class="editor-footer">
        <el-button secondary @click="visible = false"> {{ t('common.cancel') }}</el-button>
        <el-button
          v-show="!(activeStep === 0 || (editDs && activeApiStep <= 1))"
          secondary
          @click="prev"
        >
          {{ t('common.prev') }}</el-button
        >
        <el-button
          v-show="activeStep === 1 && currentDsType !== 'Excel'"
          secondary
          @click="validateDS"
        >
          {{ t('datasource.validate') }}</el-button
        >
        <el-button
          v-show="
            (activeStep === 0 && currentDsType !== 'API') ||
            (activeApiStep !== 2 && currentDsType === 'API')
          "
          type="primary"
          @click="next"
        >
          {{ t('common.next') }}</el-button
        >
        <el-button
          v-show="
            (activeStep === 1 && currentDsType !== 'API') ||
            (activeApiStep === 2 && currentDsType === 'API')
          "
          type="primary"
          @click="saveDS"
        >
          {{ t('common.save') }}</el-button
        >
      </div>
      <FinishPage
        @continue-creating="continueCreating"
        @back-to-datasource-list="backToDatasourceList"
        @create-dataset="createDataset"
        :name="dsInfo.name"
        v-if="showFinishPage"
      ></FinishPage>
    </div>
  </el-drawer>
  <creat-ds-group
    @handle-show-finish-page="handleShowFinishPage"
    @finish="complete"
    ref="creatDsFolder"
  ></creat-ds-group>
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
      top: 44%;
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
        line-height: 27px;
        border-radius: 50%;
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
    position: relative;

    .custom-tree-node {
      .ed-icon {
        margin-right: 8px;
      }
    }
    .ds-type-select {
      width: 279px;
      height: calc(100% - 64px);
      padding: 16px 7px;
      border-right: 1px solid #3370ff1a;
      float: left;
      overflow-y: auto;

      .icon-border {
        font-size: 18px;
        margin-right: 8px;
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
        margin: 4px 0 4px 16px;
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

      &.edit-ds {
        width: 100%;
      }

      .ds-type-title {
        width: 100%;
        padding: 16px 24px;
        color: #1f2329;
        font-family: PingFang SC;
        font-size: 16px;
        font-style: normal;
        font-weight: 500;
        line-height: 24px;
        border-bottom: 1px solid rgba(31, 35, 41, 0.15);
      }

      .editor-content {
        padding: 16px 24px;
        height: calc(100vh - 278px);
        overflow-y: auto;

        &.type-title {
          height: calc(100vh - 221px);
        }
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
