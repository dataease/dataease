<template>
  <el-row class="custom-row">
    <el-row class="custom-row-inner">
      <el-space wrap>
        <el-tooltip v-if="styleForm.color" effect="dark" placement="bottom">
          <template #content> {{ t('chart.text_color') }} </template>
          <el-form-item
            :effect="themes"
            class="form-item no-margin-bottom"
            :class="'form-item-' + themes"
          >
            <el-color-picker
              :title="t('chart.text_color')"
              v-model="styleForm.color"
              class="color-picker-style"
              is-custom
              :predefine="state.predefineColors"
              @change="changeStyle"
            />
          </el-form-item>
        </el-tooltip>

        <el-tooltip v-if="styleForm.headFontColor" effect="dark" placement="bottom">
          <template #content> 头部字体颜色 </template>
          <el-form-item
            :effect="themes"
            class="form-item no-margin-bottom"
            :class="'form-item-' + themes"
          >
            <el-color-picker
              title="头部字体颜色"
              v-model="styleForm.headFontColor"
              class="color-picker-style"
              is-custom
              :predefine="state.predefineColors"
              @change="changeStyle"
            />
          </el-form-item>
        </el-tooltip>
        <el-tooltip v-if="styleForm.headFontActiveColor" effect="dark" placement="bottom">
          <template #content> 头部激活字体颜色 </template>
          <el-form-item
            :effect="themes"
            class="form-item no-margin-bottom"
            :class="'form-item-' + themes"
          >
            <el-color-picker
              v-model="styleForm.headFontActiveColor"
              class="color-picker-style"
              is-custom
              :predefine="state.predefineColors"
              @change="changeStyle"
            />
          </el-form-item>
        </el-tooltip>
        <el-tooltip v-if="styleForm.fontSize" effect="dark" placement="bottom">
          <template #content> {{ t('chart.text_fontsize') }} </template>
          <el-form-item
            :effect="themes"
            class="form-item no-margin-bottom"
            :class="'form-item-' + themes"
          >
            <el-select
              style="width: 50px"
              :effect="themes"
              :title="t('chart.text_fontsize')"
              v-model="styleMounted.fontSize"
              :placeholder="'大小'"
              size="small"
              @change="sizeChange('fontSize')"
            >
              <el-option
                v-for="option in fontSizeList"
                :key="option.value"
                :label="option.name"
                :value="option.value"
              />
            </el-select>
          </el-form-item>
        </el-tooltip>
        <el-tooltip v-if="styleForm.activeFontSize" effect="dark" placement="bottom">
          <template #content> 激活字体大小 </template>
          <el-form-item
            class="form-item no-margin-bottom"
            :effect="themes"
            :class="'form-item-' + themes"
          >
            <el-select
              style="width: 50px"
              :effect="themes"
              title="激活字体大小"
              v-model="styleMounted.activeFontSize"
              :placeholder="'大小'"
              size="small"
              @change="sizeChange('activeFontSize')"
            >
              <el-option
                v-for="option in fontSizeList"
                :key="option.value"
                :label="option.name"
                :value="option.value"
              />
            </el-select>
          </el-form-item>
        </el-tooltip>
        <el-tooltip v-if="styleForm.opacity" effect="dark" placement="bottom">
          <template #content> 透明度 </template>
          <el-form-item class="form-item no-margin-bottom" :class="'form-item-' + themes">
            <el-select
              style="width: 50px"
              :effect="themes"
              v-model="styleForm.opacity"
              placeholder="透明度"
              size="small"
              @change="sizeChange"
            >
              <el-option
                v-for="option in opacitySizeList"
                :key="option"
                :label="option"
                :value="option"
              />
            </el-select>
          </el-form-item>
        </el-tooltip>
        <el-tooltip v-if="styleForm.fontWeight !== undefined" effect="dark" placement="bottom">
          <template #content>
            {{ t('chart.bolder') }}
          </template>
          <div
            class="icon-btn"
            :class="{ dark: themes === 'dark', active: styleForm.fontWeight === 'bold' }"
            @click="checkBold"
          >
            <el-icon>
              <Icon name="icon_bold_outlined" />
            </el-icon>
          </div>
        </el-tooltip>

        <el-tooltip v-if="styleForm.fontStyle !== undefined" effect="dark" placement="bottom">
          <template #content>
            {{ t('chart.italic') }}
          </template>
          <div
            class="icon-btn"
            :class="{ dark: themes === 'dark', active: styleForm.fontStyle === 'italic' }"
            @click="checkItalic"
          >
            <el-icon>
              <Icon name="icon_italic_outlined" />
            </el-icon>
          </div>
        </el-tooltip>
        <template v-if="styleForm.textAlign">
          <div class="m-divider"></div>
          <el-tooltip effect="dark" placement="bottom">
            <template #content>
              {{ t('chart.text_pos_left') }}
            </template>
            <div
              class="icon-btn"
              :class="{ dark: themes === 'dark', active: styleForm.textAlign === 'left' }"
              @click="setPosition('textAlign', 'left')"
            >
              <el-icon>
                <Icon name="icon_left-alignment_outlined" />
              </el-icon>
            </div>
          </el-tooltip>
          <el-tooltip effect="dark" placement="bottom">
            <template #content>
              {{ t('chart.text_pos_center') }}
            </template>
            <div
              class="icon-btn"
              :class="{ dark: themes === 'dark', active: styleForm.textAlign === 'center' }"
              @click="setPosition('textAlign', 'center')"
            >
              <el-icon>
                <Icon name="icon_center-alignment_outlined" />
              </el-icon>
            </div>
          </el-tooltip>
          <el-tooltip effect="dark" placement="bottom">
            <template #content>
              {{ t('chart.text_pos_right') }}
            </template>
            <div
              class="icon-btn"
              :class="{ dark: themes === 'dark', active: styleForm.textAlign === 'right' }"
              @click="setPosition('textAlign', 'right')"
            >
              <el-icon>
                <Icon name="icon_right-alignment_outlined" />
              </el-icon>
            </div>
          </el-tooltip>
        </template>
        <template v-if="styleForm.headHorizontalPosition">
          <div class="m-divider"></div>
          <el-tooltip effect="dark" placement="bottom">
            <template #content>
              {{ t('chart.text_pos_left') }}
            </template>
            <div
              class="icon-btn"
              :class="{
                dark: themes === 'dark',
                active: styleForm.headHorizontalPosition === 'left'
              }"
              @click="setPosition('headHorizontalPosition', 'left')"
            >
              <el-icon>
                <Icon name="icon_left-alignment_outlined" />
              </el-icon>
            </div>
          </el-tooltip>
          <el-tooltip effect="dark" placement="bottom">
            <template #content>
              {{ t('chart.text_pos_center') }}
            </template>
            <div
              class="icon-btn"
              :class="{
                dark: themes === 'dark',
                active: styleForm.headHorizontalPosition === 'center'
              }"
              @click="setPosition('headHorizontalPosition', 'center')"
            >
              <el-icon>
                <Icon name="icon_center-alignment_outlined" />
              </el-icon>
            </div>
          </el-tooltip>
          <el-tooltip effect="dark" placement="bottom">
            <template #content>
              {{ t('chart.text_pos_right') }}
            </template>
            <div
              class="icon-btn"
              :class="{
                dark: themes === 'dark',
                active: styleForm.headHorizontalPosition === 'right'
              }"
              @click="setPosition('headHorizontalPosition', 'right')"
            >
              <el-icon>
                <Icon name="icon_right-alignment_outlined" />
              </el-icon>
            </div>
          </el-tooltip>
        </template>
      </el-space>
    </el-row>
  </el-row>
</template>

<script lang="ts" setup>
import { computed, reactive, ref, toRefs, watch } from 'vue'
import { COLOR_PANEL } from '@/views/chart/components/editor/util/chart'
import { dvMainStoreWithOut } from '@/store/modules/data-visualization/dvMain'
import { useI18n } from '@/hooks/web/useI18n'
import Icon from '@/components/icon-custom/src/Icon.vue'
import { snapshotStoreWithOut } from '@/store/modules/data-visualization/snapshot'
import { storeToRefs } from 'pinia'
const { t } = useI18n()
const dvMainStore = dvMainStoreWithOut()
const snapshotStore = snapshotStoreWithOut()

const { curComponent, canvasStyleData } = storeToRefs(dvMainStore)

const props = withDefaults(
  defineProps<{
    themes?: EditorTheme
    element: any
  }>(),
  {
    themes: 'dark'
  }
)

const { themes, element } = toRefs(props)
const emits = defineEmits(['onTextChange'])
const styleMounted = ref({
  opacity: 1,
  fontSize: 22,
  activeFontSize: 22,
  fontWeight: 'normal',
  fontStyle: 'normal',
  textAlign: 'center',
  color: '#000000'
})

const opacitySizeList = [0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9, 1]

const styleForm = computed<any>(() => element.value.style)
const state = reactive({
  fontSize: [],
  isSetting: false,
  predefineColors: COLOR_PANEL
})
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

const styleInit = () => {
  if (curComponent.value) {
    Object.keys(styleMounted.value).forEach(key => {
      styleMounted.value[key] = Math.round(
        (curComponent.value.style[key] * 100) / canvasStyleData.value.scale
      )
    })
  }
}

const sizeChange = key => {
  curComponent.value.style[key] = Math.round(
    (styleMounted.value[key] * canvasStyleData.value.scale) / 100
  )
  changeStyle()
}

const changeStyle = () => {
  snapshotStore.recordSnapshotCache()
}

const checkBold = () => {
  if (styleForm.value.fontWeight === 'normal') {
    styleForm.value.fontWeight = 'bold'
  } else {
    styleForm.value.fontWeight = 'normal'
  }
  changeStyle()
}

const checkItalic = () => {
  if (styleForm.value.fontStyle === 'normal') {
    styleForm.value.fontStyle = 'italic'
  } else {
    styleForm.value.fontStyle = 'normal'
  }
  changeStyle()
}

function setPosition(key, p: 'left' | 'center' | 'right') {
  styleForm.value[key] = p
  changeStyle()
}

watch(
  () => curComponent.value,
  () => {
    styleInit()
  },
  {
    deep: true,
    immediate: true
  }
)
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

.custom-row-inner {
  margin: 8px 0px 24px;
}
</style>
