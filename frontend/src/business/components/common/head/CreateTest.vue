<template>
  <router-link class="create-test" :to="this.to" v-permission="this.permission">
    <el-button type="primary" size="small">{{this.title}}</el-button>
  </router-link>
</template>

<script>
  export default {
    name: "MsCreateTest",
    props: {
      to: [String, Object],
      title: {
        type: String,
        default: function () {
          return this.$t('load_test.create');
        }
      },
      permission: {
        type: Array,
        default: function () {
          return ['test_user', 'test_manager'];
        }
      }
    }
  }
</script>

<style scoped>
  .create-test {
    line-height: 38px;
  }
</style>
