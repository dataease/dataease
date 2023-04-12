<template>
  <div>
    <div class="switch-position">
      <el-radio-group
        v-model="mobileLayoutInitStatus"
        size="mini"
        @change="openMobileLayout"
      >
        <el-radio-button :label="false">
          <span class="icon iconfont icon-icon_pc_outlined icon16_only" />
        </el-radio-button>
        <el-radio-button :label="true">
          <span class="icon iconfont icon-icon_phone_outlined icon16_only" />
        </el-radio-button>
      </el-radio-group>
    </div>
    <div
      v-show="editControlButton"
      class="toolbar"
    >
      <span style="float: right;">
        <el-button
          v-if="mobileLayoutStatus"
          size="mini"
          @click="editReset"
        >
          {{ $t('commons.reset') }}
        </el-button>
        <el-button
          type="primary"
          size="mini"
          @click="editSave"
        >
          {{ $t('commons.confirm') }}
        </el-button>
        <el-button
          size="mini"
          @click="editCancel"
        >
          {{ $t('commons.cancel') }}
        </el-button>
      </span>
    </div>

    <div
      v-show="!editControlButton"
      class="toolbar"
    >
      <div class="panel-info-area">
        <!--back to panelList-->
        <svg-icon
          icon-class="icon_left_outlined"
          class="toolbar-icon-active icon20 margin-left20"
          @click="closePanelEdit"
        />
        <span class="text16 margin-left12">
          {{ panelInfo.name }}
        </span>
      </div>
      <el-tooltip :content="$t('panel.undo') ">
        <svg-icon
          icon-class="icon_undo_outlined"
          class="toolbar-icon-active icon16 margin-right20"
          @click="undo"
        />
      </el-tooltip>
      <el-tooltip :content="$t('panel.redo') ">
        <svg-icon
          icon-class="icon_redo_outlined"
          class="toolbar-icon-active icon16 margin-right20"
          @click="redo"
        />
      </el-tooltip>
      <el-tooltip
        v-if="!isOtherPlatform"
        :content="$t('panel.fullscreen_preview')"
      >
        <svg-icon
          icon-class="icon_magnify_outlined"
          class="toolbar-icon-active icon16"
          @click="clickPreview"
        />
      </el-tooltip>
      <el-divider
        style="margin-left: 20px"
        direction="vertical"
      />
      <span class="button_self">
        <el-dropdown
          :hide-on-click="false"
          trigger="click"
          placement="bottom-start"
        >
          <span class="icon iconfont icon-icon-more insert margin-right20">
            <span class="icon-font-margin">{{ $t('panel.more') }}</span>
          </span>
          <el-dropdown-menu slot="dropdown">
            <el-dropdown-item>
              <el-dropdown placement="right-start">
                <span>
                  <span class="icon iconfont icon-icon_moments-categories_outlined icon16" />
                  <span class="text14 margin-left8">{{ $t('panel.new_element_distribution') }}</span>
                  <svg-icon
                    icon-class="icon_right_outlined"
                    class="icon16 margin-left8"
                  />
                </span>
                <el-dropdown-menu slot="dropdown">
                  <el-dropdown-item @click.native="auxiliaryMatrixChange(false)">
                    <span class="text14"> {{ $t('panel.suspension') }} </span>
                    <i
                      v-if="!canvasStyleData.auxiliaryMatrix"
                      class=" font-active el-icon-check margin-left52"
                    />
                  </el-dropdown-item>
                  <el-dropdown-item @click.native="auxiliaryMatrixChange(true)">
                    <span class="text14"> {{ $t('panel.matrix') }} </span>
                    <i
                      v-if="canvasStyleData.auxiliaryMatrix"
                      class=" font-active el-icon-check margin-left52"
                    />
                  </el-dropdown-item>
                </el-dropdown-menu>
              </el-dropdown>
            </el-dropdown-item>
            <el-dropdown-item>
              <span class="icon iconfont icon-icon_dialpad_outlined icon16" />
              <span class="text14 margin-left8">{{ $t('panel.aided_grid') }}</span>
              <el-switch
                v-model="showGridSwitch"
                :class="[{['grid-active']: showGridSwitch},'margin-left8']"
                size="mini"
                @change="showGridChange"
              />
            </el-dropdown-item>
            <el-dropdown-item @click.native="openOuterParamsSet">
              <span class="icon iconfont icon-icon-quicksetting icon16" />
              <span class="text14 margin-left8">{{ $t('panel.params_setting') }}</span>
            </el-dropdown-item>
            <el-dropdown-item @click.native="clearCanvas">
              <span class="icon iconfont icon-icon_clear_outlined icon16" />
              <span class="text14 margin-left8">{{ $t('panel.clean_canvas') }}</span>
            </el-dropdown-item>
            <el-dropdown-item
              v-if="showWatermarkSetting"
            >
              <span class="icon iconfont icon-WATERMARK icon16" />
              <span class="text14 margin-left8">{{ $t('panel.watermark') }}</span>
              <el-switch
                v-model="panelInfo.watermarkOpen"
                :class="[{['grid-active']: panelInfo.watermarkOpen},'margin-left8']"
                size="mini"
                @change="styleChange"
              />
            </el-dropdown-item>

            <el-dropdown-item v-if="showPdfPageButton">
              <span>
                <svg-icon
                  style="width: 16px; height: 16px;"
                  icon-class="page-line"
                />
              </span>
              <el-tooltip
                class="item"
                :content="$t('panel.export_pdf_page_remark')"
                placement="top-start"
              >

                <span class="text14 margin-left8">{{ $t('panel.export_pdf_page') }}</span>

              </el-tooltip>
              <el-switch
                v-model="showPageLine"
                :class="[{['grid-active']: showPageLine},'margin-left8']"
                size="mini"
                @change="showPageLineChange"
              />
            </el-dropdown-item>
          </el-dropdown-menu>
        </el-dropdown>
      </span>
      <span
        class="icon iconfont icon-icon_effects_outlined insert margin-right20"
        @click="showPanel"
      >
        <span class="icon-font-margin">{{ $t('panel.panel_style') }}</span>
      </span>
      <span
        class="icon iconfont icon-icon_Batch_outlined insert margin-right20"
        @click="batchOption"
      ><span
        class="icon-font-margin"
      >{{ batchOptStatus?$t('panel.cancel_batch_opt'):$t('panel.batch_opt') }}</span></span>
      <span style="float: right;margin-right: 24px">
        <el-button
          size="mini"
          type="primary"
          :disabled="saveButtonDisabled"
          @click="save(false)"
        >
          {{ $t('commons.save') }}
        </el-button>
      </span>
    </div>

    <!--关闭弹框-->
    <el-dialog
      :visible.sync="closePanelVisible"
      :title="$t('panel.panel_save_tips')"
      :show-close="false"
      width="30%"
      class="dialog-css"
    >
      <el-row style="height: 20px">
        <el-col :span="4">
          <svg-icon
            icon-class="warn-tree"
            style="width: 20px;height: 20px;float: right"
          />
        </el-col>
        <el-col :span="20">
          <span style="font-size: 13px;margin-left: 10px;font-weight: bold;line-height: 20px">{{
            $t('panel.panel_save_warn_tips')
          }}</span>
        </el-col>
      </el-row>
      <div
        slot="footer"
        class="dialog-footer"
      >
        <el-button
          style="float: left"
          size="mini"
          @click="closeNotSave()"
        >{{ $t('panel.do_not_save') }}
        </el-button>
        <el-button
          size="mini"
          @click="closePanelVisible=false"
        >{{ $t('panel.cancel') }}
        </el-button>
        <el-button
          type="primary"
          size="mini"
          @click="save(true)"
        >{{ $t('panel.save') }}
        </el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import generateID from '@/components/canvas/utils/generateID'
import toast from '@/components/canvas/utils/toast'
import { mapState } from 'vuex'
import { commonAttr, commonStyle } from '@/components/canvas/customComponent/component-list'
import eventBus from '@/components/canvas/utils/eventBus'
import { deepCopy, mobile2MainCanvas } from '@/components/canvas/utils/utils'
import { panelUpdate, removePanelCache, saveCache } from '@/api/panel/panel'
import { getPanelAllLinkageInfo, saveLinkage } from '@/api/panel/linkage'
import bus from '@/utils/bus'
import { queryPanelJumpInfo } from '@/api/panel/linkJump'
import { inOtherPlatform } from '@/utils/index'
export default {
  name: 'Toolbar',
  props: {
    styleButtonActive: Boolean,
    aidedButtonActive: Boolean
  },
  data() {
    return {
      showPageLine: false,
      showGridSwitch: false,
      mobileLayoutInitStatus: false,
      isShowPreview: false,
      needToChange: [
        'top',
        'left',
        'width',
        'height',
        'fontSize',
        'borderWidth'
      ],
      scale: '100%',
      timer: null,
      changes: 0,
      closePanelVisible: false,
      showPdfPageButton: false
    }
  },
  computed: {
    showWatermarkSetting() {
      return this.panelInfo.watermarkInfo && this.panelInfo.watermarkInfo.settingContent.enable && this.panelInfo.watermarkInfo.settingContent.enablePanelCustom
    },
    panelInfo() {
      return this.$store.state.panel.panelInfo
    },
    saveButtonDisabled() {
      return this.changeTimes === 0 || this.snapshotIndex === this.lastSaveSnapshotIndex
    },
    editControlButton() {
      return this.linkageSettingStatus || this.mobileLayoutStatus
    },
    isOtherPlatform() {
      return inOtherPlatform()
    },
    ...mapState([
      'componentData',
      'canvasStyleData',
      'areaData',
      'curComponent',
      'changeTimes',
      'snapshotIndex',
      'lastSaveSnapshotIndex',
      'linkageSettingStatus',
      'curLinkageView',
      'targetLinkageInfo',
      'mobileLayoutStatus',
      'mobileComponentData',
      'componentDataCache',
      'batchOptStatus'
    ])
  },
  created() {
    eventBus.$on('editPanelInitReady', this.editPanelInit)
    eventBus.$on('preview', this.preview)
    eventBus.$on('checkAndSave', this.checkAndSave)
    eventBus.$on('clearCanvas', this.clearCanvas)
    this.scale = this.canvasStyleData.scale
    this.mobileLayoutInitStatus = this.mobileLayoutStatus
    this.showGridSwitch = this.canvasStyleData.aidedDesign.showGrid
    this.showPageLine = this.canvasStyleData.pdfPageLine?.showPageLine
    this.autoCache()
  },
  beforeDestroy() {
    eventBus.$off('preview', this.preview)
    eventBus.$off('checkAndSave', this.checkAndSave)
    eventBus.$off('clearCanvas', this.clearCanvas)
    eventBus.$off('editPanelInitReady', this.editPanelInit)
    clearInterval(this.timer)
    this.timer = null
  },
  methods: {
    checkAndSave() {
      if (!this.saveButtonDisabled) {
        this.save(false)
      }
    },
    editPanelInit() {
      this.showGridSwitch = this.canvasStyleData.aidedDesign.showGrid
      this.showPageLine = this.canvasStyleData.pdfPageLine?.showPageLine
    },
    close() {
      // 关闭页面清理缓存
      this.$store.commit('initCanvasBase')
      this.$store.commit('setInEditorStatus', false)
      this.$emit('close-left-panel')
      removePanelCache(this.panelInfo.id)
      this.$nextTick(() => {
        bus.$emit('PanelSwitchComponent', { name: 'PanelMain' })
      })
    },
    closePanelEdit() {
      if (this.changeTimes === 0 || this.snapshotIndex === this.lastSaveSnapshotIndex) { // 已保存
        this.close()
      } else {
        this.closePanelVisible = true
      }
    },
    goFile() {
      this.$refs.files.click()
    },
    format(value) {
      const scale = this.scale
      return value * scale / 100
    },
    getOriginStyle(value) {
      const scale = this.canvasStyleData.scale
      const result = value / (scale / 100)
      return result
    },
    handleScaleChange() {
      setTimeout(() => {
        const componentData = deepCopy(this.componentData)
        componentData.forEach(component => {
          Object.keys(component.style).forEach(key => {
            if (this.needToChange.includes(key)) {
              // 根据原来的比例获取样式原来的尺寸
              // 再用原来的尺寸 * 现在的比例得出新的尺寸
              component.style[key] = this.format(this.getOriginStyle(component.style[key]))
            }
          })
        })
        this.$store.commit('setComponentData', componentData)
        this.$store.commit('setCanvasStyle', {
          ...this.canvasStyleData,
          scale: this.scale
        })
      }, 1000)
    },

    lock() {
      this.$store.commit('lock')
    },

    unlock() {
      this.$store.commit('unlock')
    },

    compose() {
      this.$store.commit('compose')
      this.$store.commit('recordSnapshot', 'compose')
    },

    decompose() {
      this.$store.commit('decompose')
      this.$store.commit('recordSnapshot', 'decompose')
    },

    undo() {
      this.$store.commit('undo')
    },

    redo() {
      this.$store.commit('redo')
    },

    showPanel() {
      this.$emit('showPanel', 2)
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
                top: 0,
                left: 0,
                width: img.width / scale,
                height: img.height / scale
              }
            }
          })
          this.$store.commit('recordSnapshot', 'handleFileChange')
        }
        img.src = fileResult
      }

      reader.readAsDataURL(file)
    },

    preview() {
      this.isShowPreview = true
      this.$store.commit('setEditMode', 'preview')
    },
    autoCache() {
      // auto save panel cache per 5s
      const _this = this
      _this.timer = setInterval(() => {
        if (_this.$store.state.cacheStyleChangeTimes > 0) {
          const requestInfo = _this.savePrepare()
          const cacheRequest = {
            ...deepCopy(this.panelInfo),
            ...requestInfo
          }
          cacheRequest.watermarkInfo.settingContent = JSON.stringify(this.panelInfo.watermarkInfo.settingContent)
          saveCache(cacheRequest)
          _this.$store.state.cacheStyleChangeTimes = 0
        }
      }, 5000)
    },
    savePrepare() {
      // 保存到数据库
      const requestInfo = {
        id: this.panelInfo.id,
        watermarkOpen: this.panelInfo.watermarkOpen,
        panelStyle: JSON.stringify(this.canvasStyleData),
        panelData: JSON.stringify(this.componentData)
      }
      const components = deepCopy(this.componentData)
      components.forEach(view => {
        // 清理联动信息
        if (view.linkageFilters && view.linkageFilters.length > 0) {
          view.linkageFilters.splice(0, view.linkageFilters.length)
        }
        if (view.DetailAreaCode) {
          view.DetailAreaCode = null
        }
        if (view.filters && view.filters.length > 0) {
          view.filters = []
        }
        if (view.type === 'de-tabs') {
          view.options.tabList && view.options.tabList.length > 0 && view.options.tabList.forEach(tab => {
            if (tab.content && tab.content.filters && tab.content.filters.length > 0) {
              tab.content.filters = []
            }
          })
        }
      })
      // 无需保存条件
      requestInfo.panelData = JSON.stringify(components)
      return requestInfo
    },

    save(withClose) {
      const requestInfo = this.savePrepare()
      panelUpdate(requestInfo).then(response => {
        this.$store.commit('refreshSaveStatus')
        this.$message({
          message: this.$t('commons.save_success'),
          type: 'success',
          showClose: true
        })
        if (withClose) {
          this.close()
        }
      })
    },
    clearCanvas() {
      this.$store.commit('setComponentData', [])
      this.$store.commit('recordSnapshot', 'clearCanvas')
    },

    handlePreviewChange() {
      this.$store.commit('setEditMode', 'edit')
    },

    clickPreview() {
      this.$emit('previewFullScreen')
    },
    openOuterParamsSet() {
      this.$emit('outerParamsSetVisibleChange', true)
    },
    changeAidedDesign() {
      this.$emit('changeAidedDesign')
    },
    closeNotSave() {
      this.close()
    },
    saveLinkage() {
      // 字段检查
      for (const key in this.targetLinkageInfo) {
        let subCheckCount = 0
        const linkageInfo = this.targetLinkageInfo[key]
        const linkageFields = linkageInfo['linkageFields']
        if (linkageFields) {
          linkageFields.forEach(function(linkage) {
            if (!(linkage.sourceField && linkage.targetField)) {
              subCheckCount++
            }
          })
        }

        if (subCheckCount > 0) {
          this.$message({
            message: this.$t('chart.datalist') + '【' + linkageInfo.targetViewName + '】' + this.$t('panel.exit_un_march_linkage_field'),
            type: 'error',
            showClose: true
          })
          return
        }
      }
      const request = {
        panelId: this.panelInfo.id,
        sourceViewId: this.curLinkageView.propValue.viewId,
        linkageInfo: this.targetLinkageInfo
      }
      saveLinkage(request).then(rsp => {
        // 刷新联动信息
        getPanelAllLinkageInfo(this.panelInfo.id).then(rsp => {
          this.$store.commit('setNowPanelTrackInfo', rsp.data)
        })
        this.cancelLinkageSettingStatus()
        // 刷新跳转信息
        queryPanelJumpInfo(this.panelInfo.id).then(rsp => {
          this.$store.commit('setNowPanelJumpInfo', rsp.data)
        })
      })
    },
    cancelMobileLayoutStatue(sourceComponentData) {
      this.$store.commit('setComponentData', sourceComponentData)
      this.$store.commit('setMobileLayoutStatus', false)
      this.mobileLayoutInitStatus = false
    },
    cancelLinkage() {
      this.cancelLinkageSettingStatus()
    },
    cancelLinkageSettingStatus() {
      this.$store.commit('clearLinkageSettingInfo')
    },
    auxiliaryMatrixChange(value) {
      this.canvasStyleData.auxiliaryMatrix = value
    },
    showGridChange() {
      this.$store.commit('canvasChange')
      this.canvasStyleData.aidedDesign.showGrid = !this.canvasStyleData.aidedDesign.showGrid
    },
    showPageLineChange() {
      this.$store.commit('canvasChange')
      this.canvasStyleData.pdfPageLine.showPageLine = !this.canvasStyleData.pdfPageLine.showPageLine
    },
    // batch option
    batchOption() {
      bus.$emit('change_panel_right_draw', !this.batchOptStatus)
      this.$store.commit('setBatchOptStatus', !this.batchOptStatus)
    },
    // 启用移动端布局
    openMobileLayout(switchVal) {
      if (switchVal) {
        this.$store.commit('openMobileLayout')
      } else {
        this.mobileLayoutSave()
      }
    },
    editSave() {
      if (this.mobileLayoutStatus) {
        this.mobileLayoutSave()
      } else {
        this.saveLinkage()
      }
    },
    editReset() {
      this.$store.commit('setComponentData', JSON.parse(this.componentDataCache))
      this.$store.commit('setMobileLayoutStatus', false)
      this.$store.commit('openMobileLayout')
    },
    editCancel() {
      if (this.mobileLayoutStatus) {
        this.cancelMobileLayoutStatue(JSON.parse(this.componentDataCache))
      } else {
        this.cancelLinkageSettingStatus()
      }
    },
    // 移动端布局保存
    mobileLayoutSave() {
      this.$store.commit('canvasChange')
      const mobileDataObj = {}
      this.componentData.forEach(item => {
        mobileDataObj[item.id] = item
      })
      const sourceComponentData = JSON.parse(this.componentDataCache)
      this.$store.commit('setComponentDataCache', null)
      sourceComponentData.forEach(item => {
        if (mobileDataObj[item.id]) {
          mobile2MainCanvas(item, mobileDataObj[item.id])
        } else {
          item.mobileSelected = false
        }
      })
      this.cancelMobileLayoutStatue(sourceComponentData)
    },
    styleChange() {
      this.$store.commit('canvasChange')
    }
  }
}
</script>

<style lang="scss" scoped>
.toolbar {
  float: right;
  height: 56px;
  line-height: 56px;
  min-width: 400px;

  .canvas-config {
    display: inline-block;
    margin-left: 10px;
    font-size: 14px;

    input {
      width: 50px;
      margin-left: 10px;
      outline: none;
      padding: 0 5px;
      border: 1px solid #ddd;
    }

    span {
      margin-left: 10px;
    }
  }

  .insert {
    display: inline-block;
    font-weight: 400 !important;
    font-size: 16px;
    font-family: PingFang SC;
    line-height: 1;
    white-space: nowrap;
    cursor: pointer;
    color: var(--TextPrimary, #1F2329);
    -webkit-appearance: none;
    text-align: center;
    box-sizing: border-box;
    outline: 0;
    margin: 0;
    transition: .1s;
    padding: 2px 4px;
    border-radius: 3px;

    &:active {
      color: #000;
      border-color: #3a8ee6;
      background-color: red;
      outline: 0;
    }

    &:hover {
      background-color: rgba(31, 35, 41, 0.1);
      color: #3a8ee6;
    }
  }
}

.button-show {
  background-color: #ebf2fe !important;
}

.button-closed {
  background-color: #ffffff !important;
}

::v-deep .el-switch__core {
  width: 30px !important;
  height: 15px;
}

/*设置圆*/
::v-deep .el-switch__core::after {
  width: 14px;
  height: 14px;
  margin-top: -1.3px;
  margin-bottom: 2px;
}

.grid-active ::v-deep .el-switch__core::after {
  margin-left: -14.5px;
}

.iconfont-tb {
  font-family: "iconfont" !important;
  font-size: 12px;
  font-style: normal;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
}

.switch-position {
  position: absolute;
  top: 13px;
  left: 48%;
  width: 100px;
}

.button_self {
  margin-right: 5px;
}

.button_self ::v-deep .el-button--mini {
  padding: 7px 7px !important;
}

.font-active {
  font-color: #3a8ee6 !important;
}

.icon-active {
  color: #3a8ee6;
}

.icon-unactivated {
  display: none;
}

.panel-info-area {
  position: absolute;
  line-height: 56px;
  left: 0px;
}

.icon-font-margin {
  margin-left: 4px;
  font-size: 14px !important;
}

.margin-left8 {
  margin-left: 8px;
}

.toolbar-icon-active {
  cursor: pointer;
  transition: .1s;
  border-radius: 3px;

  &:active {
    color: #000;
    border-color: #3a8ee6;
    background-color: red;
    outline: 0;
  }

  &:hover {
    background-color: rgba(31, 35, 41, 0.1);
    color: #3a8ee6;
  }
}

.toolbar-icon-middle {
  font-size: 16px;
  color: var(--TextPrimary, #1F2329);
  cursor: pointer;
  transition: .1s;
  border-radius: 3px;

  &:active {
    color: #000;
    border-color: #3a8ee6;
    background-color: red;
    outline: 0;
  }

  &:hover {
    background-color: rgba(31, 35, 41, 0.1);
    color: #3a8ee6;
  }
}

.toolbar-icon {
  font-size: 20px;
  color: var(--TextPrimary, #1F2329);
  cursor: pointer;
  transition: .1s;
  border-radius: 3px;

  &:active {
    color: #000;
    border-color: #3a8ee6;
    background-color: red;
    outline: 0;
  }

  &:hover {
    background-color: rgba(31, 35, 41, 0.1);
    color: #3a8ee6 !important;
  }
}

.margin-left20 {
  margin-left: 20px !important;
}

.margin-right20 {
  margin-right: 20px !important;
}

.margin-right12 {
  margin-right: 12px !important;
}

.icon20 {
  font-size: 20px;
  color: var(--TextPrimary, #1F2329);
}

.icon16_only {
  font-size: 16px !important;
}

.icon16 {
  font-size: 16px !important;
  color: var(--TextPrimary, #1F2329);
}

.text16 {
  font-size: 16px;
  font-weight: 500;
  line-height: 24px;
  color: var(--TextPrimary, #1F2329);
}

.text14 {
  font-size: 14px;
  font-weight: 400;
  line-height: 22px;
  color: var(--TextPrimary, #1F2329);
}

.margin-left52 {
  margin-left: 52px !important;
}

.margin-left12 {
  margin-left: 12px !important;
}

.el-divider--vertical {
  margin: 0 20px 0 20px
}

.el-dropdown-menu__item {
  line-height: 32px;
}

::v-deep .el-radio-button__inner {
  padding: 7px 7px
}
</style>
