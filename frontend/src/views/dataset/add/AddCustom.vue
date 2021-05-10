<template>
  <el-col>
    <el-row style="height: 26px;">
      <span style="line-height: 26px;">
        {{ $t('dataset.add_custom_table') }}
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
        <el-form-item class="form-item">
          <el-input v-model="name" size="mini" :placeholder="$t('commons.name')" />
        </el-form-item>
      </el-form>
    </el-row>
    <el-col style="display: flex;flex-direction: row">
      <el-col class="panel-height" style="width: 220px;border-right:solid 1px #dcdfe6;border-top:solid 1px #dcdfe6;padding-right: 15px;overflow-y: auto;">
        <dataset-group-selector :mode="1" :checked-list="checkedList" :union-data="unionData" @getTable="getTable" />
      </el-col>
      <el-col class="panel-height" style="width: 235px;border-top:solid 1px #dcdfe6;padding: 0 15px;overflow-y: auto;">
        <dataset-custom-field :table="table" :checked-list="checkedList" @getChecked="getChecked" />
      </el-col>
      <el-col class="panel-height" style="flex: 1;overflow: scroll;">
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
      </el-col>
    </el-col>
  </el-col>
</template>

<script>
import { post } from '@/api/dataset/dataset'
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
      name: '自助数据集',
      table: {},
      checkedList: [],
      unionData: [],
      height: 500,
      data: [],
      fields: []
    }
  },
  watch: {
    'checkedList': function() {
      // console.log(this.checkedList)
      if (this.checkedList && this.checkedList.length > 0) {
        // 根据第一个选择的数据集找到关联视图
        post('dataset/union/listByTableId/' + this.checkedList[0].tableId, {}).then(response => {
          // console.log(response)
          this.unionData = response.data
        })
      } else {
        this.unionData = []
      }
    }
  },
  mounted() {
    window.onresize = () => {
      this.calHeight()
    }
    this.calHeight()
  },
  methods: {
    calHeight() {
      const that = this
      setTimeout(function() {
        const currentHeight = document.documentElement.clientHeight
        that.height = currentHeight - 56 - 15 - 26 - 25 - 43
      }, 10)
    },

    getTable(table) {
      // console.log(table)
      this.table = table
    },
    getChecked(tableCheckedField) {
      // console.log(tableCheckedField)
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
      // console.log(this.checkedList)
      // request to get data
      if (this.checkedList.length > 0) {
        const table = {
          id: this.param.tableId,
          name: this.name,
          sceneId: this.param.id,
          dataSourceId: null,
          type: 'custom',
          mode: 1,
          info: '{"list":' + JSON.stringify(this.checkedList) + '}'
        }
        post('/dataset/table/customPreview', table).then(response => {
          console.log(response)
          this.fields = response.data.fields
          this.data = response.data.data
          const datas = this.data
          this.$refs.plxTable.reloadData(datas)
        })
      }
    },
    save() {
      const table = {
        id: this.param.tableId,
        name: this.name,
        sceneId: this.param.id,
        dataSourceId: null,
        type: 'custom',
        mode: 1,
        info: '{"list":' + JSON.stringify(this.checkedList) + '}'
      }
      post('/dataset/table/update', table).then(response => {
        this.$store.dispatch('dataset/setSceneData', new Date().getTime())
        this.cancel()
      })
    },

    cancel() {
      this.dataReset()
      this.$emit('switchComponent', { name: '' })
    },

    dataReset() {

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

  .panel-height{
    height: calc(100vh - 56px - 15px - 26px - 25px - 43px);
  }
</style>
