<script lang="ts" setup>
import { computed, onMounted, PropType, reactive, watch } from 'vue'
import { useI18n } from '@/hooks/web/useI18n'
import { COLOR_PANEL, DEFAULT_TABLE_HEADER } from '@/views/chart/components/editor/util/chart'
import { ElSpace } from 'element-plus-secondary'
import { cloneDeep, defaultsDeep } from 'lodash-es'
import { convertToAlphaColor, isAlphaColor } from '@/views/chart/components/js/util'

const { t } = useI18n()

const props = defineProps({
  chart: {
    type: Object as PropType<ChartObj>,
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

watch(
  () => props.chart.customAttr.tableHeader,
  () => {
    init()
  },
  { deep: true }
)

const predefineColors = COLOR_PANEL

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

const state = reactive({
  tableHeaderForm: {} as ChartTableHeaderAttr
})

const emit = defineEmits(['onTableHeaderChange'])

const changeTableHeader = prop => {
  emit('onTableHeaderChange', state.tableHeaderForm, prop)
}

const init = () => {
  const tableHeader = props.chart?.customAttr?.tableHeader
  if (tableHeader) {
    state.tableHeaderForm = defaultsDeep(cloneDeep(tableHeader), cloneDeep(DEFAULT_TABLE_HEADER))
    if (!isAlphaColor(state.tableHeaderForm.tableHeaderBgColor)) {
      const alpha = props.chart.customAttr.basicStyle.alpha
      state.tableHeaderForm.tableHeaderBgColor = convertToAlphaColor(
        state.tableHeaderForm.tableHeaderBgColor,
        alpha
      )
    }
  }
}
const showProperty = prop => props.propertyInner?.includes(prop)

onMounted(() => {
  init()
})
</script>

<template>
  <el-form
    :model="state.tableHeaderForm"
    :disabled="!state.tableHeaderForm.showTableHeader"
    ref="tableHeaderForm"
    label-position="top"
  >
    <el-form-item
      :label="t('chart.backgroundColor')"
      class="form-item"
      :class="'form-item-' + themes"
      v-if="showProperty('tableHeaderBgColor')"
    >
      <el-color-picker
        :effect="themes"
        v-model="state.tableHeaderForm.tableHeaderBgColor"
        is-custom
        :trigger-width="108"
        :predefine="predefineColors"
        show-alpha
        @change="changeTableHeader('tableHeaderBgColor')"
      />
    </el-form-item>

    <el-space>
      <el-form-item
        class="form-item"
        :class="'form-item-' + themes"
        v-if="showProperty('tableHeaderFontColor')"
        :label="t('chart.text')"
      >
        <el-color-picker
          :effect="themes"
          v-model="state.tableHeaderForm.tableHeaderFontColor"
          is-custom
          :predefine="predefineColors"
          @change="changeTableHeader('tableHeaderFontColor')"
        />
      </el-form-item>
      <el-form-item
        class="form-item"
        :class="'form-item-' + themes"
        v-if="showProperty('tableTitleFontSize')"
      >
        <template #label>&nbsp;</template>
        <el-select
          style="width: 58px"
          :effect="themes"
          v-model="state.tableHeaderForm.tableTitleFontSize"
          @change="changeTableHeader('tableTitleFontSize')"
        >
          <el-option
            v-for="option in fontSizeList"
            :key="option.value"
            :label="option.name"
            :value="option.value"
          />
        </el-select>
      </el-form-item>
    </el-space>
    <el-space>
      <el-form-item class="form-item" :class="'form-item-' + themes">
        <el-checkbox
          :effect="themes"
          class="icon-checkbox"
          v-model="state.tableHeaderForm.isBolder"
          @change="changeTableHeader('isBolder')"
        >
          <el-tooltip effect="dark" placement="top">
            <template #content>
              {{ t('chart.bolder') }}
            </template>
            <div
              class="icon-btn"
              :class="{ dark: themes === 'dark', active: state.tableHeaderForm.isBolder }"
            >
              <el-icon>
                <Icon name="icon_bold_outlined" />
              </el-icon>
            </div>
          </el-tooltip>
        </el-checkbox>
      </el-form-item>

      <el-form-item class="form-item" :class="'form-item-' + themes">
        <el-checkbox
          :effect="themes"
          class="icon-checkbox"
          v-model="state.tableHeaderForm.isItalic"
          @change="changeTableHeader('isItalic')"
        >
          <el-tooltip effect="dark" placement="top">
            <template #content>
              {{ t('chart.italic') }}
            </template>
            <div
              class="icon-btn"
              :class="{ dark: themes === 'dark', active: state.tableHeaderForm.isItalic }"
            >
              <el-icon>
                <Icon name="icon_italic_outlined" />
              </el-icon>
            </div>
          </el-tooltip>
        </el-checkbox>
      </el-form-item>

      <div class="position-divider" :class="'position-divider--' + themes"></div>
      <el-form-item
        class="form-item"
        :class="'form-item-' + themes"
        v-if="showProperty('tableHeaderAlign')"
      >
        <el-radio-group
          class="icon-radio-group"
          v-model="state.tableHeaderForm.tableHeaderAlign"
          @change="changeTableHeader('tableHeaderAlign')"
        >
          <el-radio label="left">
            <el-tooltip effect="dark" placement="top">
              <template #content>
                {{ t('chart.text_pos_left') }}
              </template>
              <div
                class="icon-btn"
                :class="{
                  dark: themes === 'dark',
                  active: state.tableHeaderForm.tableHeaderAlign === 'left'
                }"
              >
                <el-icon>
                  <Icon name="icon_left-alignment_outlined" />
                </el-icon>
              </div>
            </el-tooltip>
          </el-radio>
          <el-radio label="center">
            <el-tooltip effect="dark" placement="top">
              <template #content>
                {{ t('chart.text_pos_center') }}
              </template>
              <div
                class="icon-btn"
                :class="{
                  dark: themes === 'dark',
                  active: state.tableHeaderForm.tableHeaderAlign === 'center'
                }"
              >
                <el-icon>
                  <Icon name="icon_center-alignment_outlined" />
                </el-icon>
              </div>
            </el-tooltip>
          </el-radio>
          <el-radio label="right">
            <el-tooltip effect="dark" placement="top">
              <template #content>
                {{ t('chart.text_pos_right') }}
              </template>
              <div
                class="icon-btn"
                :class="{
                  dark: themes === 'dark',
                  active: state.tableHeaderForm.tableHeaderAlign === 'right'
                }"
              >
                <el-icon>
                  <Icon name="icon_right-alignment_outlined" />
                </el-icon>
              </div>
            </el-tooltip>
          </el-radio>
        </el-radio-group>
      </el-form-item>
    </el-space>
    <el-row :gutter="8">
      <el-col :span="12">
        <el-form-item
          :label="t('visualization.lineHeight')"
          class="form-item"
          :class="'form-item-' + themes"
          v-if="showProperty('tableTitleHeight')"
        >
          <el-input-number
            :effect="themes"
            controls-position="right"
            v-model="state.tableHeaderForm.tableTitleHeight"
            :min="20"
            :max="100"
            @change="changeTableHeader('tableTitleHeight')"
          />
        </el-form-item>
      </el-col>
    </el-row>

    <el-form-item class="form-item" :class="'form-item-' + themes" v-if="showProperty('showIndex')">
      <el-checkbox
        size="small"
        :effect="themes"
        v-model="state.tableHeaderForm.showIndex"
        @change="changeTableHeader('showIndex')"
      >
        {{ t('chart.table_show_index') }}
      </el-checkbox>
    </el-form-item>
    <el-form-item
      :label="t('chart.table_index_desc')"
      class="form-item"
      :class="'form-item-' + themes"
      v-if="showProperty('showIndex') && state.tableHeaderForm.showIndex"
    >
      <el-input
        :effect="themes"
        v-model="state.tableHeaderForm.indexLabel"
        @blur="changeTableHeader('indexLabel')"
      />
    </el-form-item>
    <el-form-item
      class="form-item"
      :class="'form-item-' + themes"
      v-if="showProperty('tableHeaderSort')"
    >
      <el-checkbox
        size="small"
        :effect="themes"
        v-model="state.tableHeaderForm.tableHeaderSort"
        @change="changeTableHeader('tableHeaderSort')"
      >
        {{ t('chart.table_header_sort') }}
      </el-checkbox>
    </el-form-item>
    <el-form-item
      class="form-item"
      :class="'form-item-' + themes"
      v-if="showProperty('showHorizonBorder')"
    >
      <el-checkbox
        size="small"
        :effect="themes"
        v-model="state.tableHeaderForm.showHorizonBorder"
        @change="changeTableHeader('showHorizonBorder')"
      >
        {{ t('chart.table_header_show_horizon_border') }}
      </el-checkbox>
    </el-form-item>
    <el-form-item
      class="form-item"
      :class="'form-item-' + themes"
      v-if="showProperty('showVerticalBorder')"
    >
      <el-checkbox
        size="small"
        :effect="themes"
        v-model="state.tableHeaderForm.showVerticalBorder"
        @change="changeTableHeader('showVerticalBorder')"
      >
        {{ t('chart.table_header_show_vertical_border') }}
      </el-checkbox>
    </el-form-item>
  </el-form>
</template>

<style lang="less" scoped>
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
  margin-bottom: 8px;
  background: rgba(31, 35, 41, 0.15);

  &.position-divider--dark {
    background: rgba(235, 235, 235, 0.15);
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
</style>
