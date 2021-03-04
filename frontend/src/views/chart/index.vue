<template>
  <ms-container v-loading="$store.getters.loadingMap[$store.getters.currentPath]">

    <ms-aside-container>
      <group @switchComponent="switchComponent"/>
    </ms-aside-container>

    <ms-main-container>
      <!--        <router-view />-->
      <component :is="component" :param="param" @switchComponent="switchComponent"/>
    </ms-main-container>
  </ms-container>
</template>

<script>
import MsMainContainer from '@/metersphere/common/components/MsMainContainer'
import MsContainer from '@/metersphere/common/components/MsContainer'
import MsAsideContainer from '@/metersphere/common/components/MsAsideContainer'
import Group from './group/Group'

import ChartHome from './data/ChartHome'
import ChartEdit from './view/ChartEdit'

export default {
  name: 'Chart',
  components: { MsMainContainer, MsContainer, MsAsideContainer, Group, ChartHome, ChartEdit },
  data() {
    return {
      component: ChartHome,
      param: {}
    }
  },
  methods: {
    switchComponent(c) {
      console.log(c)
      this.param = c.param
      switch (c.name) {
        case 'ChartEdit':
          this.component = ChartEdit
          break
        default:
          this.component = ChartHome
          break
      }
    }
  }
}
</script>

<style scoped>
  .ms-aside-container {
    height: calc(100vh - 56px);
    padding: 15px;
    min-width: 260px;
    max-width: 460px;
  }

  .ms-main-container {
    height: calc(100vh - 56px);
    padding: 0;
  }

</style>
