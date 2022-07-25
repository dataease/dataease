<template>
  <div>
    <el-button type="primary" @click="execute" plain>{{text}}</el-button>
  </div>
</template>

<script>
  export default {
    name: 'SearchButton',
    props: {

    },
    data() {
      return {
        text: '查询'
      }
    },
    methods: {
      execute() {
        console.log('开始查询。。。')
      }

    }
  }

</script>

<style lang="scss" scoped>
</style>