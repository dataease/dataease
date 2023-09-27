<script lang="ts" setup>
import { ref, computed, toRefs } from 'vue'
import { ElIcon } from 'element-plus-secondary'
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
  themeInfo: {
    required: false,
    type: String,
    default: 'dark'
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

const { width, asidePosition, sideName, themeInfo } = toRefs(props)
const isCollapse = ref(false)
const collapseWidth = ref(30)
const collapseChange = () => {
  canvasCollapse.value[sideName.value] = !canvasCollapse.value[sideName.value]
}
const widthShow = computed(() => `${canvasCollapse.value[sideName.value] ? 36 : width.value}px`)
</script>

<template>
  <div
    @keyup.stop
    @keydown.stop
    class="dv-aside"
    :class="['aside-' + asidePosition + '-' + themeInfo, 'aside-area-' + themeInfo]"
    :style="{ width: widthShow }"
  >
    <el-row align="middle" :class="'title-' + themeInfo" justify="space-between">
      <span v-if="!canvasCollapse[sideName]">{{ title }}</span>
      <el-icon
        :title="title"
        :class="'custom-icon-' + themeInfo"
        size="20px"
        @click="collapseChange"
      >
        <Expand
          class="collapse-icon"
          v-if="
            (canvasCollapse[sideName] && asidePosition === 'left') ||
            (!canvasCollapse[sideName] && asidePosition === 'right')
          "
        />
        <Fold class="collapse-icon" v-else />
      </el-icon>
    </el-row>
    <div class="main-content" v-if="!canvasCollapse[sideName]">
      <el-scrollbar>
        <slot />
      </el-scrollbar>
    </div>
    <div class="collapse-title" v-if="canvasCollapse[sideName]">
      <span>{{ title }}</span>
    </div>
  </div>
</template>

<style lang="less">
.aside-area-dark {
  color: @dv-canvas-main-font-color;
  background-color: @side-area-background;
}

.aside-area-light {
  color: @canvas-main-font-color-light;
  background-color: #fff;
  :deep(.title) {
    border-bottom: #d5d6d8 1px solid !important;
  }
}
.dv-aside {
  position: relative;
  transition: 0.5s;
  .title-dark {
    font-size: 14px;
    font-weight: 500;
    border-bottom: rgba(255, 255, 255, 0.15) 1px solid;
    height: 45px;
    overflow: hidden;
    white-space: nowrap;
    text-overflow: ellipsis;
    padding: 8px 10px 8px 8px;
  }
  .title-light {
    font-size: 14px;
    font-weight: 500;
    border-bottom: #d5d6d8 1px solid;
    height: 45px;
    overflow: hidden;
    white-space: nowrap;
    text-overflow: ellipsis;
    padding: 8px 10px 8px 8px;
  }
  .collapse-title {
    width: 35px;
    font-size: 14px;
    font-weight: 500;
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
.aside-left-dark {
  border-right: @side-outline-border-color 1px solid;
}
.aside-right-dark {
  border-left: @side-outline-border-color 1px solid;
}

.aside-left-light {
  border-right: @side-outline-border-color-light 1px solid;
}
.aside-right-light {
  border-left: @side-outline-border-color-light 1px solid;
}
.ed-collapse {
  border-top: 0;
}
.is-active {
  border-bottom-color: rgba(31, 35, 41, 0.15) !important;
}

.collapse-icon-light {
  color: #646a73;
}

.collapse-icon-dark {
  color: @canvas-main-font-color;
}
</style>
