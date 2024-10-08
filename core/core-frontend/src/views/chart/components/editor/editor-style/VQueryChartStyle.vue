<script lang="tsx" setup>
import { ElMessage } from 'element-plus-secondary'
import icon_bold_outlined from '@/assets/svg/icon_bold_outlined.svg'
import { beforeUploadCheck, uploadFileResult } from '@/api/staticResource'
import ImgViewDialog from '@/custom-component/ImgViewDialog.vue'
import icon_italic_outlined from '@/assets/svg/icon_italic_outlined.svg'
import { snapshotStoreWithOut } from '@/store/modules/data-visualization/snapshot'
import icon_info_outlined from '@/assets/svg/icon_info_outlined.svg'
import { useI18n } from '@/hooks/web/useI18n'
import { PropType, toRefs, computed, reactive, watch, ref, onMounted } from 'vue'
import { imgUrlTrans } from '@/utils/imgUtils'
import { COLOR_PANEL } from '@/views/chart/components/editor/util/chart'
import CollapseSwitchItem from '@/components/collapse-switch-item/src/CollapseSwitchItem.vue'
import { cloneDeep } from 'lodash-es'
const { t } = useI18n()
const styleActiveNames = ref(['basicStyle'])

const props = defineProps({
  element: {
    type: Object,
    required: true
  },
  chart: {
    type: Object,
    required: true
  },
  commonBackgroundPop: {
    type: Object,
    required: true
  },
  themes: {
    type: String as PropType<EditorTheme>,
    default: 'dark'
  }
})
const toolTip = computed(() => {
  return props.themes === 'dark' ? 'ndark' : 'dark'
})
const predefineColors = COLOR_PANEL
const fontSizeList = []
for (let i = 10; i <= 60; i = i + 2) {
  fontSizeList.push({
    name: i + '',
    value: i + ''
  })
}
const snapshotStore = snapshotStoreWithOut()
const files = ref(null)

const state = reactive({
  commonBackground: {},
  fileList: [],
  dialogImageUrl: '',
  dialogVisible: false
})

watch(
  () => props.commonBackgroundPop,
  () => {
    init()
  }
)

watch(
  () => props.element.id,
  () => {
    initParams()
  }
)

const currentPlaceholder = ref()
const currentSearch = ref({
  placeholder: ''
})

const handleCurrentPlaceholder = val => {
  const obj = props.element.propValue.find(ele => {
    return ele.id === val
  }) || {
    placeholder: ''
  }
  if (obj.placeholder === undefined) {
    obj.placeholder = ''
  }
  currentSearch.value = obj
}

const init = () => {
  state.commonBackground = cloneDeep(props.commonBackgroundPop)
  if (state.commonBackground['outerImage']) {
    state.fileList.push({ url: imgUrlTrans(state.commonBackground['outerImage']) })
  } else {
    state.fileList = []
  }
}
const handleRemove = () => {
  state.commonBackground['outerImage'] = null
  state.fileList = []
  onBackgroundChange()
}
const handlePictureCardPreview = file => {
  state.dialogImageUrl = file.url
  state.dialogVisible = true
}
const upload = file => {
  return uploadFileResult(file.file, fileUrl => {
    state.commonBackground['outerImage'] = fileUrl
    state.fileList = [{ url: imgUrlTrans(state.commonBackground['outerImage']) }]
    onBackgroundChange()
  })
}
const goFile = () => {
  files.value.click()
}

const onBackgroundChange = () => {
  snapshotStore.recordSnapshotCache()
  commonBackgroundPop.value.outerImage = state.commonBackground['outerImage']
}
onMounted(() => {
  init()
})

const reUpload = e => {
  const file = e.target.files[0]
  if (file.size > 15000000) {
    ElMessage.success('图片大小不符合')
    return
  }
  uploadFileResult(file, fileUrl => {
    state.commonBackground['outerImage'] = fileUrl
    state.fileList = [{ url: imgUrlTrans(state.commonBackground['outerImage']) }]
    onBackgroundChange()
  })
}

const checkBold = type => {
  if (!chart.value.customStyle.component.labelShow) return
  chart.value.customStyle.component[type] = chart.value.customStyle.component[type] ? '' : 'bold'
}

const handleCurrentPlaceholderChange = () => {
  snapshotStore.recordSnapshotCache()
}

const checkItalic = type => {
  if (!chart.value.customStyle.component.labelShow) return
  chart.value.customStyle.component[type] = chart.value.customStyle.component[type] ? '' : 'italic'
}
const { chart, commonBackgroundPop } = toRefs(props)
const initParams = () => {
  if (!chart.value.customStyle.component.hasOwnProperty('labelShow')) {
    chart.value.customStyle.component = {
      ...chart.value.customStyle.component,
      labelShow: true,
      fontWeight: '',
      fontStyle: '',
      fontSize: '14',
      fontSizeBtn: '14',
      fontWeightBtn: '',
      fontStyleBtn: '',
      queryConditionWidth: 227,
      nameboxSpacing: 8,
      queryConditionSpacing: 16,
      labelColorBtn: '#ffffff',
      btnColor: '#3370ff'
    }
  }

  if (!chart.value.customStyle.component.hasOwnProperty('placeholderShow')) {
    chart.value.customStyle.component = {
      ...chart.value.customStyle.component,
      placeholderShow: true,
      placeholderSize: 14
    }
  }

  if (props.element.propValue.length) {
    currentPlaceholder.value = props.element.propValue[0].id
    handleCurrentPlaceholder(props.element.propValue[0].id)
  }
}
initParams()
</script>

<template>
  <div class="attr-style">
    <input
      id="input"
      ref="files"
      type="file"
      accept=".jpeg,.jpg,.png,.gif,.svg"
      hidden
      @click="
        e => {
          e.target.value = ''
        }
      "
      @change="reUpload"
    />
    <el-row class="de-collapse-style">
      <el-collapse v-model="styleActiveNames" class="style-collapse">
        <el-collapse-item :effect="themes" name="basicStyle" :title="t('chart.basic_style')">
          <el-form label-position="top">
            <el-form-item class="form-item margin-bottom-8" :class="'form-item-' + themes">
              <el-checkbox
                :effect="themes"
                size="small"
                v-model="chart.customStyle.component.titleShow"
              >
                {{ t('chart.show') + t('chart.title') }}
              </el-checkbox>
            </el-form-item>
            <el-form-item
              class="form-item"
              style="padding-left: 20px"
              :class="'form-item-' + themes"
            >
              <el-input
                :effect="themes"
                :disabled="!chart.customStyle.component.titleShow"
                v-model.lazy="chart.customStyle.component.title"
              />
            </el-form-item>
            <el-form-item
              label="标题颜色"
              class="form-item"
              style="padding-left: 20px"
              :class="'form-item-' + themes"
            >
              <el-color-picker
                :effect="themes"
                v-model="chart.customStyle.component.titleColor"
                :trigger-width="204"
                :disabled="!chart.customStyle.component.titleShow"
                is-custom
                :predefine="COLOR_PANEL"
              />
            </el-form-item>
            <el-form-item class="form-item margin-bottom-8" :class="'form-item-' + themes">
              <el-checkbox
                :effect="themes"
                size="small"
                v-model="commonBackgroundPop.backgroundColorSelect"
              >
                自定义组件背景
              </el-checkbox>
            </el-form-item>
            <el-form-item
              style="padding-left: 20px"
              class="form-item margin-bottom-8"
              :class="'form-item-' + themes"
            >
              <el-radio-group
                :disabled="!commonBackgroundPop.backgroundColorSelect"
                :effect="themes"
                v-model="commonBackgroundPop.backgroundType"
              >
                <el-radio
                  key="innerImage"
                  v-if="commonBackgroundPop.backgroundType === 'innerImage'"
                  label="innerImage"
                  :effect="themes"
                >
                  背景颜色
                </el-radio>
                <el-radio key="color" v-else label="color" :effect="themes"> 背景颜色 </el-radio>
                <el-radio label="outerImage" :effect="themes"> 背景图片 </el-radio>
              </el-radio-group>
            </el-form-item>
            <el-form-item
              v-if="commonBackgroundPop.backgroundType === 'outerImage'"
              style="padding-left: 20px"
              class="form-item margin-bottom-8"
              :class="'form-item-' + themes"
            >
              <div
                class="indented-item"
                :class="{
                  disabled: !commonBackgroundPop.backgroundColorSelect
                }"
              >
                <div class="avatar-uploader-container" :class="`img-area_${themes}`">
                  <el-upload
                    action=""
                    :effect="themes"
                    accept=".jpeg,.jpg,.png,.gif,.svg"
                    class="avatar-uploader"
                    list-type="picture-card"
                    :on-preview="handlePictureCardPreview"
                    :on-remove="handleRemove"
                    :before-upload="beforeUploadCheck"
                    :http-request="upload"
                    :file-list="state.fileList"
                    :disabled="!commonBackgroundPop.backgroundColorSelect"
                  >
                    <el-icon><Plus /></el-icon>
                  </el-upload>
                  <el-row>
                    <span
                      style="margin-top: 2px"
                      v-if="!state.commonBackground['outerImage']"
                      class="image-hint"
                      :class="`image-hint_${themes}`"
                    >
                      支持JPG、PNG、GIF、SVG
                    </span>

                    <el-button
                      size="small"
                      style="margin: 8px 0 0 -4px"
                      v-if="state.commonBackground['outerImage']"
                      text
                      :disabled="!commonBackgroundPop.backgroundColorSelect"
                      @click="goFile"
                    >
                      重新上传
                    </el-button>
                  </el-row>
                </div>
                <img-view-dialog v-model="state.dialogVisible" :image-url="state.dialogImageUrl" />
              </div>
            </el-form-item>
            <el-form-item
              v-else
              class="form-item"
              style="padding-left: 20px"
              :class="'form-item-' + themes"
            >
              <el-color-picker
                :effect="themes"
                :trigger-width="108"
                is-custom
                v-model="commonBackgroundPop.backgroundColor"
                :disabled="!commonBackgroundPop.backgroundColorSelect"
                :predefine="predefineColors"
              />
            </el-form-item>
          </el-form>
        </el-collapse-item>
        <el-collapse-item :effect="themes" name="addition" title="查询条件">
          <el-form label-position="top">
            <el-form-item class="form-item margin-bottom-8" :class="'form-item-' + themes">
              <el-checkbox
                :effect="themes"
                size="small"
                v-model="chart.customStyle.component.borderShow"
              >
                {{ t('visualization.board') }}
              </el-checkbox>
            </el-form-item>
            <el-form-item
              class="form-item"
              style="padding-left: 20px"
              :class="'form-item-' + themes"
            >
              <el-color-picker
                :effect="themes"
                :trigger-width="108"
                is-custom
                v-model="chart.customStyle.component.borderColor"
                :disabled="!chart.customStyle.component.borderShow"
                :predefine="predefineColors"
              />
            </el-form-item>
            <el-form-item class="form-item margin-bottom-8" :class="'form-item-' + themes">
              <el-checkbox
                :effect="themes"
                size="small"
                v-model="chart.customStyle.component.placeholderShow"
              >
                提示词
              </el-checkbox>
            </el-form-item>
            <el-form-item
              label="文本"
              class="form-item"
              style="padding-left: 20px"
              :class="'form-item-' + themes"
            >
              <div style="display: flex; align-items: center; width: 100%">
                <el-color-picker
                  :effect="themes"
                  :trigger-width="56"
                  is-custom
                  v-model="chart.customStyle.component.text"
                  :disabled="!chart.customStyle.component.placeholderShow"
                  :predefine="predefineColors"
                />
                <el-input-number
                  v-model="chart.customStyle.component.placeholderSize"
                  :min="10"
                  :max="20"
                  style="margin-left: 8px"
                  step-strictly
                  :effect="themes"
                  controls-position="right"
                />
              </div>
              <div style="display: flex; align-items: center; width: 100%; margin-top: 8px">
                <el-select
                  v-model="currentPlaceholder"
                  @change="handleCurrentPlaceholder"
                  style="width: 100%"
                >
                  <el-option
                    v-for="item in element.propValue"
                    :key="item.id"
                    :label="item.name"
                    :value="item.id"
                  />
                </el-select>
              </div>
            </el-form-item>
            <el-form-item
              label="提示词"
              class="form-item"
              style="padding-left: 20px"
              :class="'form-item-' + themes"
            >
              <el-input
                :effect="themes"
                @change="handleCurrentPlaceholderChange"
                :disabled="!chart.customStyle.component.placeholderShow || !currentPlaceholder"
                v-model.lazy="currentSearch.placeholder"
              />
            </el-form-item>
            <el-form-item class="form-item margin-bottom-8" :class="'form-item-' + themes">
              <el-checkbox
                :effect="themes"
                size="small"
                v-model="chart.customStyle.component.bgColorShow"
              >
                自定义查询条件背景
              </el-checkbox>
            </el-form-item>
            <el-form-item
              class="form-item"
              style="padding-left: 20px"
              :class="'form-item-' + themes"
            >
              <el-color-picker
                :effect="themes"
                :trigger-width="108"
                is-custom
                v-model="chart.customStyle.component.bgColor"
                :disabled="!chart.customStyle.component.bgColorShow"
                :predefine="predefineColors"
              />
            </el-form-item>
            <el-form-item
              :effect="themes"
              class="form-item"
              label="查询条件宽度"
              :class="'form-item-' + themes"
            >
              <el-input-number
                v-model="chart.customStyle.component.queryConditionWidth"
                :min="0"
                :effect="themes"
                controls-position="right"
              />
            </el-form-item>
            <el-form-item
              :effect="themes"
              class="form-item"
              label="查询条件间距"
              :class="'form-item-' + themes"
            >
              <el-input-number
                v-model="chart.customStyle.component.queryConditionSpacing"
                :min="0"
                :effect="themes"
                controls-position="right"
              />
            </el-form-item>
          </el-form>
        </el-collapse-item>
        <collapse-switch-item
          :themes="themes"
          v-model="chart.customStyle.component.labelShow"
          name="legend"
          title="查询条件名称"
        >
          <el-form
            :class="!chart.customStyle.component.labelShow && 'is-disabled'"
            :disabled="!chart.customStyle.component.labelShow"
            label-position="top"
          >
            <el-form-item
              :effect="themes"
              class="form-item"
              :label="t('visualization.position_adjust')"
              :class="'form-item-' + themes"
            >
              <el-radio-group :effect="themes" v-model="chart.customStyle.component.layout">
                <el-radio label="vertical" :effect="themes"> 上侧 </el-radio>
                <el-radio label="horizontal" :effect="themes"> 左侧 </el-radio>
              </el-radio-group>
            </el-form-item>
            <el-form-item
              :label="t('chart.textColor')"
              class="form-item margin-bottom-8"
              :class="'form-item-' + themes"
            >
              <el-color-picker
                :effect="themes"
                is-custom
                v-model="chart.customStyle.component.labelColor"
                :predefine="predefineColors"
              /><el-tooltip content="字号" :effect="toolTip" placement="top">
                <el-select
                  style="width: 80px; margin: 0 8px"
                  :effect="themes"
                  v-model="chart.customStyle.component.fontSize"
                  :placeholder="t('chart.text_fontsize')"
                  size="small"
                >
                  <el-option
                    v-for="option in fontSizeList"
                    :key="option.value"
                    :label="option.name"
                    :value="option.value"
                  />
                </el-select>
              </el-tooltip>
              <el-tooltip :effect="toolTip" placement="bottom">
                <template #content>
                  {{ t('chart.bolder') }}
                </template>
                <div
                  class="icon-btn"
                  :class="{
                    dark: themes === 'dark',
                    active: chart.customStyle.component.fontWeight === 'bold'
                  }"
                  style="margin-right: 8px"
                  @click="checkBold('fontWeight')"
                >
                  <el-icon>
                    <Icon name="icon_bold_outlined"><icon_bold_outlined class="svg-icon" /></Icon>
                  </el-icon>
                </div>
              </el-tooltip>

              <el-tooltip :effect="toolTip" placement="bottom">
                <template #content>
                  {{ t('chart.italic') }}
                </template>
                <div
                  class="icon-btn"
                  :class="{
                    dark: themes === 'dark',
                    active: chart.customStyle.component.fontStyle === 'italic'
                  }"
                  @click="checkItalic('fontStyle')"
                >
                  <el-icon>
                    <Icon name="icon_italic_outlined"
                      ><icon_italic_outlined class="svg-icon"
                    /></Icon>
                  </el-icon>
                </div>
              </el-tooltip>
            </el-form-item>
            <el-form-item
              :effect="themes"
              class="form-item"
              label="名称与选框间距"
              :class="'form-item-' + themes"
            >
              <el-input-number
                v-model="chart.customStyle.component.nameboxSpacing"
                :min="0"
                :max="50"
                :effect="themes"
                controls-position="right"
              />
            </el-form-item>
          </el-form>
        </collapse-switch-item>
        <el-collapse-item :effect="themes" name="button" :title="t('commons.button')">
          <el-form label-position="top">
            <el-form-item
              :effect="themes"
              class="form-item"
              label="展示按钮"
              :class="'form-item-' + themes"
            >
              <el-checkbox-group :effect="themes" v-model="chart.customStyle.component.btnList">
                <el-checkbox class="checkbox-with_icon" :effect="themes" size="small" label="sure">
                  {{ t('commons.adv_search.search') }}
                  <el-tooltip
                    :effect="toolTip"
                    content="如果展示查询按钮，需要点击该按钮后才能触发图表查询；如果不展示查询按钮，选择完查询条件后立即触发图表查询"
                    placement="top"
                  >
                    <el-icon class="hint-icon" :class="{ 'hint-icon--dark': themes === 'dark' }">
                      <Icon name="icon_info_outlined"><icon_info_outlined class="svg-icon" /></Icon>
                    </el-icon>
                  </el-tooltip>
                </el-checkbox>

                <el-checkbox :effect="themes" size="small" label="clear">
                  {{ t('commons.clear') }}
                </el-checkbox>
                <el-checkbox :effect="themes" size="small" label="reset">
                  {{ t('commons.adv_search.reset') }}
                </el-checkbox>
              </el-checkbox-group>
            </el-form-item>
            <el-form-item class="form-item" label="按钮颜色" :class="'form-item-' + themes">
              <el-color-picker
                :effect="themes"
                :trigger-width="108"
                is-custom
                v-model="chart.customStyle.component.btnColor"
                :predefine="predefineColors"
              />
            </el-form-item>
            <el-form-item
              label="按钮文字"
              class="form-item margin-bottom-8"
              :class="'form-item-' + themes"
            >
              <el-color-picker
                :effect="themes"
                is-custom
                v-model="chart.customStyle.component.labelColorBtn"
                :predefine="predefineColors"
              /><el-tooltip content="字号" :effect="toolTip" placement="top">
                <el-select
                  style="width: 80px; margin: 0 8px"
                  :effect="themes"
                  v-model="chart.customStyle.component.fontSizeBtn"
                  :placeholder="t('chart.text_fontsize')"
                  size="small"
                >
                  <el-option
                    v-for="option in fontSizeList"
                    :key="option.value"
                    :label="option.name"
                    :value="option.value"
                  />
                </el-select>
              </el-tooltip>
              <el-tooltip :effect="toolTip" placement="bottom">
                <template #content>
                  {{ t('chart.bolder') }}
                </template>
                <div
                  class="icon-btn"
                  :class="{
                    dark: themes === 'dark',
                    active: chart.customStyle.component.fontWeightBtn === 'bold'
                  }"
                  style="margin-right: 8px"
                  @click="checkBold('fontWeightBtn')"
                >
                  <el-icon>
                    <Icon name="icon_bold_outlined"><icon_bold_outlined class="svg-icon" /></Icon>
                  </el-icon>
                </div>
              </el-tooltip>

              <el-tooltip :effect="toolTip" placement="bottom">
                <template #content>
                  {{ t('chart.italic') }}
                </template>
                <div
                  class="icon-btn"
                  :class="{
                    dark: themes === 'dark',
                    active: chart.customStyle.component.fontStyleBtn === 'italic'
                  }"
                  @click="checkItalic('fontStyleBtn')"
                >
                  <el-icon>
                    <Icon name="icon_italic_outlined"
                      ><icon_italic_outlined class="svg-icon"
                    /></Icon>
                  </el-icon>
                </div>
              </el-tooltip>
            </el-form-item>
          </el-form>
        </el-collapse-item>
      </el-collapse>
    </el-row>
  </div>
</template>

<style lang="less" scoped>
.view-panel {
  display: flex;
  height: 100%;
  width: 100%;
}

.attr-style {
  overflow-y: auto;
  height: 100%;
  width: 100%;

  .indented-item {
    width: 100%;
    display: flex;

    .fill {
      flex: 1;
    }

    &.disabled {
      cursor: not-allowed;
      color: #8f959e;

      :deep(.avatar-uploader) {
        width: 90px;
        pointer-events: none;
      }

      :deep(.ed-upload--picture-card) {
        cursor: not-allowed;
      }

      .img-area_dark {
        :deep(.ed-upload--picture-card) {
          .ed-icon {
            color: #5f5f5f;
          }
        }
      }
      .img-area_light {
        :deep(.ed-upload--picture-card) {
          .ed-icon {
            color: #bbbfc4;
          }
        }
      }

      &:hover {
        .ed-icon {
          color: #8f959e;
        }
      }
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
}

.form-item {
  margin-bottom: 16px;

  &.margin-bottom-8 {
    margin-bottom: 8px !important;
  }
  &.no-margin-bottom {
    margin-bottom: 0 !important;
  }

  .checkbox-with_icon {
    :deep(.ed-checkbox__label) {
      display: inline-flex;
      align-items: center;

      .ed-icon {
        margin-left: 5px;
      }
    }
  }

  .hint-icon {
    cursor: pointer;
    font-size: 14px;
    color: #646a73;

    &.hint-icon--dark {
      color: #a6a6a6;
    }
  }
}
.m-divider {
  border-color: rgba(31, 35, 41, 0.15);
  margin: 0 0 8px;
}

:deep(.form-item-dark) {
  .ed-form-item__label {
    color: @dv-canvas-main-font-color !important;
  }

  &.select-append {
    .ed-input-group__append {
      background-color: transparent;
    }
    .dv-dark {
      & > .ed-input__wrapper {
        background-color: #1a1a1a;
      }
      .ed-input-group__append .ed-select {
        margin: 0 -20px;
      }
    }
  }
}

.icon-btn {
  font-size: 16px;
  line-height: 16px;
  width: 24px;
  height: 24px;
  text-align: center;
  border-radius: 4px;
  padding-top: 4px;

  color: #1f2329;

  cursor: pointer;

  &.dark {
    color: #a6a6a6;
    &:hover {
      background-color: rgba(255, 255, 255, 0.1);
    }
    &.active {
      color: var(--ed-color-primary);
      background-color: var(--ed-color-primary-1a, rgba(51, 112, 255, 0.1));
      &:hover {
        background-color: #3370ff33;
      }
    }
  }

  &.active {
    color: var(--ed-color-primary);
    background-color: var(--ed-color-primary-1a, rgba(51, 112, 255, 0.1));
  }

  &:hover {
    background-color: rgba(31, 35, 41, 0.1);
  }
}

.is-disabled {
  .icon-btn {
    color: #8f959e;
    cursor: not-allowed;

    &:hover {
      background-color: inherit;
    }

    &.active {
      background-color: #f5f7fa;
      &:hover {
        background-color: #f5f7fa;
      }
    }
    &.dark {
      color: #5f5f5f;
      &.active {
        background-color: #373737;
        &:hover {
          background-color: #373737;
        }
      }
    }
  }
}
.icon-radio-group {
  :deep(.ed-radio) {
    margin-right: 8px;

    &:last-child {
      margin-right: 0;
    }
  }
  :deep(.ed-radio__input) {
    display: none;
  }
  :deep(.ed-radio__label) {
    padding: 0;
  }
}
</style>
