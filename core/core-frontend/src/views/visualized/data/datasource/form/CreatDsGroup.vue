<script lang="ts" setup>
import { ref, reactive, computed, watch, nextTick } from 'vue'
import { ElMessage } from 'element-plus-secondary'
import { useI18n } from '@/hooks/web/useI18n'
import { getDatasetTree } from '@/api/dataset'
import { save } from '@/api/datasource'
import type { DatasetOrFolder } from '@/api/dataset'
import nothingTree from '@/assets/img/nothing-tree.png'

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
  request: any
}
const { t } = useI18n()

const state = reactive({
  tData: [],
  nameList: []
})

let request = null

const nodeType = ref()
const pid = ref()
const id = ref()
const cmd = ref('')
const treeRef = ref()
const filterText = ref('')
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
    case 'datasource':
      title = t('deDataset.create') + t('auth.datasource')
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
      message: t('common.input_content'),
      trigger: 'change'
    },
    {
      max: 50,
      message: t('common.char_can_not_more_50'),
      trigger: 'change'
    }
  ],
  pid: [
    {
      required: true,
      message: t('fu.search_bar.please_select'),
      trigger: 'blur'
    }
  ]
}
const datasetFormRules = ref()
const activeAll = ref(false)
const showAll = ref(true)
const datasource = ref()
const loading = ref(false)
const createDataset = ref(false)
const filterMethod = value => {
  state.tData = [...state.tData].filter(item => item.name.includes(value))
}
const resetForm = () => {
  datasource.value.clearValidate()
  createDataset.value = false
}

const dfs = (arr: Tree[]) => {
  arr.forEach(ele => {
    ele.value = ele.id
    if (ele.children?.length) {
      dfs(ele.children)
    }
  })
}

const createInit = (type, data: Tree, exec, name: string) => {
  nodeType.value = type
  if (type === 'dataset') {
    request = data.request
  }
  if (data.id) {
    getDatasetTree({
      nodeType: 'folder'
    }).then(res => {
      dfs(res as unknown as Tree[])
      state.tData = (res as unknown as Tree[]) || []
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

const finishCb = () => {
  datasource.value.resetFields()
  createDataset.value = false
  loading.value = false
  switch (cmd.value) {
    case 'move':
      ElMessage.success('移动成功')
      break
    case 'rename':
      ElMessage.success('重命名成功')
      break
    default:
      ElMessage.success('新建数据源成功')
      break
  }
}

const saveDataset = () => {
  datasource.value.validate(result => {
    if (result) {
      const params: Omit<DatasetOrFolder, 'nodeType'> & { nodeType: 'folder' | 'datasource' } = {
        nodeType: nodeType.value as 'folder' | 'datasource',
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
      loading.value = true

      if (!request) {
        emits('finish', params, cmd.value)
        return
      }
      save({ ...params, ...request })
        .then(() => {
          datasource.value.resetFields()
          createDataset.value = false
          switch (cmd.value) {
            case 'move':
              ElMessage.success('移动成功')
              break
            case 'rename':
              ElMessage.success('重命名成功')
              break
            default:
              ElMessage.success('新建数据集成功')
              break
          }
          ElMessage.success(t('common.save_success'))
        })
        .finally(() => {
          loading.value = false
        })
    }
  })
}

defineExpose({
  createInit,
  editeInit,
  finishCb
})

const emits = defineEmits(['finish'])
</script>

<template>
  <el-dialog :title="dialogTitle" v-model="createDataset" width="600px" :before-close="resetForm">
    <el-form
      v-loading="loading"
      label-position="top"
      require-asterisk-position="right"
      ref="datasource"
      :model="datasetForm"
      :rules="datasetFormRules"
    >
      <el-form-item v-if="showName" :label="labelName" prop="name">
        <el-input v-model="datasetForm.name" />
      </el-form-item>
      <el-form-item v-if="showPid" :label="t('deDataset.folder')" prop="pid">
        <el-tree-select
          v-model="datasetForm.pid"
          :data="state.tData"
          popper-class="dataset-tree-select"
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
          <div
            :class="activeAll && 'active'"
            @click="activeAll = !activeAll"
            v-if="showAll"
            class="list-item_primary"
          >
            <el-icon>
              <Icon name="dv-folder"></Icon>
            </el-icon>
            <span class="label"> 全部文件夹 </span>
          </div>
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
      <el-button v-loading="loading" secondary @click="resetForm"
        >{{ t('dataset.cancel') }}
      </el-button>
      <el-button v-loading="loading" type="primary" @click="saveDataset"
        >{{ t('dataset.confirm') }}
      </el-button>
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
