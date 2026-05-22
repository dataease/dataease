<script lang="ts" setup>
import { RefreshLeft } from '@element-plus/icons-vue'
import { nextTick, ref, h, computed } from 'vue'
import { ElButton, ElMessage } from 'element-plus-secondary'
import { useI18n } from '@/hooks/web/useI18n'
import { useEmitt } from '@/hooks/web/useEmitt'
import { useLinkStoreWithOut } from '@/store/modules/link'
import { useAppStoreWithOut } from '@/store/modules/app'
import ExportExcel from './ExportExcel.vue'

const { t } = useI18n()
const drawerLoading = ref(false)
const drawer = ref(false)
const pendingInitParams = ref()
const linkStore = useLinkStoreWithOut()
const appStore = useAppStoreWithOut()
const isDataEaseBi = computed(() => appStore.getIsDataEaseBi)

const handleClose = () => {
  exportExcelRef.value?.handleClose()
  drawer.value = false
}

const exportExcelRef = ref()
const runInit = () => {
  if (!exportExcelRef.value) {
    return
  }
  exportExcelRef.value.init(pendingInitParams.value)
  pendingInitParams.value = undefined
}
const init = params => {
  pendingInitParams.value = params
  drawer.value = true
  nextTick(() => {
    runInit()
  })
}

const callbackExportError = () => {
  init({ activeName: 'FAILED' })
}

const callbackExportSuc = () => {
  init({ activeName: 'SUCCESS' })
}

const openMessageLoading = (text, type = 'success', cb) => {
  const customClass = `de-message-${type || 'success'} de-message-export`
  ElMessage({
    message: h('p', null, [
      h(
        'span',
        {
          title: t(text),
          class: 'ellipsis m50-export'
        },
        t(text)
      ),
      h(
        ElButton,
        {
          text: true,
          size: 'small',
          class: 'btn-text',
          onClick: () => {
            cb()
          }
        },
        t('data_export.export_center')
      )
    ]),
    icon: type === 'loading' ? h(RefreshLeft) : '',
    type,
    showClose: true,
    customClass
  })
}

const taskExportTopicCall = task => {
  if (!linkStore.getLinkToken && !isDataEaseBi.value && !appStore.getIsIframe) {
    if (JSON.parse(task).exportStatus === 'SUCCESS') {
      openMessageLoading(
        JSON.parse(task).exportFromName + ` ${t('data_set.successful_go_to')}`,
        'success',
        callbackExportSuc
      )
      return
    }
    if (JSON.parse(task).exportStatus === 'FAILED') {
      openMessageLoading(
        JSON.parse(task).exportFromName + ` ${t('data_set.failed_go_to')}`,
        'error',
        callbackExportError
      )
    }
  }
}

useEmitt({ name: 'task-export-topic-call', callback: taskExportTopicCall })

defineExpose({
  init
})
</script>

<template>
  <el-drawer
    v-loading="drawerLoading"
    modal-class="de-export-excel"
    :title="$t('data_export.export_center')"
    v-model="drawer"
    direction="rtl"
    size="1000px"
    append-to-body
    :before-close="handleClose"
    @opened="runInit"
  >
    <ExportExcel ref="exportExcelRef"></ExportExcel>
  </el-drawer>
</template>

<style lang="less">
.de-export-excel {
  .ed-drawer__body {
    padding-bottom: 24px;

    .ed-tabs {
      margin-top: -25px;
    }
  }
  .ed-drawer__header {
    border-bottom: none;
  }
}
</style>
