<script setup lang="ts">
import { toRefs } from 'vue'
import { propTypes } from '@/utils/propTypes'
import Icon from '../icon-custom/src/Icon.vue'
import { ElCol, ElIcon } from 'element-plus-secondary'

const props = defineProps({
  title: propTypes.string,
  iconName: propTypes.string,
  showSplitLine: propTypes.bool,
  active: propTypes.bool
})

const { title, iconName, showSplitLine } = toRefs(props)
</script>

<template>
  <el-row class="group_icon" :class="{ 'group-right-border': showSplitLine }">
    <el-col :span="24" class="group_inner" :class="{ 'inner-active': active }">
      <Icon class="toolbar-icon" :name="iconName" />
      <span>{{ title }}</span>
    </el-col>
  </el-row>
</template>
<style lang="less" scoped>
.group_icon {
  padding-left: 10px;
  padding-right: 10px;
}
.group_inner {
  padding: 5px;
  display: flex;
  cursor: pointer;
  flex-direction: column;
  align-items: center;
  border-radius: 4px;
  &:hover {
    background: rgba(255, 255, 255, 0.1);
  }
  &:active {
    background: rgba(255, 255, 255, 0.2);
  }
  span {
    float: left;
    font-size: 12px;
    color: #a6a6a6;
    margin-top: 4px;
  }
}

.group-right-border {
  border-right: 1px solid rgba(255, 255, 255, 0.15);
}

.inner-active {
  border: 1px solid #3370ff;
  background: rgba(255, 255, 255, 0.2);
}

.toolbar-icon {
  float: left;
  width: 20px;
  height: 20px;
}
</style>
