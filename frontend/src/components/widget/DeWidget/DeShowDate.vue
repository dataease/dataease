<template>
  <div style="height: 100%">
    <time-default v-if="element.formatInfo.openMode === '0'" :ref="element.id" :element="element" />

    <time-elec v-if="element.formatInfo.openMode === '1'" :ref="element.id" :element="element" />

    <time-simple v-if="element.formatInfo.openMode === '2'" :ref="element.id" :element="element" />

    <time-complex v-if="element.formatInfo.openMode === '3'" :ref="element.id" :element="element" />
  </div>
</template>

<script>
import TimeDefault from './TimeDefault'
import TimeElec from './TimeElec'
import TimeSimple from './TimeSimple'
import TimeComplex from './TimeComplex'
export default {
  name: 'DeShowDate',
  components: { TimeDefault, TimeElec, TimeSimple, TimeComplex },
  props: {
    element: {
      type: Object,
      default: null
    }
  },
  data() {
    return {
      // time_margin: 0
    }
  },
  computed: {
    timeMargin() {
      return this.element.style.time_margin
    }
  },

  methods: {
    chartResize() {
      this.$nextTick(() => {
        this.$refs[this.element.id] && this.$refs[this.element.id].resize && this.$refs[this.element.id].resize()
      })
    }
  }

}
</script>
