<script lang="ts" setup>
import { ref, reactive, computed, watch, nextTick } from 'vue'
import { ElMessage } from 'element-plus-secondary'
import { useI18n } from '@/hooks/web/useI18n'
import {
  saveDatasetTree,
  getDatasetTree,
  moveDatasetTree,
  createDatasetTree,
  renameDatasetTree
} from '@/api/dataset'
import type { DatasetOrFolder } from '@/api/dataset'
import nothingTree from '@/assets/img/nothing-tree.png'
import { BusiTreeRequest } from '@/models/tree/TreeNode'
export interface Tree {
  name: string
  value?: string | number
  id: string | number
  nodeType: string
  createBy?: string
  level: number
  leaf?: boolean
  pid: string | number
  union?: Array<{}>
  createTime: number
  allfields?: Array<{}>
  children?: Tree[]
}
const { t } = useI18n()

const state = reactive({
  tData: [],
  nameList: []
})

const placeholder = ref('')
const nodeType = ref()
const pid = ref()
const id = ref()
const cmd = ref('')
const treeRef = ref()
const filterText = ref('')
let union = []
let allfields = []
const datasetForm = reactive({
  pid: '',
  name: ''
})
const searchEmpty = ref(false)

const filterNode = (value: string, data: Tree) => {
  nextTick(() => {
    searchEmpty.value = treeRef.value.isEmpty
  })
  if (!value) return true
  return data.name.includes(value)
}

watch(filterText, val => {
  showAll.value = !val
  treeRef.value.filter(val)
  nextTick(() => {
    document.querySelectorAll('.node-text').forEach(ele => {
      const content = ele.getAttribute('title')
      ele.innerHTML = content.replace(val, `<span class="highLight">${val}</span>`)
    })
  })
})

const showPid = computed(() => {
  if (nodeType.value === 'folder' && !!pid.value) {
    return false
  }
  return !['rename', 'move'].includes(cmd.value) && !!pid.value
})

const labelName = computed(() => {
  return nodeType.value === 'folder' ? t('deDataset.folder_name') : t('dataset.name')
})

const dialogTitle = computed(() => {
  let title = ''

  switch (nodeType.value) {
    case 'folder':
      title = t('deDataset.new_folder')
      break
    case 'dataset':
      title = t('common.save') + t('auth.dataset')
      break
    default:
      break
  }
  switch (cmd.value) {
    case 'move':
      title = t('chart.move_to')
      break
    case 'rename':
      title = t('chart.rename')
      break
    default:
      break
  }
  return title
})

const showName = computed(() => {
  return cmd.value !== 'move'
})

const rules = {
  name: [
    {
      required: true,
      message: t('commons.input_content'),
      trigger: 'change'
    },
    {
      max: 50,
      message: t('commons.char_can_not_more_50'),
      trigger: 'change'
    }
  ],
  pid: [
    {
      required: true,
      message: t('common.please_select'),
      trigger: 'blur'
    }
  ]
}
const datasetFormRules = ref()
const activeAll = ref(false)
const showAll = ref(true)
const dataset = ref()
const loading = ref(false)
const createDataset = ref(false)
let tData = []
const filterMethod = value => {
  state.tData = [...tData].filter(item => item.name.includes(value))
}
const resetForm = () => {
  dataset.value.clearValidate()
  createDataset.value = false
}

const dfs = (arr: Tree[]) => {
  arr?.forEach(ele => {
    ele.value = ele.id
    if (ele.children?.length) {
      dfs(ele.children)
    }
  })
}

const createInit = (type, data: Tree, exec, name: string) => {
  pid.value = ''
  id.value = ''
  cmd.value = ''
  datasetForm.pid = ''
  datasetForm.name = ''
  nodeType.value = type
  placeholder.value = type === 'folder' ? '请输入文件夹名称' : '请输入数据集名称'
  if (type === 'dataset') {
    union = data.union
    allfields = data.allfields
  }
  if (data.id) {
    const request = { leaf: false, weight: 3 } as BusiTreeRequest
    getDatasetTree(request).then(res => {
      dfs(res as unknown as Tree[])
      state.tData = (res as unknown as Tree[]) || []
      if (state.tData.length && state.tData[0].name === 'root' && state.tData[0].id === '0') {
        state.tData[0].name = '数据集'
      }
      tData = [...state.tData]
      if (exec) {
        pid.value = data.pid
        id.value = data.id
        datasetForm.pid = data.pid as string
        datasetForm.name = data.name
      } else {
        datasetForm.pid = data.id as string
        pid.value = data.id
      }
    })

    cmd.value = exec
  }
  name && (datasetForm.name = name)
  createDataset.value = true
  datasetFormRules.value = rules
}

const editeInit = (param: Tree) => {
  pid.value = param.pid
  id.value = param.id
}

const props = {
  label: 'name',
  children: 'children',
  isLeaf: node => !node.children?.length
}

const nodeClick = (data: Tree) => {
  activeAll.value = false
  datasetForm.pid = data.id as string
}

const saveDataset = () => {
  dataset.value.validate(result => {
    if (result) {
      const params: DatasetOrFolder = {
        nodeType: nodeType.value as 'folder' | 'dataset',
        name: datasetForm.name
      }

      switch (cmd.value) {
        case 'move':
          params.pid = activeAll.value ? '0' : (datasetForm.pid as string)
          params.id = id.value
          break
        case 'rename':
          params.pid = pid.value as string
          params.id = id.value
          break
        default:
          params.pid = datasetForm.pid || pid.value || '0'
          break
      }
      if (nodeType.value === 'dataset') {
        params.union = union
        params.allFields = allfields
      }
      loading.value = true
      const req =
        cmd.value === 'move' ? moveDatasetTree : params.id ? renameDatasetTree : createDatasetTree
      req(params)
        .then(res => {
          dataset.value.resetFields()
          createDataset.value = false
          emits('finish', res)
          switch (cmd.value) {
            case 'move':
              ElMessage.success('移动成功')
              break
            case 'rename':
              ElMessage.success('重命名成功')
              break
            default:
              ElMessage.success(t('common.save_success'))
              break
          }
        })
        .finally(() => {
          loading.value = false
        })
    }
  })
}

defineExpose({
  createInit,
  editeInit
})

const emits = defineEmits(['finish'])
</script>

<template>
  <el-dialog
    v-loading="loading"
    :title="dialogTitle"
    v-model="createDataset"
    class="create-dialog"
    width="420px"
    :before-close="resetForm"
  >
    <el-form
      label-position="top"
      require-asterisk-position="right"
      ref="dataset"
      @keydown.stop.prevent.enter
      :model="datasetForm"
      :rules="datasetFormRules"
    >
      <el-form-item v-if="showName" :label="labelName" prop="name">
        <el-input :placeholder="placeholder" v-model="datasetForm.name" />
      </el-form-item>
      <el-form-item v-if="showPid" :label="t('deDataset.folder')" prop="pid">
        <el-tree-select
          v-model="datasetForm.pid"
          :data="state.tData"
          popper-class="dataset-tree-select"
          :render-after-expand="false"
          style="width: 100%"
          :props="props"
          @node-click="nodeClick"
          :filter-method="filterMethod"
          filterable
        >
          <template #default="{ data: { name } }">
            <el-icon>
              <Icon name="dv-folder"></Icon>
            </el-icon>
            <span :title="name">{{ name }}</span>
          </template>
        </el-tree-select>
      </el-form-item>
      <div v-if="cmd === 'move'">
        <el-input style="margin-bottom: 12px" v-model="filterText" clearable>
          <template #prefix>
            <el-icon>
              <Icon name="icon_search-outline_outlined"></Icon>
            </el-icon>
          </template>
        </el-input>
        <div class="tree-content">
          <!-- <div
            :class="activeAll && 'active'"
            @click="activeAll = !activeAll"
            v-if="showAll"
            class="list-item_primary"
          >
            <el-icon>
              <Icon name="dv-folder"></Icon>
            </el-icon>
            <span class="label"> 全部文件夹 </span>
          </div> -->
          <el-tree
            ref="treeRef"
            :filter-node-method="filterNode"
            filterable
            v-model="datasetForm.pid"
            menu
            empty-text=""
            :data="state.tData"
            :props="props"
            @node-click="nodeClick"
          >
            <template #default="{ data }">
              <span class="custom-tree-node">
                <el-icon>
                  <Icon name="dv-folder"></Icon>
                </el-icon>
                <span class="node-text" :title="data.name">{{ data.name }}</span>
              </span>
            </template>
          </el-tree>
          <div v-if="searchEmpty" class="empty-search">
            <img :src="nothingTree" />
            <span>没有找到相关内容</span>
          </div>
        </div>
      </div>
    </el-form>
    <template #footer>
      <el-button secondary @click="resetForm">{{ t('dataset.cancel') }} </el-button>
      <el-button type="primary" @click="saveDataset">{{ t('dataset.confirm') }} </el-button>
    </template>
  </el-dialog>
</template>

<style lang="less" scoped>
.tree-content {
  width: 552px;
  height: 380px;
  border: 1px solid #dee0e3;
  border-radius: 4px;
  padding: 8px;
  overflow-y: auto;
  .custom-tree-node {
    display: flex;
    .node-text {
      margin-left: 8.75px;
      width: 120px;
      white-space: nowrap;
      text-overflow: ellipsis;
      overflow: hidden;
      :deep(.highLight) {
        color: var(--el-color-primary, #3370ff);
      }
    }
  }

  .empty-search {
    width: 100%;
    margin-top: 57px;
    display: flex;
    flex-direction: column;
    align-items: center;
    img {
      width: 100px;
      height: 100px;
      margin-bottom: 8px;
    }
    span {
      font-family: PingFang SC;
      font-size: 14px;
      font-weight: 400;
      line-height: 22px;
      color: #646a73;
    }
  }
}
</style>
<style lang="less">
.dataset-tree-select {
  .ed-select-dropdown__item {
    display: flex;
    align-items: center;
    .ed-icon {
      margin-right: 5px;
    }
  }
}
</style>
