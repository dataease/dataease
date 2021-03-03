<template>
  <el-tag :type="type" :effect="effect" :color="color" size="mini">{{content}}</el-tag>
</template>

<script>
    export default {
      name: "MsTag",
      props: {
        type: {
          type: String,
          default: 'primary',
        },
        color: {
          type: String,
          default: '',
        },
        content: {
          type: String
        },
        effect: {
          type: String,
          default: 'dark',
        }
      }
    }
</script>

<style scoped>

</style>
