<script lang="ts" setup>
import { PropType } from 'vue'
import { useI18n } from '@/hooks/web/useI18n'
import { COLOR_PANEL } from '@/views/chart/components/editor/util/chart'
import icon_bold_outlined from '@/assets/svg/icon_bold_outlined.svg'
import icon_italic_outlined from '@/assets/svg/icon_italic_outlined.svg'

const props = defineProps({
  modelValue: { type: Object as PropType<TableGrandTotalStyle>, required: true },
  themes: { type: String as PropType<EditorTheme>, default: 'dark' }
})
const emit = defineEmits(['update:modelValue', 'change'])
const { t } = useI18n()
const fontSizes: number[] = []
for (let size = 10; size <= 40; size += 2) fontSizes.push(size)
for (let size = 50; size <= 200; size += 10) fontSizes.push(size)
const fontToggles = [
  { key: 'isBolder' as const, label: 'bolder', icon: icon_bold_outlined },
  { key: 'isItalic' as const, label: 'italic', icon: icon_italic_outlined }
]

const change = (key: keyof TableGrandTotalStyle, value) => {
  emit('update:modelValue', { ...props.modelValue, [key]: value })
  emit('change', key)
}
</script>

<template>
  <el-form-item class="form-item" :class="'form-item-' + themes">
    <el-checkbox
      :effect="themes"
      :model-value="modelValue.customBackground"
      @change="change('customBackground', $event)"
    >
      {{ t('chart.total_custom_background') }}
    </el-checkbox>
  </el-form-item>
  <el-form-item class="form-item" :class="'form-item-' + themes">
    <el-color-picker
      :effect="themes"
      :model-value="modelValue.backgroundColor"
      :disabled="!modelValue.customBackground"
      is-custom
      :trigger-width="108"
      :predefine="COLOR_PANEL"
      show-alpha
      @change="change('backgroundColor', $event)"
    />
  </el-form-item>
  <el-form-item class="form-item" :class="'form-item-' + themes">
    <el-checkbox
      :effect="themes"
      :model-value="modelValue.customFont"
      @change="change('customFont', $event)"
    >
      {{ t('chart.total_custom_font') }}
    </el-checkbox>
  </el-form-item>
  <div class="total-font-style">
    <el-form-item class="form-item" :class="'form-item-' + themes">
      <el-color-picker
        :effect="themes"
        :model-value="modelValue.fontColor"
        :disabled="!modelValue.customFont"
        is-custom
        :predefine="COLOR_PANEL"
        @change="change('fontColor', $event)"
      />
    </el-form-item>
    <el-form-item class="form-item" :class="'form-item-' + themes">
      <el-select
        style="width: 58px"
        :effect="themes"
        :model-value="modelValue.fontSize"
        :disabled="!modelValue.customFont"
        @change="change('fontSize', $event)"
      >
        <el-option v-for="size in fontSizes" :key="size" :label="String(size)" :value="size" />
      </el-select>
    </el-form-item>
    <el-form-item
      v-for="item in fontToggles"
      :key="item.key"
      class="form-item"
      :class="'form-item-' + themes"
    >
      <el-checkbox
        :effect="themes"
        class="icon-checkbox"
        :model-value="modelValue[item.key]"
        :disabled="!modelValue.customFont"
        @change="change(item.key, $event)"
      >
        <el-tooltip effect="dark" placement="top" :content="t('chart.' + item.label)">
          <div
            class="icon-btn"
            :class="{
              dark: themes === 'dark',
              active: modelValue[item.key],
              disabled: !modelValue.customFont
            }"
          >
            <el-icon><component :is="item.icon" class="svg-icon" /></el-icon>
          </div>
        </el-tooltip>
      </el-checkbox>
    </el-form-item>
  </div>
</template>

<style lang="less" scoped>
.total-font-style {
  display: flex;
  align-items: center;
  gap: 8px;
}
.icon-checkbox {
  :deep(.ed-checkbox__input) {
    display: none;
  }
  :deep(.ed-checkbox__label) {
    padding: 0;
  }
}
.icon-btn {
  font-size: 16px;
  line-height: 16px;
  width: 24px;
  height: 24px;
  text-align: center;
  border-radius: 6px;
  padding-top: 4px;
  color: #1f2329;
  cursor: pointer;
  &.dark {
    color: #a6a6a6;
  }
  &.active {
    color: var(--ed-color-primary);
    background-color: var(--ed-color-primary-1a, rgba(51, 112, 255, 0.1));
  }
  &.disabled {
    opacity: 0.4;
    cursor: not-allowed;
  }
}
</style>
