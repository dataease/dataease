<template>
  <de-container>

    <de-aside-container>
      <el-tabs v-model="activeName" @tab-click="handleClick">
        <el-tab-pane name="PanelList">
          <span slot="label"><i class="el-icon-document" />列表</span>
          <panel-list />
        </el-tab-pane>
        <el-tab-pane name="panels_star">
          <span slot="label"><i class="el-icon-star-off" />收藏</span>
          开发中...
        </el-tab-pane>
        <el-tab-pane name="panels_share" :lazy="true">
          <span slot="label"><i class="el-icon-share" />分享</span>
          <share-tree v-if="showShare" />
        </el-tab-pane>

      </el-tabs>

    </de-aside-container>

    <de-main-container>
      <component :is="component" :param="param" />
    </de-main-container>
  </de-container>
</template>

<script>
import DeMainContainer from '@/components/dataease/DeMainContainer'
import DeContainer from '@/components/dataease/DeContainer'
import DeAsideContainer from '@/components/dataease/DeAsideContainer'
// import Group from './group/Group'
import PanelList from './list/PanelList'
import PanelViewShow from './list/PanelViewShow'
import ShareTree from './GrantAuth/shareTree'

export default {
  name: 'Panel',
  components: { DeMainContainer, DeContainer, DeAsideContainer, PanelList, PanelViewShow, ShareTree },
  data() {
    return {
      component: PanelViewShow,
      param: {},
      activeName: 'PanelList',
      showShare: false
    }
  },
  methods: {
    handleClick(tab, event) {
      // 点击分析面板需要刷新分享内容
      if (tab.name === 'panels_share') {
        this.refreshShare()
      }
    },
    // switchComponent(c) {
    //   console.log(c)
    //   this.param = c.param
    //   switch (c.name) {
    //     case 'PanelViewShow':
    //       this.component = PanelViewShow
    //       break
    //   }
    // },
    refreshShare() {
      this.showShare = false
      this.$nextTick(() => (this.showShare = true))
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
