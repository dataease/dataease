<script lang="tsx" setup>
import { useI18n } from '@/hooks/web/useI18n'
import { PropType } from 'vue'
const { t } = useI18n()

const props = defineProps({
  dragList: {
    type: Array,
    required: true
  },
  marginTop: {
    type: String,
    default: '0'
  },
  themes: {
    type: String as PropType<EditorTheme>,
    default: 'dark'
  }
})
</script>

<template>
  <div>
    <div
      v-if="!props.dragList || props.dragList.length === 0"
      :style="'margin-top:' + props.marginTop"
      class="drag-placeholder-style"
    >
      <span
        class="drag-placeholder-style-span"
        :class="{ 'drag-placeholder-style-span--dark': themes === 'dark' }"
        >{{ t('chart.placeholder_field') }}</span
      >
    </div>
  </div>
</template>

<style lang="less" scoped>
span {
  font-size: 12px;
}

.drag-placeholder-style {
  position: absolute;
  top: calc(50% - 4px);
  width: 100%;
  color: #757575;
  text-align: left;
  padding: 0 16px;

  .drag-placeholder-style-span {
    color: #8d9199;
    font-size: 12px;
    font-style: normal;
    font-weight: 400;

    &.drag-placeholder-style-span--dark {
      color: #757575;
    }
  }
}
</style>
