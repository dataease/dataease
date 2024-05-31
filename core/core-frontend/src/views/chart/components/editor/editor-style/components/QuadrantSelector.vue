<script lang="tsx" setup>
import { computed, inject, onMounted, PropType, reactive, ref, watch } from 'vue'
import { useI18n } from '@/hooks/web/useI18n'
import { COLOR_PANEL, DEFAULT_QUADRANT_STYLE } from '@/views/chart/components/editor/util/chart'
import { useEmitt } from '@/hooks/web/useEmitt'
import { dvMainStoreWithOut } from '@/store/modules/data-visualization/dvMain'
import { storeToRefs } from 'pinia'
import { deepCopy } from '@/utils/utils'
useEmitt({
  name: 'quadrant-default-baseline',
  callback: args => quadrantDefaultBaseline(args)
})
const quotaData = ref<Axis[]>(inject('quotaData'))
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
const regionStyle = []
const labels = []
const isDashboard = dvMainStore.dvInfo.type === 'dashboard'
for (let i = 0; i < 4; i++) {
  regionStyle.push({
    fill: isDashboard ? '#ffffff' : 'rgb(2,4,8,1,1)',
    fillOpacity: '1.0'
  })
  labels.push({
    content: '',
    style: {
      fill: isDashboard ? 'rgb(2,4,8,1,1)' : '#ffffff',
      fillOpacity: '0.5',
      fontSize: 14
    }
  })
}
const state = reactive({
  quadrantForm: {
    ...JSON.parse(JSON.stringify(DEFAULT_QUADRANT_STYLE)),
    regionStyle,
    labels
  }
})
const toolTip = computed(() => {
  return props.themes === 'dark' ? 'ndark' : 'dark'
})
const emit = defineEmits(['onChangeQuadrantForm'])

watch(
  () => props.chart.customAttr.quadrant,
  () => {
    init()
  },
  { deep: true }
)

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

const fillOpacityList = computed(() => {
  const arr = []
  for (let i = 0; i <= 1; i = i + 0.1) {
    let c = i.toFixed(1)
    arr.push({
      name: c + '',
      value: c
    })
  }
  return arr
})

const changeStyle = () => {
  emit('onChangeQuadrantForm', state.quadrantForm)
}
const quadrantDefaultBaseline = quadrant => {
  state.quadrantForm.xBaseline = quadrant.xBaseline
  state.quadrantForm.yBaseline = quadrant.yBaseline
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
    if (customAttr.quadrant) {
      state.quadrantForm = customAttr.quadrant
    } else {
      // 新增图表
      state.quadrantForm = {
        ...JSON.parse(JSON.stringify(DEFAULT_QUADRANT_STYLE)),
        regionStyle,
        labels
      }
      state.quadrantForm.xBaseline = undefined
      state.quadrantForm.yBaseline = undefined
      changeStyle()
    }
  }
}

const showProperty = prop => props.propertyInner?.includes(prop)
const tabActive = ref(1)
onMounted(() => {
  init()
})
</script>

<template>
  <el-form
    ref="quadrantForm"
    class="quadrant"
    :model="state.quadrantForm"
    size="small"
    label-position="top"
  >
    <template v-if="showProperty('lineStyle')">
      <div style="display: flex">
        <el-form-item
          :label="t('chart.split_line')"
          class="form-item"
          :class="'form-item-' + themes"
        >
          <el-color-picker
            v-model="state.quadrantForm.lineStyle.stroke"
            class="color-picker-style"
            :predefine="predefineColors"
            @change="changeStyle()"
            :effect="themes"
            is-custom
          />
        </el-form-item>
        <el-form-item class="form-item" :class="'form-item-' + themes" style="padding-left: 4px">
          <template #label>&nbsp;</template>
          <el-tooltip :content="t('chart.not_alpha')" :effect="toolTip" placement="top">
            <el-select
              style="width: 53px"
              :effect="props.themes"
              v-model="state.quadrantForm.lineStyle.opacity"
              :placeholder="t('chart.not_alpha')"
              @change="changeStyle()"
            >
              <el-option
                v-for="option in fillOpacityList"
                :key="option.value"
                :label="option.name"
                :value="option.value"
              />
            </el-select>
          </el-tooltip>
        </el-form-item>
        <el-form-item class="form-item" :class="'form-item-' + themes" style="padding-left: 4px">
          <template #label>&nbsp;</template>
          <el-tooltip :content="t('chart.funnel_width')" :effect="toolTip" placement="top">
            <el-input-number
              style="width: 108px"
              :effect="props.themes"
              v-model="state.quadrantForm.lineStyle.lineWidth"
              :min="1"
              :max="10"
              size="small"
              controls-position="right"
              @change="changeStyle()"
            />
          </el-tooltip>
        </el-form-item>
      </div>
      <div style="display: flex">
        <el-form-item class="form-item" label="X 轴恒线" :class="'form-item-' + themes">
          <el-input-number
            controls-position="right"
            style="width: 100%"
            :effect="props.themes"
            v-model="state.quadrantForm.xBaseline"
            :precision="2"
            :step="0.01"
            size="small"
            @change="changeStyle()"
          />
        </el-form-item>
        <el-form-item
          class="form-item"
          label="Y 轴恒线"
          :class="'form-item-' + themes"
          style="padding-left: 4px"
        >
          <el-input-number
            controls-position="right"
            style="width: 100%"
            :effect="props.themes"
            v-model="state.quadrantForm.yBaseline"
            :precision="2"
            :step="0.01"
            size="small"
            @change="changeStyle()"
          />
        </el-form-item>
      </div>
    </template>
    <el-tabs v-model="tabActive" class="tab-header" :class="{ dark: themes === 'dark' }">
      <el-tab-pane
        :name="index + 1"
        v-for="(l, index) in state.quadrantForm.labels"
        :key="index"
        :label="t('chart.quadrant') + (index + 1)"
        class="padding-tab"
      >
        <div style="margin-top: 8px">
          <template v-if="showProperty('regionStyle')">
            <div style="display: flex">
              <el-form-item
                class="form-item"
                :class="'form-item-' + themes"
                :label="t('chart.backgroundColor')"
              >
                <el-color-picker
                  v-model="state.quadrantForm.regionStyle[index].fill"
                  class="color-picker-style"
                  :predefine="predefineColors"
                  @change="changeStyle()"
                  :effect="themes"
                  is-custom
                />
              </el-form-item>
              <el-form-item
                class="form-item"
                :class="'form-item-' + themes"
                style="padding-left: 4px"
              >
                <template #label>&nbsp;</template>
                <el-tooltip :content="t('chart.not_alpha')" :effect="toolTip" placement="top">
                  <el-select
                    style="width: 53px"
                    :effect="props.themes"
                    v-model="state.quadrantForm.regionStyle[index].fillOpacity"
                    :placeholder="t('chart.not_alpha')"
                    @change="changeStyle()"
                  >
                    <el-option
                      v-for="option in fillOpacityList"
                      :key="option.value"
                      :label="option.name"
                      :value="option.value"
                    />
                  </el-select>
                </el-tooltip>
              </el-form-item>
            </div>
          </template>
          <template v-if="showProperty('label')">
            <el-form-item class="form-item" :class="'form-item-' + themes" :label="t('chart.text')">
              <el-input
                :effect="props.themes"
                v-model="l.content"
                size="small"
                maxlength="50"
                @blur="changeStyle()"
              />
            </el-form-item>
            <div style="display: flex">
              <el-form-item
                class="form-item"
                :class="'form-item-' + themes"
                :label="t('chart.chart_style')"
              >
                <el-color-picker
                  v-model="l.style.fill"
                  class="color-picker-style"
                  :predefine="predefineColors"
                  @change="changeStyle()"
                  :effect="themes"
                  is-custom
                />
              </el-form-item>
              <el-form-item
                class="form-item"
                :class="'form-item-' + themes"
                style="padding-left: 4px"
              >
                <template #label>&nbsp;</template>
                <el-tooltip :content="t('chart.not_alpha')" :effect="toolTip" placement="top">
                  <el-select
                    style="width: 53px"
                    :effect="props.themes"
                    v-model="l.style.fillOpacity"
                    :placeholder="t('chart.not_alpha')"
                    @change="changeStyle()"
                  >
                    <el-option
                      v-for="option in fillOpacityList"
                      :key="option.value"
                      :label="option.name"
                      :value="option.value"
                    />
                  </el-select>
                </el-tooltip>
              </el-form-item>
              <el-form-item
                class="form-item"
                :class="'form-item-' + themes"
                style="padding-left: 4px"
              >
                <template #label>&nbsp;</template>
                <el-tooltip :content="t('chart.font_size')" :effect="toolTip" placement="top">
                  <el-select
                    style="width: 108px"
                    :effect="props.themes"
                    v-model="l.style.fontSize"
                    :placeholder="t('chart.axis_name_fontsize')"
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
            </div>
          </template>
        </div>
      </el-tab-pane>
    </el-tabs>
  </el-form>
</template>

<style lang="less" scoped>
.quadrant {
  .custom-form-item-label {
    margin-bottom: 4px;
    line-height: 20px;
    color: #646a73;
    font-size: 12px;
    font-style: normal;
    font-weight: 400;
    padding: 2px 12px 0 0;

    &.custom-form-item-label--dark {
      color: #a6a6a6;
    }
  }

  .form-item-checkbox {
    margin-bottom: 10px !important;
  }

  .m-divider {
    border-color: rgba(31, 35, 41, 0.15);
    margin: 0 0 16px;

    &.m-divider--dark {
      border-color: rgba(235, 235, 235, 0.15);
    }
  }
  .padding-tab {
    padding: 0;
    height: 100%;
    width: 100%;
    display: flex;

    :deep(.ed-scrollbar) {
      &.has-footer {
        height: calc(100% - 81px);
      }
    }

    :deep(.ed-footer) {
      padding: 0;
      height: 114px;
    }
  }
}
</style>
