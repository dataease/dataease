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
import { queryPanelJumpInfo } from '@/api/panel/linkJump'

export default {
  name: 'LinkView',
  components: { Preview },
  props: {
    resourceId: {
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
        // 刷新联动信息
        getPanelAllLinkageInfo(this.resourceId).then(rsp => {
          this.$store.commit('setNowPanelTrackInfo', rsp.data)
        })
        this.$store.commit('setComponentData', this.resetID(JSON.parse(res.data.panelData)))
        // this.$store.commit('setComponentData', JSON.parse(res.data.panelData))
        this.$store.commit('setCanvasStyle', JSON.parse(res.data.panelStyle))
        // 刷新跳转信息
        queryPanelJumpInfo(this.resourceId).then(rsp => {
          this.$store.commit('setNowPanelJumpInfo', rsp.data)
        })
        this.show = true
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

<style scoped>
 *{
     margin: 0;
     padding: 0;
 }
</style>
