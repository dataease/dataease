<template>
  <de-container>
    <de-aside-container type="panel">
      <el-tabs v-model="activeName" class="tab-panel" :stretch="true" @tab-click="handleClick">
        <el-tab-pane name="PanelList">
          <span slot="label"><i class="el-icon-document tablepanel-i" />{{ $t('panel.panel_list') }}</span>
          <panel-list v-show="activeName==='PanelList'" ref="panelList" />
        </el-tab-pane>
        <el-tab-pane name="panels_star" :lazy="true">
          <span slot="label"><i class="el-icon-star-off tablepanel-i" />{{ $t('panel.store') }}</span>
          <enshrine v-if="activeName==='panels_star'" />
        </el-tab-pane>
        <el-tab-pane name="panels_share" :lazy="true">
          <span slot="label"><i class="el-icon-share tablepanel-i" />{{ $t('panel.share') }}</span>
          <share-tree v-if="showShare" ref="share_tree" :msg-panel-ids="msgPanelIds" />
        </el-tab-pane>
      </el-tabs>
    </de-aside-container>
    <de-main-container>
      <PanelViewShow v-if="mainActiveName==='PanelMain'" :active-tab="activeName" @editPanel="editPanel" @editPanelBashInfo="editPanelBashInfo" />
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
import { pluginTypes } from '@/api/chart/chart'

export default {
  name: 'PanelMain',
  components: { DeMainContainer, DeContainer, DeAsideContainer, PanelList, PanelViewShow, ShareTree, Enshrine },
  data() {
    return {
      activeName: 'PanelList',
      showShare: false,
      showEnshrine: false,
      msgPanelIds: null
    }
  },
  computed: {
    mainActiveName() {
      return this.$store.state.panel.mainActiveName
    }
  },
  watch: {
    // 切换展示页面后 重新点击一下当前节点
    '$store.state.panel.mainActiveName': function(newVal, oldVal) {
      if (newVal === 'PanelMain' && this.lastActiveNodeData) {
        this.activeNodeAndClickOnly(this.lastActiveNodeData)
      }
    },
    activeName: function(newVal, oldVal) {
      this.clear()
    }
  },
  mounted() {
    // init all views (include plugins) base info
    localStorage.removeItem('plugin-views')
    pluginTypes().then(res => {
      const plugins = res.data
      localStorage.setItem('plugin-views', JSON.stringify(plugins))
      this.$store.commit('initViewRender', plugins)
    }).catch(e => {
      localStorage.setItem('plugin-views', null)
      this.$store.commit('initViewRender', [])
    })
    // this.clear()
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
    },
    clear() {
      // 清空
      this.$store.dispatch('panel/setPanelInfo', {
        id: null,
        name: '',
        preStyle: null
      })
      this.$store.dispatch('panel/setMainActiveName', 'PanelMain')
    },
    msg2Current(panelIds) {
      this.refreshShare()
      this.$nextTick(() => {
        if (panelIds) {
          try {
            panelIds = JSON.parse(panelIds)
            this.msgPanelIds = panelIds
            this.activeName = 'panels_share'
          } catch (error) {
            console.error(error)
          }
        }
      })
    },
    editPanel() {
      this.$refs.panelList.editFromPanelViewShow()
    },
    editPanelBashInfo(param) {
      this.$refs.panelList.editPanelBashInfo(param)
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
  .tab-panel{
    height: 100%;
    overflow-y: auto;
  }
  .tab-panel ::v-deep .el-tabs__nav-wrap{
    padding: 0 10px;
  }
  .tab-panel ::v-deep .el-tabs__nav-wrap::after {
    height: 1px;
  }
  .tab-panel ::v-deep .el-tabs__item{
    /* width: 10px; */
    padding: 0 10px;
  }
</style>
