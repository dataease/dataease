<template>
  <de-container
    v-loading="panelLoading"
    class="main-class"
  >
    <de-aside-container v-loading="viewLoading" class="aside-class" type="panel">
      <multiplexing-view :view-data="viewData" @showDetails="showDetails" />
    </de-aside-container>
    <de-main-container>
      <Preview
        v-if="selectedPanel"
        :component-data="componentData"
        :canvas-style-data="canvasStyleData"
        :panel-info="panelInfo"
        :show-position="showPosition"
      />
      <el-col v-else style="height: 100%;">
        <el-row style="height: 100%; background-color: var(--ContentBG);" class="custom-position">
          {{ $t('panel.select_panel_from_left') }}
        </el-row>
      </el-col>
    </de-main-container>
  </de-container>
</template>

<script>
import DeMainContainer from '@/components/dataease/DeMainContainer'
import DeContainer from '@/components/dataease/DeContainer'
import DeAsideContainer from '@/components/dataease/DeAsideContainer'
import { findOne } from '@/api/panel/panel'
import { deepCopy, panelDataPrepare } from '@/components/canvas/utils/utils'
import Preview from '@/components/canvas/components/Editor/Preview'
import MultiplexingView from '@/views/panel/ViewSelect/multiplexingView'
import { DEFAULT_COMMON_CANVAS_STYLE_STRING } from '@/views/panel/panel'
import { USER_VIEW } from '@/components/canvas/custom-component/component-list'
import { uuid } from 'vue-uuid'

export default {
  name: 'Multiplexing',
  components: { MultiplexingView, Preview, DeMainContainer, DeContainer, DeAsideContainer },
  props: {
    viewData: {
      type: Array,
      required: true
    }
  },
  data() {
    return {
      showPosition: 'multiplexing',
      activeName: 'PanelList',
      viewLoading: false,
      panelLoading: false,
      showShare: false,
      showEnshrine: false,
      msgPanelIds: null,
      componentData: [],
      canvasStyleData: {},
      selectedPanel: null,
      panelInfo: {}
    }
  },
  watch: {

  },
  mounted() {
    this.$store.commit('initCurMultiplexingComponents')
  },
  methods: {
    showDetails(params) {
      this.$store.commit('initCurMultiplexingComponents')
      const _this = this
      _this.selectedPanel = null
      if (params.showType === 'panel') {
        _this.showPosition = 'multiplexing'
        _this.panelLoading = true
        findOne(params.showId).then(response => {
          _this.panelLoading = false
          this.panelInfo = {
            id: response.data.id,
            name: response.data.name,
            privileges: response.data.privileges,
            sourcePanelName: response.data.sourcePanelName,
            status: response.data.status
          }
          panelDataPrepare(JSON.parse(response.data.panelData), JSON.parse(response.data.panelStyle), function(rsp) {
            _this.componentData = rsp.componentData
            _this.canvasStyleData = rsp.componentStyle
            _this.selectedPanel = params
          })
        })
      } else if (params.showType === 'view') {
        _this.showPosition = 'multiplexing-view'
        _this.componentData = []
        const componentId = uuid.v1()
        _this.canvasStyleData = deepCopy(DEFAULT_COMMON_CANVAS_STYLE_STRING)
        const userView = {
          ... deepCopy(USER_VIEW),
          'id': componentId,
          'auxiliaryMatrix': false }
        userView.style.width = _this.canvasStyleData.width
        userView.style.height = _this.canvasStyleData.height
        userView['propValue'] = {
          'viewId': params.showId,
          'id': componentId
        }
        this.panelInfo = {
          'showType': 'view'
        }
        _this.$nextTick(() => {
          _this.componentData.push(userView)
          _this.selectedPanel = params
        })
      }
    }
  }
}
</script>

<style scoped>
  .ms-aside-container {
    height: calc(100vh - 120px);
    padding: 0px;
    min-width: 260px;
    max-width: 460px;
  }
  .ms-main-container {
    height: calc(100vh - 120px);
    padding: 0px;
  }
  .tab-panel{
    height: 100%;
    overflow-y: auto;
  }
  .tab-panel ::v-deep .el-tabs__nav-wrap{
    padding: 0 10px;
  }
  .tab-panel ::v-deep .el-tabs__nav-wrap::after {
    height: 1px;
  }
  .tab-panel ::v-deep .el-tabs__item{
    /* width: 10px; */
    padding: 0 10px;
  }
  .main-class {
    border: 1px gainsboro solid;
  }

  ::v-deep .drag-bar{
    height: calc(100vh - 120px)!important;
  }

  .custom-position {
    flex: 1;
    display: flex;
    align-items: center;
    justify-content: space-between;
    font-size: 14px;
    flex-flow: row nowrap;
    color: #9ea6b2;
  }
</style>
