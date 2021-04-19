<template>
  <de-container>
    <de-aside-container>
      <el-tabs v-model="activeName" @tab-click="handleClick">
        <el-tab-pane name="SystemTemplate">
          <span slot="label"><i class="el-icon-document" />系统模板</span>
          <system-template-list @showCurrentTemplate="showCurrentTemplate" />
        </el-tab-pane>
        <el-tab-pane name="UserTemplate">
          <span slot="label"><i class="el-icon-star-off" />用户模板</span>
          开发中...
        </el-tab-pane>
      </el-tabs>
    </de-aside-container>
    <de-main-container>
      <!--      <el-card class="el-card-template">-->
      <template-item
        v-for="item in currentTemplateShowList"
        :key="item.id"
        :template="item"
      />
      <!--      </el-card>-->
    </de-main-container>
  </de-container>
</template>

<script>
import DeMainContainer from '@/components/dataease/DeMainContainer'
import DeContainer from '@/components/dataease/DeContainer'
import DeAsideContainer from '@/components/dataease/DeAsideContainer'
import SystemTemplateList from './component/SystemTemplateList'
import TemplateItem from './component/TemplateItem'
import { get, post } from '@/api/panel/panel'

export default {
  name: 'PanelMain',
  components: { DeMainContainer, DeContainer, DeAsideContainer, SystemTemplateList, TemplateItem },
  data() {
    return {
      activeName: 'SystemTemplate',
      showShare: false,
      currentTemplateShowList: []
    }
  },
  methods: {
    handleClick(tab, event) {
      // 点击分析面板需要刷新分享内容
      if (tab.name === 'panels_share') {
        this.refreshShare()
      }
    },
    refreshShare() {
      this.showShare = false
      this.$nextTick(() => (this.showShare = true))
    },
    get() {
      this.showShare = false
      this.$nextTick(() => (this.showShare = true))
    },
    showCurrentTemplate(currentPid) {
      if (currentPid) {
        post('/template/templateList', { pid: currentPid }).then(response => {
          this.currentTemplateShowList = response.data
        })
      }
    }
  }
}
</script>

<style scoped>
  .el-card-template {
    min-width: 260px;
    min-width: 460px;
    width: 100%;
    height: 100%;
  }

</style>
