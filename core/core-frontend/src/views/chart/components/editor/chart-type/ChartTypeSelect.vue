<script lang="ts" setup>
import { iconChartMap } from '@/components/icon-group/chart-list'
import icon_down_outlined1 from '@/assets/svg/icon_down_outlined-1.svg'
import { toRefs } from 'vue'

const props = defineProps({
  propValue: {
    type: Array,
    default: () => []
  },
  state: {
    type: Object,
    default: () => ({
      useless: ''
    })
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

const { state } = toRefs(props)
const emits = defineEmits(['onTypeChange'])
const onTypeChange = (render, type) => {
  emits('onTypeChange', render, type)
}
</script>

<template>
  <el-popover
    :offset="4"
    placement="bottom-end"
    width="434"
    trigger="click"
    append-to-body
    :popper-class="'chart-type-style-' + themes"
    :persistent="false"
  >
    <template #reference>
      <el-select
        v-model="state.useless"
        popper-class="chart-type-hide-options"
        class="chart-type-select"
        :suffix-icon="icon_down_outlined1"
        :effect="themes"
        size="small"
      >
        <template #prefix>
          <Icon
            class-name="chart-type-select-icon"
            v-if="state.chartTypeOptions[0]?.isPlugin"
            :static-content="state.chartTypeOptions[0]?.icon"
          />
          <Icon v-else>
            <component
              class="svg-icon chart-type-select-icon"
              :is="iconChartMap[state.chartTypeOptions[0].icon]"
            ></component>
          </Icon>
        </template>
        <template #default>
          <el-option
            v-for="item in state.chartTypeOptions"
            :key="item.value"
            :label="item.title"
            :value="item.value"
          />
        </template>
      </el-select>
    </template>
    <template #default>
      <chart-type :themes="themes" :type="type" @on-type-change="onTypeChange" />
    </template>
  </el-popover>
</template>

<style lang="less" scoped>
.chart-type-select {
  width: 100%;
  margin-top: 8px;
  :deep(.ed-select__prefix) {
    padding: 0;
    margin: 0;
    border: none;
    height: 20px;
    .chart-type-select-icon {
      width: 23px;
      height: 16px;
    }

    &::after {
      display: none;
    }
  }
  :deep(.ed-input) {
    height: 28px;
  }
}
</style>
