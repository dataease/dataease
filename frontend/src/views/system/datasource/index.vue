<template>
  <de-container v-loading="$store.getters.loadingMap[$store.getters.currentPath]" style="background-color: #f7f8fa">
    <de-main-container>
      <ds-main ref="dsMain" />
    </de-main-container>
  </de-container>
</template>

<script>
import DeMainContainer from '@/components/dataease/DeMainContainer'
import DeContainer from '@/components/dataease/DeContainer'
import DsMain from './DsMain'
import bus from '@/utils/bus'
export default {
  name: 'Panel',
  components: { DeMainContainer, DeContainer, DsMain },
  data() {
    return {
      component: DsMain,
      componentName: 'DsMain',
      param: {}
    }
  },
  mounted() {
    bus.$on('to-msg-ds', params => {
      this.toMsgDs(params)
    })
  },
  created() {
    this.$store.dispatch('app/toggleSideBarHide', true)
    const routerParam = this.$router.currentRoute.params
    this.toMsgDs(routerParam)
  },
  methods: {
    toMsgDs(routerParam) {
      if (routerParam !== null && routerParam.msgNotification) {
        const panelShareTypeIds = [7, 8]
        // 说明是从消息通知跳转过来的
        if (panelShareTypeIds.includes(routerParam.msgType)) { // 是数据集同步
          if (routerParam.sourceParam) {
            try {
            //   const msgParam = JSON.parse(routerParam.sourceParam)
            //   this.param = msgParam.id
            //   this.component = ViewTable
              this.$nextTick(() => {
                this.$refs.dsMain && this.$refs.dsMain.msg2Current && this.$refs.dsMain.msg2Current(routerParam.sourceParam)
              })
            } catch (error) {
              console.error(error)
            }
          }
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
