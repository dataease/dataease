<script setup lang="ts">
import { Plus } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus-secondary'
import { computed, ref, type PropType } from 'vue'
import MaterialPickerButton from '@/components/material/MaterialPickerButton.vue'
import PictureAssetCard from '@/custom-component/common/PictureAssetCard.vue'
import ImgViewDialog from '@/custom-component/ImgViewDialog.vue'
import { beforeUploadCheck, uploadFileResult } from '@/api/staticResource'
import { useEmitt } from '@/hooks/web/useEmitt'
import { useI18n } from '@/hooks/web/useI18n'
import { dvMainStoreWithOut } from '@/store/modules/data-visualization/dvMain'
import { snapshotStoreWithOut } from '@/store/modules/data-visualization/snapshot'
import { parseJson } from '@/views/chart/components/js/util'

interface PictureEntry {
  name: string
  url: string
}

const props = defineProps({
  themes: {
    type: String as PropType<EditorTheme>,
    default: 'dark'
  },
  element: {
    type: Object,
    required: true
  },
  view: {
    type: Object as PropType<ChartObj>,
    required: true
  }
})

const { t } = useI18n()
const { emitter } = useEmitt()
const dvMainStore = dvMainStoreWithOut()
const snapshotStore = snapshotStoreWithOut()
const limit = 10
const files = ref<HTMLInputElement>()
const uploading = ref(false)
const dialogImageUrl = ref('')
const dialogVisible = ref(false)
const materialPicture = computed(() => Boolean(props.element.materialPicture))
const images = computed<PictureEntry[]>(() => props.element.propValue.urlList || [])
const primary = computed(() => (materialPicture.value ? images.value[0] : undefined))
const listedImages = computed(() => (materialPicture.value ? images.value.slice(1) : images.value))
const canAppend = computed(() => images.value.length < limit && !uploading.value)
const threshold = computed(() => parseJson(props.view?.senior)?.threshold)

const recordChange = (componentId: string) => {
  const settings = parseJson(dvMainStore.canvasViewInfo[componentId]?.senior)?.threshold
  if (settings?.enable) {
    emitter.emit('calcData-' + componentId)
    snapshotStore.recordSnapshotCache('calcData', componentId)
  } else {
    snapshotStore.recordSnapshotCache('renderChart', componentId)
  }
}

const updateThresholdImages = (
  componentId: string,
  list: PictureEntry[],
  previousUrl: string,
  replacement = ''
) => {
  if (!previousUrl || list.some(image => image.url === previousUrl)) return
  const settings = parseJson(dvMainStore.canvasViewInfo[componentId]?.senior)?.threshold
  settings?.tableThreshold?.forEach(field => {
    field.conditions?.forEach(condition => {
      if (condition.url === previousUrl) condition.url = replacement
    })
  })
}

const replacePrimary = (entry?: PictureEntry, element = props.element) => {
  const list: PictureEntry[] = (element.propValue.urlList ??= [])
  const previousUrl = list[0]?.url
  if (!entry && list.length <= 1) {
    list.splice(0, 1)
  } else {
    // Keep the first slot for the primary image while retaining conditional images.
    list.splice(0, 1, entry || { name: '', url: '' })
  }
  updateThresholdImages(element.id, list, previousUrl, entry?.url)
  recordChange(element.id)
}

const appendMaterial = (reference: string) => {
  if (!canAppend.value) return
  const { element } = props
  const list: PictureEntry[] = (element.propValue.urlList ??= [])
  if (materialPicture.value && !list.length) list.push({ name: '', url: '' })
  list.push({ name: reference, url: reference })
  recordChange(element.id)
}

const removePicture = (index: number) => {
  const [removed] = images.value.splice(index + (materialPicture.value ? 1 : 0), 1)
  if (!removed) return
  updateThresholdImages(props.element.id, images.value, removed.url)
  recordChange(props.element.id)
}

const previewPicture = (url: string) => {
  dialogImageUrl.value = url
  dialogVisible.value = true
}

const pickFiles = (replace = false) => {
  if (!files.value || uploading.value) return
  files.value.multiple = !replace
  files.value.click()
}

const upload = async (event: Event) => {
  const input = event.target as HTMLInputElement
  const selected = Array.from(input.files || [])
  const replace = !input.multiple
  input.value = ''
  if (uploading.value || !selected.length) return

  const { element } = props
  const picture = element.propValue
  const list: PictureEntry[] = (picture.urlList ??= [])
  const reservePrimary = !replace && materialPicture.value && !list.length
  const capacity = replace ? 1 : limit - list.length - Number(reservePrimary)
  if (selected.length > capacity) {
    ElMessage.warning(t('visualization.pic_group_limit', { limit }))
    return
  }

  uploading.value = true
  let changed = false
  try {
    for (const file of selected.filter(beforeUploadCheck)) {
      await uploadFileResult(file, (url, error) => {
        if (error || !url || element.propValue !== picture) return
        const entry = { name: file.name, url }
        if (replace) {
          replacePrimary(entry, element)
        } else if (!replace && list.length < limit) {
          if (element.materialPicture && !list.length) list.push({ name: '', url: '' })
          list.push(entry)
          changed = true
        }
      })
    }
  } finally {
    uploading.value = false
    if (changed) recordChange(element.id)
  }
}

const adaptation = computed({
  get: () => props.element.style.adaptation,
  set: value => {
    const { style, id } = props.element
    style.adaptation = value
    snapshotStore.recordSnapshotCache('renderChart', id)
  }
})
</script>

<template>
  <el-collapse-item :effect="themes" name="picture">
    <template #title>
      <div class="picture-section-title">
        <span>{{ t(materialPicture ? 'visualization.picture' : 'visualization.pic_group') }}</span>
        <span v-if="!materialPicture" class="picture-count">{{ images.length }} / {{ limit }}</span>
      </div>
    </template>
    <input
      ref="files"
      type="file"
      accept=".jpeg,.jpg,.png,.gif,.svg"
      :multiple="!materialPicture"
      hidden
      @change="upload"
    />

    <template v-if="materialPicture">
      <div class="picture-tools">
        <MaterialPickerButton
          :themes="themes"
          :disabled="uploading"
          @select="reference => replacePrimary({ name: reference, url: reference })"
        />
        <el-button
          v-if="primary?.url"
          type="primary"
          text
          :disabled="uploading"
          @click="pickFiles(true)"
        >
          {{ t('visualization.re_upload') }}
        </el-button>
      </div>
      <PictureAssetCard
        v-if="primary?.url"
        :url="primary.url"
        :name="primary.name"
        :disabled="uploading"
        @preview="previewPicture(primary.url)"
        @remove="replacePrimary()"
      />
      <el-button
        v-else
        class="picture-upload"
        :aria-label="t('visualization.pic_upload_tips')"
        :disabled="uploading"
        :icon="Plus"
        @click="pickFiles(true)"
      />
    </template>

    <template v-if="!materialPicture || threshold?.enable || listedImages.length">
      <div v-if="materialPicture" class="picture-condition-label">
        {{ t('visualization.pic_condition_images') }}
      </div>
      <div class="picture-tools">
        <MaterialPickerButton :themes="themes" :disabled="!canAppend" @select="appendMaterial" />
      </div>
      <div class="picture-grid">
        <PictureAssetCard
          v-for="(image, index) in listedImages"
          :key="image.url + '-' + index"
          :url="image.url"
          :name="image.name"
          :order="index + 1"
          :disabled="uploading"
          @preview="previewPicture(image.url)"
          @remove="removePicture(index)"
        />
        <el-button
          v-if="images.length < limit"
          class="picture-upload"
          :aria-label="t('visualization.pic_upload_tips')"
          :disabled="uploading"
          :icon="Plus"
          @click="pickFiles()"
        />
      </div>
      <div class="image-hint">{{ t('visualization.pic_group_upload_hint') }}</div>
    </template>
    <div v-else class="image-hint">{{ t('visualization.pic_upload_tips2') }}</div>
    <img-view-dialog v-model="dialogVisible" :image-url="dialogImageUrl" />

    <el-row class="pic-adaptor">
      <el-form-item
        v-if="element.style.adaptation"
        class="form-item form-item-custom"
        :class="'form-item-' + themes"
        :label="t('visualization.pic_adaptor_type')"
        size="small"
        :effect="themes"
      >
        <el-radio-group v-model="adaptation" size="small" :effect="themes">
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
</template>

<style lang="less" scoped>
.picture-section-title {
  display: flex;
  flex: 1;
  justify-content: space-between;
  padding-right: 8px;
}
.picture-count,
.image-hint {
  color: var(--ed-text-color-secondary);
  font-size: 12px;
  line-height: 20px;
}
.picture-count {
  align-self: center;
}
.image-hint {
  margin-top: 6px;
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
.picture-condition-label {
  margin-top: 12px;
  font-weight: 500;
}
.picture-grid {
  display: grid;
  grid-template-columns: repeat(2, 80px);
  gap: 8px;
}
.picture-upload {
  width: 80px;
  height: 80px;
  margin: 0;
  border-style: dashed;
  background: var(--ed-fill-color-light);
}
.pic-adaptor {
  margin: 8px 0;

  :deep(.ed-form-item__content) {
    margin-top: 8px !important;
  }
}
.form-item-custom {
  .ed-radio {
    margin-right: 2px !important;
  }
}
</style>
