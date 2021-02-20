<template>
  <el-dialog :title="$t('api_test.request.assertions.json_path_add')"
             :visible.sync="dialogFormVisible"
             @close="close"
             width="60%" v-loading="result.loading"
             :close-on-click-modal="false"
             top="50px">

    <el-container class="main-content">

      <el-container>
        <el-main class="case-content">
          <el-table
            :data="jsonPathList"
            row-key="id"
            @select-all="handleSelectAll"
            @select="handleSelectionChange"
            height="50vh"
            ref="table">

            <el-table-column type="selection"/>

            <el-table-column
              prop="name"
              :label="$t('api_test.request.extract.json_path_expression')"
              style="width: 100%">
              <template v-slot:default="scope">
                {{scope.row.json_path}}
              </template>
            </el-table-column>
            <el-table-column
              prop="name"
              :label="$t('api_test.request.assertions.value')"
              style="width: 100%">
              <template v-slot:default="scope">
                {{scope.row.json_value}}
              </template>
            </el-table-column>
          </el-table>

        </el-main>
      </el-container>
    </el-container>

    <template v-slot:footer>
      <ms-dialog-footer @cancel="dialogFormVisible = false" @confirm="commit"/>
    </template>

  </el-dialog>
</template>

<script>
  import MsDialogFooter from "../../../../common/components/MsDialogFooter";
  import {HttpRequest} from "../../model/ApiTestModel";

  export default {
    name: "MsApiJsonpathSuggestList",
    components: {MsDialogFooter},
    data() {
      return {
        result: {},
        dialogFormVisible: false,
        isCheckAll: false,
        selectItems: new Set(),
        jsonPathList: [],
      };
    },
    props: {
      request: HttpRequest,
    },
    methods: {
      close() {
        this.selectItems.clear();
      },
      open() {
        this.getJsonPaths();
      },
      getJsonPaths() {
        if (this.request.debugRequestResult) {
          let param = {
            jsonPath: this.request.debugRequestResult.responseResult.body
          };
          this.result = this.$post("/api/getJsonPaths", param).then(response => {
            this.jsonPathList = response.data.data;
            this.dialogFormVisible = true;
          }).catch(() => {
            this.$warning(this.$t('api_test.request.assertions.json_path_err'));
            this.dialogFormVisible = false;
          });
        }
      },
      handleSelectAll(selection) {
        if (selection.length > 0) {
          this.selectItems = new Set(this.jsonPathList);
        } else {
          this.selectItems = new Set();
        }
      },
      handleSelectionChange(selection, row) {
        if (this.selectItems.has(row)) {
          this.selectItems.delete(row);
        } else {
          this.selectItems.add(row);
        }
      },
      commit() {
        this.$emit("addJsonpathSuggest", this.selectItems);
        this.dialogFormVisible = false;
      }
    }
  }
</script>

<style scoped>

</style>
