<template>
  <de-container v-loading="$store.getters.loadingMap[$store.getters.currentPath]">
    <de-aside-container style="padding: 0 0;" type="datasource">
      <ds-tree ref="dsTree" :datasource="datasource" @switch-main="switchMain" />
    </de-aside-container>
    <de-main-container>
      <component
        :is="component"
        v-if="!!component"
        :params="param"
        :t-data="tData"
        @refresh-type="refreshType"
        @switch-component="switchMain"
      />
    </de-main-container>
  </de-container>
</template>

<script>
import DeMainContainer from '@/components/dataease/DeMainContainer'
import DeContainer from '@/components/dataease/DeContainer'
import DeAsideContainer from '@/components/dataease/DeAsideContainer'
import DsTree from './DsTree'
import DsForm from './form'
import DataHome from './DataHome'

export default {
  name: 'DsMain',
  components: { DeMainContainer, DeContainer, DeAsideContainer, DsTree, DataHome },
  data() {
    return {
      component: DataHome,
      datasource: {},
      param: null,
      tData: null
    }
  },
  computed: {},
  watch: {},
  mounted() {
    // this.clear()
  },
  methods: {
    // 切换main区内容
    switchMain(param) {
      const { component, componentParam, tData } = param
      this.component = DataHome
      this.param = null
      this.$nextTick(() => {
        switch (component) {
          case 'DsForm':
            this.component = DsForm
            this.param = componentParam
            this.tData = tData
            break
          default:
            this.component = DataHome
            this.param = null
            break
        }
      })
    },
    refreshType(datasource) {
      this.datasource = datasource
      this.$refs.dsTree && this.$refs.dsTree.refreshType(datasource)
    },
    msg2Current(sourceParam) {
      this.$refs.dsTree && this.$refs.dsTree.markInvalid(sourceParam)
    }
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
  padding: 0px;
}
</style>
