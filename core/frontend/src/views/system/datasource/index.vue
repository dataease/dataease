<template>
  <de-container
    v-loading="$store.getters.loadingMap[$store.getters.currentPath]"
  >
    <ds-main ref="dsMain" />
  </de-container>
</template>

<script>
import DeContainer from '@/components/dataease/DeContainer'
import DsMain from './DsMain'
import bus from '@/utils/bus'

export default {
  name: 'Panel',
  components: { DeContainer, DsMain },
  data() {
    return {
      component: DsMain,
      componentName: 'DsMain',
      param: {}
    }
  },
  mounted() {
    bus.$on('to-msg-ds', this.toMsgDs)
  },
  beforeDestroy() {
    bus.$off('to-msg-ds', this.toMsgDs)
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
        if (panelShareTypeIds.includes(routerParam.msgType)) {
          // 是数据集同步
          if (routerParam.sourceParam) {
            try {
              this.$nextTick(() => {
                this.$refs.dsMain &&
                  this.$refs.dsMain.msg2Current &&
                  this.$refs.dsMain.msg2Current(routerParam.sourceParam)
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
</style>
