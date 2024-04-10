<script setup lang="ts">
import { ref, toRefs } from 'vue'
import { propTypes } from '@/utils/propTypes'

const props = defineProps({
  title: propTypes.string,
  baseWidth: {
    required: false,
    type: Number,
    default: 300
  },
  actionSelection: {
    required: true,
    type: Object
  },
  themes: {
    type: String,
    default: 'dark'
  }
})

const selection = ref()

const selectionChange = () => {
  // do selection
}

const { title, themes, actionSelection } = toRefs(props)
</script>

<template>
  <el-popover placement="right-start" :width="baseWidth" trigger="click" :show-arrow="false">
    <template #reference>
      <el-icon class="option-set"><Setting /></el-icon>
    </template>
    <el-row>
      如果联动维度已配置钻取，点击维度将
      <el-radio-group
        style="margin-top: 12px"
        v-model="actionSelection.linkageActive"
        @change="selectionChange"
      >
        <el-radio label="custom"
          ><span style="font-weight: normal">弹出浮框，由用户选择联动或者下钻</span></el-radio
        >
        <el-radio label="auto"
          ><span style="font-weight: normal">同时触发联动和下钻</span></el-radio
        >
      </el-radio-group>
    </el-row>
  </el-popover>
</template>
<style lang="less" scoped>
.option-set {
  position: absolute;
  left: 90px;
  top: 30px;
}
</style>
