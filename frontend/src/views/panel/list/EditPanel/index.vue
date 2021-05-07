<template>
  <el-row>
    <el-row v-if="editPanel.optType==='new' && editPanel.panelInfo.nodeType==='panel'">
      <el-col :span="18" style="height: 40px">
        <el-radio v-model="inputType" label="self">自定义</el-radio>
        <el-radio v-model="inputType" label="import">导入模板</el-radio>
        <el-radio v-model="inputType" label="copy">复用模板</el-radio>
      </el-col>
      <el-col v-if="inputType==='import'" :span="6">
        <el-button class="el-icon-upload" size="small" type="primary" @click="goFile">上传模板</el-button>
        <input id="input" ref="files" type="file" accept=".DE" hidden @change="handleFileChange">
      </el-col>
    </el-row>
    <el-row style="margin-top: 5px">
      <el-col :span="4">{{ editPanel.titleSuf }}名称</el-col>
      <el-col :span="20">
        <el-input v-model="editPanel.panelInfo.name" clearable size="mini" />
      </el-col>
    </el-row>
    <el-row v-if="inputType==='copy'" class="preview">
      <el-col :span="8" style="overflow: auto">
        <template-all-list :template-list="templateList" @showCurrentTemplateInfo="showCurrentTemplateInfo" />
      </el-col>
      <el-col :span="16" :style="classBackground" class="preview-show" />
    </el-row>
    <el-row v-if="inputType==='import'" class="preview" :style="classBackground" />
    <el-row class="root-class">
      <el-button @click="cancel()">取 消</el-button>
      <el-button type="primary" @click="save()">确 定</el-button>
    </el-row>
  </el-row>
</template>

<script>
import { post, panelSave } from '@/api/panel/panel'
import TemplateAllList from './TemplateAllList'

export default {
  components: { TemplateAllList },
  props: {
    editPanel: {
      type: Object,
      required: true
    }
  },
  data() {
    return {
      inputType: 'self',
      fieldName: 'name',
      tableRadio: null,
      keyWordSearch: '',
      columnLabel: '所属类别',
      templateList: [],
      importTemplateInfo: {
        snapshot: ''
      }
    }
  },
  computed: {
    classBackground() {
      return {
        background: `url(${this.importTemplateInfo.snapshot}) no-repeat`
      }
    }
  },
  watch: {
    inputType() {
      this.editPanel.panelInfo.name = null
      this.editPanel.panelInfo.panelStyle = null
      this.editPanel.panelInfo.panelData = null
      this.importTemplateInfo.snapshot = null
    }
  },
  created() {
    this.getTree()
  },
  methods: {
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
      post('/template/templateList', request).then(res => {
        this.templateList = res.data
      })
    },
    handleExceed(file) {
      console.log(file.name)
    },
    cancel() {
      this.$emit('closeEditPanelDialog')
    },
    save() {
      if (!this.editPanel.panelInfo.name) {
        this.$warning('名称不能为空')
        return false
      }
      panelSave(this.editPanel.panelInfo).then(response => {
        this.$message({
          message: '保存成功',
          type: 'success',
          showClose: true
        })
        this.$emit('closeEditPanelDialog')
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
        console.log(this.importTemplateInfo)
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
    height:300px !important;
    overflow:auto;
    background-size: 100% 100% !important;
  }
  .preview-show {
    border-left:1px solid #E6E6E6;
    height:300px;
    background-size: 100% 100% !important;
  }
</style>
