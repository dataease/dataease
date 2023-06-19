<template>
  <div
    :id="previewMainDomId"
    class="bg"
    :style="customStyle"
    @scroll="canvasScroll"
  >
    <canvas-opt-bar
      v-if="canvasId==='canvas-main'"
      ref="canvas-opt-bar"
      :canvas-style-data="canvasStyleData"
      @link-export-pdf="downloadAsPDF"
    />
    <div
      :id="previewDomId"
      :ref="previewRefId"
      :style="canvasInfoMainStyle"
    >
      <el-row
        v-if="showUnpublishedArea"
        class="custom-position"
      >
        <div style="text-align: center">
          <svg-icon
            icon-class="unpublished"
            style="font-size: 75px"
          />
          <br>
          <span>{{ $t('panel.panel_off') }}</span>
        </div>
      </el-row>
      <el-row
        v-else-if="componentDataShow && componentDataShow.length===0"
        class="custom-position"
      >
        <span v-show="isMainCanvas()">{{ $t('panel.panelNull') }}</span>
      </el-row>
      <div
        v-else
        :id="previewTempDomId"
        :ref="previewTempRefId"
        :style="[canvasInfoTempStyle,screenShotStyle]"
        class="main-class"
        @mouseup="deselectCurComponent"
        @mousedown="handleMouseDown"
      >
        <ComponentWrapper
          v-for="(item, index) in componentDataInfo"
          :key="index"
          ref="viewWrapperChild"
          :config="item"
          :canvas-id="canvasId"
          :source-config="componentData[index]"
          :search-count="searchCount"
          :in-screen="inScreen"
          :terminal="terminal"
          :is-relation="relationFilterIds.includes(item.id)"
          :filters="filterMap[item.propValue && item.propValue.viewId]"
          :screen-shot="screenShot"
          :canvas-style-data="canvasStyleData"
          :show-position="showPosition"
        />
      </div>
    </div>
    <el-dialog
      v-if="pdfExportShow"
      :title="'['+panelInfo.name+']'+$t('panel.pdf_export')"
      :visible.sync="pdfExportShow"
      width="80%"
      :top="'8vh'"
      :destroy-on-close="true"
      class="dialog-css2"
    >
      <span style="position: absolute;right: 70px;top:15px">
        <svg-icon
          icon-class="PDF"
          class="ds-icon-pdf"
        />
        <el-select
          v-model="pdfTemplateSelectedIndex"
          :placeholder="$t('panel.switch_pdf_template')"
          @change="changePdfTemplate()"
        >
          <el-option
            v-for="(item, index) in pdfTemplateAll"
            :key="index"
            :label="item.name"
            :value="index"
          />
        </el-select>
      </span>
      <PDFPreExport
        :snapshot="snapshotInfo"
        :panel-name="panelInfo.name"
        :template-content="pdfTemplateContent"
        @closePreExport="closePreExport"
      />
    </el-dialog>

    <!--视图详情-->
    <el-dialog
      :visible.sync="chartDetailsVisible"
      width="80%"
      class="dialog-css"
      :destroy-on-close="true"
      :show-close="true"
      :append-to-body="false"
      top="5vh"
    >
      <span
        v-if="chartDetailsVisible"
        style="position: absolute;right: 70px;top:15px"
      >
        <el-button
          v-if="showChartInfoType==='enlarge' && hasDataPermission('export',panelInfo.privileges)&& showChartInfo && showChartInfo.type !== 'symbol-map'"
          class="el-icon-picture-outline"
          size="mini"
          :disabled="imageDownloading"
          @click="exportViewImg"
        >
          {{ $t('chart.export_img') }}
        </el-button>
        <el-button
          v-if="showChartInfoType==='details'&& hasDataPermission('export',panelInfo.privileges)"
          size="mini"
          :disabled="$store.getters.loadingMap[$store.getters.currentPath]"
          @click="exportExcel"
        >
          <svg-icon
            icon-class="ds-excel"
            class="ds-icon-excel"
          />{{ $t('chart.export') }}Excel
        </el-button>
      </span>
      <user-view-dialog
        v-if="chartDetailsVisible"
        ref="userViewDialog-canvas-main"
        :chart="showChartInfo"
        :chart-table="showChartTableInfo"
        :canvas-style-data="canvasStyleData"
        :open-type="showChartInfoType"
      />
    </el-dialog>
  </div>
</template>

<script>
import { getStyle } from '@/components/canvas/utils/style'
import { mapState } from 'vuex'
import ComponentWrapper from './ComponentWrapper'
import { changeStyleWithScale } from '@/components/canvas/utils/translate'
import { uuid } from 'vue-uuid'
import { deepCopy, imgUrlTrans } from '@/components/canvas/utils/utils'
import eventBus from '@/components/canvas/utils/eventBus'
import elementResizeDetectorMaker from 'element-resize-detector'
import CanvasOptBar from '@/components/canvas/components/editor/CanvasOptBar'
import bus from '@/utils/bus'
import { buildFilterMap, buildViewKeyMap, formatCondition, valueValid, viewIdMatch } from '@/utils/conditionUtil'
import { hasDataPermission } from '@/utils/permission'
import { activeWatermark } from '@/components/canvas/tools/watermark'
import { proxyUserLoginInfo, userLoginInfo } from '@/api/systemInfo/userLogin'
import html2canvas from 'html2canvasde'
import { queryAll } from '@/api/panel/pdfTemplate'
import PDFPreExport from '@/views/panel/export/PDFPreExport'
import { listenGlobalKeyDownPreview } from '@/components/canvas/utils/shortcutKey'
import UserViewDialog from '@/components/canvas/customComponent/UserViewDialog'

const erd = elementResizeDetectorMaker()
export default {
  components: { UserViewDialog, ComponentWrapper, CanvasOptBar, PDFPreExport },
  model: {
    prop: 'show',
    event: 'change'
  },
  props: {
    // 后端截图
    backScreenShot: {
      type: Boolean,
      default: false
    },
    screenShot: {
      type: Boolean,
      default: false
    },
    show: {
      type: Boolean,
      default: false
    },
    showType: {
      type: String,
      required: false,
      default: 'full'
    },
    inScreen: {
      type: Boolean,
      required: false,
      default: true
    },
    activeTab: {
      type: String,
      required: false,
      default: 'none'
    },
    componentData: {
      type: Array,
      required: false,
      default: function() {
        return []
      }
    },
    canvasStyleData: {
      type: Object,
      required: false,
      default: function() {
        return {}
      }
    },
    showPosition: {
      type: String,
      required: false,
      default: 'NotProvided'
    },
    panelInfo: {
      type: Object,
      required: true
    },
    canvasId: {
      type: String,
      require: false,
      default: 'canvas-main'
    },
    userId: {
      type: String,
      require: false
    }
  },
  data() {
    return {
      imageDownloading: false,
      chartDetailsVisible: false,
      showChartInfo: {},
      showChartTableInfo: {},
      showChartInfoType: 'details',
      mainHeightCount: null,
      userInfo: null,
      previewMainDomId: 'preview-main-' + this.canvasId,
      previewDomId: 'preview-' + this.canvasId,
      previewRefId: 'preview-ref-' + this.canvasId,
      previewTempDomId: 'preview-temp-' + this.canvasId,
      previewTempRefId: 'preview-temp-ref-' + this.canvasId,
      isShowPreview: false,
      panelId: '',
      needToChangeHeight: [
        'top',
        'height'
      ],
      needToChangeWidth: [
        'left',
        'width',
        'fontSize',
        'activeFontSize',
        'borderWidth',
        'letterSpacing'
      ],
      scaleWidth: '100',
      scaleHeight: '100',
      timer: null,
      componentDataShow: null,
      mainWidth: '100%',
      mainHeight: '100%',
      searchCount: 0,
      // 布局展示 1.pc pc端布局 2.mobile 移动端布局
      terminal: 'pc',
      buttonFilterMap: null,
      pdfExportShow: false,
      dataLoading: false,
      exporting: false,
      snapshotInfo: '',
      pdfTemplateSelectedIndex: 0,
      pdfTemplateContent: '',
      templateInfo: {},
      pdfTemplateAll: []
    }
  },
  computed: {
    screenShotStatues() {
      return this.exporting || this.screenShot || this.backScreenShot
    },
    mainActiveName() {
      return this.$store.state.panel.mainActiveName
    },
    showUnpublishedArea() {
      if (this.canvasId !== 'canvas-main') {
        return false
      }
      if (this.showPosition === 'edit') {
        return false
      } else if (this.panelInfo && this.panelInfo.showType === 'view') {
        return false
      } else if ((this.mainActiveName === 'PanelMain' && this.activeTab === 'PanelList') || this.showPosition.includes('multiplexing')) {
        return this.panelInfo.status === 'unpublished' && !hasDataPermission('manage', this.panelInfo.privileges)
      } else {
        return this.panelInfo.status === 'unpublished'
      }
    },
    canvasInfoMainStyle() {
      if (this.backScreenShot) {
        return {
          width: '100%',
          height: this.mainHeight
        }
      } else {
        return {
          width: '100%',
          height: '100%'
        }
      }
    },
    canvasInfoTempStyle() {
      if (this.screenShot) {
        return {
          width: '100%',
          height: this.mainHeight
        }
      } else {
        return {
          width: '100%',
          height: '100%'
        }
      }
    },
    customStyle() {
      let style = {
        width: '100%'
      }
      if (this.canvasStyleData.openCommonStyle && this.isMainCanvas()) {
        const styleInfo = this.terminal === 'mobile' && this.canvasStyleData.panel.mobileSetting && this.canvasStyleData.panel.mobileSetting.customSetting
          ? this.canvasStyleData.panel.mobileSetting : this.canvasStyleData.panel
        if (styleInfo.backgroundType === 'image' && typeof (styleInfo.imageUrl) === 'string') {
          style = {
            background: `url(${imgUrlTrans(styleInfo.imageUrl)}) no-repeat`
          }
        } else if (styleInfo.backgroundType === 'color') {
          style = {
            background: styleInfo.color
          }
        } else {
          style = {
            background: '#f7f8fa'
          }
        }
      }
      if (this.backScreenShot) {
        style.height = this.mainHeight
      } else {
        style.padding = '5px'
      }
      return style
    },
    screenShotStyle() {
      return this.screenShot ? this.customStyle : {}
    },
    // 此处单独计算componentData的值 不放入全局mapState中
    componentDataInfo() {
      return this.componentDataShow || []
    },
    ...mapState([
      'isClickComponent'
    ]),

    searchButtonInfo() {
      const result = this.buildButtonFilterMap(this.$store.state.componentData)
      return result
    },
    filterMap() {
      const result = buildFilterMap(this.componentData)
      if (this.searchButtonInfo && this.searchButtonInfo.buttonExist && !this.searchButtonInfo.autoTrigger && this.searchButtonInfo.relationFilterIds) {
        for (const key in result) {
          if (Object.hasOwnProperty.call(result, key)) {
            let filters = result[key]
            filters = filters.filter(item => !this.searchButtonInfo.relationFilterIds.includes(item.componentId))
            result[key] = filters
          }
        }
      }
      return result
    },
    buttonExist() {
      return this.searchButtonInfo && this.searchButtonInfo.buttonExist
    },
    relationFilterIds() {
      return this.buttonExist && this.searchButtonInfo.relationFilterIds || []
    }
  },
  watch: {
    componentData: {
      handler(newVal, oldVla) {
        this.restore()
      },
      deep: true
    },
    canvasStyleData: {
      handler(newVal, oldVla) {
        this.canvasStyleDataInit()
      },
      deep: true
    },
    mainHeight: {
      handler(newVal, oldVla) {
        const _this = this
        _this.$nextTick(() => {
          if (_this.screenShotStatues) {
            _this.initWatermark('preview-temp-canvas-main')
          } else {
            _this.initWatermark()
          }
        })
      }
    }
  },
  created() {
    // 防止编辑界面销毁键盘事件监听
    if (this.canvasId === 'canvas-main' && !this.showPosition.includes('multiplexing')) {
      listenGlobalKeyDownPreview()
    }
  },
  mounted() {
    this.initWatermark()
    this._isMobile()
    this.initListen()
    this.$store.commit('clearLinkageSettingInfo', false)
    this.canvasStyleDataInit()
    if (this.terminal === 'mobile') {
      this.initMobileCanvas()
    }
    this.canvasId === 'canvas-main' && bus.$on('pcChartDetailsDialog', this.openChartDetailsDialog)
    bus.$on('trigger-search-button', this.triggerSearchButton)
    bus.$on('trigger-reset-button', this.triggerResetButton)
    this.initPdfTemplate()
  },
  beforeDestroy() {
    erd.uninstall(this.$refs[this.previewTempRefId])
    erd.uninstall(this.$refs[this.previewRefId])
    clearInterval(this.timer)
    this.canvasId === 'canvas-main' && bus.$off('pcChartDetailsDialog', this.openChartDetailsDialog)
    bus.$off('trigger-search-button', this.triggerSearchButton)
    bus.$off('trigger-reset-button', this.triggerResetButton)
  },
  methods: {
    getWrapperChildRefs() {
      return this.$refs['viewWrapperChild']
    },
    getAllWrapperChildRefs() {
      let allChildRefs = []
      const currentChildRefs = this.getWrapperChildRefs()
      if (currentChildRefs && currentChildRefs.length > 0) {
        allChildRefs.push.apply(allChildRefs, currentChildRefs)
      }
      currentChildRefs && currentChildRefs.forEach(subRef => {
        if (subRef?.getType && subRef.getType() === 'de-tabs') {
          const currentTabChildRefs = subRef.getWrapperChildRefs()
          if (currentTabChildRefs && currentTabChildRefs.length > 0) {
            allChildRefs.push.apply(allChildRefs, currentTabChildRefs)
          }
        }
      })
      return allChildRefs
    },

    getCanvasHeight() {
      return this.mainHeightCount
    },
    openChartDetailsDialog(paramInfo) {
      if (this.canvasId === 'canvas-main') {
        this.showChartInfo = paramInfo.showChartInfo
        this.showChartTableInfo = paramInfo.showChartTableInfo
        this.showChartInfoType = paramInfo.showChartInfoType
        this.chartDetailsVisible = true
      }
    },
    initWatermark(waterDomId = 'preview-main-canvas-main') {
      if (this.panelInfo.watermarkInfo && this.canvasId === 'canvas-main') {
        if (this.userInfo) {
          activeWatermark(this.panelInfo.watermarkInfo.settingContent, this.userInfo, waterDomId, this.canvasId, this.panelInfo.watermarkOpen)
        } else {
          const method = this.userId ? proxyUserLoginInfo : userLoginInfo
          method().then(res => {
            this.userInfo = res.data
            activeWatermark(this.panelInfo.watermarkInfo.settingContent, this.userInfo, waterDomId, this.canvasId, this.panelInfo.watermarkOpen)
          })
        }
      }
    },
    isMainCanvas() {
      return this.canvasId === 'canvas-main'
    },
    triggerResetButton() {
      this.triggerSearchButton(true)
      this.$refs['viewWrapperChild']?.forEach(item => {
        if (item?.responseResetButton) {
          item.responseResetButton()
        }
      })
    },
    triggerSearchButton(isClear = false) {
      if (this.canvasId !== 'canvas-main') {
        return
      }
      const result = this.buildButtonFilterMap(this.$store.state.componentData, isClear)
      this.searchButtonInfo.autoTrigger = result.autoTrigger
      this.searchButtonInfo.filterMap = result.filterMap
      this.buttonFilterMap = this.searchButtonInfo.filterMap

      this.$store.state.componentData.forEach(component => {
        if (component.type === 'view' && this.buttonFilterMap[component.propValue.viewId]) {
          component.filters = this.buttonFilterMap[component.propValue.viewId]
        }
        if (component.type === 'de-tabs') {
          for (let idx = 0; idx < component.options.tabList.length; idx++) {
            const ele = component.options.tabList[idx].content
            if (!ele.type || ele.type !== 'view') continue
            ele.filters = this.buttonFilterMap[ele.propValue.viewId]
          }
        }
      })
    },
    buildButtonFilterMap(panelItems, isClear = false) {
      const result = {
        buttonExist: false,
        relationFilterIds: [],
        autoTrigger: true,
        filterMap: {}
      }
      if (!panelItems || !panelItems.length) return result
      let sureButtonItem = null
      result.buttonExist = panelItems.some(item => {
        if (item.type === 'custom-button' && item.serviceName === 'buttonSureWidget') {
          sureButtonItem = item
          return true
        }
      })

      if (!result.buttonExist) return result

      const customRange = sureButtonItem.options.attrs.customRange
      result.autoTrigger = sureButtonItem.options.attrs.autoTrigger
      const allFilters = panelItems.filter(item => item.type === 'custom')

      const matchFilters = customRange && allFilters.filter(item => sureButtonItem.options.attrs.filterIds.includes(item.id)) || allFilters

      result.relationFilterIds = matchFilters.map(item => item.id)

      let viewKeyMap = buildViewKeyMap(panelItems)
      viewKeyMap = this.buildViewKeyFilters(matchFilters, viewKeyMap, isClear)
      result.filterMap = viewKeyMap
      return result
    },
    buildViewKeyFilters(panelItems, result, isClear = false) {
      const wrapperChildAll = this.getAllWrapperChildRefs()
      if (!wrapperChildAll || !wrapperChildAll.length) return result
      panelItems.forEach((element) => {
        if (element.type !== 'custom') {
          return true
        }
        let wrapperChild
        wrapperChildAll?.forEach(item => {
          if (item?.['getComponentId'] && item.getComponentId() === element.id) {
            wrapperChild = item
          }
        })
        if (!wrapperChild || !wrapperChild.getCondition) return true
        let param = null
        if (isClear) {
          wrapperChild.clearHandler && wrapperChild.clearHandler()
        }
        param = wrapperChild.getCondition && wrapperChild.getCondition()
        const condition = formatCondition(param)
        const vValid = valueValid(condition)
        const filterComponentId = condition.componentId
        const conditionCanvasId = wrapperChild.getCanvasId && wrapperChild.getCanvasId()
        Object.keys(result).forEach(viewId => {
          const vidMatch = viewIdMatch(condition.viewIds, viewId)
          const viewFilters = result[viewId]
          const canvasMatch = this.checkCanvasViewIdsMatch(conditionCanvasId, viewId)

          let j = viewFilters.length
          while (j--) {
            const filter = viewFilters[j]
            if (filter.componentId === filterComponentId) {
              viewFilters.splice(j, 1)
            }
          }
          canvasMatch && vidMatch && vValid && viewFilters.push(condition)
        })
      })
      return result
    },
    checkCanvasViewIdsMatch(conditionCanvasId, viewId) {
      if (conditionCanvasId === 'canvas-main') {
        return true
      }
      for (let index = 0; index < this.$store.state.componentData.length; index++) {
        const item = this.$store.state.componentData[index]
        if (item.type === 'view' && item.propValue.viewId === viewId && item.canvasId === conditionCanvasId) return true
      }
      return false
    },
    getComponentIndex(id) {
      for (let index = 0; index < this.componentData.length; index++) {
        const item = this.componentData[index]
        if (item.id === id) return index
      }
      return -1
    },
    _isMobile() {
      const flag = navigator.userAgent.match(/(phone|pad|pod|iPhone|iPod|ios|iPad|Android|Mobile|BlackBerry|IEMobile|MQQBrowser|JUC|Fennec|wOSBrowser|BrowserNG|WebOS|Symbian|Windows Phone)/i)
      this.terminal = flag ? 'mobile' : 'pc'
      // this.terminal = 'mobile'
    },
    canvasStyleDataInit() {
      // 数据刷新计时器
      if (this.canvasStyleData.refreshViewEnable) {
        this.searchCount = 0
        this.timer && clearInterval(this.timer)
        let refreshTime = 300000
        if (this.canvasStyleData.refreshTime && this.canvasStyleData.refreshTime > 0) {
          if (this.canvasStyleData.refreshUnit === 'second') {
            refreshTime = this.canvasStyleData.refreshTime * 1000
          } else {
            refreshTime = this.canvasStyleData.refreshTime * 60000
          }
        }
        this.timer = setInterval(() => {
          this.clearAllLinkage()
          this.searchCount++
        }, refreshTime)
      }
    },
    clearAllLinkage() {
      this.$store.commit('clearPanelLinkageInfo')
      bus.$emit('clear_panel_linkage', { viewId: 'all' })
    },
    changeStyleWithScale,
    getStyle,
    restore() {
      const canvasHeight = document.getElementById(this.previewDomId).offsetHeight
      const canvasWidth = document.getElementById(this.previewDomId).offsetWidth
      this.scaleWidth = (canvasWidth) * 100 / this.canvasStyleData.width // 获取宽度比
      // 如果是后端截图方式使用 的高度伸缩比例和宽度比例相同
      if (this.backScreenShot) {
        this.scaleHeight = this.scaleWidth
      } else {
        this.scaleHeight = canvasHeight * 100 / this.canvasStyleData.height// 获取高度比
      }
      if (this.isMainCanvas()) {
        this.$store.commit('setPreviewCanvasScale', {
          scaleWidth: (this.scaleWidth / 100),
          scaleHeight: (this.scaleHeight / 100)
        })
      }
      this.handleScaleChange()
    },
    resetID(data) {
      if (data) {
        data.forEach(item => {
          item.type !== 'custom' && (item.id = uuid.v1())
        })
      }
      return data
    },
    format(value, scale) {
      return value * scale / 100
    },
    handleScaleChange() {
      if (this.componentData) {
        const componentData = deepCopy(this.componentData)
        componentData.forEach(component => {
          Object.keys(component.style).forEach(key => {
            if (this.needToChangeHeight.includes(key)) {
              component.style[key] = this.format(component.style[key], this.scaleHeight)
            }
            if (this.needToChangeWidth.includes(key)) {
              if ((key === 'fontSize' || key === 'activeFontSize') && (this.terminal === 'mobile' || ['custom', 'v-text'].includes(component.type))) {
                // do nothing 移动端字符大小无需按照比例缩放，当前保持不变(包括 v-text 和 过滤组件)
              } else {
                component.style[key] = this.format(component.style[key], this.scaleWidth)
              }
            }
          })
        })
        this.componentDataShow = componentData
        this.$nextTick(() => (eventBus.$emit('resizing', '')))
      }
    },
    exportExcel() {
      this.$refs['userViewDialog-canvas-main'].exportExcel()
    },
    exportViewImg() {
      this.imageDownloading = true
      this.$refs['userViewDialog-canvas-main'].exportViewImg(()=>{
        this.imageDownloading = false
      })
    },
    deselectCurComponent(e) {
      if (!this.isClickComponent) {
        this.$store.commit('setCurComponent', { component: null, index: null })
        if (this.$refs?.['canvas-opt-bar']) {
          this.$refs['canvas-opt-bar'].setWidgetStatus()
        }
      }
    },
    handleMouseDown() {
      this.$store.commit('setClickComponentStatus', false)
    },
    initMobileCanvas() {
      this.$store.commit('openMobileLayout')
    },
    canvasScroll() {
      bus.$emit('onScroll')
    },
    initListen() {
      const _this = this
      const canvasMain = document.getElementById(this.previewDomId)
      // 监听主div变动事件
      if (canvasMain) {
        erd.listenTo(canvasMain, element => {
          _this.$nextTick(() => {
            _this.restore()
          })
        })
      }
      setTimeout(() => {
        // 监听画布div变动事件
        const tempCanvas = document.getElementById(this.previewTempDomId)
        if (tempCanvas) {
          erd.listenTo(document.getElementById(this.previewTempDomId), element => {
            _this.$nextTick(() => {
              // 将mainHeight 修改为px 临时解决html2canvas 截图不全的问题
              _this.mainHeight = tempCanvas.scrollHeight + 'px!important'
              _this.mainHeightCount = tempCanvas.scrollHeight
              this.$emit('mainHeightChange', _this.mainHeight)
            })
          })
        }
      }, 1500)
    },
    downloadAsPDF() {
      this.dataLoading = true
      this.$emit('change-load-status', true)
      const domId = this.previewMainDomId
      setTimeout(() => {
        this.exporting = true
        this.backScreenShot = true
        const scrollHeight = document.getElementById('preview-temp-canvas-main').scrollHeight

        document.getElementById('preview-canvas-main').style.height = (scrollHeight + 'px')
        setTimeout(() => {
          html2canvas(document.getElementById(domId)).then(canvas => {
            const snapshot = canvas.toDataURL('image/jpeg', 1) // 是图片质量
            this.dataLoading = false
            this.$emit('change-load-status', false)
            this.exporting = false
            this.backScreenShot = false
            if (snapshot !== '') {
              this.snapshotInfo = snapshot
              this.pdfExportShow = true
            }
          })
        }, 2500)
      }, 500)
    },
    closePreExport() {
      this.pdfExportShow = false
    },
    changePdfTemplate() {
      this.pdfTemplateContent = this.pdfTemplateAll[this.pdfTemplateSelectedIndex] ? this.pdfTemplateAll[this.pdfTemplateSelectedIndex].templateContent : ''
    },
    initPdfTemplate() {
      queryAll().then(res => {
        this.pdfTemplateAll = res.data
        this.changePdfTemplate()
      })
    }
  }
}
</script>

<style lang="scss" scoped>
.bg {
  min-width: 200px;
  width: 100%;
  height: 100%;
  overflow-x: hidden;
  background-size: 100% 100% !important;
}

.main-class {
  width: 100%;
  height: 100%;
  background-size: 100% 100% !important;
}

.custom-position {
  line-height: 30px;
  width: 100%;
  z-index: 100;
  height: 100%;
  text-align: center;
  cursor: not-allowed;
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-size: 14px;
  flex-flow: row nowrap;
  color: #9ea6b2;
}

.dialog-css ::v-deep .el-dialog__title {
  font-size: 14px;
}

.dialog-css ::v-deep .el-dialog__header {
  padding: 40px 20px 0;
}

.dialog-css ::v-deep .el-dialog__body {
  padding: 10px 20px 20px;
}

::-webkit-scrollbar {
  width: 0px !important;
  height: 0px !important;
}

::v-deep .el-tabs__nav {
  z-index: 0;
}

</style>
