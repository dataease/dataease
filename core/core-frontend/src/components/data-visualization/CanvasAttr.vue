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

const canvasAttrActiveNames = ref(['size', 'background', 'color'])
</script>

<template>
  <div class="attr-container">
    <el-row>
      <el-collapse v-model="canvasAttrActiveNames">
        <el-collapse-item title="尺寸" name="size">
          <el-row style="display: flex; padding-top: 10px">
            <span>W</span>
            <el-input-number
              class="input-border-style"
              style="margin-left: 5px"
              v-model="canvasStyleData.width"
              :min="600"
              :max="4096"
              size="small"
              controls-position="right"
            />
            <span style="margin-left: 10px">H</span>
            <el-input-number
              style="margin-left: 5px"
              v-model="canvasStyleData.height"
              :min="600"
              :max="4096"
              size="small"
              controls-position="right"
            />
          </el-row>
        </el-collapse-item>
        <el-collapse-item title="背景" name="background">
          <el-row>
            <el-radio-group v-model="canvasStyleData.backgroundType">
              <el-radio label="color">颜色</el-radio>
              <el-radio label="image">图片</el-radio>
            </el-radio-group>
          </el-row>
          <el-row v-show="canvasStyleData.backgroundType === 'color'">
            <el-color-picker v-model="canvasStyleData.background" show-alpha></el-color-picker>
          </el-row>
          <el-row v-show="canvasStyleData.backgroundType === 'image'"> 图片上传 </el-row>
        </el-collapse-item>
        <el-collapse-item title="配色方案" name="color">
          <div>配色方案设置</div>
        </el-collapse-item>
      </el-collapse>
    </el-row>
  </div>
</template>

<style lang="less" scoped>
.attr-container {
  background-color: rgba(37, 45, 54, 1);
  color: #fff;
  z-index: 20;
  height: 100%;
  width: 100%;
  min-width: 300px;
}

:deep(.el-collapse-item__header) {
  background-color: rgba(29, 36, 42, 1) !important;
  color: #ffffff;
  border-bottom: 1px solid rgba(85, 85, 85, 1);
}
:deep(.el-collapse-item__content) {
  background-color: rgba(37, 45, 54, 1);
  color: #ffffff;
  padding-left: 5px;
}

:deep(.el-collapse-item__wrap) {
  border-bottom: 1px solid rgba(85, 85, 85, 1);
}
:deep(.el-collapse) {
  width: 100%;
}
:deep(.el-input__wrapper) {
  background-color: rgba(37, 45, 54, 1);
}

:deep(.el-input__inner) {
  color: #ffffff;
}

:deep(.el-input-number__decrease) {
  background-color: rgba(37, 45, 54, 1);
  color: #ffffff;
}
:deep(.el-input-number__increase) {
  background-color: rgba(37, 45, 54, 1);
  color: #ffffff;
}
</style>
