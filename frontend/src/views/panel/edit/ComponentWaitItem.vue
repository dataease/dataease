<template>
  <div
    v-proportion="0.8"
    :style="componentItemStyle"
  >
    <mobile-check-bar v-if="mobileCheckBarShow" :element="config" />
    <de-out-widget
      v-if="config.type==='custom'"
      :id="'component' + config.id"
      class="component-custom"
      :out-style="outStyle"
      :element="config"
    />
    <component
      :is="config.component"
      v-else
      ref="wrapperChild"
      :out-style="outStyle"
      :prop-value="config.propValue"
      :is-edit="false"
      :element="config"
      :h="itemHeight"
    />
  </div>
</template>

<script>
import { mapState } from 'vuex'
import MobileCheckBar from '@/components/canvas/components/Editor/MobileCheckBar'

export default {
  name: 'ComponentWaitItem',
  components: { MobileCheckBar },
  props: {
    config: {
      type: Object,
      required: true
    }
  },
  data() {
    return {
      itemWidth: 280,
      itemHeight: 200,
      outStyle: {
        width: this.itemWidth,
        height: this.itemHeight
      }
    }
  },
  computed: {
    // 移动端编辑组件选择按钮显示
    mobileCheckBarShow() {
      // 显示条件：1.当前是移动端画布编辑状态
      return this.mobileLayoutStatus
    },
    componentDataWaite() {
      const result = []
      this.componentData.forEach(item => {
        if (!item.mobileSelected) {
          result.push(item)
        }
      })
      return result
    },
    componentItemStyle() {
      return {
        padding: '5px',
        display: 'inline-block',
        width: '33.3333%'
      }
    },
    ...mapState([
      'mobileLayoutStatus',
      'componentData'
    ])
  },
  methods: {
  }
}
</script>

<style scoped>
  .component-custom {
    outline: none;
    width: 100% !important;
    height: 100%;
  }
</style>
