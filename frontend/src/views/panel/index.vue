<template>
  <de-container v-loading="$store.getters.loadingMap[$store.getters.currentPath]" style="background-color: #f7f8fa">
    <de-main-container>
      <panel-main v-show="componentName==='PanelMain'" ref="panel_main" />
      <chart-edit v-if="componentName==='ChartEdit'" :param="param" />
      <panel-edit v-if="componentName==='PanelEdit'" />
      <!--      <component :is="component" :param="param" />-->
    </de-main-container>
  </de-container>
</template>

<script>
import bus from '@/utils/bus'
import DeMainContainer from '@/components/dataease/DeMainContainer'
import DeContainer from '@/components/dataease/DeContainer'
import PanelMain from '@/views/panel/list/PanelMain'
import ChartEdit from '@/views/chart/view/ChartEdit'
import PanelEdit from '@/views/panel/edit'

export default {
  name: 'Panel',
  components: { DeMainContainer, DeContainer, PanelMain, ChartEdit, PanelEdit },
  data() {
    return {
      component: PanelMain,
      componentName: 'PanelMain',
      param: {}
    }
  },
  watch: {
    $route(to, from) {
      console.log(to)
      console.log(from)
      // 对路由变化作出响应...
    }
  },
  mounted() {
    bus.$on('to-msg-share', params => {
      this.toMsgShare(params)
    })
    bus.$on('PanelSwitchComponent', (c) => {
      this.param = c.param
      this.componentName = c.name
      this.$store.dispatch('panel/setMainActiveName', c.name)
      // switch (c.name) {
      //   case 'PanelEdit':
      //     this.component = PanelEdit
      //     this.componentName = 'PanelEdit'
      //     break
      //   case 'ChartEdit':
      //     this.component = ChartEdit
      //     this.componentName = 'ChartEdit'
      //     break
      //   default:
      //     this.component = PanelMain
      //     this.componentName = 'PanelMain'
      //     break
      // }
    })
  },
  created() {
    this.$store.dispatch('app/toggleSideBarHide', true)
    const routerParam = this.$router.currentRoute.params
    // if ((routerParam = this.$router.currentRoute.params) !== null && routerParam.msgNotification) {
    //   // 说明是从消息通知跳转过来的
    //   if (routerParam.msgType === 0) { // 是仪表板分享
    //     this.componentName = 'PanelMain'
    //     this.$nextTick(() => {
    //       this.$refs.panel_main.msg2Current(routerParam.sourceParam)
    //     })
    //   }
    // }
    this.toMsgShare(routerParam)
  },
  methods: {
    toMsgShare(routerParam) {
      if (routerParam !== null && routerParam.msgNotification) {
      // 说明是从消息通知跳转过来的
        if (routerParam.msgType === 0) { // 是仪表板分享
          this.componentName = 'PanelMain'
          this.$nextTick(() => {
            this.$refs.panel_main.msg2Current(routerParam.sourceParam)
          })
        }
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
    padding: 0;
  }

</style>
