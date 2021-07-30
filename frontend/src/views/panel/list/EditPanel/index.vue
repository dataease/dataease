<template>
  <el-row v-loading="loading">
    <el-row v-if="editPanel.optType==='new' && editPanel.panelInfo.nodeType==='panel'">
      <el-col :span="18" style="height: 40px">
        <el-radio v-model="inputType" label="self"> {{ $t('panel.custom') }}</el-radio>
        <!--        <el-radio v-model="inputType" label="import">{{ $t('panel.import_template') }}  </el-radio>-->
        <el-radio v-model="inputType" label="copy" @click.native="getTree">{{ $t('panel.copy_template') }}  </el-radio>
      </el-col>
      <el-col v-if="inputType==='import'" :span="6">
        <el-button class="el-icon-upload" size="small" type="primary" @click="goFile">{{ $t('panel.upload_template') }}</el-button>
        <input id="input" ref="files" type="file" accept=".DE" hidden @change="handleFileChange">
      </el-col>
    </el-row>
    <el-row style="margin-top: 5px">
      <el-col :span="4">{{ editPanel.titleSuf }}{{ $t('commons.name') }}</el-col>
      <el-col :span="20">
        <el-input v-model="editPanel.panelInfo.name" clearable size="mini" />
      </el-col>
    </el-row>
    <el-row v-if="inputType==='copy'" class="preview">
      <el-col :span="8" style="height:100%;overflow-y: auto">
        <template-all-list :template-list="templateList" @showCurrentTemplateInfo="showCurrentTemplateInfo" />
      </el-col>
      <el-col :span="16" :style="classBackground" class="preview-show" />
    </el-row>
    <!--    <el-row v-if="inputType==='import'" class="preview" :style="classBackground" />-->
    <el-row class="root-class">
      <el-button size="mini" @click="cancel()">{{ $t('commons.cancel') }}</el-button>
      <el-button type="primary" size="mini" @click="save()">{{ $t('commons.confirm') }}</el-button>
    </el-row>
  </el-row>
</template>

<script>
import { panelSave } from '@/api/panel/panel'
import { showTemplateList } from '@/api/system/template'
import TemplateAllList from './TemplateAllList'
import { deepCopy } from '@/components/canvas/utils/utils'

export default {
  components: { TemplateAllList },
  props: {
    editPanelOut: {
      type: Object,
      required: true
    }
  },
  data() {
    return {
      loading: false,
      inputType: 'self',
      fieldName: 'name',
      tableRadio: null,
      keyWordSearch: '',
      columnLabel: this.$t('panel.belong_to_category'),
      templateList: [],
      importTemplateInfo: {
        snapshot: ''
      },
      editPanel: null
    }
  },
  computed: {
    classBackground() {
      if (this.importTemplateInfo.snapshot) {
        return {
          background: `url(${this.importTemplateInfo.snapshot}) no-repeat`
        }
      } else {
        return {}
      }
    }
  },
  watch: {
    inputType(newVal) {
      if (newVal === 'self') {
        this.editPanel = deepCopy(this.editPanelOut)
      } else {
        this.editPanel.panelInfo.name = null
        this.editPanel.panelInfo.panelStyle = null
        this.editPanel.panelInfo.panelData = null
        this.importTemplateInfo.snapshot = null
      }
    }
  },
  created() {
    this.editPanel = deepCopy(this.editPanelOut)
    // this.getTree()
  },
  mounted() {
    this.bindKey()
  },
  destroyed() {
    this.unBindKey()
  },
  methods: {
    entryKey(event) {
      const keyCode = event.keyCode
      if (keyCode === 13) {
        this.save()
      }
    },
    bindKey() {
      document.addEventListener('keypress', this.entryKey)
    },
    unBindKey() {
      document.removeEventListener('keypress', this.entryKey)
    },
    showCurrentTemplateInfo(data) {
      this.editPanel.panelInfo.name = data.name
      this.editPanel.panelInfo.panelStyle = data.templateStyle
      this.editPanel.panelInfo.panelData = data.templateData
      this.importTemplateInfo.snapshot = data.snapshot
    },
    getTree() {
      const request = {
        level: '-1',
        withChildren: true
      }
      this.loading = true
      showTemplateList(request).then(res => {
        this.templateList = res.data
        this.loading = false
      })
    },
    handleExceed(file) {
    },
    cancel() {
      this.$emit('closeEditPanelDialog')
    },
    save() {
      if (!this.editPanel.panelInfo.name) {
        this.$warning(this.$t('chart.name_can_not_empty'))
        return false
      }

      debugger
      if (this.editPanel.panelInfo.name.length > 50) {
        this.$warning(this.$t('commons.char_can_not_more_50'))
        return false
      }

      if (!this.editPanel.panelInfo.panelData && this.editPanel.optType === 'new' && this.inputType === 'copy') {
        this.$warning(this.$t('chart.template_can_not_empty'))
        return false
      }
      panelSave(this.editPanel.panelInfo).then(response => {
        this.$message({
          message: this.$t('commons.save_success'),
          type: 'success',
          showClose: true
        })
        this.$emit('closeEditPanelDialog', response.data)
      })
    },
    handleFileChange(e) {
      const file = e.target.files[0]
      const reader = new FileReader()
      reader.onload = (res) => {
        const result = res.target.result
        this.importTemplateInfo = JSON.parse(result)
        this.editPanel.panelInfo.name = this.importTemplateInfo.name
        this.editPanel.panelInfo.panelStyle = this.importTemplateInfo.panelStyle
        this.editPanel.panelInfo.panelData = this.importTemplateInfo.panelData
      }
      reader.readAsText(file)
    },
    goFile() {
      this.$refs.files.click()
    }

  }
}
</script>

<style scoped>

.my_table >>> .el-table__row>td{
  /* 去除表格线 */
  border: none;
  padding: 0 0;
}
.my_table >>> .el-table th.is-leaf {
  /* 去除上边框 */
    border: none;
}
.my_table >>> .el-table::before{
  /* 去除下边框 */
  height: 0;
}

  .root-class {
    margin: 15px 0px 5px;
    text-align: center;
  }
  .preview {
    margin-top: 5px;
    border:1px solid #E6E6E6;
    height:250px !important;
    overflow:hidden;
    background-size: 100% 100% !important;
  }
  .preview-show {
    border-left:1px solid #E6E6E6;
    height:250px;
    background-size: 100% 100% !important;
  }
</style>
