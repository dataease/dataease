<script lang="ts" setup>
import { ref, onMounted, computed, toRefs } from 'vue'
import { ElAside, ElIcon, ElRow } from 'element-plus-secondary'
import { dvMainStoreWithOut } from '@/store/modules/data-visualization/dvMain'
import { storeToRefs } from 'pinia'
const dvMainStore = dvMainStoreWithOut()
const { canvasCollapse } = storeToRefs(dvMainStore)

const props = defineProps({
  width: {
    required: false,
    type: Number,
    default: 200
  },
  asidePosition: {
    required: false,
    type: String,
    default: 'left'
  },
  sideName: {
    required: false,
    type: String,
    default: 'defaultSide'
  },
  title: String
})

const { width, asidePosition, sideName } = toRefs(props)
const isCollapse = ref(false)
const collapseWidth = ref(30)
const collapseChange = () => {
  canvasCollapse.value[sideName.value] = !canvasCollapse.value[sideName.value]
}
const widthShow = computed(() => `${canvasCollapse.value[sideName.value] ? 36 : width.value}px`)
</script>

<template>
  <el-aside class="dv-aside" :class="'aside-' + asidePosition" :width="widthShow">
    <el-row align="middle" class="title" justify="space-between">
      <span v-show="!canvasCollapse[sideName]">{{ title }}</span>
      <el-icon :title="title" class="custom-icon" size="20px" @click="collapseChange">
        <Expand
          v-if="
            (canvasCollapse[sideName] && asidePosition === 'left') ||
            (!canvasCollapse[sideName] && asidePosition === 'right')
          "
        />
        <Fold v-else />
      </el-icon>
    </el-row>
    <div class="main-content" v-show="!canvasCollapse[sideName]">
      <slot />
    </div>
    <div class="collapse-title" v-show="canvasCollapse[sideName]">
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
