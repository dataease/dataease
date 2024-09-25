<template>
  <div style="width: 100%" ref="bgForm">
    <el-form label-position="top" style="width: 100%; margin-bottom: 16px">
      <el-form-item class="form-item no-margin-bottom" :class="'form-item-' + themes">
        <el-checkbox
          size="small"
          :effect="themes"
          v-model="canvasStyleData.popupButtonAvailable"
          @change="onPopButtonChange"
        >
          <div style="display: flex; line-height: 14px">
            <span style="margin-right: 4px">显示弹窗区查询按钮</span>
            <el-tooltip class="item" :effect="themes" placement="bottom">
              <template #content>
                <div>预览时启用</div>
              </template>
              <el-icon class="hint-icon" :class="{ 'hint-icon--dark': themes === 'dark' }">
                <Icon name="icon_info_outlined"><icon_info_outlined class="svg-icon" /></Icon>
              </el-icon>
            </el-tooltip>
          </div>
        </el-checkbox>
      </el-form-item>
    </el-form>
  </div>
</template>

<script setup lang="ts">
import icon_info_outlined from '@/assets/svg/icon_info_outlined.svg'
import { snapshotStoreWithOut } from '@/store/modules/data-visualization/snapshot'
import { dvMainStoreWithOut } from '@/store/modules/data-visualization/dvMain'
import { storeToRefs } from 'pinia'
import { ElIcon } from 'element-plus-secondary'
import Icon from '../icon-custom/src/Icon.vue'
const snapshotStore = snapshotStoreWithOut()

const dvMainStore = dvMainStoreWithOut()
const { canvasStyleData } = storeToRefs(dvMainStore)

const onPopButtonChange = () => {
  snapshotStore.recordSnapshotCache()
}

withDefaults(
  defineProps<{
    themes?: EditorTheme
  }>(),
  {
    themes: 'dark'
  }
)
</script>

<style scoped lang="less">
.hint-icon {
  cursor: pointer;
  font-size: 14px;
  color: #646a73;

  &.hint-icon--dark {
    color: #a6a6a6;
  }
}
:deep(.ed-form-item) {
  display: block;
  margin-bottom: 16px;
}
</style>
