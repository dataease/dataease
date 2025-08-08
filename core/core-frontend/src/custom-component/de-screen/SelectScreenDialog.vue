<script setup lang="ts">
import { computed, reactive, ref } from 'vue'
import { queryTreeApi } from '@/api/visualization/dataVisualization'
import { filterEmptyFolderTree } from '@/utils/canvasUtils'
const dialogVisible = ref(false)
import dvDashboardSpine from '@/assets/svg/dv-dashboard-spine.svg'
import dvFolder from '@/assets/svg/dv-folder.svg'
import { useI18n } from '@/hooks/web/useI18n'
const closeHandler = () => {
  dialogVisible.value = false
}
const { t } = useI18n()
const emits = defineEmits(['selectConfirm'])

const state = reactive({
  panelList: [],
  dvSelectProps: {
    label: 'name',
    children: 'children',
    value: 'id',
    isLeaf: 'leaf',
    disabled: 'disabled'
  },
  curScreenId: null,
  dvType: 'dashboard'
})
const canvasTypeName = computed(() =>
  state.dvType === 'dataV' ? t('work_branch.big_data_screen') : t('work_branch.dashboard')
)

const init = param => {
  const { dvType, screenId } = param
  state.dvType = dvType
  dialogVisible.value = true
  state.curScreenId = screenId
  loadRTree(dvType)
}

const loadRTree = dvType => {
  const request = { busiFlag: dvType, resourceTable: 'core' }
  queryTreeApi(request).then(rsp => {
    if (rsp && rsp[0]?.id === '0') {
      state.panelList = rsp[0].children
    } else {
      state.panelList = rsp
    }
    state.panelList = filterEmptyFolderTree(state.panelList)
  })
}

const dvNodeClick = data => {
  if (data.leaf && data.id !== state.curScreenId) {
    state.curScreenId = data.id
  }
}

const close = () => {
  dialogVisible.value = false
}

const confirm = () => {
  emits('selectConfirm', state.curScreenId)
  close()
}

defineExpose({
  init
})
</script>

<template>
  <el-dialog
    v-model="dialogVisible"
    :show-close="true"
    :close-on-click-modal="false"
    :title="t('visualization.select_resource', [canvasTypeName])"
    append-to-body
    @close="closeHandler"
    width="400"
  >
    <el-form-item style="position: relative" prop="rid">
      <el-tree-select
        v-model="state.curScreenId"
        :data="state.panelList"
        :props="state.dvSelectProps"
        :render-after-expand="false"
        filterable
        @node-click="dvNodeClick"
        class="dv-selector"
      >
        <template #default="{ node, data }">
          <div class="label-content-details">
            <el-icon size="18px" style="display: inline-block" v-if="data.leaf">
              <Icon name="dv-dashboard-spine"><dvDashboardSpine class="svg-icon" /></Icon>
            </el-icon>
            <el-icon size="18px" style="display: inline-block" v-else>
              <Icon name="dv-folder"><dvFolder class="svg-icon" /></Icon>
            </el-icon>
            <span style="margin-left: 8px; font-size: 14px" :title="node.label">{{
              node.label
            }}</span>
          </div>
        </template>
      </el-tree-select>
    </el-form-item>
    <template #footer>
      <span class="m-dialog-footer">
        <el-button secondary @click="close">{{ t('commons.close') }}</el-button>
        <el-button type="primary" @click="confirm">{{ t('commons.confirm') }}</el-button>
      </span>
    </template>
  </el-dialog>
</template>

<style scoped lang="less">
.m-dialog-footer {
  display: flex;
  flex-direction: row;
  align-items: center;
  justify-content: center;
}
</style>
