<script setup lang="ts">
import { PropType, toRefs } from 'vue'
import { BASE_VIEW_CONFIG } from '@/views/chart/components/editor/util/chart'
import { snapshotStoreWithOut } from '@/store/modules/data-visualization/snapshot'
import Threshold from '@/views/chart/components/editor/editor-senior/components/Threshold.vue'
import { CollapseSwitchItem } from '@/components/collapse-switch-item'
import { useI18n } from '@/hooks/web/useI18n'
const snapshotStore = snapshotStoreWithOut()
const { t } = useI18n()

const props = defineProps({
  themes: {
    type: String as PropType<EditorTheme>,
    default: 'dark'
  },
  view: {
    type: Object as PropType<ChartObj>,
    required: false,
    default() {
      return { ...BASE_VIEW_CONFIG }
    }
  }
})
const { view } = toRefs(props)

const onThresholdChange = val => {
  // do
  view.value.senior.threshold = val
  snapshotStore.recordSnapshotCache('calcData', view.value.id)
}
</script>

<template>
  <collapse-switch-item
    :effect="themes"
    :title="t('chart.threshold')"
    :change-model="view.senior.threshold"
    v-model="view.senior.threshold.enable"
    name="threshold"
    @modelChange="onThresholdChange"
  >
    <threshold
      :themes="themes"
      :chart="view"
      :property-inner="['tableThreshold']"
      @onThresholdChange="onThresholdChange"
    />
  </collapse-switch-item>
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
