<template>
  <TinyMCE v-model="curComponent.propValue" />
</template>

<script>
import TinyMCE from '@/components/TinyMCE/index.vue'

export default {
  components: { TinyMCE },
  data() {
    return {

    }
  },
  computed: {
    curComponent() {
      return this.$store.state.curComponent
    }
  }
}
</script>

<style lang="scss" scoped>
  .attr-list {
    overflow: auto;
    padding: 20px;
    padding-top: 0;
    height: 100%;
  }
</style>
