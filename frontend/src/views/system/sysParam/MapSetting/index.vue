<template>
  <de-container
    v-loading="$store.getters.loadingMap[$store.getters.currentPath]"
    class="de-earth"
    style="height: calc(100vh - 150px);"
  >

    <de-aside-container
      type="mapset"
      style="height: 100%;"
    >
      <map-setting-left
        ref="map_setting_tree"
        :tree-data="treeData"
        @emit-add="emitAdd"
        @refresh-tree="refreshTree"
        @show-node-info="loadForm"
      />
    </de-aside-container>

    <de-main-container style="height: 100%;">
      <map-setting-right
        ref="map_setting_form"
        :tree-data="treeData"
        :status="formStatus"
        @refresh-tree="refreshTree"
      />
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
      treeData: []
    }
  },
  created() {
    this.loadTreeData()
  },
  methods: {
    emitAdd(form) {
      this.setStatus(form.status)
      this.$refs['map_setting_form']?.emitAdd(form)
    },

    loadForm(nodeInfo) {
      this.setStatus(nodeInfo.status)
      this.$refs['map_setting_form']?.loadForm(nodeInfo)
    },

    setStatus(status) {
      this.formStatus = status
    },
    loadTreeData() {
      !Object.keys(this.treeData).length && areaMapping().then(res => {
        this.treeData = res.data
      })
    },
    refreshTree(node) {
      areaMapping().then(res => {
        this.treeData = res.data
        if (!node?.code) return
        this.$refs['map_setting_tree']?.showNewNode(node.code)
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
