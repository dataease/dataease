<template>
  <el-card class="template-component">

    <template v-slot:header >

      <slot name="header">
        <span class="title">{{title}}</span>
      </slot>
    </template>

    <template>
      <el-main>
        <slot></slot>
      </el-main>
    </template>

  </el-card>

</template>

<script>
    export default {
      name: "CommonComponent",
      props: {
        title: {
          type: String,
          default() {
            this.$t('commons.title');
          }
        }
      },
      methods: {

      }
    }
</script>

<style scoped>

  .el-main {
    min-height: 200px;
  }

</style>
