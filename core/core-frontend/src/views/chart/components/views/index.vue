<script lang="tsx" setup>
import ChartComponentG2Plot from './components/ChartComponentG2Plot.vue'
import { onMounted, reactive, ref, toRefs, watch } from 'vue'
import { useEmitt } from '@/hooks/web/useEmitt'
import { hexColorToRGBA } from '@/views/chart/components/js/util.js'
import { DEFAULT_TITLE_STYLE } from '@/views/chart/components/editor/util/chart'

const g2 = ref<any>()

const props = defineProps({
  view: {
    type: Object,
    default() {
      return {
        propValue: null
      }
    }
  }
})

const { view } = toRefs(props)

const state = reactive({
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
  [() => view.value],
  () => {
    initTitle()
  },
  { deep: true }
)

const initTitle = () => {
  if (view.value.customStyle) {
    const customStyle = view.value.customStyle
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
    name: 'calcData-' + view.value.id,
    callback: function (val) {
      view.value = val
      g2?.value?.calcData(val)
      initTitle()
    }
  })
  useEmitt({
    name: 'renderChart-' + view.value.id,
    callback: function (val) {
      view.value = val
      g2?.value?.renderChart(val)
      initTitle()
    }
  })
})
</script>

<template>
  <div class="chart-area">
    <p v-if="state.title_show" :style="state.title_class">{{ view.title }}</p>
    <!--这里去渲染不同图库的视图-->
    <chart-component-g2-plot :view="view" v-if="view?.render === 'antv'" ref="g2" />
  </div>
</template>

<style lang="less" scoped>
.chart-area {
  width: 100%;
  height: 100%;
}
</style>
