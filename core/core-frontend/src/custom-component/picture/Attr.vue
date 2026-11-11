<script setup lang="ts">
import { Plus } from '@element-plus/icons-vue'
import MaterialPickerButton from '@/components/material/MaterialPickerButton.vue'
import CommonAttr from '@/custom-component/common/CommonAttr.vue'
import PictureAssetCard from '@/custom-component/common/PictureAssetCard.vue'
import { CollapseSwitchItem } from '@/components/collapse-switch-item'
import { dvMainStoreWithOut } from '@/store/modules/data-visualization/dvMain'
import { snapshotStoreWithOut } from '@/store/modules/data-visualization/snapshot'
import { storeToRefs } from 'pinia'
import { computed, onBeforeUnmount, onMounted, ref, type PropType } from 'vue'
import { beforeUploadCheck, uploadFileResult } from '@/api/staticResource'
import eventBus from '@/utils/eventBus'
import ImgViewDialog from '@/custom-component/ImgViewDialog.vue'
import { useI18n } from '@/hooks/web/useI18n'
const { t } = useI18n()

const props = defineProps({
  themes: {
    type: String as PropType<EditorTheme>,
    default: 'dark'
  }
})

const dvMainStore = dvMainStoreWithOut()
const snapshotStore = snapshotStoreWithOut()

const { curComponent, mobileInPc } = storeToRefs(dvMainStore)

const files = ref<HTMLInputElement>()
const dialogVisible = ref(false)
const uploading = ref(false)
const pictureUrl = computed(() => curComponent.value?.propValue?.url)
const materialPicture = computed(() =>
  /^material:\/\/[1-9][0-9]{0,18}$/.test(pictureUrl.value || '')
)

const enableThreshold = (enabled: boolean) => {
  if (!enabled || !materialPicture.value) return
  const component = curComponent.value
  dvMainStore.enableMaterialPictureThreshold(component)
  snapshotStore.recordSnapshotCache('renderChart', component.id)
}

const setPicture = (component, url: string | null) => {
  component.propValue.url = url
  snapshotStore.recordSnapshotCache('renderChart', component.id)
}

const applyMaterial = (reference: string) => {
  setPicture(curComponent.value, reference)
}

const removePicture = () => {
  dialogVisible.value = false
  setPicture(curComponent.value, null)
}

const upload = async (event: Event) => {
  const input = event.target as HTMLInputElement
  const file = input.files?.[0]
  input.value = ''
  if (uploading.value || !file || !beforeUploadCheck(file)) return

  const component = curComponent.value
  const picture = component.propValue
  uploading.value = true
  try {
    await uploadFileResult(file, (url, error) => {
      if (!error && url && component.propValue === picture) setPicture(component, url)
    })
  } finally {
    uploading.value = false
  }
}

const onStyleChange = () => {
  snapshotStore.recordSnapshotCache('pic-onStyleChange')
}

const goFile = () => {
  if (!uploading.value) files.value?.click()
}
const onCanvasUpload = (componentId: string) => {
  if (curComponent.value?.id === componentId && !mobileInPc.value) goFile()
}

onMounted(() => eventBus.on('uploadPicture', onCanvasUpload))
onBeforeUnmount(() => eventBus.off('uploadPicture', onCanvasUpload))
</script>

<template>
  <div class="attr-list de-collapse-style">
    <input ref="files" type="file" accept=".jpeg,.jpg,.png,.gif,.svg" hidden @change="upload" />
    <CommonAttr
      :themes="themes"
      :element="curComponent"
      :background-color-picker-width="197"
      :background-border-select-width="197"
    >
      <el-collapse-item
        :effect="themes"
        :title="t('visualization.picture')"
        name="picture"
        v-show="!mobileInPc"
      >
        <div class="picture-tools">
          <MaterialPickerButton :themes="themes" :disabled="uploading" @select="applyMaterial" />
          <el-button v-if="pictureUrl" text type="primary" :disabled="uploading" @click="goFile">
            {{ t('visualization.re_upload') }}
          </el-button>
        </div>
        <PictureAssetCard
          v-if="pictureUrl"
          :url="pictureUrl"
          :disabled="uploading"
          @preview="dialogVisible = true"
          @remove="removePicture"
        />
        <el-button
          v-else
          class="picture-upload"
          :aria-label="t('visualization.pic_upload_tips')"
          :disabled="uploading"
          :icon="Plus"
          @click="goFile"
        />
        <img-view-dialog v-model="dialogVisible" :image-url="pictureUrl" />
        <div class="image-hint">{{ t('visualization.pic_upload_tips2') }}</div>
        <el-row class="pic-adaptor">
          <el-form-item
            v-if="curComponent.style.adaptation"
            class="form-item"
            :label="t('visualization.pic_adaptor_type')"
            size="small"
            :effect="themes"
            :class="'form-item-' + themes"
          >
            <el-radio-group
              size="small"
              v-model="curComponent.style.adaptation"
              @change="onStyleChange"
              :effect="themes"
            >
              <el-radio value="adaptation" :effect="themes">{{
                t('visualization.pic_adaptation')
              }}</el-radio>
              <el-radio value="original" :effect="themes">{{
                t('visualization.pic_original')
              }}</el-radio>
              <el-radio value="equiratio" :effect="themes">{{
                t('visualization.pic_equiratio')
              }}</el-radio>
            </el-radio-group>
          </el-form-item>
        </el-row>
      </el-collapse-item>
      <template v-if="materialPicture && !mobileInPc" #threshold>
        <collapse-switch-item
          :title="t('chart.threshold')"
          :themes="themes"
          :model-value="false"
          name="threshold"
          @update:model-value="enableThreshold"
        />
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

.picture-tools {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;

  :deep(.ed-button) {
    margin-left: 0;
    padding: 0 4px;
  }

  &:empty {
    display: none;
  }
}
.picture-upload {
  width: 80px;
  height: 80px;
  border-style: dashed;
  background: var(--ed-fill-color-light);
}
.image-hint {
  margin-top: 6px;
  color: var(--ed-text-color-secondary);
  font-size: 12px;
  line-height: 20px;
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
    border-radius: 6px;
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
      color: var(--ed-color-primary, #3370ff);
      border-color: var(--ed-color-primary, #3370ff);
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
