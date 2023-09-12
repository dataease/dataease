<script setup lang="tsx">
import { reactive, ref, toRefs } from 'vue'
import eventBus from '@/utils/eventBus'
import { CHART_TYPE_CONFIGS } from '@/views/chart/components/editor/util/chart'
import Icon from '@/components/icon-custom/src/Icon.vue'
import { commonHandleDragEnd, commonHandleDragStart } from '@/utils/canvasUtils'
import { ElScrollbar } from 'element-plus-secondary'

const props = defineProps({
  propValue: {
    type: Array,
    default: () => []
  },
  dvModel: {
    type: String,
    default: 'dv'
  },
  element: {
    type: Object,
    default() {
      return {
        propValue: null
      }
    }
  },
  themes: {
    type: String,
    default: 'dark'
  }
})

const { propValue, element, dvModel } = toRefs(props)
const currentPane = ref('common')

const userViewGroup = ref<InstanceType<typeof ElScrollbar>>()

const state = reactive({
  curCategory: 'quota',
  chartGroupList: CHART_TYPE_CONFIGS
})

const scrollTo = offsetTop => {
  userViewGroup?.value!.setScrollTop(offsetTop)
}

const anchorPosition = anchor => {
  const element = document.querySelector(anchor)
  scrollTo(element.offsetTop)
}

const newComponent = innerType => {
  eventBus.emit('handleNew', { componentName: 'UserView', innerType: innerType })
}

const handleDragStart = e => {
  commonHandleDragStart(e, dvModel.value)
}

const handleDragEnd = e => {
  commonHandleDragEnd(e, dvModel.value)
}

const groupActiveChange = category => {
  state.curCategory = category
  anchorPosition('#' + category)
}
</script>

<template>
  <el-row class="group" :class="themes" @dragstart="handleDragStart" @dragend="handleDragEnd">
    <div class="group-left">
      <ul class="ul-custom">
        <li
          class="li-custom"
          :class="{ 'li-custom-active': state.curCategory === chartGroupInfo.category }"
          v-for="chartGroupInfo in state.chartGroupList"
          v-show="chartGroupInfo.display !== 'hidden'"
          :key="chartGroupInfo.category"
          @click="groupActiveChange(chartGroupInfo.category)"
        >
          {{ chartGroupInfo.title }}
        </li>
      </ul>
    </div>
    <el-scrollbar ref="userViewGroup" class="group-right" height="392px">
      <el-row
        :id="chartGroupInfo.category"
        v-for="chartGroupInfo in state.chartGroupList"
        v-show="chartGroupInfo.display !== 'hidden'"
        :key="chartGroupInfo.title"
      >
        <el-row class="group-title">{{ chartGroupInfo.title }}</el-row>
        <el-row style="width: 100%">
          <el-col
            class="item"
            :span="8"
            v-for="chartInfo in chartGroupInfo.details"
            :key="chartInfo.title"
          >
            <div
              v-on:click="newComponent(chartInfo.value)"
              class="item-top"
              draggable="true"
              :data-id="'UserView&' + chartInfo.value"
            >
              <Icon
                class-name="item-top-icon"
                :name="chartInfo.icon + (props.themes === 'dark' ? '-dark' : '')"
              />
            </div>
            <div class="item-bottom">
              <span>{{ chartInfo.title }}</span>
            </div>
          </el-col>
        </el-row>
      </el-row>
    </el-scrollbar>
  </el-row>
</template>

<style lang="less" scoped>
.light {
  color: #646a73 !important;
  :deep(.group-right) {
    border-left: 1px solid @side-outline-border-color-light!important;
  }
  :deep(.item-top) {
    background-color: #f5f6f7 !important;
  }
  :deep(.ul-custom) {
    color: @chart-change-font-color-light!important;
  }
  :deep(.item-bottom) {
    color: @chart-change-font-color-light!important;
  }
  :deep(.item-top-icon) {
    color: @chart-change-font-color-light!important;
  }
  :deep(.group-title) {
    color: #1f2329;
  }
}
.group {
  display: flex;
  max-height: 400px;
  height: 100%;
  .group-left {
    width: 100px;
    height: 100%;
    .ul-custom {
      padding-top: 8px;
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
          background: rgba(31, 35, 41, 0.1);
          cursor: pointer;
        }
      }

      .li-custom a:hover {
        //background: none;
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
.custom_img {
  width: 100px;
  height: 70px;
  cursor: pointer;
}

.li-custom-active {
  background: rgba(51, 112, 255, 0.1);
  color: #3370ff !important;
  .li-a {
    color: #3370ff !important;
  }
}

.item {
  margin-bottom: 12px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
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

.group-title {
  width: 100%;
  font-weight: 400;
  font-size: 12px;
  line-height: 20px;
  color: @canvas-main-font-color;
}
</style>
