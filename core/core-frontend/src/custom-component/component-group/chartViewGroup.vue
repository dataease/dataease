<script setup lang="tsx">
import { iconChartMap } from '@/components/icon-group/chart-list'
import { computed, reactive, ref, toRefs } from 'vue'
import eventBus from '@/utils/eventBus'
import { CHART_TYPE_CONFIGS } from '@/views/chart/components/editor/util/chart'
import Icon from '@/components/icon-custom/src/Icon.vue'
import { commonHandleDragEnd, commonHandleDragStart } from '@/utils/canvasUtils'
import { ElScrollbar } from 'element-plus-secondary'
import { XpackComponent } from '@/components/plugin'
import { iconChartDarkMap } from '@/components/icon-group/chart-dark-list'

const props = defineProps({
  propValue: {
    type: Array,
    default: () => []
  },
  dvModel: {
    type: String,
    default: 'dv'
  },
  chartType: {
    type: String,
    default: 'quota'
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

const emit = defineEmits(['onTypeChange'])

const userViewGroup = ref<InstanceType<typeof ElScrollbar>>()
const state = reactive({
  curCategory: 'quota',
  chartGroupList: CHART_TYPE_CONFIGS
})

const scrollTo = offsetTop => {
  userViewGroup?.value.setScrollTop(offsetTop)
}

const anchorPosition = anchor => {
  const element = document.querySelector(anchor)
  scrollTo(element.offsetTop)
}

const newComponent = (render, innerType) => {
  emit('onTypeChange', render, innerType)
}
const chartGroupListScroll = computed(() => {
  return state.chartGroupList.reduce(
    (pre, next) => {
      if (next.display !== 'hidden') {
        const height = (Math.floor((next.details.length - 1) / 3) + 1) * 88 + 20
        if (pre.top === 0) {
          pre.top += height
          pre[0] = next.category
        } else {
          pre[pre.top] = next.category
          pre.top += height
        }
        return pre
      }
      return pre
    },
    { top: 0 }
  )
})

const handleScroll = val => {
  let scrollTop: string | number = 0
  for (const key in chartGroupListScroll.value) {
    if (val.scrollTop > key) {
      scrollTop = key
    }
  }
  state.curCategory = chartGroupListScroll.value[scrollTop]
}

const groupActiveChange = category => {
  state.curCategory = category
  anchorPosition('#' + category)
}
const loadPluginCategory = data => {
  data.forEach(item => {
    const { category, title, render, chartValue, chartTitle, icon, staticMap } = item
    const node = {
      render,
      category,
      icon,
      value: chartValue,
      title: chartTitle,
      isPlugin: true,
      staticMap
    }
    const stack = [...state.chartGroupList]
    let findParent = false
    while (stack?.length) {
      const parent = stack.pop()
      if (parent.category === category) {
        const chart = parent.details.find(chart => chart.value === node.value)
        if (!chart) {
          parent.details.push(node)
        }
        findParent = true
      }
    }
    if (!findParent) {
      state.chartGroupList.push({
        category,
        title,
        display: 'show',
        details: [node]
      })
    }
  })
}
</script>

<template>
  <div class="group-right">
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
          :span="6"
          v-for="chartInfo in chartGroupInfo.details"
          :key="chartInfo.title"
        >
        <el-tooltip placement="right" :offset="6" :hide-after="0" :enterable="false">
          <template #content>
            <div>点击可切换为{{ chartInfo.title }}</div>
            <div>其他信息</div>
          </template>
          <div
            v-on:click="newComponent(chartInfo.render, chartInfo.value)"
            class="item-top"
            :class="{active: chartInfo.value === chartType}"
            draggable="true"
            :data-id="'UserView&' + chartInfo.value"
          >
            <Icon
              class-name="item-top-icon"
              v-if="chartInfo['isPlugin']"
              :static-content="chartInfo.icon"
            />
            <Icon v-else class-name="item-top-icon"
              ><component
                class="svg-icon item-top-icon"
                :is="
                  props.themes === 'dark'
                    ? iconChartDarkMap[`${chartInfo.icon}-dark`]
                    : iconChartMap[chartInfo.icon]
                "
              ></component
            ></Icon>
          </div>
        </el-tooltip>
          <!-- <div :title="chartInfo.title" class="item-bottom">
            <span>{{ chartInfo.title }}</span>
          </div> -->
        </el-col>
      </el-row>
    </el-row>
    <XpackComponent
      jsname="L2NvbXBvbmVudC9wbHVnaW5zLWhhbmRsZXIvVmlld0NhdGVnb3J5SGFuZGxlcg=="
      @load-plugin-category="loadPluginCategory"
    />
  </div>
</template>

<style lang="less" scoped>
.custom_img {
  width: 100px;
  height: 70px;
  cursor: pointer;
}
.group-right{
  border-bottom: solid 1px #e0e0e0;
  overflow: scroll;
  height: 210px;
}

.li-custom-active {
  background: var(--ed-color-primary-1a, rgba(51, 112, 255, 0.1));
  color: var(--ed-color-primary) !important;
  .li-a {
    color: var(--ed-color-primary) !important;
  }
}
/* 针对 el-scrollbar 的垂直滚动条宽度设置 */
::-webkit-scrollbar {
  width: 6px!important; /* 垂直滚动条宽度（根据需要调整） */
}

.item {
  margin-bottom: 6px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  .item-top {
    width: 40px;
    height: 34px;
    border-radius: 4px;
    cursor: pointer;
    display: flex;
    align-items: center;
    justify-content: center;
    background-color: #f5f6f7;
    border: 1px solid #e4e4e4;
    &:hover, &.active {
      border: 1px solid var(--ed-color-primary);
    }
  }
  .item-bottom {
    height: 20px;
    line-height: 20px;
    color: #a6a6a6;
    font-size: 12px;
    text-align: center;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
  }
  .item-top-icon{
    transform: scale(0.9);
  }
}

.group-title {
  padding: 0 8px 4px 8px;
  font-weight: 400;
  font-size: 12px;
  line-height: 20px;
  color: #000;
  margin-bottom: 4px;
}
</style>
