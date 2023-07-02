<script lang="tsx" setup>
import { reactive, ref, toRefs } from 'vue'
import { CHART_TYPE_CONFIGS } from '@/views/chart/components/editor/util/chart'
import { ElCol, ElRow } from 'element-plus-secondary'
import Icon from '@/components/icon-custom/src/Icon.vue'

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
  },
  type: {
    type: String,
    required: true
  },
  themes: {
    type: String,
    default: 'dark'
  }
})

const emit = defineEmits(['onTypeChange'])

const { propValue, element, themes } = toRefs(props)
const currentPane = ref('common')

const state = reactive({
  curCategory: 'quota',
  chartGroupList: CHART_TYPE_CONFIGS
})

const anchorPosition = anchor => {
  const element = document.querySelector(anchor)
  element.scrollIntoView({
    behavior: 'smooth'
  })
}

const newComponent = innerType => {
  emit('onTypeChange', innerType)
}

const groupActiveChange = category => {
  state.curCategory = category
  anchorPosition('#' + category + '-edit')
}
</script>

<template>
  <el-row class="group" :class="'chart-' + themes">
    <div class="group-left">
      <ul class="ul-custom">
        <li
          class="li-custom"
          :class="{ 'li-custom-active': state.curCategory === chartGroupInfo.category }"
          v-for="chartGroupInfo in state.chartGroupList"
          :key="chartGroupInfo.category"
          @click="groupActiveChange(chartGroupInfo.category)"
        >
          {{ chartGroupInfo.title }}
        </li>
      </ul>
    </div>
    <div id="userViewGroup" class="group-right">
      <el-row
        :id="chartGroupInfo.category + '-edit'"
        v-for="chartGroupInfo in state.chartGroupList"
        :key="chartGroupInfo.title"
      >
        <el-col
          class="item"
          :span="8"
          v-for="chartInfo in chartGroupInfo.details"
          :key="chartInfo.title"
        >
          <div
            v-on:click="newComponent(chartInfo.value)"
            class="item-top"
            :class="props.type === chartInfo.value ? 'item-active' : ''"
            :data-id="'UserView&' + chartInfo.value"
          >
            <Icon class-name="item-top-icon" :name="chartInfo.icon" />
          </div>
          <div class="item-bottom">
            <span>{{ chartInfo.title }}</span>
          </div>
        </el-col>
      </el-row>
    </div>
  </el-row>
</template>

<style lang="less" scoped>
.chart-light {
  color: #646a73 !important;
  :deep(.group-right) {
    border-left: 1px solid @side-outline-border-color-light!important;
  }
  :deep(.item-top) {
    background-color: #dee0e3 !important;
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
}
.group {
  display: flex !important;
  max-height: 400px;
  height: 100%;
  width: 410px;
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
    max-height: 400px;
    overflow-y: auto;
    border-left: 1px solid @side-outline-border-color;
    flex: 1;
    padding: 4px 0px 0 12px;
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
  .item-top {
    width: 88px;
    height: 64px;
    background: transparent;
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
  .item-active {
    border: 1px solid #3370ff;
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
