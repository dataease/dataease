<template>
  <div
    v-loading="$store.getters.loadingMap[$store.getters.currentPath]"
    class="template-import"
  >
    <el-form
      ref="templateImportForm"
      class="de-form-item"
      :model="templateInfo"
      :rules="templateInfoRules"
    >
      <el-form-item
        :label="$t('app_template.app_name')"
        prop="name"
      >
        <div class="flex-template">
          <el-input
            v-model="templateInfo.name"
            clearable
            size="small"
          />
          <deBtn
            style="margin-left: 10px"
            class="el-icon-upload2"
            secondary
            @click="goFile"
          >
            {{ $t('app_template.app_upload') }}
          </deBtn>
          <input
            id="input"
            ref="files"
            type="file"
            accept=".zip"
            hidden
            @change="handleFileChange"
          >
        </div>
      </el-form-item>
    </el-form>
    <el-row
      class="preview"
      :style="classBackground"
    />
    <el-row class="de-root-class">
      <deBtn
        secondary
        @click="cancel()"
      >{{
        $t('commons.cancel')
      }}
      </deBtn>
      <deBtn
        type="primary"
        @click="save()"
      >{{
        $t('commons.confirm')
      }}
      </deBtn>
    </el-row>
  </div>
</template>

<script>
import { save, update, nameCheck } from '@/api/system/appTemplate'
import msgCfm from '@/components/msgCfm/index'
import { find } from '@/api/system/template'
import { imgUrlTrans } from '@/components/canvas/utils/utils'
import JSZip from 'jszip'

export default {
  mixins: [msgCfm],
  props: {
    pid: {
      type: String,
      required: true
    },
    optType: {
      type: String,
      required: false
    },
    appTemplateInfo: {
      type: Object,
      required: false
    }
  },
  data() {
    return {
      appResultInfo: {},
      importTemplateInfo: {
        snapshot: ''
      },
      templateInfoRules: {
        name: [
          {
            required: true,
            message: this.$t('commons.input_content'),
            trigger: 'change'
          }
        ]
      },
      recover: false,
      templateInfo: {
        id: null,
        level: '1',
        pid: this.pid,
        name: '',
        templateStyle: null,
        templateData: null,
        dynamicData: null,
        staticResource: null,
        snapshot: ''
      }
    }
  },
  computed: {
    classBackground() {
      if (this.templateInfo.snapshot) {
        return {
          background: `url(${imgUrlTrans(this.templateInfo.snapshot)}) no-repeat`
        }
      } else {
        return {}
      }
    }
  },
  mounted() {
    if (this.optType === 'update') {
      this.templateInfo.id = this.appTemplateInfo.id
      this.templateInfo.name = this.appTemplateInfo.name
      this.templateInfo.snapshot = this.appTemplateInfo.snapshot
      this.templateInfo.pid = this.appTemplateInfo.pid
      this.templateInfo.level = 1
    }
  },
  created() {
    this.showCurrentTemplate(this.pid)
  },
  methods: {
    showCurrentTemplate(pid) {
      find({ pid }).then((response) => {
        this.nameList = response.data
      })
    },
    cancel() {
      this.$emit('closeEditTemplateDialog')
    },
    save() {
      if (!this.templateInfo.name) {
        this.$warning(this.$t('chart.name_can_not_empty'))
        return false
      }
      if (this.optType !== 'update' && !this.templateInfo.templateData) {
        this.$warning(this.$t('chart.template_can_not_empty'))
        return false
      }
      const nameCheckRequest = {
        pid: this.templateInfo.pid,
        name: this.templateInfo.name,
        optType: 'insert'
      }
      if (this.optType === 'update') {
        nameCheckRequest.optType = 'update'
        nameCheckRequest['id'] = this.templateInfo.id
        this.appResultInfo['id'] = this.templateInfo.id
        this.appResultInfo['optType'] = 'update'
      }
      this.appResultInfo['pid'] = this.templateInfo.pid
      this.appResultInfo['level'] = this.templateInfo.level
      this.appResultInfo['name'] = this.templateInfo.name
      this.appResultInfo['snapshot'] = this.templateInfo.snapshot
      const method = this.optType === 'update' ? update : save
      const successMsg = this.optType === 'update' ? '更新成功' : this.$t('system_parameter_setting.import_succeeded')
      const _this = this
      nameCheck(nameCheckRequest).then((response) => {
        if (response.data.indexOf('exist') > -1) {
          this.$message({
            message: '当前名称已经存在',
            type: 'error',
            showClose: true
          })
        } else {
          method(_this.appResultInfo).then((response) => {
            this.openMessageSuccess(successMsg)
            this.$emit('refresh')
            this.$emit('closeEditTemplateDialog')
          })
        }
      })
    },
    handleFileChange(e) {
      const jsZip = new JSZip()
      const file = e.target.files[0]
      const _this = this
      jsZip.loadAsync(file).then(function(file) {
        jsZip.file('DATA_RELATION.DE').async('string').then(function(content) {
          _this.appResultInfo = { ...JSON.parse(content), ..._this.appResultInfo }
        })
        jsZip.file('APP.json').async('string').then(function(content) {
          _this.appResultInfo['applicationInfo'] = content
          const appInfo = JSON.parse(content)
          _this.templateInfo.name = appInfo.appName
        })
        jsZip.file('TEMPLATE.DET').async('string').then(function(content) {
          _this.appResultInfo['panelInfo'] = content
          _this.importTemplateInfo = JSON.parse(content)
          _this.templateInfo.templateStyle = _this.importTemplateInfo.panelStyle
          _this.templateInfo.templateData = _this.importTemplateInfo.panelData
          _this.templateInfo.snapshot = _this.importTemplateInfo.snapshot
          _this.templateInfo.dynamicData = _this.importTemplateInfo.dynamicData
          _this.templateInfo.staticResource = _this.importTemplateInfo.staticResource
          _this.templateInfo.nodeType = 'template'
        })
      }).catch(() => {
        _this.$warning(this.$t('app_template.file_error_tips'))
      })
    },
    goFile() {
      this.$refs.files.click()
    }
  }
}
</script>

<style scoped>
.my_table ::v-deep .el-table__row > td {
  /* 去除表格线 */
  border: none;
  padding: 0 0;
}

.my_table ::v-deep .el-table th.is-leaf {
  /* 去除上边框 */
  border: none;
}

.my_table ::v-deep .el-table::before {
  /* 去除下边框 */
  height: 0;
}

.de-root-class {
  margin-top: 24px;
  text-align: right;
}

.preview {
  margin-top: -12px;
  border: 1px solid #e6e6e6;
  height: 300px !important;
  overflow: auto;
  background-size: 100% 100% !important;
  border-radius: 4px;
}

.preview-show {
  border-left: 1px solid #e6e6e6;
  height: 300px;
  background-size: 100% 100% !important;
}
</style>

<style lang="scss">
.flex-template {
  width: 100%;
  display: flex;
  align-items: center;
  justify-content: space-between;

  .el-input {
    margin-right: 2px;
    flex: 1;
  }
}
</style>
