<script lang="ts" setup>
import icon_letterSpacing_outlined from '@/assets/svg/icon_letter-spacing_outlined.svg'
import icon_bold_outlined from '@/assets/svg/icon_bold_outlined.svg'
import icon_italic_outlined from '@/assets/svg/icon_italic_outlined.svg'
import icon_leftAlignment_outlined from '@/assets/svg/icon_left-alignment_outlined.svg'
import icon_centerAlignment_outlined from '@/assets/svg/icon_center-alignment_outlined.svg'
import icon_rightAlignment_outlined from '@/assets/svg/icon_right-alignment_outlined.svg'
import icon_edit_outlined from '@/assets/svg/icon_edit_outlined.svg'
import { PropType, computed, onMounted, reactive, toRefs, watch, nextTick, ref } from 'vue'
import { useAppearanceStoreWithOut } from '@/store/modules/appearance'
import { useI18n } from '@/hooks/web/useI18n'
import {
  COLOR_PANEL,
  CHART_FONT_FAMILY,
  CHART_FONT_LETTER_SPACE,
  DEFAULT_TITLE_STYLE
} from '@/views/chart/components/editor/util/chart'
import { dvMainStoreWithOut } from '@/store/modules/data-visualization/dvMain'
import { storeToRefs } from 'pinia'
import { cloneDeep, defaultsDeep } from 'lodash-es'
import { ElButton, ElIcon } from 'element-plus-secondary'
import Icon from '@/components/icon-custom/src/Icon.vue'
const dvMainStore = dvMainStoreWithOut()
const { batchOptStatus } = storeToRefs(dvMainStore)

const { t } = useI18n()

const props = defineProps({
  chart: {
    type: Object,
    required: true
  },
  themes: {
    type: String as PropType<EditorTheme>,
    default: 'dark'
  },
  propertyInner: {
    type: Array<string>
  }
})
const appearanceStore = useAppearanceStoreWithOut()
const emit = defineEmits(['onTextChange'])
const toolTip = computed(() => {
  return props.themes === 'dark' ? 'ndark' : 'dark'
})
const predefineColors = COLOR_PANEL
const fontFamily = CHART_FONT_FAMILY.concat(
  appearanceStore.fontList.map(ele => ({
    name: ele.name,
    value: ele.name
  }))
)
const fontLetterSpace = CHART_FONT_LETTER_SPACE

const state = reactive({
  titleForm: JSON.parse(JSON.stringify(DEFAULT_TITLE_STYLE))
})

const { chart } = toRefs(props)

const fontSizeList = computed(() => {
  const arr = []
  for (let i = 10; i <= 40; i = i + 2) {
    arr.push({
      name: i + '',
      value: i
    })
  }
  return arr
})

const changeTitleStyle = prop => {
  emit('onTextChange', state.titleForm, prop)
}

const init = () => {
  const customText = defaultsDeep(
    cloneDeep(props.chart?.customStyle?.text),
    cloneDeep(DEFAULT_TITLE_STYLE)
  )

  state.titleForm = cloneDeep(customText)

  //第一次颜色可能赋值失败，单独赋值一次
  nextTick(() => {
    state.titleForm.color = customText.color
  })
}

const showEditRemark = ref<boolean>(false)
const tempRemark = ref<string>('')

const openEditRemark = () => {
  tempRemark.value = cloneDeep(state.titleForm.remark)
  showEditRemark.value = true
}

const closeEditRemark = () => {
  showEditRemark.value = false
  tempRemark.value = ''
}

const saveEditRemark = () => {
  showEditRemark.value = false
  state.titleForm.remark = tempRemark.value
  changeTitleStyle('remark')
}

onMounted(() => {
  init()
})

watch(
  () => props.chart?.customStyle?.text,
  () => {
    init()
  },
  { deep: true }
)
</script>

<template>
  <div>
    <el-form
      ref="titleForm"
      :disabled="!state.titleForm.show"
      :model="state.titleForm"
      label-position="top"
    >
      <el-form-item
        :label="t('chart.title')"
        class="form-item"
        :class="'form-item-' + themes"
        v-if="!batchOptStatus"
      >
        <el-input
          :effect="themes"
          v-model="chart.title"
          size="small"
          maxlength="100"
          :placeholder="t('chart.title')"
          clearable
          @blur="changeTitleStyle('title')"
        />
      </el-form-item>

      <el-form-item
        class="form-item"
        :class="'form-item-' + themes"
        :effect="themes"
        :label="t('chart.text')"
      >
        <el-select
          :effect="themes"
          v-model="state.titleForm.fontFamily"
          :placeholder="t('chart.font_family')"
          @change="changeTitleStyle('fontFamily')"
        >
          <el-option
            v-for="option in fontFamily"
            :key="option.value"
            :label="option.name"
            :value="option.value"
          />
        </el-select>
      </el-form-item>

      <div style="display: flex">
        <el-form-item class="form-item" :class="'form-item-' + themes" style="padding-right: 4px">
          <el-color-picker
            :effect="themes"
            v-model="state.titleForm.color"
            class="color-picker-style"
            :predefine="predefineColors"
            @change="changeTitleStyle('color')"
            is-custom
          />
        </el-form-item>
        <el-form-item class="form-item" :class="'form-item-' + themes" style="padding: 0 4px">
          <el-tooltip content="字号" :effect="toolTip" placement="top">
            <el-select
              style="width: 56px"
              :effect="themes"
              v-model="state.titleForm.fontSize"
              :placeholder="t('chart.text_fontsize')"
              size="small"
              @change="changeTitleStyle('fontSize')"
            >
              <el-option
                v-for="option in fontSizeList"
                :key="option.value"
                :label="option.name"
                :value="option.value"
              />
            </el-select>
          </el-tooltip>
        </el-form-item>

        <el-form-item class="form-item" :class="'form-item-' + themes" style="padding-left: 4px">
          <el-select
            :effect="themes"
            v-model="state.titleForm.letterSpace"
            :placeholder="t('chart.quota_letter_space')"
            @change="changeTitleStyle('letterSpace')"
          >
            <template #prefix>
              <el-icon>
                <Icon name="icon_letter-spacing_outlined"
                  ><icon_letterSpacing_outlined class="svg-icon"
                /></Icon>
              </el-icon>
            </template>
            <el-option
              v-for="option in fontLetterSpace"
              :key="option.value"
              :label="option.name"
              :value="option.value"
            />
          </el-select>
        </el-form-item>
      </div>

      <el-space>
        <el-form-item class="form-item" :class="'form-item-' + themes">
          <el-checkbox
            :effect="themes"
            class="icon-checkbox"
            v-model="state.titleForm.isBolder"
            @change="changeTitleStyle('isBolder')"
          >
            <el-tooltip :effect="toolTip" placement="top">
              <template #content>
                {{ t('chart.bolder') }}
              </template>
              <div
                class="icon-btn"
                :class="{ dark: themes === 'dark', active: state.titleForm.isBolder }"
              >
                <el-icon>
                  <Icon name="icon_bold_outlined"><icon_bold_outlined class="svg-icon" /></Icon>
                </el-icon>
              </div>
            </el-tooltip>
          </el-checkbox>
        </el-form-item>

        <el-form-item class="form-item" :class="'form-item-' + themes">
          <el-checkbox
            :effect="themes"
            class="icon-checkbox"
            v-model="state.titleForm.isItalic"
            @change="changeTitleStyle('isItalic')"
          >
            <el-tooltip :effect="toolTip" placement="top">
              <template #content>
                {{ t('chart.italic') }}
              </template>
              <div
                class="icon-btn"
                :class="{ dark: themes === 'dark', active: state.titleForm.isItalic }"
              >
                <el-icon>
                  <Icon name="icon_italic_outlined"><icon_italic_outlined class="svg-icon" /></Icon>
                </el-icon>
              </div>
            </el-tooltip>
          </el-checkbox>
        </el-form-item>

        <div class="position-divider" :class="'position-divider--' + themes"></div>

        <el-form-item class="form-item" :class="'form-item-' + themes">
          <el-radio-group
            :effect="themes"
            class="icon-radio-group"
            v-model="state.titleForm.hPosition"
            @change="changeTitleStyle('hPosition')"
          >
            <el-radio :effect="themes" label="left">
              <el-tooltip :effect="toolTip" placement="top">
                <template #content>
                  {{ t('chart.text_pos_left') }}
                </template>
                <div
                  class="icon-btn"
                  :class="{ dark: themes === 'dark', active: state.titleForm.hPosition === 'left' }"
                >
                  <el-icon>
                    <Icon name="icon_left-alignment_outlined"
                      ><icon_leftAlignment_outlined class="svg-icon"
                    /></Icon>
                  </el-icon>
                </div>
              </el-tooltip>
            </el-radio>
            <el-radio :effect="themes" label="center">
              <el-tooltip :effect="toolTip" placement="top">
                <template #content>
                  {{ t('chart.text_pos_center') }}
                </template>
                <div
                  class="icon-btn"
                  :class="{
                    dark: themes === 'dark',
                    active: state.titleForm.hPosition === 'center'
                  }"
                >
                  <el-icon>
                    <Icon name="icon_center-alignment_outlined"
                      ><icon_centerAlignment_outlined class="svg-icon"
                    /></Icon>
                  </el-icon>
                </div>
              </el-tooltip>
            </el-radio>
            <el-radio :effect="themes" label="right">
              <el-tooltip :effect="toolTip" placement="top">
                <template #content>
                  {{ t('chart.text_pos_right') }}
                </template>
                <div
                  class="icon-btn"
                  :class="{
                    dark: themes === 'dark',
                    active: state.titleForm.hPosition === 'right'
                  }"
                >
                  <el-icon>
                    <Icon name="icon_right-alignment_outlined"
                      ><icon_rightAlignment_outlined class="svg-icon"
                    /></Icon>
                  </el-icon>
                </div>
              </el-tooltip>
            </el-radio>
          </el-radio-group>
        </el-form-item>
      </el-space>

      <el-form-item class="form-item" :class="'form-item-' + themes">
        <el-checkbox
          size="small"
          :effect="themes"
          v-model="state.titleForm.fontShadow"
          @change="changeTitleStyle('fontShadow')"
        >
          {{ t('chart.font_shadow') }}
        </el-checkbox>
      </el-form-item>

      <el-form-item class="form-item" :class="'form-item-' + themes">
        <el-checkbox
          size="small"
          :effect="themes"
          v-model="state.titleForm.remarkShow"
          @change="changeTitleStyle('remarkShow')"
        >
          {{ t('chart.remark_show') }}
        </el-checkbox>
      </el-form-item>
      <el-form-item class="form-item" :class="'form-item-' + themes" style="margin-left: 22px">
        <label class="remark-label" :class="{ 'remark-label--dark': themes === 'dark' }">
          {{ t('chart.remark_edit') }}
        </label>
        <el-button text @click="openEditRemark" :effect="themes">
          <el-icon size="14px">
            <Icon name="icon_edit_outlined"><icon_edit_outlined class="svg-icon" /></Icon>
          </el-icon>
        </el-button>
      </el-form-item>
    </el-form>

    <el-dialog
      :title="t('chart.remark_edit')"
      :visible="showEditRemark"
      v-model="showEditRemark"
      width="420px"
      :close-on-click-modal="false"
    >
      <div @keydown.stop @keyup.stop>
        <el-form-item :label="t('chart.remark')" class="form-item" prop="chartShowName">
          <el-input
            type="textarea"
            autosize
            v-model="tempRemark"
            :maxlength="50"
            clearable
            :placeholder="t('chart.remark_placeholder')"
          />
        </el-form-item>
      </div>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="closeEditRemark">{{ t('chart.cancel') }}</el-button>
          <el-button type="primary" @click="saveEditRemark()">{{ t('chart.confirm') }}</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<style lang="less" scoped>
:deep(.ed-input .ed-select__prefix--light) {
  padding-right: 6px;
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
    &.active {
      color: var(--ed-color-primary);
      background-color: var(--ed-color-primary-1a, rgba(51, 112, 255, 0.1));
    }
    &:hover {
      background-color: rgba(255, 255, 255, 0.1);
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

.icon-checkbox {
  :deep(.ed-checkbox__input) {
    display: none;
  }
  :deep(.ed-checkbox__label) {
    padding: 0;
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
.position-divider {
  width: 1px;
  height: 18px;
  margin-bottom: 16px;
  background: rgba(31, 35, 41, 0.15);

  &.position-divider--dark {
    background: rgba(235, 235, 235, 0.15);
  }
}
.remark-label {
  color: var(--N600, #646a73);
  font-family: var(--de-custom_font, 'PingFang');
  font-size: 12px;
  font-style: normal;
  font-weight: 400;
  line-height: 20px;

  &.remark-label--dark {
    color: var(--N600-Dark, #a6a6a6);
  }
}
</style>
