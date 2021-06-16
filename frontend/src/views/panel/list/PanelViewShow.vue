<template>
  <el-row style="height: 100%;width: 100%;">
    <el-col v-if="panelInfo.name.length>0" class="panel-design">
      <el-row class="panel-design-head">
        <!--仪表板头部区域-->
        <div style="border-bottom: 1px solid #dfe4ed;height: 100%;">
          <el-col :span="12" style="text-overflow:ellipsis;overflow: hidden;white-space: nowrap;font-size: 14px">
            <span>{{ panelInfo.name || '测试仪表板' }}</span>
            &nbsp;
            <span v-if="panelInfo.isDefault" style="color: green;font-size: 12px">({{ $t('panel.default_panel_name') }}:{{ panelInfo.defaultPanelName }})</span>
            <span v-if="panelInfo.sourcePanelName" style="color: green;font-size: 12px">({{ $t('panel.source_panel_name') }}:{{ panelInfo.sourcePanelName }})</span>
          </el-col>
          <el-col :span="12">
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

            <span v-if="!hasStar && panelInfo" style="float: right;margin-right: 10px">
              <el-tooltip :content="$t('panel.store')">
                <el-button class="el-icon-star-off" size="mini" circle @click="star" />
              </el-tooltip>
            </span>

            <span v-if="hasStar && panelInfo" style="float: right;margin-right: 10px">
              <el-tooltip :content="$t('commons.cancel')">
                <el-button class="el-icon-star-on" size="mini" circle @click="unstar" />
              </el-tooltip>
            </span>
          </el-col>
        </div>
      </el-row>
      <!-- 仪表板预览区域-->
      <el-row class="panel-design-preview">
        <div ref="imageWrapper" style="width: 100%;height: 100%">
          <fullscreen style="height: 100%;background: #f7f8fa" :fullscreen.sync="fullscreen">
            <Preview v-if="showMain" :show-type="canvasStyleData.selfAdaption?'full':'width'" />
          </fullscreen>
        </div>
      </el-row>
    </el-col>
    <el-col v-if="panelInfo.name.length===0" style="height: 100%;">
      <el-row style="height: 100%;" class="custom-position">
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
  </el-row>
</template>
<script>
import Preview from '@/components/canvas/components/Editor/Preview'
import SaveToTemplate from '@/views/panel/list/SaveToTemplate'
import { mapState } from 'vuex'
import html2canvas from 'html2canvas'
import FileSaver from 'file-saver'
import { enshrineList, saveEnshrine, deleteEnshrine } from '@/api/panel/enshrine'
import bus from '@/utils/bus'
export default {
  name: 'PanelViewShow',
  components: { Preview, SaveToTemplate },
  data() {
    return {
      showMain: true,
      templateInfo: {},
      templateSaveTitle: '保存为模板',
      templateSaveShow: false,
      hasStar: false,
      fullscreen: false
    }
  },
  computed: {
    panelInfo() {
      return this.$store.state.panel.panelInfo
    },
    ...mapState([
      'componentData',
      'canvasStyleData'
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
    }
  },
  mounted() {
  },
  methods: {
    clickFullscreen() {
      this.fullscreen = true
    },
    newTab() {
      const url = '#/preview/' + this.$store.state.panel.panelInfo.id
      window.open(url, '_blank')
    },
    saveToTemplate() {
      this.templateSaveShow = true
      html2canvas(this.$refs.imageWrapper).then(canvas => {
        const snapshot = canvas.toDataURL('image/jpeg', 0.2) // 0.2是图片质量
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
            dynamicData: ''
          }
        }
      })
    },
    downloadToTemplate() {
      html2canvas(this.$refs.imageWrapper).then(canvas => {
        const snapshot = canvas.toDataURL('image/jpeg', 0.2) // 0.2是图片质量
        if (snapshot !== '') {
          this.templateInfo = {
            name: this.$store.state.panel.panelInfo.name,
            templateType: 'self',
            snapshot: snapshot,
            panelStyle: JSON.stringify(this.canvasStyleData),
            panelData: JSON.stringify(this.componentData),
            dynamicData: ''
          }
          const blob = new Blob([JSON.stringify(this.templateInfo)], { type: '' })
          FileSaver.saveAs(blob, this.$store.state.panel.panelInfo.name + '-TEMPLATE.DE')
        }
      })
    },
    refreshTemplateInfo() {
      this.templateInfo = {}
      html2canvas(this.$refs.imageWrapper).then(canvas => {
        const snapshot = canvas.toDataURL('image/jpeg', 0.2) // 0.2是图片质量
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
      const param = {}
      enshrineList(param).then(res => {
        this.hasStar = res.data && res.data.some(item => item.panelGroupId === this.panelInfo.id)
      })
    },
    refreshStarList(isStar) {
      bus.$emit('panle_start_list_refresh', isStar)
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
    overflow-y: auto;
    border-top: 1px solid #E6E6E6;
  }

  .panel-design-head {
    height: 40px;
    background-color: white;
    padding: 0 10px;
    line-height: 40px;
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
</style>
