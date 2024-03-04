<script setup lang="ts">
import { toRefs } from 'vue'
import { propTypes } from '@/utils/propTypes'
import Icon from '../icon-custom/src/Icon.vue'
import { ElCol } from 'element-plus-secondary'

const props = defineProps({
  title: propTypes.string,
  tips: propTypes.string,
  iconName: propTypes.string,
  showSplitLine: propTypes.bool,
  active: propTypes.bool
})

const { title, tips, iconName, showSplitLine } = toRefs(props)
const emits = defineEmits(['customClick'])
</script>

<template>
  <el-row
    class="group_icon"
    :title="tips"
    :class="{ 'group-right-border': showSplitLine }"
    v-on:click="emits('customClick')"
  >
    <el-col :span="24" class="group_inner" :class="{ 'inner-active': active }">
      <el-icon class="toolbar-icon">
        <Icon :name="iconName" />
      </el-icon>
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
  display: flex;
  cursor: pointer;
  align-items: center;
  justify-content: center;
  border-radius: 4px;
  color: #ffffff99;
  position: relative;
  &::after {
    content: '';
    border-radius: 4px;
    display: none;
    position: absolute;
    width: calc(100% + 10px);
    height: calc(100% + 10px);
    top: -5px;
    left: -5px;
  }

  &:hover {
    &::after {
      display: block;
      background: rgba(255, 255, 255, 0.1);
    }
  }
  &:active {
    &::after {
      display: block;
      background: rgba(255, 255, 255, 0.2);
    }
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
  border: 1px solid var(--ed-color-primary);
  background: rgba(255, 255, 255, 0.2);
}

.toolbar-icon {
  font-size: 20px;
}
</style>
