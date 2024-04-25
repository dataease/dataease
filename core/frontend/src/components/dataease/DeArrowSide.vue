<template>
  <div
    v-if="arrowSide && !isInside"
    class="arrow-side-tree arrow-side-tree-left"
    @click="handleClick(false)"
  >
    <svg-icon
      icon-class="icon_left_outlined"
      class="icon16"
    />
  </div>
  <div
    v-else-if="!arrowSide && isInside"
    class="arrow-side-tree arrow-side-tree-right"
    @click="handleClick(true)"
  >
    <svg-icon
      icon-class="icon_right_outlined"
      class="icon16"
    />
  </div>
</template>

<script>
import { mapState } from 'vuex'
export default {
  name: 'DeArrowSide',
  props: {
    isInside: {
      type: Boolean,
      default: false
    }
  },
  computed: {
    ...mapState('app', [
      'arrowSide'
    ])
  },
  methods: {
    handleClick(val) {
      this.$store.dispatch('app/setArrowSide', val)
      this.$emit('changeSideTreeStatus', val)
    }
  }
}
</script>

<style lang="less" scoped>
.arrow-side-tree-left {
  top: 146px;
  height: 24px;
  width: 24px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0px 5px 10px 0px #1f23291a;
  z-index: 100;
}

.arrow-side-tree-right {
  box-shadow: 0px 4px 8px 0px #0000001a;
  top: 146px;
  height: 24px;
  width: 20px;
  display: flex;
  align-items: center;
  padding-left: 2px;
  border-top-right-radius: 12px;
  border-bottom-right-radius: 12px;
  &:hover {
    padding-left: 4px;
    width: 24px;
  }
}

.arrow-side-tree {
  position: absolute;
  border: 1px solid #dee0e3;
  background: #fff;
  cursor: pointer;
  z-index: 10;
  &:hover {
    .icon16 {
      color: #3370ff;
    }
  }
  .icon16 {
    font-size: 12px;
  }
}
</style>
