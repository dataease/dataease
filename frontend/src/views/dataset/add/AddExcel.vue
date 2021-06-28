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
              />
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

const token = getToken()

export default {
  name: 'AddExcel',
  props: {
    param: {
      type: Object,
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
      headers: { Authorization: token },
      baseUrl: process.env.VUE_APP_BASE_API,
      path: '',
      uploading: false
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
        message: this.$t('dataset.parse_error'),
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
      // console.log(this.checkTableList);
      // console.log(this.scene);
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
          info: JSON.stringify({ data: this.path })
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
          info: JSON.stringify({ data: this.path }),
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
      this.$emit('switchComponent', { name: '' })
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
