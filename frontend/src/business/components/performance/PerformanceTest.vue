<template>
  <el-col>
    <performance-header-menus/>
    <div>
      <transition>
        <keep-alive>
          <router-view :baseUrl="baseUrl"/>
        </keep-alive>
      </transition>
    </div>
  </el-col>
</template>

<script>

  import PerformanceHeaderMenus from "./head/PerformanceHeaderMenus";

  export default {
    name: "PerformanceTest",
    components: {PerformanceHeaderMenus},
    data() {
      return {
        baseUrl: "performance"
      }
    }
  }

</script>

<style scoped>

</style>
