<script lang="ts" setup>
import { PropType, computed, onMounted, reactive, toRefs, watch } from 'vue'
import { useI18n } from '@/hooks/web/useI18n'
import {
  COLOR_PANEL,
  CHART_FONT_FAMILY,
  CHART_FONT_LETTER_SPACE,
  DEFAULT_TITLE_STYLE
} from '@/views/chart/components/editor/util/chart'
import { dvMainStoreWithOut } from '@/store/modules/data-visualization/dvMain'
import { storeToRefs } from 'pinia'
const dvMainStore = dvMainStoreWithOut()
const { batchOptStatus } = storeToRefs(dvMainStore)

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

const emit = defineEmits(['onTextChange'])

const predefineColors = COLOR_PANEL
const fontFamily = CHART_FONT_FAMILY
const fontLetterSpace = CHART_FONT_LETTER_SPACE

const state = reactive({
  titleForm: JSON.parse(JSON.stringify(DEFAULT_TITLE_STYLE)),
  fontSize: []
})

watch(
  () => props.chart,
  () => {
    init()
  },
  { deep: false }
)

const { chart } = toRefs(props)

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

const changeTitleStyle = s => {
  emit('onTextChange', state.titleForm)
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
    if (customStyle.text) {
      state.titleForm = customStyle.text
    }
  }
}

function checkBold() {
  state.titleForm.isBolder = !state.titleForm.isBolder
  changeTitleStyle('isBolder')
}
function checkItalic() {
  state.titleForm.isItalic = !state.titleForm.isItalic
  changeTitleStyle('isItalic')
}

function setPosition(p: 'left' | 'center' | 'right') {
  state.titleForm.hPosition = p
  changeTitleStyle('hPosition')
}

const showProperty = prop => props.propertyInner?.includes(prop)

onMounted(() => {
  init()
})
</script>

<template>
  <el-form
    ref="titleForm"
    :disabled="!state.titleForm.show"
    :model="state.titleForm"
    label-position="top"
  >
    <el-form-item
      :label="t('chart.title')"
      class="form-item"
      :class="'form-item-' + themes"
      v-if="!batchOptStatus"
    >
      <el-input
        :effect="themes"
        v-model="chart.title"
        size="small"
        maxlength="100"
        :placeholder="t('chart.title')"
        clearable
        @blur="changeTitleStyle('title')"
      />
    </el-form-item>

    <el-form-item
      class="form-item"
      :class="'form-item-' + themes"
      :effect="themes"
      :label="t('chart.text')"
    >
      <el-select
        :effect="themes"
        v-model="state.titleForm.fontFamily"
        :placeholder="t('chart.font_family')"
        @change="changeTitleStyle('fontFamily')"
      >
        <el-option
          v-for="option in fontFamily"
          :key="option.value"
          :label="option.name"
          :value="option.value"
        />
      </el-select>
    </el-form-item>

    <div style="display: flex">
      <el-form-item class="form-item" :class="'form-item-' + themes" style="padding-right: 4px">
        <el-color-picker
          :effect="themes"
          v-model="state.titleForm.color"
          class="color-picker-style"
          :predefine="predefineColors"
          @change="changeTitleStyle('color')"
          is-custom
        />
      </el-form-item>
      <el-form-item class="form-item" :class="'form-item-' + themes" style="padding: 0 4px">
        <el-select
          style="width: 56px"
          :effect="themes"
          v-model="state.titleForm.fontSize"
          :placeholder="t('chart.text_fontsize')"
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

      <el-form-item class="form-item" :class="'form-item-' + themes" style="padding-left: 4px">
        <el-select
          :effect="themes"
          v-model="state.titleForm.letterSpace"
          :placeholder="t('chart.quota_letter_space')"
          @change="changeTitleStyle('letterSpace')"
        >
          <template #prefix>
            <el-icon>
              <Icon name="icon_letter-spacing_outlined" />
            </el-icon>
          </template>
          <el-option
            v-for="option in fontLetterSpace"
            :key="option.value"
            :label="option.name"
            :value="option.value"
          />
        </el-select>
      </el-form-item>
    </div>

    <el-space wrap style="margin-bottom: 16px">
      <el-tooltip effect="dark" placement="top">
        <template #content>
          {{ t('chart.bolder') }}
        </template>
        <div
          class="icon-btn"
          :class="{ dark: themes === 'dark', active: state.titleForm.isBolder }"
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
          :class="{ dark: themes === 'dark', active: state.titleForm.isItalic }"
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
          :class="{ dark: themes === 'dark', active: state.titleForm.hPosition === 'left' }"
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
          :class="{ dark: themes === 'dark', active: state.titleForm.hPosition === 'center' }"
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
          :class="{ dark: themes === 'dark', active: state.titleForm.hPosition === 'right' }"
          @click="setPosition('right')"
        >
          <el-icon>
            <Icon name="icon_right-alignment_outlined" />
          </el-icon>
        </div>
      </el-tooltip>
    </el-space>

    <el-form-item class="form-item" :class="'form-item-' + themes">
      <el-checkbox
        size="small"
        :effect="themes"
        v-model="state.titleForm.fontShadow"
        @change="changeTitleStyle('fontShadow')"
      >
        {{ t('chart.font_shadow') }}
      </el-checkbox>
    </el-form-item>
  </el-form>
</template>

<style lang="less" scoped>
:deep(.ed-input .ed-select__prefix--light) {
  padding-right: 6px;
}
.icon-btn {
  font-size: 16px;
  width: 24px;
  height: 24px;
  text-align: center;
  border-radius: 4px;

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
.m-divider {
  width: 1px;
  height: 18px;
  background: rgba(31, 35, 41, 0.15);
}
</style>
