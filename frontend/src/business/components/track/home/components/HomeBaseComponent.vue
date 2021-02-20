<template>
  <div>
    <el-card>
      <template v-slot:header>
        <span class="title">{{ title }}</span>
        <slot name="header-area"></slot>
      </template>
      <slot></slot>
    </el-card>
  </div>
</template>

<script>
export default {
  name: "HomeBaseComponent",
  props: {
    title: {
      type: String,
      default() {
        this.$t('commons.title')
      }
    }
  }
}
</script>

<style scoped>

</style>
