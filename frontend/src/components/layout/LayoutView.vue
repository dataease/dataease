<template>
  <main class="view-container">
    <transition name="el-fade-in" mode="out-in">
      <keep-alive>
        <router-view :key="key"/>
      </keep-alive>
    </transition>
  </main>
</template>

<script>
export default {
  name: "LayoutView",
  computed: {
    key() {
      return this.$route.path
    }
  }
}
</script>
