<template>
  <de-container v-loading="$store.getters.loadingMap[$store.getters.currentPath]">

    <de-aside-container>
      <group ref="group" :save-status="saveStatus" @switchComponent="switchComponent" />
    </de-aside-container>

    <de-main-container>
      <component :is="component" :param="param" @switchComponent="switchComponent" @saveSuccess="saveSuccess" @typeChange="typeChange" />
    </de-main-container>
  </de-container>
</template>

<script>
import DeMainContainer from '@/components/dataease/DeMainContainer'
import DeContainer from '@/components/dataease/DeContainer'
import DeAsideContainer from '@/components/dataease/DeAsideContainer'
import Group from './group/Group'

import ChartHome from './data/ChartHome'
import ChartEdit from './view/ChartEdit'
import { removeClass } from '@/utils'

export default {
  name: 'Chart',
  components: { DeMainContainer, DeContainer, DeAsideContainer, Group, ChartHome, ChartEdit },
  data() {
    return {
      component: ChartHome,
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
        case 'ChartEdit':
          this.component = ChartEdit
          break
        default:
          this.component = ChartHome
          break
      }
    },
    saveSuccess(val) {
      this.saveStatus = val
    },
    typeChange(newType) {
      this.$refs.group.nodeTypeChange(newType)
    }
  }
}
</script>

<style scoped>
  .ms-aside-container {
    height: calc(100vh - 56px);
    padding: 0 0;
    min-width: 260px;
    max-width: 460px;
  }

  .ms-main-container {
    height: calc(100vh - 56px);
    padding: 0;
  }

</style>
