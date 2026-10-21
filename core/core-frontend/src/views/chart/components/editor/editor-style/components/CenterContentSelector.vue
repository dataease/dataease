<script lang="ts" setup>
import { computed, reactive, watch } from 'vue'
import { cloneDeep, defaultsDeep } from 'lodash-es'
import { ElIcon } from 'element-plus-secondary'
import { useI18n } from '@/hooks/web/useI18n'
import { useAppearanceStoreWithOut } from '@/store/modules/appearance'
import {
  CHART_FONT_FAMILY_ORIGIN,
  CHART_FONT_LETTER_SPACE,
  COLOR_PANEL,
  DEFAULT_CENTER_CONTENT
} from '@/views/chart/components/editor/util/chart'
import {
  formatterType,
  getUnitTypeList,
  initFormatCfgUnit,
  isEnLocal,
  onChangeFormatCfgUnitLanguage
} from '@/views/chart/components/js/formatter'
import Icon from '@/components/icon-custom/src/Icon.vue'
import icon_bold_outlined from '@/assets/svg/icon_bold_outlined.svg'
import icon_italic_outlined from '@/assets/svg/icon_italic_outlined.svg'
import icon_letterSpacing_outlined from '@/assets/svg/icon_letter-spacing_outlined.svg'

const props = defineProps<{
  chart: ChartObj
  themes: EditorTheme
}>()
const emit = defineEmits<{
  onCenterContentChange: [value: ChartCenterContentAttr, prop: string]
}>()
const { t } = useI18n()
const appearanceStore = useAppearanceStoreWithOut()
const state = reactive({ form: cloneDeep(DEFAULT_CENTER_CONTENT) })
const textSections = ['titleStyle', 'contentStyle', 'suffixStyle'] as const
const contentTypes = [
  { value: 'sum', label: 'total_show' },
  { value: 'avg', label: 'center_content_average' },
  { value: 'max', label: 'max' },
  { value: 'min', label: 'min' },
  { value: 'count', label: 'center_content_count' },
  { value: 'custom', label: 'center_content_custom_text' }
]
const fontSizeList = [
  ...Array.from({ length: 26 }, (_, index) => 10 + index * 2),
  ...Array.from({ length: 15 }, (_, index) => 70 + index * 10)
]
const fontFamily = computed(() =>
  CHART_FONT_FAMILY_ORIGIN.concat(
    appearanceStore.fontList.map(item => ({ name: item.name, value: item.name }))
  )
)
const showFormatter = computed(() => !['custom', 'count'].includes(state.form.contentType))

const change = (prop: string) => {
  emit('onCenterContentChange', state.form, prop)
}
const changeUnitLanguage = (language: BaseFormatter['unitLanguage']) => {
  onChangeFormatCfgUnitLanguage(state.form.formatter, language)
  change('formatter')
}

watch(
  () => props.chart.customAttr.centerContent,
  value => {
    state.form = defaultsDeep(cloneDeep(value || {}), cloneDeep(DEFAULT_CENTER_CONTENT))
    initFormatCfgUnit(state.form.formatter)
  },
  { immediate: true, deep: true }
)
</script>

<template>
  <el-form :model="state.form" :disabled="!state.form.show" label-position="top" size="small">
    <el-form-item :label="t('chart.title')" class="form-item" :class="'form-item-' + themes">
      <el-input
        v-model="state.form.title"
        :effect="themes"
        maxlength="50"
        clearable
        @change="change('title')"
      />
    </el-form-item>
    <el-form-item :label="t('chart.position')" class="form-item" :class="'form-item-' + themes">
      <el-select
        v-model="state.form.titlePosition"
        :effect="themes"
        @change="change('titlePosition')"
      >
        <el-option :label="t('chart.center_content_top')" value="top" />
        <el-option :label="t('chart.center_content_bottom')" value="bottom" />
      </el-select>
    </el-form-item>

    <template v-for="section in textSections" :key="section">
      <template v-if="section === 'contentStyle'">
        <el-divider class="m-divider" :class="{ 'divider-dark': themes === 'dark' }" />
        <el-form-item
          :label="t('chart.center_content_type')"
          class="form-item"
          :class="'form-item-' + themes"
        >
          <el-select
            v-model="state.form.contentType"
            :effect="themes"
            @change="change('contentType')"
          >
            <el-option
              v-for="item in contentTypes"
              :key="item.value"
              :label="t('chart.' + item.label)"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item
          v-if="state.form.contentType === 'custom'"
          class="form-item"
          :class="'form-item-' + themes"
        >
          <el-input
            v-model="state.form.content"
            :effect="themes"
            maxlength="50"
            clearable
            @change="change('content')"
          />
        </el-form-item>
        <template v-if="showFormatter">
          <el-form-item
            :label="t('chart.value_formatter')"
            class="form-item"
            :class="'form-item-' + themes"
          >
            <el-select
              v-model="state.form.formatterMode"
              :effect="themes"
              @change="change('formatterMode')"
            >
              <el-option :label="t('chart.center_content_follow_quota')" value="quota" />
              <el-option :label="t('chart.custom')" value="custom" />
            </el-select>
          </el-form-item>
          <template v-if="state.form.formatterMode === 'custom'">
            <el-form-item
              :label="t('chart.value_formatter_type')"
              class="form-item"
              :class="'form-item-' + themes"
            >
              <el-select
                v-model="state.form.formatter.type"
                :effect="themes"
                @change="change('formatter.type')"
              >
                <el-option
                  v-for="item in formatterType"
                  :key="item.value"
                  :label="t('chart.' + item.name)"
                  :value="item.value"
                />
              </el-select>
            </el-form-item>
            <el-form-item
              v-if="state.form.formatter.type !== 'auto'"
              :label="t('chart.value_formatter_decimal_count')"
              class="form-item"
              :class="'form-item-' + themes"
            >
              <el-input-number
                v-model="state.form.formatter.decimalCount"
                :effect="themes"
                controls-position="right"
                :precision="0"
                :min="0"
                :max="10"
                @change="change('formatter.decimalCount')"
              />
            </el-form-item>
            <template v-if="state.form.formatter.type !== 'percent'">
              <el-row :gutter="8">
                <el-col v-if="!isEnLocal" :span="12">
                  <el-form-item
                    :label="t('chart.value_formatter_unit_language')"
                    class="form-item"
                    :class="'form-item-' + themes"
                  >
                    <el-select
                      v-model="state.form.formatter.unitLanguage"
                      :effect="themes"
                      @change="changeUnitLanguage"
                    >
                      <el-option :label="t('chart.value_formatter_unit_language_ch')" value="ch" />
                      <el-option :label="t('chart.value_formatter_unit_language_en')" value="en" />
                    </el-select>
                  </el-form-item>
                </el-col>
                <el-col :span="isEnLocal ? 24 : 12">
                  <el-form-item
                    :label="t('chart.value_formatter_unit')"
                    class="form-item"
                    :class="'form-item-' + themes"
                  >
                    <el-select
                      v-model="state.form.formatter.unit"
                      :effect="themes"
                      @change="change('formatter.unit')"
                    >
                      <el-option
                        v-for="item in getUnitTypeList(state.form.formatter.unitLanguage)"
                        :key="item.value"
                        :label="item.name"
                        :value="item.value"
                      />
                    </el-select>
                  </el-form-item>
                </el-col>
              </el-row>
              <el-form-item
                :label="t('chart.value_formatter_suffix')"
                class="form-item"
                :class="'form-item-' + themes"
              >
                <el-input
                  v-model="state.form.formatter.suffix"
                  :effect="themes"
                  clearable
                  @change="change('formatter.suffix')"
                />
              </el-form-item>
            </template>
            <el-form-item class="form-item" :class="'form-item-' + themes">
              <el-checkbox
                v-model="state.form.formatter.thousandSeparator"
                :effect="themes"
                @change="change('formatter.thousandSeparator')"
              >
                {{ t('chart.value_formatter_thousand_separator') }}
              </el-checkbox>
            </el-form-item>
          </template>
        </template>
      </template>

      <template v-if="section === 'suffixStyle'">
        <el-divider class="m-divider" :class="{ 'divider-dark': themes === 'dark' }" />
        <el-form-item class="form-item" :class="'form-item-' + themes">
          <el-checkbox
            v-model="state.form.suffixEnable"
            :effect="themes"
            @change="change('suffixEnable')"
          >
            {{ t('chart.indicator_suffix') }}
          </el-checkbox>
        </el-form-item>
        <el-form-item class="form-item" :class="'form-item-' + themes">
          <el-input
            v-model="state.form.suffix"
            :disabled="!state.form.suffixEnable"
            :effect="themes"
            :placeholder="t('chart.indicator_suffix_placeholder')"
            maxlength="10"
            @change="change('suffix')"
          />
        </el-form-item>
      </template>

      <el-form-item :label="t('chart.text')" class="form-item" :class="'form-item-' + themes">
        <el-select
          v-model="state.form[section].fontFamily"
          :disabled="section === 'suffixStyle' && !state.form.suffixEnable"
          :effect="themes"
          :placeholder="t('chart.font_family')"
          @change="change(section + '.fontFamily')"
        >
          <el-option
            v-for="item in fontFamily"
            :key="item.value"
            :label="item.name"
            :value="item.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item class="form-item" :class="'form-item-' + themes">
        <el-select
          v-model="state.form[section].letterSpace"
          :disabled="section === 'suffixStyle' && !state.form.suffixEnable"
          :effect="themes"
          :placeholder="t('chart.quota_letter_space')"
          @change="change(section + '.letterSpace')"
        >
          <template #prefix>
            <el-icon>
              <Icon name="icon_letter-spacing_outlined">
                <icon_letterSpacing_outlined class="svg-icon" />
              </Icon>
            </el-icon>
          </template>
          <el-option
            v-for="item in CHART_FONT_LETTER_SPACE"
            :key="item.value"
            :label="item.name"
            :value="item.value"
          />
        </el-select>
      </el-form-item>
      <div style="display: flex; align-items: flex-end">
        <el-form-item class="form-item" :class="'form-item-' + themes" style="padding-right: 4px">
          <el-color-picker
            v-model="state.form[section].color"
            :disabled="section === 'suffixStyle' && !state.form.suffixEnable"
            :effect="themes"
            :predefine="COLOR_PANEL"
            class="color-picker-style"
            show-alpha
            is-custom
            @change="change(section + '.color')"
          />
        </el-form-item>
        <el-form-item class="form-item" :class="'form-item-' + themes" style="padding: 0 4px">
          <el-tooltip :content="t('chart.font_size')" :effect="themes" placement="top">
            <el-select
              v-model="state.form[section].fontSize"
              :disabled="section === 'suffixStyle' && !state.form.suffixEnable"
              :effect="themes"
              style="width: 56px"
              :placeholder="t('chart.text_fontsize')"
              @change="change(section + '.fontSize')"
            >
              <el-option
                v-for="size in fontSizeList"
                :key="size"
                :label="String(size)"
                :value="size"
              />
            </el-select>
          </el-tooltip>
        </el-form-item>
        <el-form-item class="form-item" :class="'form-item-' + themes" style="padding: 0 4px">
          <el-checkbox
            v-model="state.form[section].isBolder"
            :disabled="section === 'suffixStyle' && !state.form.suffixEnable"
            :effect="themes"
            class="icon-checkbox"
            @change="change(section + '.isBolder')"
          >
            <el-tooltip :content="t('chart.bolder')" :effect="themes" placement="top">
              <div
                class="icon-btn"
                :class="{ dark: themes === 'dark', active: state.form[section].isBolder }"
              >
                <el-icon>
                  <Icon name="icon_bold_outlined"><icon_bold_outlined class="svg-icon" /></Icon>
                </el-icon>
              </div>
            </el-tooltip>
          </el-checkbox>
        </el-form-item>
        <el-form-item class="form-item" :class="'form-item-' + themes" style="padding-left: 4px">
          <el-checkbox
            v-model="state.form[section].isItalic"
            :disabled="section === 'suffixStyle' && !state.form.suffixEnable"
            :effect="themes"
            class="icon-checkbox"
            @change="change(section + '.isItalic')"
          >
            <el-tooltip :content="t('chart.italic')" :effect="themes" placement="top">
              <div
                class="icon-btn"
                :class="{ dark: themes === 'dark', active: state.form[section].isItalic }"
              >
                <el-icon>
                  <Icon name="icon_italic_outlined"><icon_italic_outlined class="svg-icon" /></Icon>
                </el-icon>
              </div>
            </el-tooltip>
          </el-checkbox>
        </el-form-item>
      </div>
      <el-form-item class="form-item" :class="'form-item-' + themes">
        <el-checkbox
          v-model="state.form[section].fontShadow"
          :disabled="section === 'suffixStyle' && !state.form.suffixEnable"
          :effect="themes"
          @change="change(section + '.fontShadow')"
        >
          {{ t('chart.font_shadow') }}
        </el-checkbox>
      </el-form-item>
    </template>
  </el-form>
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
  border-radius: 6px;
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
  height: 24px;
  :deep(.ed-checkbox__input) {
    display: none;
  }
  :deep(.ed-checkbox__label) {
    padding: 0;
  }
}
.m-divider {
  margin: 0 0 16px;
  border-color: rgba(31, 35, 41, 0.15);
  &.divider-dark {
    border-color: rgba(255, 255, 255, 0.15);
  }
}
</style>
