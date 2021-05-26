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
    />
    <component
      :is="config.component"
      v-else
      class="component"
      :out-style="config.style"
      :style="getStyle(config.style)"
      :prop-value="config.propValue"
      :element="config"
      :filter="filter"
    />
  </div>
</template>

<script>
import { getStyle } from '@/components/canvas/utils/style'
import runAnimation from '@/components/canvas/utils/runAnimation'
import { mixins } from '@/components/canvas/utils/events'

export default {
  mixins: [mixins],
  props: {
    config: {
      type: Object,
      require: true
    },
    filter: {
      type: Object,
      require: false
    }
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
