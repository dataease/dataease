<script lang="tsx" setup>
import { reactive, watch } from 'vue'
import { useI18n } from '@/hooks/web/useI18n'
import { COLOR_PANEL, DEFAULT_LEGEND_STYLE } from '@/views/chart/components/editor/util/chart'

const { t } = useI18n()

const props = defineProps({
  chart: {
    type: Object,
    required: true
  },
  themes: {
    type: String,
    default: 'dark'
  },
  propertyInner: {
    type: Array<string>
  }
})

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
  legendForm: JSON.parse(JSON.stringify(DEFAULT_LEGEND_STYLE)),
  fontSize: []
})

const initFontSize = () => {
  const arr = []
  for (let i = 10; i <= 40; i = i + 2) {
    arr.push({
      name: i + '',
      value: i
    })
  }
  state.fontSize = arr
}

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
initFontSize()
init()
</script>

<template>
  <div style="width: 100%">
    <el-col>
      <el-form
        ref="legendForm"
        :disabled="!state.legendForm.show"
        :model="state.legendForm"
        size="small"
        label-position="top"
      >
        <el-form-item :label="t('chart.icon')" class="form-item" v-if="showProperty('icon')">
          <el-select
            style="width: 100%"
            :effect="props.themes"
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

        <div class="custom-form-item-label">{{ t('chart.text') }}</div>
        <div style="display: flex">
          <el-form-item class="form-item" v-if="showProperty('color')" style="padding-right: 4px">
            <el-color-picker
              v-model="state.legendForm.color"
              class="color-picker-style"
              :predefine="predefineColors"
              @change="changeLegendStyle('color')"
              is-custom
            />
          </el-form-item>

          <el-form-item class="form-item" v-if="showProperty('fontSize')" style="padding-left: 4px">
            <el-select
              style="width: 108px"
              :effect="props.themes"
              v-model="state.legendForm.fontSize"
              :placeholder="t('chart.text_fontsize')"
              size="small"
              @change="changeLegendStyle('fontSize')"
            >
              <el-option
                v-for="option in state.fontSize"
                :key="option.value"
                :label="option.name"
                :value="option.value"
              />
            </el-select>
          </el-form-item>
        </div>
        <el-form-item :label="t('chart.orient')" class="form-item" v-if="showProperty('orient')">
          <el-radio-group
            v-model="state.legendForm.orient"
            size="small"
            @change="changeLegendStyle('orient')"
          >
            <el-radio :effect="props.themes" label="horizontal">{{
              t('chart.horizontal')
            }}</el-radio>
            <el-radio :effect="props.themes" label="vertical">{{ t('chart.vertical') }}</el-radio>
          </el-radio-group>
        </el-form-item>

        <template v-if="showProperty('hPosition')">
          <div class="custom-form-item-label">{{ t('chart.text_h_position') }}</div>

          <el-space wrap style="margin-bottom: 16px">
            <el-tooltip effect="dark" placement="top">
              <template #content>
                {{ t('chart.text_pos_left') }}
              </template>
              <div
                class="icon-btn"
                :class="{ dark: themes === 'dark', active: state.legendForm.hPosition === 'left' }"
                @click="setHPosition('left')"
              >
                <el-icon>
                  <Icon name="icon_left-align_outlined" />
                </el-icon>
              </div>
            </el-tooltip>

            <el-tooltip
              effect="dark"
              placement="top"
              v-if="state.legendForm.vPosition !== 'center'"
            >
              <template #content>
                {{ t('chart.text_pos_center') }}
              </template>
              <div
                class="icon-btn"
                :class="{
                  dark: themes === 'dark',
                  active: state.legendForm.hPosition === 'center'
                }"
                @click="setHPosition('center')"
              >
                <el-icon>
                  <Icon name="icon_horizontal-align_outlined" />
                </el-icon>
              </div>
            </el-tooltip>

            <el-tooltip effect="dark" placement="top">
              <template #content>
                {{ t('chart.text_pos_right') }}
              </template>
              <div
                class="icon-btn"
                :class="{ dark: themes === 'dark', active: state.legendForm.hPosition === 'right' }"
                @click="setHPosition('right')"
              >
                <el-icon>
                  <Icon name="icon_right-align_outlined" />
                </el-icon>
              </div>
            </el-tooltip>
          </el-space>
        </template>

        <template v-if="showProperty('vPosition')">
          <div class="custom-form-item-label">{{ t('chart.text_v_position') }}</div>

          <el-space wrap style="margin-bottom: 16px">
            <el-tooltip effect="dark" placement="top">
              <template #content>
                {{ t('chart.text_pos_left') }}
              </template>
              <div
                class="icon-btn"
                :class="{ dark: themes === 'dark', active: state.legendForm.vPosition === 'top' }"
                @click="setVPosition('top')"
              >
                <el-icon>
                  <Icon name="icon_top-align_outlined" />
                </el-icon>
              </div>
            </el-tooltip>

            <el-tooltip
              effect="dark"
              placement="top"
              v-if="state.legendForm.hPosition !== 'center'"
            >
              <template #content>
                {{ t('chart.text_pos_center') }}
              </template>
              <div
                class="icon-btn"
                :class="{
                  dark: themes === 'dark',
                  active: state.legendForm.vPosition === 'center'
                }"
                @click="setVPosition('center')"
              >
                <el-icon>
                  <Icon name="icon_vertical-align_outlined" />
                </el-icon>
              </div>
            </el-tooltip>

            <el-tooltip effect="dark" placement="top">
              <template #content>
                {{ t('chart.text_pos_right') }}
              </template>
              <div
                class="icon-btn"
                :class="{
                  dark: themes === 'dark',
                  active: state.legendForm.vPosition === 'bottom'
                }"
                @click="setVPosition('bottom')"
              >
                <el-icon>
                  <Icon name="icon_bottom-align_outlined" />
                </el-icon>
              </div>
            </el-tooltip>
          </el-space>
        </template>
      </el-form>
    </el-col>
  </div>
</template>

<style lang="less" scoped>
:deep(.ed-color-picker.is-custom .ed-color-picker__trigger) {
  height: 24px;
}
.custom-form-item-label {
  margin-bottom: 4px;
  line-height: 20px;
  color: #a6a6a6;
  font-size: 12px;
  padding: 2px 12px 0 0;
}
.form-item-checkbox {
  margin-bottom: 10px !important;
}

.icon-btn {
  font-size: 16px;
  width: 24px;
  height: 24px;
  text-align: center;
  border-radius: 4px;
  padding-top: 1px;

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
</style>
