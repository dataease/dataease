<template>
  <el-col>
    <el-row>
      <el-row style="height: 26px;" class="title-text">
        <span style="line-height: 26px;">
          {{ param.tableId?$t('dataset.edit_excel_table'):$t('dataset.add_excel_table') }}
        </span>
        <span style="line-height: 26px;">
          <el-tooltip class="item" effect="dark" content="Right Bottom 提示文字" placement="bottom">
            <div slot="content">
              {{ $t('dataset.excel_info_1') }}<br>
              {{ $t('dataset.excel_info_2') }}<br>
              {{ $t('dataset.excel_info_3') }}
            </div>
            <i class="el-icon-info" style="cursor: pointer;" />
          </el-tooltip>
        </span>
        <el-row style="float: right">
          <el-button size="mini" @click="cancel">
            {{ $t('dataset.cancel') }}
          </el-button>
          <el-button size="mini" type="primary" @click="save">
            {{ $t('dataset.confirm') }}
          </el-button>
        </el-row>
      </el-row>
      <el-divider />

      <el-row style="margin-top: 10px;">

        <el-container>
          <el-aside width="200px">
            <el-row>
              <el-col style="width: 200px;">
                <el-form :inline="true" size="mini" class="row-style">
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

            <el-tree
              ref="tree"
              :data="excelData"
              :default-expanded-keys="defaultExpandedKeys"
              :default-checked-keys="defaultCheckedKeys"
              node-key="id"
              :props="props"
              show-checkbox
              highlight-current
              @node-click="handleNodeClick"
              @check-change="handleCheckChange"
            />

          </el-aside>

          <el-container>
            <el-header style="text-align: left;" height="30px">
              <el-row>
                <el-col style="width: 500px;">
                  <el-form :inline="true" size="mini" class="row-style">
                    <el-form-item v-show="!param.tableId" class="form-item" :label="$t('dataset.name')">
                      <el-input v-model="sheetObj.datasetName" :placeholder="$t('commons.name')" @change="changeDatasetName" />
                    </el-form-item>
                    <el-form-item>
                      <div>
                        <span>{{ $t('dataset.data_preview') }}</span>
                        <span class="limit-length-data">（{{ $t('dataset.preview_100_data') }}）</span>
                      </div>
                    </el-form-item>
                  </el-form>
                </el-col>
              </el-row>
            </el-header>
            <el-main>

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
                    v-for="field in sheetObj.fields"
                    :key="field.fieldName"
                    min-width="200px"
                    :field="field.fieldName"
                    :title="field.remarks"
                    :resizable="true"
                  >
                    <template slot="header" slot-scope="scope">
                      <span style="display: flex;align-items: center;">
                        <span style="display: inline-block;font-size: 12px;">
                          <div style="display: inline-block;">
                            <el-select v-model="field.fieldType" size="mini" style="display: inline-block;width: 120px;" @change="changeDatasetName">
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
                        </span>
                        <span style="font-size: 12px;margin-left: 10px;">
                          {{ field.remarks }}
                        </span>
                      </span>
                      <!--                      <span v-else style="font-size: 12px;">-->
                      <!--                        {{ field.remarks }}-->
                      <!--                      </span>-->
                    </template>
                  </ux-table-column>
                </ux-grid>
              </div>
            </el-main>
          </el-container>
        </el-container>
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
      default: ''
    },
    editType: {
      type: Number,
      default: 0
    }
  },
  data() {
    return {
      sheetObj: { datasetName: ' ', fields: [] },
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
      ],
      props: {
        label: 'excelLable',
        children: 'sheets'
      },
      count: 1,
      excelData: [],
      defaultExpandedKeys: [],
      defaultCheckedKeys: []
    }
  },
  watch: {
  },
  mounted() {
    window.onresize = () => {
      this.calHeight()
    }
    this.calHeight()
  },
  created() {
    if (!this.param.tableId) {
      this.param.tableId = ''
    }
    if (!this.param.editType) {
      this.param.editType = 0
    }
  },
  methods: {
    handleCheckChange(data, checked, indeterminate) {
      if (checked) {
        this.defaultCheckedKeys.push(data.id)
        this.handleNodeClick(data)
      } else {
        var index = this.defaultCheckedKeys.findIndex(id => {
          if (id == data.id) {
            return true
          }
        })
        this.defaultCheckedKeys.splice(index, 1)
      }
    },
    handleNodeClick(data) {
      if (data.sheet) {
        this.sheetObj = data
        this.fields = data.fields
        this.jsonArray = data.jsonArray
        const datas = this.jsonArray
        this.$refs.plxTable.reloadData(datas)
      }
    },
    changeDatasetName() {
      for (var i = 0; i < this.excelData.length; i++) {
        if (this.excelData[i].excelId == this.sheetObj.sheetExcelId) {
          for (var j = 0; j < this.excelData[i].sheets.length; j++) {
            if (this.excelData[i].sheets[j].excelId == this.sheetObj.sheetId) {
              this.excelData[i].sheets[j] = this.sheetObj
            }
          }
        }
      }
    },
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
      this.fileList = []
      this.uploading = false
      this.$message({
        type: 'error',
        message: errorMessage,
        showClose: true
      })
    },
    uploadSuccess(response, file, fileList) {
      this.uploading = false
      this.excelData.push(response.data)
      this.defaultExpandedKeys.push(response.data.id)
      this.defaultCheckedKeys.push(response.data.sheets[0].id)
      this.$nextTick(() => {
        this.$refs.tree.setCheckedKeys(this.defaultCheckedKeys)
      })
      this.fileList = fileList
    },

    save() {
      var validate = true
      var selectedSheet = []
      var sheetFileMd5 = []
      var selectNode = this.$refs.tree.getCheckedNodes()
      for (var i = 0; i < selectNode.length; i++) {
        if (selectNode[i].sheet) {
          if (!selectNode[i].datasetName || selectNode[i].datasetName === '') {
            validate = false
            this.$message({
              showClose: true,
              message: this.$t('dataset.pls_input_name'),
              type: 'error'
            })
            return
          }
          if (selectNode[i].datasetName.length > 50) {
            validate = false
            this.$message({
              showClose: true,
              message: this.$t('dataset.char_can_not_more_50'),
              type: 'error'
            })
            return
          }
          selectedSheet.push(selectNode[i])
          sheetFileMd5.push(selectNode[i].fieldsMd5)
        }
      }
      if (selectedSheet.length == 0) {
        this.$message.warning(this.$t('dataset.ple_select_excel'))
        return
      }
      if (!validate) {
        return
      }

      let table = {}
      if (!this.param.tableId) {
        table = {
          id: this.param.tableId,
          sceneId: this.param.id,
          dataSourceId: null,
          type: 'excel',
          sheets: selectedSheet,
          mode: parseInt(this.mode),
          editType: 0
        }
      } else {
        table = {
          id: this.param.tableId,
          name: this.param.table.name,
          sceneId: this.param.id,
          dataSourceId: null,
          type: 'excel',
          sheets: selectedSheet,
          mode: parseInt(this.mode),
          editType: this.param.editType ? this.param.editType : 0
        }
      }
      if (new Set(sheetFileMd5).size !== sheetFileMd5.length && !this.param.tableId) {
        this.$confirm(this.$t('dataset.merge_msg'), this.$t('dataset.merge_title'), {
          distinguishCancelAndClose: true,
          confirmButtonText: this.$t('dataset.merge'),
          cancelButtonText: this.$t('dataset.no_merge'),
          type: 'info'
        }).then(() => {
          table.mergeSheet = true
          post('/dataset/table/update', table).then(response => {
            this.$emit('saveSuccess', table)
            this.cancel()
          })
        }).catch(action => {
          if (action === 'close') {
            return
          }
          table.mergeSheet = false
          post('/dataset/table/update', table).then(response => {
            this.$emit('saveSuccess', table)
            this.cancel()
          })
        })
      } else {
        post('/dataset/table/update', table).then(response => {
          this.$emit('saveSuccess', table)
          this.cancel()
        })
      }
    },
    cancel() {
      this.dataReset()
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

  .el-header {
    background-color: var(--ContentBG, rgb(241, 243, 248));
    color: var(--TextActive, #333);
    line-height: 30px;
  }

  .el-main {
    padding: 0px
  }

  .limit-length-data {
      font-size: 12px;
      color: var(--TableColor, #3d4d66);
  }
</style>
