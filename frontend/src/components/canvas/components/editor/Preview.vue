<template>
  <div
    :id="previewMainDomId"
    class="bg"
    :style="customStyle"
    @scroll="canvasScroll"
  >
    <canvas-opt-bar />
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
import { userLoginInfo } from '@/api/systemInfo/userLogin'

const erd = elementResizeDetectorMaker()

export default {
  components: { ComponentWrapper, CanvasOptBar },
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
    }
  },
  data() {
    return {
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
      buttonFilterMap: null
    }
  },
  computed: {
    mainActiveName() {
      return this.$store.state.panel.mainActiveName
    },
    showUnpublishedArea() {
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
        if (this.canvasStyleData.panel.backgroundType === 'image' && this.canvasStyleData.panel.imageUrl) {
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
      const result = this.buildButtonFilterMap(this.componentData)
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
    }
  },
  created() {
    // 取消视图请求
    this.$cancelRequest('/chart/view/getData/**')
    this.$cancelRequest('/api/link/viewDetail/**')
    this.$cancelRequest('/static-resource/**')
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
    bus.$on('trigger-search-button', this.triggerSearchButton)
    bus.$on('trigger-reset-button', this.triggerResetButton)
  },
  beforeDestroy() {
    erd.uninstall(this.$refs[this.previewTempRefId])
    erd.uninstall(this.$refs[this.previewRefId])
    clearInterval(this.timer)
    bus.$off('trigger-search-button', this.triggerSearchButton)
    bus.$off('trigger-reset-button', this.triggerResetButton)
  },
  methods: {
    initWatermark() {
      if (this.panelInfo.watermarkInfo) {
        userLoginInfo().then(res => {
          const userInfo = res.data
          activeWatermark(this.panelInfo.watermarkInfo.settingContent, userInfo, 'preview-main-canvas-main', this.canvasId, this.panelInfo.watermarkOpen)
        })
      }
    },
    isMainCanvas() {
      return this.canvasId === 'canvas-main'
    },
    triggerResetButton() {
      this.triggerSearchButton(true)
    },
    triggerSearchButton(isClear = false) {
      const result = this.buildButtonFilterMap(this.componentData, isClear)
      this.searchButtonInfo.autoTrigger = result.autoTrigger
      this.searchButtonInfo.filterMap = result.filterMap
      this.buttonFilterMap = this.searchButtonInfo.filterMap

      this.componentData.forEach(component => {
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
      const refs = this.$refs
      if (!this.$refs['viewWrapperChild'] || !this.$refs['viewWrapperChild'].length) return result
      panelItems.forEach((element) => {
        if (element.type !== 'custom') {
          return true
        }

        const index = this.getComponentIndex(element.id)
        if (index < 0) {
          return true
        }
        let param = null
        const wrapperChild = refs['viewWrapperChild'][index]
        if (isClear) {
          wrapperChild.clearHandler && wrapperChild.clearHandler()
        }
        param = wrapperChild.getCondition && wrapperChild.getCondition()
        const condition = formatCondition(param)
        const vValid = valueValid(condition)
        const filterComponentId = condition.componentId
        Object.keys(result).forEach(viewId => {
          const vidMatch = viewIdMatch(condition.viewIds, viewId)
          const viewFilters = result[viewId]
          let j = viewFilters.length
          while (j--) {
            const filter = viewFilters[j]
            if (filter.componentId === filterComponentId) {
              viewFilters.splice(j, 1)
            }
          }
          vidMatch && vValid && viewFilters.push(condition)
        })
      })
      return result
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
        this.searchCount++
      }, refreshTime)
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
              if (key === 'fontSize' && this.terminal === 'mobile') {
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
      this.$refs['userViewDialog'].exportExcel()
    },
    exportViewImg() {
      this.$refs['userViewDialog'].exportViewImg()
    },
    deselectCurComponent(e) {
      if (!this.isClickComponent) {
        this.$store.commit('setCurComponent', { component: null, index: null })
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
              this.$emit('mainHeightChange', _this.mainHeight)
            })
          })
        }
      }, 1500)
    }
  }
}
</script>

<style lang="scss" scoped>
.bg {
  min-width: 200px;
  min-height: 300px;
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

.mobile-dialog-css ::v-deep .el-dialog__headerbtn {
  top: 7px
}

.mobile-dialog-css ::v-deep .el-dialog__body {
  padding: 0px;
}

::-webkit-scrollbar {
  width: 0px !important;
  height: 0px !important;
}

::v-deep .el-tabs__nav {
  z-index: 0;
}

</style>
