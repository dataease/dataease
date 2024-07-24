<script lang="tsx" setup>
import { PropType, reactive, watch } from 'vue'
import { useI18n } from '@/hooks/web/useI18n'

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

const emit = defineEmits(['onBubbleAnimateChange'])

watch(
  () => props.chart.senior.bubbleCfg,
  () => {
    init()
  },
  { deep: true }
)

const state = reactive({
  bubbleAnimateForm: {} as BubbleCfg,
  isAutoBreakLine: false
})

const onBubbleAnimateChange = () => {
  emit('onBubbleAnimateChange', state.bubbleAnimateForm)
}

const changeSpeedSize = v => {
  const _v = parseInt(v)
  if (_v >= 0 && _v <= 5) {
    state.bubbleAnimateForm.speed = _v
  } else if (_v < 0) {
    state.bubbleAnimateForm.speed = 0
  } else if (_v > 5) {
    state.bubbleAnimateForm.speed = 5
  }
  onBubbleAnimateChange()
}

const changeRingsSize = v => {
  const _v = parseInt(v)
  if (_v >= 0 && _v <= 5) {
    state.bubbleAnimateForm.rings = _v
  } else if (_v < 0) {
    state.bubbleAnimateForm.rings = 0
  } else if (_v > 5) {
    state.bubbleAnimateForm.rings = 5
  }
  onBubbleAnimateChange()
}

const init = () => {
  const chart = JSON.parse(JSON.stringify(props.chart))
  if (chart.senior) {
    let senior = null
    if (Object.prototype.toString.call(chart.senior) === '[object Object]') {
      senior = JSON.parse(JSON.stringify(chart.senior))
    } else {
      senior = JSON.parse(chart.senior)
    }
    if (senior.bubbleCfg) {
      state.bubbleAnimateForm = senior.bubbleCfg
    }
  }
}

init()
</script>

<template>
  <div :style="{ width: '100%', display: 'block' }" @keydown.stop @keyup.stop>
    <el-form
      ref="scrollForm"
      :model="state.bubbleAnimateForm"
      :disabled="!state.bubbleAnimateForm.enable"
      label-position="top"
    >
      <div class="bubble-animate-setting">
        <label
          class="bubble-animate-label"
          style="line-height: 20px"
          :class="{ dark: 'dark' === themes }"
        >
          动效类型
        </label>
        <el-row style="flex: 1" :gutter="8">
          <el-col :span="13">
            <el-form-item class="form-item" :class="'form-item-' + themes">
              <el-radio-group
                style="padding-left: 1px"
                size="small"
                :effect="themes"
                v-model="state.bubbleAnimateForm.type"
                @change="onBubbleAnimateChange()"
              >
                <el-radio :effect="themes" label="wave"> 水波 </el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
        </el-row>
      </div>

      <div class="bubble-animate-setting">
        <label class="bubble-animate-label" :class="{ dark: 'dark' === themes }"> 动效速率 </label>
        <el-row style="flex: 1" :gutter="8">
          <el-col :span="13">
            <el-form-item class="form-item bubble-animate-slider" :class="'form-item-' + themes">
              <el-slider
                :effect="themes"
                :min="1"
                :max="5"
                v-model="state.bubbleAnimateForm.speed"
                @change="onBubbleAnimateChange()"
              />
            </el-form-item>
          </el-col>
          <el-col :span="11" style="padding-top: 2px">
            <el-form-item class="form-item" :class="'form-item-' + themes">
              <el-input
                type="number"
                :effect="themes"
                v-model="state.bubbleAnimateForm.speed"
                :min="1"
                :max="5"
                class="basic-input-number"
                :controls="false"
                @change="changeSpeedSize"
              >
              </el-input>
            </el-form-item>
          </el-col>
        </el-row>
      </div>
      <div class="bubble-animate-setting">
        <label class="bubble-animate-label" :class="{ dark: 'dark' === themes }"> 水波环数 </label>
        <el-row style="flex: 1" :gutter="8">
          <el-col :span="13">
            <el-form-item class="form-item bubble-animate-slider" :class="'form-item-' + themes">
              <el-slider
                :effect="themes"
                :min="1"
                :max="5"
                v-model="state.bubbleAnimateForm.rings"
                @change="onBubbleAnimateChange()"
              />
            </el-form-item>
          </el-col>
          <el-col :span="11" style="padding-top: 2px">
            <el-form-item class="form-item" :class="'form-item-' + themes">
              <el-input
                type="number"
                :effect="themes"
                v-model="state.bubbleAnimateForm.rings"
                :min="1"
                :max="5"
                class="basic-input-number"
                :controls="false"
                @change="changeRingsSize"
              >
              </el-input>
            </el-form-item>
          </el-col>
        </el-row>
      </div>
    </el-form>
  </div>
</template>

<style lang="less" scoped>
.basic-input-number {
  :deep(input) {
    -webkit-appearance: none;
    -moz-appearance: textfield;

    &::-webkit-inner-spin-button,
    &::-webkit-outer-spin-button {
      -webkit-appearance: none;
    }
  }
}
.bubble-animate-setting {
  display: flex;
  width: 100%;

  .bubble-animate-slider {
    padding: 0 8px;
    :deep(.ed-slider__button-wrapper) {
      --ed-slider-button-wrapper-size: 36px;
      --ed-slider-button-size: 16px;
    }
  }

  .bubble-animate-label {
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
</style>
