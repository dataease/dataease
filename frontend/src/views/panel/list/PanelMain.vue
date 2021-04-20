<template>
  <de-container>
    <de-aside-container>
      <el-tabs v-model="activeName" @tab-click="handleClick">
        <el-tab-pane name="PanelList">
          <span slot="label"><i class="el-icon-document" />列表</span>
          <panel-list />
        </el-tab-pane>
        <el-tab-pane name="panels_star" :lazy="true">
          <span slot="label"><i class="el-icon-star-off" />收藏</span>
          <enshrine v-if="showEnshrine" />
        </el-tab-pane>
        <el-tab-pane name="panels_share" :lazy="true">
          <span slot="label"><i class="el-icon-share" />分享</span>
          <share-tree v-if="showShare" />
        </el-tab-pane>
      </el-tabs>
    </de-aside-container>
    <de-main-container>
      <PanelViewShow />
    </de-main-container>
  </de-container>
</template>

<script>
import DeMainContainer from '@/components/dataease/DeMainContainer'
import DeContainer from '@/components/dataease/DeContainer'
import DeAsideContainer from '@/components/dataease/DeAsideContainer'
import PanelList from '../list/PanelList'
import PanelViewShow from '../list/PanelViewShow'
import ShareTree from '../GrantAuth/shareTree'
import Enshrine from '../enshrine/index'

export default {
  name: 'PanelMain',
  components: { DeMainContainer, DeContainer, DeAsideContainer, PanelList, PanelViewShow, ShareTree, Enshrine },
  data() {
    return {
      activeName: 'PanelList',
      showShare: false,
      showEnshrine: false
    }
  },
  methods: {
    handleClick(tab, event) {
      // 点击分析面板需要刷新分享内容
      if (tab.name === 'panels_share') {
        this.refreshShare()
      }
      if (tab.name === 'panels_star') {
        this.refreshEnshrine()
      }
    },
    refreshShare() {
      this.showShare = false
      this.$nextTick(() => (this.showShare = true))
    },
    refreshEnshrine() {
      this.showEnshrine = false
      this.$nextTick(() => (this.showEnshrine = true))
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
