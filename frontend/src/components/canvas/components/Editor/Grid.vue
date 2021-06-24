<template>
  <svg class="grid" width="100%" height="100%" xmlns="http://www.w3.org/2000/svg">
    <defs>
      <!--      <pattern id="smallGrid" width="5" height="5" patternUnits="userSpaceOnUse">-->
      <!--        <path-->
      <!--          d="M 5 0 L 0 0 0 5"-->
      <!--          fill="none"-->
      <!--          stroke="rgba(207, 207, 207, 0.3)"-->
      <!--          stroke-width="1"-->
      <!--        />-->
      <!--      </pattern>-->
      <pattern id="grid" :width="matrixStyle.width" :height="matrixStyle.height" patternUnits="userSpaceOnUse">
        <rect :width="matrixStyle.width" :height="matrixStyle.height" fill="url(#smallGrid)" />
        <path
          :d="pathD"
          fill="none"
          stroke="rgba(186, 186, 186, 0.5)"
          stroke-width="1"
        />
      </pattern>
    </defs>
    <rect width="100%" height="100%" fill="url(#grid)" />
  </svg>
</template>

<script>

export default {
  props: {
    // eslint-disable-next-line vue/require-default-prop
    matrixStyle: {
      type: Object
    }
  },
  data() {
    return {

    }
  },

  computed: {
    pathD: function() {
      return 'M ' + this.matrixStyle.width + ' 0 L 0 0 0 ' + this.matrixStyle.height
    }
  }
}
</script>

<style lang="scss" scoped>
.grid {
    position: absolute;
    top: 0;
    left: 0;
}
</style>
