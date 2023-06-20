<template>
  <el-row class="custom-row">
    <el-row><span class="custom-item-text">文本</span> </el-row>
    <el-row style="margin-top: 8px">
      <div style="padding-top: 4px; margin-right: 8px">
        <el-color-picker
          :title="t('chart.text_color')"
          v-model="state.titleForm.color"
          class="color-picker-style"
          size="small"
          :predefine="state.predefineColors"
          @change="changeTitleStyle('color')"
        />
      </div>
      <div style="padding-top: 4px">
        <el-select
          style="width: 56px"
          :title="t('chart.text_fontsize')"
          v-model="state.titleForm.fontSize"
          :placeholder="'大小'"
          size="small"
          @change="changeTitleStyle('fontSize')"
        >
          <el-option
            v-for="option in state.fontSize"
            :key="option.value"
            :label="option.name"
            :value="option.value"
          />
        </el-select>
      </div>
      <div>
        <el-checkbox v-model="state.titleForm.isBolder" :title="t('chart.bolder')"
          ><Icon class-name="bash-icon" name="title-bold"
        /></el-checkbox>
      </div>
      <div>
        <el-checkbox v-model="state.titleForm.isItalic" :title="t('chart.italic')"
          ><Icon class-name="bash-icon" name="title-italic"
        /></el-checkbox>
      </div>
      <div class="custom-divider"></div>
      <div>
        <el-radio-group v-model="state.titleForm.hPosition" @change="changeTitleStyle('hPosition')">
          <el-radio label="left" :title="t('chart.text_pos_left')"
            ><Icon class-name="bash-icon" name="title-left"
          /></el-radio>
          <el-radio label="center" :title="t('chart.text_pos_center')"
            ><Icon class-name="bash-icon" name="title-center"
          /></el-radio>
          <el-radio label="right" :title="t('chart.text_pos_right')"
            ><Icon class-name="bash-icon" name="title-right"
          /></el-radio>
        </el-radio-group>
      </div>
      <div class="custom-divider"></div>
      <div>
        <el-radio-group
          v-model="state.titleForm.vPosition"
          size="mini"
          @change="changeTitleStyle('vPosition')"
        >
          <el-radio label="top" :title="t('chart.text_pos_top')"
            ><Icon class-name="bash-icon" name="title-v-top"
          /></el-radio>
          <el-radio label="center" :title="t('chart.text_pos_center')"
            ><Icon class-name="bash-icon" name="title-v-center"
          /></el-radio>
          <el-radio label="bottom" :title="t('chart.text_pos_bottom')"
            ><Icon class-name="bash-icon" name="title-v-bottom"
          /></el-radio>
        </el-radio-group>
      </div>
    </el-row>
  </el-row>
</template>

<script lang="ts" setup>
import { reactive } from 'vue'
import { COLOR_PANEL } from '@/views/chart/components/editor/util/chart'
import { dvMainStoreWithOut } from '@/store/modules/data-visualization/dvMain'
import { useI18n } from '@/hooks/web/useI18n'
import Icon from '@/components/icon-custom/src/Icon.vue'
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
init()
</script>

<style scoped lang="less">
.custom-item-text {
  font-size: 12px !important;
  font-weight: 400 !important;
  line-height: 20px;
  color: #646a73 !important;
}

:deep(.ed-radio) {
  margin-right: 0;
}
:deep(.ed-radio-group) {
  padding-top: 2px;
}

:deep(.ed-checkbox__input) {
  display: none;
}

:deep(.ed-checkbox.is-checked) {
  .ed-checkbox__label {
    .bash-icon {
      background: rgba(51, 112, 255, 0.1);
      border-radius: 4px;
      color: #3370ff;
    }
  }
}

:deep(.ed-radio.is-checked) {
  .ed-radio__label {
    .bash-icon {
      background: rgba(51, 112, 255, 0.1);
      border-radius: 4px;
    }
  }
}

:deep(.ed-radio__input) {
  display: none;
}

:deep(.ed-radio__input.is-checked) {
  .ed-radio__inner {
    padding: 4px;
    background-color: green;
    background-clip: content-box;
  }
}

.bash-icon {
  width: 24px;
  height: 24px;
}

.custom-divider {
  margin: 5px 0 0 8px;
  height: 20px;
  width: 1px;
  background-color: rgba(31, 35, 41, 0.15);
}
</style>
