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
        <div style="width: 60px; left: 0px; top: 0px; bottom: 0px;  position: absolute">
          <div style="width: 60px;height: 100%;overflow: hidden auto;position: relative;margin: 0px auto; font-size: 14px">
            <!-- 视图图表 start -->
            <div class="button-div-class" style=" width: 24px;height: 24px;text-align: center;line-height: 1;position: relative;margin: 16px auto 0px;">
              <el-button :class="show&&showIndex===0? 'button-show':'button-closed'" circle class="el-icon-circle-plus-outline" size="mini" @click="showPanel(0)" />
            </div>
            <div style="position: relative; margin: 18px auto 16px;">
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
                <div style=" position: absolute;left: 0px;right: 0px;bottom: 10px; height: 16px;">
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
                <div style=" position: absolute;left: 0px;right: 0px;bottom: 10px; height: 16px;">
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

        <div
          id="canvasInfo"
          :class="{'style-hidden':canvasStyleData.selfAdaption}"
          class="content this_canvas"
          @drop="handleDrop"
          @dragover="handleDragOver"
          @mousedown="handleMouseDown"
          @mouseup="deselectCurComponent"
          @scroll="canvasScroll"
        >
          <Editor v-if="!previewVisible" :out-style="outStyle" />
        </div>
      </de-main-container>
      <!--      <de-aside-container v-if="aidedButtonActive" :class="aidedButtonActive ? 'show' : 'hidden'" class="style-aside">-->
      <!--        <AttrListExtend v-if="curComponent" />-->
      <!--        <p v-else class="placeholder">{{ $t('panel.select_component') }}</p>-->
      <!--      </de-aside-container>-->

    </de-container>

    <el-dialog
      v-if="filterVisible && panelInfo.id"
      :title="$t('panel.module')"
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
    <RectangleAttr v-if="curComponent&&curComponent.type==='rect-shape'" :scroll-left="scrollLeft" :scroll-top="scrollTop" />
    <TextAttr v-if="curComponent&&curComponent.type==='v-text'" :scroll-left="scrollLeft" :scroll-top="scrollTop" />
    <FilterTextAttr v-if="curComponent&&curComponent.type==='custom'&&curComponent.options.attrs.title" :scroll-left="scrollLeft" :scroll-top="scrollTop" />
    <!--复用ChartGroup组件 不做显示-->
    <ChartGroup
      ref="chartGroup"
      :opt-from="'panel'"
      :advice-group-id="adviceGroupId"
      style="height: 0px;width:0px;padding:0px;overflow: hidden"
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
import componentList from '@/components/canvas/custom-component/component-list' // 左侧列表数据
import { listenGlobalKeyDown } from '@/components/canvas/utils/shortcutKey'
import { mapState } from 'vuex'
import { uuid } from 'vue-uuid'
import Toolbar from '@/components/canvas/components/Toolbar'
import { findOne } from '@/api/panel/panel'
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
// import '@/components/canvas/styles/reset.css'

import { ApplicationContext } from '@/utils/ApplicationContext'
import FilterDialog from '../filter/filterDialog'
import toast from '@/components/canvas/utils/toast'
import { commonStyle, commonAttr } from '@/components/canvas/custom-component/component-list'
import generateID from '@/components/canvas/utils/generateID'
import RectangleAttr from '@/components/canvas/components/RectangleAttr'
import TextAttr from '@/views/Tinymce/TextAttr'
import FilterTextAttr from '@/views/Tinymce/FilterTextAttr'

export default {
  name: 'PanelEdit',
  components: {
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
      timeMachine: null
    }
  },

  computed: {
    panelInfo() {
      return this.$store.state.panel.panelInfo
    },
    ...mapState([
      'curComponent',
      'curCanvasScale',
      'isClickComponent',
      'canvasStyleData',
      'curComponentIndex',
      'componentData'
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
    }
  },
  created() {
    this.init(this.$store.state.panel.panelInfo.id)
    // this.restore()
    // 全局监听按键事件
    listenGlobalKeyDown()

    this.$store.commit('setCurComponent', { component: null, index: null })
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
        })
        this.$store.commit('setComponentData', this.resetID(componentDatas))
        // this.$store.commit('setComponentData', this.resetID(JSON.parse(componentDataTemp)))
        this.$store.commit('setCanvasStyle', JSON.parse(canvasStyleDataTemp))
        // 清空临时画布数据
        this.$store.dispatch('panel/setComponentDataTemp', null)
        this.$store.dispatch('panel/setCanvasStyleDataTemp', null)
      } else if (panelId) {
        findOne(panelId).then(response => {
          const componentDatas = JSON.parse(response.data.panelData)
          componentDatas.forEach(item => {
            item.filters = (item.filters || [])
          })
          this.$store.commit('setComponentData', this.resetID(componentDatas))
          //   this.$store.commit('setComponentData', this.resetID(JSON.parse(response.data.panelData)))
          const panelStyle = JSON.parse(response.data.panelStyle)
          this.$store.commit('setCanvasStyle', panelStyle)
          this.$store.commit('recordSnapshot')// 记录快照
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
      if (!parent && !self && !stick) {
        this.show = false
        window.removeEventListener('click', this.closeSidebar)
        this.showIndex = -1
      }
    },
    // insertToBody() {
    //   this.$nextTick(() => {
    //     const elx = this.$refs.leftPanel
    //     const body = document.querySelector('body')
    //     body.insertBefore(elx, body.firstChild)
    //   })
    // },

    resetID(data) {
      if (data) {
        data.forEach(item => {
          item.type !== 'custom' && (item.id = uuid.v1())
        })
      }
      return data
    },
    handleDrop(e) {
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
          }
        })
      } else {
        this.currentWidget = ApplicationContext.getService(componentInfo.id)

        this.currentFilterCom = this.currentWidget.getDrawPanel()
        this.currentFilterCom.style.top = this.getPositionY(e.layerY)
        this.currentFilterCom.style.left = this.getPositionX(e.layerX)
        this.currentFilterCom.id = newComponentId
        if (this.currentWidget.filterDialog) {
          this.show = false
          this.openFilterDialog()
          return
        }
        component = deepCopy(this.currentFilterCom)
      }

      // position = absolution 或导致有偏移 这里中和一下偏移量
      component.style.top = this.getPositionY(e.layerY)
      component.style.left = this.getPositionX(e.layerX)
      component.id = newComponentId
      this.$store.commit('addComponent', { component })
      this.$store.commit('recordSnapshot')
      this.clearCurrentInfo()

      // // 文字组件
      // if (component.type === 'v-text') {
      //   this.$store.commit('setCurComponent', { component: component, index: this.componentData.length })
      //   this.styleDialogVisible = true
      //   this.show = false
      // }
    },
    clearCurrentInfo() {
      this.currentWidget = null
      this.currentFilterCom = null
    },

    handleDragOver(e) {
      e.preventDefault()
      e.dataTransfer.dropEffect = 'copy'
    },

    handleMouseDown() {
      // console.log('handleMouseDown123')

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
    cancelFilter() {
      this.beforeDialogValue = []
      this.filterVisible = false
      this.currentWidget = null
      this.clearCurrentInfo()
    },
    sureFilter() {
      this.currentFilterCom.options.value = []
      const component = deepCopy(this.currentFilterCom)

      //   this.$store.commit('addComponent', { component })
      this.$store.commit('setComponentWithId', component)
      this.$store.commit('recordSnapshot')
      this.cancelFilter()
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
      if (document.getElementById('canvasInfo')) {
        this.$nextTick(() => {
          const canvasHeight = document.getElementById('canvasInfo').offsetHeight
          const canvasWidth = document.getElementById('canvasInfo').offsetWidth
          this.outStyle.height = canvasHeight
          this.outStyle.width = canvasWidth
          // console.log(canvasHeight + '--' + canvasWidth)
        })
      }
    },
    closeStyleDialog() {
      this.styleDialogVisible = false
    },
    goFile() {
      this.$refs.files.click()
    },
    handleFileChange(e) {
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
          const scaleWith = img.width / 400
          const scaleHeight = img.height / 200
          let scale = scaleWith > scaleHeight ? scaleWith : scaleHeight
          scale = scale > 1 ? scale : 1
          this.$store.commit('addComponent', {
            component: {
              ...commonAttr,
              id: generateID(),
              component: 'Picture',
              label: '图片',
              icon: '',
              propValue: fileResult,
              style: {
                ...commonStyle,
                top: this.getPositionY(this.currentDropElement.layerY),
                left: this.getPositionX(this.currentDropElement.layerX),
                width: img.width / scale,
                height: img.height / scale
              }
            }
          })

          this.$store.commit('recordSnapshot')
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
        }
      })

      // position = absolution 或导致有偏移 这里中和一下偏移量
      component.style.top = 0
      component.style.left = 600
      component.id = newComponentId
      this.$store.commit('addComponent', { component })
      this.$store.commit('recordSnapshot')
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
          this.$store.commit('recordSnapshot')
          this.$store.state.styleChangeTimes = 0
        }
        this.destroyTimeMachine()
      }, 1000)
    }
  }
}
</script>

<style scoped>
  .ms-aside-container {
    height: calc(100vh - 91px);
    max-width: 60px;
    border: none;
    width: 60px;
  }

  .ms-main-container {
    height: calc(100vh - 91px);
  }

  .de-header {
    height: 35px !important;
    border-bottom: 1px solid #E6E6E6;
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
  height: calc(100vh - 91px);
  position: fixed;
  top: 91px;
  left: 60px;
  box-shadow: 0px 0px 15px 0px rgba(0, 0, 0, .05);
  transition: all .25s cubic-bezier(.7, .3, .1, 1);
  transform: translate(100%);
  background: #fff;
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

.this_canvas{
  height: calc(100vh - 91px);
  overflow: auto;
}
.el-main{
  height: calc(100vh - 91px);
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
    background-color: #ebf2fe!important;
}

.button-closed{
  background-color: #ffffff!important;
}
.style-aside{
  width: 250px;
  max-width:250px!important;
  border: 1px solid #E6E6E6;
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
    // background-color: #f4f4f5;
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
    overflow: hidden;
  }

</style>
