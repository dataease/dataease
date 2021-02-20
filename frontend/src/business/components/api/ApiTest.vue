<template>
  <el-col>
    <ms-api-header-menus/>
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

  import MsApiHeaderMenus from "./head/ApiHeaderMenus";

  export default {
    name: "MsApiTest",
    components: {MsApiHeaderMenus},
    data() {
      return {
        baseUrl: "api"
      }
    }
  }

</script>

<style scoped>

</style>
