<template>
  <div style="width: 100%;height: 100vh;background-color: #f7f8fa">
    <Preview v-if="show" />
  </div>
</template>

<script>
import { loadResource } from '@/api/link'
import { uuid } from 'vue-uuid'
import Preview from '@/components/canvas/components/Editor/Preview'
import { getPanelAllLinkageInfo } from '@/api/panel/linkage'
import { queryPanelJumpInfo, queryTargetPanelJumpInfo } from '@/api/panel/linkJump'

export default {
  name: 'LinkView',
  components: { Preview },
  props: {
    resourceId: {
      type: String,
      default: null
    },
    user: {
      type: String,
      default: null
    }
  },
  data() {
    return {
      show: false
    }
  },
  created() {
    this.show = false
    this.setPanelInfo()
  },
  methods: {
    setPanelInfo() {
      loadResource(this.resourceId).then(res => {
        this.$store.dispatch('panel/setPanelInfo', {
          id: res.data.id,
          name: res.data.name
        })
        // 设置浏览器title为当前仪表板名称
        document.title = res.data.name
        // 刷新联动信息
        getPanelAllLinkageInfo(this.resourceId).then(rsp => {
          this.$store.commit('setNowPanelTrackInfo', rsp.data)
        })
        this.$store.commit('setComponentData', this.resetID(JSON.parse(res.data.panelData)))
        this.$store.commit('setCanvasStyle', JSON.parse(res.data.panelStyle))
        // 刷新跳转信息
        queryPanelJumpInfo(this.resourceId).then(rsp => {
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
            targetPanelId: this.resourceId
          }
          this.show = false
          // 刷新跳转目标仪表板联动信息
          queryTargetPanelJumpInfo(jumpRequestParam).then(rsp => {
            this.show = true
            this.$store.commit('setNowTargetPanelJumpInfo', rsp.data)
            this.$store.commit('addViewTrackFilter', jumpParam)
          })
        } else {
          this.show = true
        }
      })
    },
    resetID(data) {
      if (data) {
        data.forEach(item => {
          item.type !== 'custom' && (item.id = uuid.v1())
          // item.user = this.user
          item.fromSource = 'link'
        })
      }
      return data
    }

  }
}
</script>

<style scoped>
 *{
     margin: 0;
     padding: 0;
 }
</style>
