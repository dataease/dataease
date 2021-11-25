<template>
  <div v-loading="dataLoading" class="bg">
    <Preview v-if="!dataLoading" />
  </div>
</template>
<script>
import Preview from './Preview'
import { uuid } from 'vue-uuid'
import { findOne } from '@/api/panel/panel'
import { getPanelAllLinkageInfo } from '@/api/panel/linkage'
import { queryPanelJumpInfo, queryTargetPanelJumpInfo } from '@/api/panel/linkJump'

export default {
  components: { Preview },
  data() {
    return {
      dataLoading: false
    }
  },
  mounted() {
    this.restore()
  },
  methods: {
    restore() {
      this.dataLoading = true
      this.panelId = this.$route.path.split('/')[2]
      // 加载视图数据
      findOne(this.panelId).then(response => {
        this.dataLoading = false
        this.$store.commit('setComponentData', this.resetID(JSON.parse(response.data.panelData)))
        this.$store.commit('setCanvasStyle', JSON.parse(response.data.panelStyle))
        const data = {
          id: response.data.id,
          name: response.data.name
        }
        // 刷新联动信息
        getPanelAllLinkageInfo(this.panelId).then(rsp => {
          this.$store.commit('setNowPanelTrackInfo', rsp.data)
        })
        // 刷新跳转信息
        queryPanelJumpInfo(this.panelId).then(rsp => {
          this.$store.commit('setNowPanelJumpInfo', rsp.data)
        })

        // 如果含有跳转参数 进行触发
        const tempParam = localStorage.getItem('jumpInfoParam')
        if (tempParam) {
          localStorage.removeItem('jumpInfoParam')
          const jumpParam = JSON.parse(tempParam)
          const jumpRequestParam = {
            sourcePanelId: jumpParam.sourcePanelId,
            sourceViewId: jumpParam.sourceViewId,
            sourceFieldId: jumpParam.sourceFieldId,
            targetPanelId: this.panelId
          }
          this.dataLoading = true
          // 刷新跳转目标仪表板联动信息
          queryTargetPanelJumpInfo(jumpRequestParam).then(rsp => {
            this.dataLoading = false
            this.$store.commit('setNowTargetPanelJumpInfo', rsp.data)
            this.$store.commit('addViewTrackFilter', jumpParam)
          })
        }
        this.$store.dispatch('panel/setPanelInfo', data)
      })
    },
    resetID(data) {
      if (data) {
        data.forEach(item => {
          item.type !== 'custom' && (item.id = uuid.v1())
        })
      }
      return data
    }
  }
}
</script>

<style lang="scss" scoped>
  .bg {
    width: 100%;
    height: 100vh!important;
    min-width: 800px;
    min-height: 600px;
    background-color: #f7f8fa;
  }
</style>

