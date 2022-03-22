<template>
  <el-row
    v-loading="dataLoading"
    style="height: 100%;width: 100%;"
    :element-loading-text="$t('panel.data_loading')"
    element-loading-spinner="el-icon-loading"
    element-loading-background="rgba(0, 0, 0, 1)"
  >
    <el-col v-if="panelInfo.name.length>0" class="panel-design">

      <el-row v-if="showType === 2" class="panel-design-head panel-share-head">
        <div style="border-bottom: 1px solid #dfe4ed;height: 100%;">
          <share-head />
        </div>
      </el-row>
      <el-row v-else class="panel-design-head">
        <!--仪表板头部区域-->
        <div style="border-bottom: 1px solid #dfe4ed;height: 100%;">
          <el-col :span="12" style="text-overflow:ellipsis;overflow: hidden;white-space: nowrap;font-size: 14px">
            <span>{{ panelInfo.name || '测试仪表板' }}</span>
            &nbsp;
            <span v-if="panelInfo.isDefault" style="color: green;font-size: 12px">({{ $t('panel.default_panel_name') }}:{{ panelInfo.defaultPanelName }})</span>
            <span v-if="panelInfo.sourcePanelName" style="color: green;font-size: 12px">({{ $t('panel.source_panel_name') }}:{{ panelInfo.sourcePanelName }})</span>
          </el-col>
          <el-col :span="12">
            <span v-if="hasDataPermission('manage',panelInfo.privileges)&&activeTab==='PanelList'&&!panelInfo.sourcePanelName" style="float: right;margin-right: 10px">
              <el-button size="mini" type="primary" @click="editPanel">
                {{ $t('commons.edit') }}
              </el-button>
            </span>

            <span v-if="hasDataPermission('export',panelInfo.privileges)" style="float: right;margin-right: 10px">
              <el-tooltip :content="$t('panel.save_to_panel')">
                <el-button class="el-icon-folder-checked" size="mini" circle @click="saveToTemplate" />
              </el-tooltip>
            </span>
            <span v-if="hasDataPermission('export',panelInfo.privileges)" style="float: right;margin-right: 10px">
              <el-tooltip :content="$t('panel.export_to_panel')">
                <el-button class="el-icon-download" size="mini" circle @click="downloadToTemplate" />
              </el-tooltip>
            </span>
            <span v-if="hasDataPermission('export',panelInfo.privileges)" style="float: right;margin-right: 10px">
              <el-tooltip :content="$t('panel.export_to_pdf')">
                <el-button class="el-icon-notebook-2" size="mini" circle @click="downloadAsPDF" />
              </el-tooltip>
            </span>
            <span style="float: right;margin-right: 10px">
              <el-tooltip :content="$t('panel.fullscreen_preview')">
                <el-button class="el-icon-view" size="mini" circle @click="clickFullscreen" />
              </el-tooltip>
            </span>

            <span style="float: right;margin-right: 10px">
              <el-tooltip :content="$t('panel.new_tab_preview')">
                <el-button class="el-icon-data-analysis" size="mini" circle @click="newTab" />
              </el-tooltip>
            </span>

            <span v-if="!hasStar && panelInfo && showType !== 1" style="float: right;margin-right: 10px">
              <el-tooltip :content="$t('panel.store')">
                <el-button class="el-icon-star-off" size="mini" circle @click="star" />
              </el-tooltip>
            </span>

            <span v-if="hasStar && panelInfo && showType !== 1" style="float: right;margin-right: 10px">
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
            <Preview v-if="showMainFlag" :in-screen="!fullscreen" :show-type="'width'" :screen-shot="dataLoading" />
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
import { initPanelData } from '@/api/panel/panel'
import { proxyInitPanelData } from '@/api/panel/shareProxy'
export default {
  name: 'PanelViewShow',
  components: { Preview, SaveToTemplate, PDFPreExport, ShareHead },
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
    bus.$on('set-panel-show-type', type => {
      this.showType = type || 0
    })
    bus.$on('set-panel-share-user', userId => {
      this.shareUserId = userId
    })
    this.initPdfTemplate()
  },
  methods: {
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
      this.dataLoading = true
      setTimeout(() => {
        html2canvas(document.getElementById('canvasInfoTemp')).then(canvas => {
          this.dataLoading = false
          const snapshot = canvas.toDataURL('image/jpeg', 0.1) // 0.2是图片质量
          if (snapshot !== '') {
            this.templateInfo = {
              name: this.$store.state.panel.panelInfo.name,
              templateType: 'self',
              snapshot: snapshot,
              panelStyle: JSON.stringify(this.canvasStyleData),
              panelData: JSON.stringify(this.componentData),
              dynamicData: JSON.stringify(this.panelViewDetailsInfo)
            }
            const blob = new Blob([JSON.stringify(this.templateInfo)], { type: '' })
            FileSaver.saveAs(blob, this.$store.state.panel.panelInfo.name + '-TEMPLATE.DET')
          }
        })
      }, 50)
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
      } else { initPanelData(this.panelInfo.id) }
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
