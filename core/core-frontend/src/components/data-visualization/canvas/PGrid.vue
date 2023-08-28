<template>
  <div class="grid">
    <div v-for="(yItem, index) in positionBox" :key="index + 'y'" class="outer-class">
      <div v-for="(xItem, idx) in yItem" :key="idx + 'x'" :style="classInfo" class="inner-class">
        {{ xItem.el ? '1' : '0' }}
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, toRefs } from 'vue'

const props = defineProps({
  // eslint-disable-next-line vue/require-default-prop
  positionBox: {
    type: Array
  },
  matrixStyle: {
    type: Object
  }
})

const { positionBox, matrixStyle } = toRefs(props)

const classInfo = computed(() => {
  return {
    width: matrixStyle.value.width + 'px',
    height: matrixStyle.value.height + 'px'
  }
})
</script>

<style lang="less" scoped>
.grid {
  position: absolute;
  top: 0;
  left: 0;
}
.outer-class {
  float: left;
  width: 105%;
}

.inner-class {
  float: left;
  border: 1px solid #b3d4fc;
}
</style>
