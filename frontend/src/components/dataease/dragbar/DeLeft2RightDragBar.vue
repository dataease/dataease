<template>
  <div
    v-left-to-right-drag="type"
    class="drag-bar"
  />
</template>

<script>
export default {
  name: 'DeLeft2RightDragBar',
  props: {
    type: {
      type: String,
      default: null
    }
  }
}
</script>

<style scoped>

  .drag-bar {
    height: 100%;
    width: 1px;
    position: absolute;
    right: 0px;
    top: 0;
    cursor: col-resize;
    background-color: #E6E6E6;;
    border: 0px;
  }

  .blackTheme .drag-bar {
    height: 100%;
    width: 1px;
    position: absolute;
    right: 0px;
    top: 0;
    cursor: col-resize;
    background-color: var(--SiderTextColor) !important;
    border: 0px;
  }

  .drag-bar:hover {
    width: 3px;
  }

</style>
