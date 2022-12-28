<template>
  <div>
    <span>{{ someProp.title }}</span>
    <br />
    <span class="use-html">{{ someProp.content }}</span>
    <br />
    <span @click="change" class="link-msg">{{ someProp.link }}</span>
  </div>
</template>
<script>
export default {
  props: {
    someProp: {
      type: Object,
      default: () => {}
    },
  },
  methods: {
    change() {
      this.$emit('change')
    }
  }
}
</script>
<style lang="less">
.link-msg {
  color: #307eff;
  cursor: pointer;
}
</style>