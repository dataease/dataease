<template>
  <div class="jar-config-list">

    <el-table border :data="tableData"
              class="adjust-table table-content"
              highlight-current-row
              @row-click="handleView">

      <el-table-column prop="name" :label="$t('commons.name')" show-overflow-tooltip/>
      <el-table-column prop="fileName" :label="$t('api_test.jar_config.jar_file')"  show-overflow-tooltip/>
      <el-table-column prop="description" :label="$t('commons.description')" show-overflow-tooltip/>
      <el-table-column prop="updateTime" :label="$t('commons.update_time')" show-overflow-tooltip>
        <template v-slot:default="scope">
          <span>{{ scope.row.updateTime | timestampFormatDate }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="creator" :label="$t('report.user_name')"  show-overflow-tooltip/>
      <el-table-column prop="modifier" :label="$t('commons.modifier')"  show-overflow-tooltip/>

      <el-table-column :label="$t('commons.operating')" min-width="100">
        <template v-slot:default="scope">
          <ms-table-operator-button :isTesterPermission="true" :tip="$t('commons.delete')" icon="el-icon-delete" type="danger" @exec="handleDelete(scope.row.id)"/>
        </template>
      </el-table-column>

    </el-table>

  </div>
</template>

<script>

    import MsTableOperatorButton from "../../../../common/components/MsTableOperatorButton";

    export default {
      name: "MsJarConfigList",
      components: {MsTableOperatorButton},
      props: {
        tableData: Array,
        isReadOnly: {
          type: Boolean,
          default: false
        },
      },
      data() {
        return {
          result: {},
        }
      },
      methods: {
        handleView(row) {
          this.$emit('rowSelect', row);
        },
        handleDelete(id) {
          this.$confirm(this.$t('api_test.jar_config.delete_tip'), '', {
            confirmButtonText: this.$t('commons.confirm'),
            cancelButtonText: this.$t('commons.cancel'),
            type: 'warning'
          }).then(() => {
            this.result = this.$get("/jar/delete/" + id, () => {
              this.$success(this.$t('commons.delete_success'));
              this.$emit('refresh');
            });
          }).catch(() => {
            this.$message({
              type: 'info',
              message: this.$t('commons.delete_cancelled')
            });
          });
        },
      }
    }
</script>

<style scoped>

  .el-icon-check {
    font-size: 20px;
    font-weight: bold;
    color: green;
    margin-left: 20px;
  }
</style>
