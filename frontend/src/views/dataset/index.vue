<template>
  <de-container v-loading="$store.getters.loadingMap[$store.getters.currentPath]">

    <de-aside-container>
      <group @switchComponent="switchComponent" />
    </de-aside-container>

    <de-main-container>
      <!--<router-view/>-->
      <component :is="component" :param="param" @switchComponent="switchComponent" />
    </de-main-container>
  </de-container>
</template>

<script>
import DeMainContainer from '@/components/dataease/DeMainContainer'
import DeContainer from '@/components/dataease/DeContainer'
import DeAsideContainer from '@/components/dataease/DeAsideContainer'
import Group from './group/Group'

import DataHome from './data/DataHome'
import ViewTable from './data/ViewTable'
import AddDB from './add/AddDB'
import AddSQL from './add/AddSQL'
import AddExcel from './add/AddExcel'
import AddCustom from './add/AddCustom'
import FieldEdit from './data/FieldEdit'

export default {
  name: 'DataSet',
  components: { DeMainContainer, DeContainer, DeAsideContainer, Group, DataHome, ViewTable, AddDB, AddSQL, AddExcel, AddCustom },
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
        case 'AddExcel':
          this.component = AddExcel
          break
        case 'AddCustom':
          this.component = AddCustom
          break
        case 'FieldEdit':
          this.component = FieldEdit
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
