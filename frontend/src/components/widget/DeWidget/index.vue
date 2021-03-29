<template>
  <div>de-widget</div>
</template>

<script>
import { ApplicationContext } from '@/utils/ApplicationContext'
export default {
  props: {
    options: {
      type: Object,
      default: null
    }
  },
  data() {
    return {
      widget: null
    }
  },
  computed() {
    this.widget = ApplicationContext.getService(this.options.name)
    console.log(this.widget.name)
  },
  methods: {
    onDraw() {

    }
  }
}
</script>
<style lang="scss" scoped>

</style>
