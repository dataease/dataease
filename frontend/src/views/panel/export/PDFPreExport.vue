<template>
  <el-row>
    <el-row class="export_body_class">
      <div id="exportPdf" ref="exportPdf">
        <div class="export_body_inner_class" v-html="templateContentChange" />
      </div>
    </el-row>
    <el-row class="root_class">
      <el-button size="mini" @click="cancel()">{{ $t('commons.cancel') }}</el-button>
      <el-button type="primary" size="mini" @click="save()">导出PDF</el-button>
    </el-row>
  </el-row>
</template>

<script>
import JsPDF from 'jspdf'
import html2canvas from 'html2canvasde'
import { formatTimeToStr } from './date.js'
import { pdfTemplateReplaceAll } from '@/utils/StringUtils.js'

export default {
  name: 'PDFPreExport',
  props: {
    panelName: {
      type: String,
      required: false
    },
    snapshot: {
      type: String,
      required: true
    },
    templateContent: {
      type: String,
      required: true
    }
  },
  data() {
    return {
      activeName: '',
      templateContentChange: '',
      time: '',
      varsInfo: {
        panelName: this.panelName,
        snapshot: this.snapshot,
        userName: this.$store.state.user.user.name
      },
      timeInfo: [
        'yyyy-MM-dd',
        'yyyy-MM-dd hh:mm:ss'
      ]
    }
  },
  computed: {

  },
  watch: {
    templateContent(newVal, oldVla) {
      this.initContent()
    }

  },
  mounted() {
    var date = new Date()
    var _this = this
    _this.timeInfo.forEach(timeFormat => {
      _this.varsInfo[timeFormat] = formatTimeToStr(date, timeFormat)
    })

    this.initContent()
  },
  methods: {
    initContent() {
      this.templateContentChange = this.templateContent
      for (const [key, value] of Object.entries(this.varsInfo)) {
        this.templateContentChange = pdfTemplateReplaceAll(this.templateContentChange, key, value)
      }
    },

    cancel() {
      this.$emit('closePreExport')
    },
    save() {
      const _this = this
      html2canvas(document.getElementById('exportPdf')).then(function(canvas) {
        const contentWidth = canvas.width
        const contentHeight = canvas.height
        const pageData = canvas.toDataURL('image/jpeg', 1.0)
        const PDF = new JsPDF('l', 'px', [contentWidth, contentHeight])
        PDF.addImage(pageData, 'JPEG', 0, 0, contentWidth, contentHeight)
        PDF.save(_this.panelName + '.pdf')
      }
      )
    }

  }
}
</script>

<style scoped>
  .root_class {
    margin: 15px 0px 5px;
    text-align: center;
  }
  .export_body_class{
    border: 1px solid #dcdfe6 ;
    height: 65vh;
    overflow-y: auto;
  }

  .export_body_inner_class{
    margin: 10px;
  }
</style>
