<script lang="ts" setup>
import dvFolder from '@/assets/svg/dv-folder.svg'
import icon_searchOutline_outlined from '@/assets/svg/icon_search-outline_outlined.svg'
import { ref, reactive, computed, watch, nextTick, unref } from 'vue'
import treeSort from '@/utils/treeSortUtils'
import { useCache } from '@/hooks/web/useCache'
import { ElMessage } from 'element-plus-secondary'
import { cloneDeep } from 'lodash-es'
import { useI18n } from '@/hooks/web/useI18n'
import nothingTree from '@/assets/img/nothing-tree.png'
import { BusiTreeRequest } from '@/models/tree/TreeNode'
import { filterFreeFolder, getHighlightSegments } from '@/utils/utils'
import {createFolder, listDataFillingForms, move, reName, save} from '../data-filling'
import type {DataFillingOrFolder, Tree} from '../data-filling'
const { t } = useI18n()
const { wsCache } = useCache()
const state = reactive<{
  tData: Tree[]
  nameList: string[]
}>({
  tData: [],
  nameList: []
})

const placeholder = ref('')
const nodeType = ref()
const pid = ref()
const id = ref()
const orgRoot = ref(false)
const cmd = ref('')
const treeRef = ref()
const filterText = ref('')
let union = []
let allfields = []
let isCross = false
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
  treeRef.value?.filter(val)
})

const showPid = computed(() => {
  if (nodeType.value === 'folder' && !!pid.value) {
    return false
  }
  return cmd.value !== 'rename' && cmd.value !== 'move'
})

const labelName = computed(() => {
  return nodeType.value === 'folder' ? t('deDataset.folder_name') : t('data_fill.data_fill_name')
})

const dialogTitle = computed(() => {
  let title = ''

  switch (nodeType.value) {
    case 'folder':
      title = t('deDataset.new_folder')
      break
    case 'data-filling':
      title = t('deDataset.create') + '表单'
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

const datasetFormRules = ref()
const activeAll = ref(false)
const showAll = ref(true)
const dataFilling = ref()
const loading = ref(false)
const createDataset = ref(false)
const filterMethod = (value, data) => data.name.includes(value)
const resetForm = () => {
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
const formatRootMiss = (id: string | number, treeData: Tree[]) => {
  if (!treeData?.length) {
    return ''
  }
  if (id === '0' && treeData[0].id !== '0') {
    return treeData[0].id
  }
  return id
}
const findNodeById = (nodes: Tree[], id: string): Tree | null => {
  for (const node of nodes) {
    if (String(node.id) === String(id)) {
      return node
    }
    if (node.children?.length) {
      const found = findNodeById(node.children, id)
      if (found) return found
    }
  }
  return null
}
const originResourceTree = ref([])
const sortList = ['time_asc', 'time_desc', 'name_asc', 'name_desc']
const createInit = (type, data: Tree, exec, name: string) => {
  pid.value = ''
  id.value = ''
  cmd.value = ''
  datasetForm.pid = ''
  datasetForm.name = ''
  filterText.value = ''
  nodeType.value = type
  placeholder.value =
      type === 'folder' ? t('data_set.a_folder_name') : t('data_set.the_dataset_name')
  if (type === 'dataset') {
    union = data.union
    allfields = data.allfields
    isCross = data.isCross
  }
  const request = { leaf: false, weight: 7 } as BusiTreeRequest
  listDataFillingForms(request).then(res => {
    filterFreeFolder(res, 'data-filling')
    dfs(res as unknown as Tree[])
    state.tData = (res as unknown as Tree[]) || []
    let curSortType = sortList[Number(wsCache.get('TreeSort-backend')) ?? 1]
    curSortType = wsCache.get('TreeSort-dataFillingForm') ?? curSortType
    originResourceTree.value = cloneDeep(unref(state.tData))
    state.tData = treeSort(originResourceTree.value, curSortType)
    if (state.tData.length && state.tData[0].name === 'root' && state.tData[0].id === '0') {
      state.tData[0].name = t('data_fill.data_fill')
    }
    if (data.id) {
      data.id = formatRootMiss(data.id, state.tData)
      if (exec) {
        pid.value = data.pid
        id.value = data.id
        datasetForm.pid = data.pid as string
        datasetForm.name = data.name
        orgRoot.value = data.orgRoot
      } else {
        datasetForm.pid = data.id as string
        pid.value = data.id
        if (!findNodeById(state.tData, data.id as string)) {
          datasetForm.pid = ''
          pid.value = ''
        }
      }
    }
  })

  cmd.value = exec
  name && (datasetForm.name = name)
  createDataset.value = true
  datasetFormRules.value = {
    name: [
      {
        required: true,
        message: placeholder.value,
        trigger: 'change'
      },
      {
        required: true,
        message: placeholder.value,
        trigger: 'blur'
      },
      {
        min: 1,
        max: 64,
        message: t('datasource.input_limit_1_64', [1, 64]),
        trigger: 'blur'
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
  setTimeout(() => {
    dataFilling.value.clearValidate()
  }, 50)
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
const checkPid = pid => {
  if (pid !== 0 && !pid) {
    ElMessage.error(t('data_set.the_destination_folder'))
    return false
  }
  return true
}
const saveDataset = () => {
  dataFilling.value.validate(result => {
    if (result) {
      const params: DataFillingOrFolder = {
        nodeType: nodeType.value as 'folder' | 'data-filling',
        name: datasetForm.name,
        orgRoot: orgRoot.value
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
      if (cmd.value === 'move' && !checkPid(params.pid)) {
        return
      }
      loading.value = true
      let method, message
      switch (cmd) {
        case "move":
          method = move;
          message = t("data_fill.move_success");

          break;
        case "rename":
          method = reName;
          message = t("data_fill.rename_success");
          break;
        default:
          method = createFolder;
          message = t("data_fill.create_success");
          break;
      }
      method(params)
          .then(res => {
            dataFilling.value.resetFields()
            createDataset.value = false
            emits('finish', params, cmd.value)
            ElMessage.success(message);
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

const emits = defineEmits(['finish', 'onDfSave'])
</script>

<template>
  <el-dialog
      :title="dialogTitle"
      v-model="createDataset"
      class="create-dialog"
      :width="cmd === 'move' ? '600px' : '420px'"
      :before-close="resetForm"
      :close-on-click-modal="false"
      :close-on-press-escape="false"
  >
    <el-form
        label-position="top"
        require-asterisk-position="right"
        ref="dataFilling"
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
            :filter-node-method="filterMethod"
            filterable
        >
          <template #default="{ data: { name } }">
            <el-icon>
              <Icon name="dv-folder"><dvFolder class="svg-icon" /></Icon>
            </el-icon>
            <span :title="name">{{ name }}</span>
          </template>
        </el-tree-select>
      </el-form-item>
      <div v-if="cmd === 'move'">
        <el-input style="margin-bottom: 12px" v-model="filterText" clearable>
          <template #prefix>
            <el-icon>
              <Icon name="icon_search-outline_outlined"
              ><icon_searchOutline_outlined class="svg-icon"
              /></Icon>
            </el-icon>
          </template>
        </el-input>
        <div class="tree-content">
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
                <el-icon style="font-size: 18px">
                  <Icon name="dv-folder"><dvFolder class="svg-icon" /></Icon>
                </el-icon>
                <span class="node-text" :title="data.name">
                  <template
                      v-for="(segment, index) in getHighlightSegments(data.name, filterText)"
                      :key="`${data.id}-${index}`"
                  >
                    <span v-if="segment.highlight" class="highLight">{{ segment.text }}</span>
                    <template v-else>{{ segment.text }}</template>
                  </template>
                </span>
              </span>
            </template>
          </el-tree>
          <div v-if="searchEmpty" class="empty-search">
            <img :src="nothingTree" />
            <span>{{ t('data_set.relevant_content_found') }}</span>
          </div>
        </div>
      </div>
    </el-form>
    <template #footer>
      <el-button secondary @click="resetForm">{{ t('dataset.cancel') }} </el-button>
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
  border-radius: 6px;
  padding: 8px;
  overflow-y: auto;
  .custom-tree-node {
    display: flex;
    align-items: center;
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
      font-family: var(--de-custom_font, 'PingFang');
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
