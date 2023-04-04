<script lang="ts" setup>
import { ref, reactive, PropType } from 'vue'
import { ElMessage } from 'element-plus-secondary'
import { useI18n } from '@/hooks/web/useI18n'
import type { FormInstance, FormRules } from 'element-plus-secondary'

export interface User {
  deptId: number
  pid: number
  subCount: number
  name: string
  deptSort: number
  createBy?: string
  updateBy?: string
  createTime: number
  updateTime: number
  hasChildren: boolean
  leaf: boolean
  top: boolean
}

interface UserForm {
  pid: number | string
  name: string
  deptId?: number | string
  top?: boolean
}

const props = defineProps({
  treeData: {
    type: Array as PropType<User[]>,
    default: () => []
  }
})

const { t } = useI18n()

const tree = ref()
const roleForm = ref<FormInstance>()

const formType = ref('add')
const searchNodeLoading = ref(false)
const dialogTableVisible = ref(false)
const treeDataMap = ref([])
const deptId = ref('')
const originName = ref('')
const loadFirst = ref(false)

const form = reactive<UserForm>({
  pid: '',
  name: ''
})

const selectDepts = ref([])
const treeDataFormat = ref([])
const treeDefaultProps = {
  children: 'children',
  label: 'name',
  isLeaf: 'leaf'
}
const roleValidator = (_, value, callback) => {
  if (!value?.length) {
    callback(new Error(t('organization.input_name')))
  } else if (nameRepeat(value)) {
    callback(new Error(t('organization.organization_name_exist')))
  } else {
    callback()
  }
}
const rule = reactive<FormRules>({
  name: [
    { required: true, trigger: 'blur', validator: roleValidator },
    {
      min: 1,
      max: 10,
      message: t('commons.input_limit', [1, 10]),
      trigger: 'blur'
    }
  ]
})

const filterNode = (value, data) => {
  if (!value) return true
  return data.name.indexOf(value) !== -1
}
const initTree = () => {
  treeDataFormat.value = props.treeData.map(dept => {
    return normalizer(dept)
  })
  if (deptId.value) {
    treeDataFormat.value = treeDataFormat.value.filter(ele => ele.deptId !== deptId.value)
  }
  treeDataMap.value = treeDataFormat.value.map(ele => ele.name)
}
const loadNode = (node, resolve) => {
  if (!treeDataFormat.value.length || !loadFirst.value) {
    loadFirst.value = true
    initTree()
    return
  }
  executeAxios('/plugin/dept/childNodes/' + node.data.deptId, 'post', {}, res => {
    if (deptId.value) {
      res.data = res.data.filter(ele => ele.deptId !== deptId.value)
    }
    resolve(
      res.data.map(dept => {
        return normalizer(dept)
      })
    )
  })
}
const normalizer = node => {
  return {
    ...node,
    leaf: !node.hasChildren
  }
}
const filterMethod = val => {
  tree.value.filter(val)
}
const handleNodeClick = (data, node) => {
  selectDepts.value = [data]
  form.pid = data.deptId
  treeDataMap.value = []
  if (!node) return
  if (!node.isLeaf) {
    if (!node.childNodes.length) {
      searchNodeLoading.value = true
      executeAxios('/plugin/dept/childNodes/' + data.deptId, 'post', {}, res => {
        treeDataMap.value = res.data.map(ele => ele.name)
        if (form.name) {
          roleForm.value.validateField('name')
        }
        searchNodeLoading.value = false
      })
    } else {
      treeDataMap.value = node.childNodes.map(ele => ele.data.name)
      if (form.name) {
        roleForm.value.validateField('name')
      }
    }
  }
}
const executeAxios = (url, type, data, callBack) => {
  console.log(url, type, data, callBack)
}
const createOrg = ({ deptId, name }) => {
  form.name = ''
  form.pid = ''
  if (deptId) {
    selectDepts.value = [{ name, deptId }]
    form.pid = deptId === 0 ? '' : deptId
  }
  formType.value = 'add'
  dialogTableVisible.value = true
  initTree()
}
const editOrg = ({ deptId, pid, name, pidName }) => {
  formType.value = 'modify'
  form.name = name
  originName.value = name
  deptId.value = deptId
  selectDepts.value = [{ name: pidName, deptId: pid }]
  form.pid = pid === 0 ? '' : pid
  dialogTableVisible.value = true
  initTree()
}
const reset = () => {
  form.name = ''
  deptId.value = ''
  form.pid = ''
  selectDepts.value = []
  treeDataMap.value = []
  roleForm.value.resetFields()
  dialogTableVisible.value = false
}
const save = () => {
  roleForm.value.validate(valid => {
    if (valid) {
      let url = ''
      const param = { ...form }
      if (formType.value === 'add') {
        url = '/plugin/dept/create'
      } else {
        url = '/plugin/dept/update'
        param.deptId = deptId.value
      }
      if (param.pid === '') {
        param.pid = 0
        param.top = true
      }
      executeAxios(url, 'post', param, () => {
        ElMessage({
          message: t('commons.save_success'),
          type: 'success'
        })
        reset()
        emit('saved')
      })
    } else {
      return false
    }
  })
}
const nameRepeat = value => {
  if (!treeDataMap.value?.length) {
    return false
  }
  // 编辑场景 不能 因为名称重复而报错
  if (formType.value === 'modify' && originName.value === value) {
    return false
  }
  return treeDataMap.value.some(role => role === value)
}

const emit = defineEmits(['saved'])

defineExpose({
  createOrg,
  editOrg
})
</script>
<template>
  <el-dialog
    :title="
      formType == 'add' ? t('organization.add_organization') : t('organization.edite_organization')
    "
    v-model="dialogTableVisible"
    append-to-body
    class="org-editer-form"
    width="600px"
    v-loading="searchNodeLoading"
    :before-close="reset"
  >
    <el-form
      require-asterisk-position="right"
      label-position="top"
      ref="roleForm"
      :model="form"
      :rules="rule"
    >
      <el-row>
        <el-col :span="24">
          <el-form-item :label="t('organization.organization_name')" prop="name">
            <el-input
              v-model="form.name"
              :placeholder="t('organization.input_organization_name')"
            />
          </el-form-item>
        </el-col>
      </el-row>
      <el-row>
        <el-col :span="24">
          <el-form-item :label="t('organization.relate_top_organization')" prop="description">
            <el-popover
              placement="bottom"
              popper-class="dept-popper dept"
              width="552"
              trigger="click"
            >
              <template #reference>
                <el-select
                  v-model="form.pid"
                  filterable
                  style="width: 100%"
                  :filter-method="filterMethod"
                  clearable
                  popper-class="tree-select"
                  :placeholder="t('fu.search_bar.please_input')"
                >
                  <el-option
                    v-for="item in selectDepts"
                    :key="item.name"
                    :label="item.name"
                    :value="item.deptId"
                  />
                </el-select>
              </template>
              <el-tree
                :load="loadNode"
                :lazy="true"
                ref="tree"
                :expand-on-click-node="false"
                :data="treeDataFormat"
                :highlight-current="!!form.pid"
                check-on-click-node
                :filter-node-method="filterNode"
                :props="treeDefaultProps"
                @node-click="handleNodeClick"
              ></el-tree>
            </el-popover>
          </el-form-item>
        </el-col>
      </el-row>
    </el-form>
    <template #footer>
      <el-button secondary @click="reset">{{ t('commons.cancel') }}</el-button>
      <el-button type="primary" @click="save">{{ t('commons.save') }}</el-button>
    </template>
  </el-dialog>
</template>

<style lang="less">
.dept-popper {
  padding: 0;
  .popper__arrow {
    display: none;
  }
}
.tree-select {
  display: none;
  .el-select-dropdown__empty,
  .popper__arrow {
    display: none;
  }
}
.dept-popper.dept {
  max-height: 300px;
  overflow: auto;
}
</style>
