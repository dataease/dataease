<template>
  <div
    :id="canvasDomId"
    class="canvas_content"
    @drop="handleDrop"
    @dragover="handleDragOver"
    @mousedown="handleMouseDown"
    @mouseup="deselectCurComponent"
    @scroll="canvasScroll"
  >
    <de-editor
      :ref="editorRefName"
      :canvas-style-data="canvasStyleData"
      :component-data="componentData"
      :canvas-id="canvasId"
      :parent-forbid="parentForbid"
      :matrix-count="matrixCountBase"
      :out-style="outStyle"
      :scroll-top="scrollTop"
      @canvasDragging="canvasDragging"
    />
    <input
      id="input"
      ref="files"
      type="file"
      accept="image/*"
      hidden
      @click="e => {e.target.value = '';}"
      @change="handleFileChange"
    >
    <el-dialog
      v-if="buttonVisible && panelInfo.id"
      :title="(currentWidget && currentWidget.getLeftPanel && currentWidget.getLeftPanel().label ? $t(currentWidget.getLeftPanel().label) : '') + $t('panel.module')"
      :visible.sync="buttonVisible"
      custom-class="de-button-dialog"
      :append-to-body="true"
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
      custom-class="de-filter-dialog min-width-730"
      append-to-body
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
          <el-button
            size="mini"
            @click="cancelFilter"
          >{{ $t('commons.cancel') }}</el-button>
          <el-button
            :disabled="!enableSureButton"
            type="primary"
            size="mini"
            @click="sureFilter"
          >{{ $t('commons.confirm') }}</el-button>
        </span>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { mapState } from 'vuex'
import DeEditor from '@/components/canvas/components/editor/DeEditor'
import elementResizeDetectorMaker from 'element-resize-detector'
import bus from '@/utils/bus'
import { deepCopy, imgUrlTrans } from '@/components/canvas/utils/utils'
import { uuid } from 'vue-uuid'
import componentList, {
  BASE_MOBILE_STYLE,
  COMMON_BACKGROUND,
  commonAttr,
  HYPERLINKS,
  PIC_STYLE
} from '@/components/canvas/customComponent/component-list'
import { ApplicationContext } from '@/utils/ApplicationContext'
import { chartCopy } from '@/api/chart/chart'
import { adaptCurThemeCommonStyle } from '@/components/canvas/utils/style'
import toast from '@/components/canvas/utils/toast'
import ButtonDialog from '@/views/panel/filter/ButtonDialog'
import ButtonResetDialog from '@/views/panel/filter/ButtonResetDialog'
import FilterDialog from '@/views/panel/filter/FilterDialog'
import { uploadFileResult } from '@/api/staticResource/staticResource'

export default {
  components: { FilterDialog, ButtonResetDialog, ButtonDialog, DeEditor },
  props: {
    parentForbid: {
      type: Boolean,
      require: false,
      default: true
    },
    canvasStyleData: {
      type: Object,
      require: true
    },
    componentData: {
      type: Array,
      require: false,
      default: () => []
    },
    canvasId: {
      type: String,
      require: true
    },
    canvasPid: {
      type: String,
      require: true
    },
    mobileLayoutStatus: {
      type: Boolean,
      require: false,
      default: false
    }
  },
  data() {
    return {
      maxImageSize: 15000000,
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
      filterVisible: false,
      autoMoveOffSet: 15,
      buttonVisible: false,
      currentWidget: null,
      currentFilterCom: null,
      currentDropElement: null,
      canvasDomId: 'canvas-id-' + this.canvasId,
      editorRefName: 'canvas-editor-' + this.canvasId,
      scrollLeft: 0,
      scrollTop: 0,
      outStyle: {
        width: null,
        height: null
      }
    }
  },
  computed: {
    matrixCountBase() {
      if (this.isMainCanvas && this.mobileLayoutStatus) {
        return this.mobileMatrixCount
      } else {
        return this.pcMatrixCountBase
      }
    },
    isMainCanvas() {
      return this.canvasId === 'canvas-main'
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
    curCanvasScaleSelf() {
      return this.curCanvasScaleMap[this.canvasId]
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
    ...mapState([
      'curComponent',
      'componentGap',
      'isClickComponent',
      'linkageSettingStatus',
      'pcMatrixCount',
      'pcTabMatrixCount',
      'dragComponentInfo',
      'curCanvasScaleMap',
      'mobileMatrixCount'
    ])
  },
  watch: {
    mobileLayoutStatus() {
      this.restore()
    }
  },
  created() {
  },
  mounted() {
    const _this = this
    // 监听div变动事件
    const erd = elementResizeDetectorMaker()
    erd.listenTo(document.getElementById(this.canvasDomId), element => {
      _this.$nextTick(() => {
        _this.restore()
      })
    })
  },
  beforeDestroy() {
    bus.$off('component-dialog-edit', this.editDialog)
    bus.$off('button-dialog-edit', this.editButtonDialog)
  },
  methods: {
    getWrapperChildRefs() {
      return this.$refs[this.editorRefName].getWrapperChildRefs()
    },
    initEvents() {
      bus.$on('component-dialog-edit', this.editDialog)
      bus.$on('button-dialog-edit', this.editButtonDialog)
    },
    getGap() {
      return this.mobileLayoutStatus ? 0 : this.componentGap * 2
    },
    restore() {
      this.$nextTick(() => {
        const domInfo = document.getElementById(this.canvasDomId)
        if (domInfo) {
          this.outStyle.height = domInfo.offsetHeight - this.getGap()
          // 临时处理 确保每次restore 有会更新
          this.outStyle.width = domInfo.offsetWidth + (Math.random() * 0.000001)
        }
      })
    },
    handleDragOver(e) {
      e.preventDefault()
      e.dataTransfer.dropEffect = 'copy'
      this.$refs[this.editorRefName].handleDragOver(e)
    },
    handleMouseDown() {
      this.$store.commit('setClickComponentStatus', false)
    },

    deselectCurComponent(e) {
      if (!this.isClickComponent && !this.linkageSettingStatus) {
        this.$store.commit('setCurComponent', { component: null, index: null })
      }

      // 0 左击 1 滚轮 2 右击
      if (e.button !== 2) {
        this.$store.commit('hideContextMenu')
      }
    },
    canvasScroll(e) {
      this.scrollTop = e.target.scrollTop
      this.$emit('canvasScroll', { scrollLeft: e.target.scrollLeft, scrollTop: e.target.scrollTop })
      bus.$emit('onScroll')
    },
    // handleDrop(e) {
    //   this.$emit('handleDrop', e)
    // }
    handleDrop(e) {
      if (!this.dragComponentInfo) {
        return
      }
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
        this.currentFilterCom['canvasId'] = this.canvasId
        this.currentFilterCom['canvasPid'] = this.canvasPid// 待处理
        if (this.canvasStyleData.auxiliaryMatrix) {
          this.currentFilterCom.x = this.dropComponentInfo.x
          this.currentFilterCom.y = this.dropComponentInfo.y
          this.currentFilterCom.sizex = this.dropComponentInfo.sizex
          this.currentFilterCom.sizey = this.dropComponentInfo.sizey
          this.currentFilterCom.style.left = (this.dragComponentInfo.x - 1) * this.curCanvasScaleSelf.matrixStyleOriginWidth
          this.currentFilterCom.style.top = (this.dragComponentInfo.y - 1) * this.curCanvasScaleSelf.matrixStyleOriginHeight
          this.currentFilterCom.style.width = this.dragComponentInfo.sizex * this.curCanvasScaleSelf.matrixStyleOriginWidth
          this.currentFilterCom.style.height = this.dragComponentInfo.sizey * this.curCanvasScaleSelf.matrixStyleOriginHeight
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

        component.style.left = (this.dragComponentInfo.x - 1) * this.curCanvasScaleSelf.matrixStyleOriginWidth
        component.style.top = (this.dragComponentInfo.y - 1) * this.curCanvasScaleSelf.matrixStyleOriginHeight
        component.style.width = this.dragComponentInfo.sizex * this.curCanvasScaleSelf.matrixStyleOriginWidth
        component.style.height = this.dragComponentInfo.sizey * this.curCanvasScaleSelf.matrixStyleOriginHeight
      } else {
        component.style.top = this.dropComponentInfo.shadowStyle.y
        component.style.left = this.dropComponentInfo.shadowStyle.x
        component.style.width = this.dropComponentInfo.shadowStyle.width
        component.style.height = this.dropComponentInfo.shadowStyle.height
      }

      component.id = newComponentId
      component['canvasId'] = this.canvasId
      component['canvasPid'] = this.canvasPid
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
    goFile() {
      this.$refs.files.click()
    },
    sizeMessage() {
      this.$notify({
        message: this.$t('panel.image_size_tips'),
        position: 'top-left'
      })
    },
    handleFileChange(e) {
      const _this = this
      const file = e.target.files[0]
      if (!file.type.includes('image')) {
        toast(this.$t('panel.image_size_tips'))
        return
      }
      if (file.size > this.maxImageSize) {
        this.sizeMessage()
      }
      uploadFileResult(file, (fileUrl) => {
        const component = {
          ...commonAttr,
          id: uuid.v1(),
          component: 'Picture',
          type: 'picture-add',
          label: '图片',
          icon: '',
          hyperlinks: HYPERLINKS,
          mobileStyle: BASE_MOBILE_STYLE,
          propValue: imgUrlTrans(fileUrl),
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
        component['canvasId'] = this.canvasId
        component['canvasPid'] = this.canvasPid
        this.$store.commit('addComponent', {
          component: component
        })
        this.$store.commit('recordSnapshot', 'handleFileChange')
      })
    },
    clearCurrentInfo() {
      this.currentWidget = null
      this.currentFilterCom = null
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
      // 主画布打开
      if (this.isMainCanvas && this.curComponent && this.curComponent.serviceName) {
        this.editType = editType
        const serviceName = this.curComponent.serviceName
        this.currentWidget = ApplicationContext.getService(serviceName)
        this.currentFilterCom = this.curComponent
        this.openFilterDialog()
      }
    },
    editButtonDialog(editType) {
      // 主画布打开
      if (this.isMainCanvas && this.curComponent && this.curComponent.serviceName) {
        this.editType = editType
        const serviceName = this.curComponent.serviceName
        this.currentWidget = ApplicationContext.getService(serviceName)
        this.currentFilterCom = this.curComponent
        this.openButtonDialog()
      }
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
      // if (this.isMainCanvas && this.mobileLayoutStatus && this.curComponent && this.curComponent.optStatus.dragging) {
      //   // 触发滚动的区域偏移量
      //   const touchOffset = 100
      //   const canvasInfoMobile = document.getElementById(this.canvasDomId)
      //   // 获取子盒子（高度肯定比父盒子大）
      //   // const editorMobile = document.getElementById('editorMobile')
      //   // 画布区顶部到浏览器顶部距离
      //   const canvasTop = canvasInfoMobile.offsetTop + 75
      //   // 画布区有高度
      //   const canvasHeight = canvasInfoMobile.offsetHeight
      //   // 画布区域底部距离浏览器顶部距离
      //   const canvasBottom = canvasTop + canvasHeight
      //   if (mY > (canvasBottom - touchOffset) && offsetY > 0) {
      //     // 触发底部滚动
      //     this.scrollMove(this.autoMoveOffSet)
      //   } else if (mY < (canvasTop + touchOffset) && offsetY < 0) {
      //     // 触发顶部滚动
      //     this.scrollMove(-this.autoMoveOffSet)
      //   }
      // }
    },
    scrollMove(offset) {
      // const canvasInfoMobile = document.getElementById(this.canvasDomId)
      // canvasInfoMobile.scrollTop = canvasInfoMobile.scrollTop + offset
      // this.$store.commit('setScrollAutoMove', this.scrollAutoMove + offset)
    }
  }
}
</script>

<style lang="scss">
.canvas_content {
  width: 100%;
  height: 100%;
  overflow-x: hidden;
  overflow-y: auto;
  background-size: 100% 100% !important;
}

.min-width-730 {
  min-width: 730px !important;
}
</style>
