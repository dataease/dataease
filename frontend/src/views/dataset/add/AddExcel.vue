<template>
  <el-col>
    <el-row>
      <el-row style="height: 26px;">
        <span style="line-height: 26px;">
          {{ param.tableId?$t('dataset.edit_excel_table'):$t('dataset.add_excel_table') }}
        </span>
        <el-row style="float: right">
          <el-button size="mini" @click="cancel">
            {{ $t('dataset.cancel') }}
          </el-button>
          <el-button :disabled="!name || fileList.length === 0" size="mini" type="primary" @click="save">
            {{ $t('dataset.confirm') }}
          </el-button>
        </el-row>
      </el-row>
      <el-divider />
      <el-row>
        <el-row>
          <el-col style="width: 500px;">
            <el-form :inline="true" size="mini" class="row-style">
              <el-form-item class="form-item">
                <el-input v-show="!param.tableId" v-model="name" :placeholder="$t('commons.name')" />
              </el-form-item>
              <el-form-item class="form-item">
                <el-upload
                  :action="baseUrl+'dataset/table/excel/upload'"
                  :multiple="false"
                  :show-file-list="false"
                  :file-list="fileList"
                  :data="param"
                  accept=".xls,.xlsx,"
                  :before-upload="beforeUpload"
                  :on-success="uploadSuccess"
                  :on-error="uploadFail"
                  name="file"
                  :headers="headers"
                >
                  <el-button size="mini" type="primary" :disabled="uploading">
                    <span v-if="!uploading" style="font-size: 12px;">{{ $t('dataset.upload_file') }}</span>
                    <span v-if="uploading" style="font-size: 12px;"><i class="el-icon-loading" /> {{ $t('dataset.uploading') }}</span>
                  </el-button>
                </el-upload>
              </el-form-item>
            </el-form>
          </el-col>
        </el-row>
      </el-row>

      <el-row style="margin-top: 10px;">
        <el-card class="box-card dataPreview" shadow="never">
          <div slot="header" class="clearfix">
            <span>{{ $t('dataset.data_preview') }}</span>
            <span style="font-size: 12px;color: #3d4d66;">（{{ $t('dataset.preview_100_data') }}）</span>
          </div>
          <div class="text item">
            <ux-grid
              ref="plxTable"
              size="mini"
              style="width: 100%;"
              :height="height"
              :checkbox-config="{highlight: true}"
              :width-resize="true"
            >
              <ux-table-column
                v-for="field in fields"
                :key="field.fieldName"
                min-width="200px"
                :field="field.fieldName"
                :title="field.remarks"
                :resizable="true"
              >
                <template slot="header" slot-scope="scope">
                  <span v-if="!param.tableId" style="display: flex;align-items: center;">
                    <span style="display: inline-block;font-size: 12px;">
                      <div style="display: inline-block;">
                        <el-select v-model="field.fieldType" size="mini" style="display: inline-block;width: 120px;">
                          <el-option
                            v-for="item in fieldOptions"
                            :key="item.value"
                            :label="item.label"
                            :value="item.value"
                          >
                            <span style="float: left">
                              <svg-icon v-if="item.value === 'TEXT'" icon-class="field_text" class="field-icon-text" />
                              <svg-icon v-if="item.value === 'DATETIME'" icon-class="field_time" class="field-icon-time" />
                              <svg-icon v-if="item.value === 'LONG' || item.value === 'DOUBLE'" icon-class="field_value" class="field-icon-value" />
                            </span>
                            <span style="float: left; color: #8492a6; font-size: 12px">{{ item.label }}</span>
                          </el-option>
                        </el-select>
                      </div>
                      <!--                      <span style="margin-left: 8px;">-->
                      <!--                        <span v-if="field.fieldType === 'TEXT'">-->
                      <!--                          <svg-icon v-if="field.fieldType === 'TEXT'" icon-class="field_text" class="field-icon-text" />-->
                      <!--                          <span class="field-class">{{ $t('dataset.text') }}</span>-->
                      <!--                        </span>-->
                      <!--                        <span v-if="field.fieldType === 'DATETIME'">-->
                      <!--                          <svg-icon v-if="field.fieldType === 'DATETIME'" icon-class="field_time" class="field-icon-time" />-->
                      <!--                          <span class="field-class">{{ $t('dataset.time') }}</span>-->
                      <!--                        </span>-->
                      <!--                        <span v-if="field.fieldType === 'LONG' || field.fieldType === 'DOUBLE'">-->
                      <!--                          <svg-icon v-if="field.fieldType === 'LONG' || field.fieldType === 'DOUBLE'" icon-class="field_value" class="field-icon-value" />-->
                      <!--                          <span v-if="field.fieldType === 'LONG'" class="field-class">{{ $t('dataset.value') }}</span>-->
                      <!--                          <span v-if="field.fieldType === 'DOUBLE'" class="field-class">{{ $t('dataset.value') + '(' + $t('dataset.float') + ')' }}</span>-->
                      <!--                        </span>-->
                      <!--                      </span>-->
                    </span>
                    <span style="font-size: 12px;margin-left: 10px;">
                      {{ field.remarks }}
                    </span>
                  </span>
                  <span v-else style="font-size: 12px;">
                    {{ field.remarks }}
                  </span>
                </template>
              </ux-table-column>
            </ux-grid>
          </div>
        </el-card>
      </el-row>
    </el-row>
  </el-col>
</template>

<script>
import { post } from '@/api/dataset/dataset'
import { getToken } from '@/utils/auth'
import i18n from '@/lang'

const token = getToken()

export default {
  name: 'AddExcel',
  props: {
    param: {
      type: Object,
      default: null
    },
    tableId: {
      type: String,
      default: null
    }
  },
  data() {
    return {
      name: '',
      fields: [],
      sheets: [],
      data: [],
      mode: '1',
      height: 600,
      fileList: [],
      headers: { Authorization: token, 'Accept-Language': i18n.locale.replace('_', '-') },
      baseUrl: process.env.VUE_APP_BASE_API,
      path: '',
      uploading: false,
      fieldOptions: [
        { label: this.$t('dataset.text'), value: 'TEXT' },
        { label: this.$t('dataset.time'), value: 'DATETIME' },
        { label: this.$t('dataset.value'), value: 'LONG' },
        { label: this.$t('dataset.value') + '(' + this.$t('dataset.float') + ')', value: 'DOUBLE' }
      ]
    }
  },
  watch: {
  },
  mounted() {
    // this.initDataSource()
    window.onresize = () => {
      this.calHeight()
    }
    this.calHeight()
  },
  created() {
    if (!this.param.tableId) {
      this.param.tableId = ''
    }
  },
  methods: {
    // initDataSource() {
    //   listDatasource().then(response => {
    //     this.options = response.data
    //   })
    // },
    calHeight() {
      const that = this
      setTimeout(function() {
        const currentHeight = document.documentElement.clientHeight
        that.height = currentHeight - 56 - 30 - 26 - 25 - 35 - 10 - 37 - 20 - 10
      }, 10)
    },
    beforeUpload(file) {
      this.uploading = true
    },
    uploadFail(response, file, fileList) {
      let myError = response.toString()
      myError = myError.replace('Error: ', '')
      const errorMessage = JSON.parse(myError).message + ', ' + this.$t('dataset.parse_error')

      this.path = ''
      this.fields = []
      this.sheets = []
      this.data = []
      const datas = this.data
      this.$refs.plxTable.reloadData(datas)
      this.name = ''
      this.fileList = []
      this.uploading = false
      this.$message({
        type: 'error',
        message: errorMessage,
        showClose: true
      })
    },
    uploadSuccess(response, file, fileList) {
      this.path = response.data.path
      this.fields = response.data.fields
      this.sheets = response.data.sheets
      if (this.sheets.length > 1) {
        this.$warning(this.$t('dataset.sheet_warn'))
      }
      this.data = response.data.data
      const datas = this.data
      this.$refs.plxTable.reloadData(datas)

      if (file.name.lastIndexOf('.') > 0) {
        this.name = file.name.substring(0, file.name.lastIndexOf('.'))
      }
      this.fileList = fileList
      this.uploading = false
    },

    save() {
      if (!this.name || this.name === '') {
        this.$message({
          showClose: true,
          message: this.$t('dataset.pls_input_name'),
          type: 'error'
        })
        return
      }
      if (this.name.length > 50) {
        this.$message({
          showClose: true,
          message: this.$t('dataset.char_can_not_more_50'),
          type: 'error'
        })
        return
      }
      let table = {}
      if (!this.param.tableId) {
        table = {
          id: this.param.tableId,
          name: this.name,
          sceneId: this.param.id,
          dataSourceId: null,
          type: 'excel',
          mode: parseInt(this.mode),
          // info: '{"data":"' + this.path + '"}',
          info: JSON.stringify({ data: this.path, sheets: [this.sheets[0]] }),
          fields: this.fields
        }
      } else {
        table = {
          id: this.param.tableId,
          name: this.param.table.name,
          sceneId: this.param.id,
          dataSourceId: null,
          type: 'excel',
          mode: parseInt(this.mode),
          // info: '{"data":"' + this.path + '"}',
          info: JSON.stringify({ data: this.path, sheets: [this.sheets[0]] }),
          editType: this.param.editType ? this.param.editType : 0
        }
      }
      post('/dataset/table/update', table).then(response => {
        // this.$store.dispatch('dataset/setSceneData', new Date().getTime())
        this.$emit('saveSuccess', table)
        this.cancel()
      })
    },

    cancel() {
      this.dataReset()
      // this.$router.push('/dataset/home')
      if (this.param.tableId) {
        this.$emit('switchComponent', { name: 'ViewTable', param: this.param.table })
      } else {
        this.$emit('switchComponent', { name: '' })
      }
    },

    dataReset() {
      this.searchTable = ''
      this.options = []
      this.dataSource = ''
      this.tables = []
      this.checkTableList = []
    }
  }

}
</script>

<style scoped>
  .el-divider--horizontal {
    margin: 12px 0;
  }

  .form-item {
    margin-bottom: 6px !important;
  }

  .el-checkbox {
    margin-bottom: 14px;
    margin-left: 0;
    margin-right: 14px;
  }

  .el-checkbox.is-bordered + .el-checkbox.is-bordered {
    margin-left: 0;
  }

  span{
    font-size: 14px;
  }

  .row-style>>>.el-form-item__label{
    font-size: 12px;
  }

  .dataPreview>>>.el-card__header{
    padding: 6px 8px;
  }

  .dataPreview>>>.el-card__body{
    padding:10px;
  }
</style>
