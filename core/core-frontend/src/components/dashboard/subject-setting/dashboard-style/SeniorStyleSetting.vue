<template>
  <div style="width: 100%; padding-bottom: 8px">
    <el-form label-position="top" style="width: 100%">
      <div style="width: 100%; padding: 16px 8px 0">
        <el-col :span="12">
          <el-form-item class="form-item" label="联动、钻取、调整的图标颜色">
            <el-color-picker
              v-model="filterStyle.titleColor"
              :trigger-width="197"
              is-custom
              :predefine="state.predefineColors"
              @change="themeChange('titleColor')"
            />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item class="form-item" label="钻取层级展示颜色">
            <el-color-picker
              v-model="filterStyle.labelColor"
              :trigger-width="197"
              is-custom
              :predefine="state.predefineColors"
              @change="themeChange('labelColor')"
            />
          </el-form-item>
        </el-col>
      </div>
    </el-form>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, computed } from 'vue'
import { COLOR_PANEL } from '@/views/chart/components/editor/util/chart'
import { dvMainStoreWithOut } from '@/store/modules/data-visualization/dvMain'
import { useI18n } from '@/hooks/web/useI18n'
import { cloneDeep } from 'lodash-es'
import { adaptCurThemeFilterStyleAll } from '@/utils/canvasStyle'
import { snapshotStoreWithOut } from '@/store/modules/data-visualization/snapshot'
import eventBus from '@/utils/eventBus'
import { ElIcon } from 'element-plus-secondary'
const { t } = useI18n()
const dvMainStore = dvMainStoreWithOut()
const snapshotStore = snapshotStoreWithOut()
const filterStyle = computed<any>(() => {
  return dvMainStore.canvasStyleData.component.filterStyle
})

const state = reactive({
  fontSize: [],
  isSetting: false,
  predefineColors: COLOR_PANEL
})

const initForm = () => {
  // do
}
const themeChange = styleKey => {
  dvMainStore.canvasStyleData.component.filterStyle = cloneDeep(filterStyle.value)
  adaptCurThemeFilterStyleAll(styleKey)
  snapshotStore.recordSnapshotCache('filterStyleSimpleSelector-themeChange')
}

const handleHorizontalChange = (type, horizontal = 'titleLayout') => {
  filterStyle.value[horizontal] = type
  themeChange(horizontal)
}

onMounted(() => {
  eventBus.on('onThemeColorChange', initForm)
})
</script>

<style scoped lang="less">
.hover-icon {
  &.active {
    color: var(--ed-color-primary) !important;
    background-color: var(--ed-color-primary-1a, rgba(51, 112, 255, 0.1));
  }
  & + & {
    margin-left: 8px;
  }
}
.m-divider {
  border-color: rgba(31, 35, 41, 0.15);
  margin: 0 0 8px;
}
.inner-collapse {
  :deep(.ed-collapse-item__header) {
    background-color: transparent !important;
  }
  :deep(.ed-collapse-item__header) {
    border: none;
  }
  :deep(.ed-collapse-item__wrap) {
    border: none;
  }
}
.ed-form-item {
  margin-bottom: 16px;

  :deep(.ed-form-item__label) {
    color: #646a73;
    font-size: 12px;
    font-weight: 400;
    line-height: 20px;
  }
}
</style>
