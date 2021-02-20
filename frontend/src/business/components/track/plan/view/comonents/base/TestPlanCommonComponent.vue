<template>
  <ms-container>

    <ms-aside-container>
      <slot name="aside"></slot>
    </ms-aside-container>

    <ms-main-container>
      <slot name="main"></slot>
    </ms-main-container>

    <slot></slot>

  </ms-container>
</template>

<script>
    import MsMainContainer from "../../../../../common/components/MsMainContainer";
    import MsAsideContainer from "../../../../../common/components/MsAsideContainer";
    import MsContainer from "../../../../../common/components/MsContainer";
    export default {
        name: "MsTestPlanCommonComponent",
      components: {MsContainer, MsAsideContainer, MsMainContainer}
    }
</script>

<style scoped>

</style>
