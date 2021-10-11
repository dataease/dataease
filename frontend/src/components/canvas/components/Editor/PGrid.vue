<template>
  <div class="grid">
    <!--    positionBox:{{ positionBox.length }}-->
    <!--    <div v-for="(yItem, index) in positionBox" v-if="index<positionBox.length-5" :key="index+'y'" style="float: left; width: 105%">-->
    <div v-for="(yItem, index) in positionBox" :key="index+'y'" style="float: left; width: 105%">
<!--      <div v-for="(xItem, index) in yItem" :key="index+'x'" :style="classInfo" style="float: left; border: 0.2px solid #b3d4fc ;color: #00feff">-->
      <div v-for="(xItem, index) in yItem" :key="index+'x'" :style="classInfo" style="float: left; border: 0.2px solid #b3d4fc ;">
        {{ xItem.el?1:0 }}
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
</style>
