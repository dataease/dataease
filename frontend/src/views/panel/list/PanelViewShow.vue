<template>
  <el-row
    v-loading="dataLoading"
    style="height: 100%;width: 100%;"
    :element-loading-text="$t('panel.data_loading')"
    element-loading-spinner="el-icon-loading"
    element-loading-background="rgba(220,220,220,1)"
  >
    <el-col v-if="panelInfo.name.length>0" class="panel-design">

      <el-row v-if="showType === 2" class="panel-design-head panel-share-head" style="border-bottom: 1px solid;border-bottom-color:#E6E6E6;">
        <div style="height: 100%;">
          <share-head />
        </div>
      </el-row>
      <el-row v-else class="panel-design-head" style="border-bottom: 1px solid;border-bottom-color:#E6E6E6;">
        <!--仪表板头部区域-->
        <div style="height: 100%;">
          <el-col :span="12" style="text-overflow:ellipsis;overflow: hidden;white-space: nowrap;font-size: 14px">
            <span>{{ panelInfo.name || '测试仪表板' }}</span>
            <span v-if="panelInfo.isDefault" style="color: green;font-size: 12px">({{ $t('panel.default_panel_name') }}:{{ panelInfo.defaultPanelName }})</span>
            <span v-if="panelInfo.sourcePanelName" style="color: green;font-size: 12px">&nbsp;({{ $t('panel.source_panel_name') }}:{{ panelInfo.sourcePanelName }})</span>
            <el-popover
              placement="right-start"
              width="400"
              trigger="click"
            >
              <panel-detail-info />
              <i
                slot="reference"
                class="el-icon-warning icon-class"
                style="margin-left: 4px;cursor: pointer;font-size: 14px;"
              />
            </el-popover>
          </el-col>
          <el-col :span="12">

            <span v-if="hasDataPermission('manage',panelInfo.privileges)&&activeTab==='PanelList'&&!panelInfo.sourcePanelName" style="float: right;margin-right: 10px">
              <el-button size="mini" type="primary" @click="editPanel">
                {{ $t('commons.edit') }}
              </el-button>
            </span>

            <span v-if="hasDataPermission('manage',panelInfo.privileges)&&activeTab==='PanelList'&&!panelInfo.sourcePanelName" style="float: right;margin-right: 10px">
              <el-button size="mini" type="primary" @click="changePublishState">
                <span v-if="panelInfo.status==='publish'">{{ $t('commons.unpublished') }}</span>
                <span v-if="panelInfo.status!=='publish'">{{ $t('commons.publish') }}</span>
              </el-button>
            </span>

            <span v-if="hasDataPermission('export',panelInfo.privileges)&&panelInfo.status==='publish'" style="float: right;margin-right: 10px">
              <el-tooltip :content="$t('panel.save_to_panel')">
                <el-button class="el-icon-folder-checked" size="mini" circle @click="saveToTemplate" />
              </el-tooltip>
            </span>
            <span v-if="hasDataPermission('export',panelInfo.privileges)&&panelInfo.status==='publish'" style="float: right;margin-right: 10px">
              <el-dropdown>
                <el-button size="mini" class="el-icon-download" circle />
                <el-dropdown-menu slot="dropdown">
                  <el-dropdown-item icon="el-icon-copy-document" @click.native="downloadToTemplate">{{ $t('panel.export_to_panel') }}</el-dropdown-item>
                  <el-dropdown-item icon="el-icon-notebook-2" @click.native="downloadAsPDF">{{ $t('panel.export_to_pdf') }}</el-dropdown-item>
                  <el-dropdown-item icon="el-icon-picture-outline" @click.native="downloadAsImage">{{ $t('panel.export_to_img') }}</el-dropdown-item>
                </el-dropdown-menu>
              </el-dropdown>
            </span>
            <span v-if="panelInfo.status==='publish'" style="float: right;margin-right: 10px">
              <el-tooltip :content="$t('panel.fullscreen_preview')">
                <el-button class="el-icon-view" size="mini" circle @click="clickFullscreen" />
              </el-tooltip>
            </span>

            <span v-if="panelInfo.status==='publish'" style="float: right;margin-right: 10px">
              <el-tooltip :content="$t('panel.new_tab_preview')">
                <el-button class="el-icon-data-analysis" size="mini" circle @click="newTab" />
              </el-tooltip>
            </span>

            <span v-if="!hasStar && panelInfo && showType !== 1&&panelInfo.status==='publish'" style="float: right;margin-right: 10px">
              <el-tooltip :content="$t('panel.store')">
                <el-button class="el-icon-star-off" size="mini" circle @click="star" />
              </el-tooltip>
            </span>

            <span v-if="hasStar && panelInfo && showType !== 1&&panelInfo.status==='publish'" style="float: right;margin-right: 10px">
              <el-tooltip :content="$t('commons.cancel')">
                <el-button class="el-icon-star-on" size="mini" circle @click="unstar" />
              </el-tooltip>
            </span>

            <span style="float: right;margin-right: 10px">
              <el-tooltip :content="$t('commons.refresh')">
                <el-button class="el-icon-refresh" size="mini" circle @click="refreshPanel" />
              </el-tooltip>
            </span>

          </el-col>
        </div>
      </el-row>
      <!-- 仪表板预览区域-->
      <el-row class="panel-design-preview">
        <!--        <div id="imageWrapper" ref="imageWrapper" style="width: 4096px;height: 2160px">-->
        <div id="imageWrapper" ref="imageWrapper" :style="imageWrapperStyle">
          <fullscreen style="height: 100%;background: #f7f8fa;overflow-y: auto" :fullscreen.sync="fullscreen">
            <Preview
              v-if="showMainFlag"
              :component-data="componentData"
              :canvas-style-data="canvasStyleData"
              :active-tab="activeTab"
              :in-screen="!fullscreen"
              :show-type="'width'"
              :panel-info="panelInfo"
              :screen-shot="dataLoading"
            />
          </fullscreen>
        </div>
      </el-row>
    </el-col>
    <el-col v-if="panelInfo.name.length===0" style="height: 100%;">
      <el-row style="height: 100%; background-color: var(--ContentBG);" class="custom-position">
        {{ $t('panel.select_panel_from_left') }}
      </el-row>
    </el-col>

    <el-dialog
      v-if="templateSaveShow"
      :title="templateSaveTitle"
      :visible.sync="templateSaveShow"
      width="500px"
    >
      <save-to-template :template-info="templateInfo" @closeSaveDialog="closeSaveDialog" />
    </el-dialog>
    <el-dialog
      v-if="pdfExportShow"
      :title="'['+panelInfo.name+']'+'PDF导出'"
      :visible.sync="pdfExportShow"
      width="80%"
      :top="'8vh'"
      :destroy-on-close="true"
      class="dialog-css2"
    >
      <span style="position: absolute;right: 70px;top:15px">
        <svg-icon icon-class="PDF" class="ds-icon-pdf" />
        <el-select v-model="pdfTemplateSelectedIndex" :placeholder="'切换PDF模板'" @change="changePdfTemplate()">
          <el-option
            v-for="(item, index) in pdfTemplateAll"
            :key="index"
            :label="item.name"
            :value="index"
          />
        </el-select>
      </span>
      <PDFPreExport :snapshot="snapshotInfo" :panel-name="panelInfo.name" :template-content="pdfTemplateContent" @closePreExport="closePreExport" />
    </el-dialog>
  </el-row>
</template>
<script>
import PDFPreExport from '@/views/panel/export/PDFPreExport'
import Preview from '@/components/canvas/components/Editor/Preview'
import SaveToTemplate from '@/views/panel/list/SaveToTemplate'
import { mapState } from 'vuex'
import html2canvas from 'html2canvasde'
import FileSaver from 'file-saver'
import { starStatus, saveEnshrine, deleteEnshrine } from '@/api/panel/enshrine'
import bus from '@/utils/bus'
import { queryAll } from '@/api/panel/pdfTemplate'
import ShareHead from '@/views/panel/GrantAuth/ShareHead'
import { export2AppCheck, initPanelData, updatePanelStatus } from '@/api/panel/panel'
import { proxyInitPanelData } from '@/api/panel/shareProxy'
import { dataURLToBlob } from '@/components/canvas/utils/utils'
import { findResourceAsBase64 } from '@/api/staticResource/staticResource'
import PanelDetailInfo from '@/views/panel/list/common/PanelDetailInfo'

export default {
  name: 'PanelViewShow',
  components: { PanelDetailInfo, Preview, SaveToTemplate, PDFPreExport, ShareHead },
  props: {
    activeTab: {
      type: String,
      required: false
    }
  },
  data() {
    return {
      showMain: true,
      pdfTemplateSelectedIndex: 0,
      pdfTemplateContent: '',
      templateInfo: {},
      pdfTemplateAll: [],
      templateSaveTitle: '保存为模板',
      templateSaveShow: false,
      hasStar: false,
      fullscreen: false,
      pdfExportShow: false,
      snapshotInfo: '',
      showType: 0,
      dataLoading: false,
      exporting: false,
      shareUserId: null
    }
  },
  computed: {
    imageWrapperStyle() {
      if (this.exporting) {
        return {
          width: '2560px',
          height: '1440px'
        }
      } else {
        return {
          width: '100%',
          height: '100%'
        }
      }
    },
    showMainFlag() {
      return this.showMain
    },
    panelInfo() {
      return this.$store.state.panel.panelInfo
    },
    ...mapState([
      'componentData',
      'canvasStyleData',
      'panelViewDetailsInfo'
    ])
  },
  watch: {
    panelInfo(newVal, oldVla) {
      // 刷新 进行重新渲染
      this.showMain = false

      this.$nextTick(() => {
        this.showMain = true
        this.initHasStar()
      })
    },
    fullscreen(newVal, oldVla) {
      // 刷新 进行重新渲染
      this.showMain = false
      this.$nextTick(() => {
        this.showMain = true
        this.initHasStar()
      })
    }
  },
  mounted() {
    bus.$on('set-panel-show-type', this.setPanelShowType)
    bus.$on('set-panel-share-user', this.setPanelShareUser)
    this.initPdfTemplate()
  },
  beforeDestroy() {
    bus.$off('set-panel-show-type', this.setPanelShowType)
    bus.$off('set-panel-share-user', this.setPanelShareUser)
  },
  methods: {
    setPanelShowType(type) {
      this.showType = type || 0
    },
    setPanelShareUser(userId) {
      this.shareUserId = userId
    },
    initPdfTemplate() {
      queryAll().then(res => {
        this.pdfTemplateAll = res.data
        this.changePdfTemplate()
      })
    },
    clickFullscreen() {
      this.fullscreen = true
    },
    newTab() {
      let url = '#/preview/' + this.$store.state.panel.panelInfo.id
      if (this.showType === 1 && this.shareUserId !== null) {
        url += ('|' + this.shareUserId)
      }
      window.open(url, '_blank')
    },
    saveToTemplate() {
      this.dataLoading = true
      setTimeout(() => {
        html2canvas(document.getElementById('canvasInfoTemp')).then(canvas => {
          this.templateSaveShow = true
          this.dataLoading = false
          const snapshot = canvas.toDataURL('image/jpeg', 0.1) // 0.2是图片质量
          if (snapshot !== '') {
            this.templateInfo = {
              name: this.$store.state.panel.panelInfo.name,
              snapshot: snapshot,
              templateStyle: JSON.stringify(this.canvasStyleData),
              templateData: JSON.stringify(this.componentData),
              templateType: 'self',
              nodeType: 'template',
              level: 1,
              pid: null,
              dynamicData: JSON.stringify(this.panelViewDetailsInfo)
            }
          }
        })
      }, 50)
    },
    downloadToTemplate() {
      const _this = this
      _this.dataLoading = true
      try {
        _this.findStaticSource(function(staticResource) {
          html2canvas(document.getElementById('canvasInfoTemp')).then(canvas => {
            _this.dataLoading = false
            const snapshot = canvas.toDataURL('image/jpeg', 0.1) // 0.1是图片质量
            if (snapshot !== '') {
              _this.templateInfo = {
                name: _this.$store.state.panel.panelInfo.name,
                templateType: 'self',
                snapshot: snapshot,
                panelStyle: JSON.stringify(_this.canvasStyleData),
                panelData: JSON.stringify(_this.componentData),
                dynamicData: JSON.stringify(_this.panelViewDetailsInfo),
                staticResource: JSON.stringify(staticResource || {})
              }
              const blob = new Blob([JSON.stringify(_this.templateInfo)], { type: '' })
              FileSaver.saveAs(blob, _this.$store.state.panel.panelInfo.name + '-TEMPLATE.DET')
            }
          })
        })
      } catch (e) {
        console.error(e)
        _this.dataLoading = false
      }
    },
    saveAppFile(appAttachInfo) {
      const _this = this
      _this.dataLoading = true
      try {
        _this.findStaticSource(function(staticResource) {
          html2canvas(document.getElementById('canvasInfoTemp')).then(canvas => {
            _this.dataLoading = false
            const snapshot = canvas.toDataURL('image/jpeg', 0.1) // 0.1是图片质量
            if (snapshot !== '') {
              const panelInfo = {
                name: _this.$store.state.panel.panelInfo.name,
                id: _this.$store.state.panel.panelInfo.id,
                snapshot: snapshot,
                panelStyle: JSON.stringify(_this.canvasStyleData),
                panelData: JSON.stringify(_this.componentData),
                staticResource: JSON.stringify(staticResource || {})
              }
              appAttachInfo['panelInfo'] = JSON.stringify(panelInfo)
              const blob = new Blob([JSON.stringify(appAttachInfo)], { type: '' })
              FileSaver.saveAs(blob, _this.$store.state.panel.panelInfo.name + '-APP.DEAPP')
            }
          })
        })
      } catch (e) {
        console.error(e)
        _this.dataLoading = false
      }
    },
    downLoadToApp() {
      this.dataLoading = true
      export2AppCheck(this.$store.state.panel.panelInfo.id).then(rsp => {
        if (rsp.data.checkStatus) {
          this.saveAppFile(rsp.data)
        } else {
          this.dataLoading = false
          this.$message({
            message: rsp.data.checkMes,
            type: 'error'
          })
        }
      })
    },
    // 解析静态文件
    findStaticSource(callBack) {
      const staticResource = []
      // 系统背景文件
      if (typeof this.canvasStyleData.panel.imageUrl === 'string' && this.canvasStyleData.panel.imageUrl.indexOf('static-resource') > -1) {
        staticResource.push(this.canvasStyleData.panel.imageUrl)
      }
      this.componentData.forEach(item => {
        if (typeof item.commonBackground.outerImage === 'string' && item.commonBackground.outerImage.indexOf('static-resource') > -1) {
          staticResource.push(item.commonBackground.outerImage)
        }
      })
      if (staticResource.length > 0) {
        try {
          findResourceAsBase64({ resourcePathList: staticResource }).then((rsp) => {
            callBack(rsp.data)
          })
        } catch (e) {
          console.error('findResourceAsBase64 error', e)
          callBack()
        }
      } else {
        setTimeout(() => {
          callBack()
        }, 0)
      }
    },

    downloadAsImage() {
      this.dataLoading = true
      setTimeout(() => {
        this.exporting = true
        setTimeout(() => {
          const canvasID = document.getElementById('canvasInfoTemp')
          const a = document.createElement('a')
          html2canvas(canvasID).then(canvas => {
            this.exporting = false
            const dom = document.body.appendChild(canvas)
            dom.style.display = 'none'
            a.style.display = 'none'
            document.body.removeChild(dom)
            const blob = dataURLToBlob(dom.toDataURL('image/png', 1))
            a.setAttribute('href', URL.createObjectURL(blob))
            a.setAttribute('download', this.$store.state.panel.panelInfo.name + '.png')
            document.body.appendChild(a)
            a.click()
            URL.revokeObjectURL(blob)
            document.body.removeChild(a)
            setTimeout(() => {
              this.dataLoading = false
            }, 300)
          })
        }, 1500)
      }, 500)
    },

    downloadAsPDF() {
      // this.pdfExportShow = true
      //
      this.dataLoading = true

      setTimeout(() => {
        this.exporting = true
        setTimeout(() => {
          html2canvas(document.getElementById('canvasInfoTemp')).then(canvas => {
            const snapshot = canvas.toDataURL('image/jpeg', 1) // 是图片质量
            this.dataLoading = false
            this.exporting = false
            if (snapshot !== '') {
              this.snapshotInfo = snapshot
              this.pdfExportShow = true
            }
          })
        }, 1500)
      }, 500)
    },
    refreshTemplateInfo() {
      this.templateInfo = {}
      html2canvas(document.getElementById('canvasInfoTemp')).then(canvas => {
        const snapshot = canvas.toDataURL('image/jpeg', 0.1) // 0.2是图片质量
        if (snapshot !== '') {
          this.templateInfo = {
            snapshot: snapshot,
            panelStyle: JSON.stringify(this.canvasStyleData),
            panelData: JSON.stringify(this.componentData),
            dynamicData: ''
          }
        }
      })
    },
    closeSaveDialog() {
      this.templateSaveShow = false
    },
    star() {
      this.panelInfo && saveEnshrine(this.panelInfo.id).then(res => {
        this.hasStar = true
        this.refreshStarList(true)
      })
    },
    unstar() {
      this.panelInfo && deleteEnshrine(this.panelInfo.id).then(res => {
        this.hasStar = false
        this.refreshStarList(false)
      })
    },
    initHasStar() {
      this.panelInfo && this.panelInfo.id && starStatus(this.panelInfo.id).then(res => {
        this.hasStar = res.data
      })
    },
    refreshStarList(isStar) {
      if (this.activeTab !== 'PanelList') {
        bus.$emit('panle_start_list_refresh', isStar)
      }
    },
    changePdfTemplate() {
      this.pdfTemplateContent = this.pdfTemplateAll[this.pdfTemplateSelectedIndex] ? this.pdfTemplateAll[this.pdfTemplateSelectedIndex].templateContent : ''
    },
    closePreExport() {
      this.pdfExportShow = false
    },
    editPanel() {
      this.$emit('editPanel')
    },
    refreshPanel() {
      if (this.showType === 1 && this.shareUserId !== null) {
        const param = { userId: this.shareUserId }
        proxyInitPanelData(this.panelInfo.id, param, null)
      } else { initPanelData(this.panelInfo.id, false) }
    },
    changePublishState() {
      if (this.panelInfo.status === 'publish') {
        this.panelInfo.status = 'unpublished'
      } else {
        this.panelInfo.status = 'publish'
      }
      updatePanelStatus(this.panelInfo.id, { 'status': this.panelInfo.status })
      this.$emit('editPanelBashInfo', {
        'operation': 'status',
        'value': this.panelInfo.status
      })
    }
  }
}
</script>

<style>
  .view-list {
    height: 100%;
    width: 20%;
    min-width: 180px;
    max-width: 220px;
    border: 1px solid #E6E6E6;
    border-left: 0 solid;
    overflow-y: auto;
  }

  .view-list-thumbnails-outline {
    height: 100%;
    overflow-y: auto;
  }

  .view-list-thumbnails {
    width: 100%;
    padding: 0px 15px 15px 0px;
  }

  .panel-design {
    min-height: 400px;
    height: 100%;
    min-width: 500px;
    overflow-y: hidden;
    border-top: 1px solid #E6E6E6;
  }

  .panel-design-head {
    height: 40px;
    background-color: var(--SiderBG, white);
    padding: 0 10px;
    line-height: 40px;
  }
  .panel-share-head {
      height: auto !important;
  }
  .blackTheme .panel-design-head  {
      color: var(--TextActive);
  }

  .panel-design-preview {
    width: 100%;
    height: calc(100% - 40px);
    overflow-x: hidden;
    overflow-y: auto;
    /*padding: 5px;*/
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

  .dialog-css2 ::v-deep .el-dialog__title {
    font-size: 14px!important;
  }
  .dialog-css2 ::v-deep .el-dialog__header {
    padding: 20px 20px 0!important;
  }
  .dialog-css2 ::v-deep .el-dialog__body {
    padding: 0px 20px!important;
  }
</style>
