<script lang="tsx" setup>
import { computed, onMounted, reactive, watch } from 'vue'
import { useI18n } from '@/hooks/web/useI18n'
import { COLOR_PANEL, DEFAULT_LEGEND_STYLE } from '@/views/chart/components/editor/util/chart'

const { t } = useI18n()

const props = withDefaults(
  defineProps<{
    chart: any
    themes?: EditorTheme
    propertyInner: Array<string>
  }>(),
  { themes: 'dark' }
)

const emit = defineEmits(['onLegendChange'])

watch(
  () => props.chart.customStyle,
  () => {
    init()
  },
  { deep: true }
)

const predefineColors = COLOR_PANEL
const iconSymbolOptions = [
  { name: t('chart.line_symbol_circle'), value: 'circle' },
  { name: t('chart.line_symbol_rect'), value: 'square' },
  { name: t('chart.line_symbol_triangle'), value: 'triangle' },
  { name: t('chart.line_symbol_diamond'), value: 'diamond' }
]

const state = reactive({
  legendForm: JSON.parse(JSON.stringify(DEFAULT_LEGEND_STYLE))
})

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

const changeLegendStyle = prop => {
  emit('onLegendChange', state.legendForm)
}

function setHPosition(p: 'left' | 'center' | 'right') {
  state.legendForm.hPosition = p
  changeLegendStyle('hPosition')
}
function setVPosition(p: 'top' | 'center' | 'bottom') {
  state.legendForm.vPosition = p
  changeLegendStyle('vPosition')
}

const init = () => {
  const chart = JSON.parse(JSON.stringify(props.chart))
  if (chart.customStyle) {
    let customStyle = null
    if (Object.prototype.toString.call(chart.customStyle) === '[object Object]') {
      customStyle = JSON.parse(JSON.stringify(chart.customStyle))
    } else {
      customStyle = JSON.parse(chart.customStyle)
    }
    if (customStyle.legend) {
      state.legendForm = customStyle.legend
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
    ref="legendForm"
    :disabled="!state.legendForm.show"
    :model="state.legendForm"
    label-position="top"
  >
    <el-form-item
      :label="t('chart.icon')"
      class="form-item"
      :class="'form-item-' + themes"
      v-if="showProperty('icon')"
    >
      <el-select
        :effect="themes"
        v-model="state.legendForm.icon"
        :placeholder="t('chart.icon')"
        @change="changeLegendStyle('icon')"
      >
        <el-option
          v-for="item in iconSymbolOptions"
          :key="item.value"
          :label="item.name"
          :value="item.value"
        />
      </el-select>
    </el-form-item>

    <el-space>
      <el-form-item
        class="form-item"
        :class="'form-item-' + themes"
        v-if="showProperty('color')"
        :label="t('chart.text')"
      >
        <el-color-picker
          v-model="state.legendForm.color"
          class="color-picker-style"
          :predefine="predefineColors"
          @change="changeLegendStyle('color')"
          is-custom
        />
      </el-form-item>

      <el-form-item
        class="form-item"
        :class="'form-item-' + themes"
        v-if="showProperty('fontSize')"
      >
        <template #label> &nbsp; </template>
        <el-select
          :effect="themes"
          v-model="state.legendForm.fontSize"
          :placeholder="t('chart.text_fontsize')"
          size="small"
          @change="changeLegendStyle('fontSize')"
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
    <el-form-item
      :label="t('chart.orient')"
      class="form-item"
      :class="'form-item-' + themes"
      v-if="showProperty('orient')"
    >
      <el-radio-group
        v-model="state.legendForm.orient"
        size="small"
        @change="changeLegendStyle('orient')"
      >
        <el-radio :effect="themes" label="horizontal">{{ t('chart.horizontal') }}</el-radio>
        <el-radio :effect="themes" label="vertical">{{ t('chart.vertical') }}</el-radio>
      </el-radio-group>
    </el-form-item>

    <el-form-item
      :label="t('chart.text_h_position')"
      class="form-item"
      :class="'form-item-' + themes"
      v-if="showProperty('hPosition')"
    >
      <el-radio-group
        class="icon-radio-group"
        v-model="state.legendForm.hPosition"
        @change="changeLegendStyle('hPosition')"
      >
        <el-radio label="left">
          <el-tooltip effect="dark" placement="top">
            <template #content>
              {{ t('chart.text_pos_left') }}
            </template>
            <div
              class="icon-btn"
              :class="{ dark: themes === 'dark', active: state.legendForm.hPosition === 'left' }"
            >
              <el-icon>
                <Icon name="icon_left-align_outlined" />
              </el-icon>
            </div>
          </el-tooltip>
        </el-radio>
        <el-radio label="center" :disabled="state.legendForm.vPosition === 'center'">
          <el-tooltip effect="dark" placement="top">
            <template #content>
              {{ t('chart.text_pos_center') }}
            </template>
            <div
              class="icon-btn"
              :class="{
                dark: themes === 'dark',
                active: state.legendForm.hPosition === 'center'
              }"
            >
              <el-icon>
                <Icon name="icon_horizontal-align_outlined" />
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
              :class="{ dark: themes === 'dark', active: state.legendForm.hPosition === 'right' }"
            >
              <el-icon>
                <Icon name="icon_right-align_outlined" />
              </el-icon>
            </div>
          </el-tooltip>
        </el-radio>
      </el-radio-group>
    </el-form-item>

    <el-form-item
      :label="t('chart.text_v_position')"
      class="form-item"
      :class="'form-item-' + themes"
      v-if="showProperty('vPosition')"
    >
      <el-radio-group
        class="icon-radio-group"
        v-model="state.legendForm.vPosition"
        @change="changeLegendStyle('vPosition')"
      >
        <el-radio label="top">
          <el-tooltip effect="dark" placement="top">
            <template #content>
              {{ t('chart.text_pos_top') }}
            </template>
            <div
              class="icon-btn"
              :class="{ dark: themes === 'dark', active: state.legendForm.vPosition === 'top' }"
            >
              <el-icon>
                <Icon name="icon_top-align_outlined" />
              </el-icon>
            </div>
          </el-tooltip>
        </el-radio>
        <el-radio label="center" :disabled="state.legendForm.hPosition === 'center'">
          <el-tooltip effect="dark" placement="top">
            <template #content>
              {{ t('chart.text_pos_center') }}
            </template>
            <div
              class="icon-btn"
              :class="{
                dark: themes === 'dark',
                active: state.legendForm.vPosition === 'center'
              }"
            >
              <el-icon>
                <Icon name="icon_vertical-align_outlined" />
              </el-icon>
            </div>
          </el-tooltip>
        </el-radio>
        <el-radio label="bottom">
          <el-tooltip effect="dark" placement="top">
            <template #content>
              {{ t('chart.text_pos_bottom') }}
            </template>
            <div
              class="icon-btn"
              :class="{
                dark: themes === 'dark',
                active: state.legendForm.vPosition === 'bottom'
              }"
            >
              <el-icon>
                <Icon name="icon_bottom-align_outlined" />
              </el-icon>
            </div>
          </el-tooltip>
        </el-radio>
      </el-radio-group>
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
  }
  :deep(.ed-radio__input) {
    display: none;
  }
  :deep(.ed-radio__label) {
    padding: 0;
  }
}
</style>
