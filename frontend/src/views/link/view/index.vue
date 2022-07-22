<template>
  <div style="width: 100%;height: 100vh;background-color: #f7f8fa">
    <Preview
      v-if="show"
      :component-data="componentData"
      :canvas-style-data="canvasStyleData"
      :panel-info="panelInfo"
    />
  </div>
</template>

<script>
import { loadResource } from '@/api/link'
import { uuid } from 'vue-uuid'
import Preview from '@/components/canvas/components/Editor/Preview'
import { getPanelAllLinkageInfo } from '@/api/panel/linkage'
import { queryPanelJumpInfo, queryTargetPanelJumpInfo } from '@/api/panel/linkJump'
import { panelInit } from '@/components/canvas/utils/utils'
import { getOuterParamsInfo } from '@/api/panel/outerParams'
import { mapState } from 'vuex'

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
      show: false,
      panelInfo: {}
    }
  },
  computed: {
    ...mapState([
      'canvasStyleData',
      'componentData'
    ])
  },
  created() {
    this.show = false
    this.setPanelInfo()
  },
  methods: {
    setPanelInfo() {
      loadResource(this.resourceId).then(res => {
        this.show = false
        let loadingCount = 0
        this.panelInfo = {
          id: res.data.id,
          name: res.data.name,
          privileges: res.data.privileges,
          status: res.data.status,
          createBy: res.data.createBy,
          createTime: res.data.createTime,
          updateBy: res.data.updateBy,
          updateTime: res.data.updateTime
        }
        this.$store.dispatch('panel/setPanelInfo', this.panelInfo)

        panelInit(JSON.parse(res.data.panelData), JSON.parse(res.data.panelStyle))
        // 设置浏览器title为当前仪表板名称
        document.title = res.data.name
        // 刷新联动信息
        getPanelAllLinkageInfo(this.resourceId).then(rsp => {
          this.$store.commit('setNowPanelTrackInfo', rsp.data)
        })
        // 刷新跳转信息
        queryPanelJumpInfo(this.resourceId).then(rsp => {
          this.$store.commit('setNowPanelJumpInfo', rsp.data)
        })

        // 如果含有跳转参数 进行触发
        const tempParam = localStorage.getItem('jumpInfoParam')
        // 添加外部参数
        const attachParamsEncode = this.$route.query.attachParams

        tempParam && loadingCount++
        attachParamsEncode && loadingCount++

        if (attachParamsEncode) {
          try {
            const Base64 = require('js-base64').Base64
            const attachParam = JSON.parse(decodeURIComponent(Base64.decode(attachParamsEncode)))
            getOuterParamsInfo(this.resourceId).then(rsp => {
              if (--loadingCount === 0) {
                this.show = true
              }
              this.$store.commit('setNowPanelOuterParamsInfo', rsp.data)
              this.$store.commit('addOuterParamsFilter', attachParam)
            })
          } catch (e) {
            if (--loadingCount === 0) {
              this.show = true
            }

            this.$message({
              message: this.$t('panel.outer_param_decode_error'),
              type: 'error'
            })
            console.error('outerParams Decode error：', e)
          }
        }

        if (tempParam) {
          try {
            localStorage.removeItem('jumpInfoParam')
            const jumpParam = JSON.parse(tempParam)
            const jumpRequestParam = {
              sourcePanelId: jumpParam.sourcePanelId,
              sourceViewId: jumpParam.sourceViewId,
              sourceFieldId: jumpParam.sourceFieldId,
              targetPanelId: this.resourceId
            }
            // 刷新跳转目标仪表板联动信息
            queryTargetPanelJumpInfo(jumpRequestParam).then(rsp => {
              this.show = true
              this.$store.commit('setNowTargetPanelJumpInfo', rsp.data)
              this.$store.commit('addViewTrackFilter', jumpParam)
            })
          } catch (e) {
            if (--loadingCount === 0) {
              this.show = true
            }
            console.error('tempParam error：', e)
          }
        }

        if (loadingCount === 0) {
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
