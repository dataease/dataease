<script lang="ts" setup>
import { ref, reactive, computed } from 'vue'
import { useI18n } from '@/hooks/web/useI18n'
import { saveDatasetTree, getDatasetTree } from '@/api/dataset'
import type { DatesetOrFolder } from '@/api/dataset'
export interface Tree {
  name: string
  value?: string | number
  id: string | number
  nodeType: string
  createBy?: string
  level: number
  pid: string | number
  union?: Array<{}>
  allfields?: Array<{}>
  children?: Tree[]
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
let union = []
let allfields = []
const datasetForm = reactive({
  pid: '',
  name: ''
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
  return cmd.value !== 'rename' && !!pid.value
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
const datasetFormRules = ref()

const dataset = ref()
const loading = ref(false)
const createDataset = ref(false)
const dialogTitle = ref('新建文件夹')
const filterMethod = value => {
  state.tData = [...state.tData].filter(item => item.name.includes(value))
}
const resetForm = () => {
  dataset.value.clearValidate()
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

const createInit = (type, data: Tree, exec) => {
  nodeType.value = type
  if (type === 'dataset') {
    union = data.union
    allfields = data.allfields
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
  datasetForm.pid = data.id as string
}

const saveDataset = () => {
  dataset.value.validate(result => {
    if (result) {
      const params: DatesetOrFolder = {
        nodeType: nodeType.value as 'folder' | 'dataset',
        name: datasetForm.name
      }

      switch (cmd.value) {
        case 'move':
          params.pid = datasetForm.pid as string
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
      saveDatasetTree(params)
        .then(() => {
          dataset.value.resetFields()
          createDataset.value = false
          emits('finish')
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
    width="600px"
    :before-close="resetForm"
  >
    <el-form
      label-position="top"
      require-asterisk-position="right"
      ref="dataset"
      :model="datasetForm"
      :rules="datasetFormRules"
    >
      <el-form-item v-if="showName" :label="t('dataset.name')" prop="name">
        <el-input v-model="datasetForm.name" />
      </el-form-item>
      <el-form-item v-if="showPid" :label="t('deDataset.folder')" prop="pid">
        <el-tree-select
          v-model="datasetForm.pid"
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
    </el-form>
    <template #footer>
      <el-button secondary @click="resetForm">{{ t('dataset.cancel') }} </el-button>
      <el-button type="primary" @click="saveDataset">{{ t('dataset.confirm') }} </el-button>
    </template>
  </el-dialog>
</template>

<style lang="less" scoped></style>
