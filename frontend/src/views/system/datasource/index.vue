<template>
  <de-container v-loading="$store.getters.loadingMap[$store.getters.currentPath]" style="background-color: #f7f8fa">
    <de-main-container>
      <ds-main />
    </de-main-container>
  </de-container>
</template>

<script>
import DeMainContainer from '@/components/dataease/DeMainContainer'
import DeContainer from '@/components/dataease/DeContainer'
import DsMain from './DsMain'

export default {
  name: 'Panel',
  components: { DeMainContainer, DeContainer, DsMain },
  data() {
    return {
      component: DsMain,
      componentName: 'DsMain',
      param: {}
    }
  },
  mounted() {

  },
  methods: {

  }
}
</script>

<style scoped>
  .ms-aside-container {
    height: calc(100vh - 56px);
    padding: 0px;
    min-width: 260px;
    max-width: 460px;
  }

  .ms-main-container {
    height: calc(100vh - 56px);
    padding: 0;
  }

</style>
