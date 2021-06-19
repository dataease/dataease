<template>
  <de-container v-loading="$store.getters.loadingMap[$store.getters.currentPath]">

    <de-aside-container>
      <group :save-status="saveStatus" @switchComponent="switchComponent" />
    </de-aside-container>

    <de-main-container>
      <!--<router-view/>-->
      <component :is="component" :param="param" @switchComponent="switchComponent" @saveSuccess="saveSuccess" />
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
import { removeClass } from '@/utils'

export default {
  name: 'DataSet',
  components: { DeMainContainer, DeContainer, DeAsideContainer, Group, DataHome, ViewTable, AddDB, AddSQL, AddExcel, AddCustom },
  data() {
    return {
      component: DataHome,
      param: {},
      saveStatus: null
    }
  },
  mounted() {
    removeClass(document.body, 'showRightPanel')
  },
  methods: {
    switchComponent(c) {
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
    },

    saveSuccess(val) {
      this.saveStatus = val
    }
  }
}
</script>

<style scoped>
  .ms-aside-container {
    height: calc(100vh - 56px);
    padding: 10px 15px;
    min-width: 260px;
    max-width: 460px;
  }

  .ms-main-container {
    height: calc(100vh - 56px);
    padding: 10px 15px 0 15px;
  }

</style>
