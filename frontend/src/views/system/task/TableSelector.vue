<template>
  <el-dialog
    :title="$t('chart.select_dataset')"
    :visible.sync="selectDatasetFlag"
    width="1200px"
    class="de-dialog-form"
    append-to-body
  >
    <div class="dataset-task-table">
      <div class="dataset-tree">
        <el-input
          v-model="filterText"
          size="mini"
          :placeholder="$t('commons.search')"
          prefix-icon="el-icon-search"
          clearable
        />
        <div class="tree" v-loading="treeLoading">
          <el-tree
            ref="datasetTreeRef"
            current-node-key="id"
            :data="treeData"
            node-key="id"
            highlight-current
            :expand-on-click-node="true"
            :filter-node-method="filterNode"
            @node-click="nodeClick"
          >
            <span slot-scope="{ data }" class="custom-tree-node">
              <span v-if="data.modelInnerType === 'group'">
                <svg-icon icon-class="scene"/>
                <span
                  style="
                    margin-left: 6px;
                    white-space: nowrap;
                    overflow: hidden;
                    text-overflow: ellipsis;
                  "
                  :title="data.name"
                  >{{ data.name }}</span
                >
              </span>
              <span v-else>
                <span>
                  <svg-icon
                    :icon-class="`ds-${data.modelInnerType}`"
                    :class="`ds-icon-${data.modelInnerType}`"
                  />
                </span>
                <span
                  style="
                    margin-left: 6px;
                    white-space: nowrap;
                    overflow: hidden;
                    text-overflow: ellipsis;
                  "
                  :title="data.name"
                  >{{ data.name }}</span
                >
              </span>
            </span>
          </el-tree>
        </div>
      </div>
      <div v-loading="dataLoading" class="dataset-tree-table">
        <p v-if="tableName" class="table-name">
          {{ tableName }} <span>{{ $t("chart.preview_100_data") }}</span>
        </p>
        <el-table border v-if="table.length" style="width: 100%" :data="table">
          <el-table-column
            v-for="field in fields"
            :key="field.dataeaseName"
            min-width="200"
            :prop="field.dataeaseName"
            :resizable="true"
          >
            <template slot="header">
              <svg-icon
                :icon-class="iconFormate(field.deType).iconClass"
                :class="iconFormate(field.deType).class"
              />
              <span>{{ field.name }}</span>
            </template>
          </el-table-column>
        </el-table>
        <el-empty v-else :description="$t('暂无数据')"></el-empty>
      </div>
    </div>
    <div slot="footer" class="dialog-footer">
      <deBtn secondary @click="selectDatasetFlag = false">{{
        $t("chart.cancel")
      }}</deBtn>
      <deBtn @click="setIdName" :disabled="!tableName" type="primary">{{
        $t("fu.table.ok")
      }}</deBtn>
    </div>
  </el-dialog>
</template>

<script>
import { queryAuthModel } from "@/api/authModel/authModel";
import { post } from "@/api/dataset/dataset";

export default {
  props: {
    customType: {
      type: Array,
      required: false,
      default: null,
    },
    mode: {
      type: Number,
      required: false,
      default: -1,
    },
    privileges: {
      type: String,
      required: false,
      default: "use",
    },
    clearEmptyDir: {
      type: Boolean,
      required: false,
      default: false,
    },
  },
  watch: {
    filterText(val) {
      this.$refs.datasetTreeRef.filter(val);
    },
  },
  data() {
    return {
      selectDatasetFlag: false,
      tableName: "",
      tableId: "",
      treeData: [],
      table: [],
      filterText: "",
      fields: [],
      dataLoading: false,
      treeLoading: false,
    };
  },
  mounted() {
    this.treeNode();
  },
  methods: {
    iconFormate(deType) {
      const val = ["text", "time", "value", "value", "location"][deType];
      return {
        class: `field-icon-${val}`,
        iconClass: `field_${val}`,
      };
    },
    treeNode() {
      this.treeLoading = true;
      const modelInnerTypeArray = Array.isArray(this.customType)
        ? [...this.customType, "group"]
        : null;
      queryAuthModel(
        {
          modelType: "dataset",
          privileges: this.privileges,
          datasetMode: this.mode,
          clearEmptyDir: this.clearEmptyDir,
          mode: this.mode < 0 ? null : this.mode,
          modelInnerTypeArray,
        },
        true
      )
        .then((res) => {
          this.treeData = res.data;
        })
        .finally(() => {
          this.treeLoading = false;
        });
    },
    filterNode(value, data) {
      if (!value) return true;
      return data.label.indexOf(value) !== -1;
    },
    initData(table) {
      this.dataLoading = true;
      table.row = 100;
      table.previewForTask = true
      post("/dataset/table/getPreviewData/1/100", table, false, 30000)
        .then((response) => {
          this.fields = response.data.fields;
          this.table = response.data.data;
        })
        .finally((res) => {
          this.dataLoading = false;
        });
    },
    init() {
      this.selectDatasetFlag = true;
    },
    setIdName() {
      this.$emit("getTableId", this.tableId, this.tableName);
      this.selectDatasetFlag = false;
    },
    nodeClick(data) {
      const { id, name, modelInnerType: type } = data;
      if (type === "group") return;
      this.tableName = name;
      this.tableId = id;
      this.initData(data);
    },
  },
};
</script>

<style lang="scss">
.dataset-task-table {
  box-sizing: border-box;
  height: 628px;
  border: 1px solid #dee0e3;
  border-radius: 4px;
  display: flex;
  max-height: 628px;
  .el-tree-node__content {
    height: 40px;
    border-radius: 4px;
    &:hover {
      background-color: var(--deWhiteHover, #3370ff) !important;
      .custom-tree-node {
        color: var(--primary, #3370ff);
      }
    }
  }
  .dataset-tree {
    width: 253px;
    border-right: 1px solid #dee0e3;
    padding: 16px;

    .tree {
      padding-top: 16px;
      overflow-y: auto;
      height: calc(100% - 30px);
    }
  }
  .dataset-tree-table {
    flex: 1;
    padding: 16px;
    overflow-y: auto;
    .table-name {
      margin: 0;
      margin-bottom: 16px;
      display: flex;
      align-items: center;
      justify-content: space-between;
      font-family: PingFang SC;
      font-size: 16px;
      font-weight: 500;
      line-height: 24px;
      color: var(--deTextPrimary, #000000);
      span {
        font-size: 14px;
        font-weight: 400;
        color: #646a73;
      }
    }
  }
}
</style>
