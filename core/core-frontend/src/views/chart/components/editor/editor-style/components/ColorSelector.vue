<script lang="tsx" setup>
import { reactive, toRefs } from 'vue'
import { useI18n } from '@/hooks/web/useI18n'
import {
  COLOR_PANEL,
  DEFAULT_COLOR_CASE,
  COLOR_CASES
} from '@/views/chart/components/editor/util/chart'

const { t } = useI18n()

const props = defineProps({
  chart: {
    type: Object,
    required: true
  }
})

const colorCases = COLOR_CASES

const predefineColors = COLOR_PANEL

const state = reactive({
  colorForm: JSON.parse(JSON.stringify(DEFAULT_COLOR_CASE)),
  customColor: null,
  colorIndex: 0
})

const emit = defineEmits(['onColorChange'])

const changeColorOption = () => {
  const items = colorCases.filter(ele => {
    return ele.value === state.colorForm.value
  })
  state.colorForm.colors = [...items[0].colors]

  state.customColor = state.colorForm.colors[0]
  state.colorIndex = 0

  changeColorCase()
}

const resetCustomColor = () => {
  changeColorOption()
}

const switchColor = index => {
  state.colorIndex = index
}

const switchColorCase = () => {
  state.colorForm.colors[state.colorIndex] = state.customColor
  changeColorCase()
}

const changeColorCase = () => {
  emit('onColorChange', state.colorForm)
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
    if (customAttr.color) {
      state.colorForm = customAttr.color
      if (!state.customColor) {
        state.customColor = state.colorForm.colors[0]
        state.colorIndex = 0
      }
    }
  }
}

init()
</script>

<template>
  <div style="width: 100%">
    <el-col>
      <el-form ref="colorForm" :model="state.colorForm" label-width="80px" size="small">
        <div>
          <el-form-item :label="t('chart.color_case')" class="form-item">
            <el-popover placement="bottom" width="400" trigger="click">
              <template #reference>
                <div :style="{ cursor: 'pointer', marginTop: '2px', width: '180px' }">
                  <span
                    v-for="(c, index) in state.colorForm.colors"
                    :key="index"
                    :style="{
                      width: '20px',
                      height: '20px',
                      display: 'inline-block',
                      backgroundColor: c
                    }"
                  />
                </div>
              </template>

              <div style="padding: 6px 10px">
                <div>
                  <span class="color-label">{{ t('chart.system_case') }}</span>
                  <el-select
                    v-model="state.colorForm.value"
                    :placeholder="t('chart.pls_slc_color_case')"
                    size="small"
                    @change="changeColorOption('value')"
                  >
                    <el-option
                      v-for="option in colorCases"
                      :key="option.value"
                      :label="option.name"
                      :value="option.value"
                      style="display: flex; align-items: center"
                    >
                      <div style="float: left">
                        <span
                          v-for="(c, index) in option.colors"
                          :key="index"
                          :style="{
                            width: '20px',
                            height: '20px',
                            float: 'left',
                            backgroundColor: c
                          }"
                        />
                      </div>
                      <span style="margin-left: 4px">{{ option.name }}</span>
                    </el-option>
                  </el-select>
                  <el-button
                    size="small"
                    type="text"
                    style="margin-left: 2px"
                    @click="resetCustomColor"
                    >{{ t('chart.reset') }}
                  </el-button>
                </div>
                <!--自定义配色方案-->
                <div>
                  <div style="display: flex; align-items: center; margin-top: 10px">
                    <span class="color-label">{{ t('chart.custom_case') }}</span>
                    <span>
                      <el-radio-group v-model="state.customColor" class="color-type">
                        <el-radio
                          v-for="(c, index) in state.colorForm.colors"
                          :key="index"
                          :label="c"
                          style="padding: 2px"
                          @change="switchColor(index)"
                        >
                          <span
                            :style="{
                              width: '20px',
                              height: '20px',
                              display: 'inline-block',
                              backgroundColor: c
                            }"
                          />
                        </el-radio>
                      </el-radio-group>
                    </span>
                  </div>
                  <div style="display: flex; align-items: center; margin-top: 10px">
                    <span class="color-label" />
                    <span>
                      <el-color-picker
                        v-model="state.customColor"
                        class="color-picker-style"
                        :predefine="predefineColors"
                        @change="switchColorCase"
                      />
                    </span>
                  </div>
                </div>
                <!--自定义系列或维度枚举值颜色-->
                <!--                <div-->
                <!--                  v-show="showProperty('colorPanel')"-->
                <!--                  style="display: flex; align-items: center; margin-top: 10px"-->
                <!--                >-->
                <!--                  <span class="color-label" />-->
                <!--                  <span>-->
                <!--                    <span v-for="(c, index) in colorForm.colors" :key="index" style="padding: 2px">-->
                <!--                      <span-->
                <!--                        :style="{-->
                <!--                          width: '20px',-->
                <!--                          height: '20px',-->
                <!--                          display: 'inline-block',-->
                <!--                          backgroundColor: c-->
                <!--                        }"-->
                <!--                      />-->
                <!--                    </span>-->
                <!--                  </span>-->
                <!--                </div>-->
              </div>

              <!--              <div class="custom-color-style">-->
              <!--                <div-->
              <!--                  v-for="(item, index) in state.colorForm.seriesColors"-->
              <!--                  :key="index"-->
              <!--                  style="display: flex; align-items: center; margin: 2px 0"-->
              <!--                >-->
              <!--                  <el-color-picker-->
              <!--                    v-model="item.color"-->
              <!--                    class="color-picker-style"-->
              <!--                    :predefine="predefineColors"-->
              <!--                    @change="switchCustomColor(index)"-->
              <!--                  />-->
              <!--                  <span class="span-label" :title="item.name">{{ item.name }}</span>-->
              <!--                </div>-->
              <!--              </div>-->
            </el-popover>
          </el-form-item>
          <!--alpha-->
          <el-form-item :label="t('chart.not_alpha')" class="form-item form-item-slider">
            <el-slider
              v-model="state.colorForm.alpha"
              show-input
              :show-input-controls="false"
              input-size="small"
              @change="changeColorCase('alpha')"
            />
          </el-form-item>
        </div>
      </el-form>
    </el-col>
  </div>
</template>

<style lang="less" scoped>
.form-item-slider :deep(.ed-form-item__label) {
  font-size: 12px;
  line-height: 38px;
}

.form-item :deep(.ed-form-item__label) {
  font-size: 12px;
}

.color-picker-style {
  cursor: pointer;
  z-index: 1003;
}

.color-label {
  display: inline-block;
  width: 60px;
}

.color-type :deep(.ed-radio__input) {
  display: none;
}

.ed-radio {
  margin: 0 2px 0 0 !important;
  border: 1px solid transparent;
}

.ed-radio :deep(.ed-radio__label) {
  padding-left: 0;
}

.ed-radio.is-checked {
  border: 1px solid #0a7be0;
}

.custom-color-style {
  height: 300px;
  overflow-y: auto;
  padding: 4px 12px;
  border: 1px solid #e6e6e6;
}
</style>
