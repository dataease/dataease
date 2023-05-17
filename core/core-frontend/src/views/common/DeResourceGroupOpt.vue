<script lang="ts" setup>
import { ref, reactive, computed, watch } from 'vue'
import { useI18n } from '@/hooks/web/useI18n'
import { DvOrFolder, findTree, savaOrUpdateBase } from '@/api/dataVisualization'
export interface ResourceTree {
  id: string | number
  pid: string | number
  name: string
  label: string
  value?: string | number
  nodeType: string
  type?: string
  mobileLayout?: string
  remark?: string
  source?: string
  level?: number
  createTime?: number
  createBy?: string
  updateTime?: number
  updateBy?: string
  children?: ResourceTree[]
}
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
const dvForm = reactive({
  pid: '',
  name: ''
})

const filterNode = (value: string, data: ResourceTree) => {
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
    callback(new Error(t('deDataset.already_exists')))
  } else {
    callback()
  }
}

const showPid = computed(() => {
  return !['rename', 'move'].includes(cmd.value) && !!pid.value
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
const dvFormRules = ref()

const dv = ref()
const loading = ref(false)
const resourceDialogShow = ref(false)
const dialogTitle = ref('新建文件夹')
const filterMethod = value => {
  state.tData = [...state.tData].filter(item => item.name.includes(value))
}
const resetForm = () => {
  dv.value.clearValidate()
  resourceDialogShow.value = false
}

const dfs = (arr: ResourceTree[]) => {
  arr.forEach(ele => {
    ele.value = ele.id
    if (ele.children?.length) {
      dfs(ele.children)
    }
  })
}

const optInit = (type, data: ResourceTree, exec) => {
  nodeType.value = type
  if (data.id) {
    findTree({
      nodeType: 'folder'
    }).then(res => {
      const resultTree = res.data
      dfs(resultTree as unknown as ResourceTree[])
      state.tData = (resultTree as unknown as ResourceTree[]) || []
      if (exec) {
        pid.value = data.pid
        id.value = data.id
        dvForm.pid = data.pid as string
        dvForm.name = data.name
      } else {
        dvForm.pid = data.id as string
        pid.value = data.id
      }
    })
    cmd.value = exec
  }
  resourceDialogShow.value = true
  dvFormRules.value = rules
}

const editeInit = (param: ResourceTree) => {
  pid.value = param.pid
  id.value = param.id
}

const props = {
  label: 'name',
  children: 'children',
  isLeaf: node => !node.children?.length
}

const nodeClick = (data: ResourceTree) => {
  dvForm.pid = data.id as string
}

const saveDv = () => {
  dv.value.validate(result => {
    if (result) {
      const params: DvOrFolder = {
        nodeType: nodeType.value as 'folder' | 'dv',
        name: dvForm.name,
        type: 'dataV'
      }

      switch (cmd.value) {
        case 'move':
          params.pid = dvForm.pid as string
          params.id = id.value
          break
        case 'rename':
          params.pid = pid.value as string
          params.id = id.value
          break
        default:
          params.pid = dvForm.pid || pid.value || '0'
          break
      }
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
      ref="dv"
      :model="dvForm"
      :rules="dvFormRules"
    >
      <el-form-item v-if="showName" :label="'名称'" prop="name">
        <el-input v-model="dvForm.name" />
      </el-form-item>
      <el-form-item v-if="showPid" :label="'目录'" prop="pid">
        <el-tree-select
          v-model="dvForm.pid"
          :data="state.tData"
          :props="props"
          @node-click="nodeClick"
          :filter-method="filterMethod"
          filterable
        >
          <template #default="{ data: { name } }">
            <el-icon>
              <Icon name="scene"></Icon>
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
            v-model="dvForm.pid"
            menu
            :data="state.tData"
            :props="props"
            @node-click="nodeClick"
          >
            <template #default="{ data }">
              <span class="custom-tree-node">
                <el-icon>
                  <Icon name="scene"></Icon>
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
      <el-button type="primary" @click="saveDv()">确认 </el-button>
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
