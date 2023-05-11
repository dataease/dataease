<script lang="tsx" setup>
import { useI18n } from '@/hooks/web/useI18n'
const { t } = useI18n()

const props = defineProps({
  dragList: {
    type: Array,
    required: true
  }
})
</script>

<template>
  <div>
    <div v-if="!props.dragList || props.dragList.length === 0" class="drag-placeholder-style">
      <span class="drag-placeholder-style-span">{{ t('chart.placeholder_field') }}</span>
    </div>
  </div>
</template>

<style lang="less" scoped>
span {
  font-size: 14px;
}

.drag-placeholder-style {
  position: absolute;
  top: calc(50% - 2px);
  left: 0;
  width: 100%;
  color: #cccccc;
}

.drag-placeholder-style-span {
  padding-left: 16px;
}
</style>
