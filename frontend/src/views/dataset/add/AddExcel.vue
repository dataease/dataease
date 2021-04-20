<template>
  <el-col>
    <el-row>
      <el-row style="height: 26px;">
        <span style="line-height: 26px;">
          {{ $t('dataset.add_excel_table') }}
        </span>
        <el-row style="float: right">
          <el-button size="mini" @click="cancel">
            {{ $t('dataset.cancel') }}
          </el-button>
          <el-button :disabled="!form.name || fileList.length === 0" size="mini" type="primary" @click="save">
            {{ $t('dataset.confirm') }}
          </el-button>
        </el-row>
      </el-row>
      <el-divider />
      <el-row>
        <el-row>
          <el-col style="width: 500px;">
            <el-form :model="form" :inline="true" size="mini" class="row-style">
              <el-form-item>
                <el-input v-model="form.name" :placeholder="$t('commons.name')" />
              </el-form-item>
              <el-form-item>
                <el-upload
                  :action="baseUrl+'dataset/table/excel/upload'"
                  :multiple="false"
                  :show-file-list="false"
                  :file-list="fileList"
                  accept=".xls,.xlsx,.csv"
                  :on-success="uploadSuccess"
                  name="file"
                  :headers="headers"
                >
                  <el-button size="mini" type="primary">{{ $t('dataset.upload_file') }}</el-button>
                </el-upload>
              </el-form-item>
            </el-form>
          </el-col>
        </el-row>
      </el-row>

      <el-row>
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
      form: {
        name: ''
      },
      fields: [],
      data: [],
      mode: '1',
      height: 600,
      fileList: [],
      headers: { Authorization: token },
      baseUrl: process.env.VUE_APP_BASE_API
    }
  },
  watch: {
    // dataSource(val) {
    //   if (val) {
    //     post('/datasource/getTables', { id: val }).then(response => {
    //       this.tables = response.data
    //       this.tableData = JSON.parse(JSON.stringify(this.tables))
    //     })
    //   }
    // },
    // searchTable(val) {
    //   if (val && val !== '') {
    //     this.tableData = JSON.parse(JSON.stringify(this.tables.filter(ele => { return ele.includes(val) })))
    //   } else {
    //     this.tableData = JSON.parse(JSON.stringify(this.tables))
    //   }
    // }
  },
  mounted() {
    // this.initDataSource()
  },
  activated() {
    // this.initDataSource()
  },
  methods: {
    // initDataSource() {
    //   listDatasource().then(response => {
    //     this.options = response.data
    //   })
    // },
    uploadSuccess(response, file, fileList) {
      console.log(response)
      console.log(file)
      console.log(fileList)

      this.fields = response.data.fields
      this.data = response.data.data
      const datas = this.data
      this.$refs.plxTable.reloadData(datas)

      if (file.name.lastIndexOf('.') > 0) {
        this.form.name = file.name.substring(0, file.name.lastIndexOf('.'))
      }
      this.fileList = fileList
    },

    save() {
      // console.log(this.checkTableList);
      // console.log(this.scene);
      const sceneId = this.param.id
      const dataSourceId = this.dataSource
      const tables = []
      const mode = this.mode
      this.checkTableList.forEach(function(name) {
        tables.push({
          name: name,
          sceneId: sceneId,
          dataSourceId: dataSourceId,
          type: 'excel',
          mode: parseInt(mode)
        })
      })
      post('/dataset/table/batchAdd', tables).then(response => {
        this.$store.dispatch('dataset/setSceneData', new Date().getTime())
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
    margin-bottom: 6px;
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
