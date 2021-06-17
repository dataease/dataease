<template>
  <de-container>
    <de-aside-container style="padding: 0 10px;">
      <ds-tree @switch-main="switchMain" />
    </de-aside-container>
    <de-main-container>
      <component :is="component" v-if="!!component" :params="param" @switch-component="switchMain" />
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
      param: null
    }
  },
  computed: {

  },
  watch: {

  },
  mounted() {
    // this.clear()
  },
  methods: {
    // 切换main区内容
    switchMain(param) {
      const { component, componentParam } = param

      switch (component) {
        case 'DsForm':
          this.component = DsForm
          this.param = componentParam
          break
        default:
          this.component = DataHome
          this.param = null
          break
      }
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

  /*.tab-panel>>>.is-stretch{*/
  /*  min-width: 80% !important;*/
  /*}*/
  /*.tab-panel>>>.el-tabs__nav-scroll {*/
  /*  overflow: hidden;*/
  /*  text-align: center;*/
  /*  display: flex;*/
  /*  align-items: center;*/
  /*  justify-content: center;*/
  /*}*/
  .tab-panel>>>.el-tabs__nav-wrap{
    padding: 0 10px;
  }
  .tab-panel>>>.el-tabs__nav-wrap::after {
    height: 1px;
  }
  .tab-panel>>>.el-tabs__item{
    /* width: 10px; */
    padding: 0 10px;
  }
</style>
