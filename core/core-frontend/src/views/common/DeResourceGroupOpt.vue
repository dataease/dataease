<script lang="ts" setup>
import { ref, reactive, computed, watch, toRefs, nextTick } from 'vue'
import { useI18n } from '@/hooks/web/useI18n'
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
import { cutTargetTree } from '@/utils/utils'
const props = defineProps({
  curCanvasType: {
    type: String,
    required: true
  }
})

const { curCanvasType } = toRefs(props)

const { t } = useI18n()

const state = reactive({
  tData: [],
  nameList: []
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
const sourceLabel = computed(() => (curCanvasType.value === 'dataV' ? '数据大屏' : '仪表板'))

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
    callback(new Error('名称重复'))
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
  resourceForm.name = '新建'
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
    newFolder: '新建文件夹',
    newLeaf: props.curCanvasType === 'dataV' ? '新建数据大屏' : '新建仪表板',
    move: '移动到',
    copy: '复制' + sourceLabel.value,
    rename: '重命名',
    newLeafAfter: '所属文件夹'
  }[exec]
}
const placeholder = ref('')

const optInit = (type, data: BusiTreeNode, exec, parentSelect = false) => {
  showParentSelected.value = parentSelect
  nodeType.value = type
  const optSource = data.leaf || type === 'leaf' ? sourceLabel.value : '文件夹'
  placeholder.value =
    data.leaf || type === 'leaf'
      ? props.curCanvasType === 'dataV'
        ? '请输入数据大屏名称'
        : '请输入仪表板名称'
      : '请输入文件夹名称'
  filterText.value = ''
  dialogTitle.value = getDialogTitle(exec) + ('rename' === exec ? optSource : '')
  resourceFormNameLabel.value = (exec === 'move' ? '' : optSource) + '名称'
  const request = { busiFlag: curCanvasType.value, leaf: false, weight: 7 }
  if (['newFolder'].includes(exec)) {
    resourceForm.name = ''
  } else if ('copy' === exec) {
    resourceForm.name = data.name + '_copy'
  } else {
    resourceForm.name = data.name
  }
  queryTreeApi(request).then(res => {
    const resultTree = res || []
    dfs(resultTree as unknown as BusiTreeNode[])
    state.tData = (resultTree as unknown as BusiTreeNode[]) || []
    if (state.tData.length && state.tData[0].name === 'root' && state.tData[0].id === '0') {
      state.tData[0].name = curCanvasType.value === 'dataV' ? '数据大屏' : '仪表板'
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
        min: 2,
        max: 64,
        message: t('commons.char_2_64'),
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
    ElMessage.error('请选择目标文件夹')
    return false
  }
  // 如果有搜索需要校验当前pName 是否包含关键字（解决先点击再搜索后，未点击搜索结果也可以移动的问题）
  if (filterText.value && !resourceForm.pName.includes(filterText.value)) {
    ElMessage.error('请选择目标文件夹')
    return false
  }
  // 点击后不能选择自身作为父ID
  if (params.pid === params.id) {
    ElMessage.warning('不能选择自身，请选择其他文件夹')
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
        type: curCanvasType.value
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
      if (cmd.value === 'move' && !checkParent(params)) {
        return
      }
      if (['newLeaf', 'newLeafAfter', 'newFolder', 'rename', 'move', 'copy'].includes(cmd.value)) {
        await dvNameCheck({ opt: cmd.value, ...params })
      }
      if (cmd.value === 'newLeaf') {
        resourceDialogShow.value = false
        emits('finish', { opt: 'newLeaf', ...params })
      } else {
        loading.value = true
        const method = methodMap[cmd.value] ? methodMap[cmd.value] : updateBase
        method(params)
          .then(data => {
            loading.value = false
            resourceDialogShow.value = false
            emits('finish')
            ElMessage.success('保存成功')
            if (cmd.value === 'copy') {
              const baseUrl =
                curCanvasType.value === 'dataV'
                  ? '#/dvCanvas?opt=copy&dvId='
                  : '#/dashboard?opt=copy&resourceId='
              window.open(baseUrl + data.data, '_blank')
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
      <el-form-item v-if="showPid" :label="'所属文件夹'" prop="pid">
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
                <Icon name="dv-folder"></Icon>
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
              <Icon name="icon_search-outline_outlined"></Icon>
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
                  <Icon name="dv-folder"></Icon>
                </el-icon>
                <span :title="data.name">{{ data.name }}</span>
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
      <el-button secondary @click="resetForm()">取消 </el-button>
      <el-button type="primary" @click="saveResource()">确认 </el-button>
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
      font-family: '阿里巴巴普惠体 3.0 55 Regular L3';
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
