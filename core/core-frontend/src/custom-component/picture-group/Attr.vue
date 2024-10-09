<script setup lang="ts">
import CommonAttr from '@/custom-component/common/CommonAttr.vue'
import { dvMainStoreWithOut } from '@/store/modules/data-visualization/dvMain'

import { storeToRefs } from 'pinia'
import { PropType } from 'vue'
import PictureGroupUploadAttr from '@/custom-component/picture-group/PictureGroupUploadAttr.vue'
import PictureGroupDatasetSelect from '@/custom-component/picture-group/PictureGroupDatasetSelect.vue'
import CarouselSetting from '@/custom-component/common/CarouselSetting.vue'
import PictureGroupThreshold from '@/custom-component/picture-group/PictureGroupThreshold.vue'

const props = defineProps({
  themes: {
    type: String as PropType<EditorTheme>,
    default: 'dark'
  }
})

const dvMainStore = dvMainStoreWithOut()

const { curComponent, canvasViewInfo } = storeToRefs(dvMainStore)
</script>

<template>
  <div class="attr-list de-collapse-style">
    <CommonAttr
      :themes="themes"
      :element="curComponent"
      :background-color-picker-width="197"
      :background-border-select-width="197"
    >
      <template v-slot:dataset>
        <picture-group-dataset-select
          :themes="themes"
          :view="canvasViewInfo[curComponent ? curComponent.id : 'default']"
        >
        </picture-group-dataset-select>
      </template>
      <picture-group-upload-attr
        :themes="themes"
        :element="curComponent"
      ></picture-group-upload-attr>
      <template v-slot:carousel>
        <carousel-setting
          v-if="curComponent?.innerType === 'picture-group'"
          :element="curComponent"
          :themes="themes"
        ></carousel-setting>
      </template>
      <template v-slot:threshold>
        <picture-group-threshold
          :themes="themes"
          :view="canvasViewInfo[curComponent ? curComponent.id : 'default']"
        ></picture-group-threshold>
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

  :deep(.ed-collapse-item__content) {
    padding: 16px 8px 0;
  }
  :deep(.ed-form-item) {
    display: block;
    margin-bottom: 8px;
  }
  :deep(.ed-form-item__label) {
    justify-content: flex-start;
  }
}

.disabled :deep(.el-upload--picture-card) {
  display: none;
}

.avatar-uploader :deep(.ed-upload) {
  width: 80px;
  height: 80px;
  line-height: 90px;
}

.avatar-uploader :deep(.ed-upload-list li) {
  width: 80px !important;
  height: 80px !important;
}

:deep(.ed-upload--picture-card) {
  background: #eff0f1;
  border: 1px dashed #dee0e3;
  border-radius: 4px;

  .ed-icon {
    color: #1f2329;
  }

  &:hover {
    .ed-icon {
      color: var(--ed-color-primary);
    }
  }
}
.img-area {
  height: 80px;
  width: 80px;
  margin-top: 10px;
  overflow: hidden;

  &.img-area_dark {
    :deep(.ed-upload-list__item).is-success {
      border-color: #434343;
    }
    :deep(.ed-upload--picture-card) {
      background: #373737;
      border-color: #434343;
      .ed-icon {
        color: #ebebeb;
      }
      &:hover {
        .ed-icon {
          color: var(--ed-color-primary);
        }
      }
    }
  }

  &.img-area_light {
    :deep(.ed-upload-list__item).is-success {
      border-color: #dee0e3;
    }
  }
}

.image-hint {
  color: #8f959e;
  size: 14px;
  line-height: 22px;
  font-weight: 400;
  margin-top: 2px;
  &.image-hint_dark {
    color: #757575;
  }
}

.re-update-span {
  cursor: pointer;
  color: var(--ed-color-primary);
  size: 14px;
  line-height: 22px;
  font-weight: 400;
}

.pic-adaptor {
  margin: 8px 0 16px 0;
  :deep(.ed-form-item__content) {
    margin-top: 8px !important;
  }
}

.form-item-dark {
  .ed-radio {
    margin-right: 4px !important;
  }
}

.drag-data {
  padding-top: 8px;
  padding-bottom: 16px;

  .tree-btn {
    width: 100%;
    margin-top: 8px;
    background: #fff;
    height: 32px;
    border-radius: 4px;
    border: 1px solid #dcdfe6;
    display: flex;
    color: #cccccc;
    align-items: center;
    cursor: pointer;
    justify-content: center;
    font-size: 12px;
    &.tree-btn--dark {
      background: rgba(235, 235, 235, 0.05);
      border-color: #5f5f5f;
    }

    &.active {
      color: #3370ff;
      border-color: #3370ff;
    }
  }

  &.no-top-border {
    border-top: none !important;
  }
  &.no-top-padding {
    padding-top: 0 !important;
  }
  &:nth-child(n + 2) {
    border-top: 1px solid @side-outline-border-color;
  }
  &:first-child {
    border-top: none !important;
  }
}
</style>
