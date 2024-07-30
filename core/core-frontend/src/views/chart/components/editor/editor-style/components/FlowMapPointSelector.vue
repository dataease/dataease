<script lang="tsx" setup>
import { computed, onMounted, PropType, reactive, watch } from 'vue'
import { useI18n } from '@/hooks/web/useI18n'
import { COLOR_PANEL, DEFAULT_MISC } from '@/views/chart/components/editor/util/chart'
import { dvMainStoreWithOut } from '@/store/modules/data-visualization/dvMain'
import { ElSpace } from 'element-plus-secondary'

const { t } = useI18n()
const dvMainStore = dvMainStoreWithOut()
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
  pointForm: {
    ...JSON.parse(JSON.stringify(DEFAULT_MISC.flowMapConfig.pointConfig))
  }
})
const toolTip = computed(() => {
  return props.themes === 'dark' ? 'ndark' : 'dark'
})
const emit = defineEmits(['onChangeFlowMapPointForm'])

watch(
  () => props.chart.customAttr.misc.flowMapConfig.pointConfig,
  () => {
    init()
  },
  { deep: true }
)

const changeStyle = () => {
  emit('onChangeFlowMapPointForm', state.pointForm)
}

const init = () => {
  const chart = JSON.parse(JSON.stringify(props.chart))
  if (chart.customAttr) {
    let customAttr = null
    if (Object.prototype.toString.call(chart.customAttr) === '[object Object]') {
      customAttr = JSON.parse(JSON.stringify(chart.customAttr))
    } else {
      customAttr = JSON.parse(chart.customAttr)
    }
    if (customAttr.misc.flowMapConfig.lineConfig) {
      state.pointForm = customAttr.misc.flowMapConfig.pointConfig
    } else {
      // 新增图表
      state.pointForm = {
        ...JSON.parse(JSON.stringify(DEFAULT_MISC.flowMapConfig.pointConfig))
      }
      changeStyle()
    }
  }
}

const showProperty = prop => props.propertyInner?.includes(prop)
onMounted(() => {
  init()
})
</script>

<template>
  <el-form ref="pointForm" :model="state.pointForm" size="small" label-position="top">
    <div style="padding-bottom: 8px">
      <el-space>
        <el-form-item class="form-item" :class="'form-item-' + themes" :label="t('chart.text')">
          <el-color-picker
            :effect="themes"
            size="default"
            v-model="state.pointForm.text.color"
            class="color-picker-style"
            :predefine="predefineColors"
            @change="changeStyle()"
            is-custom
          />
        </el-form-item>

        <el-form-item class="form-item" :class="'form-item-' + themes">
          <template #label>&nbsp;</template>
          <el-tooltip content="字号" :effect="toolTip" placement="top">
            <el-select
              size="small"
              style="width: 108px"
              :effect="themes"
              v-model.number="state.pointForm.text.fontSize"
              :placeholder="t('chart.text_fontsize')"
              @change="changeStyle()"
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
      </el-space>
    </div>
    <el-space>
      <el-form-item class="form-item" :class="'form-item-' + themes" label="标注点气泡颜色">
        <el-color-picker
          :effect="themes"
          size="default"
          v-model="state.pointForm.point.color"
          class="color-picker-style"
          :predefine="predefineColors"
          @change="changeStyle()"
          is-custom
        />
      </el-form-item>
    </el-space>
    <div class="alpha-setting">
      <label class="alpha-label" :class="{ dark: 'dark' === themes }"> 标注点气泡大小 </label>
      <el-row style="flex: 1">
        <el-col>
          <el-form-item class="form-item alpha-slider" :class="'form-item-' + themes">
            <el-slider
              :effect="themes"
              :min="0"
              :max="20"
              v-model="state.pointForm.point.size"
              @change="changeStyle()"
            />
          </el-form-item>
        </el-col>
      </el-row>
    </div>
  </el-form>
</template>

<style lang="less" scoped>
:deep(.ed-input .ed-select__prefix--light) {
  padding-right: 6px;
}

.alpha-setting {
  display: flex;
  width: 100%;

  .alpha-slider {
    padding: 4px 8px;

    :deep(.ed-slider__button-wrapper) {
      --ed-slider-button-wrapper-size: 36px;
      --ed-slider-button-size: 16px;
    }
  }

  .alpha-label {
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
