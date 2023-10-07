<script setup lang="tsx">
import { reactive, ref, toRefs } from 'vue'
import eventBus from '@/utils/eventBus'
import Icon from '@/components/icon-custom/src/Icon.vue'
import { CANVAS_MATERIAL } from '@/custom-component/common/ComponentConfig'
import { ElScrollbar } from 'element-plus-secondary'

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

const commonGroup = ref<InstanceType<typeof ElScrollbar>>()

const state = reactive({
  curCategory: 'CanvasBoard',
  groupList: CANVAS_MATERIAL
})

const scrollTo = offsetTop => {
  commonGroup?.value!.setScrollTop(offsetTop)
}

const anchorPosition = anchor => {
  const element = document.querySelector(anchor)
  scrollTo(element.offsetTop)
}

const newComponent = ({ category, innerType }) => {
  eventBus.emit('handleNew', { componentName: category, innerType: innerType })
}

const handleDragStart = e => {
  e.dataTransfer.setData('id', e.target.dataset.id)
}

const groupActiveChange = category => {
  state.curCategory = category
  anchorPosition('#' + category)
}
</script>

<template>
  <el-row class="group" @dragstart="handleDragStart">
    <div class="group-left">
      <ul class="ul-custom">
        <li
          class="li-custom"
          :class="{ 'li-custom-active': state.curCategory === groupInfo.category }"
          v-for="groupInfo in state.groupList"
          :key="groupInfo.category"
          @click="groupActiveChange(groupInfo.category)"
        >
          {{ groupInfo.title }}
        </li>
      </ul>
    </div>
    <el-scrollbar ref="commonGroup" class="group-right" height="392px">
      <el-row :id="groupInfo.category" v-for="groupInfo in state.groupList" :key="groupInfo.title">
        <el-col
          v-show="state.curCategory === groupInfo.category"
          :class="'item' + groupInfo.span"
          :span="groupInfo.span"
          v-for="chartInfo in groupInfo.details"
          :key="chartInfo.title"
        >
          <div
            v-on:click="newComponent({ category: groupInfo.category, innerType: chartInfo.value })"
            class="item-top"
            draggable="true"
            :data-id="groupInfo.category + '&' + chartInfo.value"
          >
            <Icon
              v-if="chartInfo.type === 'outer_svg'"
              class-name="item-top-icon"
              :name="chartInfo.icon"
            />
            <component v-else style="color: #a6a6a6" :is="chartInfo.icon"></component>
          </div>
          <div v-if="chartInfo.title" class="item-bottom">
            <span>{{ chartInfo.title }}</span>
          </div>
        </el-col>
      </el-row>
    </el-scrollbar>
  </el-row>
</template>

<style lang="less" scoped>
.group {
  display: flex;
  max-height: 400px;
  height: 100%;
  margin-top: 4px;
  .group-left {
    width: 100px;
    height: 100%;
    .ul-custom {
      padding-inline-start: 0px;
      color: @canvas-main-font-color;
      .li-custom {
        margin-top: 1px;
        font-weight: 400;
        font-size: 14px;
        line-height: 32px;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
        list-style-type: none;
        list-style-position: inside;
        border-radius: 4px;
        padding-left: 8px;
        &:hover {
          background: rgba(255, 255, 255, 0.1);
          cursor: pointer;
        }
      }

      .li-custom a:hover {
        background: none;
      }

      .li-a {
        color: #1f2329;
      }
    }
  }
  .group-right {
    border-left: 1px solid @side-outline-border-color;
    flex: 1;
    padding: 4px 0 4px 12px;
  }
}

.li-custom-active {
  background: rgba(51, 112, 255, 0.1);
  color: #3370ff !important;
  .li-a {
    color: #3370ff !important;
  }
}

.item8 {
  margin-bottom: 12px;
  .item-top {
    width: 88px;
    height: 64px;
    background: #1a1a1a;
    border-radius: 4px;
    cursor: pointer;
    &:hover {
      border: 1px solid #3370ff;
    }
    .item-top-icon {
      width: 86px;
      height: 62px;
      color: @canvas-main-font-color;
    }
  }
  .item-bottom {
    height: 20px;
    line-height: 20px;
    color: #a6a6a6;
    font-size: 12px;
    text-align: center;
  }
}

.item4 {
  margin-bottom: 12px;
  .item-top {
    width: 24px;
    height: 24px;
    border-radius: 4px;
    cursor: pointer;
    &:hover {
      border: 1px solid #3370ff;
    }
    .item-top-icon {
      width: 23px;
      height: 23px;
      color: @canvas-main-font-color;
    }
  }
  .item-bottom {
    height: 20px;
    line-height: 20px;
    color: #a6a6a6;
    font-size: 12px;
    text-align: center;
  }
}
</style>
