<script setup lang="ts">
import { ref, toRefs } from 'vue'
import eventBus from '@/utils/eventBus'

const props = defineProps({
  propValue: {
    type: Array,
    default: () => []
  },
  element: {
    type: Object,
    default() {
      return {
        propValue: null
      }
    }
  }
})

const { propValue, element } = toRefs(props)
const currentPane = ref('common')
const newComponent = innerType => {
  eventBus.emit('handleNew', { componentName: 'UserView', innerType: 'bar' })
}

const handleDragStart = e => {
  e.dataTransfer.setData('id', e.target.dataset.id)
}
</script>

<template>
  <div class="group" @dragstart="handleDragStart">
    <el-tabs v-model="currentPane" tab-position="left" style="height: 200px" class="demo-tabs">
      <el-tab-pane label="常见图标" name="common"></el-tab-pane>
      <el-tab-pane label="指标图" name="quota"></el-tab-pane>
      <el-tab-pane label="线/面图" name="line-surface"></el-tab-pane>
      <el-tab-pane label="柱形图" name="column"></el-tab-pane>
      <div>
        <img
          class="custom_img"
          src="@/assets/img/demo_bar.png"
          :data-id="'UserView&bar'"
          alt=""
          v-on:click="newComponent"
        />
      </div>
    </el-tabs>
  </div>
</template>

<style lang="less" scoped>
.group {
}
.custom_img {
  width: 100px;
  height: 70px;
  cursor: pointer;
}
</style>
