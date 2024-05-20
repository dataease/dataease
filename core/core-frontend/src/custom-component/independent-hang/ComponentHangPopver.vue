<script setup lang="ts">
import { computed, toRefs } from 'vue'
import ComponentHang from '@/custom-component/independent-hang/ComponentHang.vue'

const props = defineProps({
  componentData: {
    type: Object,
    required: true
  },
  canvasViewInfo: {
    type: Object,
    required: true
  },
  themes: {
    type: String,
    required: true,
    default: 'dark'
  }
})

const { componentData, canvasViewInfo } = toRefs(props)

const hangComponentData = computed(() =>
  componentData.value.filter(ele => {
    return ele.component === 'VQuery' && ele.isHang === true
  })
)
</script>

<template>
  <el-popover
    width="300"
    trigger="click"
    :show-arrow="false"
    :popper-class="'custom-popover-' + themes"
  >
    <template #reference>
      <div class="reference-position">
        <el-button style="margin-right: 16px">隐藏按钮</el-button>
      </div>
    </template>
    <div>
      <component-hang
        :hang-component-data="hangComponentData"
        :canvas-view-info="canvasViewInfo"
        :scale="100"
      >
      </component-hang>
    </div>
  </el-popover>
</template>
<style lang="less" scoped>
.reference-position {
  position: absolute;
  right: 100px;
  top: 100px;
}
</style>
