<script setup lang="ts">
import CommonAttr from '@/custom-component/common/CommonAttr.vue'
import { dvMainStoreWithOut } from '@/store/modules/data-visualization/dvMain'

import { storeToRefs } from 'pinia'
import { PropType } from 'vue'
import PictureGroupUploadAttr from '@/custom-component/picture-group/PictureGroupUploadAttr.vue'
import PictureGroupDatasetSelect from '@/custom-component/picture-group/PictureGroupDatasetSelect.vue'
import CarouselSetting from '@/custom-component/common/CarouselSetting.vue'
import PictureGroupThreshold from '@/custom-component/picture-group/PictureGroupThreshold.vue'
defineProps({
  themes: {
    type: String as PropType<EditorTheme>,
    default: 'dark'
  }
})

const dvMainStore = dvMainStoreWithOut()

const { curComponent, canvasViewInfo, mobileInPc, batchOptStatus } = storeToRefs(dvMainStore)
</script>

<template>
  <div class="attr-list de-collapse-style">
    <CommonAttr
      :themes="themes"
      :element="curComponent"
      :background-color-picker-width="197"
      :background-border-select-width="197"
    >
      <picture-group-upload-attr
        :themes="themes"
        :element="curComponent"
        v-if="!mobileInPc && !batchOptStatus"
      ></picture-group-upload-attr>
      <template v-slot:carousel v-if="!mobileInPc">
        <carousel-setting
          v-if="curComponent?.innerType === 'picture-group'"
          :element="curComponent"
          :themes="themes"
        ></carousel-setting>
      </template>
      <template v-slot:threshold v-if="!mobileInPc">
        <picture-group-threshold
          :themes="themes"
          :element="curComponent"
          :view="canvasViewInfo[curComponent ? curComponent.id : 'default']"
        >
          <template v-slot:dataset>
            <picture-group-dataset-select
              :themes="themes"
              :view="canvasViewInfo[curComponent ? curComponent.id : 'default']"
            >
            </picture-group-dataset-select>
          </template>
        </picture-group-threshold>
      </template>
    </CommonAttr>
  </div>
</template>

<style lang="less" scoped>
.de-collapse-style {
  :deep(.ed-collapse-item__header) {
    height: 36px !important;
    line-height: 36px !important;
    font-size: 12px !important;
    padding: 0 !important;
    font-weight: 500 !important;

    .ed-collapse-item__arrow {
      margin: 0 6px 0 8px;
    }
  }
  :deep(.ed-form-item) {
    display: block;
    margin-bottom: 8px;
  }
  :deep(.ed-form-item__label) {
    justify-content: flex-start;
  }
}
</style>
