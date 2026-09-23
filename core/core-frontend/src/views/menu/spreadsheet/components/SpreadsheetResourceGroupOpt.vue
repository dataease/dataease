<script lang="ts" setup>
import dvFolder from '@/assets/svg/dv-folder.svg'
import { ref, reactive, toRefs } from 'vue'
import { useI18n } from '@/hooks/web/useI18n'
import { tree as getTree, nameCheck } from '../api/index'
import { ElMessage } from 'element-plus-secondary'
import type { BusiTreeNode } from '@/models/tree/TreeNode'
import { Icon } from '@/components/icon-custom'

const { t } = useI18n()

const state = reactive({
  tData: [] as BusiTreeNode[],
})

const resourceDialogShow = ref(false)
const loading = ref(false)
const resourceForm = reactive({
  pid: '0' as number | string,
  name: ''
})

const emits = defineEmits<{
  (e: 'finish', destination: { name: string; pid: string | number }): void
  (e: 'cancel'): void
}>()

const resetForm = () => {
  resourceDialogShow.value = false
  resourceForm.name = ''
  resourceForm.pid = '0'
  emits('cancel')
}

const getFolderTree = (tree: BusiTreeNode[]): BusiTreeNode[] => {
  return tree
    .filter(node => !node.leaf)
    .map(node => ({
      ...node,
      children: node.children ? getFolderTree(node.children) : undefined
    }))
}

const findNodeById = (nodes: BusiTreeNode[], id: string): BusiTreeNode | null => {
  for (const node of nodes) {
    if (node.id === id) {
      return node
    }
    if (node.children?.length) {
      const found = findNodeById(node.children, id)
      if (found) return found
    }
  }
  return null
}

const findFirstValidFolder = (nodes: BusiTreeNode[]): BusiTreeNode | null => {
  for (const node of nodes) {
    if (node.weight > 0 && !node.leaf) {
      return node
    }
    if (node.children?.length) {
      const found = findFirstValidFolder(node.children)
      if (found) return found
    }
  }
  return null
}

const optInit = (name: string, pid: number | string = 0) => {
  resourceForm.name = name
  // IDs are serialized as strings in DataEase, so keep pid as string to match tree nodes
  resourceForm.pid = pid && String(pid) !== '0' ? String(pid) : '0'

  loading.value = true
  getTree({
    busiFlag: 'spreadsheet',
    leaf: false,
    resourceTable: 'core',
    weight: 7
  }).then(res => {
    const nodeData = (res as unknown as BusiTreeNode[]) || []
    if (nodeData.length && nodeData[0]['id'] === '0' && nodeData[0]['name'] === 'root') {
      nodeData[0]['name'] = t('spreadsheet.title')
    }
    state.tData = getFolderTree(nodeData)
    if (!findNodeById(state.tData, resourceForm.pid as string)) {
      const firstValid = findFirstValidFolder(state.tData)
      if (firstValid) {
        resourceForm.pid = firstValid.id as string
      }
    }
  }).finally(() => {
    loading.value = false
  })

  resourceDialogShow.value = true
}

const handleConfirm = async () => {
  if (!resourceForm.name?.trim()) {
    ElMessage.warning(t('spreadsheet.name_required'))
    return
  }

  loading.value = true
  try {
    const isNameRepeat = await nameCheck({
      name: resourceForm.name,
      pid: resourceForm.pid,
      nodeType: 'sheet'
    })

    if (isNameRepeat) {
      ElMessage.warning(t('visualization.name_repeat'))
      return
    }

    resourceDialogShow.value = false
    emits('finish', {
      name: resourceForm.name,
      pid: resourceForm.pid
    })
  } catch (e) {
  } finally {
    loading.value = false
  }
}

const nodeClick = (data: BusiTreeNode) => {
  if (!data.leaf) {
    resourceForm.pid = data.id as string
  }
}

const propsTree = {
  label: 'name',
  children: 'children'
}

defineExpose({
  optInit
})

</script>

<template>
  <el-dialog
    v-model="resourceDialogShow"
    :title="t('common.save') + t('spreadsheet.title')"
    width="420px"
    :before-close="resetForm"
    @submit.prevent
    append-to-body
  >
    <el-form
      v-loading="loading"
      label-position="top"
      :model="resourceForm"
    >
      <el-form-item :label="t('visualization.name')" prop="name">
        <el-input
          v-model="resourceForm.name"
          :placeholder="t('spreadsheet.name_placeholder')"
          maxlength="50"
        />
      </el-form-item>

      <el-form-item :label="t('visualization.belong_folder')" prop="pid">
        <el-tree-select
          style="width: 100%"
          v-model="resourceForm.pid"
          :data="state.tData"
          :props="propsTree"
          @node-click="nodeClick"
          node-key="id"
          filterable
          check-strictly
          :placeholder="t('spreadsheet.root_folder')"
        >
          <template #default="{ data }">
            <span class="custom-tree-node">
              <el-icon v-if="!data.leaf">
                <Icon name="dv-folder"><dvFolder class="svg-icon" /></Icon>
              </el-icon>
              <span>{{ data.name }}</span>
            </span>
          </template>
        </el-tree-select>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="resetForm()">{{ t('common.cancel') }}</el-button>
      <el-button type="primary" @click="handleConfirm()">{{ t('common.sure') }}</el-button>
    </template>
  </el-dialog>
</template>

<style lang="less" scoped>
.custom-tree-node {
  display: flex;
  align-items: center;
  span {
    margin-left: 8px;
  }
}
</style>
