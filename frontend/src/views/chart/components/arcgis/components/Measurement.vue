<template>
  <div class="measurementdiv" v-show="show">
    <div class="div-header">
      <span class="title">测量组件</span>
      <span class="close" @click="close">X</span>
    </div>
    <div id="measurement"></div>
  </div>
</template>

<script>
export default {
  name: "Measurement",
  props: {
    show: Boolean,
  },
  methods: {
    close() {
      this.$emit("closMmeasurement", 0);
    },
  },
};
</script>