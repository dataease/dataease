<template>
  <div class="grid">
    <div
      v-for="(yItem, index) in positionBox"
      :key="index+'y'"
      class="outer-class"
    >
      <div
        v-for="(xItem, idx) in yItem"
        :key="idx+'x'"
        :style="classInfo"
        class="inner-class"
      >
        {{ xItem.el?'.':'' }}
      </div>
    </div>
  </div>
</template>

<script>

export default {
  props: {
    // eslint-disable-next-line vue/require-default-prop
    positionBox: {
      type: Array
    },
    matrixStyle: {
      type: Object
    }
  },
  data() {
    return {

    }
  },

  computed: {
    classInfo: function() {
      return {
        width: this.matrixStyle.width + 'px',
        height: this.matrixStyle.height + 'px'
      }
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
.outer-class{
  float: left; width: 105%
}

.inner-class{
  float: left; border: 1px solid #b3d4fc ;
}
</style>
