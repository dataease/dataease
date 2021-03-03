<template>
<el-col>
  <el-row>
    <el-button icon="el-icon-setting" size="mini" @click="showConfig">
      {{$t('dataset.update_setting')}}
    </el-button>
  </el-row>
  <el-row style="margin-top: 10px;">
    <!--TODO-->
    <el-table
      size="mini"
      :data="tableData"
      border
      style="width: 100%">
      <el-table-column
        prop="date"
        label="日期"
        width="180">
      </el-table-column>
      <el-table-column
        prop="name"
        label="姓名"
        width="180">
      </el-table-column>
      <el-table-column
        prop="address"
        label="地址">
      </el-table-column>
    </el-table>
  </el-row>

  <el-dialog :title="table.name+' '+$t('dataset.update_setting')" :visible="update_setting" :show-close="false"
             width="50%" class="dialog-css">
    <el-row>
      <el-button icon="el-icon-download" size="mini">
        {{$t('dataset.sync_now')}}
      </el-button>
      <el-button icon="el-icon-plus" size="mini">
        {{$t('dataset.add_task')}}
      </el-button>
    </el-row>
    <div slot="footer" class="dialog-footer">
      <el-button size="mini" @click="update_setting = false">{{$t('dataset.cancel')}}</el-button>
      <el-button type="primary" size="mini" @click="update_setting = false">{{$t('dataset.confirm')}}</el-button>
    </div>
  </el-dialog>
</el-col>
</template>

<script>
export default {
  props: {
    table: Object,
  },
  name: "UpdateInfo",
  data() {
    return {
      update_setting: false,
      tableData: [{
        date: '2016-05-02',
        name: '王小虎',
        address: '上海市普陀区金沙江路 1518 弄'
      }, {
        date: '2016-05-04',
        name: '王小虎',
        address: '上海市普陀区金沙江路 1517 弄'
      }, {
        date: '2016-05-01',
        name: '王小虎',
        address: '上海市普陀区金沙江路 1519 弄'
      }, {
        date: '2016-05-03',
        name: '王小虎',
        address: '上海市普陀区金沙江路 1516 弄'
      }]
    }
  },
  methods: {
    showConfig() {
      this.update_setting = true;
    }
  }
}
</script>

<style scoped>
  .dialog-css >>> .el-dialog__header {
    padding: 20px 20px 0;
  }

  .dialog-css >>> .el-dialog__body {
    padding: 10px 20px 20px;
  }
</style>
