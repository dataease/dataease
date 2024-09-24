<template>
  <div
    class="bar-main"
    :class="showEditPosition"
    @mousedown="showLabelInfo"
  >
    <input
      id="input"
      ref="files"
      type="file"
      accept="image/*"
      hidden
      @click="e => {e.target.value = '';}"
      @change="handleFileChange"
    >
    <div
      v-if="linkageAreaShow"
      style="margin-right: -1px;width: 20px"
    >
      <el-checkbox
        v-model="linkageInfo.linkageActive"
        size="medium"
      />
      <linkage-field
        v-if="linkageInfo.linkageActive"
        :element="element"
      />
    </div>
    <div
      v-if="positionCheck('multiplexing') && showMultiplexingCheck"
      style="margin-right: 1px;width: 18px;z-index: 5"
    >
      <el-checkbox
        v-model="multiplexingCheckModel"
        size="medium"
        @change="multiplexingCheck"
      />
    </div>
    <div
      v-if="batchOptAreaShow"
      style="margin-right: -1px;width: 20px;z-index: 5"
    >
      <el-checkbox
        size="medium"
        @change="batchOptChange"
      />
    </div>
    <div v-if="normalAreaShow">
      <span :title="$t('panel.edit')">
        <i
          v-if="activeModel==='edit'&&curComponent&&editFilter.includes(curComponent.type)"
          class="icon iconfont icon-edit"
          @click.stop="edit"
        />
      </span>
      <span :title="$t('panel.matrix')">
        <i
          v-if="activeModel==='edit'&&curComponent.auxiliaryMatrix"
          class="icon iconfont icon-shujujuzhen"
          @click.stop="auxiliaryMatrixChange"
        />
      </span>
      <span :title="$t('panel.suspension')">
        <i
          v-if="activeModel==='edit'&&!curComponent.auxiliaryMatrix"
          class="icon iconfont icon-xuanfuanniu"
          @click.stop="auxiliaryMatrixChange"
        />
      </span>
      <span :title="$t('panel.enlarge')">
        <i
          v-if="enlargeShow"
          class="icon iconfont icon-fangda"
          @click.stop="showViewDetails('enlarge')"
        />
      </span>
      <span :title="$t('panel.details')">
        <i
          v-if="detailsShow"
          class="icon iconfont icon-chakan"
          @click.stop="showViewDetails('details')"
        />
      </span>
      <span :title="$t('route.exportExcel')">
        <i
          v-if="exportExcelShow"
          style="line-height: 24px"
          class="el-icon-document-delete"
          @click.stop="exportExcelDownload()"
        />
      </span>
      <span
        v-if="exportFormattedExcelShow"
        :title="$t('chart.export_formatted_excel')"
        @click.stop="exportFormattedExcel()"
      >
        <svg-icon
          style="font-size: 14px; color: white; margin-right: 3px"
          icon-class="ds-excel-format"
        />
      </span>
      <setting-menu
        v-if="activeModel==='edit'"
        style="float: right;height: 24px!important;"
        @amRemoveItem="amRemoveItem"
        @linkJumpSet="linkJumpSet"
        @boardSet="boardSet"
      >
        <span
          slot="icon"
          :title="$t('panel.setting')"
        >
          <i
            class="icon iconfont icon-shezhi"
            style="margin-top:2px; width: 16px;"
          />
        </span>
      </setting-menu>
      <span :title="$t('panel.cancel_linkage')">
        <i
          v-if="curComponent.type==='view'&&existLinkage"
          class="icon iconfont icon-quxiaoliandong"
          @click.stop="clearLinkage"
        />
      </span>
      <span :title="$t('panel.switch_picture')">
        <i
          v-if="activeModel==='edit'&&curComponent&&curComponent.type==='picture-add'"
          class="icon iconfont icon-genghuan"
          @click.stop="goFile"
        />
      </span>
      <el-popover
        v-if="selectFieldShow"
        width="200"
        trigger="click"
        @mousedown="fieldsAreaDown"
      >
        <fields-list
          :fields="curFields"
          :element="element"
        />
        <i
          slot="reference"
          :disabled="element.editing"
          :title="$t('panel.select_field')"
          class="icon iconfont icon-datasource-select"
          style="margin-left: 4px;cursor: pointer;font-size: 14px;"
        />
      </el-popover>
      <span :title="$t('panel.jump')">
        <a
          v-if="showJumpFlag"
          :title="curComponent.hyperlinks.content "
          :target="curComponent.hyperlinks.openMode "
          :href="curComponent.hyperlinks.content "
        >
          <i class="icon iconfont icon-com-jump" />
        </a>
      </span>

      <map-layer-controller
        v-if="chart && showMapLayerController"
        :chart="chart"
        :series-id-map="seriesIdMap"
        :show-edit-position="showEditPosition"
      />
    </div>

    <!--跳转设置-->
    <el-dialog
      :visible.sync="linkJumpSetVisible"
      width="900px"
      class="dialog-css"
      :show-close="true"
      :destroy-on-close="true"
      :append-to-body="true"
    >
      <link-jump-set
        v-if="linkJumpSetVisible"
        :view-id="linkJumpSetViewId"
        @closeJumpSetDialog="closeJumpSetDialog"
      />
    </el-dialog>

    <!--背景设置-->
    <el-dialog
      :visible.sync="boardSetVisible"
      width="750px"
      top="5vh"
      class="dialog-css"
      :close-on-click-modal="false"
      :show-close="false"
      :destroy-on-close="true"
      :append-to-body="true"
    >
      <background
        v-if="boardSetVisible"
        @backgroundSetClose="backgroundSetClose"
      />
    </el-dialog>
  </div>
</template>

<script>
import { mapState } from 'vuex'
import bus from '@/utils/bus'
import SettingMenu from '@/components/canvas/components/editor/SettingMenu'
import LinkageField from '@/components/canvas/components/editor/LinkageField'
import toast from '@/components/canvas/utils/toast'
import FieldsList from '@/components/canvas/components/editor/FieldsList'
import LinkJumpSet from '@/views/panel/linkJumpSet'
import Background from '@/views/background/index'
import MapLayerController from '@/views/chart/components/map/MapLayerController'
import { uploadFileResult } from '@/api/staticResource/staticResource'
import eventBus from '@/components/canvas/utils/eventBus'
import { hasDataPermission } from '@/utils/permission'
import { exportExcelDownload } from '@/components/canvas/utils/utils'
import { Button } from 'element-ui'
import { exportPivotExcel } from '@/views/chart/chart/common/common_table'

export default {
  components: { Background, LinkJumpSet, FieldsList, SettingMenu, LinkageField, MapLayerController },

  props: {
    canvasId: {
      type: String,
      required: true
    },
    terminal: {
      type: String,
      default: 'pc'
    },
    sourceElement: {
      type: Object,
      default: () => {
      }
    },
    element: {
      type: Object,
      required: true
    },
    active: {
      type: Boolean,
      required: false,
      default: false
    },
    // 当前模式 preview 预览 edit 编辑，
    activeModel: {
      type: String,
      required: false,
      default: 'preview'
    },
    previewVisible: {
      type: Boolean,
      default: false
    },
    showPosition: {
      type: String,
      required: false,
      default: 'NotProvided'
    },
    chart: {
      type: Object,
      default: null
    },
    seriesIdMap: {
      type: Object,
      default: () => {
        return {
          id: ''
        }
      }
    }
  },
  data() {
    return {
      systemOS: 'Mac',
      maxImageSize: 15000000,
      boardSetVisible: false,
      linkJumpSetVisible: false,
      linkJumpSetViewId: null,
      curFields: [],
      multiplexingCheckModel: false,
      barWidth: 24,
      componentType: null,
      linkageActiveStatus: false,
      editFilter: [
        'view',
        'custom',
        'custom-button'
      ],
      timer: null,
      viewXArray: []
    }
  },

  computed: {
    yaxis() {
      if (!this.chart) return []
      return JSON.parse(this.chart.yaxis)
    },
    showMapLayerController() {
      return this.curComponent.type === 'view' && this.terminal === 'pc' && this.curComponent.propValue.innerType && this.curComponent.propValue.innerType === 'map' && this.yaxis.length > 1
    },
    detailsShow() {
      return this.curComponent.type === 'view' && this.terminal === 'pc' && this.curComponent.propValue.innerType && this.curComponent.propValue.innerType !== 'richTextView'
    },
    exportExcelShow() {
      return this.detailsShow && hasDataPermission('export', this.$store.state.panel.panelInfo.privileges) && this.chart && this.chart.dataFrom !== 'template'
    },
    exportFormattedExcelShow() {
      return this.detailsShow &&
        hasDataPermission('export', this.$store.state.panel.panelInfo.privileges) &&
        this.chart &&
        this.chart.dataFrom !== 'template' &&
        this.chart.type === 'table-pivot' &&
        JSON.parse(this.chart.customAttr).size?.tableLayoutMode !== 'tree'
    },
    enlargeShow() {
      return this.curComponent.type === 'view' && this.curComponent.propValue.innerType && this.curComponent.propValue.innerType !== 'richTextView' && !this.curComponent.propValue.innerType.includes('table')
    },
    selectFieldShow() {
      return this.activeModel === 'edit' && this.curComponent.type === 'view' && this.curComponent.propValue.innerType && this.curComponent.propValue.innerType === 'richTextView' && this.curComponent.editing
    },
    curComponentTypes() {
      const types = []
      this.componentData.forEach(component => {
        types.push(component.type)
      })
      return types
    },
    showMultiplexingCheck() {
      return this.element.type !== 'custom-button' || (this.element.type === 'custom-button' && !this.curComponentTypes.includes('custom-button'))
    },
    showEditPosition() {
      if (this.activeModel === 'edit' && !this.linkageAreaShow && !this.batchOptAreaShow) {
        const toRight = (this.canvasStyleData.width - this.element.style.left - this.element.style.width) * this.curCanvasScaleSelf.scalePointWidth
        const toLeft = this.element.style.left * this.curCanvasScaleSelf.scalePointWidth
        if (this.barWidth < toRight) {
          return 'bar-main-right'
        } else if (this.barWidth > toRight && this.barWidth > toLeft) {
          return 'bar-main-left-inner'
        } else {
          return 'bar-main-left-outer'
        }
      } else {
        return 'bar-main-preview'
      }
    },
    showJumpFlag() {
      return this.curComponent && this.curComponent.hyperlinks && this.curComponent.hyperlinks.enable
    },
    // batch operation area
    batchOptAreaShow() {
      return this.batchOptStatus && this.element.type === 'view' && !this.element.isPlugin
    },
    // 联动区域按钮显示
    linkageAreaShow() {
      return this.linkageSettingStatus && this.element !== this.curLinkageView && this.element.type === 'view'
    },
    // 编辑或预览区域显示
    normalAreaShow() {
      return !this.linkageSettingStatus && !this.batchOptStatus && !this.positionCheck('multiplexing')
    },
    existLinkage() {
      let linkageFiltersCount = 0
      this.componentData.forEach(item => {
        if (item.linkageFilters && item.linkageFilters.length > 0) {
          item.linkageFilters.forEach(linkage => {
            if (this.element.propValue.viewId === linkage.sourceViewId) {
              linkageFiltersCount++
            }
          })
        }
      })
      return linkageFiltersCount
    },
    linkageInfo() {
      return this.targetLinkageInfo[this.element.propValue.viewId]
    },
    miniHeight() {
      return this.mobileLayoutStatus ? 1 : 1
    },
    miniWidth() {
      return this.mobileLayoutStatus ? 1 : 1
    },
    curCanvasScaleSelf() {
      return this.curCanvasScaleMap[this.canvasId]
    },
    ...mapState([
      'menuTop',
      'menuLeft',
      'menuShow',
      'curComponent',
      'componentData',
      'canvasStyleData',
      'linkageSettingStatus',
      'targetLinkageInfo',
      'curLinkageView',
      'curCanvasScaleMap',
      'batchOptStatus',
      'mobileLayoutStatus',
      'curBatchOptComponents',
      'panelViewDetailsInfo'
    ])
  },
  watch: {
    linkageAreaShow: {
      handler(val) {
        // 1.当前正在进行联动设置（val） 2.当前视图联动未启用!this.linkageInfo.linkageActive
        // 3.当前视图没有设置过当前目标联动!this.linkageInfo.sourceViewId
        // 4.数据集相同 this.linkageInfo.tableId === this.targetLinkageInfo[this.curLinkageView.propValue.viewId].tableId)
        // 满足以上条件自动勾选
        if (val && !this.linkageInfo.linkageActive && !this.linkageInfo.sourceViewId && this.linkageInfo.tableId === this.targetLinkageInfo[this.curLinkageView.propValue.viewId].tableId) {
          this.linkageInfo.linkageActive = true
        }
      },
      immediate: true
    }
  },
  mounted() {
    if (navigator.platform.indexOf('Mac') === -1) {
      this.systemOS = 'Other'
    }
    this.initCurFields()
    if (this.element.type === 'view') {
      bus.$on('initCurFields-' + this.element.id, this.initCurFields)
      eventBus.$on('viewEnlarge', this.viewEnlarge)
    }
  },
  beforeDestroy() {
    eventBus.$off('preview', this.showViewDetails)
  },
  methods: {
    viewEnlarge() {
      this.showViewDetails('enlarge')
    },
    backgroundSetClose() {
      this.boardSetVisible = false
    },
    linkJumpSet() {
      this.linkJumpSetViewId = this.element.propValue.viewId
      this.linkJumpSetVisible = true
    },
    closeJumpSetDialog() {
      this.linkJumpSetVisible = false
    },
    fieldsAreaDown(e) {
      // ignore
      e.preventDefault()
    },
    initCurFields() {
      if (this.element.type === 'view') {
        const chartInfo = this.panelViewDetailsInfo[this.element.propValue.viewId]
        if (chartInfo) {
          this.curFields = []
          const chartDetails = JSON.parse(chartInfo)
          if (chartDetails.type === 'richTextView' && chartDetails.data) {
            this.curFields = chartDetails.data.fields
          }
        }
      }
    },
    positionCheck(position) {
      return this.showPosition.includes(position)
    },
    multiplexingCheck(val) {
      if (val) {
        // push
        this.$store.commit('addCurMultiplexingComponent', {
          'component': this.sourceElement,
          'componentId': this.element.id
        })
      } else {
        // remove
        this.$store.commit('removeCurMultiplexingComponentWithId', this.element.id)
      }
    },
    closePreview() {
      this.$emit('closePreview')
    },
    destroyTimer() {
      if (this.timer) {
        clearInterval(this.timer)
        this.timer = null
      }
    },
    showViewDetails(openType = 'details') {
      this.$emit('showViewDetails', { openType: openType })
    },
    exportDataCb(val) {
      if (val && val.success) {
        this.openMessageLoading(this.exportData)
      }

      if (val && val.success === false) {
        this.openMessageSuccess(`${this.chart.title ? this.chart.title : this.chart.name} 导出失败，前往`, 'error', this.exportData)
      }
    },
    exportData() {
      bus.$emit('data-export-center')
    },
    openMessageLoading(cb) {
      const h = this.$createElement
      const iconClass = `el-icon-loading`
      const customClass = `de-message-loading de-message-export`
      this.$message({
        message: h('p', null, [
          this.$t('data_export.exporting'),
          h(
            Button,
            {
              props: {
                type: 'text'
              },
              class: 'btn-text',
              on: {
                click: () => {
                  cb()
                }
              }
            },
            this.$t('data_export.export_center')
          ),
          this.$t('data_export.export_info')
        ]),
        iconClass,
        showClose: true,
        customClass
      })
    },
    openMessageSuccess(text, type, cb) {
      const h = this.$createElement
      const iconClass = `el-icon-${type || 'success'}`
      const customClass = `de-message-${type || 'success'} de-message-export`
      this.$message({
        message: h('p', null, [
          h('span', null, text),
          h(
            Button,
            {
              props: {
                type: 'text'
              },
              class: 'btn-text',
              on: {
                click: () => {
                  cb()
                }
              }
            },
            this.$t('data_export.export_center')
          )
        ]),
        iconClass,
        showClose: true,
        customClass
      })
    },
    exportExcelDownload() {
      exportExcelDownload(this.chart, null, null, null, null, null, this.exportDataCb)
    },
    exportFormattedExcel() {
      const instance = this.$store.state.chart.tableInstance[this.chart.id]
      if (!instance) {
        return
      }
      exportPivotExcel(instance, this.chart)
    },
    auxiliaryMatrixChange() {
      if (this.curComponent.auxiliaryMatrix) {
        this.curComponent.auxiliaryMatrix = false
        this.$emit('amRemoveItem')
      } else {
        this.curComponent.x = Math.round(this.curComponent.style.left / this.curCanvasScaleSelf.matrixStyleOriginWidth) + 1
        this.curComponent.y = Math.round(this.curComponent.style.top / this.curCanvasScaleSelf.matrixStyleOriginHeight) + 1
        this.curComponent.sizex = Math.round(this.curComponent.style.width / this.curCanvasScaleSelf.matrixStyleOriginWidth)
        this.curComponent.sizey = Math.round(this.curComponent.style.height / this.curCanvasScaleSelf.matrixStyleOriginHeight)
        this.curComponent.sizey = this.curComponent.sizey > this.miniHeight ? this.curComponent.sizey : this.miniHeight
        this.curComponent.sizex = this.curComponent.sizex > this.miniWidth ? this.curComponent.sizex : this.miniWidth
        this.curComponent.auxiliaryMatrix = true
        this.$emit('amAddItem')
      }
      setTimeout(() => {
        this.recordMatrixCurShadowStyle()
      }, 50)
      this.$store.commit('canvasChange')
      bus.$emit('auxiliaryMatrixChange')
    },
    // 记录当前样式 跟随阴影位置 矩阵处理
    recordMatrixCurShadowStyle() {
      const left = (this.curComponent.x - 1) * this.curCanvasScaleSelf.matrixStyleWidth
      const top = (this.curComponent.y - 1) * this.curCanvasScaleSelf.matrixStyleHeight
      let width = this.curComponent.sizex * this.curCanvasScaleSelf.matrixStyleWidth
      const height = this.curComponent.sizey * this.curCanvasScaleSelf.matrixStyleHeight
      const ruleWidth = this.curCanvasScaleSelf.scalePointWidth * this.canvasStyleData.width - 5
      if (width > ruleWidth) {
        width = ruleWidth
      }
      const style = {
        left: left,
        top: top,
        width: width,
        height: height
      }
      this.$store.commit('setShapeStyle', style)
      // resize
      this.$emit('resizeView')
    },
    edit() {
      if (this.curComponent.type === 'custom') {
        bus.$emit('component-dialog-edit', 'update')
      } else if (this.curComponent.type === 'custom-button') {
        bus.$emit('button-dialog-edit')
      } else if (this.curComponent.type === 'v-text' || this.curComponent.type === 'de-rich-text' || this.curComponent.type === 'rect-shape') {
        bus.$emit('component-dialog-style')
      } else {
        bus.$emit('change_panel_right_draw', true)
      }
    },
    linkageEdit() {

    },
    amRemoveItem() {
      this.$emit('amRemoveItem')
    },
    // 清除相同sourceViewId 的 联动条件
    clearLinkage() {
      this.$store.commit('clearViewLinkage', this.element.propValue.viewId)
    },
    goFile() {
      this.$refs.files.click()
    },
    showLabelInfo(e) {
      // ignore
      e.preventDefault()
    },
    sizeMessage() {
      this.$notify({
        message: this.$t('panel.image_size_tips'),
        position: 'top-left'
      })
    },
    handleFileChange(e) {
      const file = e.target.files[0]
      if (!file.type.includes('image')) {
        toast(this.$t('panel.image_size_tips'))
        return
      }
      if (file.size > this.maxImageSize) {
        this.sizeMessage()
      }
      uploadFileResult(file, (fileUrl) => {
        this.curComponent.propValue = fileUrl
        this.$store.commit('recordSnapshot', 'handleFileChange')
      })
    },
    boardSet() {
      this.boardSetVisible = true
    },
    batchOptChange(val) {
      if (val) {
        // push
        this.$store.commit('addCurBatchComponent', this.element.propValue.viewId)
      } else {
        // remove
        this.$store.commit('removeCurBatchComponentWithId', this.element.propValue.viewId)
      }
    }
  }
}
</script>

<style lang="scss" scoped>
.bar-main {
  line-height: 24px;
  position: absolute;
  float: right;
  z-index: 10;
  border-radius: 2px;
  padding-left: 3px;
  padding-right: 0px;
  cursor: pointer !important;
  background-color: var(--primary, #3370ff);
}

.bar-main ::v-deep i {
  color: white;
  float: right;
  margin-right: 3px;
}

.bar-main ::v-deep .el-checkbox__inner {
  width: 16px;
  height: 16px;
}

.bar-main ::v-deep .el-checkbox__inner::after {
  width: 4.5px;
}

.bar-main-right {
  width: 22px;
  right: -25px;
}

.bar-main-left-inner {
  width: 22px;
  left: 0px;
}

.bar-main-left-outer {
  width: 22px;
  left: -25px;
}

.bar-main-preview {
  right: 0px;
}

</style>
