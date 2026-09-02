<script lang="ts" setup>
import dvFolder from '@/assets/svg/dv-folder.svg'
import { computed, nextTick, reactive, ref, shallowRef } from 'vue'
import { useI18n } from '@/hooks/web/useI18n'
import { createFolder, rename as renameResource, tree } from '../api'
import type { BusiTreeNode } from '@/models/tree/TreeNode'
import type { FormInstance, FormRules } from 'element-plus-secondary'
import { ElMessage } from 'element-plus-secondary'
import { Icon } from '@/components/icon-custom'

const { t } = useI18n()

const visible = ref(false)
const loading = ref(false)
const submitting = ref(false)
const showParentFolder = ref(true)
const operationMode = ref<'create' | 'rename'>('create')
const renameResourceType = ref<'folder' | 'sheet'>('folder')
const originalName = ref('')
const formRef = ref<FormInstance>()
const folderTree = shallowRef<BusiTreeNode[]>([])
const folderForm = reactive({
  id: '' as string | number,
  name: '',
  pid: '' as string | number,
  orgRoot: false
})
const emits = defineEmits(['success'])

const dialogTitle = computed(() => {
  return operationMode.value === 'rename'
    ? t('chart.rename')
    : t('spreadsheet.new_folder')
})

const nameLabel = computed(() => {
  return operationMode.value === 'rename' && renameResourceType.value === 'sheet'
    ? t('spreadsheet.spreadsheet_name')
    : t('deDataset.folder_name')
})

const namePlaceholder = computed(() => {
  return operationMode.value === 'rename' && renameResourceType.value === 'sheet'
    ? t('spreadsheet.spreadsheet_name_placeholder')
    : t('data_source.a_folder_name')
})

const folderFormRules = computed<FormRules>(() => ({
  name: [
    {
      required: true,
      message: namePlaceholder.value,
      trigger: 'change'
    },
    {
      required: true,
      message: namePlaceholder.value,
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
}))

const treeProps = {
  label: 'name',
  children: 'children',
  isLeaf: (node: BusiTreeNode) => !node.children?.length
}

type FolderTreeNode = BusiTreeNode & {
  value: string | number
  children?: FolderTreeNode[]
}

const getFolderTree = (nodes: BusiTreeNode[]): FolderTreeNode[] => {
  return nodes
    .filter(node => !node.leaf)
    .map(node => ({
      ...node,
      // TreeSelect 默认使用 value 匹配选中项，需与数据源资源树保持一致。
      value: node.id,
      children: node.children ? getFolderTree(node.children) : undefined
    }))
}

const loadFolderTree = async () => {
  loading.value = true
  try {
    const res = await tree({
      busiFlag: 'spreadsheet',
      leaf: false,
      resourceTable: 'core',
      weight: 7
    })
    const nodeData = (res as unknown as BusiTreeNode[]) || []
    const rootNode = nodeData[0]
    if (String(rootNode?.id) === '0' && rootNode.name === 'root') {
      rootNode.name = t('spreadsheet.title')
    }
    folderTree.value = getFolderTree(nodeData)
  } catch (error) {
  } finally {
    loading.value = false
  }
}

const resetForm = () => {
  visible.value = false
  operationMode.value = 'create'
  renameResourceType.value = 'folder'
  originalName.value = ''
  folderForm.id = ''
  folderForm.name = ''
  folderForm.pid = ''
  folderForm.orgRoot = false
  folderTree.value = []
  formRef.value?.clearValidate()
}

const open = (parent?: BusiTreeNode) => {
  operationMode.value = 'create'
  renameResourceType.value = 'folder'
  originalName.value = ''
  folderForm.id = ''
  folderForm.name = ''
  folderForm.orgRoot = false
  // 顶部入口必须手动选择所属文件夹，节点入口则固定使用当前文件夹。
  showParentFolder.value = !parent
  folderForm.pid = parent?.id ?? ''
  visible.value = true
  void loadFolderTree()
  nextTick(() => formRef.value?.clearValidate())
}

const rename = (resource: BusiTreeNode, resourceType: 'folder' | 'sheet') => {
  operationMode.value = 'rename'
  renameResourceType.value = resourceType
  originalName.value = resource.name || ''
  folderForm.id = resource.id
  folderForm.name = resource.name || ''
  folderForm.pid = resource.pid
  folderForm.orgRoot = resource.orgRoot || false
  showParentFolder.value = false
  visible.value = true
  nextTick(() => formRef.value?.clearValidate())
}

const handleNodeClick = (data: BusiTreeNode) => {
  folderForm.pid = data.id
}

const submit = async () => {
  if (!formRef.value || submitting.value) {
    return
  }
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) {
    return
  }

  submitting.value = true
  try {
    const name = folderForm.name.trim()
    if (operationMode.value === 'rename') {
      if (name === originalName.value) {
        resetForm()
        return
      }
      await renameResource({
        id: folderForm.id,
        name,
        orgRoot: folderForm.orgRoot
      })
      ElMessage.success(t('system.update_successful'))
    } else {
      await createFolder({
        name,
        pid: folderForm.pid,
        nodeType: 'folder'
      })
      ElMessage.success(t('common.create_success'))
    }
    resetForm()
    emits('success')
  } catch (error) {
  } finally {
    submitting.value = false
  }
}

defineExpose({
  open,
  rename
})
</script>

<template>
  <el-dialog
    v-model="visible"
    v-loading="loading || submitting"
    :title="dialogTitle"
    width="420px"
    class="create-dialog"
    :before-close="resetForm"
    @submit.prevent
  >
    <el-form
      ref="formRef"
      label-position="top"
      require-asterisk-position="right"
      :model="folderForm"
      :rules="folderFormRules"
      @keydown.stop.prevent.enter
    >
      <el-form-item :label="nameLabel" prop="name">
        <el-input
          v-model="folderForm.name"
          :placeholder="namePlaceholder"
        />
      </el-form-item>
      <el-form-item v-if="showParentFolder" :label="t('deDataset.folder')" prop="pid">
        <el-tree-select
          v-model="folderForm.pid"
          :data="folderTree"
          :props="treeProps"
          popper-class="spreadsheet-folder-tree-select"
          style="width: 100%"
          :render-after-expand="false"
          filterable
          @node-click="handleNodeClick"
        >
          <template #default="{ data }">
            <span class="folder-tree-node">
              <el-icon>
                <Icon name="dv-folder"><dvFolder class="svg-icon" /></Icon>
              </el-icon>
              <span :title="data.name">{{ data.name }}</span>
            </span>
          </template>
        </el-tree-select>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button secondary @click="resetForm">{{ t('dataset.cancel') }}</el-button>
      <el-button type="primary" @click="submit">{{ t('dataset.confirm') }}</el-button>
    </template>
  </el-dialog>
</template>

<style lang="less" scoped>
.folder-tree-node {
  display: flex;
  align-items: center;

  .ed-icon {
    margin-right: 5px;
  }
}
</style>
