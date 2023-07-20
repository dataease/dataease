<script lang="ts" setup>
import { ref, reactive, computed, watch, toRefs } from 'vue'
import { useI18n } from '@/hooks/web/useI18n'
import { BusiTreeNode, BusiTreeRequest } from '@/models/tree/TreeNode'
import {
  dvNameCheck,
  queryTreeApi,
  ResourceOrFolder,
  savaOrUpdateBase
} from '@/api/visualization/dataVisualization'

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

const nodeType = ref()
const pid = ref()
const id = ref()
const cmd = ref('')
const treeRef = ref()
const filterText = ref('')
const resourceForm = reactive({
  pid: '',
  name: ''
})

const nameMap = {
  newFolder: '新建文件夹',
  newLeaf: '新建',
  move: '移动到',
  rename: '编辑'
}

const filterNode = (value: string, data: BusiTreeNode) => {
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
  return ['newLeaf'].includes(cmd.value) && !pid.value
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
    },
    { required: true, trigger: 'blur', validator: nameValidator }
  ],
  pid: [
    {
      required: true,
      message: t('fu.search_bar.please_select'),
      trigger: 'blur'
    }
  ]
}
let nameList = []
const resourceFormRules = ref()

const resource = ref()
const loading = ref(false)
const resourceDialogShow = ref(false)
const dialogTitle = ref('新建文件夹')
const filterMethod = value => {
  state.tData = [...state.tData].filter(item => item.name.includes(value))
}
const resetForm = () => {
  resource.value.clearValidate()
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

const optInit = (type, data: BusiTreeNode, exec) => {
  nodeType.value = type
  dialogTitle.value = nameMap[exec]
  const request = { busiFlag: curCanvasType.value, leaf: false, weight: 3 }
  queryTreeApi(request).then(res => {
    const resultTree = res
    dfs(resultTree as unknown as BusiTreeNode[])
    state.tData = (resultTree as unknown as BusiTreeNode[]) || []
    if (['newLeaf', 'newFolder'].includes(exec)) {
      resourceForm.pid = data.id as string
      pid.value = data.id
      resourceForm.name = nameMap[exec]
    } else {
      pid.value = data['pid']
      id.value = data.id
      resourceForm.pid = data['pid'] as string
      resourceForm.name = data.name
    }
  })
  cmd.value = exec
  resourceDialogShow.value = true
  resourceFormRules.value = rules
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
}

const saveResource = () => {
  resource.value.validate(result => {
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
        case 'rename':
          params.pid = pid.value as string
          params.id = id.value
          break
        default:
          params.pid = resourceForm.pid || pid.value || '0'
          break
      }
      if (cmd.value === 'newLeaf') {
        dvNameCheck({ opt: 'newLeaf', ...params }).then(() => {
          resourceDialogShow.value = false
          emits('finish', { opt: 'newLeaf', ...params })
        })
      } else {
        loading.value = true
        savaOrUpdateBase(params)
          .then(() => {
            resourceDialogShow.value = false
            emits('finish')
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
    v-loading="loading"
    :title="dialogTitle"
    v-model="resourceDialogShow"
    width="600px"
    :before-close="resetForm"
  >
    <el-form
      label-position="top"
      require-asterisk-position="right"
      ref="resource"
      :model="resourceForm"
      :rules="resourceFormRules"
    >
      <el-form-item v-if="showName" :label="'名称'" prop="name">
        <el-input v-model="resourceForm.name" />
      </el-form-item>
      <el-form-item v-if="showPid" :label="'目录'" prop="pid">
        <el-tree-select
          style="width: 100%"
          v-model="resourceForm.pid"
          :data="state.tData"
          :props="propsTree"
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
          <el-tree
            ref="treeRef"
            :filter-node-method="filterNode"
            filterable
            v-model="resourceForm.pid"
            menu
            :data="state.tData"
            :props="propsTree"
            @node-click="nodeClick"
          >
            <template #default="{ data }">
              <span class="custom-tree-node">
                <el-icon>
                  <Icon name="dv-folder"></Icon>
                </el-icon>
                <span :title="data.name">{{ data.name }}</span>
              </span>
            </template>
          </el-tree>
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
}
</style>
