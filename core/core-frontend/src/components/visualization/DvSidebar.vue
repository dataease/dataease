<script lang="ts" setup>
import { ref, onMounted, computed, toRefs } from 'vue'
import { ElAside, ElIcon, ElRow } from 'element-plus-secondary'
import { string } from 'vue-types'

const props = defineProps({
  width: {
    required: false,
    default: 200
  },
  title: string
})

const { width } = toRefs(props)
const isCollapse = ref(false)
const collapseWidth = ref(30)
const collapseChange = () => {
  isCollapse.value = !isCollapse.value
}
const widthShow = computed(() => `${isCollapse.value ? 36 : width.value}px`)
</script>

<template>
  <el-aside class="dv-aside" :width="widthShow">
    <el-row align="middle" class="title" justify="space-between">
      <span v-show="!isCollapse">{{ title }}</span>
      <el-icon :title="title" class="custom-icon" size="20px" @click="collapseChange">
        <Expand v-if="isCollapse" />
        <Fold v-else />
      </el-icon>
    </el-row>
    <div v-show="!isCollapse">
      <slot />
    </div>
  </el-aside>
</template>

<style lang="less">
.dv-aside {
  position: relative;
  transition: 0.5s;
  color: white;
  background-color: #232c31;
  border-right: #525552 1px solid;
  border-bottom: #525552 1px solid;
  .title {
    border-bottom: #525552 1px solid;
    height: 45px;
    overflow: hidden;
    white-space: nowrap;
    text-overflow: ellipsis;
    padding: 8px 10px 8px 8px;
  }
  .custom-icon {
    position: absolute;
    right: 5px;
    top: 12px;
    cursor: pointer;
  }
}
</style>
