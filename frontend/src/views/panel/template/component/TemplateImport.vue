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
        :label="$t('system_parameter_setting.template_name')"
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
          >{{ $t("panel.upload_template") }}</deBtn>
          <input
            id="input"
            ref="files"
            type="file"
            accept=".DET"
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
        $t("commons.cancel")
      }}</deBtn>
      <deBtn
        type="primary"
        @click="save()"
      >{{
        $t("commons.confirm")
      }}</deBtn>
    </el-row>
  </div>
</template>

<script>
import { save, nameCheck } from '@/api/system/template'
import msgCfm from '@/components/msgCfm/index'
import { find } from '@/api/system/template'
import { imgUrlTrans } from '@/components/canvas/utils/utils'

export default {
  mixins: [msgCfm],
  props: {
    pid: {
      type: String,
      required: true
    }
  },
  data() {
    return {
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
      if (this.importTemplateInfo.snapshot) {
        return {
          background: `url(${imgUrlTrans(this.importTemplateInfo.snapshot)}) no-repeat`
        }
      } else {
        return {}
      }
    }
  },
  created() {
    this.showCurrentTemplate(this.pid)
  },
  methods: {
    // selectRecover() {
    //   this.recover = true;
    //   this.templateInfo
    //   this.$refs.templateImportForm.clearValidate();
    // },
    // validator(rule, value, callback) {
    //   if (this.nameRepeat(value)) {

    //     callback(new Error(this.$t('commons.already_exists')));
    //   } else {
    //     callback();
    //   }
    // },
    showCurrentTemplate(pid) {
      find({ pid }).then((response) => {
        this.nameList = response.data
      })
    },
    // nameRepeat(value) {
    //   if (!this.nameList || this.nameList.length === 0 || this.recover) {
    //     return false;
    //   }
    //   return this.nameList.some((ele) => ele.name === value);
    // },
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
      nameCheck(nameCheckRequest).then((response) => {
        if (response.data.indexOf('exist') > -1) {
          const options = {
            title: 'commons.prompt',
            content: 'system_parameter_setting.to_overwrite_them',
            type: 'primary',
            cb: () => save(this.templateInfo).then((response) => {
              this.openMessageSuccess('system_parameter_setting.import_succeeded')
              this.$emit('refresh')
              this.$emit('closeEditTemplateDialog')
            }),
            confirmButtonText: this.$t('template.override')
          }
          this.handlerConfirm(options)
        } else {
          save(this.templateInfo).then((response) => {
            this.openMessageSuccess('system_parameter_setting.import_succeeded')
            this.$emit('refresh')
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
        this.templateInfo.staticResource =
          this.importTemplateInfo.staticResource
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
