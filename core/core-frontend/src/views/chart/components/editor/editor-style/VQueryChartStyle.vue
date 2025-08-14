<script lang="tsx" setup>
import { ElMessage, ElMessageBox } from 'element-plus-secondary'
import icon_bold_outlined from '@/assets/svg/icon_bold_outlined.svg'
import { uploadFileResult } from '@/api/staticResource'
import icon_italic_outlined from '@/assets/svg/icon_italic_outlined.svg'
import { snapshotStoreWithOut } from '@/store/modules/data-visualization/snapshot'
import icon_info_outlined from '@/assets/svg/icon_info_outlined.svg'
import { useI18n } from '@/hooks/web/useI18n'
import { PropType, toRefs, computed, reactive, watch, ref, onMounted } from 'vue'
import { imgUrlTrans } from '@/utils/imgUtils'
import { COLOR_PANEL } from '@/views/chart/components/editor/util/chart'
import CollapseSwitchItem from '@/components/collapse-switch-item/src/CollapseSwitchItem.vue'
import { cloneDeep } from 'lodash-es'
import { useEmitt } from '@/hooks/web/useEmitt'
import { dvMainStoreWithOut } from '@/store/modules/data-visualization/dvMain'
import { storeToRefs } from 'pinia'
import BackgroundOverallCommon from '@/components/visualization/component-background/BackgroundOverallCommon.vue'
import { isDashboard, isMainCanvas } from '@/utils/canvasUtils'
const { t } = useI18n()
const styleActiveNames = ref(['basicStyle'])
const dvMainStore = dvMainStoreWithOut()
const { mobileInPc } = storeToRefs(dvMainStore)

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
const { chart, commonBackgroundPop, element } = toRefs(props)
const toolTip = computed(() => {
  return props.themes === 'dark' ? 'light' : 'dark'
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

const mobileStyleChange = () => {
  if (mobileInPc.value) {
    //移动端设计
    useEmitt().emitter.emit('onMobileStatusChange', {
      type: 'componentStyleChange',
      value: { type: 'renderChart', component: JSON.parse(JSON.stringify(chart.value)) }
    })
  }
}

const mobileBackgroundChange = () => {
  if (mobileInPc.value) {
    //移动端设计
    useEmitt().emitter.emit('onMobileStatusChange', {
      type: 'componentStyleChange',
      value: { type: 'commonBackground', component: JSON.parse(JSON.stringify(props.element)) }
    })
  }
}

watch(
  [() => state.commonBackground, () => commonBackgroundPop.value],
  () => {
    mobileBackgroundChange()
  },
  { deep: true }
)

watch(
  [() => chart.value.customStyle],
  () => {
    mobileStyleChange()
  },
  { deep: true }
)

watch(
  () => props.commonBackgroundPop,
  () => {
    init()
    mobileBackgroundChange()
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
  placeholder: '',
  queryConditionWidth: 227
})

const onFreezeChange = () => {
  if (element.value.freeze) {
    let historyFreezeCount = 0
    dvMainStore.componentData.forEach(item => {
      if (item.innerType === 'VQuery' && item.id !== element.value.id && item.freeze) {
        historyFreezeCount++
      }
    })
    if (historyFreezeCount) {
      ElMessageBox.confirm(t('visualization.filter_freeze_tips'), {
        confirmButtonType: 'primary',
        type: 'warning',
        confirmButtonText: t('common.sure'),
        cancelButtonText: t('common.cancel'),
        autofocus: false,
        showClose: false
      })
        .then(() => {
          dvMainStore.componentData.forEach(item => {
            if (item.innerType === 'VQuery' && item.id !== element.value.id && item.freeze) {
              item.freeze = false
            }
          })
          snapshotStore.recordSnapshotCache('onFreezeChange')
        })
        .catch(() => {
          element.value.freeze = false
        })
    } else {
      dvMainStore.componentData.forEach(item => {
        if (item.innerType === 'VQuery' && item.id !== element.value.id && item.freeze) {
          item.freeze = false
        }
      })
      snapshotStore.recordSnapshotCache('onFreezeChange')
    }
  } else {
    snapshotStore.recordSnapshotCache('onFreezeChange')
  }
}

const handleCurrentPlaceholder = val => {
  const obj = props.element.propValue.find(ele => {
    return ele.id === val
  }) || {
    placeholder: ''
  }
  if (obj.placeholder === undefined) {
    obj.placeholder = ''
  }

  if (obj.queryConditionWidth === undefined) {
    obj.queryConditionWidth = 227
  }
  currentSearch.value = obj
  snapshotStore.recordSnapshotCacheToMobile('propValue')
}

const init = () => {
  state.commonBackground = cloneDeep(props.commonBackgroundPop)
  if (state.commonBackground['outerImage']) {
    state.fileList.push({ url: imgUrlTrans(state.commonBackground['outerImage']) })
  } else {
    state.fileList = []
  }
}
const onBackgroundChangeV2 = val => {
  snapshotStore.recordSnapshotCache('onBackgroundChange')
  element.value.commonBackground = val
}

const onBackgroundChange = () => {
  snapshotStore.recordSnapshotCache('onBackgroundChange')
  commonBackgroundPop.value.outerImage = state.commonBackground['outerImage']
}
onMounted(() => {
  init()
})

const reUpload = e => {
  const file = e.target.files[0]
  if (file.size > 15000000) {
    ElMessage.error('图片大小不能超过15M')
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

const handleCurrentPlaceholderCustomChange = () => {
  if (mobileInPc.value) {
    //移动端设计
    useEmitt().emitter.emit('onMobileStatusChange', {
      type: 'componentStyleChange',
      value: { type: 'renderChart', component: JSON.parse(JSON.stringify(chart.value)) }
    })
  } else {
    snapshotStore.recordSnapshotCache('handleCurrentPlaceholderCustomChange')
  }
}

const handleCurrentPlaceholderChange = () => {
  snapshotStore.recordSnapshotCacheToMobile('propValue')
}

const checkItalic = type => {
  if (!chart.value.customStyle.component.labelShow) return
  chart.value.customStyle.component[type] = chart.value.customStyle.component[type] ? '' : 'italic'
}
const initParams = () => {
  if (!chart.value.customStyle.component.hasOwnProperty('queryConditionHeight')) {
    chart.value.customStyle.component = {
      ...chart.value.customStyle.component,
      queryConditionHeight: 32
    }
  }
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
const onTitleChange = () => {
  element.value.label = chart.value.customStyle.component.title
  element.value.name = chart.value.customStyle.component.title
  chart.value.title = chart.value.customStyle.component.title
}

const onPlaceholderChange = () => {
  props.element.propValue.forEach(ele => {
    if (ele.id === currentPlaceholder.value) {
      ele.placeholder = currentSearch.value.placeholder
      ele.queryConditionWidth = currentSearch.value.queryConditionWidth
    }
  })
  snapshotStore.recordSnapshotCacheToMobile('propValue')
}
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
          <el-form size="small" @keydown.stop.prevent.enter label-position="top">
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
                @change="onTitleChange"
              />
            </el-form-item>
            <el-form-item
              :label="t('components.title_color')"
              class="form-item"
              style="padding-left: 20px"
              :class="'form-item-' + themes"
            >
              <el-color-picker
                :effect="themes"
                v-model="chart.customStyle.component.titleColor"
                :trigger-width="204"
                show-alpha
                :disabled="!chart.customStyle.component.titleShow"
                is-custom
                :predefine="COLOR_PANEL"
              />
            </el-form-item>
            <el-form-item
              v-if="!mobileInPc && isDashboard() && isMainCanvas(element.canvasId)"
              class="form-item margin-bottom-8"
              :class="'form-item-' + themes"
              :label="t('visualization.query_position')"
            >
              <el-radio-group
                v-model="element.freeze"
                :effect="themes"
                size="small"
                @change="onFreezeChange"
              >
                <el-radio :effect="themes" style="min-width: 80px" :label="true">{{
                  t('visualization.to_top')
                }}</el-radio>
                <el-radio :effect="themes" style="min-width: 80px" :label="false">{{
                  t('visualization.default')
                }}</el-radio>
              </el-radio-group>
            </el-form-item>
            <background-overall-common
              :common-background-pop="commonBackgroundPop"
              :themes="themes"
              @onBackgroundChange="onBackgroundChangeV2"
              component-position="component"
            />
          </el-form>
        </el-collapse-item>
        <el-collapse-item :effect="themes" name="addition" :title="t('v_query.query_condition')">
          <el-form
            size="small"
            @keydown.stop.prevent.enter
            label-position="top"
            style="padding-bottom: 8px"
          >
            <el-row :gutter="8">
              <el-col :span="12">
                <el-form-item
                  :label="t('visualization.board')"
                  class="form-item w100"
                  :class="'form-item-' + themes"
                >
                  <el-color-picker
                    :effect="themes"
                    :trigger-width="106"
                    is-custom
                    show-alpha
                    v-model="chart.customStyle.component.borderColor"
                    :predefine="predefineColors"
                  />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item
                  :label="t('chart.background')"
                  class="form-item w100"
                  :class="'form-item-' + themes"
                >
                  <el-color-picker
                    :effect="themes"
                    :trigger-width="106"
                    is-custom
                    show-alpha
                    v-model="chart.customStyle.component.bgColor"
                    :predefine="predefineColors"
                  />
                </el-form-item>
              </el-col>
            </el-row>
            <el-form-item
              :effect="themes"
              class="form-item"
              :label="t('visualization.query_condition_space')"
              :class="'form-item-' + themes"
            >
              <el-input-number
                v-model="chart.customStyle.component.queryConditionSpacing"
                :min="0"
                :effect="themes"
                controls-position="right"
              />
            </el-form-item>
            <el-form-item
              :effect="themes"
              class="form-item"
              :label="t('visualization.query_condition_height')"
              :class="'form-item-' + themes"
            >
              <el-input-number
                v-model="chart.customStyle.component.queryConditionHeight"
                :min="32"
                :effect="themes"
                controls-position="right"
              />
            </el-form-item>
            <el-form-item class="form-item margin-bottom-8" :class="'form-item-' + themes">
              <el-checkbox
                :effect="themes"
                size="small"
                @change="handleCurrentPlaceholderCustomChange"
                v-model="chart.customStyle.component.placeholderShow"
              >
                {{ t('v_query.custom_condition_style') }}
              </el-checkbox>
            </el-form-item>
            <el-form-item
              :label="t('visualization.text_html')"
              class="form-item"
              style="padding-left: 20px"
              :class="'form-item-' + themes"
            >
              <div style="display: flex; align-items: center; width: 100%">
                <el-color-picker
                  :effect="themes"
                  :trigger-width="56"
                  style="max-width: 56px; min-width: 56px"
                  is-custom
                  show-alpha
                  v-model="chart.customStyle.component.text"
                  :disabled="!chart.customStyle.component.placeholderShow"
                  @change="handleCurrentPlaceholderCustomChange"
                  :predefine="predefineColors"
                />
                <el-input-number
                  v-model="chart.customStyle.component.placeholderSize"
                  @change="handleCurrentPlaceholderCustomChange"
                  :min="10"
                  :max="40"
                  :disabled="!chart.customStyle.component.placeholderShow"
                  style="margin-left: 8px"
                  step-strictly
                  :effect="themes"
                  controls-position="right"
                />
              </div>
              <div style="display: flex; align-items: center; width: 100%; margin-top: 8px">
                <el-select
                  v-model="currentPlaceholder"
                  :disabled="!chart.customStyle.component.placeholderShow"
                  @change="handleCurrentPlaceholder"
                  :effect="themes"
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
              :label="t('visualization.tips_world')"
              class="form-item"
              style="padding-left: 20px"
              :class="'form-item-' + themes"
            >
              <el-input
                :effect="themes"
                @change="onPlaceholderChange"
                :disabled="!chart.customStyle.component.placeholderShow || !currentPlaceholder"
                v-model.lazy="currentSearch.placeholder"
              />
            </el-form-item>
            <el-form-item
              :label="t('v_query.query_condition_width')"
              class="form-item"
              style="padding-left: 20px"
              :class="'form-item-' + themes"
            >
              <el-input-number
                :effect="themes"
                :min="100"
                controls-position="right"
                @change="onPlaceholderChange"
                :disabled="!chart.customStyle.component.placeholderShow || !currentPlaceholder"
                v-model.lazy="currentSearch.queryConditionWidth"
              />
            </el-form-item>
          </el-form>
        </el-collapse-item>
        <collapse-switch-item
          :themes="themes"
          v-model="chart.customStyle.component.labelShow"
          name="legend"
          :title="t('visualization.query_condition_name')"
        >
          <el-form
            :class="!chart.customStyle.component.labelShow && 'is-disabled'"
            :disabled="!chart.customStyle.component.labelShow"
            label-position="top"
            size="small"
            style="padding-bottom: 8px"
          >
            <el-form-item
              :effect="themes"
              class="form-item"
              :label="t('visualization.position_adjust')"
              :class="'form-item-' + themes"
            >
              <el-radio-group :effect="themes" v-model="chart.customStyle.component.layout">
                <el-radio label="vertical" :effect="themes">
                  {{ t('visualization.condition_top') }}
                </el-radio>
                <el-radio label="horizontal" :effect="themes">
                  {{ t('visualization.condition_left') }}
                </el-radio>
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
                show-alpha
                style="width: 50px"
                v-model="chart.customStyle.component.labelColor"
                :predefine="predefineColors"
              /><el-tooltip
                :content="t('visualization.font_size')"
                :effect="toolTip"
                placement="top"
              >
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
              :label="t('visualization.query_name_space2')"
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
          <el-form
            size="small"
            @keydown.stop.prevent.enter
            label-position="top"
            style="padding-bottom: 8px"
          >
            <el-form-item
              :effect="themes"
              class="form-item"
              :label="t('visualization.show_button')"
              :class="'form-item-' + themes"
            >
              <el-checkbox-group :effect="themes" v-model="chart.customStyle.component.btnList">
                <el-checkbox class="checkbox-with_icon" :effect="themes" size="small" label="sure">
                  {{ t('commons.adv_search.search') }}
                  <el-tooltip
                    :effect="toolTip"
                    :content="t('visualization.query_tips')"
                    placement="top"
                  >
                    <el-icon class="hint-icon" :class="{ 'hint-icon--dark': themes === 'dark' }">
                      <Icon name="icon_info_outlined"><icon_info_outlined class="svg-icon" /></Icon>
                    </el-icon>
                  </el-tooltip>
                </el-checkbox>
                <br />
                <el-checkbox style="margin-top: 8px" :effect="themes" size="small" label="clear">
                  {{ t('commons.clear') }}
                </el-checkbox>
                <br />
                <el-checkbox style="margin-top: 8px" :effect="themes" size="small" label="reset">
                  {{ t('commons.adv_search.reset') }}
                </el-checkbox>
              </el-checkbox-group>
            </el-form-item>
            <el-form-item
              class="form-item"
              :label="t('visualization.button_color')"
              :class="'form-item-' + themes"
            >
              <el-color-picker
                :effect="themes"
                :trigger-width="108"
                is-custom
                show-alpha
                v-model="chart.customStyle.component.btnColor"
                :predefine="predefineColors"
              />
            </el-form-item>
            <el-form-item
              :label="t('visualization.button_text')"
              class="form-item margin-bottom-8"
              :class="'form-item-' + themes"
            >
              <el-color-picker
                :effect="themes"
                is-custom
                show-alpha
                v-model="chart.customStyle.component.labelColorBtn"
                :predefine="predefineColors"
              /><el-tooltip
                :content="t('visualization.font_size')"
                :effect="toolTip"
                placement="top"
              >
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
