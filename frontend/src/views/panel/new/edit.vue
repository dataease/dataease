<template>
  <el-container>
    <!-- <de-header>Header</de-header> -->
    <el-header class="de-header">Header</el-header>
    <de-container>
      <de-aside-container class="ms-aside-container">Aside</de-aside-container>
      <de-main-container class="ms-main-container"> Main</de-main-container>
    </de-container>
  </el-container>
</template>

<script>
import DeMainContainer from '@/components/dataease/DeMainContainer'
import DeContainer from '@/components/dataease/DeContainer'
import DeAsideContainer from '@/components/dataease/DeAsideContainer'

export default {
  components: {
    DeMainContainer,
    DeContainer,
    DeAsideContainer
  }
}
</script>

<style scoped>
  .ms-aside-container {
    height: calc(100vh - 91px);
    padding: 15px;
    min-width: 260px;
    max-width: 460px;
    border: none;
  }

  .ms-main-container {
    height: calc(100vh - 91px);
  }

  .de-header {
    height: 35px !important;
    border-bottom: 1px solid #E6E6E6;
}

</style>
