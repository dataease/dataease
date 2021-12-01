<template>
  <el-row>
    <el-header class="de-header">
      <el-col :span="8" style="text-overflow:ellipsis;overflow: hidden;white-space: nowrap;color: #606266;font-size: 16px">
        <span style="line-height: 35px;">
          {{ $t('commons.name') }} ：{{ panelInfo.name || '测试仪表板' }}
        </span>
      </el-col>
      <!--横向工具栏-->
      <el-col :span="16">
        <Toolbar
          ref="toolbar"
          :style-button-active="show&&showIndex===2"
          :aided-button-active="aidedButtonActive"
          @showPanel="showPanel"
          @previewFullScreen="previewFullScreen"
          @changeAidedDesign="changeAidedDesign"
        />
      </el-col>
    </el-header>
    <de-container>
      <!--左侧导航栏-->
      <de-aside-container class="ms-aside-container">
        <div v-if="showAside" style="width: 60px; left: 0px; top: 0px; bottom: 0px;  position: absolute">
          <div style="width: 60px;height: 100%;overflow: hidden auto;position: relative;margin: 0px auto; font-size: 14px">
            <!-- 视图图表 start -->
            <div class="button-div-class" style=" width: 24px;height: 24px;text-align: center;line-height: 1;position: relative;margin: 16px auto 0px;">
              <el-button :class="show&&showIndex===0? 'button-show':'button-closed'" circle class="el-icon-circle-plus-outline" size="mini" @click="showPanel(0)" />
            </div>
            <div class="button-text" style="position: relative; margin: 18px auto 16px;">
              <div style="max-width: 100%;text-align: center;white-space: nowrap;text-overflow: ellipsis;position: relative;flex-shrink: 0;">
                {{ $t('panel.view') }}
              </div>
            </div>
            <div style="height: 1px; position: relative; margin: 0px auto;background-color:#E6E6E6;">
              <div style="width: 60px;height: 1px;line-height: 1px;text-align: center;white-space: pre;text-overflow: ellipsis;position: relative;flex-shrink: 0;" />
            </div>
            <!-- 视图图表 end -->
            <!-- 过滤组件 start -->
            <div tabindex="-1" style="position: relative; margin: 16px auto">
              <div style="height: 60px; position: relative">
                <div class="button-div-class" style=" text-align: center;line-height: 1;position: absolute;inset: 0px 0px 45px; ">
                  <el-button circle :class="show&&showIndex===1? 'button-show':'button-closed'" class="el-icon-s-tools" size="mini" @click="showPanel(1)" />
                </div>
                <div class="button-text" style=" position: absolute;left: 0px;right: 0px;bottom: 10px; height: 16px;">
                  <div style=" max-width: 100%;text-align: center;white-space: nowrap;text-overflow: ellipsis;position: relative;flex-shrink: 0;">
                    {{ $t('panel.module') }}
                  </div>
                </div>
              </div>
            </div>
            <div style="height: 1px; position: relative; margin: 0px auto;background-color:#E6E6E6;">
              <div style="width: 60px;height: 1px;line-height: 1px;text-align: center;white-space: pre;text-overflow: ellipsis;position: relative;flex-shrink: 0;" />
            </div>
            <!-- 过滤组件 end -->
            <!-- 其他组件 start -->
            <div tabindex="-1" style="position: relative; margin: 16px auto">
              <div style="height: 60px; position: relative">
                <div class="button-div-class" style=" text-align: center;line-height: 1;position: absolute;inset: 0px 0px 45px; ">
                  <el-button circle :class="show&&showIndex===3? 'button-show':'button-closed'" class="el-icon-brush" size="mini" @click="showPanel(3)" />
                </div>
                <div class="button-text" style=" position: absolute;left: 0px;right: 0px;bottom: 10px; height: 16px;">
                  <div style=" max-width: 100%;text-align: center;white-space: nowrap;text-overflow: ellipsis;position: relative;flex-shrink: 0;">
                    {{ $t('panel.other_module') }}
                  </div>
                </div>
              </div>
            </div>
            <div style="height: 1px; position: relative; margin: 0px auto;background-color:#E6E6E6;">
              <div style="width: 60px;height: 1px;line-height: 1px;text-align: center;white-space: pre;text-overflow: ellipsis;position: relative;flex-shrink: 0;" />
            </div>
            <!-- 其他组件 end -->
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
          <view-select v-show=" show && showIndex===0" @newChart="newChart" />
          <filter-group v-show=" show &&showIndex===1" />
          <subject-setting v-show=" show &&showIndex===2" />
          <assist-component v-show=" show &&showIndex===3" />
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
          <Editor ref="canvasEditor" :matrix-count="pcMatrixCount" :out-style="outStyle" :scroll-top="scrollTop" />
        </div>
        <!--移动端画布区域 保持宽高比2.5-->
        <el-row v-if="mobileLayoutStatus" class="mobile_canvas_main">
          <el-col :span="8" class="this_mobile_canvas_cell">
            <div
              v-proportion="2.5"
              :style="customCanvasStyle"
              class="this_mobile_canvas"
              @drop="handleDrop"
              @dragover="handleDragOver"
              @mousedown="handleMouseDown"
              @mouseup="deselectCurComponent"
              @scroll="canvasScroll"
            >
              <el-row class="this_mobile_canvas_top" />
              <el-row
                id="canvasInfoMobile"
                class="this_mobile_canvas_main"
              >
                <Editor ref="editorMobile" :matrix-count="mobileMatrixCount" :out-style="outStyle" :scroll-top="scrollTop" />
              </el-row>
              <el-row class="this_mobile_canvas_bottom" />
            </div>
          </el-col>
          <el-col :span="16" class="this_mobile_canvas_cell">
            <component-wait />
          </el-col>
        </el-row>
      </de-main-container>
    </de-container>

    <el-dialog
      v-if="filterVisible && panelInfo.id"
      :title="(currentWidget && currentWidget.getLeftPanel && currentWidget.getLeftPanel().label ? $t(currentWidget.getLeftPanel().label) : '') + $t('panel.module')"
      :visible.sync="filterVisible"
      custom-class="de-filter-dialog"
    >
      <filter-dialog v-if="filterVisible && currentWidget" :widget-info="currentWidget" :component-info="currentFilterCom" @re-fresh-component="reFreshComponent">
        <component
          :is="currentFilterCom.component"
          :id="'component' + currentFilterCom.id"
          class="component"
          :style="currentFilterCom.style"
          :element="currentFilterCom"
          :in-draw="false"
        />
      </filter-dialog>
      <div style="text-align: end !important;margin: 0 15px 10px !important;">
        <span slot="footer">
          <el-button size="mini" @click="cancelFilter">{{ $t('commons.cancel') }}</el-button>
          <el-button :disabled="!currentFilterCom.options.attrs.fieldId" type="primary" size="mini" @click="sureFilter">{{ $t('commons.confirm') }}</el-button>
        </span>
      </div>
    </el-dialog>

    <!--文字组件对话框-->
    <el-dialog
      v-if="styleDialogVisible && curComponent"
      :title="$t('panel.style')"
      :visible.sync="styleDialogVisible"
      custom-class="de-style-dialog"
    >
      <PanelTextEditor v-if="curComponent.type==='v-text'" />
      <AttrListExtend v-else />
      <div style="text-align: center">
        <span slot="footer">
          <el-button size="mini" @click="closeStyleDialog">{{ $t('commons.confirm') }}</el-button>
        </span>
      </div>
    </el-dialog>

    <fullscreen style="height: 100%;background: #f7f8fa;overflow-y: auto" :fullscreen.sync="previewVisible">
      <Preview v-if="previewVisible" :in-screen="!previewVisible" :show-type="canvasStyleData.selfAdaption?'full':'width'" />
    </fullscreen>
    <input id="input" ref="files" type="file" accept="image/*" hidden @click="e => {e.target.value = '';}" @change="handleFileChange">

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

  </el-row>
</template>

<script>
import DeMainContainer from '@/components/dataease/DeMainContainer'
import DeContainer from '@/components/dataease/DeContainer'
import DeAsideContainer from '@/components/dataease/DeAsideContainer'
import { addClass, removeClass } from '@/utils'
import FilterGroup from '../filter'
import ViewSelect from '../ViewSelect'
import SubjectSetting from '../SubjectSetting'
import bus from '@/utils/bus'
import Editor from '@/components/canvas/components/Editor/index'
import { deepCopy } from '@/components/canvas/utils/utils'
import componentList, { BASE_MOBILE_STYLE } from '@/components/canvas/custom-component/component-list' // 左侧列表数据
import { mapState } from 'vuex'
import { uuid } from 'vue-uuid'
import Toolbar from '@/components/canvas/components/Toolbar'
import { findOne } from '@/api/panel/panel'
import { getPanelAllLinkageInfo } from '@/api/panel/linkage'
import PreviewFullScreen from '@/components/canvas/components/Editor/PreviewFullScreen'
import Preview from '@/components/canvas/components/Editor/Preview'
import AttrList from '@/components/canvas/components/AttrList'
import AttrListExtend from '@/components/canvas/components/AttrListExtend'
import elementResizeDetectorMaker from 'element-resize-detector'
import AssistComponent from '@/views/panel/AssistComponent'
import PanelTextEditor from '@/components/canvas/custom-component/PanelTextEditor'
import ChartGroup from '@/views/chart/group/Group'
import { searchAdviceSceneId } from '@/api/chart/chart'
// 引入样式
import '@/components/canvas/assets/iconfont/iconfont.css'
import '@/components/canvas/styles/animate.css'
import { ApplicationContext } from '@/utils/ApplicationContext'
import FilterDialog from '../filter/filterDialog'
import toast from '@/components/canvas/utils/toast'
import { commonStyle, commonAttr } from '@/components/canvas/custom-component/component-list'
import generateID from '@/components/canvas/utils/generateID'
import RectangleAttr from '@/components/canvas/components/RectangleAttr'
import TextAttr from '@/components/canvas/components/TextAttr'
import FilterTextAttr from '@/components/canvas/components/FilterTextAttr'
import { queryPanelJumpInfo } from '@/api/panel/linkJump'
import ComponentWait from '@/views/panel/edit/ComponentWait'

export default {
  name: 'PanelEdit',
  components: {
    ComponentWait,
    DeMainContainer,
    DeContainer,
    DeAsideContainer,
    FilterGroup,
    ViewSelect,
    Editor,
    Toolbar,
    FilterDialog,
    SubjectSetting,
    PreviewFullScreen,
    Preview,
    AttrList,
    AttrListExtend,
    AssistComponent,
    PanelTextEditor,
    RectangleAttr,
    TextAttr,
    ChartGroup,
    FilterTextAttr
  },
  data() {
    return {
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
      beforeDialogValue: [],
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
        'de-video'
      ]
    }
  },

  computed: {
    // 侧边显示控制
    showAside() {
      return !this.linkageSettingStatus && !this.mobileLayoutStatus
    },
    showAttr() {
      if (this.curComponent && this.showAttrComponent.includes(this.curComponent.type)) {
        // 过滤组件有标题才显示
        if (this.curComponent.type === 'custom' && !this.curComponent.options.attrs.title) {
          return false
        } else {
          return true
        }
      } else {
        return false
      }
    },
    customCanvasStyle() {
      let style = {
        padding: this.componentGap + 'px'
      }

      if (this.canvasStyleData.openCommonStyle) {
        if (this.canvasStyleData.panel.backgroundType === 'image' && this.canvasStyleData.panel.imageUrl) {
          style = {
            background: `url(${this.canvasStyleData.panel.imageUrl}) no-repeat`,
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
      'mobileLayoutStyle'
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
    panelInfo(newVal, oldVal) {
      this.init(newVal.id)
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
    this.init(this.$store.state.panel.panelInfo.id)
    // this.restore()
    // 全局监听按键事件
    // listenGlobalKeyDown()

    this.$store.commit('setCurComponent', { component: null, index: null })
    this.$store.commit('clearLinkageSettingInfo', false)
  },
  mounted() {
    // this.insertToBody()
    bus.$on('component-on-drag', () => {
      this.show = false
    })

    bus.$on('component-dialog-edit', () => {
      this.editDialog()
    })
    bus.$on('component-dialog-style', () => {
      this.styleDialogVisible = true
    })

    bus.$on('previewFullScreenClose', () => {
      this.previewVisible = false
    })
    const _this = this
    const erd = elementResizeDetectorMaker()
    // 监听div变动事件
    erd.listenTo(document.getElementById('canvasInfo-main'), element => {
      _this.$nextTick(() => {
        _this.restore()
      })
    })
  },
  beforeDestroy() {
    const elx = this.$refs.rightPanel
    elx && elx.remove()
  },
  methods: {
    init(panelId) {
      // 如果临时画布有数据 则使用临时画布数据（视图编辑的时候 会保存临时画布数据）
      const componentDataTemp = this.$store.state.panel.componentDataTemp
      const canvasStyleDataTemp = this.$store.state.panel.canvasStyleDataTemp
      if (componentDataTemp && canvasStyleDataTemp) {
        const componentDatas = JSON.parse(componentDataTemp)
        componentDatas.forEach(item => {
          item.filters = (item.filters || [])
          item.linkageFilters = (item.linkageFilters || [])
          item.auxiliaryMatrix = (item.auxiliaryMatrix || false)
          item.x = (item.x || 1)
          item.y = (item.y || 1)
          item.sizex = (item.sizex || 5)
          item.sizey = (item.sizey || 5)
        })
        this.$store.commit('setComponentData', this.resetID(componentDatas))
        const temp = JSON.parse(canvasStyleDataTemp)
        temp.refreshTime = (temp.refreshTime || 5)
        temp.refreshViewLoading = (temp.refreshViewLoading || false)
        temp.refreshUnit = (temp.refreshUnit || 'minute')

        this.$store.commit('setCanvasStyle', temp)
        // 清空临时画布数据
        this.$store.dispatch('panel/setComponentDataTemp', null)
        this.$store.dispatch('panel/setCanvasStyleDataTemp', null)
      } else if (panelId) {
        findOne(panelId).then(response => {
          const componentDatas = JSON.parse(response.data.panelData)
          const mobileComponentData = response.data.panelDataMobile ? JSON.parse(response.data.panelDataMobile) : []
          componentDatas.forEach(item => {
            item.filters = (item.filters || [])
            item.linkageFilters = (item.linkageFilters || [])
            item.auxiliaryMatrix = (item.auxiliaryMatrix || false)
            item.x = (item.x || 1)
            item.y = (item.y || 1)
            item.sizex = (item.sizex || 5)
            item.sizey = (item.sizey || 5)
            item.mobileSelected = (item.mobileSelected || false)
            item.mobileStyle = (item.mobileStyle || deepCopy(BASE_MOBILE_STYLE))
          })
          this.$store.commit('setComponentData', this.resetID(componentDatas))
          this.$store.commit('setMobileComponentData', this.resetID(mobileComponentData))
          const panelStyle = JSON.parse(response.data.panelStyle)
          panelStyle.refreshTime = (panelStyle.refreshTime || 5)
          panelStyle.refreshViewLoading = (panelStyle.refreshViewLoading || false)
          panelStyle.refreshUnit = (panelStyle.refreshUnit || 'minute')

          this.$store.commit('setCanvasStyle', panelStyle)
          this.$store.commit('recordSnapshot', 'init')// 记录快照
          // 刷新联动信息
          getPanelAllLinkageInfo(panelId).then(rsp => {
            this.$store.commit('setNowPanelTrackInfo', rsp.data)
          })
          // 刷新跳转信息
          queryPanelJumpInfo(panelId).then(rsp => {
            this.$store.commit('setNowPanelJumpInfo', rsp.data)
          })
        })
      }
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
      const stick = evt.target.closest('.el-icon-magic-stick')
      const xuanfuanniu = evt.target.closest('.icon-xuanfuanniu')
      const shujujuzhen = evt.target.closest('.icon-shujujuzhen')
      const suffix = evt.target.closest('.el-input__suffix')
      if (!parent && !self && !stick && !xuanfuanniu && !shujujuzhen && !suffix) {
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

        if (this.currentWidget.filterDialog) {
          this.show = false
          this.openFilterDialog()
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
      this.$store.commit('addComponent', { component })
      this.$store.commit('recordSnapshot', 'handleDrop')
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
    openFilterDialog() {
      this.beforeDialogValue = []
      this.filterVisible = true
    },
    closeFilter() {
      this.beforeDialogValue = []
      this.filterVisible = false
      this.currentWidget = null
      this.clearCurrentInfo()
    },
    cancelFilter() {
      this.closeFilter()
      bus.$emit('onRemoveLastItem')
    },
    sureFilter() {
      this.currentFilterCom.options.value = []
      this.$store.commit('setComponentWithId', this.currentFilterCom)
      this.$store.commit('recordSnapshot', 'sureFilter')
      this.closeFilter()
    },
    reFreshComponent(component) {
      this.currentFilterCom = component
      this.$forceUpdate()
    },
    editDialog() {
      if (this.curComponent && this.curComponent.serviceName) {
        const serviceName = this.curComponent.serviceName
        this.currentWidget = ApplicationContext.getService(serviceName)
      }
      this.currentFilterCom = this.curComponent
      this.openFilterDialog()
    },
    closeLeftPanel() {
      this.show = false
      // this.beforeDestroy()
    },
    previewFullScreen() {
      this.previewVisible = true
    },
    changeAidedDesign() {
      this.aidedButtonActive = !this.aidedButtonActive
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
            propValue: fileResult,
            style: {
              ...commonStyle
            }
          }
          component.auxiliaryMatrix = _this.canvasStyleData.auxiliaryMatrix
          if (_this.canvasStyleData.auxiliaryMatrix) {
            component.x = _this.dropComponentInfo.x
            component.y = _this.dropComponentInfo.y
            component.sizex = _this.dropComponentInfo.sizex
            component.sizey = _this.dropComponentInfo.sizey
            component.style.left = (_this.dropComponentInfo.x - 1) * _this.curCanvasScale.matrixStyleOriginWidth
            component.style.top = (_this.dropComponentInfo.y - 1) * _this.curCanvasScale.matrixStyleOriginHeight
            component.style.width = _this.dropComponentInfo.sizex * _this.curCanvasScale.matrixStyleOriginWidth
            component.style.height = _this.dropComponentInfo.sizey * _this.curCanvasScale.matrixStyleOriginHeight
          } else {
            component.style.top = _this.dropComponentInfo.shadowStyle.y
            component.style.left = _this.dropComponentInfo.shadowStyle.x
            component.style.width = _this.dropComponentInfo.shadowStyle.width
            component.style.height = _this.dropComponentInfo.shadowStyle.height
          }
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
      this.adviceGroupId = null
      this.show = false
      searchAdviceSceneId(this.panelInfo.id).then(res => {
        this.adviceGroupId = res.data
        this.$refs['chartGroup'].selectTable()
      })
    },
    newViewInfo(newViewInfo) {
      let component
      const newComponentId = uuid.v1()
      // 用户视图设置 复制一个模板
      componentList.forEach(componentTemp => {
        if (componentTemp.type === 'view') {
          component = deepCopy(componentTemp)
          const propValue = {
            id: newComponentId,
            viewId: newViewInfo.id
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
      this.$store.commit('addComponent', { component })
      this.$store.commit('recordSnapshot', 'newViewInfo')
      this.clearCurrentInfo()
      this.$store.commit('setCurComponent', { component: component, index: this.componentData.length - 1 })

      // 编辑时临时保存 当前修改的画布
      this.$store.dispatch('panel/setComponentDataTemp', JSON.stringify(this.componentData))
      this.$store.dispatch('panel/setCanvasStyleDataTemp', JSON.stringify(this.canvasStyleData))
      if (this.curComponent.type === 'view') {
        this.$store.dispatch('chart/setViewId', null)
        this.$store.dispatch('chart/setViewId', this.curComponent.propValue.viewId)
        bus.$emit('PanelSwitchComponent', { name: 'ChartEdit', param: { 'id': this.curComponent.propValue.viewId, 'optType': 'edit' }})
      }
    },
    canvasScroll(event) {
      this.scrollLeft = event.target.scrollLeft
      this.scrollTop = event.target.scrollTop
    },
    destroyTimeMachine() {
      this.timeMachine && clearTimeout(this.timeMachine)
      this.timeMachine = null
    },

    // 如果内部样式有变化 1秒钟后保存一个镜像
    recordStyleChange(index) {
      this.timeMachine = setTimeout(() => {
        if (index === this.$store.state.styleChangeTimes) {
          this.$store.commit('recordSnapshot', 'recordStyleChange')
          this.$store.state.styleChangeTimes = 0
        }
        this.destroyTimeMachine()
      }, 1000)
    },
    handleDragOver(e) {
      e.preventDefault()
      e.dataTransfer.dropEffect = 'copy'
      this.$refs.canvasEditor.handleDragOver(e)
    }
  }
}
</script>

<style scoped>
  .ms-aside-container {
    height: calc(100vh - 35px);
    max-width: 60px;
    border: none;
    width: 60px;
  }

  .ms-main-container {
    height: calc(100vh - 35px);
  }

  .de-header {
    height: 35px !important;
    border-bottom: 1px solid #E6E6E6;

  }
  .blackTheme .de-header {
      background-color: var(--SiderBG) !important;
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
  height: calc(100vh - 35px);
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

.mobile_canvas_main{
  width: 80%;
  height: 90%;
  margin-left: 10%;
  margin-top: 3%;
}

.this_mobile_canvas{
  border-radius:30px;
  min-width: 280px;
  max-width: 300px;
  min-height: 700px;
  max-height: 750px;
  overflow: hidden;
  background-color: #000000;
  background-size:100% 100% !important;
}

.this_mobile_canvas_top{
  height: 30px;
  width: 100%;
}

.this_mobile_canvas_bottom{
  height: 30px;
  width: 100%;
}

.this_mobile_canvas_main{
  overflow-x: hidden;
  overflow-y: auto;
  height:  calc(100% - 60px);;
  background-color: #d7d9e3;
  background-size:100% 100% !important;
}

.this_mobile_canvas_cell{
  text-align: center;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.this_canvas{
  width: 100%;
  height: calc(100vh - 35px);
  overflow-x: hidden;
  overflow-y: auto;
  background-size:100% 100% !important;
}
.el-main{
  height: calc(100vh - 35px);
  padding: 0!important;
  overflow: auto;
  position: relative;
}

.el-main ::v-deep .el-drawer__wrapper{
  width: 310px!important;
}
.el-main ::v-deep .el-drawer__body{
  overflow-y: auto;
}
.button-show{
    background-color: var(--ContentBG, #ebf2fe)!important;
}

.button-closed{
  background-color: var(--SiderBG, #ffffff)!important;
}
.style-aside{
  width: 250px;
  max-width:250px!important;
  border: 1px solid var(--TableBorderColor, #E6E6E6);
  padding: 10px;
  transition: all 0.3s;

}
.placeholder{
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
  width: 300px!important;
  height: 400px!important;

  .el-dialog__header{
    padding: 10px 20px !important;

    .el-dialog__headerbtn {
      top: 15px !important;
    }
  }
  .el-dialog__body{
    padding: 1px 15px !important;
  }
}
.style-hidden{
  overflow-x: hidden;
}
.button-text {
    color: var(--TextActive);
}
  .mobile-canvas{
    width: 300px;
    height: 600px;
  }

.info-class{
  text-align: center;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #FFFFFF;
  font-size: 12px;
  color: #9ea6b2;
}

::-webkit-scrollbar {
  width: 2px!important;
  height: 2px!important;
}

</style>
