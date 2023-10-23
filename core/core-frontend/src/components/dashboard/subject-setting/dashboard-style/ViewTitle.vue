<template>
  <div style="width: 100%">
    <el-col>
      <el-form ref="titleForm" :model="state.titleForm" label-width="80px" size="mini">
        <el-form-item :label="t('chart.show')" class="form-item">
          <el-checkbox v-model="state.titleForm.show" @change="changeTitleStyle('show')">{{
            t('chart.show')
          }}</el-checkbox>
        </el-form-item>
        <div v-show="state.titleForm.show">
          <el-form-item :label="t('chart.text_fontsize')" class="form-item">
            <el-select
              v-model="state.titleForm.fontSize"
              :placeholder="t('chart.text_fontsize')"
              size="mini"
              @change="changeTitleStyle('fontSize')"
            >
              <el-option
                v-for="option in state.fontSize"
                :key="option.value"
                :label="option.name"
                :value="option.value"
              />
            </el-select>
          </el-form-item>
          <el-form-item :label="t('chart.text_color')" class="form-item">
            <el-color-picker
              v-model="state.titleForm.color"
              class="color-picker-style"
              :predefine="state.predefineColors"
              @change="changeTitleStyle('color')"
            />
          </el-form-item>
          <el-form-item :label="t('chart.text_h_position')" class="form-item">
            <el-radio-group
              v-model="state.titleForm.hPosition"
              size="mini"
              @change="changeTitleStyle('hPosition')"
            >
              <el-radio-button label="left">{{ t('chart.text_pos_left') }}</el-radio-button>
              <el-radio-button label="center">{{ t('chart.text_pos_center') }}</el-radio-button>
              <el-radio-button label="right">{{ t('chart.text_pos_right') }}</el-radio-button>
            </el-radio-group>
          </el-form-item>
          <el-form-item :label="t('chart.text_v_position')" class="form-item">
            <el-radio-group
              v-model="state.titleForm.vPosition"
              size="mini"
              @change="changeTitleStyle('vPosition')"
            >
              <el-radio-button label="top">{{ t('chart.text_pos_top') }}</el-radio-button>
              <el-radio-button label="center">{{ t('chart.text_pos_center') }}</el-radio-button>
              <el-radio-button label="bottom">{{ t('chart.text_pos_bottom') }}</el-radio-button>
            </el-radio-group>
          </el-form-item>
          <el-form-item :label="t('chart.text_style')" class="form-item">
            <el-checkbox
              v-model="state.titleForm.isItalic"
              @change="changeTitleStyle('isItalic')"
              >{{ t('chart.italic') }}</el-checkbox
            >
            <el-checkbox
              v-model="state.titleForm.isBolder"
              @change="changeTitleStyle('isBolder')"
              >{{ t('chart.bolder') }}</el-checkbox
            >
          </el-form-item>
        </div>
      </el-form>
    </el-col>
  </div>
</template>

<script lang="ts" setup>
import { reactive } from 'vue'
import { COLOR_PANEL } from '@/views/chart/components/editor/util/chart'
import { dvMainStoreWithOut } from '@/store/modules/data-visualization/dvMain'
import { useI18n } from '@/hooks/web/useI18n'
const { t } = useI18n()
const dvMainStore = dvMainStoreWithOut()
const emits = defineEmits(['onTextChange'])

const state = reactive({
  titleForm: {},
  fontSize: [],
  isSetting: false,
  predefineColors: COLOR_PANEL
})

const initForm = () => {
  state.titleForm = dvMainStore.canvasStyleData.component.chartTitle
}

const init = () => {
  const arr = []
  for (let i = 10; i <= 60; i = i + 2) {
    arr.push({
      name: i + '',
      value: i + ''
    })
  }
  state.fontSize = arr
}
const changeTitleStyle = modifyName => {
  state.titleForm['modifyName'] = modifyName
  emits('onTextChange', state.titleForm)
}
</script>

<style scoped lang="less">
.shape-item {
  padding: 6px;
  border: none;
  width: 100%;
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.form-item-slider :deep(.ed-form-item__label) {
  font-size: 12px;
  line-height: 38px;
}
.form-item :deep(.ed-form-item__label) {
  font-size: 12px;
}
.ed-select-dropdown__item {
  padding: 0 20px;
}
span {
  font-size: 12px;
}
.ed-form-item {
  margin-bottom: 6px;
}

.switch-style {
  position: absolute;
  right: 10px;
  margin-top: -4px;
}
.color-picker-style {
  cursor: pointer;
  z-index: 1003;
}
</style>
