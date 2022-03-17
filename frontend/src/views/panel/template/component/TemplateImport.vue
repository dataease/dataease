<template>
  <el-row v-loading="$store.getters.loadingMap[$store.getters.currentPath]">
    <el-row style="margin-top: 5px">
      <el-col :span="4">{{ $t('commons.name') }}</el-col>
      <el-col :span="16">
        <el-input v-model="templateInfo.name" clearable size="mini" />
      </el-col>
      <el-col :span="4">
        <el-button style="margin-left: 10px" class="el-icon-upload" size="small" type="primary" @click="goFile">{{ $t('panel.upload_template') }}</el-button>
        <input id="input" ref="files" type="file" accept=".DET" hidden @change="handleFileChange">
      </el-col>
    </el-row>
    <el-row class="preview" :style="classBackground" />
    <el-row class="root-class">
      <el-button size="mini" @click="cancel()">{{ $t('commons.cancel') }}</el-button>
      <el-button type="primary" size="mini" @click="save()">{{ $t('commons.confirm') }}</el-button>
    </el-row>
  </el-row>
</template>

<script>
import { save, nameCheck } from '@/api/system/template'

export default {

  props: {
    pid: {
      type: String,
      required: true
    }
  },
  data() {
    return {
      tableRadio: null,
      keyWordSearch: '',
      columnLabel: this.$t('panel.belong_to_category'),
      templateList: [],
      importTemplateInfo: {
        snapshot: ''
      },
      templateInfo: {
        level: '1',
        pid: this.pid,
        name: '',
        templateStyle: null,
        templateData: null,
        dynamicData: null,
        snapshot: ''
      }
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
  methods: {
    handleExceed(file) {
    },
    cancel() {
      this.$emit('closeEditTemplateDialog')
    },
    save() {
      if (!this.templateInfo.name) {
        this.$warning(this.$t('chart.name_can_not_empty'))
        return false
      }
      if (!this.templateInfo.templateData) {
        this.$warning(this.$t('chart.template_can_not_empty'))
        return false
      }
      const nameCheckRequest = {
        pid: this.templateInfo.pid,
        name: this.templateInfo.name,
        optType: 'insert'
      }
      nameCheck(nameCheckRequest).then(response => {
        if (response.data.indexOf('exist') > -1) {
          this.$confirm(this.$t('template.exit_same_template_check'), this.$t('template.confirm_upload'), {
            confirmButtonText: this.$t('template.override'),
            cancelButtonText: this.$t('template.cancel'),
            type: 'warning'
          }).then(() => {
            save(this.templateInfo).then(response => {
              this.$message({
                message: this.$t('commons.save_success'),
                type: 'success',
                showClose: true
              })
              this.$emit('closeEditTemplateDialog')
            })
          }).catch(() => {
          })
        } else {
          save(this.templateInfo).then(response => {
            this.$message({
              message: this.$t('commons.save_success'),
              type: 'success',
              showClose: true
            })
            this.$emit('closeEditTemplateDialog')
          })
        }
      })
    },
    handleFileChange(e) {
      const file = e.target.files[0]
      const reader = new FileReader()
      reader.onload = (res) => {
        const result = res.target.result
        this.importTemplateInfo = JSON.parse(result)
        this.templateInfo.name = this.importTemplateInfo.name
        this.templateInfo.templateStyle = this.importTemplateInfo.panelStyle
        this.templateInfo.templateData = this.importTemplateInfo.panelData
        this.templateInfo.snapshot = this.importTemplateInfo.snapshot
        this.templateInfo.dynamicData = this.importTemplateInfo.dynamicData
        this.templateInfo.nodeType = 'template'
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
