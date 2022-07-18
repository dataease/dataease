<template>
  <de-container v-loading="$store.getters.loadingMap[$store.getters.currentPath]" style="background-color: #f7f8fa;position:relative;">
    <de-main-container :class="{'full-height':fullHeightFlag}">
      <panel-main v-show="componentName==='PanelMain'" ref="panel_main" />
      <chart-edit v-if="componentName==='ChartEdit'" :param="param" />
      <panel-edit v-if="componentName==='PanelEdit'" />
    </de-main-container>
    <!-- <div class="nav_log">213213</div> -->
  </de-container>
</template>

<script>
import bus from '@/utils/bus'
import DeMainContainer from '@/components/datains/DeMainContainer'
import DeContainer from '@/components/datains/DeContainer'
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
  computed: {
    fullHeightFlag() {
      return this.$route.path.indexOf('panel') > -1 && (this.componentName === 'PanelEdit' || this.componentName === 'ChartEdit')
    }
  },
  watch: {
    $route(to, from) {
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
    })
  },
  created() {
    bus.$emit('PanelSwitchComponent', { name: 'PanelMain' })
    this.$store.dispatch('app/toggleSideBarHide', true)
    const routerParam = this.$router.currentRoute.params
    this.toMsgShare(routerParam)
  },
  methods: {
    toMsgShare(routerParam) {
      if (routerParam !== null && routerParam.msgNotification) {
        const panelShareTypeIds = [1, 2, 3]
        // 说明是从消息通知跳转过来的
        if (panelShareTypeIds.includes(routerParam.msgType)) { // 是仪表板分享
          this.componentName = 'PanelMain'
          this.$nextTick(() => {
            this.$refs.panel_main && this.$refs.panel_main.msg2Current && this.$refs.panel_main.msg2Current(routerParam.sourceParam)
          })
        }
      }
    }

  }
}
</script>

<style scoped>
    .nav_log{
      position:absolute;

    }
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

  .full-height {
    height: 100vh !important;
  }
</style>
