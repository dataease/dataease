<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { DEFAULT_BASIC_STYLE } from '@/views/chart/components/editor/util/chart'
import { ElMessage, UploadProps } from 'element-plus-secondary'
import { svgStrToUrl } from '@/views/chart/components/js/util'
import { useI18n } from '@/hooks/web/useI18n'
import { cloneDeep, debounce, defaultsDeep } from 'lodash-es'

const props = withDefaults(
  defineProps<{
    chart: ChartObj
    themes?: EditorTheme
    propertyInner?: Array<string>
  }>(),
  {
    themes: 'dark'
  }
)

const { t } = useI18n()
const showProperty = prop => props.propertyInner?.includes(prop)
const emit = defineEmits(['onBasicStyleChange', 'onMiscChange'])
const state = reactive({
  basicStyleForm: JSON.parse(JSON.stringify(DEFAULT_BASIC_STYLE)) as ChartBasicStyle,
  customColor: null,
  colorIndex: 0,
  fieldColumnWidth: {
    fieldId: '',
    width: 0
  },
  fileList: []
})

const changeBasicStyle = (prop?: string, requestData = false) => {
  emit('onBasicStyleChange', { data: state.basicStyleForm, requestData }, prop)
}

const iconUpload = ref()
const acceptedFileType = ['image/svg+xml', 'image/jpeg', 'image/png']

const mapSymbolOptions = [
  { name: t('chart.line_symbol_circle'), value: 'circle' },
  { name: t('chart.line_symbol_rect'), value: 'square' },
  { name: t('chart.line_symbol_triangle'), value: 'triangle' },
  { name: t('chart.map_symbol_pentagon'), value: 'pentagon' },
  { name: t('chart.map_symbol_hexagon'), value: 'hexagon' },
  { name: t('chart.map_symbol_octagon'), value: 'octogon' },
  { name: t('chart.line_symbol_diamond'), value: 'rhombus' },
  { name: t('commons.custom'), value: 'custom' }
]

const onIconChange: UploadProps['onChange'] = async uploadFile => {
  const rawFile = uploadFile.raw
  let validIcon = true
  if (!acceptedFileType.includes(rawFile.type)) {
    ElMessage.error(t('chart.symbolic_error_icon'))
    validIcon = false
  }
  if (rawFile.size / 1024 / 1024 > 1) {
    ElMessage.error(t('chart.symbolic_error_size'))
    validIcon = false
  }
  if (!validIcon) {
    iconUpload.value?.clearFiles()
    state.fileList.splice(0)
    const customIcon = state.basicStyleForm.customIcon
    if (customIcon) {
      let file = ''
      // 图片
      if (customIcon.startsWith('data')) {
        file = customIcon
      } else {
        // svg
        file = svgStrToUrl(customIcon)
      }
      file && (state.fileList[0] = { url: file })
    }
  } else {
    if (rawFile.type === 'image/svg+xml') {
      state.basicStyleForm.customIcon = await rawFile.text()
      changeBasicStyle('customIcon')
    } else {
      const fileReader = new FileReader()
      fileReader.onloadend = () => {
        state.basicStyleForm.customIcon = fileReader.result as string
        changeBasicStyle('customIcon')
      }
      fileReader.readAsDataURL(rawFile)
    }
  }
}

const changeMapSymbol = () => {
  const { mapSymbol, customIcon } = state.basicStyleForm
  if (mapSymbol === 'custom' && customIcon) {
    let file
    if (customIcon.startsWith('data')) {
      file = customIcon
    } else {
      file = svgStrToUrl(state.basicStyleForm.customIcon)
    }
    file && (state.fileList[0] = { url: file })
  }
  changeBasicStyle('mapSymbol')
}

const customSymbolicMapSizeRange = computed(() => {
  let { extBubble } = JSON.parse(JSON.stringify(props.chart))
  return ['symbolic-map'].includes(props.chart.type) && extBubble?.length > 0
})
const mapCustomRangeValidate = prop => {
  const { mapSymbolSizeMax = '0', mapSymbolSizeMin = '1' } = state.basicStyleForm
  let max = parseInt(mapSymbolSizeMax)
  let min = parseInt(mapSymbolSizeMin)
  state.basicStyleForm.mapSymbolSizeMin = Math.max(min, 0)
  state.basicStyleForm.mapSymbolSizeMax = Math.max(max, 1)
  if (max < min) {
    ElMessage.warning(t('chart.symbolic_error_range'))
    return
  }
  changeBasicStyle(prop)
}

const init = () => {
  const basicStyle = cloneDeep(props.chart.customAttr.basicStyle)
  if (
    basicStyle.mapSymbol === 'custom' &&
    state.basicStyleForm.customIcon !== basicStyle.customIcon
  ) {
    let file
    if (basicStyle.customIcon?.startsWith('data')) {
      file = basicStyle.customIcon
    } else {
      file = svgStrToUrl(basicStyle.customIcon)
    }
    file && (state.fileList[0] = { url: file })
  }
  state.basicStyleForm = defaultsDeep(basicStyle, cloneDeep(DEFAULT_BASIC_STYLE)) as ChartBasicStyle
  if (!state.customColor) {
    state.customColor = state.basicStyleForm.colors[0]
    state.colorIndex = 0
  }
}

const debouncedInit = debounce(init, 500)
watch(
  [() => props.chart.customAttr.basicStyle, () => props.chart.xAxis, () => props.chart.yAxis],
  debouncedInit,
  { deep: true }
)
onMounted(() => {
  init()
})
</script>

<template>
  <el-form size="small" style="width: 100%">
    <div class="map-flow-style" v-if="showProperty('symbolicMapStyle')">
      <el-row style="flex: 1">
        <el-col>
          <el-form-item class="form-item" :class="'form-item-' + themes">
            <template #label>
              <span class="data-area-label">
                <span style="margin-right: 4px">{{ t('chart.symbolic_shape') }}</span>
                <el-tooltip class="item" effect="dark" placement="bottom">
                  <template v-if="state.basicStyleForm.mapSymbol === 'custom'" #content>
                    <div>{{ t('chart.symbolic_upload_hint') }}</div>
                  </template>
                  <el-icon class="hint-icon" :class="{ 'hint-icon--dark': themes === 'dark' }">
                    <Icon name="icon_info_outlined"><icon_info_outlined class="svg-icon" /></Icon>
                  </el-icon>
                </el-tooltip>
              </span>
            </template>
            <el-select
              :effect="themes"
              v-model="state.basicStyleForm.mapSymbol"
              @change="changeMapSymbol()"
            >
              <el-option
                v-for="item in mapSymbolOptions"
                :key="item.name"
                :label="item.name"
                :value="item.value"
              />
            </el-select>
          </el-form-item>
        </el-col>
      </el-row>
      <el-row style="flex: 1" v-if="state.basicStyleForm.mapSymbol === 'custom'">
        <el-col>
          <el-form-item class="form-item uploader" :class="'form-item-' + themes">
            <div class="avatar-uploader-container" :class="`img-area_${themes}`">
              <el-upload
                action="#"
                accept=".svg,.png,.jpeg,.jpg"
                class="avatar-uploader"
                list-type="picture-card"
                ref="iconUpload"
                :effect="themes"
                :auto-upload="false"
                :file-list="state.fileList"
                :on-change="onIconChange"
                :limit="1"
              >
                <el-icon><Plus /></el-icon>
              </el-upload>
            </div>
          </el-form-item>
        </el-col>
      </el-row>
      <div class="alpha-setting">
        <label class="alpha-label" :class="{ dark: 'dark' === themes }">
          {{ t('chart.size') }}
        </label>
        <el-row style="flex: 1">
          <el-col>
            <el-form-item class="form-item alpha-slider" :class="'form-item-' + themes">
              <el-slider
                :effect="themes"
                :min="1"
                :max="40"
                v-model="state.basicStyleForm.mapSymbolSize"
                @change="changeBasicStyle('mapSymbolSize')"
                :disabled="customSymbolicMapSizeRange"
              />
            </el-form-item>
          </el-col>
        </el-row>
      </div>
      <div class="alpha-setting">
        <label class="alpha-label" :class="{ dark: 'dark' === themes }">
          {{ t('chart.size') }}{{ t('chart.symbolic_range') }}
        </label>
        <el-row style="flex: 1">
          <el-col :span="11">
            <el-form-item class="form-item alpha-slider" :class="'form-item-' + themes">
              <el-input
                type="number"
                :effect="themes"
                v-model="state.basicStyleForm.mapSymbolSizeMin"
                class="basic-input-number"
                :controls="false"
                @blur="mapCustomRangeValidate('mapSymbolSizeMin')"
                :disabled="!customSymbolicMapSizeRange"
              >
              </el-input>
            </el-form-item>
          </el-col>
          <el-col :span="1.2">
            <span>-</span>
          </el-col>
          <el-col :span="11">
            <el-form-item class="form-item alpha-slider" :class="'form-item-' + themes">
              <el-input
                type="number"
                :effect="themes"
                v-model="state.basicStyleForm.mapSymbolSizeMax"
                class="basic-input-number"
                :controls="false"
                @blur="mapCustomRangeValidate('mapSymbolSizeMax')"
                :disabled="!customSymbolicMapSizeRange"
              >
              </el-input>
            </el-form-item>
          </el-col>
        </el-row>
      </div>
      <div v-if="state.basicStyleForm.mapSymbol !== 'custom'" class="alpha-setting">
        <label class="alpha-label" :class="{ dark: 'dark' === themes }">
          {{ t('chart.not_alpha') }}
        </label>
        <el-row style="flex: 1">
          <el-col>
            <el-form-item class="form-item alpha-slider" :class="'form-item-' + themes">
              <el-slider
                :effect="themes"
                :min="1"
                :max="10"
                v-model="state.basicStyleForm.mapSymbolOpacity"
                @change="changeBasicStyle('mapSymbolOpacity')"
              />
            </el-form-item>
          </el-col>
        </el-row>
      </div>
      <div v-if="state.basicStyleForm.mapSymbol !== 'custom'" class="alpha-setting">
        <label class="alpha-label" :class="{ dark: 'dark' === themes }">
          {{ t('visualization.borderWidth') }}
        </label>
        <el-row style="flex: 1">
          <el-col>
            <el-form-item class="form-item alpha-slider" :class="'form-item-' + themes">
              <el-slider
                :effect="themes"
                :min="1"
                :max="5"
                v-model="state.basicStyleForm.mapSymbolStrokeWidth"
                @change="changeBasicStyle('mapSymbolStrokeWidth')"
              />
            </el-form-item>
          </el-col>
        </el-row>
      </div>
    </div>
  </el-form>
</template>

<style scoped lang="less">
.color-picker-style {
  cursor: pointer;
  z-index: 1003;
}

.alpha-setting {
  display: flex;
  width: 100%;

  .alpha-slider {
    padding: 0 8px;
    :deep(.ed-slider__button-wrapper) {
      --ed-slider-button-wrapper-size: 36px;
      --ed-slider-button-size: 16px;
    }
  }

  .alpha-label {
    padding-right: 8px;
    font-size: 12px;
    font-style: normal;
    font-weight: 400;
    height: 32px;
    line-height: 32px;
    display: inline-flex;
    align-items: flex-start;

    min-width: 56px;

    &.dark {
      color: #a6a6a6;
    }
  }
}
.data-area-label {
  text-align: left;
  position: relative;
  width: 100%;
  display: flex;
  flex-direction: row;
  align-items: center;
}
.avatar-uploader-container {
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

  &.img-area_dark {
    :deep(.ed-upload-list__item).is-ready {
      border-color: #434343;
    }
    :deep(.ed-upload--picture-card) {
      background: #373737;
      border-color: #434343;
      .ed-icon {
        color: #ebebeb;
      }
    }
  }

  &.img-area_light {
    :deep(.ed-upload-list__item).is-ready {
      border-color: #dee0e3;
    }
  }
  :deep(.ed-upload-list__item-preview) {
    display: none !important;
  }
  :deep(.ed-upload-list__item-delete) {
    margin-left: 0 !important;
  }
  :deep(.ed-upload-list__item-status-label) {
    display: none !important;
  }
  :deep(.ed-icon--close-tip) {
    display: none !important;
  }
}
.avatar-uploader {
  width: 90px;
  height: 80px;
  overflow: hidden;
}
.avatar-uploader {
  width: 90px;
  :deep(.ed-upload) {
    width: 80px;
    height: 80px;
    line-height: 90px;
  }

  :deep(.ed-upload-list li) {
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
}
.uploader {
  :deep(.ed-form-item__content) {
    justify-content: center;
  }
}
</style>
