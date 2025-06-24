<script lang="ts" setup>
import icon_bold_outlined from '@/assets/svg/icon_bold_outlined.svg'
import icon_italic_outlined from '@/assets/svg/icon_italic_outlined.svg'
import icon_leftAlignment_outlined from '@/assets/svg/icon_left-alignment_outlined.svg'
import icon_centerAlignment_outlined from '@/assets/svg/icon_center-alignment_outlined.svg'
import icon_rightAlignment_outlined from '@/assets/svg/icon_right-alignment_outlined.svg'
import icon_edit_outlined from '@/assets/svg/icon_edit_outlined.svg'
import { computed, onMounted, PropType, reactive, watch } from 'vue'
import { useI18n } from '@/hooks/web/useI18n'
import { COLOR_PANEL, DEFAULT_TABLE_HEADER } from '@/views/chart/components/editor/util/chart'
import { ElDivider, ElSpace } from 'element-plus-secondary'
import { cloneDeep, defaultsDeep, isEqual } from 'lodash-es'
import { convertToAlphaColor, isAlphaColor } from '@/views/chart/components/js/util'
import { dvMainStoreWithOut } from '@/store/modules/data-visualization/dvMain'
import { storeToRefs } from 'pinia'
import TableHeaderGroupConfig from './TableHeaderGroupConfig.vue'
import { getLeafNodes } from '@/views/chart/components/js/panel/common/common_table'

const dvMainStore = dvMainStoreWithOut()
const { batchOptStatus, mobileInPc } = storeToRefs(dvMainStore)
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
  for (let i = 50; i <= 200; i = i + 10) {
    arr.push({
      name: i + '',
      value: i
    })
  }
  return arr
})

const state = reactive({
  tableHeaderForm: {} as ChartTableHeaderAttr,
  showTableHeaderGroupConfig: false
})

const emit = defineEmits(['onTableHeaderChange'])

const changeTableHeader = prop => {
  emit('onTableHeaderChange', state.tableHeaderForm, prop)
}

const changeHeaderGroupConfig = (headerGroupConfig: ChartTableHeaderAttr['headerGroupConfig']) => {
  state.tableHeaderForm.headerGroupConfig = headerGroupConfig
  state.showTableHeaderGroupConfig = false
  changeTableHeader('headerGroupConfig')
}

const enableGroupConfig = computed(() => {
  return (
    !batchOptStatus.value &&
    showProperty('headerGroup') &&
    state.tableHeaderForm.headerGroup &&
    state.tableHeaderForm.showTableHeader !== false
  )
})

const groupConfigValid = computed(() => {
  const columns = props.chart?.customAttr?.tableHeader?.headerGroupConfig?.columns
  if (!columns?.length) {
    return false
  }
  const noGroup = columns.every(item => !item.children?.length)
  if (noGroup) {
    return false
  }
  const allAxis = [...props.chart?.xAxis]
  if (props.chart.type === 'table-normal') {
    allAxis.push(...props.chart?.yAxis)
  }
  const showColumns = []
  allAxis?.forEach(axis => {
    axis.hide !== true && showColumns.push({ key: axis.dataeaseName })
  })
  if (!showColumns.length) {
    return false
  }
  const showColumnFields = showColumns.map(item => item.key)
  const leafNodes = getLeafNodes(columns as Array<ColumnNode>)
  const leafKeys = leafNodes.map(item => item.key)
  return isEqual(showColumnFields, leafKeys)
})
const init = () => {
  const tableHeader = props.chart?.customAttr?.tableHeader
  if (tableHeader) {
    // 存量透视表处理
    if (!tableHeader.tableHeaderColBgColor) {
      tableHeader.tableHeaderColBgColor = tableHeader.tableHeaderBgColor
      tableHeader.tableHeaderColFontColor = tableHeader.tableHeaderFontColor
      tableHeader.tableTitleColFontSize = tableHeader.tableTitleFontSize
      tableHeader.tableHeaderColAlign = tableHeader.tableHeaderAlign
      tableHeader.isColBolder = tableHeader.isBolder
      tableHeader.isColItalic = tableHeader.isItalic

      tableHeader.tableHeaderCornerBgColor = tableHeader.tableHeaderBgColor
      tableHeader.tableHeaderCornerFontColor = tableHeader.tableHeaderFontColor
      tableHeader.tableTitleCornerFontSize = tableHeader.tableTitleFontSize
      tableHeader.tableHeaderCornerAlign = tableHeader.tableHeaderAlign
      tableHeader.isCornerBolder = tableHeader.isBolder
      tableHeader.isCornerItalic = tableHeader.isItalic
    }
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
    size="small"
  >
    <el-form-item
      :label="
        chart.type === 'table-pivot' ? t('chart.rowBackgroundColor') : t('chart.backgroundColor')
      "
      class="form-item"
      :class="'form-item-' + themes"
      v-if="showProperty('tableHeaderBgColor') && state.tableHeaderForm.tableHeaderBgColor"
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
    <el-space :class="{ 'mobile-style': mobileInPc }">
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
                <Icon name="icon_italic_outlined"><icon_italic_outlined class="svg-icon" /></Icon>
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
                  <Icon name="icon_left-alignment_outlined"
                    ><icon_leftAlignment_outlined class="svg-icon"
                  /></Icon>
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
                  <Icon name="icon_center-alignment_outlined"
                    ><icon_centerAlignment_outlined class="svg-icon"
                  /></Icon>
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

    <template v-if="chart.type === 'table-pivot' && showProperty('tableHeaderBgColor')">
      <el-divider class="m-divider" :class="{ 'divider-dark': themes === 'dark' }" />
      <el-form-item
        :label="t('chart.colBackgroundColor')"
        class="form-item"
        :class="'form-item-' + themes"
      >
        <el-color-picker
          :effect="themes"
          v-model="state.tableHeaderForm.tableHeaderColBgColor"
          is-custom
          :trigger-width="108"
          :predefine="predefineColors"
          show-alpha
          @change="changeTableHeader('tableHeaderColBgColor')"
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
            v-model="state.tableHeaderForm.tableHeaderColFontColor"
            is-custom
            :predefine="predefineColors"
            @change="changeTableHeader('tableHeaderColFontColor')"
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
            v-model="state.tableHeaderForm.tableTitleColFontSize"
            @change="changeTableHeader('tableTitleColFontSize')"
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
      <el-space :class="{ 'mobile-style': mobileInPc }">
        <el-form-item class="form-item" :class="'form-item-' + themes">
          <el-checkbox
            :effect="themes"
            class="icon-checkbox"
            v-model="state.tableHeaderForm.isColBolder"
            @change="changeTableHeader('isColBolder')"
          >
            <el-tooltip effect="dark" placement="top">
              <template #content>
                {{ t('chart.bolder') }}
              </template>
              <div
                class="icon-btn"
                :class="{ dark: themes === 'dark', active: state.tableHeaderForm.isColBolder }"
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
            v-model="state.tableHeaderForm.isColItalic"
            @change="changeTableHeader('isColItalic')"
          >
            <el-tooltip effect="dark" placement="top">
              <template #content>
                {{ t('chart.italic') }}
              </template>
              <div
                class="icon-btn"
                :class="{ dark: themes === 'dark', active: state.tableHeaderForm.isColItalic }"
              >
                <el-icon>
                  <Icon name="icon_italic_outlined"><icon_italic_outlined class="svg-icon" /></Icon>
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
            v-model="state.tableHeaderForm.tableHeaderColAlign"
            @change="changeTableHeader('tableHeaderColAlign')"
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
                    active: state.tableHeaderForm.tableHeaderColAlign === 'left'
                  }"
                >
                  <el-icon>
                    <Icon name="icon_left-alignment_outlined"
                      ><icon_leftAlignment_outlined class="svg-icon"
                    /></Icon>
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
                    active: state.tableHeaderForm.tableHeaderColAlign === 'center'
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
            <el-radio label="right">
              <el-tooltip effect="dark" placement="top">
                <template #content>
                  {{ t('chart.text_pos_right') }}
                </template>
                <div
                  class="icon-btn"
                  :class="{
                    dark: themes === 'dark',
                    active: state.tableHeaderForm.tableHeaderColAlign === 'right'
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

      <el-divider class="m-divider" :class="{ 'divider-dark': themes === 'dark' }" />
      <el-form-item
        :label="t('chart.cornerBackgroundColor')"
        class="form-item"
        :class="'form-item-' + themes"
      >
        <el-color-picker
          :effect="themes"
          v-model="state.tableHeaderForm.tableHeaderCornerBgColor"
          is-custom
          :trigger-width="108"
          :predefine="predefineColors"
          show-alpha
          @change="changeTableHeader('tableHeaderCornerBgColor')"
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
            v-model="state.tableHeaderForm.tableHeaderCornerFontColor"
            is-custom
            :predefine="predefineColors"
            @change="changeTableHeader('tableHeaderCornerFontColor')"
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
            v-model="state.tableHeaderForm.tableTitleCornerFontSize"
            @change="changeTableHeader('tableTitleCornerFontSize')"
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
      <el-space :class="{ 'mobile-style': mobileInPc }">
        <el-form-item class="form-item" :class="'form-item-' + themes">
          <el-checkbox
            :effect="themes"
            class="icon-checkbox"
            v-model="state.tableHeaderForm.isCornerBolder"
            @change="changeTableHeader('isCornerBolder')"
          >
            <el-tooltip effect="dark" placement="top">
              <template #content>
                {{ t('chart.bolder') }}
              </template>
              <div
                class="icon-btn"
                :class="{ dark: themes === 'dark', active: state.tableHeaderForm.isCornerBolder }"
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
            v-model="state.tableHeaderForm.isCornerItalic"
            @change="changeTableHeader('isCornerItalic')"
          >
            <el-tooltip effect="dark" placement="top">
              <template #content>
                {{ t('chart.italic') }}
              </template>
              <div
                class="icon-btn"
                :class="{ dark: themes === 'dark', active: state.tableHeaderForm.isCornerItalic }"
              >
                <el-icon>
                  <Icon name="icon_italic_outlined"><icon_italic_outlined class="svg-icon" /></Icon>
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
            v-model="state.tableHeaderForm.tableHeaderCornerAlign"
            @change="changeTableHeader('tableHeaderCornerAlign')"
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
                    active: state.tableHeaderForm.tableHeaderCornerAlign === 'left'
                  }"
                >
                  <el-icon>
                    <Icon name="icon_left-alignment_outlined"
                      ><icon_leftAlignment_outlined class="svg-icon"
                    /></Icon>
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
                    active: state.tableHeaderForm.tableHeaderCornerAlign === 'center'
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
            <el-radio label="right">
              <el-tooltip effect="dark" placement="top">
                <template #content>
                  {{ t('chart.text_pos_right') }}
                </template>
                <div
                  class="icon-btn"
                  :class="{
                    dark: themes === 'dark',
                    active: state.tableHeaderForm.tableHeaderCornerAlign === 'right'
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

      <el-divider class="m-divider" :class="{ 'divider-dark': themes === 'dark' }" />
    </template>

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
            :max="1000"
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
    <el-form-item
      class="form-item"
      :class="'form-item-' + themes"
      v-if="showProperty('rowHeaderFreeze')"
    >
      <el-checkbox
        size="small"
        :effect="themes"
        v-model="state.tableHeaderForm.rowHeaderFreeze"
        @change="changeTableHeader('rowHeaderFreeze')"
      >
        {{ t('chart.table_row_header_freeze') }}
      </el-checkbox>
    </el-form-item>
    <el-form-item
      v-if="!batchOptStatus && showProperty('headerGroup')"
      class="form-item"
      :class="'form-item-' + themes"
      :disabled="!state.tableHeaderForm.showTableHeader"
    >
      <el-checkbox
        size="small"
        :effect="themes"
        v-model="state.tableHeaderForm.headerGroup"
        @change="changeTableHeader('headerGroup')"
      >
        {{ t('chart.table_header_group') }}
      </el-checkbox>
    </el-form-item>
    <el-form-item v-if="enableGroupConfig" class="form-item" :class="'form-item-' + themes">
      <div class="header-group-config">
        <span>{{ t('chart.table_header_group_config') }}</span>
        <div class="group-icon">
          <span v-if="groupConfigValid">
            {{ t('visualization.already_setting') }}
          </span>
          <div
            class="icon-btn"
            :class="{
              dark: themes === 'dark'
            }"
          >
            <el-icon @click="state.showTableHeaderGroupConfig = true">
              <Icon>
                <icon_edit_outlined class="svg-icon" />
              </Icon>
            </el-icon>
          </div>
        </div>
      </div>
    </el-form-item>
  </el-form>
  <el-dialog
    v-model="state.showTableHeaderGroupConfig"
    destroy-on-close
    append-to-body
    :effect="themes"
    :show-close="false"
    :class="themes === 'dark' ? 'table-header-group-config-dialog' : ''"
  >
    <template #header>
      {{ t('chart.table_header_group_config') }}
      <span style="font-size: 12px">({{ t('chart.table_header_group_config_tip') }})</span>
    </template>
    <table-header-group-config
      :chart="chart"
      :themes="themes"
      :tableHeaderForm="state.tableHeaderForm"
      @onConfigChange="changeHeaderGroupConfig"
      @onCancelConfig="() => (state.showTableHeaderGroupConfig = false)"
    />
  </el-dialog>
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
.mobile-style {
  margin-top: 25px;
}
.m-divider {
  margin: 0 0 16px;
  border-color: rgba(31, 35, 41, 0.15);

  &.divider-dark {
    border-color: rgba(255, 255, 255, 0.15);
  }
}
.header-group-config {
  display: flex;
  width: 100%;
  justify-content: space-between;
  align-items: center;
  padding-left: 22px;
  font-size: 12px;
  .group-icon {
    display: flex;
    justify-content: center;
    flex-direction: row;
    align-items: center;
  }
}
</style>
<style lang="less">
.table-header-group-config-dialog {
  background-color: #1a1a1a;
  border: 1px solid #2a2a2a;
  .ed-dialog__header,
  .ed-dialog__body {
    color: #a6a6a6;
    background-color: #1a1a1a;
    margin-right: 0;
  }
}
</style>
