<template>
  <de-container>

    <de-aside-container>
      <el-tabs v-model="activeName" @tab-click="handleClick">
        <el-tab-pane name="PanelList">
          <span slot="label"><i class="el-icon-document" />列表</span>
          <PanelList @switchComponent="switchComponent" />
        </el-tab-pane>
        <el-tab-pane name="panels_star">
          <span slot="label"><i class="el-icon-star-off" />收藏</span>
          开发中...
        </el-tab-pane>
        <el-tab-pane name="panels_share">
          <span slot="label"><i class="el-icon-share" />分享</span>
          开发中...
        </el-tab-pane>
        <!--        <el-tab-pane name="example">-->
        <!--          <span slot="label"><i class="el-icon-star-on"></i>示例</span>-->
        <!--          <group @switchComponent="switchComponent"/>-->
        <!--        </el-tab-pane>-->
      </el-tabs>

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
// import Group from './group/Group'
import PanelList from './list/PanelList'
import PanelView from './list/PanelView'
import PanelViewShow from './list/PanelViewShow'

export default {
  name: 'Panel',
  components: { DeMainContainer, DeContainer, DeAsideContainer, PanelList, PanelView, PanelViewShow },
  data() {
    return {
      component: PanelViewShow,
      param: {},
      activeName: 'PanelList'
    }
  },
  methods: {
    handleClick(tab, event) {
      console.log(tab, event)
    },
    switchComponent(c) {
      console.log(c)
      this.param = c.param
      switch (c.name) {
        case 'PanelViewShow':
          this.component = PanelViewShow
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
