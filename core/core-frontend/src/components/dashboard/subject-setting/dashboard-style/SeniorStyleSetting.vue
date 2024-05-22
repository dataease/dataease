<template>
  <div style="width: 100%; padding-bottom: 8px">
    <el-form label-position="top" style="width: 100%">
      <div style="width: 100%; padding: 16px 8px 0">
        <el-col :span="24">
          <el-form-item
            :effect="themes"
            class="form-item"
            :class="'form-item-' + themes"
            label="联动、钻取、跳转的图标颜色"
          >
            <el-color-picker
              :effect="themes"
              v-model="seniorStyleSetting.linkageIconColor"
              :trigger-width="197"
              is-custom
              :predefine="state.predefineColors"
              @change="themeChange"
            />
          </el-form-item>
        </el-col>
        <el-col :span="24">
          <el-form-item
            :effect="themes"
            class="form-item"
            :class="'form-item-' + themes"
            label="钻取层级展示颜色"
          >
            <el-color-picker
              v-model="seniorStyleSetting.drillLayerColor"
              :effect="themes"
              :trigger-width="197"
              is-custom
              :predefine="state.predefineColors"
              @change="themeChange"
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
import { snapshotStoreWithOut } from '@/store/modules/data-visualization/snapshot'
import eventBus from '@/utils/eventBus'
const { t } = useI18n()
const dvMainStore = dvMainStoreWithOut()
const snapshotStore = snapshotStoreWithOut()
const seniorStyleSetting = computed<any>(() => {
  return dvMainStore.canvasStyleData.component.seniorStyleSetting
})

const props = defineProps({
  themes: {
    type: String,
    default: 'light'
  }
})
const state = reactive({
  fontSize: [],
  isSetting: false,
  predefineColors: COLOR_PANEL
})

const initForm = () => {
  // do
}
const themeChange = () => {
  dvMainStore.canvasStyleData.component.seniorStyleSetting = cloneDeep(seniorStyleSetting.value)
  snapshotStore.recordSnapshotCache('seniorStyleSettingSimpleSelector-themeChange')
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
.form-item-dark {
  :deep(.ed-form-item__label) {
    color: #6a6a6a;
    font-size: 12px;
    font-weight: 400;
    line-height: 20px;
  }
}
</style>
