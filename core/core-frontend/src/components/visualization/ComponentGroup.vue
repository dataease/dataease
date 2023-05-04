<script setup lang="ts">
import { toRefs } from 'vue'
import { propTypes } from '@/utils/propTypes'
import Icon from '../icon-custom/src/Icon.vue'
import { ElCol, ElIcon } from 'element-plus-secondary'

const props = defineProps({
  title: propTypes.string,
  iconName: propTypes.string,
  baseWidth: {
    required: false,
    type: Number,
    default: 200
  }
})

const { title, iconName, baseWidth } = toRefs(props)
</script>

<template>
  <el-popover
    placement="bottom-start"
    :width="300"
    trigger="click"
    effect="dark"
    :show-arrow="false"
  >
    <template #reference>
      <el-row class="group_icon">
        <el-col :span="24">
          <Icon class="toolbar-icon" :name="iconName" />
          <br />
          <span style="float: left; font-size: 12px">{{ title }}</span>
        </el-col>
      </el-row>
    </template>
    <slot></slot>
  </el-popover>
</template>
<style lang="less">
.group_icon {
  margin-left: 35px;
  cursor: pointer;
}
.toolbar-icon {
  float: left;
  width: 24px;
  height: 24px;
}
</style>
