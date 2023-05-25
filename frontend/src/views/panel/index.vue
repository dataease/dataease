<template>
  <de-container
    v-loading="$store.getters.loadingMap[$store.getters.currentPath]"
    style="background-color: #f7f8fa"
  >
    <de-main-container :class="{'full-height':fullHeightFlag}">
      <panel-main
        v-show="componentName==='PanelMain'"
        ref="panel_main"
      />
      <panel-edit v-if="componentName==='PanelEdit'" />
    </de-main-container>
  </de-container>
</template>

<script>
import bus from '@/utils/bus'
import DeMainContainer from '@/components/dataease/DeMainContainer'
import DeContainer from '@/components/dataease/DeContainer'
import PanelMain from '@/views/panel/list/PanelMain'
import PanelEdit from '@/views/panel/edit'
export default {
  name: 'Panel',
  components: { DeMainContainer, DeContainer, PanelMain, PanelEdit },
  beforeRouteLeave(to, from, next) {
    if (this.componentName === 'PanelEdit') {
      next(false)
      if (confirm(this.$t('panel.edit_leave_tips'))) {
        bus.$emit('PanelSwitchComponent', { name: 'PanelMain' })
        next()
      }
    } else {
      next()
    }
  },
  data() {
    return {
      component: PanelMain,
      componentName: 'PanelMain',
      param: {},
      contentHasSave: false
    }
  },
  computed: {
    fullHeightFlag() {
      return this.$route.path.indexOf('panel') > -1 && (this.componentName === 'PanelEdit' || this.componentName === 'ChartEdit')
    }
  },
  watch: {
  },
  mounted() {
    bus.$on('to-msg-share', this.toMsgShare)
  },
  beforeDestroy() {
    bus.$off('to-msg-share', this.toMsgShare)
    bus.$off('PanelSwitchComponent', this.panelSwitchComponent)
  },
  created() {
    bus.$on('PanelSwitchComponent', this.panelSwitchComponent)
    this.$store.dispatch('app/toggleSideBarHide', true)
    const routerParam = this.$router.currentRoute.params
    this.toMsgShare(routerParam)
  },
  methods: {
    panelSwitchComponent(c) {
      this.param = c.param
      this.componentName = c.name
      this.$store.dispatch('panel/setMainActiveName', c.name)
    },
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
