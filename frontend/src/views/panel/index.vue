<template>
  <ms-container>

    <ms-aside-container>
      <el-tabs v-model="activeName" @tab-click="handleClick">
        <el-tab-pane name="PanelList">
          <span slot="label"><i class="el-icon-document"></i>列表</span>
          <PanelList @switchComponent="switchComponent"/>
        </el-tab-pane>
        <el-tab-pane name="panels_star">
          <span slot="label"><i class="el-icon-star-off"></i>收藏</span>
          开发中...
        </el-tab-pane>
        <el-tab-pane name="panels_share">
          <span slot="label"><i class="el-icon-share"></i>分享</span>
          开发中...
        </el-tab-pane>
<!--        <el-tab-pane name="example">-->
<!--          <span slot="label"><i class="el-icon-star-on"></i>示例</span>-->
<!--          <group @switchComponent="switchComponent"/>-->
<!--        </el-tab-pane>-->
      </el-tabs>

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
  import PanelList from './list/PanelList'
  import PanelView from './list/PanelView'

  export default {
    name: 'Panel',
    components: {MsMainContainer, MsContainer, MsAsideContainer, Group, PanelList,PanelView},
    data() {
      return {
        component: PanelView,
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
          case 'PanelView':
            this.component = PanelView
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
