<template>
  <svg class="grid" width="100%" height="100%" xmlns="http://www.w3.org/2000/svg">
    <defs>
      <pattern id="smallGrid" :width="smallGridW" :height="smallGridH" patternUnits="userSpaceOnUse">
        <path
          :d="smallGridPathD"
          fill="none"
          stroke="rgba(207, 207, 207, 0.3)"
          stroke-width="1"
        />
      </pattern>
      <pattern id="middleGrid" :width="middleGridW" :height="middleGridH" patternUnits="userSpaceOnUse">
        <rect :width="middleGridW" :height="middleGridH" fill="url(#smallGrid)" />
        <path
          :d="middleGridPathD"
          fill="none"
          stroke="rgba(207, 207, 207, 0.3)"
          stroke-width="1.5"
        />
      </pattern>
      <pattern id="grid" :width="gridW" :height="gridH" patternUnits="userSpaceOnUse">
        <rect :width="gridW" :height="gridH" fill="url(#middleGrid)" />
        <path
          :d="pathD"
          fill="none"
          stroke="rgba(207, 207, 207, 0.7)"
          stroke-width="2.5"
        />
      </pattern>
    </defs>
    <rect width="100%" height="100%" fill="url(#grid)" />
  </svg>
</template>

<script>

import { mapState } from 'vuex'

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
      return 'M ' + this.gridW + ' 0 L 0 0 0 ' + this.gridH
    },
    middleGridPathD: function() {
      return 'M ' + this.middleGridW + ' 0 L 0 0 0 ' + this.middleGridH
    },
    smallGridPathD: function() {
      return 'M ' + this.smallGridW + ' 0 L 0 0 0 ' + this.smallGridH
    },
    gridW: function() {
      return this.matrixStyle.width * 2 * this.matrixBase
    },
    gridH: function() {
      return this.matrixStyle.height * 2 * this.matrixBase
    },
    middleGridW: function() {
      return this.matrixStyle.width * this.matrixBase
    },
    middleGridH: function() {
      return this.matrixStyle.height * this.matrixBase
    },
    smallGridW: function() {
      return this.matrixStyle.width
    },
    smallGridH: function() {
      return this.matrixStyle.height
    },
    matrixBase: function() {
      // return this.canvasStyleData.aidedDesign ? this.canvasStyleData.aidedDesign.matrixBase : 1
      return this.canvasStyleData.aidedDesign.matrixBase
    },
    ...mapState([
      'canvasStyleData'
    ])
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
