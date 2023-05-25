<script lang="ts" setup>
import { ref, onMounted, computed, toRefs } from 'vue'
import { ElAside, ElIcon, ElRow } from 'element-plus-secondary'
import { string } from 'vue-types'

const props = defineProps({
  width: {
    required: false,
    type: Number,
    default: 200
  },
  asidePosition: {
    required: false,
    type: string,
    default: 'left'
  },
  title: string
})

const { width, asidePosition } = toRefs(props)
const isCollapse = ref(false)
const collapseWidth = ref(30)
const collapseChange = () => {
  isCollapse.value = !isCollapse.value
}
const widthShow = computed(() => `${isCollapse.value ? 36 : width.value}px`)
</script>

<template>
  <el-aside class="dv-aside" :class="'aside-' + asidePosition" :width="widthShow">
    <el-row align="middle" class="title" justify="space-between">
      <span v-show="!isCollapse">{{ title }}</span>
      <el-icon :title="title" class="custom-icon" size="20px" @click="collapseChange">
        <Expand
          v-if="
            (isCollapse && asidePosition === 'left') || (!isCollapse && asidePosition === 'right')
          "
        />
        <Fold v-else />
      </el-icon>
    </el-row>
    <div class="main-content" v-show="!isCollapse">
      <slot />
    </div>
    <div class="collapse-title" v-show="isCollapse">
      <span>{{ title }}</span>
    </div>
  </el-aside>
</template>

<style lang="less">
.dv-aside {
  position: relative;
  transition: 0.5s;
  color: white;
  background-color: @side-area-background;
  .title {
    border-bottom: #525552 1px solid;
    height: 45px;
    overflow: hidden;
    white-space: nowrap;
    text-overflow: ellipsis;
    padding: 8px 10px 8px 8px;
  }
  .collapse-title {
    width: 35px;
    text-align: center;
    padding: 5px;
  }
  .main-content {
    height: calc(100% - 45px);
    overflow-y: auto;
  }
  .custom-icon {
    position: absolute;
    right: 5px;
    top: 12px;
    cursor: pointer;
  }
}
.aside-left {
  border-right: @side-outline-border-color 1px solid;
}
.aside-right {
  border-left: @side-outline-border-color 1px solid;
}
</style>
