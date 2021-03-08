<template>
  <ms-container v-loading="$store.getters.loadingMap[$store.getters.currentPath]">

    <ms-aside-container>
      <group @switchComponent="switchComponent"/>
    </ms-aside-container>

    <ms-main-container>
      <!--<router-view/>-->
      <component :is="component" :param="param" @switchComponent="switchComponent"/>
    </ms-main-container>
  </ms-container>
</template>

<script>
import MsMainContainer from '@/metersphere/common/components/MsMainContainer'
import MsContainer from '@/metersphere/common/components/MsContainer'
import MsAsideContainer from '@/metersphere/common/components/MsAsideContainer'
import Group from './group/Group'

import DataHome from './data/DataHome'
import ViewTable from './data/ViewTable'
import AddDB from './add/AddDB'
import AddSQL from './add/AddSQL'

export default {
  name: 'DataSet',
  components: { MsMainContainer, MsContainer, MsAsideContainer, Group, DataHome, ViewTable, AddDB, AddSQL },
  data() {
    return {
      component: DataHome,
      param: {}
    }
  },
  methods: {
    switchComponent(c) {
      console.log(c)
      this.param = c.param
      switch (c.name) {
        case 'ViewTable':
          this.component = ViewTable
          break
        case 'AddDB':
          this.component = AddDB
          break
        case 'AddSQL':
          this.component = AddSQL
          break
        default:
          this.component = DataHome
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
  }

</style>
