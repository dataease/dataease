<script lang="ts" setup>
import { computed, onBeforeMount, ref, shallowRef } from 'vue'
import { useResizeObserver } from '@vueuse/core'
import type { IWorkbookData } from '@univerjs/core'
import { useI18n } from '@/hooks/web/useI18n'
import { useEmbedded } from '@/store/modules/embedded'
import { useLocaleStoreWithOut } from '@/store/modules/locale'
import { onInitReady } from '@/utils/canvasUtils'
import EmptyBackground from '@/components/empty-background/src/EmptyBackground.vue'
import UniverSheet from '@/views/menu/spreadsheet/components/UniverSheet.vue'
import PluginRenderIndicator from '@/views/menu/spreadsheet/components/PluginRenderIndicator.vue'
import { findById, SpreadsheetPublishStatus } from '@/views/menu/spreadsheet/api'
import { parseSheetData } from '@/views/menu/spreadsheet/utils/univerConfig'

const { t } = useI18n()
const resourceId = useEmbedded().resourceId
const localeStore = useLocaleStoreWithOut()
const currentLang = computed(() => localeStore.getCurrentLocale.lang)
const previewRootRef = ref<HTMLElement>()
const spreadsheetRef = ref<InstanceType<typeof UniverSheet>>()
const workbookData = shallowRef<Partial<IWorkbookData>>()
const loading = ref(true)
const emptyDescription = ref(t('spreadsheet.no_data'))

// DIV 容器尺寸可能独立变化，直接复用电子表格已有的尺寸刷新入口。
useResizeObserver(previewRootRef, () => spreadsheetRef.value?.resize())

onBeforeMount(async () => {
  try {
    if (!resourceId) {
      return
    }
    const spreadsheet = await findById(resourceId)
    if (!spreadsheet) {
      return
    }
    // 嵌入预览与管理页保持一致，只展示已发布版本。
    const status = spreadsheet.status ?? SpreadsheetPublishStatus.Unpublished
    if (status === SpreadsheetPublishStatus.Unpublished) {
      emptyDescription.value = t('spreadsheet.unpublished')
      return
    }
    workbookData.value = parseSheetData(spreadsheet.sheetData) || undefined
  } catch (error) {
    console.error(error)
    emptyDescription.value = t('spreadsheet.load_error')
  } finally {
    loading.value = false
  }
})
</script>

<template>
  <div ref="previewRootRef" class="spreadsheet-embedded-preview" v-loading="loading">
    <template v-if="workbookData">
      <UniverSheet
        ref="spreadsheetRef"
        :model-value="workbookData"
        :locale="currentLang"
        mode="preview"
        @ready="onInitReady({ resourceId })"
      />
      <PluginRenderIndicator />
    </template>
    <EmptyBackground v-else-if="!loading" :description="emptyDescription" img-type="none" />
  </div>
</template>

<style lang="less" scoped>
.spreadsheet-embedded-preview {
  position: relative;
  width: 100%;
  height: 100%;
  overflow: hidden;
}
</style>
