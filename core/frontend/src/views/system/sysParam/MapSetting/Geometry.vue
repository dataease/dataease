<template>
  <de-container
    v-loading="$store.getters.loadingMap[$store.getters.currentPath]"
    class="de-earth"
  >
    <div class="de-map-tips">
      <el-alert
        :title="$t('map_setting.prohibit_prompts')"
        type="warning"
        description=""
        :closable="false"
        show-icon
      />
    </div>
    <de-aside-container
      type="mapset"
      close
      class="map-setting-aside"
    >
      <map-setting-left
        ref="map_setting_tree"
        :tree-data="treeData"
        @emit-add="emitAdd"
        @refresh-tree="refreshTree"
        @show-node-info="loadForm"
      />
    </de-aside-container>

    <de-main-container
      class="map-setting-main"
    >
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
  width: 100%;
  height: 100%;
  overflow: auto;
  .de-map-tips {
    position: absolute;
    width: calc(100% - 310px);
  }
  .map-setting-aside {
    top: 50px;
    height: calc(100% - 50px) !important;
  }
  .map-setting-main {
    margin-top: 50px;
    height: calc(100% - 50px);
  }
}
</style>
