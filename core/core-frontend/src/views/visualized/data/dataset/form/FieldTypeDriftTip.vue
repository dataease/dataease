<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { Icon } from '@/components/icon-custom'
import icon_warning_colorful from '@/assets/svg/icon_warning_colorful.svg'
import { useI18n } from '@/hooks/web/useI18n'
import { deTypeLabel } from './util'

const props = withDefaults(
  defineProps<{
    currentType: number
    detectedType: number
    active?: boolean
  }>(),
  { active: true }
)
const emit = defineEmits<{ keep: [] }>()
const { t } = useI18n()
const visible = ref(false)
// 触发元素被 v-show 隐藏后 popper 会重新定位到左上角，因此在关闭动画期间直接隐藏 popper 避免闪现。
const popperClass = computed(() =>
  props.active ? 'field-type-drift-popper' : 'field-type-drift-popper is-trigger-hidden'
)

// 切换 tab 时触发元素会被 v-show 隐藏，popover 会保留旧定位并漂移，因此隐藏前主动关闭。
watch(
  () => props.active,
  active => {
    if (!active) visible.value = false
  }
)
</script>

<template>
  <el-popover
    v-model:visible="visible"
    trigger="click"
    placement="bottom-start"
    :width="240"
    :popper-class="popperClass"
  >
    <template #reference>
      <span
        class="field-type-drift-tip"
        role="button"
        tabindex="0"
        :aria-label="t('dataset.type_change_pending_title')"
      >
        <el-icon
          ><Icon><icon_warning_colorful class="svg-icon" /></Icon
        ></el-icon>
      </span>
    </template>
    <div class="field-type-drift-compare">
      <div class="compare-row">
        <span>{{ t('dataset.current_field_type') }}</span>
        <b>{{ deTypeLabel(currentType) }}</b>
      </div>
      <div class="compare-row">
        <span>{{ t('dataset.sql_detected_type') }}</span>
        <b class="detected">{{ deTypeLabel(detectedType) }}</b>
      </div>
    </div>
    <el-button text type="primary" class="keep-type" @click="emit('keep')">{{
      t('dataset.keep_current_type')
    }}</el-button>
  </el-popover>
</template>

<style scoped lang="less">
.field-type-drift-tip {
  display: inline-flex;
  align-items: center;
  flex-shrink: 0;
  color: #8c5108;
  cursor: pointer;
}
.field-type-drift-compare {
  display: grid;
  gap: 6px;
  font-size: 13px;
  .compare-row {
    display: flex;
    justify-content: space-between;
    gap: 8px;
    color: #646a73;
    b {
      font-weight: 500;
      color: #1f2329;
    }
    .detected {
      color: #8c5108;
    }
  }
}
.keep-type {
  margin-top: 8px;
  padding: 0;
}
:global(.field-type-drift-popper.is-trigger-hidden) {
  display: none !important;
}
</style>
