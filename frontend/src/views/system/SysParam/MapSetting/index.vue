<template>
  <de-container class="de-earth" v-loading="$store.getters.loadingMap[$store.getters.currentPath]" style="height: calc(100vh - 150px);">

    <de-aside-container type="mapset" style="height: 100%;">
      <map-setting-left ref="map_setting_tree" :tree-datas="treeDatas" @emit-add="emitAdd" @refresh-tree="refreshTree" @show-node-info="loadForm" />
    </de-aside-container>

    <de-main-container style="height: 100%;">
      <map-setting-right ref="map_setting_form" :tree-datas="treeDatas" :status="formStatus" @refresh-tree="refreshTree" />
    </de-main-container>
  </de-container>
</template>

<script>
import DeMainContainer from '@/components/dataease/DeMainContainer'
import DeContainer from '@/components/dataease/DeContainer'
import DeAsideContainer from '@/components/dataease/DeAsideContainer'
import { areaMapping } from '@/api/map/map'
import MapSettingLeft from './MapSettingLeft'
import MapSettingRight from './MapSettingRight'
export default {
  name: 'MapSetting',
  components: { DeMainContainer, DeContainer, DeAsideContainer, MapSettingLeft, MapSettingRight },
  data() {
    return {
      formStatus: 'empty',
      treeDatas: []
    }
  },
  created() {
    this.loadTreeData()
  },
  methods: {
    emitAdd(form) {
      this.setStatus(form.status)
      this.$refs && this.$refs['map_setting_form'] && this.$refs['map_setting_form'].emitAdd(form)
    },

    loadForm(nodeInfo) {
      this.setStatus(nodeInfo.status)
      this.$refs && this.$refs['map_setting_form'] && this.$refs['map_setting_form'].loadForm(nodeInfo)
    },

    setStatus(status) {
      this.formStatus = status
    },
    loadTreeData() {
      Object.keys(this.treeDatas).length === 0 && areaMapping().then(res => {
        this.treeDatas = res.data
      })
    },
    refreshTree(node) {
      areaMapping().then(res => {
        this.treeDatas = res.data
        if (node && node.code) {
          this.$refs && this.$refs['map_setting_tree'] && this.$refs['map_setting_tree'].showNewNode(node.code)
        }
      })
    }
  }
}
</script>

<style lang="scss" scoped>
.de-earth {
  padding: 24px;
  width: 100%;
  overflow: auto;
}
</style>
