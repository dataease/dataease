<template>
  <div @click="handleClick">
    <component
      :is="config.component"
      v-if="config.type==='custom'"
      :id="'component' + config.id"
      class="component"
      :style="getStyle(config.style)"
      :out-style="config.style"
      :element="config"
      :class="{'gap_class':canvasStyleData.panel.gap==='yes'}"
    />
    <component
      :is="config.component"
      v-else
      class="component"
      :out-style="config.style"
      :style="getStyle(config.style)"
      :prop-value="config.propValue"
      :element="config"
      :class="{'gap_class':canvasStyleData.panel.gap==='yes'}"
    />
  </div>
</template>

<script>
import { getStyle } from '@/components/canvas/utils/style'
import runAnimation from '@/components/canvas/utils/runAnimation'
import { mixins } from '@/components/canvas/utils/events'
import { mapState } from 'vuex'

export default {
  mixins: [mixins],
  props: {
    config: {
      type: Object,
      require: true,
      default: null
    },
    filter: {
      type: Object,
      require: false,
      default: null
    }
  },
  computed: {
    ...mapState([
      'canvasStyleData'
    ])
  },
  mounted() {
    runAnimation(this.$el, this.config.animations)
  },
  methods: {
    getStyle,

    handleClick() {
      const events = this.config.events
      Object.keys(events).forEach(event => {
        this[event](events[event])
      })
    }
  }
}
</script>

<style lang="scss" scoped>
.component {
    position: absolute;
}
</style>
