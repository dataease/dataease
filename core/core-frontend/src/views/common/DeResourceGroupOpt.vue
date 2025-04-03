<script lang="ts" setup>
import dvFolder from '@/assets/svg/dv-folder.svg'
import icon_searchOutline_outlined from '@/assets/svg/icon_search-outline_outlined.svg'
import { ref, reactive, computed, watch, toRefs, nextTick } from 'vue'
import { useI18n } from '@/hooks/web/useI18n'
import { useCache } from '@/hooks/web/useCache'
import nothingTree from '@/assets/img/nothing-tree.png'
import { BusiTreeNode } from '@/models/tree/TreeNode'
import {
  copyResource,
  dvNameCheck,
  moveResource,
  queryTreeApi,
  ResourceOrFolder,
  updateBase,
  saveCanvas
} from '@/api/visualization/dataVisualization'
import { ElMessage } from 'element-plus-secondary'
import { cutTargetTree, filterFreeFolder, nameTrim } from '@/utils/utils'
const props = defineProps({
  curCanvasType: {
    type: String,
    required: true
  }
})

const { curCanvasType } = toRefs(props)
const { wsCache } = useCache('localStorage')
const { t } = useI18n()

const state = reactive({
  tData: [],
  nameList: [],
  targetInfo: null,
  attachParams: null
})

const showParentSelected = ref(false)
const loading = ref(false)
const nodeType = ref()
const pid = ref()
const id = ref()
const cmd = ref('')
const treeRef = ref()
const filterText = ref('')
const resourceFormNameLabel = ref('')
const resourceForm = reactive({
  pid: '',
  pName: null,
  name: '新建'
})
const sourceLabel = computed(() =>
  curCanvasType.value === 'dataV' ? t('work_branch.big_data_screen') : t('work_branch.dashboard')
)

const methodMap = {
  move: moveResource,
  copy: copyResource,
  newFolder: saveCanvas
}
const searchEmpty = ref(false)

const filterNode = (value: string, data: BusiTreeNode) => {
  nextTick(() => {
    searchEmpty.value = treeRef.value.isEmpty
  })
  if (!value) return true
  return data.name.includes(value)
}

watch(filterText, val => {
  treeRef.value.filter(val)
})

const nameRepeat = value => {
  if (!nameList || nameList.length === 0) {
    return false
  }
  return nameList.some(name => name === value)
}
const nameValidator = (_, value, callback) => {
  if (nameRepeat(value)) {
    callback(new Error(t('visualization.name_repeat')))
  } else {
    callback()
  }
}

const showPid = computed(() => {
  return ['newLeaf', 'copy', 'newLeafAfter'].includes(cmd.value) && showParentSelected.value
})

const showName = computed(() => {
  return !['newLeafAfter', 'move'].includes(cmd.value)
})

let nameList = []
const resourceFormRules = ref()

const resource = ref()
const resourceDialogShow = ref(false)
const dialogTitle = ref('')
let tData = []
const filterMethod = value => {
  state.tData = [...tData].filter(item => item.name.includes(value))
}
const resetForm = () => {
  dialogTitle.value = null
  resourceFormNameLabel.value = ''
  resourceForm.name = t('visualization.new')
  resourceForm.pid = ''
  resourceDialogShow.value = false
}

const dfs = (arr: BusiTreeNode[]) => {
  arr.forEach(ele => {
    ele['value'] = ele.id
    if (ele.children?.length) {
      dfs(ele.children)
    }
  })
}

const getDialogTitle = exec => {
  return {
    newFolder: t('visualization.new_folder'),
    newLeaf:
      props.curCanvasType === 'dataV'
        ? t('visualization.new_screen')
        : t('visualization.new_dashboard'),
    move: t('visualization.move_to'),
    copy: t('visualization.copy') + sourceLabel.value,
    rename: t('visualization.rename'),
    newLeafAfter: t('visualization.belong_folder')
  }[exec]
}
const placeholder = ref('')

const optInit = (type, data: BusiTreeNode, exec, parentSelect = false, attachParams?) => {
  showParentSelected.value = parentSelect
  state.targetInfo = data
  state.attachParams = attachParams
  nodeType.value = type
  const optSource = data.leaf || type === 'leaf' ? sourceLabel.value : t('visualization.folder')
  const placeholderLabel =
    data.leaf || type === 'leaf'
      ? props.curCanvasType === 'dataV'
        ? t('work_branch.big_data_screen')
        : t('work_branch.dashboard')
      : t('visualization.folder')
  placeholder.value = t('visualization.input_name_tips', [placeholderLabel])
  filterText.value = ''
  dialogTitle.value = getDialogTitle(exec) + ('rename' === exec ? optSource : '')
  resourceFormNameLabel.value = (exec === 'move' ? '' : optSource) + t('visualization.name')
  const request = { busiFlag: curCanvasType.value, leaf: false, resourceTable: 'core', weight: 7 }
  if (['newFolder'].includes(exec)) {
    resourceForm.name = ''
  } else if ('copy' === exec) {
    resourceForm.name = data.name + '_copy'
  } else {
    resourceForm.name = data.name
  }
  queryTreeApi(request).then(res => {
    filterFreeFolder(res, curCanvasType.value)
    const resultTree = res || []
    dfs(resultTree as unknown as BusiTreeNode[])
    state.tData = (resultTree as unknown as BusiTreeNode[]) || []
    if (state.tData.length && state.tData[0].name === 'root' && state.tData[0].id === '0') {
      state.tData[0].name =
        curCanvasType.value === 'dataV'
          ? t('work_branch.big_data_screen')
          : t('work_branch.dashboard')
    }
    tData = [...state.tData]
    if ('move' === exec) {
      cutTargetTree(state.tData, data.id)
    }
    if (['newLeaf', 'newFolder'].includes(exec)) {
      resourceForm.pid = data.id as string
      pid.value = data.id
    } else {
      id.value = data.id
    }
  })
  cmd.value = exec
  resourceDialogShow.value = true
  resourceFormRules.value = {
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
        message: t('commons.char_1_64'),
        trigger: 'change'
      },
      { required: true, trigger: 'blur', validator: nameValidator }
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
    resource.value.clearValidate()
  }, 50)
}

const editeInit = (param: BusiTreeNode) => {
  pid.value = param['pid']
  id.value = param.id
}

const propsTree = {
  label: 'name',
  children: 'children',
  isLeaf: node => !node.children?.length
}

const nodeClick = (data: BusiTreeNode) => {
  resourceForm.pid = data.id as string
  resourceForm.pName = data.name as string
}

const checkParent = params => {
  if (params.pid !== 0 && !params.pid) {
    ElMessage.error(t('visualization.select_target_folder'))
    return false
  }
  // 如果有搜索需要校验当前pName 是否包含关键字（解决先点击再搜索后，未点击搜索结果也可以移动的问题）
  if (filterText.value && !resourceForm.pName.includes(filterText.value)) {
    ElMessage.error(t('visualization.select_target_folder'))
    return false
  }
  // 点击后不能选择自身作为父ID
  if (params.pid === params.id) {
    ElMessage.warning(t('visualization.select_target_tips'))
    return
  }
  return true
}

const saveResource = () => {
  resource.value.validate(async result => {
    if (result) {
      const params: ResourceOrFolder = {
        nodeType: nodeType.value as 'folder' | 'leaf',
        name: resourceForm.name,
        type: curCanvasType.value,
        mobileLayout: state.targetInfo?.extraFlag,
        status: state.targetInfo?.extraFlag1
      }

      switch (cmd.value) {
        case 'move':
          params.pid = resourceForm.pid as string
          params.id = id.value
          break
        case 'copy':
          params.id = id.value
          params.pid = resourceForm.pid || pid.value || '0'
          break
        case 'rename':
          params.pid = pid.value as string
          params.id = id.value
          break
        default:
          params.pid = resourceForm.pid || pid.value || '0'
          break
      }
      nameTrim(params, t('components.length_1_64_characters'))
      if (cmd.value === 'move' && !checkParent(params)) {
        return
      }
      if (['newLeaf', 'newLeafAfter', 'newFolder', 'rename', 'move', 'copy'].includes(cmd.value)) {
        await dvNameCheck({ opt: cmd.value, ...params })
      }
      if (cmd.value === 'newLeaf') {
        resourceDialogShow.value = false
        emits('finish', { opt: 'newLeaf', ...params, ...state.attachParams })
      } else {
        loading.value = true
        const method = methodMap[cmd.value] ? methodMap[cmd.value] : updateBase
        method(params)
          .then(data => {
            loading.value = false
            resourceDialogShow.value = false
            emits('finish')
            ElMessage.success(t('visualization.save_success'))
            if (cmd.value === 'copy') {
              const openType = wsCache.get('open-backend') === '1' ? '_self' : '_blank'
              const baseUrl =
                curCanvasType.value === 'dataV'
                  ? '#/dvCanvas?opt=copy&dvId='
                  : '#/dashboard?opt=copy&resourceId='
              window.open(baseUrl + data.data, openType)
            }
          })
          .finally(() => {
            loading.value = false
          })
      }
    }
  })
}

defineExpose({
  optInit,
  editeInit
})

const emits = defineEmits(['finish'])
</script>

<template>
  <el-dialog
    class="create-dialog"
    :title="dialogTitle"
    v-model="resourceDialogShow"
    :width="cmd === 'move' ? '600px' : '420px'"
    :before-close="resetForm"
    @submit.prevent
  >
    <el-form
      v-loading="loading"
      label-position="top"
      require-asterisk-position="right"
      ref="resource"
      :model="resourceForm"
      :rules="resourceFormRules"
    >
      <el-form-item v-if="showName" :label="resourceFormNameLabel" prop="name">
        <el-input
          @keydown.stop
          @keyup.stop
          :placeholder="placeholder"
          v-model="resourceForm.name"
        />
      </el-form-item>
      <el-form-item v-if="showPid" :label="t('visualization.belong_folder')" prop="pid">
        <el-tree-select
          style="width: 100%"
          @keydown.stop
          @keyup.stop
          v-model="resourceForm.pid"
          :data="state.tData"
          :props="propsTree"
          @node-click="nodeClick"
          :filter-method="filterMethod"
          :render-after-expand="false"
          filterable
        >
          <template #default="{ data: { name } }">
            <span class="custom-tree-node">
              <el-icon>
                <Icon name="dv-folder"><dvFolder class="svg-icon" /></Icon>
              </el-icon>
              <span :title="name">{{ name }}</span>
            </span>
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
            v-model="resourceForm.pid"
            empty-text=""
            menu
            :data="state.tData"
            :props="propsTree"
            @node-click="nodeClick"
          >
            <template #default="{ data }">
              <span class="custom-tree-node">
                <el-icon style="font-size: 18px">
                  <Icon name="dv-folder"><dvFolder class="svg-icon" /></Icon>
                </el-icon>
                <span :title="data.name">{{ data.name }}</span>
              </span>
            </template>
          </el-tree>
          <div v-if="searchEmpty" class="empty-search">
            <img :src="nothingTree" />
            <span>{{ t('visualization.no_content') }}</span>
          </div>
        </div>
      </div>
    </el-form>
    <template #footer>
      <el-button secondary @click="resetForm()">{{ t('visualization.cancel') }} </el-button>
      <el-button type="primary" @click="saveResource()"
        >{{ t('visualization.confirm') }}
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
  overflow-y: auto;
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
</style>
