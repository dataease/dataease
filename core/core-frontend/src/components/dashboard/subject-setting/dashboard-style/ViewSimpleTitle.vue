<template>
  <el-row class="custom-row">
    <el-row><span class="custom-item-text">文本</span> </el-row>
    <el-row style="margin-top: 8px">
      <el-space wrap>
        <el-form-item class="form-item no-margin-bottom" :class="'form-item-' + themes">
          <el-color-picker
            :title="t('chart.text_color')"
            v-model="titleForm.color"
            class="color-picker-style"
            is-custom
            :predefine="state.predefineColors"
            @change="changeTitleStyle('color')"
          />
        </el-form-item>

        <el-form-item class="form-item no-margin-bottom" :class="'form-item-' + themes">
          <el-select
            style="width: 56px"
            :title="t('chart.text_fontsize')"
            v-model="titleForm.fontSize"
            :placeholder="'大小'"
            size="small"
            @change="changeTitleStyle('fontSize')"
          >
            <el-option
              v-for="option in fontSizeList"
              :key="option.value"
              :label="option.name"
              :value="option.value"
            />
          </el-select>
        </el-form-item>

        <el-tooltip effect="dark" placement="top">
          <template #content>
            {{ t('chart.bolder') }}
          </template>
          <div
            class="icon-btn"
            :class="{ dark: themes === 'dark', active: titleForm.isBolder }"
            @click="checkBold"
          >
            <el-icon>
              <Icon name="icon_bold_outlined" />
            </el-icon>
          </div>
        </el-tooltip>

        <el-tooltip effect="dark" placement="top">
          <template #content>
            {{ t('chart.italic') }}
          </template>
          <div
            class="icon-btn"
            :class="{ dark: themes === 'dark', active: titleForm.isItalic }"
            @click="checkItalic"
          >
            <el-icon>
              <Icon name="icon_italic_outlined" />
            </el-icon>
          </div>
        </el-tooltip>

        <div class="m-divider"></div>

        <el-tooltip effect="dark" placement="top">
          <template #content>
            {{ t('chart.text_pos_left') }}
          </template>
          <div
            class="icon-btn"
            :class="{ dark: themes === 'dark', active: titleForm.hPosition === 'left' }"
            @click="setPosition('left')"
          >
            <el-icon>
              <Icon name="icon_left-alignment_outlined" />
            </el-icon>
          </div>
        </el-tooltip>
        <el-tooltip effect="dark" placement="top">
          <template #content>
            {{ t('chart.text_pos_center') }}
          </template>
          <div
            class="icon-btn"
            :class="{ dark: themes === 'dark', active: titleForm.hPosition === 'center' }"
            @click="setPosition('center')"
          >
            <el-icon>
              <Icon name="icon_center-alignment_outlined" />
            </el-icon>
          </div>
        </el-tooltip>
        <el-tooltip effect="dark" placement="top">
          <template #content>
            {{ t('chart.text_pos_right') }}
          </template>
          <div
            class="icon-btn"
            :class="{ dark: themes === 'dark', active: titleForm.hPosition === 'right' }"
            @click="setPosition('right')"
          >
            <el-icon>
              <Icon name="icon_right-alignment_outlined" />
            </el-icon>
          </div>
        </el-tooltip>
      </el-space>
    </el-row>
  </el-row>
</template>

<script lang="ts" setup>
import { computed, reactive, ref } from 'vue'
import { COLOR_PANEL } from '@/views/chart/components/editor/util/chart'
import { dvMainStoreWithOut } from '@/store/modules/data-visualization/dvMain'
import { useI18n } from '@/hooks/web/useI18n'
import Icon from '@/components/icon-custom/src/Icon.vue'
const { t } = useI18n()
const dvMainStore = dvMainStoreWithOut()

const emits = defineEmits(['onTextChange'])

const titleForm = computed<any>(() => dvMainStore.canvasStyleData.component.chartTitle)
const state = reactive({
  fontSize: [],
  isSetting: false,
  predefineColors: COLOR_PANEL
})
const themes = ref('light')

const fontSizeList = computed(() => {
  const arr = []
  for (let i = 10; i <= 60; i = i + 2) {
    arr.push({
      name: i + '',
      value: i + ''
    })
  }
  return arr
})

const changeTitleStyle = modifyName => {
  titleForm.value['modifyName'] = modifyName
  emits('onTextChange', titleForm.value)
}

function checkBold() {
  titleForm.value.isBolder = !titleForm.value.isBolder
  changeTitleStyle('isBolder')
}
function checkItalic() {
  titleForm.value.isItalic = !titleForm.value.isItalic
  changeTitleStyle('isItalic')
}

function setPosition(p: 'left' | 'center' | 'right') {
  titleForm.value.hPosition = p
  changeTitleStyle('hPosition')
}
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
      background: var(--ed-color-primary-1a, rgba(51, 112, 255, 0.1));
      border-radius: 4px;
      color: var(--ed-color-primary);
    }
  }
}

:deep(.ed-radio.is-checked) {
  .ed-radio__label {
    .bash-icon {
      background: var(--ed-color-primary-1a, rgba(51, 112, 255, 0.1));
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
.m-divider {
  width: 1px;
  height: 18px;
  background: rgba(31, 35, 41, 0.15);
}
.form-item {
  &.no-margin-bottom {
    margin-bottom: 0 !important;
  }
}
</style>
