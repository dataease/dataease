<template>
  <el-col
    v-for="templateItem in arr"
    :key="templateItem"
    style="float: left; width: 25%; padding: 8px 8px 0; text-align: center; flex: 0"
  >
    <div class="testcase-template_skeleton">
      <div class="template-img_skeleton" :style="classBackground" />
      <el-row class="bottom-area_skeleton">
        <div class="area-skeleton_inner"></div>
      </el-row>
    </div>
  </el-col>
</template>

<script setup lang="ts">
import { computed } from 'vue'

const props = defineProps({
  width: {
    type: Number
  }
})
const classBackground = computed(() => {
  return {
    width: props.width + 'px',
    height: props.width * 0.58 + 'px'
  }
})
const arr = Array(40)
  .fill(0)
  .map((_, index) => index)
</script>

<style scoped lang="less">
.testcase-template_skeleton {
  position: relative;
  display: inline-block;
  margin: 0;
  border: 1px solid rgba(222, 224, 227, 1);
  box-sizing: border-box;
  border-radius: 4px;
  width: 100%;
  background: #fff;
  overflow: hidden;
  &:hover {
    box-shadow: 0px 6px 24px 0px #1f232914;
  }
}

.template-img_skeleton {
  background: #eff0f199;
  margin: 4px 4px 0 4px;
  width: 100%;
  height: 100%;
  border-radius: 4px 4px 0 0;
}

.bottom-area_skeleton {
  height: 38px;
  padding: 12px;
  border-top: 1px solid rgba(222, 224, 227, 1);
  .area-skeleton_inner {
    background-color: #eff0f1;
    width: 100%;
    height: 100%;
    border-radius: 4px;
  }
}
</style>
