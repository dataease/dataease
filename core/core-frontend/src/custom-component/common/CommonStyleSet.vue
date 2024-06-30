<template>
  <el-row class="custom-row">
    <el-row class="custom-row-inner">
      <el-space wrap>
        <template v-for="styleOptionKey in styleOptionKeyArrayPre">
          <el-tooltip
            :key="styleOptionKey.value"
            v-if="styleForm[styleOptionKey.value] !== undefined"
            effect="dark"
            placement="bottom"
          >
            <template #content> {{ styleOptionKey.label }} </template>
            <el-form-item class="form-item no-margin-bottom" :class="'form-item-' + themes">
              <el-select
                :style="{ width: styleOptionKey.width }"
                :effect="themes"
                v-model="styleForm[styleOptionKey.value]"
                size="small"
                @change="changeStyle"
              >
                <template #prefix>
                  <el-icon :class="{ 'dark-icon': themes === 'dark' }">
                    <Icon :name="styleOptionKey.icon" />
                  </el-icon>
                </template>
                <el-option
                  class="custom-style-option"
                  v-for="option in styleOptionKey.customOption"
                  :key="option.value"
                  :label="option.name"
                  :value="option.value"
                />
              </el-select>
            </el-form-item>
          </el-tooltip>
        </template>

        <template v-for="styleColorKey in styleColorKeyArray">
          <el-tooltip
            :key="styleColorKey.value"
            v-if="styleForm[styleColorKey.value] !== undefined"
            effect="dark"
            placement="bottom"
          >
            <template #content> {{ styleColorKey.label }} </template>
            <el-form-item
              :effect="themes"
              class="form-item no-margin-bottom"
              :class="'form-item-' + themes"
            >
              <el-color-picker
                :title="t('chart.text_color')"
                v-model="styleForm[styleColorKey.value]"
                class="color-picker-style"
                :prefix-icon="expandIcon(styleColorKey.icon)"
                :triggerWidth="styleColorKey.width"
                is-custom
                :predefine="state.predefineColors"
                @change="changeStyle"
              >
              </el-color-picker>
            </el-form-item>
          </el-tooltip>
        </template>

        <template v-for="styleOptionMountedKey in styleOptionMountedKeyArray">
          <el-tooltip
            :key="styleOptionMountedKey.value"
            v-if="styleForm[styleOptionMountedKey.value] !== undefined"
            effect="dark"
            placement="bottom"
          >
            <template #content> {{ styleOptionMountedKey.label }} </template>
            <el-form-item
              :effect="themes"
              class="form-item no-margin-bottom"
              :class="'form-item-' + themes"
            >
              <el-select
                :style="{ width: styleOptionMountedKey.width }"
                :effect="themes"
                v-model="styleMounted[styleOptionMountedKey.value]"
                size="small"
                @change="sizeChange(styleOptionMountedKey.value)"
              >
                <template #prefix>
                  <el-icon :class="{ 'dark-icon': themes === 'dark' }">
                    <Icon :name="styleOptionMountedKey.icon" />
                  </el-icon>
                </template>
                <el-option
                  class="custom-style-option"
                  v-for="option in styleOptionMountedKey.customOption"
                  :key="option.value"
                  :label="option.name"
                  :value="option.value"
                />
              </el-select>
            </el-form-item>
          </el-tooltip>
        </template>

        <template v-for="styleOptionKey in styleOptionKeyArray">
          <el-tooltip
            :key="styleOptionKey.value"
            v-if="styleForm[styleOptionKey.value] !== undefined"
            effect="dark"
            placement="bottom"
          >
            <template #content> {{ styleOptionKey.label }} </template>
            <el-form-item class="form-item no-margin-bottom" :class="'form-item-' + themes">
              <el-select
                :style="{ width: styleOptionKey.width }"
                :effect="themes"
                v-model="styleForm[styleOptionKey.value]"
                size="small"
                @change="changeStylePre(styleOptionKey.value)"
              >
                <template #prefix>
                  <el-icon :class="{ 'dark-icon': themes === 'dark' }">
                    <Icon :name="styleOptionKey.icon" />
                  </el-icon>
                </template>
                <el-option
                  class="custom-style-option"
                  v-for="option in styleOptionKey.customOption"
                  :key="option.value"
                  :label="option.name"
                  :value="option.value"
                />
              </el-select>
            </el-form-item>
          </el-tooltip>
        </template>
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
          <div class="m-divider" :class="'custom-divider-' + themes"></div>
          <div style="display: flex">
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
          </div>
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

<script lang="tsx" setup>
import { computed, h, reactive, ref, toRefs, watch } from 'vue'
import { COLOR_PANEL } from '@/views/chart/components/editor/util/chart'
import { dvMainStoreWithOut } from '@/store/modules/data-visualization/dvMain'
import { useI18n } from '@/hooks/web/useI18n'
import Icon from '@/components/icon-custom/src/Icon.vue'
import { snapshotStoreWithOut } from '@/store/modules/data-visualization/snapshot'
import { storeToRefs } from 'pinia'
const { t } = useI18n()
const dvMainStore = dvMainStoreWithOut()
const snapshotStore = snapshotStoreWithOut()

const { canvasStyleData } = storeToRefs(dvMainStore)

const props = withDefaults(
  defineProps<{
    themes?: EditorTheme
    element: any
  }>(),
  {
    themes: 'dark'
  }
)
const expandIcon = (name: string) => {
  return h(Icon, { className: '', name })
}
const { themes, element } = toRefs(props)
const emits = defineEmits(['onStyleAttrChange'])
const styleMounted = ref({
  opacity: 1,
  fontSize: 22,
  activeFontSize: 22,
  letterSpacing: 0,
  scrollSpeed: 0,
  fontWeight: 'normal',
  fontStyle: 'normal',
  textAlign: 'center',
  color: '#000000'
})

const fontFamilyList = [
  { name: '微软雅黑', value: 'Microsoft YaHei' },
  { name: '宋体', value: 'SimSun, "Songti SC", STSong' },
  { name: '黑体', value: 'SimHei, Helvetica' },
  { name: '楷体', value: 'KaiTi, "Kaiti SC", STKaiti' }
]

const scrollSpeedList = [
  { name: '停止', value: 0 },
  { name: '10', value: 10 },
  { name: '20', value: 20 },
  { name: '30', value: 30 },
  { name: '40', value: 40 },
  { name: '50', value: 50 },
  { name: '60', value: 60 },
  { name: '70', value: 70 },
  { name: '80', value: 80 },
  { name: '90', value: 90 },
  { name: '100', value: 100 }
]

const opacitySizeList = [
  { name: '0.1', value: 0.1 },
  { name: '0.2', value: 0.2 },
  { name: '0.3', value: 0.3 },
  { name: '0.4', value: 0.4 },
  { name: '0.5', value: 0.5 },
  { name: '0.6', value: 0.6 },
  { name: '0.7', value: 0.7 },
  { name: '0.8', value: 0.8 },
  { name: '0.9', value: 0.9 },
  { name: '1', value: 1 }
]

const styleForm = computed<any>(() => element.value.style)
const state = reactive({
  fontSize: [],
  isSetting: false,
  predefineColors: COLOR_PANEL
})

const styleColorKeyArray = [
  { value: 'color', label: '颜色', width: 90, icon: 'dv-style-color' },
  { value: 'borderColor', label: '边框颜色', width: 90, icon: 'dv-style-borderColor' },
  {
    value: 'headFontColor',
    label: '头部字体颜色',
    width: 90,
    icon: 'dv-style-headFontColor'
  },
  {
    value: 'headFontActiveColor',
    label: '激活字体颜色',
    width: 90,
    icon: 'dv-style-headFontActiveColor'
  },
  { value: 'backgroundColor', label: '背景色', width: 90, icon: 'dv-style-backgroundColor' }
]

const letterSpacingList = computed(() => {
  const arr = []
  for (let i = 0; i <= 60; i = i + 1) {
    arr.push({
      name: i + '',
      value: i
    })
  }
  return arr
})

const fontSizeList = computed(() => {
  const arr = []
  for (let i = 10; i <= 60; i = i + 1) {
    arr.push({
      name: i + '',
      value: i
    })
  }
  return arr
})

const borderWidthList = computed(() => {
  const arr = []
  for (let i = 0; i <= 20; i = i + 1) {
    arr.push({
      name: i + '',
      value: i
    })
  }
  return arr
})

const borderRadiusList = computed(() => {
  const arr = []
  for (let i = 0; i <= 50; i = i + 1) {
    arr.push({
      name: i + '',
      value: i
    })
  }
  return arr
})

const borderStyleList = [
  { name: '实线', value: 'solid' },
  { name: '虚线', value: 'dashed' },
  { name: '点线', value: 'dotted' }
]

const styleOptionKeyArrayPre = [
  {
    value: 'fontFamily',
    label: '字体',
    customOption: fontFamilyList,
    width: '188px',
    icon: 'dv-style-fontFamily'
  }
]

//大小随画布缩放动态变化
const styleOptionMountedKeyArray = [
  {
    value: 'letterSpacing',
    label: '字间距',
    customOption: letterSpacingList.value,
    width: '90px',
    icon: 'dv-style-letterSpacing'
  },
  {
    value: 'fontSize',
    label: '字体大小',
    customOption: fontSizeList.value,
    width: '90px',
    icon: 'dv-style-fontSize'
  },
  {
    value: 'activeFontSize',
    label: '激活字体大小',
    customOption: fontSizeList.value,
    width: '90px',
    icon: 'dv-style-activeFont'
  }
]

//大小不变
const styleOptionKeyArray = [
  {
    value: 'scrollSpeed',
    label: '滚动速度',
    customOption: scrollSpeedList,
    width: '90px',
    icon: 'dv-style-scroll-speed'
  },
  {
    value: 'opacity',
    label: '不透明度',
    customOption: opacitySizeList,
    width: '90px',
    icon: 'dv-style-opacity'
  },
  {
    value: 'borderWidth',
    label: '边框宽度',
    customOption: borderWidthList.value,
    width: '90px',
    icon: 'dv-style-borderSize'
  },
  {
    value: 'borderRadius',
    label: '圆角',
    customOption: borderRadiusList.value,
    width: '90px',
    icon: 'dv-style-borderRadius'
  },
  {
    value: 'borderStyle',
    label: '边框样式',
    customOption: borderStyleList,
    width: '90px',
    icon: 'dv-style-borderStyle'
  }
]

const styleInit = () => {
  if (element.value) {
    Object.keys(styleMounted.value).forEach(key => {
      styleMounted.value[key] = Math.round(
        (element.value.style[key] * 100) / canvasStyleData.value.scale
      )
    })
  }
}

const changeStylePre = key => {
  changeStyle({ key: key, value: element.value.style[key] })
}

const sizeChange = key => {
  element.value.style[key] = Math.round(
    (styleMounted.value[key] * canvasStyleData.value.scale) / 100
  )
  changeStyle({ key: key, value: element.value.style[key] })
}

const changeStyle = params => {
  snapshotStore.recordSnapshotCache()
  emits('onStyleAttrChange', params)
}

const checkBold = () => {
  if (styleForm.value.fontWeight === 'normal') {
    styleForm.value.fontWeight = 'bold'
  } else {
    styleForm.value.fontWeight = 'normal'
  }
  changeStyle({ key: 'fontWeight', value: styleForm.value.fontWeight })
}

const checkItalic = () => {
  if (styleForm.value.fontStyle === 'normal') {
    styleForm.value.fontStyle = 'italic'
  } else {
    styleForm.value.fontStyle = 'normal'
  }
  changeStyle({ key: 'fontStyle', value: styleForm.value.fontStyle })
}

function setPosition(key, p: 'left' | 'center' | 'right') {
  styleForm.value[key] = p
  changeStyle({ key: key, p })
}

watch(
  () => element.value,
  () => {
    styleInit()
  },
  {
    deep: true,
    immediate: true
  }
)
</script>

<style lang="less">
.custom-style-option::after {
  display: none;
}
</style>

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

.custom-divider-light {
  background-color: rgba(31, 35, 41, 0.15);
}

.custom-divider-dark {
  background-color: #757575;
}
.form-item {
  &.no-margin-bottom {
    margin-bottom: 0 !important;
  }
}
.custom-row-inner {
  margin: 8px 0px 24px;
}

.dark-icon {
  color: #ffffff;
}
</style>
