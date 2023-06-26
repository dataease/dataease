<script lang="tsx" setup>
import { reactive, watch } from 'vue'
import { useI18n } from '@/hooks/web/useI18n'
import { reverseColor } from '../util/util'
import { ArrowRight } from '@element-plus/icons-vue'

const { t } = useI18n()

const props = defineProps({
  drillFilters: {
    type: Array,
    default: () => []
  },
  themeStyle: {
    type: Object,
    required: false,
    default: null
  }
})

const emit = defineEmits(['onDrillJump'])

const state = reactive({
  textColor: null
})

watch(
  [() => props.themeStyle?.backgroundColorSelect, () => props.themeStyle?.color],
  () => {
    loadThemeStyle()
  },
  { deep: true }
)

const drillJump = index => {
  if (index < props.drillFilters.length) {
    emit('onDrillJump', index)
  }
}
const loadThemeStyle = () => {
  let themeStyle = null
  if (props.themeStyle) {
    themeStyle = JSON.parse(JSON.stringify(props.themeStyle))
    if (themeStyle && themeStyle.commonBackground) {
      const viewBGColor = themeStyle.commonBackground.color
      if (viewBGColor !== '#FFFFFF') {
        const reverseValue = reverseColor(viewBGColor)
        state.textColor = reverseValue
      } else {
        state.textColor = null
      }
    }
    if (themeStyle && themeStyle.backgroundColorSelect) {
      const panelColor = themeStyle.color
      if (panelColor !== '#FFFFFF') {
        const reverseValue = reverseColor(panelColor)
        state.textColor = reverseValue
      } else {
        state.textColor = null
      }
    }
  }
}
</script>

<template>
  <div v-if="props.drillFilters && props.drillFilters.length > 0" class="drill">
    <el-breadcrumb :separator-icon="ArrowRight" class="drill-style">
      <el-breadcrumb-item class="drill-item" @click="drillJump(0)">
        <span :style="{ color: state.textColor }">{{ t('commons.all') }}</span>
      </el-breadcrumb-item>
      <el-breadcrumb-item
        v-for="(filter, index) in props.drillFilters"
        :key="index"
        class="drill-item"
        @click="drillJump(index + 1)"
      >
        <span :style="{ color: state.textColor }">{{ filter.value[0] }}</span>
      </el-breadcrumb-item>
    </el-breadcrumb>
  </div>
</template>

<style lang="less" scoped>
.drill-style {
  font-size: 12px;
}
.drill-style :deep(.el-breadcrumb__separator) {
  margin: 0 !important;
}
.drill-item {
  cursor: pointer;
}
.drill {
  height: 20px;
  padding: 0 16px;
}
</style>
