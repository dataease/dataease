<template>
  <el-row
    v-loading="exportLoading"
    style="height: 100%;width: 100%;"
    :element-loading-text="$t('panel.export_loading')"
    element-loading-spinner="el-icon-loading"
    element-loading-background="rgba(220,220,220, 1)"
  >
    <el-row class="export_body_class">
      <div
        id="exportPdf"
        ref="exportPdf"
        :style="mainCanvasStyle"
      >
        <div
          class="export_body_inner_class"
          :style="templateHtmlStyle"
          v-html="templateContentChange"
        />
      </div>
    </el-row>
    <el-row class="root_class">
      <el-button
        size="mini"
        @click="cancel()"
      >{{ $t('commons.cancel') }}
      </el-button>
      <el-button
        type="primary"
        size="mini"
        @click="save()"
      >{{ $t('panel.export_pdf') }}
      </el-button>
    </el-row>
  </el-row>
</template>

<script>
import JsPDF from 'jspdf'
import html2canvas from 'html2canvasde'
import { formatTimeToStr } from './date.js'
import { pdfTemplateReplaceAll, replaceInlineI18n } from '@/utils/StringUtils.js'

export default {
  name: 'PDFPreExport',
  components: {},
  props: {
    // eslint-disable-next-line vue/require-default-prop
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
      toExport: false,
      exportLoading: false,
      activeName: '',
      templateContentChange: '',
      time: '',
      varsInfo: {
        panelName: this.panelName,
        snapshot: this.snapshot,
        userName: this.$store.state.user.user.userName,
        nickName: this.$store.state.user.user.nickName
      },
      timeInfo: [
        'yyyy-MM-dd',
        'yyyy-MM-dd hh:mm:ss'
      ]
    }
  },
  computed: {
    mainCanvasStyle() {
      if (this.toExport) {
        return {
          width: '1280px'
        }
      } else {
        return {
          width: '100%'
        }
      }
    },
    templateHtmlStyle() {
      if (this.toExport) {
        return {
          fontSize: '14px!important'
        }
      } else {
        return {}
      }
    }
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
        this.templateContentChange = pdfTemplateReplaceAll(this.templateContentChange, key, value || '')
      }
      this.templateContentChange = replaceInlineI18n(this.templateContentChange)
    },

    cancel() {
      this.$emit('closePreExport')
    },
    save() {
      const _this = this
      _this.exportLoading = true
      setTimeout(() => {
        _this.toExport = true
        setTimeout(() => {
          html2canvas(document.getElementById('exportPdf')).then(function(canvas) {
            _this.exportLoading = false
            const contentWidth = canvas.width / 2
            const contentHeight = canvas.height / 2
            const pageData = canvas.toDataURL('image/jpeg', 1.0)
            const lp = contentWidth > contentHeight ? 'l' : 'p'
            const PDF = new JsPDF(lp, 'pt', [contentWidth, contentHeight])
            PDF.addImage(pageData, 'JPEG', 0, 0, contentWidth, contentHeight)
            PDF.save(_this.panelName + '.pdf')
            _this.$emit('closePreExport')
          })
        }, 1500)
      }, 500)
    }
  }
}
</script>

<style scoped>
.root_class {
  margin: 15px 0px 5px;
  text-align: center;
}

.export_body_class {
  border: 1px solid #dcdfe6;
  height: 65vh;
  overflow-y: auto;
}

.export_body_inner_class {
  margin: 10px;
}
</style>
