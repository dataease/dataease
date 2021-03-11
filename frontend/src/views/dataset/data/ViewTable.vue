<template>
  <el-col>
    <el-row>
      <el-row style="height: 26px;">
        <span v-show="false">{{ tableId }}</span>
        <span style="line-height: 26px;">
          {{ table.name }}
        </span>
        <el-row style="float: right">
          <el-button size="mini" @click="edit">
            {{ $t('dataset.edit') }}
          </el-button>
          <!--        <el-button size="mini" type="primary" @click="createChart">-->
          <!--          {{$t('dataset.create_view')}}-->
          <!--        </el-button>-->
        </el-row>
      </el-row>
      <el-divider />

      <el-tabs v-model="tabActive">
        <el-tab-pane :label="$t('dataset.data_preview')" name="dataPreview">
          <tab-data-preview :table="table" :fields="fields" :data="data" />
        </el-tab-pane>
        <el-tab-pane :label="$t('dataset.join_view')" name="joinView">
          关联视图 TODO
        </el-tab-pane>
        <el-tab-pane v-if="table.mode === 1" :label="$t('dataset.update_info')" name="updateInfo">
          <update-info :table="table" />
        </el-tab-pane>
      </el-tabs>

      <el-dialog :title="table.name" :visible.sync="editField" :fullscreen="true" :show-close="false">
        <el-table :data="tableFields" size="mini" max-height="600px">
          <el-table-column property="type" :label="$t('dataset.field_type')" width="100">
            <template slot-scope="scope">
              <span v-if="scope.row.deType === 0">{{ $t('dataset.text') }}</span>
              <span v-if="scope.row.deType === 1">{{ $t('dataset.time') }}</span>
              <span v-if="scope.row.deType === 2">{{ $t('dataset.value') }}</span>
            </template>
          </el-table-column>
          <el-table-column property="name" :label="$t('dataset.field_name')" width="180">
            <template slot-scope="scope">
              <el-input v-model="scope.row.name" size="mini" />
            </template>
          </el-table-column>
          <el-table-column property="originName" :label="$t('dataset.field_origin_name')" width="180" />
          <el-table-column property="checked" :label="$t('dataset.field_check')" width="80">
            <template slot-scope="scope">
              <el-checkbox v-model="scope.row.checked" />
            </template>
          </el-table-column>
          <!--下面这一列占位-->
          <el-table-column property="" />
        </el-table>
        <div slot="footer" class="dialog-footer">
          <el-button size="mini" @click="closeEdit">{{ $t('dataset.cancel') }}</el-button>
          <el-button type="primary" size="mini" @click="saveEdit">{{ $t('dataset.confirm') }}</el-button>
        </div>
      </el-dialog>

      <!--    <el-dialog title="view" :visible.sync="createViewDialog" :fullscreen="true">-->
      <!--      <chart-edit/>-->
      <!--    </el-dialog>-->
    </el-row>
  </el-col>
</template>

<script>
import { getTable, getPreviewData, fieldList, batchEdit } from '@/api/dataset/dataset'
import TabDataPreview from './TabDataPreview'
import UpdateInfo from './UpdateInfo'

export default {
  name: 'ViewTable',
  components: { UpdateInfo, TabDataPreview },
  data() {
    return {
      createViewDialog: false,
      editField: false,
      table: {
        name: ''
      },
      fields: [],
      data: [],
      tabActive: 'dataPreview',
      tableFields: []
    }
  },
  computed: {
    tableId() {
      this.initTable(this.$store.state.dataset.table)
      return this.$store.state.dataset.table
    }
  },
  watch: {},
  mounted() {
    this.initTable(this.$store.state.dataset.table)
  },
  methods: {
    initTable(id) {
      console.log(id)
      if (id !== null) {
        this.fields = []
        this.data = []
        getTable(id).then(response => {
          this.table = response.data
          this.initPreviewData()
        })
      }
    },

    initPreviewData() {
      if (this.table.id) {
        getPreviewData(this.table).then(response => {
          this.fields = response.data.fields
          this.data = response.data.data
        })
      }
    },

    initTableFields() {
      fieldList(this.table.id).then(response => {
        this.tableFields = response.data
      })
    },

    edit() {
      this.editField = true
      // 请求当前表的所有字段，进行编辑
      this.initTableFields()
    },

    // createChart() {
    //   console.log(this.table);
    //   this.createViewDialog = true;
    // },

    saveEdit() {
      console.log(this.tableFields)
      batchEdit(this.tableFields).then(response => {
        this.closeEdit()
        this.initTable(this.table.id)
      })
    },

    closeEdit() {
      this.editField = false
      this.tableFields = []
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
</style>
