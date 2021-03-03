<template>
<el-col>
  <el-row>
    <el-button icon="el-icon-setting" size="mini" @click="showConfig">
      {{ $t('dataset.update_setting') }}
    </el-button>
  </el-row>
  <el-row style="margin-top: 10px;">
    <!--TODO-->
    <el-table
      size="mini"
      :data="tableData"
      border
      style="width: 100%"
    >
      <el-table-column
        prop="name"
        label="更新任务名称"
      />
      <el-table-column
        prop="startTime"
        label="开始时间"
      />
      <el-table-column
        prop="endTime"
        label="结束时间"
      />
      <el-table-column
        prop="info"
        label="更新信息"
      />
    </el-table>
  </el-row>

  <el-dialog
    :title="table.name+' '+$t('dataset.update_setting')"
    :visible="update_setting"
    :show-close="false"
    width="50%"
    class="dialog-css"
  >
    <el-row>
      <el-button icon="el-icon-download" size="mini">
        {{ $t('dataset.sync_now') }}
      </el-button>
      <el-button icon="el-icon-plus" size="mini">
        {{ $t('dataset.add_task') }}
      </el-button>
    </el-row>
    <div slot="footer" class="dialog-footer">
      <el-button size="mini" @click="update_setting = false">{{ $t('dataset.cancel') }}</el-button>
      <el-button type="primary" size="mini" @click="update_setting = false">{{ $t('dataset.confirm') }}</el-button>
    </div>
  </el-dialog>
</el-col>
</template>

<script>
export default {
  name: 'UpdateInfo',
  props: {
    table: Object
  },
  data() {
    return {
      update_setting: false,
      tableData: [{
        startTime: '2016-05-02 00:00:00',
        endTime: '2016-05-02 00:00:10',
        name: '每天0点更新',
        info: '更新成功'
      }, {
        startTime: '2016-05-02 00:00:00',
        endTime: '2016-05-02 00:00:10',
        name: '每天0点更新',
        info: '更新成功'
      }, {
        startTime: '2016-05-02 00:00:00',
        endTime: '2016-05-02 00:00:10',
        name: '每天0点更新',
        info: '更新成功'
      }, {
        startTime: '2016-05-02 00:00:00',
        endTime: '2016-05-02 00:00:10',
        name: '每天0点更新',
        info: '更新成功'
      }]
    }
  },
  methods: {
    showConfig() {
      this.update_setting = true
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
