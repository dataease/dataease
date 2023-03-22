<script setup lang="ts">
import { dvMainStoreWithOut } from '@/store/modules/data-visualization/dvMain'
import { storeToRefs } from 'pinia'
import { ref } from 'vue'
const dvMainStore = dvMainStoreWithOut()

const { canvasStyleData } = storeToRefs(dvMainStore)

const options = ref({
  color: '颜色',
  opacity: '不透明度',
  backgroundColor: '背景色',
  fontSize: '字体大小'
})

const isIncludesColor = str => {
  return str.toLowerCase().includes('color')
}
</script>

<template>
  <div class="attr-container">
    <p class="title">画布属性</p>
    <el-form style="padding: 20px">
      <el-form-item v-for="(key, index) in Object.keys(options)" :key="index" :label="options[key]">
        <el-color-picker
          v-if="isIncludesColor(key)"
          v-model="canvasStyleData[key]"
          show-alpha
        ></el-color-picker>
        <el-input v-else v-model.number="canvasStyleData[key]" type="number" />
      </el-form-item>
    </el-form>
  </div>
</template>

<style lang="less">
.attr-container {
  .title {
    text-align: center;
    margin-bottom: 10px;
    height: 40px;
    line-height: 40px;
    border-bottom: 2px solid #e4e7ed;
    font-size: 14px;
    font-weight: 500;
    color: #303133;
  }
}
</style>
