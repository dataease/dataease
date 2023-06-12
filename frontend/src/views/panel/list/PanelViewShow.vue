<template>
  <el-row
    v-loading="dataLoading"
    style="height: 100%;width: 100%;"
    :element-loading-text="$t('panel.data_loading')"
    element-loading-spinner="el-icon-loading"
    element-loading-background="rgba(220,220,220,1)"
  >
    <el-col
      v-if="panelInfo.name.length>0"
      class="panel-design"
    >

      <el-row
        v-if="showType === 2"
        class="panel-design-head panel-share-head"
        style="border-bottom: 1px solid;border-bottom-color:#E6E6E6;"
      >
        <div style="height: 100%;">
          <share-head/>
        </div>
      </el-row>
      <el-row
        v-else
        class="panel-design-head"
        style="border-bottom: 1px solid;border-bottom-color:#E6E6E6;"
      >
        <!--仪表板头部区域-->
        <div style="height: 100%;">
          <el-col
            :span="12"
            style="font-size: 14px;display: flex"
          >
            <div :title="showName" style="text-overflow:ellipsis;overflow: hidden;white-space: nowrap;font-size: 14px;max-width: 300px"><span
              class="panel-name"
            >
              {{ panelInfo.name || '测试仪表板' }}</span>
              <span
                v-if="panelInfo.isDefault"
                style="color: green;font-size: 12px"
              >({{ $t('panel.default_panel_name') }}:{{ panelInfo.defaultPanelName }})</span>
              <span
                v-if="panelInfo.sourcePanelName"
                style="color: green;font-size: 12px"
              >&nbsp;({{ $t('panel.source_panel_name') }}:{{ panelInfo.sourcePanelName }})</span>
            </div>
            <div class="panel-create-cont">
              <span
                v-if="!hasStar && panelInfo && showType !== 1&&panelInfo.status==='publish'"
                style="margin-left: 9px"
              >
              <el-tooltip :content="$t('panel.store')">
                <i
                  class="el-icon-star-off"
                  @click="star"
                />
              </el-tooltip>
            </span>
              <span
                v-if="hasStar && panelInfo && showType !== 1&&panelInfo.status==='publish'"
                style="margin-left: 9px"
              >
              <el-tooltip :content="$t('commons.cancel') + $t('panel.store')">
                <i
                  class="el-icon-star-on"
                  @click="unstar"
                />
              </el-tooltip>
            </span>
              <template v-if="panelInfo.creatorName">
                <el-divider
                  style="margin: 0 16px;"
                  direction="vertical"
                />
                <span :title="panelInfo.creatorName" class="panel-create">
                {{ $t('panel.create_by') + ':' + panelInfo.creatorName }}
              </span>
              </template>
              <el-popover
                placement="right-start"
                width="400"
                trigger="click"
              >
                <panel-detail-info/>
                <svg-icon slot="reference" style="margin-left: 4px;cursor: pointer;font-size: 14px;" class="icon-class"
                          icon-class="icon_info_outlined"
                />
              </el-popover>
            </div>

          </el-col>
          <el-col :span="12">

            <span
              v-if="hasDataPermission('manage',panelInfo.privileges)&&activeTab==='PanelList'&&!panelInfo.sourcePanelName"
              style="float: right;margin-right: 10px"
            >
              <de-btn
                type="primary"
                @click="editPanel"
              >
                {{ $t('commons.edit') }}
              </de-btn>
            </span>

            <span
              v-if="showType !== 1"
              style="float: right;margin-right: 10px"
            >
              <de-btn
                secondary
                @click="share"
              >
                {{ $t('panel.share') }}
              </de-btn>
            </span>

            <span
              v-if="panelInfo.status==='publish' && !isOtherPlatform"
              style="float: right;margin-right: 10px"
            >
              <de-btn
                secondary
                @click="clickFullscreen"
              >
                {{ $t('panel.fullscreen_preview') }}
              </de-btn>
            </span>

            <span
              v-if="activeTab!=='panels_star' || (activeTab ==='panels_star' && panelInfo.status === 'publish')"
              style="float: right;margin-right: 10px"
              class="de-tree"
            >
              <el-dropdown
                trigger="click"
                size="small"
                placement="bottom-start"
              >
                <span class="el-dropdown-link de-el-tree-node__content">
                  <el-button
                    icon="el-icon-more"
                    type="text"
                    size="small"
                  />
                </span>
                <el-dropdown-menu
                  slot="dropdown"
                  class="de-card-dropdown"
                >
                  <el-dropdown-item
                    v-if="panelInfo.status==='publish' && !isOtherPlatform"
                    @click.native="newTab"
                  >
                    <svg-icon
                      icon-class="icon_pc_outlined_copy"
                      class="preview-icon-svg"
                    />
                    {{ $t('panel.new_tab_preview') }}
                  </el-dropdown-item>

                  <el-dropdown-item
                    icon="el-icon-refresh"
                    @click.native="refreshPanel"
                  >
                    {{ $t('commons.refresh') + $t('chart.chart_data') }}
                  </el-dropdown-item>

                  <el-dropdown-item
                    v-if="hasDataPermission('export',panelInfo.privileges)&&panelInfo.status==='publish'"
                    @click.native="saveToTemplate"
                  >
                    <svg-icon
                      icon-class="icon_yes_outlined"
                      class="preview-icon-svg"
                    />
                    {{ $t('panel.save_to_panel') }}
                  </el-dropdown-item>

                  <el-dropdown-item
                    v-if="hasDataPermission('manage',panelInfo.privileges)&&activeTab==='PanelList'&&!panelInfo.sourcePanelName"
                    @click.native="changePublishState"
                  >
                    <svg-icon
                      :icon-class="panelInfo.status==='publish' ? 'cancel_release' : 'release'"
                      class="preview-icon-svg"
                    />
                    {{ $t(`commons.${panelInfo.status === 'publish' ? 'unpublished' : 'publish'}`) }}
                  </el-dropdown-item>

                  <el-dropdown
                    v-if="hasDataPermission('export',panelInfo.privileges)&&panelInfo.status==='publish'"
                    style="width: 100%"
                    trigger="hover"
                    placement="right-start"
                  >
                    <div class="el-dropdown-menu__item">
                      <svg-icon
                        icon-class="icon_bottom-align_outlined"
                        class="preview-icon-svg"
                      />
                      {{ $t('log.export_as') }}
                      <svg-icon
                        style="margin-top: 7px;float: right"
                        icon-class="icon_right_outlined"
                        class="preview-icon-svg"
                      />
                    </div>
                    <el-dropdown-menu
                      slot="dropdown"
                      class="de-card-dropdown de-card-dropdown-right"
                    >
                      <el-dropdown-item
                        icon="el-icon-copy-document"
                        @click.native="downloadToTemplate"
                      >{{ $t('panel.export_to_panel') }}</el-dropdown-item>
                      <el-dropdown-item
                        icon="el-icon-notebook-2"
                        @click.native="downloadAsPDF"
                      >{{ $t('panel.export_to_pdf') }}</el-dropdown-item>
                      <el-dropdown-item
                        icon="el-icon-picture-outline"
                        @click.native="downloadAsImage"
                      >{{ $t('panel.export_to_img') }}</el-dropdown-item>
                      <el-dropdown-item
                        icon="el-icon-s-data"
                        @click.native="downLoadToAppPre"
                      >{{ $t('panel.export_to_app') }}</el-dropdown-item>
                    </el-dropdown-menu>
                  </el-dropdown>
                </el-dropdown-menu>
              </el-dropdown>
            </span>
          </el-col>
        </div>
      </el-row>
      <!-- 仪表板预览区域-->
      <el-row class="panel-design-preview">
        <!--        <div id="imageWrapper" ref="imageWrapper" style="width: 4096px;height: 2160px">-->
        <div
          id="imageWrapper"
          ref="imageWrapper"
          :style="imageWrapperStyle"
        >
          <fullscreen
            style="height: 100%;background: #f7f8fa;overflow-y: auto"
            :fullscreen.sync="fullscreen"
          >
            <Preview
              v-if="showMainFlag"
              ref="paneViewPreviewRef"
              :component-data="mainCanvasComponentData"
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
    <el-col
      v-if="panelInfo.name.length===0"
      style="height: 100%;"
    >
      <el-row
        style="height: 100%; background-color: var(--ContentBG);"
        class="custom-position"
      >
        {{ $t('panel.select_panel_from_left') }}
      </el-row>
    </el-col>

    <el-dialog
      v-if="templateSaveShow"
      :title="$t('panel.save_to_panel')"
      :visible.sync="templateSaveShow"
      width="500px"
    >
      <save-to-template
        :template-info="templateInfo"
        @closeSaveDialog="closeSaveDialog"
      />
    </el-dialog>
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

    <el-dialog
      v-if="appExportShow"
      :title="$t('app_template.app_export')"
      :visible.sync="appExportShow"
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

    <el-dialog
      v-dialogDrag
      :title="authTitle"
      :visible.sync="authVisible"
      width="800px"
      class="dialog-css"
    >
      <grant-auth
        v-if="authVisible"
        :resource-id="authResourceId"
        @close-grant="closeGrant"
      />
    </el-dialog>

    <keep-alive>
      <app-export-form
        ref="appExportForm"
        @downLoadApp="downLoadApp"
      />
    </keep-alive>
  </el-row>
</template>
<script>
import PDFPreExport from '@/views/panel/export/PDFPreExport'
import Preview from '@/components/canvas/components/editor/Preview'
import SaveToTemplate from '@/views/panel/list/SaveToTemplate'
import { mapState } from 'vuex'
import html2canvas from 'html2canvasde'
import FileSaver from 'file-saver'
import JSZip from 'jszip'
import { deleteEnshrine, saveEnshrine, starStatus } from '@/api/panel/enshrine'
import bus from '@/utils/bus'
import { queryAll } from '@/api/panel/pdfTemplate'
import ShareHead from '@/views/panel/grantAuth/shareHead'
import { export2AppCheck, initPanelData, updatePanelStatus } from '@/api/panel/panel'
import { proxyInitPanelData } from '@/api/panel/shareProxy'
import { dataURLToBlob, getNowCanvasComponentData } from '@/components/canvas/utils/utils'
import { findResourceAsBase64 } from '@/api/staticResource/staticResource'
import PanelDetailInfo from '@/views/panel/list/common/PanelDetailInfo'
import AppExportForm from '@/views/panel/list/AppExportForm'
import GrantAuth from '../grantAuth'
import msgCfm from '@/components/msgCfm/index'
import { inOtherPlatform } from '@/utils/index'

export default {
  name: 'PanelViewShow',
  components: { AppExportForm, PanelDetailInfo, Preview, SaveToTemplate, PDFPreExport, ShareHead, GrantAuth },
  mixins: [msgCfm],
  props: {
    activeTab: {
      type: String,
      required: false
    }
  },
  data() {
    return {
      authTitle: null,
      authResourceId: null,
      authVisible: false,
      canvasInfoTemp: 'preview-temp-canvas-main',
      canvasId: 'canvas-main',
      showMain: true,
      pdfTemplateSelectedIndex: 0,
      pdfTemplateContent: '',
      templateInfo: {},
      pdfTemplateAll: [],
      templateSaveShow: false,
      hasStar: false,
      fullscreen: false,
      pdfExportShow: false,
      appExportShow: false,
      snapshotInfo: '',
      showType: 0,
      dataLoading: false,
      exporting: false,
      shareUserId: null
    }
  },
  computed: {
    showName(){
      let name = this.panelInfo.name || '测试仪表板'
      if(this.panelInfo.isDefault){
        name = name +'('+ this.$t('panel.default_panel_name') +':'+ this.panelInfo.defaultPanelName +')'
      }

      if(this.panelInfo.sourcePanelName){
        name = name +'('+ this.$t('panel.source_panel_name') +':'+ this.panelInfo.sourcePanelName +')'
      }
      return name
    },
    mainCanvasComponentData() {
      return getNowCanvasComponentData(this.canvasId)
    },
    imageWrapperStyle() {
      if (this.exporting) {
        return {
          width: '1280px',
          height: '720px'
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
    isOtherPlatform() {
      return inOtherPlatform()
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
    // 画布高度大于6000px 自动使用原尺寸导出 减小浏览器再渲染压力
    changeExportingState() {
      const canvasHeight = this.$refs.paneViewPreviewRef.getCanvasHeight()
      if (canvasHeight && canvasHeight > 6000) {
        return false
      } else {
        return true
      }
    },
    downLoadApp(appAttachInfo) {
      this.downLoadToApp(appAttachInfo)
    },
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
    share() {
      this.authResourceId = this.panelInfo.id
      this.authTitle = this.$t('panel.share_to_some', { some: this.panelInfo.name })
      this.authVisible = true
    },
    closeGrant() {
      this.authResourceId = null
      this.authVisible = false
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
        html2canvas(document.getElementById(this.canvasInfoTemp)).then(canvas => {
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
          html2canvas(document.getElementById(_this.canvasInfoTemp)).then(canvas => {
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
    saveAppFile(appRelationInfo, appAttachInfo) {
      const _this = this
      _this.dataLoading = true
      try {
        const jsZip = new JSZip()
        _this.findStaticSource(function(staticResource) {
          html2canvas(document.getElementById(_this.canvasInfoTemp)).then(canvas => {
            _this.dataLoading = false
            const snapshot = canvas.toDataURL('image/jpeg', 1) // 0.1是图片质量
            if (snapshot !== '') {
              const panelInfo = {
                name: _this.$store.state.panel.panelInfo.name,
                id: _this.$store.state.panel.panelInfo.id,
                snapshot: snapshot,
                panelStyle: JSON.stringify(_this.canvasStyleData),
                panelData: JSON.stringify(_this.componentData),
                dynamicData: JSON.stringify(_this.panelViewDetailsInfo),
                staticResource: JSON.stringify(staticResource || {})
              }
              const blobTemplate = new Blob([JSON.stringify(panelInfo)], { type: '' })
              const blobRelation = new Blob([JSON.stringify(appRelationInfo)], { type: '' })
              const blobAppInfo = new Blob([JSON.stringify(appAttachInfo)], { type: '' })
              jsZip.file('TEMPLATE.DET', blobTemplate, { binary: true })
              jsZip.file('DATA_RELATION.DE', blobRelation, { binary: true })
              jsZip.file('APP.json', blobAppInfo, { binary: true })
              jsZip.generateAsync({ type: 'blob' }).then(content => {
                // 生成二进制流
                FileSaver.saveAs(content, appAttachInfo.appName + '.zip') // 利用file-saver保存文件  自定义文件名
              })
            }
          })
        })
      } catch (e) {
        console.error(e)
        _this.dataLoading = false
      }
    },
    downLoadToAppPre() {
      this.$refs.appExportForm.init({
        appName: this.$store.state.panel.panelInfo.name,
        icon: null,
        version: '1.0',
        creator: this.$store.getters.user.nickName,
        required: '1.16.0',
        description: null
      })
    },
    downLoadToApp(appAttachInfo) {
      this.dataLoading = true
      export2AppCheck(this.$store.state.panel.panelInfo.id).then(rsp => {
        if (rsp.data.checkStatus) {
          this.saveAppFile(rsp.data, appAttachInfo)
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
        if (item.type === 'picture-add' && item.propValue && typeof item.propValue === 'string' && item.propValue.indexOf('static-resource') > -1) {
          staticResource.push(item.propValue)
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
        this.exporting = this.changeExportingState()
        setTimeout(() => {
          const canvasID = document.getElementById(this.canvasInfoTemp)
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
        this.exporting = this.changeExportingState()
        setTimeout(() => {
          html2canvas(document.getElementById(this.canvasInfoTemp)).then(canvas => {
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
      html2canvas(document.getElementById(this.canvasInfoTemp)).then(canvas => {
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
      } else {
        initPanelData(this.panelInfo.id, false)
      }
    },
    changePublishState() {
      if (this.panelInfo.status === 'publish') {
        const options = {
          title: this.$t('panel.unpublished_tips'),
          type: 'primary',
          cb: () => this.updatePublishStatus('unpublished')
        }
        this.handlerConfirm(options, this.$t('commons.confirm'))
      } else {
        this.updatePublishStatus('publish')
      }
    },
    updatePublishStatus(newStatus) {
      this.panelInfo.status = newStatus
      updatePanelStatus(this.panelInfo.id, { 'status': this.panelInfo.status })
      this.$emit('editPanelBashInfo', {
        'operation': 'status',
        'value': this.panelInfo.status
      })
    }
  }
}
</script>

<style lang="less">
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
  height: 56px;
  background-color: var(--SiderBG, white);
  padding: 0 10px;
  line-height: 56px;

  .panel-name {
    font-family: PingFang SC;
    font-size: 16px;
    font-weight: 500;
    line-height: 24px;
    flex: 1;
    color: var(--deTextPrimary, #1F2329);

  }

  .panel-create-cont {
    display: flex;
    align-items: center;
  }

  .panel-create {
    font-family: PingFang SC;
    font-size: 14px;
    font-weight: 400;
    line-height: 22px;
    color: var(--deTextSecondary, #646A73);
    text-overflow: ellipsis;
    overflow: hidden;
    white-space: nowrap;
    max-width: 200px;
  }
}

.panel-share-head {
  height: auto !important;
}

.blackTheme .panel-design-head {
  color: var(--TextActive);
}

.panel-design-preview {
  width: 100%;
  height: calc(100% - 56px);
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
  font-size: 14px !important;
}

.dialog-css2 ::v-deep .el-dialog__header {
  padding: 20px 20px 0 !important;
}

.dialog-css2 ::v-deep .el-dialog__body {
  padding: 0px 20px !important;
}

.preview-icon-svg {
  color: inherit;
  margin-right: 5px;
}
</style>

