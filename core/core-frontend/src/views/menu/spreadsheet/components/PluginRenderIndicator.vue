<script lang="ts" setup>
import { computed, ref } from 'vue'
import { useI18n } from '@/hooks/web/useI18n'
import { useEmitt } from '@/hooks/web/useEmitt'
import { SPREADSHEET_EVENTS } from '../utils/events'

const { t } = useI18n()

interface PlaceholderTooltipPayload {
  status?: string
  reason?: string
  left?: number
  top?: number
}

const tooltip = ref<PlaceholderTooltipPayload>()
const visible = computed(() => !!tooltip.value)

const text = computed(() => {
  const payload = tooltip.value
  if (payload?.reason) {
    return payload.reason
  }
  return payload?.status === 'empty'
    ? t('spreadsheet.data_empty')
    : t('spreadsheet.render_failed')
})

const style = computed(() => ({
  left: `${tooltip.value?.left ?? 0}px`,
  top: `${tooltip.value?.top ?? 0}px`
}))

useEmitt({
  name: SPREADSHEET_EVENTS.PLACEHOLDER_TOOLTIP_SHOW,
  callback: (payload: PlaceholderTooltipPayload) => {
    tooltip.value = payload
  }
})

useEmitt({
  name: SPREADSHEET_EVENTS.PLACEHOLDER_TOOLTIP_HIDE,
  callback: () => {
    tooltip.value = undefined
  }
})
</script>

<template>
  <div class="plugin-placeholder-tooltip-layer">
    <div v-if="visible" class="placeholder-tooltip" :style="style">
      {{ text }}
    </div>
  </div>
</template>

<style lang="less" scoped>
.plugin-placeholder-tooltip-layer {
  position: fixed;
  inset: 0;
  overflow: hidden;
  pointer-events: none;
  z-index: 150;
}

.placeholder-tooltip {
  position: absolute;
  max-width: 320px;
  padding: 6px 10px;
  border-radius: 4px;
  background: #1f2329;
  color: #fff;
  font-size: 12px;
  line-height: 18px;
  white-space: normal;
  word-break: break-word;
  pointer-events: none;
}
</style>
