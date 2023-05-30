<script lang="tsx" setup>
import ChartComponentG2Plot from './components/ChartComponentG2Plot.vue'
import { onMounted, reactive, ref, watch } from 'vue'
import { useEmitt } from '@/hooks/web/useEmitt'
import { hexColorToRGBA } from '@/views/chart/components/js/util'
import { DEFAULT_TITLE_STYLE } from '@/views/chart/components/editor/util/chart'

const g2 = ref<any>()

const state = reactive({
  view: {},
  title_show: true,
  title_class: {
    margin: '0 0',
    width: '100%',
    fontSize: '18px',
    color: '#303133',
    textAlign: 'left',
    fontStyle: 'normal',
    fontWeight: 'normal',
    background: ''
  }
})

watch(
  [() => state.view],
  () => {
    initTitle()
  },
  { deep: true }
)

const initTitle = () => {
  if (state.view.customStyle) {
    const customStyle = state.view.customStyle
    if (customStyle.text) {
      state.title_show = customStyle.text.show
      state.title_class.fontSize = customStyle.text.fontSize + 'px'
      state.title_class.color = customStyle.text.color
      state.title_class.textAlign = customStyle.text.hPosition
      state.title_class.fontStyle = customStyle.text.isItalic ? 'italic' : 'normal'
      state.title_class.fontWeight = customStyle.text.isBolder ? 'bold' : 'normal'

      state.title_class.fontFamily = customStyle.text.fontFamily
        ? customStyle.text.fontFamily
        : DEFAULT_TITLE_STYLE.fontFamily
      state.title_class.letterSpacing =
        (customStyle.text.letterSpace
          ? customStyle.text.letterSpace
          : DEFAULT_TITLE_STYLE.letterSpace) + 'px'
      state.title_class.textShadow = customStyle.text.fontShadow ? '2px 2px 4px' : 'none'
    }
    if (customStyle.background) {
      state.title_class.background = hexColorToRGBA(
        customStyle.background.color,
        customStyle.background.alpha
      )
    }
  }
}

onMounted(() => {
  useEmitt({
    name: 'calcData',
    callback: function (val) {
      state.view = val
      g2?.value?.calcData(val)
      initTitle()
    }
  })
  useEmitt({
    name: 'renderChart',
    callback: function (val) {
      state.view = val
      g2?.value?.renderChart(val)
      initTitle()
    }
  })
})
</script>

<template>
  <div>
    <p v-if="state.title_show" :style="state.title_class">{{ state.view.title }}</p>
    <!--这里去渲染不同图库的视图-->
    <chart-component-g2-plot v-if="state.view?.render === 'antv'" ref="g2" />
  </div>
</template>

<style lang="less" scoped></style>
