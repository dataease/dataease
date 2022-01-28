<template>
  <div v-loading="dataLoading" class="bg" :style="bgStyle">
    <Preview v-if="!dataLoading" :back-screen-shot="backScreenShot" @mainHeightChange="mainHeightChange" />
  </div>
</template>
<script>
import Preview from './Preview'
import { uuid } from 'vue-uuid'
import { initPanelData } from '@/api/panel/panel'
import { queryTargetPanelJumpInfo } from '@/api/panel/linkJump'

export default {
  components: { Preview },
  data() {
    return {
      dataLoading: false,
      backScreenShot: false,
      mainHeight: '100vh!important'
    }
  },
  watch: {
    '$route.params.reportId': function() {
      this.restore()
    }
  },
  computed: {
    bgStyle() {
      if (this.backScreenShot) {
        return { height: this.mainHeight }
      } else {
        return { height: '100vh!important' }
      }
    }
  },
  mounted() {
    this.restore()
  },
  methods: {
    mainHeightChange(mainHeight) {
      this.mainHeight = mainHeight
    },
    restore() {
      const _this = this
      _this.dataLoading = true
      _this.panelId = this.$route.params.reportId
      if (_this.$route.params.backScreenShot !== undefined) {
        _this.backScreenShot = _this.$route.params.backScreenShot
      }
      // 加载视图数据
      initPanelData(this.panelId, function() {
        // 如果含有跳转参数 进行触发
        const tempParam = localStorage.getItem('jumpInfoParam')
        if (tempParam) {
          localStorage.removeItem('jumpInfoParam')
          const jumpParam = JSON.parse(tempParam)
          const jumpRequestParam = {
            sourcePanelId: jumpParam.sourcePanelId,
            sourceViewId: jumpParam.sourceViewId,
            sourceFieldId: jumpParam.sourceFieldId,
            targetPanelId: _this.panelId
          }
          _this.dataLoading = true
          // 刷新跳转目标仪表板联动信息
          queryTargetPanelJumpInfo(jumpRequestParam).then(rsp => {
            _this.dataLoading = false
            _this.$store.commit('setNowTargetPanelJumpInfo', rsp.data)
            _this.$store.commit('addViewTrackFilter', jumpParam)
          })
        }
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
    min-width: 200px;
    min-height: 300px;
    background-color: #f7f8fa;
  }
</style>

