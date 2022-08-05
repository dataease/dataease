<template>
  <el-col>
    <el-row style="height: 26px;" class="title-text">
      <span style="line-height: 26px;">
        {{ param.tableId?$t('dataset.edit_custom_table'):$t('dataset.add_custom_table') }}
      </span>
      <el-row style="float: right">
        <el-button size="mini" @click="cancel">
          {{ $t('dataset.cancel') }}
        </el-button>
        <el-button :disabled="!name || checkedList.length === 0" size="mini" type="primary" @click="save">
          {{ $t('dataset.confirm') }}
        </el-button>
      </el-row>
    </el-row>
    <el-divider />
    <el-row>
      <el-form :inline="true">
        <el-form-item class="form-item" :label="$t('commons.name')">
          <el-input v-model="name" size="mini" :placeholder="$t('commons.name')" />
        </el-form-item>
      </el-form>
    </el-row>
    <el-col style="display: flex;flex-direction: row">
      <el-col class="panel-height" style="width: 220px;border-right:solid 1px #dcdfe6;border-top:solid 1px #dcdfe6;padding-right: 15px;overflow-y: auto;">
        <dataset-group-selector :custom-type="customType" :table="table" :checked-list="checkedList" :union-data="unionData" @getTable="getTable" />
      </el-col>
      <el-col class="panel-height" style="width: 235px;border-top:solid 1px #dcdfe6;padding: 0 15px;overflow-y: auto;">
        <dataset-custom-field :table="table" :checked-list="checkedList" @getChecked="getChecked" />
      </el-col>
      <el-col class="panel-height" style="flex: 1;overflow: hidden;">
        <el-card class="box-card dataPreview" shadow="never">
          <div slot="header" class="clearfix">
            <span style="font-size: 16px;">{{ $t('dataset.data_preview') }}</span>
          </div>
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
          <span class="table-count">
            {{ $t('dataset.preview_show') }}
            <span class="span-number">1000</span>
            {{ $t('dataset.preview_item') }}
          </span>
        </el-card>
      </el-col>
    </el-col>
  </el-col>
</template>

<script>
import { post, getTable } from '@/api/dataset/dataset'
import DatasetGroupSelector from '../common/DatasetGroupSelector'
import DatasetCustomField from '../common/DatasetCustomField'

export default {
  name: 'AddCustom',
  components: { DatasetCustomField, DatasetGroupSelector },
  props: {
    param: {
      type: Object,
      default: null
    }
  },
  data() {
    return {
      name: '自定义数据集',
      table: {},
      checkedList: [],
      unionData: [],
      height: 500,
      data: [],
      fields: [],
      customType: ['db', 'sql', 'excel', 'api']
    }
  },
  watch: {
    'param.tableId': {
      handler: function() {
        this.resetComponent()
      }
    },
    'checkedList': function() {
      this.getUnionData()
    }
  },
  mounted() {
    window.onresize = () => {
      this.calHeight()
    }
    this.calHeight()

    if (this.param && this.param.id && this.param.tableId) {
      this.getCustomTable()
      this.getUnionData()
    }
  },
  methods: {
    calHeight() {
      const that = this
      setTimeout(function() {
        const currentHeight = document.documentElement.clientHeight
        that.height = currentHeight - 56 - 15 - 26 - 25 - 43 - 16 - 37 - 20 - 10
      }, 10)
    },

    getTable(table) {
      this.table = table
    },
    getChecked(tableCheckedField) {
      if (tableCheckedField.checkedFields && tableCheckedField.checkedFields.length > 0) {
        if (!this.checkedList.some(ele => ele.tableId === tableCheckedField.tableId)) {
          this.checkedList.push(tableCheckedField)
        } else {
          this.checkedList.forEach(ele => {
            if (ele.tableId === tableCheckedField.tableId) {
              ele.checkedFields = tableCheckedField.checkedFields
            }
          })
        }
      } else {
        let index = -1
        for (let i = 0; i < this.checkedList.length; i++) {
          if (this.checkedList[i].tableId === tableCheckedField.tableId) {
            index = i
            break
          }
        }
        if (index > -1) {
          this.checkedList.splice(index, 1)
        }
      }
      this.getData()
    },
    getData() {
      // request to get data
      if (this.checkedList.length > 0) {
        const table = {
          id: this.param.tableId,
          name: this.name,
          sceneId: this.param.id,
          dataSourceId: this.param.tableId ? this.param.table.dataSourceId : this.table.dataSourceId,
          type: 'custom',
          mode: this.param.tableId ? this.param.table.mode : this.table.mode,
          info: '{"list":' + JSON.stringify(this.checkedList) + '}'
        }
        post('/dataset/table/customPreview', table).then(response => {
          this.fields = response.data.fields
          this.data = response.data.data
          const datas = this.data
          this.$refs.plxTable.reloadData(datas)
        })
      } else {
        this.fields = []
        this.data = []
        const datas = this.data
        this.$refs.plxTable.reloadData(datas)
      }
    },
    getUnionData() {
      if (this.checkedList && this.checkedList.length > 0) {
        // 根据第一个选择的数据集找到关联视图
        post('dataset/union/listByTableId/' + this.checkedList[0].tableId, {}).then(response => {
          this.unionData = response.data
        })
      } else {
        this.unionData = []
      }
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
      const table = {
        id: this.param.tableId,
        name: this.name,
        sceneId: this.param.id,
        dataSourceId: this.table.dataSourceId,
        type: 'custom',
        mode: this.table.mode,
        info: '{"list":' + JSON.stringify(this.checkedList) + '}'
      }
      post('/dataset/table/update', table).then(response => {
        // this.$store.dispatch('dataset/setSceneData', new Date().getTime())
        this.$emit('saveSuccess', table)
        this.cancel()
      })
    },

    getCustomTable() {
      getTable(this.param.tableId).then(response => {
        const table = response.data
        this.name = table.name
        this.checkedList = JSON.parse(table.info).list

        this.getCheckTable(this.checkedList[0].tableId)
        this.getData()
      })
    },
    getCheckTable(tableId) {
      getTable(tableId).then(response => {
        this.table = response.data
      })
    },

    cancel() {
      // this.dataReset()
      if (this.param.tableId) {
        this.$emit('switchComponent', { name: 'ViewTable', param: this.param.table })
      } else {
        this.$emit('switchComponent', { name: '' })
      }
    },

    resetComponent() {
      this.name = '自定义数据集'
      this.table = {}
      this.checkedList = []
      this.unionData = []
      this.data = []
      this.fields = []
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

  .dataPreview ::v-deep .el-card__header{
    padding: 0 8px 12px;
  }

  .dataPreview ::v-deep .el-card__body{
    padding:10px;
  }

  span{
    font-size: 14px;
  }

  .panel-height{
    height: calc(100vh - 56px - 15px - 26px - 25px - 43px);
  }

  .blackTheme .panel-height{
    height: calc(100vh - 56px - 15px - 26px - 25px - 43px);
    border-color: var(--TableBorderColor) !important;
  }

  .span-number{
    color: #0a7be0;
  }
  .table-count{
    color: #606266;
  }
</style>
