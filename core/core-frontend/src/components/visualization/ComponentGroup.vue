<script setup lang="ts">
import { toRefs } from 'vue'
import { propTypes } from '@/utils/propTypes'
import Icon from '../icon-custom/src/Icon.vue'
import { ElCol, ElIcon } from 'element-plus-secondary'
import ComponentButton from '@/components/visualization/ComponentButton.vue'

const props = defineProps({
  title: propTypes.string,
  iconName: propTypes.string,
  showSplitLine: propTypes.bool,
  baseWidth: {
    required: false,
    type: Number,
    default: 200
  },
  themes: {
    type: String,
    default: 'dark'
  }
})

const { title, iconName, baseWidth, themes } = toRefs(props)
</script>

<template>
  <el-popover
    placement="bottom-start"
    :width="baseWidth"
    trigger="click"
    :show-arrow="false"
    :popper-class="'custom-popover-' + themes"
  >
    <template #reference>
      <component-button
        :title="title"
        :icon-name="iconName"
        :show-split-line="showSplitLine"
      ></component-button>
    </template>
    <slot></slot>
  </el-popover>
</template>
<style lang="less" scoped></style>
