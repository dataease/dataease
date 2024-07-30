<template>
  <el-drawer
    :title="'保存应用'"
    v-model="state.appApplyDrawer"
    custom-class="de-app-drawer"
    :show-close="false"
    size="500px"
    direction="rtl"
    z-index="1000"
  >
    <div class="app-export">
      <el-form
        ref="appSaveForm"
        :model="state.form"
        :rules="state.rule"
        class="de-form-item"
        size="middle"
        label-width="180px"
        label-position="top"
      >
        <div class="de-row-rules" style="margin: 0 0 16px">
          <span>基本信息</span>
        </div>
        <el-form-item :label="dvPreName + '名称'" prop="name">
          <el-input v-model="state.form.name" autocomplete="off" :placeholder="'请输入名称'" />
        </el-form-item>
        <el-form-item :label="dvPreName + '所在位置'" prop="pid">
          <el-tree-select
            style="width: 100%"
            @keydown.stop
            @keyup.stop
            v-model="state.form.pid"
            :data="state.dvTree"
            :props="state.propsTree"
            @node-click="dvTreeSelect"
            :filter-method="dvTreeFilterMethod"
            :render-after-expand="false"
            filterable
          >
            <template #default="{ data: { name } }">
              <span class="custom-tree-node">
                <el-icon>
                  <Icon name="dv-folder"></Icon>
                </el-icon>
                <span :title="name">{{ name }}</span>
              </span>
            </template>
          </el-tree-select>
        </el-form-item>
        <el-form-item :label="'数据集分组名称'" prop="datasetFolderName">
          <el-input
            v-model="state.form.datasetFolderName"
            autocomplete="off"
            :placeholder="'请输入名称'"
          />
        </el-form-item>
        <el-form-item label="数据集分组位置" prop="datasetFolderPid">
          <el-tree-select
            style="width: 100%"
            @keydown.stop
            @keyup.stop
            v-model="state.form.datasetFolderPid"
            :data="state.dsTree"
            :props="state.propsTree"
            @node-click="dsTreeSelect"
            :filter-method="dsTreeFilterMethod"
            :render-after-expand="false"
            filterable
          >
            <template #default="{ data: { name } }">
              <span class="custom-tree-node">
                <el-icon>
                  <Icon name="dv-folder"></Icon>
                </el-icon>
                <span :title="name">{{ name }}</span>
              </span>
            </template>
          </el-tree-select>
        </el-form-item>
        <div class="de-row-rules" style="margin: 0 0 16px">
          <span>数据源信息</span>
        </div>
        <el-row class="datasource-link">
          <el-row class="head">
            <el-col :span="11">应用数据源</el-col><el-col :span="2"></el-col
            ><el-col :span="11">系统数据源</el-col>
          </el-row>
          <el-row
            :key="index"
            class="content"
            v-for="(appDatasource, index) in state.appData.datasourceInfo"
          >
            <el-col :span="11">
              <el-select style="width: 100%" v-model="appDatasource.name" disabled>
                <el-option
                  :key="appDatasource.name"
                  :label="appDatasource.name"
                  :value="appDatasource.name"
                >
                </el-option>
              </el-select> </el-col
            ><el-col :span="2" class="icon-center">
              <Icon style="width: 20px; height: 20px" name="dv-link-target" /></el-col
            ><el-col :span="11">
              <dataset-select
                ref="datasetSelector"
                v-model="appDatasource.systemDatasourceId"
                style="flex: 1"
                :state-obj="state"
                themes="light"
                source-type="datasource"
                @add-ds-window="addDsWindow"
                view-id="0"
              />
            </el-col>
          </el-row>
        </el-row>
      </el-form>
    </div>
    <template #footer>
      <div class="apply" style="width: 100%">
        <el-button type="primary" @click="saveApp">保存</el-button>
      </div>
    </template>
  </el-drawer>
</template>

<script lang="ts" setup>
import {
  ElButton,
  ElDrawer,
  ElForm,
  ElFormItem,
  ElInput,
  ElMessage,
  ElTreeSelect
} from 'element-plus-secondary'
import { computed, PropType, reactive, ref, toRefs } from 'vue'
import { useI18n } from '@/hooks/web/useI18n'
import { queryTreeApi } from '@/api/visualization/dataVisualization'
import { BusiTreeNode, BusiTreeRequest } from '@/models/tree/TreeNode'
import { getDatasetTree } from '@/api/dataset'
import DatasetSelect from '@/views/chart/components/editor/dataset-select/DatasetSelect.vue'
import { dvMainStoreWithOut } from '@/store/modules/data-visualization/dvMain'
import { storeToRefs } from 'pinia'
import { deepCopy } from '@/utils/utils'
import { snapshotStoreWithOut } from '@/store/modules/data-visualization/snapshot'
const { t } = useI18n()
const emits = defineEmits(['closeDraw', 'saveAppCanvas'])
const appSaveForm = ref(null)
const dvMainStore = dvMainStoreWithOut()
const { dvInfo, appData } = storeToRefs(dvMainStore)
const snapshotStore = snapshotStoreWithOut()
const props = defineProps({
  componentData: {
    type: Object,
    required: true
  },
  canvasViewInfo: {
    type: Object,
    required: true
  },
  curCanvasType: {
    type: String,
    required: true
  },
  themes: {
    type: String as PropType<EditorTheme>,
    default: 'dark'
  }
})

const { componentData, canvasViewInfo, curCanvasType, themes } = toRefs(props)

const dvPreName = computed(() => (curCanvasType.value === 'dashboard' ? '仪表板' : '数据大屏'))
const addDsWindow = () => {
  // do addDsWindow
  const url = '#/data/datasource?opt=create'
  window.open(url, '_blank')
}

const state = reactive({
  appApplyDrawer: false,
  dvTree: [],
  dsTree: [],
  propsTree: {
    label: 'name',
    children: 'children',
    isLeaf: node => !node.children?.length
  },
  appData: {
    datasourceInfo: []
  },
  form: {
    pid: '',
    name: '新建',
    datasetFolderPid: null,
    datasetFolderName: null
  },
  rule: {
    name: [
      {
        required: true,
        min: 2,
        max: 25,
        message: t('datasource.input_limit_2_25', [2, 25]),
        trigger: 'blur'
      }
    ],
    pid: [
      {
        required: true,
        message: '请选择所属文件夹',
        trigger: 'blur'
      }
    ],
    datasetFolderName: [
      {
        required: true,
        min: 2,
        max: 25,
        message: t('datasource.input_limit_2_25', [2, 25]),
        trigger: 'blur'
      }
    ],
    datasetFolderPid: [
      {
        required: true,
        message: '请选择数据集分组所属文件夹',
        trigger: 'blur'
      }
    ]
  }
})

const initData = () => {
  const request = { busiFlag: curCanvasType.value, leaf: false, weight: 7 }
  queryTreeApi(request).then(res => {
    const resultTree = res || []
    dfs(resultTree as unknown as BusiTreeNode[])
    state.dvTree = (resultTree as unknown as BusiTreeNode[]) || []
    if (state.dvTree.length && state.dvTree[0].name === 'root' && state.dvTree[0].id === '0') {
      state.dvTree[0].name = curCanvasType.value === 'dataV' ? '数据大屏' : '仪表板'
    }
  })

  const requestDs = { leaf: false, weight: 7 } as BusiTreeRequest
  getDatasetTree(requestDs).then(res => {
    dfs(res as unknown as BusiTreeNode[])
    state.dsTree = (res as unknown as BusiTreeNode[]) || []
    if (state.dsTree.length && state.dsTree[0].name === 'root' && state.dsTree[0].id === '0') {
      state.dsTree[0].name = '数据集'
    }
  })
}

const dfs = (arr: BusiTreeNode[]) => {
  arr.forEach(ele => {
    ele['value'] = ele.id
    if (ele.children?.length) {
      dfs(ele.children)
    }
  })
}

const init = params => {
  state.appApplyDrawer = true
  state.form = params.base
  state.appData.datasourceInfo = deepCopy(appData.value?.datasourceInfo)
  initData()
}

const dvTreeFilterMethod = value => {
  state.dvTree = [...state.dvTree].filter(item => item.name.includes(value))
}

const dsTreeFilterMethod = value => {
  state.dsTree = [...state.dsTree].filter(item => item.name.includes(value))
}

const dvTreeSelect = element => {
  state.form.pid = element.id
}

const dsTreeSelect = element => {
  state.form.datasetFolderPid = element.id
}

const close = () => {
  emits('closeDraw')
  snapshotStore.recordSnapshotCache('renderChart')
  state.appApplyDrawer = false
}

const saveApp = () => {
  let datasourceMatchReady = true
  state.appData.datasourceInfo.forEach(datasource => {
    if (!datasource.systemDatasourceId) {
      datasourceMatchReady = false
    }
  })
  if (!datasourceMatchReady) {
    ElMessage.error('存在未配置的数据源')
    return
  }
  appSaveForm.value?.validate(valid => {
    if (valid) {
      // 还原datasource
      appData.value['datasourceInfo'] = state.appData.datasourceInfo
      dvInfo.value['pid'] = state.form.pid
      dvInfo.value['name'] = state.form.name
      dvInfo.value['datasetFolderPid'] = state.form.datasetFolderPid
      dvInfo.value['datasetFolderName'] = state.form.datasetFolderName
      dvInfo.value['dataState'] = 'ready'
      snapshotStore.recordSnapshotCache('renderChart')
      emits('saveAppCanvas')
    } else {
      return false
    }
  })
}

defineExpose({
  init,
  close
})
</script>
<style lang="less" scoped>
.app-export {
  width: 100%;
  height: calc(100% - 56px);
}

.app-export-bottom {
  width: 100%;
  height: 56px;
  text-align: right;
}

:deep(.ed-drawer__body) {
  padding-bottom: 0 !important;
}

.de-row-rules {
  display: flex;
  align-items: center;
  position: relative;
  font-size: 14px;
  font-weight: 500;
  line-height: 22px;
  padding-left: 10px;
  margin: 24px 0 16px 0;
  color: var(--ed-text-color-regular);

  &::before {
    content: '';
    position: absolute;
    left: 0;
    top: 50%;
    transform: translateY(-50%);
    height: 14px;
    width: 2px;
    background: #3370ff;
  }
}

.custom-tree-node {
  display: flex;
  align-items: center;
  span {
    margin-left: 8.75px;
    width: 120px;
    white-space: nowrap;
    text-overflow: ellipsis;
    overflow: hidden;
  }
}
.datasource-link {
  color: var(--ed-text-color-regular);
  font-size: 12px;
  font-weight: 500;
  width: 100%;
  .head {
    width: 100%;
  }
  .content {
    width: 100%;
    margin-top: 8px;
  }
}

.icon-center {
  padding: 0 8px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}
</style>

<style lang="less">
.de-app-drawer {
  z-index: 1000;
}
</style>
