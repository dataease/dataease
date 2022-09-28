<template>
  <el-row>
    <el-header class="de-header">
      <!--横向工具栏-->
      <el-col :span="24">
        <Toolbar
          ref="toolbar"
          :style-button-active="show&&showIndex===2"
          :aided-button-active="aidedButtonActive"
          @showPanel="showPanel"
          @previewFullScreen="previewFullScreen"
          @changeAidedDesign="changeAidedDesign"
          @outerParamsSetVisibleChange="outerParamsSetVisibleChange"
        />
      </el-col>
    </el-header>
    <de-container>
      <de-aside-container class="ms-aside-container">
        <div v-show="showAside" style="width: 60px; left: 0px; top: 0px; bottom: 0px;  position: absolute">
          <div
            style="width: 60px;height: 100%;overflow: hidden auto;position: relative;margin: 0px auto; font-size: 14px"
          >
            <!-- 视图图表 start -->
            <div
              class="button-div-class"
              style=" width: 24px;height: 24px;text-align: center;line-height: 1;position: relative;margin: 16px auto 0px;"
            >
              <el-button circle class="el-icon-circle-plus-outline" size="mini" @click="newChart()" />
            </div>
            <div class="button-text" style="position: relative; margin: 18px auto 16px;">
              <div
                style="max-width: 100%;text-align: center;white-space: nowrap;text-overflow: ellipsis;position: relative;flex-shrink: 0;"
              >
                {{ $t('panel.view') }}
              </div>
            </div>
            <div style="height: 1px; position: relative; margin: 0px auto;background-color:#E6E6E6;">
              <div
                style="width: 60px;height: 1px;line-height: 1px;text-align: center;white-space: pre;text-overflow: ellipsis;position: relative;flex-shrink: 0;"
              />
            </div>
            <!-- 视图图表 end -->
            <!-- 过滤组件 start -->

            <div
              class="button-div-class"
              style="  width: 24px;height: 24px;text-align: center;line-height: 1;position: relative;margin: 16px auto 0px; "
            >
              <el-button
                circle
                :class="show&&showIndex===1? 'button-show':'button-closed'"
                class="el-icon-s-tools"
                size="mini"
                @click="showPanel(1)"
              />
            </div>
            <div class="button-text" style=" position: relative; margin: 18px auto 16px;">
              <div
                style=" max-width: 100%;text-align: center;white-space: nowrap;text-overflow: ellipsis;position: relative;flex-shrink: 0;"
              >
                {{ $t('panel.module') }}
              </div>
            </div>

            <div style="height: 1px; position: relative; margin: 0px auto;background-color:#E6E6E6;">
              <div
                style="width: 60px;height: 1px;line-height: 1px;text-align: center;white-space: pre;text-overflow: ellipsis;position: relative;flex-shrink: 0;"
              />
            </div>
            <!-- 过滤组件 end -->
            <!-- 其他组件 start -->

            <div
              class="button-div-class"
              style="  width: 24px;height: 24px;text-align: center;line-height: 1;position: relative;margin: 16px auto 0px; "
            >
              <el-button
                circle
                :class="show&&showIndex===3? 'button-show':'button-closed'"
                class="el-icon-brush"
                size="mini"
                @click="showPanel(3)"
              />
            </div>
            <div class="button-text" style=" position: relative; margin: 18px auto 16px;">
              <div
                style=" max-width: 100%;text-align: center;white-space: nowrap;text-overflow: ellipsis;position: relative;flex-shrink: 0;"
              >
                {{ $t('panel.other_module') }}
              </div>
            </div>

            <div style="height: 1px; position: relative; margin: 0px auto;background-color:#E6E6E6;">
              <div
                style="width: 60px;height: 1px;line-height: 1px;text-align: center;white-space: pre;text-overflow: ellipsis;position: relative;flex-shrink: 0;"
              />
            </div>
            <!-- 其他组件 end -->

            <!-- 视图复用 start -->
            <div
              class="button-div-class"
              style=" width: 24px;height: 24px;text-align: center;line-height: 1;position: relative;margin: 16px auto 0px;"
            >
              <el-button
                circle
                class="el-icon-copy-document button-closed"
                size="mini"
                @click="showMultiplexing(true)"
              />
            </div>
            <div class="button-text" style="position: relative; margin: 18px auto 16px;">
              <div
                style="max-width: 100%;text-align: center;white-space: nowrap;text-overflow: ellipsis;position: relative;flex-shrink: 0;"
              >
                <!--                {{ $t('panel.view') }}-->
                {{ $t('panel.multiplexing') }}
              </div>
            </div>
            <div style="height: 1px; position: relative; margin: 0px auto;background-color:#E6E6E6;">
              <div
                style="width: 60px;height: 1px;line-height: 1px;text-align: center;white-space: pre;text-overflow: ellipsis;position: relative;flex-shrink: 0;"
              />
            </div>
            <!-- 视图复用 end -->
          </div>
        </div>
      </de-aside-container>
      <!--画布区域-->
      <de-main-container id="canvasInfo-main">
        <!--左侧抽屉-->
        <el-drawer
          :visible.sync="show"
          :with-header="false"
          style="position: absolute;"
          direction="ltr"
          :modal="false"
          :size="drawerSize"
          :wrapper-closable="false"
          :close-on-press-escape="false"
          :modal-append-to-body="true"
        >
          <div style="width: 295px">
            <filter-group v-show=" show &&showIndex===1" />
            <subject-setting v-show=" show &&showIndex===2" />
            <assist-component v-show=" show &&showIndex===3" />
          </div>
        </el-drawer>
        <!--PC端画布区域-->
        <div
          v-if="!previewVisible&&!mobileLayoutStatus"
          id="canvasInfo"
          class="this_canvas"
          :style="customCanvasStyle"
          @drop="handleDrop"
          @dragover="handleDragOver"
          @mousedown="handleMouseDown"
          @mouseup="deselectCurComponent"
          @scroll="canvasScroll"
        >
          <!-- 仪表板联动清除按钮-->
          <canvas-opt-bar />
          <Editor ref="canvasEditor" :matrix-count="pcMatrixCountBase" :out-style="outStyle" :scroll-top="scrollTop" />
        </div>
        <!--移动端画布区域 保持宽高比2.5-->
        <el-row v-if="mobileLayoutStatus" class="mobile_canvas_main">
          <el-col :span="8" class="this_mobile_canvas_cell">
            <div
              v-proportion="2.1"
              :style="customCanvasMobileStyle"
              class="this_mobile_canvas"
              @drop="handleDrop"
              @dragover="handleDragOver"
              @mousedown="handleMouseDown"
              @mouseup="deselectCurComponent"
              @scroll="canvasScroll"
            >
              <el-row class="this_mobile_canvas_top" />
              <el-row class="this_mobile_canvas_inner_top">
                {{ panelInfo.name }}
              </el-row>
              <el-row
                id="canvasInfoMobile"
                class="this_mobile_canvas_main"
                :style="mobileCanvasStyle"
              >
                <Editor
                  v-if="mobileEditorShow"
                  id="editorMobile"
                  ref="editorMobile"
                  :matrix-count="mobileMatrixCount"
                  :out-style="outStyle"
                  :scroll-top="scrollTop"
                  @canvasDragging="canvasDragging"
                />
              </el-row>
              <el-row class="this_mobile_canvas_inner_bottom">
                <el-col :span="12">
                  <i v-if="!hasStar" class="el-icon-star-off" size="mini" @click="star" />
                  <i
                    v-if="hasStar"
                    class="el-icon-star-on"
                    style="color: #0a7be0;font-size: 18px"
                    size="mini"
                    @click="unstar"
                  />
                </el-col>
                <el-col :span="12" style="float: right">
                  <i class="el-icon-refresh-right" size="mini" @click="mobileRefresh" />
                </el-col>
              </el-row>
              <el-row class="this_mobile_canvas_bottom" />
            </div>
          </el-col>
          <el-col :span="16" class="this_mobile_canvas_cell this_mobile_canvas_wait_cell" :style="mobileCanvasStyle">
            <component-wait />
          </el-col>
        </el-row>
      </de-main-container>

      <div v-show="!mobileLayoutStatus&&rightDrawOpen" class="tools-window-main">
        <div v-if="showViewToolsAside">
          <chart-edit ref="chartEditRef" :edit-statue="showViewToolsAside&&!mobileLayoutStatus&&rightDrawOpen" :edit-from="'panel'" :param="chartEditParam" />
        </div>
        <div v-if="showBatchViewToolsAside">
          <chart-style-batch-set />
        </div>
        <div v-if="!showViewToolsAside&&!showBatchViewToolsAside">
          <el-row style="height: 40px">
            <el-tooltip :content="$t('chart.draw_back')">
              <el-button class="el-icon-d-arrow-right" style="position:absolute;left: 4px;top: 5px;" size="mini" circle @click="changeRightDrawOpen(false)" />
            </el-tooltip>
          </el-row>
          <el-row>
            <div class="view-selected-message-class">
              <span style="font-size: 14px;margin-left: 10px;font-weight: bold;line-height: 20px">{{ $t('panel.select_view') }}</span>
            </div>
          </el-row>
        </div>
      </div>
    </de-container>

    <el-dialog
      v-if="buttonVisible && panelInfo.id"
      :title="(currentWidget && currentWidget.getLeftPanel && currentWidget.getLeftPanel().label ? $t(currentWidget.getLeftPanel().label) : '') + $t('panel.module')"
      :visible.sync="buttonVisible"
      custom-class="de-button-dialog"
      @close="cancelButton"
    >
      <button-dialog
        v-if="buttonVisible && currentWidget && currentWidget.name === 'buttonSureWidget'"
        :ref="'filter-setting-' + currentFilterCom.id"
        :widget-info="currentWidget"
        :element="currentFilterCom"
        @sure-handler="sureHandler"
        @cancel-handler="cancelHandler"
      />

      <button-reset-dialog
        v-if="buttonVisible && currentWidget && currentWidget.name === 'buttonResetWidget'"
        :ref="'filter-setting-' + currentFilterCom.id"
        :widget-info="currentWidget"
        :element="currentFilterCom"
        @reset-button-handler="sureHandler"
        @cancel-button-handler="cancelHandler"
      />

    </el-dialog>

    <el-dialog
      v-if="filterVisible && panelInfo.id"
      :title="(currentWidget && currentWidget.getLeftPanel && currentWidget.getLeftPanel().label ? $t(currentWidget.getLeftPanel().label) : '') + $t('panel.module')"
      :visible.sync="filterVisible"
      custom-class="de-filter-dialog"
      @close="cancelFilter"
    >
      <filter-dialog
        v-if="filterVisible && currentWidget"
        :ref="'filter-setting-' + currentFilterCom.id"
        :widget-info="currentWidget"
        :element="currentFilterCom"
        @sure-button-status="sureStatusChange"
        @re-fresh-component="reFreshComponent"
      />
      <div style="text-align: end !important;margin: 0 15px 10px !important;">
        <span slot="footer">
          <el-button size="mini" @click="cancelFilter">{{ $t('commons.cancel') }}</el-button>
          <el-button :disabled="!enableSureButton" type="primary" size="mini" @click="sureFilter">{{ $t('commons.confirm') }}</el-button>
        </span>
      </div>
    </el-dialog>

    <fullscreen style="height: 100%;background: #f7f8fa;overflow-y: auto" :fullscreen.sync="previewVisible">
      <Preview
        v-if="previewVisible"
        :in-screen="!previewVisible"
        :panel-info="panelInfo"
        :show-type="canvasStyleData.selfAdaption?'full':'width'"
        :canvas-style-data="canvasStyleData"
        :component-data="componentData"
        show-position="edit"
      />
    </fullscreen>
    <input
      id="input"
      ref="files"
      type="file"
      accept="image/*"
      hidden
      @click="e => {e.target.value = '';}"
      @change="handleFileChange"
    >

    <!--矩形样式组件-->
    <TextAttr v-if="showAttr" :scroll-left="scrollLeft" :scroll-top="scrollTop" />
    <!--复用ChartGroup组件 不做显示-->
    <ChartGroup
      ref="chartGroup"
      :opt-from="'panel'"
      :advice-group-id="adviceGroupId"
      style="height: 0px;width:0px;padding:0px;overflow: hidden"
      :mounted-init="false"
      @newViewInfo="newViewInfo"
    />

    <!--仪表板外部参数设置组件-->
    <el-dialog
      :visible.sync="outerParamsSetVisible"
      width="900px"
      class="dialog-css"
      :show-close="true"
      :destroy-on-close="true"
      :append-to-body="true"
    >
      <OuterParamsSet v-if="outerParamsSetVisible" @outerParamsSetVisibleChange="outerParamsSetVisibleChange" />
    </el-dialog>

    <!--复用视图全屏显示框-->
    <el-dialog
      :visible="multiplexingShow"
      :show-close="false"
      class="dialog-css"
      :fullscreen="true"
    >
      <multiplexing v-if="multiplexingShow" :view-data="viewData" />
      <div slot="title" class="dialog-footer title-text">
        <span style="font-size: 14px;">
          {{ $t('panel.multiplexing') }}
        </span>
        <span style="float: right;">
          <el-button type="primary" size="mini" @click="saveMultiplexing()">{{ $t('commons.confirm') }}</el-button>
          <el-button size="mini" @click="showMultiplexing(false)">{{ $t('commons.cancel') }}</el-button>
        </span>
      </div>
    </el-dialog>

    <!--关闭弹框-->
    <el-dialog
      :visible.sync="panelCacheExist"
      :title="$t('panel.panel_no_save_tips')"
      :show-close="false"
      width="30%"
    >
      <el-row style="height: 20px">
        <el-col :span="3">
          <svg-icon icon-class="warn-tre" style="width: 20px;height: 20px;float: right" />
        </el-col>
        <el-col :span="21">
          <span style="font-size: 13px;margin-left: 10px;font-weight: bold;line-height: 20px">{{ $t('panel.panel_cache_use_tips') }}</span>
        </el-col>
      </el-row>
      <div slot="footer" class="dialog-footer">
        <el-button size="mini" @click="useCache(false)">{{ $t('panel.no') }}</el-button>
        <el-button type="primary" size="mini" @click="useCache(true)">{{ $t('panel.yes') }}</el-button>
      </div>
    </el-dialog>

  </el-row>
</template>

<script>
import DeMainContainer from '@/components/dataease/DeMainContainer'
import DeContainer from '@/components/dataease/DeContainer'
import DeAsideContainer from '@/components/dataease/DeAsideContainer'
import { addClass, removeClass } from '@/utils'
import FilterGroup from '../filter'
import SubjectSetting from '../SubjectSetting'
import bus from '@/utils/bus'
import Editor from '@/components/canvas/components/Editor/index'
import { deepCopy, imgUrlTrans, matrixBaseChange } from '@/components/canvas/utils/utils'
import componentList, {
  BASE_MOBILE_STYLE,
  COMMON_BACKGROUND,
  HYPERLINKS, PIC_STYLE
} from '@/components/canvas/custom-component/component-list' // 左侧列表数据
import { mapState } from 'vuex'
import { uuid } from 'vue-uuid'
import Toolbar from '@/components/canvas/components/Toolbar'
import {
  checkUserCache,
  findUserCache,
  initPanelData,
  initViewCache,
  queryPanelMultiplexingViewTree
} from '@/api/panel/panel'
import Preview from '@/components/canvas/components/Editor/Preview'
import elementResizeDetectorMaker from 'element-resize-detector'
import AssistComponent from '@/views/panel/AssistComponent'
import ChartGroup from '@/views/chart/group/Group'
import { chartCopy } from '@/api/chart/chart'
import CanvasOptBar from '@/components/canvas/components/Editor/CanvasOptBar'

// 引入样式
import '@/components/canvas/assets/iconfont/iconfont.css'
import '@/components/canvas/styles/animate.css'
import { ApplicationContext } from '@/utils/ApplicationContext'
import FilterDialog from '../filter/filterDialog'
import ButtonDialog from '../filter/ButtonDialog'
import ButtonResetDialog from '../filter/ButtonResetDialog'
import toast from '@/components/canvas/utils/toast'
import { commonAttr } from '@/components/canvas/custom-component/component-list'
import generateID from '@/components/canvas/utils/generateID'
import TextAttr from '@/components/canvas/components/TextAttr'
import ComponentWait from '@/views/panel/edit/ComponentWait'
import { deleteEnshrine, saveEnshrine, starStatus } from '@/api/panel/enshrine'
import ChartEdit from '@/views/chart/view/ChartEdit'
import OuterParamsSet from '@/views/panel/OuterParamsSet/index'
import ChartStyleBatchSet from '@/views/chart/view/ChartStyleBatchSet'
import Multiplexing from '@/views/panel/ViewSelect/multiplexing'
import { listenGlobalKeyDown } from '@/components/canvas/utils/shortcutKey'
import { adaptCurThemeCommonStyle } from '@/components/canvas/utils/style'
import eventBus from '@/components/canvas/utils/eventBus'
export default {
  name: 'PanelEdit',
  components: {
    Multiplexing,
    ChartStyleBatchSet,
    OuterParamsSet,
    ComponentWait,
    DeMainContainer,
    DeContainer,
    DeAsideContainer,
    FilterGroup,
    Editor,
    Toolbar,
    FilterDialog,
    ButtonDialog,
    ButtonResetDialog,
    SubjectSetting,
    Preview,
    AssistComponent,
    TextAttr,
    ChartGroup,
    ChartEdit,
    CanvasOptBar
  },
  data() {
    return {
      panelCacheExist: false,
      viewData: [],
      multiplexingShow: false,
      asideToolType: 'none',
      outerParamsSetVisible: false,
      autoMoveOffSet: 15,
      mobileEditorShow: true,
      hasStar: false,
      drawerSize: '300px',
      visible: false,
      show: false,
      editView: false,
      clickNotClose: false,
      showIndex: -1,
      activeName: 'attr',
      reSelectAnimateIndex: undefined,
      filterVisible: false,
      currentWidget: null,
      currentFilterCom: null,
      subjectVisible: false,
      previewVisible: false,
      componentStyleShow: true,
      aidedButtonActive: false,
      timer: null,
      needToChange: [
        'top',
        'left',
        'width',
        'height',
        'fontSize',
        'borderWidth'
      ],
      scale: '100',
      outStyle: {
        width: null,
        height: null
      },
      styleDialogVisible: false,
      currentDropElement: null,
      adviceGroupId: null,
      scrollLeft: 0,
      scrollTop: 0,
      timeMachine: null,
      dropComponentInfo: null,
      // 需要展示属性设置的组件类型
      showAttrComponent: [
        'custom',
        'v-text',
        'picture-add',
        'de-tabs',
        'rect-shape',
        'de-show-date',
        'de-video',
        'de-stream-media',
        'de-frame'
      ],
      enableSureButton: false,
      filterFromDrag: false,
      buttonFromDrag: false,
      activeToolsName: 'view',
      rightDrawOpen: false,
      editType: null,
      buttonVisible: false
    }
  },

  computed: {
    // 侧边显示控制
    chartEditParam() {
      if (this.curComponent) {
        if (this.curComponent.type === 'view') {
          return { 'id': this.curComponent.propValue.viewId, 'optType': 'edit' }
        } else if (this.curComponent.type === 'de-tabs' && this.$store.state.chart.viewId) {
          return { 'id': this.$store.state.chart.viewId, 'optType': 'edit' }
        } else {
          return {}
        }
      }
      return this.curComponent ? { 'id': this.curComponent.propValue.viewId, 'optType': 'edit' } : {}
    },
    // 侧边显示控制
    showAside() {
      return !this.linkageSettingStatus && !this.mobileLayoutStatus
    },
    // 显示视图工具栏
    showViewToolsAside() {
      return !this.batchOptStatus && this.curComponent && (this.curComponent.type === 'view' || this.curComponent.type === 'de-tabs')
    },
    showBatchViewToolsAside() {
      return this.batchOptStatus
    },
    showViewToolAsideType() {
      if (this.curComponent) {
        if (this.curComponent.type === 'view') {
          return 'view'
        } else {
          return 'publicSet'
        }
      } else {
        return 'none'
      }
    },
    showAttr() {
      if (this.mobileLayoutStatus) {
        return false
      } else if (this.curComponent && this.showAttrComponent.includes(this.curComponent.type)) {
        // 过滤组件有标题才显示
        if (this.curComponent.type === 'custom' && (!this.curComponent.options.attrs.showTitle || !this.curComponent.options.attrs.title)) {
          return false
        } else {
          return true
        }
      } else {
        return false
      }
    },
    customCanvasMobileStyle() {
      return {
        padding: this.componentGap + 'px'
      }
    },
    mobileCanvasStyle() {
      let style
      if (this.canvasStyleData.openCommonStyle) {
        if (this.canvasStyleData.panel.backgroundType === 'image' && typeof (this.canvasStyleData.panel.imageUrl) === 'string') {
          style = {
            background: `url(${imgUrlTrans(this.canvasStyleData.panel.imageUrl)}) no-repeat`
          }
        } else if (this.canvasStyleData.panel.backgroundType === 'color') {
          style = {
            background: this.canvasStyleData.panel.color
          }
        } else {
          style = {
            background: '#f7f8fa'
          }
        }
      }
      return style
    },
    customCanvasStyle() {
      let style = {
        padding: this.componentGap + 'px'
      }

      if (this.canvasStyleData.openCommonStyle) {
        if (this.canvasStyleData.panel.backgroundType === 'image' && typeof (this.canvasStyleData.panel.imageUrl) === 'string') {
          style = {
            background: `url(${imgUrlTrans(this.canvasStyleData.panel.imageUrl)}) no-repeat`,
            ...style
          }
        } else if (this.canvasStyleData.panel.backgroundType === 'color') {
          style = {
            background: this.canvasStyleData.panel.color,
            ...style
          }
        }
      }
      return style
    },
    panelInfo() {
      return this.$store.state.panel.panelInfo
    },
    pcMatrixCountBase() {
      if (this.canvasStyleData.aidedDesign) {
        return {
          x: this.pcMatrixCount.x * this.canvasStyleData.aidedDesign.matrixBase,
          y: this.pcMatrixCount.y * this.canvasStyleData.aidedDesign.matrixBase
        }
      } else {
        return this.pcMatrixCount
      }
    },
    multiplexingDisabled() {
      return Object.keys(this.curMultiplexingComponents) === 0
    },
    ...mapState([
      'curComponent',
      'curCanvasScale',
      'isClickComponent',
      'canvasStyleData',
      'curComponentIndex',
      'componentData',
      'linkageSettingStatus',
      'dragComponentInfo',
      'componentGap',
      'mobileLayoutStatus',
      'pcMatrixCount',
      'mobileMatrixCount',
      'mobileLayoutStyle',
      'scrollAutoMove',
      'batchOptStatus',
      'curMultiplexingComponents'
    ])
  },

  watch: {
    show(value) {
      if (value && !this.clickNotClose) {
        this.addEventClick()
      }
      if (value) {
        addClass(document.body, 'showRightPanel')
      } else {
        removeClass(document.body, 'showRightPanel')
      }
    },
    '$store.state.styleChangeTimes'() {
      if (this.$store.state.styleChangeTimes > 0) {
        this.destroyTimeMachine()
        this.recordStyleChange(this.$store.state.styleChangeTimes)
      }
    },
    mobileLayoutStatus() {
      this.restore()
    }
  },
  created() {
    // Global listening for key events
    listenGlobalKeyDown()
  },
  mounted() {
    this.initEvents()
    const _this = this
    const erd = elementResizeDetectorMaker()
    // 监听div变动事件
    erd.listenTo(document.getElementById('canvasInfo-main'), element => {
      _this.$nextTick(() => {
        _this.restore()
      })
    })
    this.loadMultiplexingViewTree()
    this.init(this.$store.state.panel.panelInfo.id)
  },
  beforeDestroy() {
    bus.$off('component-on-drag', this.componentOnDrag)
    bus.$off('component-dialog-edit', this.editDialog)
    bus.$off('button-dialog-edit', this.editButtonDialog)
    bus.$off('component-dialog-style', this.componentDialogStyle)
    bus.$off('previewFullScreenClose', this.previewFullScreenClose)
    bus.$off('change_panel_right_draw', this.changeRightDrawOpen)
    bus.$off('delete-condition', this.deleteCustomComponent)
    bus.$off('current-component-change', this.asideRefresh)
    const elx = this.$refs.rightPanel
    elx && elx.remove()
  },
  methods: {
    componentOnDrag() {
      this.show = false
    },
    componentDialogStyle() {
      this.styleDialogVisible = true
    },
    previewFullScreenClose() {
      this.previewVisible = false
    },
    initEvents() {
      bus.$on('component-on-drag', this.componentOnDrag)
      bus.$on('component-dialog-edit', this.editDialog)
      bus.$on('button-dialog-edit', this.editButtonDialog)
      bus.$on('component-dialog-style', this.componentDialogStyle)
      bus.$on('previewFullScreenClose', this.previewFullScreenClose)
      bus.$on('change_panel_right_draw', this.changeRightDrawOpen)
      bus.$on('delete-condition', this.deleteCustomComponent)
      bus.$on('current-component-change', this.asideRefresh)
    },
    asideRefresh() {
      if (this.$refs['chartEditRef']) {
        this.$refs['chartEditRef'].resetChartData()
      }
    },
    deleteCustomComponent(param) {
      param && param.componentId && this.componentData.forEach(com => {
        if (com.type === 'custom-button' && com.options.attrs.filterIds) {
          const filterIds = com.options.attrs.filterIds
          let len = filterIds.length
          while (len--) {
            if (param.componentId === filterIds[len]) { filterIds.splice(len, 1) }
          }
          com.options.attrs.filterIds = filterIds
        }
      })
    },
    loadMultiplexingViewTree() {
      queryPanelMultiplexingViewTree().then(res => {
        this.viewData = res.data
      })
    },
    closeOuterParamsSetDialog() {
      this.outerParamsSetVisible = false
    },
    changeRightDrawOpen(param) {
      if (!param) {
        this.$store.dispatch('chart/setViewId', null)
      }
      this.rightDrawOpen = param
      if (this.rightDrawOpen) {
        setTimeout(() => {
          this.outStyle.width = this.outStyle.width + 0.000001
        }, 0)
      }
    },
    init(panelId) {
      const _this = this
      _this.initHasStar()
      this.$store.commit('initCanvas')
      if (panelId) {
        checkUserCache(panelId, function(rsp) {
          // the panel have cache
          if (rsp.data) {
            _this.panelCacheExist = true
          } else {
            _this.editPanelDataInit(panelId, false)
          }
        })
      }
    },
    useCache(useCache) {
      this.editPanelDataInit(this.$store.state.panel.panelInfo.id, useCache)
      this.panelCacheExist = false
    },
    editPanelDataInit(panelId, useCache) {
      const _this = this
      initPanelData(panelId, useCache, function() {
        // 清空当前缓存,快照
        _this.$store.commit('refreshSnapshot')
        // 初始化视图缓存
        initViewCache(panelId)
        // 初始化记录的视图信息
        _this.$store.commit('setComponentViewsData')
        // if panel data load from cache the save button should be active
        // 初始化保存状态
        setTimeout(() => {
          if (useCache) {
            _this.$store.commit('recordSnapshot', 'cache')
            _this.$store.commit('recordChangeTimes')
          } else {
            _this.$store.commit('refreshSaveStatus')
          }
          eventBus.$emit('editPanelInitReady')
        }, 500)
      })
    },
    star() {
      this.panelInfo && saveEnshrine(this.panelInfo.id, false).then(res => {
        this.hasStar = true
      })
    },
    unstar() {
      this.panelInfo && deleteEnshrine(this.panelInfo.id, false).then(res => {
        this.hasStar = false
      })
    },
    initHasStar() {
      this.panelInfo && this.panelInfo.id && starStatus(this.panelInfo.id, false).then(res => {
        this.hasStar = res.data
      })
    },
    mobileRefresh() {
      this.mobileEditorShow = false
      this.$nextTick(() => {
        this.mobileEditorShow = true
      })
    },
    save() {

    },
    toDir() {
      this.$router.replace('/panel/index')
    },
    showPanel(type) {
      if (this.showIndex === -1 || this.showIndex === type) {
        this.$nextTick(() => {
          if (this.show) {
            this.showIndex === -1
          }
          this.show = !this.show
        }
        )
      }
      this.showIndex = type
    },
    addEventClick() {
      window.addEventListener('click', this.closeSidebar)
    },
    closeSidebar(evt) {
      const parent = evt.target.closest('.button-div-class')
      const self = evt.target.closest('.el-drawer__wrapper')
      // 点击样式按钮 排除
      const stick = evt.target.closest('.icon-icon_effects_outlined')
      const xuanfuanniu = evt.target.closest('.icon-xuanfuanniu')
      const shujujuzhen = evt.target.closest('.icon-shujujuzhen')
      const suffix = evt.target.closest('.el-input__suffix')
      const elButton = evt.target.closest('.el-button')
      if (!parent && !self && !stick && !xuanfuanniu && !shujujuzhen && !suffix && !elButton) {
        this.show = false
        window.removeEventListener('click', this.closeSidebar)
        this.showIndex = -1
      }
    },
    resetID(data) {
      if (data) {
        data.forEach(item => {
          item.type !== 'custom' && (item.id = uuid.v1())
        })
      }
      return data
    },
    handleDrop(e) {
      this.dragComponentInfo.moveStatus = 'drop'
      // 记录拖拽信息
      this.dropComponentInfo = deepCopy(this.dragComponentInfo)
      this.currentDropElement = e
      e.preventDefault()
      e.stopPropagation()
      let component
      const newComponentId = uuid.v1()
      const componentInfo = JSON.parse(e.dataTransfer.getData('componentInfo'))
      if (componentInfo.type === 'assist') {
        // 辅助设计组件
        componentList.forEach(componentTemp => {
          if (componentInfo.id === componentTemp.id) {
            component = deepCopy(componentTemp)
          }
        })
        if (component.type === 'picture-add') {
          this.goFile()
          this.clearCurrentInfo()
          return
        }
      } else if (componentInfo.type === 'view') {
        // 用户视图设置 复制一个模板
        componentList.forEach(componentTemp => {
          if (componentTemp.type === 'view') {
            component = deepCopy(componentTemp)
            const propValue = {
              id: newComponentId,
              viewId: componentInfo.id
            }
            component.propValue = propValue
            component.filters = []
            component.linkageFilters = []
          }
        })
      } else {
        this.currentWidget = ApplicationContext.getService(componentInfo.id)
        this.currentFilterCom = this.currentWidget.getDrawPanel()
        if (this.canvasStyleData.auxiliaryMatrix) {
          this.currentFilterCom.x = this.dropComponentInfo.x
          this.currentFilterCom.y = this.dropComponentInfo.y
          this.currentFilterCom.sizex = this.dropComponentInfo.sizex
          this.currentFilterCom.sizey = this.dropComponentInfo.sizey
          this.currentFilterCom.style.left = (this.dragComponentInfo.x - 1) * this.curCanvasScale.matrixStyleOriginWidth
          this.currentFilterCom.style.top = (this.dragComponentInfo.y - 1) * this.curCanvasScale.matrixStyleOriginHeight
          this.currentFilterCom.style.width = this.dragComponentInfo.sizex * this.curCanvasScale.matrixStyleOriginWidth
          this.currentFilterCom.style.height = this.dragComponentInfo.sizey * this.curCanvasScale.matrixStyleOriginHeight
        } else {
          this.currentFilterCom.style.left = this.dragComponentInfo.shadowStyle.x
          this.currentFilterCom.style.top = this.dragComponentInfo.shadowStyle.y
          this.currentFilterCom.style.width = this.dragComponentInfo.style.width
          this.currentFilterCom.style.height = this.dragComponentInfo.style.height
        }
        this.currentFilterCom.id = newComponentId
        this.currentFilterCom.auxiliaryMatrix = this.canvasStyleData.auxiliaryMatrix
        this.currentFilterCom.mobileStyle = deepCopy(BASE_MOBILE_STYLE)
        this.currentFilterCom['hyperlinks'] = deepCopy(HYPERLINKS)
        this.currentFilterCom.commonBackground = this.currentFilterCom.commonBackground || deepCopy(COMMON_BACKGROUND)

        if (this.currentWidget.filterDialog) {
          this.show = false
          this.openFilterDialog(true)
          return
        }
        if (this.currentWidget.buttonDialog) {
          this.show = false
          this.openButtonDialog(true)
          return
        }
        component = deepCopy(this.currentFilterCom)
      }
      if (this.canvasStyleData.auxiliaryMatrix) {
        component.x = this.dropComponentInfo.x
        component.y = this.dropComponentInfo.y
        component.sizex = this.dropComponentInfo.sizex
        component.sizey = this.dropComponentInfo.sizey

        component.style.left = (this.dragComponentInfo.x - 1) * this.curCanvasScale.matrixStyleOriginWidth
        component.style.top = (this.dragComponentInfo.y - 1) * this.curCanvasScale.matrixStyleOriginHeight
        component.style.width = this.dragComponentInfo.sizex * this.curCanvasScale.matrixStyleOriginWidth
        component.style.height = this.dragComponentInfo.sizey * this.curCanvasScale.matrixStyleOriginHeight
      } else {
        component.style.top = this.dropComponentInfo.shadowStyle.y
        component.style.left = this.dropComponentInfo.shadowStyle.x
        component.style.width = this.dropComponentInfo.shadowStyle.width
        component.style.height = this.dropComponentInfo.shadowStyle.height
      }

      component.id = newComponentId
      // 新拖入的组件矩阵状态 和仪表板当前的矩阵状态 保持一致
      component.auxiliaryMatrix = this.canvasStyleData.auxiliaryMatrix
      // 统一设置背景信息
      component.commonBackground = component.commonBackground || deepCopy(COMMON_BACKGROUND)

      // 视图统一调整为复制
      if (componentInfo.type === 'view') {
        chartCopy(component.propValue.viewId, this.panelInfo.id).then(res => {
          component.propValue.viewId = res.data
          this.$store.commit('addComponent', { component })
          this.$store.commit('recordSnapshot', 'handleDrop')
        })
      } else {
        this.$store.commit('addComponent', { component })
        this.$store.commit('recordSnapshot', 'handleDrop')
      }
      adaptCurThemeCommonStyle(component)
      this.clearCurrentInfo()
    },
    clearCurrentInfo() {
      this.currentWidget = null
      this.currentFilterCom = null
    },

    handleMouseDown() {
      this.$store.commit('setClickComponentStatus', false)
    },

    deselectCurComponent(e) {
      if (!this.isClickComponent) {
        this.$store.commit('setCurComponent', { component: null, index: null })
      }

      // 0 左击 1 滚轮 2 右击
      if (e.button !== 2) {
        this.$store.commit('hideContextMenu')
      }
    },
    openButtonDialog(fromDrag = false) {
      this.buttonFromDrag = fromDrag
      this.buttonVisible = true
    },
    closeButton() {
      this.buttonVisible = false
      this.currentWidget = null
      this.clearCurrentInfo()
    },
    cancelButton() {
      this.closeButton()
      if (this.buttonFromDrag) {
        bus.$emit('onRemoveLastItem')
      }
    },
    sureButton() {

    },
    openFilterDialog(fromDrag = false) {
      this.filterFromDrag = fromDrag
      this.filterVisible = true
    },
    closeFilter() {
      this.filterVisible = false
      this.currentWidget = null
      this.clearCurrentInfo()
    },
    cancelFilter() {
      this.closeFilter()
      if (this.filterFromDrag) {
        bus.$emit('onRemoveLastItem')
      }
    },
    sureFilter() {
      this.currentFilterCom = this.$refs['filter-setting-' + this.currentFilterCom.id].getElementInfo()
      if (this.editType !== 'update') {
        adaptCurThemeCommonStyle(this.currentFilterCom)
      }
      this.$store.commit('setComponentWithId', this.currentFilterCom)
      this.$store.commit('recordSnapshot', 'sureFilter')
      this.$store.commit('setCurComponent', { component: this.currentFilterCom, index: this.curComponentIndex })
      bus.$emit('reset-default-value', this.currentFilterCom.id)
      this.closeFilter()
    },
    reFreshComponent(component) {
      this.currentFilterCom = component
      this.$forceUpdate()
    },
    editDialog(editType) {
      this.editType = editType
      if (this.curComponent && this.curComponent.serviceName) {
        const serviceName = this.curComponent.serviceName
        this.currentWidget = ApplicationContext.getService(serviceName)
        this.currentFilterCom = this.curComponent
        this.openFilterDialog()
      }
    },
    editButtonDialog(editType) {
      this.editType = editType
      if (this.curComponent && this.curComponent.serviceName) {
        const serviceName = this.curComponent.serviceName
        this.currentWidget = ApplicationContext.getService(serviceName)
        this.currentFilterCom = this.curComponent
        this.openButtonDialog()
      }
    },
    closeLeftPanel() {
      this.show = false
    },
    previewFullScreen() {
      this.previewVisible = true
    },
    changeAidedDesign() {
      this.aidedButtonActive = !this.aidedButtonActive
    },
    outerParamsSetVisibleChange(param) {
      this.outerParamsSetVisible = param
    },
    getOriginStyle(value) {
      const scale = this.canvasStyleData.scale
      const result = value / (scale / 100)
      return result
    },
    restore() {
      this.$nextTick(() => {
        const domInfo = this.mobileLayoutStatus ? document.getElementById('canvasInfoMobile') : document.getElementById('canvasInfo')
        if (domInfo) {
          this.outStyle.height = domInfo.offsetHeight - this.getGap()
          // 临时处理 确保每次restore 有会更新
          this.outStyle.width = domInfo.offsetWidth - this.getGap() + (Math.random() * 0.000001) + 2
        }
      })
    },
    getGap() {
      return this.mobileLayoutStatus ? 0 : this.componentGap * 2
    },
    closeStyleDialog() {
      this.styleDialogVisible = false
    },
    goFile() {
      this.$refs.files.click()
    },
    handleFileChange(e) {
      const _this = this
      const file = e.target.files[0]
      if (!file.type.includes('image')) {
        toast('只能插入图片')
        return
      }
      const reader = new FileReader()
      reader.onload = (res) => {
        const fileResult = res.target.result
        const img = new Image()
        img.onload = () => {
          const component = {
            ...commonAttr,
            id: generateID(),
            component: 'Picture',
            type: 'picture-add',
            label: '图片',
            icon: '',
            hyperlinks: HYPERLINKS,
            mobileStyle: BASE_MOBILE_STYLE,
            propValue: fileResult,
            commonBackground: deepCopy(COMMON_BACKGROUND),
            style: {
              ...PIC_STYLE
            }
          }
          component.auxiliaryMatrix = false
          component.style.top = _this.dropComponentInfo.shadowStyle.y
          component.style.left = _this.dropComponentInfo.shadowStyle.x
          component.style.width = _this.dropComponentInfo.shadowStyle.width
          component.style.height = _this.dropComponentInfo.shadowStyle.height
          this.$store.commit('addComponent', {
            component: component
          })
          this.$store.commit('recordSnapshot', 'handleFileChange')
        }

        img.src = fileResult
      }

      reader.readAsDataURL(file)
    },
    getPositionX(x) {
      if (this.canvasStyleData.selfAdaption) {
        return x * 100 / this.curCanvasScale.scaleWidth
      } else {
        return x
      }
    },
    getPositionY(y) {
      if (this.canvasStyleData.selfAdaption) {
        return y * 100 / this.curCanvasScale.scaleHeight
      } else {
        return y
      }
    },
    newChart() {
      this.adviceGroupId = this.panelInfo.id
      this.show = false
      this.$refs['chartGroup'].selectTable()
    },
    newViewInfo(newViewInfo) {
      let component
      const newComponentId = uuid.v1()
      // 用户视图设置 复制一个模板
      componentList.forEach(componentTemp => {
        if (componentTemp.type === 'view') {
          component = matrixBaseChange(deepCopy(componentTemp))
          const propValue = {
            id: newComponentId,
            viewId: newViewInfo.id,
            textValue: '双击输入文本内容'
          }
          component.propValue = propValue
          component.filters = []
          component.linkageFilters = []
        }
      })

      component.auxiliaryMatrix = this.canvasStyleData.auxiliaryMatrix
      // position = absolution 或导致有偏移 这里中和一下偏移量
      if (this.canvasStyleData.auxiliaryMatrix) {
        component.style.left = (component.x - 1) * this.curCanvasScale.matrixStyleOriginWidth
        component.style.top = (component.y - 1) * this.curCanvasScale.matrixStyleOriginHeight
        component.style.width = component.sizex * this.curCanvasScale.matrixStyleOriginWidth
        component.style.height = component.sizey * this.curCanvasScale.matrixStyleOriginHeight
      } else {
        component.style.left = 0
        component.style.top = 0
        component.x = 1
        component.y = 1
      }
      component.id = newComponentId
      // 统一设置背景信息
      component.commonBackground = deepCopy(COMMON_BACKGROUND)
      // 适配当前主题
      adaptCurThemeCommonStyle(component)
      this.$store.commit('addComponent', { component })
      this.$store.commit('recordSnapshot', 'newViewInfo')
      this.clearCurrentInfo()
      this.$store.commit('setCurComponent', { component: component, index: this.componentData.length - 1 })

      // 打开属性栏
      bus.$emit('change_panel_right_draw', true)
    },
    canvasScroll(event) {
      this.scrollLeft = event.target.scrollLeft
      this.scrollTop = event.target.scrollTop
      bus.$emit('onScroll')
    },
    destroyTimeMachine() {
      this.timeMachine && clearTimeout(this.timeMachine)
      this.timeMachine = null
    },

    // 如果内部样式有变化 1秒钟后保存一个镜像
    recordStyleChange(index) {
      if (index === this.$store.state.styleChangeTimes) {
        this.timeMachine = setTimeout(() => {
          this.$store.commit('recordSnapshot')
          this.$store.state.styleChangeTimes = 0
          this.destroyTimeMachine()
        }, 1000)
      }
    },
    handleDragOver(e) {
      e.preventDefault()
      e.dataTransfer.dropEffect = 'copy'
      this.$refs.canvasEditor.handleDragOver(e)
    },
    sureHandler() {
      this.currentFilterCom = this.$refs['filter-setting-' + this.currentFilterCom.id].getElementInfo()
      if (this.editType !== 'update') {
        adaptCurThemeCommonStyle(this.currentFilterCom)
      }
      this.$store.commit('setComponentWithId', this.currentFilterCom)
      this.$store.commit('recordSnapshot', 'sureFilter')
      this.$store.commit('setCurComponent', { component: this.currentFilterCom, index: this.curComponentIndex })
      bus.$emit('refresh-button-info')
      this.closeButton()
    },
    cancelHandler() {
      this.cancelButton()
    },
    sureStatusChange(status) {
      this.enableSureButton = status
    },
    canvasDragging(mY, offsetY) {
      if (this.curComponent && this.curComponent.optStatus.dragging) {
        // 触发滚动的区域偏移量
        const touchOffset = 100
        const canvasInfoMobile = document.getElementById('canvasInfoMobile')
        // 获取子盒子（高度肯定比父盒子大）
        // const editorMobile = document.getElementById('editorMobile')
        // 画布区顶部到浏览器顶部距离
        const canvasTop = canvasInfoMobile.offsetTop + 75
        // 画布区有高度
        const canvasHeight = canvasInfoMobile.offsetHeight
        // 画布区域底部距离浏览器顶部距离
        const canvasBottom = canvasTop + canvasHeight
        if (mY > (canvasBottom - touchOffset) && offsetY > 0) {
          // 触发底部滚动
          this.scrollMove(this.autoMoveOffSet)
        } else if (mY < (canvasTop + touchOffset) && offsetY < 0) {
          // 触发顶部滚动
          this.scrollMove(-this.autoMoveOffSet)
        }
      }
    },
    scrollMove(offset) {
      const canvasInfoMobile = document.getElementById('canvasInfoMobile')
      canvasInfoMobile.scrollTop = canvasInfoMobile.scrollTop + offset
      this.$store.commit('setScrollAutoMove', this.scrollAutoMove + offset)
    },
    showMultiplexing(type) {
      this.multiplexingShow = type
    },
    saveMultiplexing() {
      this.showMultiplexing(false)
      this.$store.commit('copyMultiplexingComponents')
      this.$store.commit('recordSnapshot')
      this.$store.commit('canvasChange')
    }
  }
}
</script>

<style scoped>
  .ms-aside-container {
    height: calc(100vh - 56px);
    max-width: 60px;
    border: none;
    width: 60px;
  }

  .ms-main-container {
    height: calc(100vh - 56px);
  }

  .de-header {
    height: 56px !important;
    padding: 0px!important;
    border-bottom: 1px solid #E6E6E6;
    background-color: var(--SiderBG, white);
  }

  .blackTheme .de-header {
    background-color: var(--SiderBG, white) !important;
    color: var(--TextActive);
  }

  .showLeftPanel {
    overflow: hidden;
    position: relative;
    width: 100%;
  }
</style>

<style lang="scss" scoped>
  .leftPanel-background {
    position: fixed;
    top: 0;
    left: 0;
    opacity: 0;
    transition: opacity .3s cubic-bezier(.7, .3, .1, 1);
    background: rgba(0, 0, 0, .2);
    z-index: -1;
  }

  .leftPanel {
    width: 100%;
    max-width: 300px;
    height: calc(100vh - 56px);
    position: fixed;
    top: 91px;
    left: 60px;
    box-shadow: 0px 0px 15px 0px rgba(0, 0, 0, .05);
    transition: all .25s cubic-bezier(.7, .3, .1, 1);
    transform: translate(100%);
    background: var(--SiderBG, #fff);
    z-index: 1003;
  }

  .show {
    transition: all .3s cubic-bezier(.7, .3, .1, 1);

    .leftPanel-background {
      z-index: 1002;
      opacity: 1;
      width: 100%;
      height: 100%;
    }

    .leftPanel {
      transform: translate(0);
    }
  }

  .mobile_canvas_main {
    width: 80%;
    height: 90%;
    margin-left: 10%;
    margin-top: 3%;
  }

  .this_mobile_canvas {
    border-radius: 30px;
    min-width: 300px;
    max-width: 350px;
    min-height: 600px;
    max-height: 700px;
    overflow: hidden;
    background-color: #000000;
    background-size: 100% 100% !important;
  }

  .this_mobile_canvas_inner_top {
    vertical-align: middle;
    text-align: center;
    background-color: #f7f8fa;
    height: 30px;
    line-height: 30px;
    font-size: 14px;
    width: 100%;
  }

  .this_mobile_canvas_top {
    height: 30px;
    width: 100%;
  }

  .this_mobile_canvas_inner_bottom {
    background-color: #f7f8fa;
    line-height: 30px;
    vertical-align: middle;
    color: gray;
    height: 30px;
    width: 100%;
  }

  .this_mobile_canvas_bottom {
    height: 30px;
    width: 100%;
  }

  .this_mobile_canvas_main {
    overflow-x: hidden;
    overflow-y: auto;
    height: calc(100% - 120px);;
    background-color: #d7d9e3;
    background-size: 100% 100% !important;
  }

  .this_mobile_canvas_cell {
    height: 100%;
    display: flex;
    align-items: center;
    justify-content: center;
  }

  .this_mobile_canvas_wait_cell {
    background-size: 100% 100% !important;
    border: 2px solid #9ea6b2
  }

  .this_canvas {
    width: 100%;
    height: calc(100vh - 56px);
    overflow-x: hidden;
    overflow-y: auto;
    background-size: 100% 100% !important;
  }

  .el-main {
    height: calc(100vh - 56px);
    padding: 0 !important;
    overflow: auto;
    position: relative;
  }

  .el-main ::v-deep .el-drawer__wrapper {
    width: 310px !important;
  }

  .el-main ::v-deep .el-drawer__body {
    overflow-y: auto;
  }

  .button-show {
    background-color: var(--ContentBG, #ebf2fe) !important;
  }

  .button-closed {
    background-color: var(--SiderBG, #ffffff) !important;
  }

  .style-aside {
    width: 250px;
    max-width: 250px !important;
    border: 1px solid var(--TableBorderColor, #E6E6E6);
    padding: 10px;
    transition: all 0.3s;

  }

  .placeholder {
    font-size: 14px;
    color: gray;
  }

  .show {
    transform: translateX(0);
  }

  .hidden {
    transform: translateX(100%);
  }

  .style-edit-dialog {
    width: 300px !important;
    height: 400px !important;

    .el-dialog__header {
      padding: 10px 20px !important;

      .el-dialog__headerbtn {
        top: 15px !important;
      }
    }

    .el-dialog__body {
      padding: 1px 15px !important;
    }
  }

  .style-hidden {
    overflow-x: hidden;
  }

  .button-text {
    color: var(--TextActive);
  }

  .mobile-canvas {
    width: 300px;
    height: 600px;
  }

  ::-webkit-scrollbar {
    width: 2px !important;
    height: 2px !important;
  }

  .tools-window-main {
    width: 350px;
    background-color: #FFFFFF;
    transition: 1s;
  }

  .tools-window-tabs {
    height: calc(100vh - 100px);
    overflow: hidden;
    overflow-y: auto;
    overflow-x: hidden;
  }

  ::v-deep .el-tabs__item {
    padding: 0 15px;
  }
  .view-selected-message-class {
    font-size: 12px;
    color: #9ea6b2;
    height: 100%;
    display: flex;
    align-items: center;
    justify-content: center;
    height: calc(100vh - 100px);
  }

  .dialog-css ::v-deep .el-dialog__title {
    font-size: 14px;
  }

  .dialog-css ::v-deep .el-dialog__header {
    padding: 20px 20px 0;
  }

  .dialog-css ::v-deep .el-dialog__body {
    padding: 10px 20px 20px;
  }
</style>
