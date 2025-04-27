<template>
  <div style="width: 100%" ref="bgForm">
    <el-form size="small" label-position="top" style="width: 100%; margin-bottom: 16px">
      <el-form-item
        class="form-item no-margin-bottom"
        :class="'form-item-' + themes"
        :label="t('visualization.screen_font_family_select')"
      >
        <el-select
          :effect="themes"
          v-model="canvasStyleData.fontFamily"
          @change="onFontFamilyChange"
        >
          <el-option
            v-for="option in fontFamily"
            :key="option.value"
            :label="option.name"
            :value="option.value"
          />
        </el-select>
      </el-form-item>

      <el-form-item class="form-item no-margin-bottom" :class="'form-item-' + themes">
        <el-checkbox
          size="small"
          :effect="themes"
          v-model="canvasStyleData.popupButtonAvailable"
          @change="onThemeChange"
        >
          <div style="display: flex; line-height: 14px">
            <span style="margin-right: 4px">{{ t('visualization.show_pop_button') }}</span>
            <el-tooltip class="item" :effect="themes" placement="bottom">
              <template #content>
                <div>{{ t('visualization.effective_during_preview') }}</div>
              </template>
              <el-icon class="hint-icon" :class="{ 'hint-icon--dark': themes === 'dark' }">
                <Icon name="icon_info_outlined"><icon_info_outlined class="svg-icon" /></Icon>
              </el-icon>
            </el-tooltip>
          </div>
        </el-checkbox>
      </el-form-item>

      <el-form-item
        v-if="!isDesktopFlag"
        class="form-item no-margin-bottom"
        :class="'form-item-' + themes"
      >
        <el-checkbox
          size="small"
          :effect="themes"
          v-model="canvasStyleData.suspensionButtonAvailable"
          @change="onThemeChange"
        >
          <div style="display: flex; line-height: 14px">
            <span style="margin-right: 4px">{{ t('visualization.show_zoom_button') }}</span>
            <el-tooltip class="item" :effect="themes" placement="bottom">
              <template #content>
                <div>{{ t('visualization.effective_during_link') }}</div>
              </template>
              <el-icon class="hint-icon" :class="{ 'hint-icon--dark': themes === 'dark' }">
                <Icon name="icon_info_outlined"><icon_info_outlined class="svg-icon" /></Icon>
              </el-icon>
            </el-tooltip>
          </div>
        </el-checkbox>
      </el-form-item>

      <el-form-item class="form-item no-margin-bottom" :class="'form-item-' + themes">
        <el-checkbox
          :effect="themes"
          size="small"
          v-model="canvasStyleData.dashboard.showGrid"
          @change="onThemeChange"
          >{{ t('visualization.display_auxiliary_grid') }}</el-checkbox
        >
      </el-form-item>
    </el-form>
  </div>
</template>

<script setup lang="ts">
import icon_info_outlined from '@/assets/svg/icon_info_outlined.svg'
import { snapshotStoreWithOut } from '@/store/modules/data-visualization/snapshot'
import { dvMainStoreWithOut } from '@/store/modules/data-visualization/dvMain'
import { storeToRefs } from 'pinia'
import { ElFormItem, ElIcon } from 'element-plus-secondary'
import Icon from '../icon-custom/src/Icon.vue'
import { useAppearanceStoreWithOut } from '@/store/modules/appearance'
import { CHART_FONT_FAMILY_ORIGIN } from '@/views/chart/components/editor/util/chart'
import { adaptTitleFontFamilyAll } from '@/utils/canvasStyle'
import { useI18n } from '@/hooks/web/useI18n'
import { isDesktop } from '@/utils/ModelUtil'
const snapshotStore = snapshotStoreWithOut()
const { t } = useI18n()
const dvMainStore = dvMainStoreWithOut()
const { canvasStyleData } = storeToRefs(dvMainStore)
const appearanceStore = useAppearanceStoreWithOut()

const isDesktopFlag = isDesktop()

const fontFamily = CHART_FONT_FAMILY_ORIGIN.concat(
  appearanceStore.fontList.map(ele => ({
    name: ele.name,
    value: ele.name
  }))
)
const onFontFamilyChange = () => {
  appearanceStore.setCurrentFont(canvasStyleData.value.fontFamily)
  document.documentElement.style.setProperty(
    '--de-canvas_custom_font',
    `${canvasStyleData.value.fontFamily}`
  )
  adaptTitleFontFamilyAll(canvasStyleData.value.fontFamily)
  snapshotStore.recordSnapshotCache('renderChart')
}
const onThemeChange = () => {
  snapshotStore.recordSnapshotCache('onThemeChange')
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
