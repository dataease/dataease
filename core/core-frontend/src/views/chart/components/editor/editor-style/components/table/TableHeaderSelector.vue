<script lang="ts" setup>
import { computed, onMounted, PropType, reactive, watch } from 'vue'
import { useI18n } from '@/hooks/web/useI18n'
import { COLOR_PANEL, DEFAULT_TABLE_HEADER } from '@/views/chart/components/editor/util/chart'
import { ElSpace } from 'element-plus-secondary'

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
  tableHeaderForm: JSON.parse(JSON.stringify(DEFAULT_TABLE_HEADER))
})

const emit = defineEmits(['onTableHeaderChange'])

const changeTableHeader = val => {
  emit('onTableHeaderChange', state.tableHeaderForm)
}

const init = () => {
  const chart = JSON.parse(JSON.stringify(props.chart))
  if (chart.customAttr) {
    if (chart.customAttr.label) {
      state.tableHeaderForm = chart.customAttr.tableHeader
    }
  }
}
const showProperty = prop => props.propertyInner?.includes(prop)

onMounted(() => {
  init()
})
</script>

<template>
  <el-form ref="tableHeaderForm" :model="state.tableHeaderForm" label-position="top">
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

      <el-form-item
        class="form-item"
        :class="'form-item-' + themes"
        v-if="showProperty('tableHeaderAlign')"
      >
        <template #label>&nbsp;</template>

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

    <el-form-item
      :label="t('chart.table_show_index')"
      class="form-item"
      :class="'form-item-' + themes"
      v-if="showProperty('showIndex')"
    >
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
      color: #3370ff;
      background-color: rgba(51, 112, 255, 0.1);
    }
    &:hover {
      background-color: rgba(255, 255, 255, 0.1);
    }
  }

  &.active {
    color: #3370ff;
    background-color: rgba(51, 112, 255, 0.1);
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
</style>
