<template>
  <el-row style="height: 100%;width: 100%;">
    <el-col v-if="panelInfo.name.length>0" class="panel-design">
      <el-row class="panel-design-head">
        <!--TODO 仪表盘头部区域-->
        <span>{{ panelInfo.name || '测试仪表板' }}</span>
        <span style="float: right;margin-right: 10px">
          <el-tooltip content="保存为模板">
            <el-button class="el-icon-folder-checked" size="mini" circle @click="saveToTemplate" />
          </el-tooltip>
        </span>
        <span style="float: right;margin-right: 10px">
          <el-tooltip content="导出为模板">
            <el-button class="el-icon-download" size="mini" circle @click="downloadToTemplate" />
          </el-tooltip>
        </span>
        <span style="float: right;margin-right: 10px">
          <el-tooltip content="预览">
            <el-button class="el-icon-view" size="mini" circle @click="clickPreview" />
          </el-tooltip>
        </span>

        <span v-if="!hasStar && panelInfo" style="float: right;margin-right: 10px">
          <el-tooltip content="收藏">
            <el-button class="el-icon-star-off" size="mini" circle @click="star" />
          </el-tooltip>
        </span>

        <span v-if="hasStar && panelInfo" style="float: right;margin-right: 10px">
          <el-tooltip content="取消">
            <el-button class="el-icon-star-on" size="mini" circle @click="unstar" />
          </el-tooltip>
        </span>
      </el-row>
      <!--TODO 仪表盘预览区域-->
      <el-row class="panel-design-preview">
        <div ref="imageWrapper" style="width: 100%;height: 100%">
          <Preview v-if="showMain" />
        </div>
      </el-row>
    </el-col>
    <el-col v-if="panelInfo.name.length===0" style="height: 100%;">
      <el-row style="height: 100%;" class="custom-position">
        请从左侧选择仪表盘
      </el-row>
    </el-col>

    <el-dialog
      :title="templateSaveTitle"
      :visible.sync="templateSaveShow"
      custom-class="de-dialog"
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
export default {
  name: 'PanelViewShow',
  components: { Preview, SaveToTemplate },
  data() {
    return {
      showMain: true,
      templateInfo: '',
      templateSaveTitle: '保存为模板',
      templateSaveShow: false,
      hasStar: false
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
    clickPreview() {
      const url = '#/preview/' + this.$store.state.panel.panelInfo.id
      window.open(url, '_blank')
    },
    saveToTemplate() {
      this.templateSaveShow = true
      html2canvas(this.$refs.imageWrapper).then(canvas => {
        debugger
        const snapshot = canvas.toDataURL('image/jpeg', 0.2) // 0.2是图片质量
        if (snapshot !== '') {
          this.templateInfo = {
            name: this.$store.state.panel.panelInfo.name,
            snapshot: snapshot,
            templateStyle: JSON.stringify(this.canvasStyleData),
            templateData: JSON.stringify(this.componentData),
            templateType: 'self',
            nodeType: 'folder',
            level: 1,
            pid: null,
            dynamicData: ''
          }
        }
      })
    },
    downloadToTemplate() {
      html2canvas(this.$refs.imageWrapper).then(canvas => {
        debugger
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
      this.templateInfo = ''
      html2canvas(this.$refs.imageWrapper).then(canvas => {
        debugger
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
      })
    },
    unstar() {
      this.panelInfo && deleteEnshrine(this.panelInfo.id).then(res => {
        this.hasStar = false
      })
    },
    initHasStar() {
      const param = {}
      enshrineList(param).then(res => {
        this.hasStar = res.data && res.data.some(item => item.panelGroupId === this.panelInfo.id)
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
    overflow-y: auto;
    border-top: 1px solid #E6E6E6;
  }

  .panel-design-head {
    height: 40px;
    background-color: white;
    padding: 0 6px;
    line-height: 40px;
  }

  .panel-design-preview {
    width: 100%;
    height: calc(100% - 40px);
    overflow-x: hidden;
    overflow-y: auto;
    padding: 5px;
    border-top: 1px solid #E6E6E6;
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
