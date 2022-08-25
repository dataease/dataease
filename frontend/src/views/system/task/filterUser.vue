<template>
  <el-drawer
    :title="$t('user.filter_method')"
    :visible.sync="userDrawer"
    custom-class="user-drawer-task"
    size="680px"
    v-closePress
    direction="rtl"
  >
  <div class="el-drawer__body-cont">
    <div class="filter">
      <span>{{ $t("dataset.datalist") }}</span>
      <div class="filter-item">
        <span
          class="item"
          @click="activeDatasetChange(ele.id)"
          :class="[activeDataset.includes(ele.id) ? 'active' : '']"
          :key="ele.id"
          v-for="ele in selectDatasetsCahe"
          >{{ ele.name }}</span
        >
        <el-popover
          placement="bottom"
          popper-class="user-popper"
          width="200"
          trigger="click"
        >
          <el-popover
            placement="bottom"
            popper-class="user-popper dept"
            width="200"
            trigger="click"
            v-loading="treeLoading"
          >
            <el-tree
              ref="datasetTreeRef"
              current-node-key="id"
              :data="treeData"
              node-key="id"
              highlight-current
              :filter-node-method="filterNode"
              :expand-on-click-node="true"
              @node-click="nodeClick"
            >
              <span slot-scope="{ data }" class="custom-tree-node">
                <span v-if="data.modelInnerType === 'group'">
                  <svg-icon icon-class="scene" class="ds-icon-scene" />
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

            <el-select
              ref="datasetSelect"
              v-model="selectDatasets"
              slot="reference"
              popper-class="tree-select"
              multiple
              :placeholder="$t('commons.please_select')"
              value-key="id"
            >
              <el-option
                v-for="item in selectDatasets"
                :key="item.name"
                :label="item.name"
                :value="item"
              />
            </el-select>
          </el-popover>
          <span class="more" slot="reference">+ {{ $t("panel.more") }}</span>
        </el-popover>
      </div>
    </div>
    <div v-for="ele in filterDataset" :key="ele.name" class="filter">
      <span>{{ $t(ele.name) }}</span>
      <div class="filter-item">
        <span
          class="item"
          @click="statusChange(item.value, ele.activeType)"
          :class="[active[ele.activeType].includes(item.value) ? 'active' : '']"
          :key="item.name"
          v-for="item in ele.list"
          >{{ $t(item.name) }}</span
        >
      </div>
    </div>

    <div class="filter">
      <span>{{ $t("dedaterange.label") }}</span>
      <div class="filter-item">
        <el-date-picker
          v-model="dataRange"
          size="small"
          type="daterange"
          range-separator=""
          start-placeholder="开始日期"
          end-placeholder="结束日期"
        >
        </el-date-picker>
      </div>
    </div>
    </div>
    <div class="foot">
      <el-button class="btn normal" @click="reset">{{
        $t("commons.reset")
      }}</el-button>
      <el-button type="primary" class="btn" @click="search">{{
        $t("commons.adv_search.search")
      }}</el-button>
    </div>
  </el-drawer>
</template>

<script>
import { filterDataset, dateFormat } from "./options";
import { allRoles } from "@/api/system/user";
import { getDatasetTree, treeByDatasetId } from "@/api/system/dept";
import { queryAuthModel } from "@/api/authModel/authModel";

export default {
  data() {
    return {
      treeLoading: false,
      dataRange: [],
      selectDatasets: [],
      datasetCahe: [],
      activeDataset: [],
      selectDatasetsCahe: [],
      treeData: [],
      filterDataset,
      active: {
        execStatus: [],
        status: [],
        rate: [],
      },
      userDrawer: false,
    };
  },
  mounted() {
    this.treeNode();
  },
  methods: {
    treeNode() {
      this.treeLoading = true;
      queryAuthModel(
        {
          modelType: "dataset",
          privileges: "manage",
          datasetMode: 1,
          clearEmptyDir: true,
          mode: 1,
          modelInnerTypeArray: ["db", "sql", "api", "group"],
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
    nodeClick(data) {
      const { id, name, modelInnerType: type } = data;
      if (type === "group") return;
      this.handleNodeClick(id, name);
    },
    filterNode(value, data) {
      if (!value) return true;
      return !this.activeDataset.includes(data.id);
    },
    clearFilter() {
      this.active = {
        execStatus: [],
        status: [],
        rate: [],
      };
      this.dataRange = [];
      this.activeDataset = [];
      this.selectDatasets = [];
      this.datasetCahe = [];
      this.selectDatasetsCahe = [];
      this.$refs.datasetTreeRef.filter();
      this.$emit("search", [], []);
    },
    clearOneFilter(index) {
      (this.filterTextMap[index] || []).forEach((ele) => {
        const eleKey = ele.split(".");
        if (eleKey.length === 2) {
          const [p, c] = eleKey;
          this[p][c] = [];
        } else {
          this[ele] = [];
        }
        if (ele === 'activeDataset') {
          this.$refs.datasetTreeRef.filter();
        }
      });
    },
    statusChange(value, type) {
      const statusIndex = this.active[type].findIndex((ele) => ele === value);
      if (statusIndex === -1) {
        this.active[type].push(value);
      } else {
        this.active[type].splice(statusIndex, 1);
      }
    },
    handleNodeClick(id, name) {
      const datasetIdx = this.selectDatasets.findIndex((ele) => ele.id === id);
      if (datasetIdx !== -1) {
        this.selectDatasets.splice(datasetIdx, 1);
        this.selectDatasetsCahe = this.selectDatasetsCahe.filter(
          (ele) => ele.id !== id
        );
        this.datasetCahe = this.datasetCahe.filter((ele) => ele.id !== id);
        this.$refs.datasetTreeRef.filter(id);
        return;
      }
      this.activeDataset.push(id);
      this.selectDatasetsCahe.push({ id, name });
      this.datasetCahe.push({ id, name });
      this.$refs.datasetTreeRef.filter(id);
    },
    activeDatasetChange(id) {
      const dataset = this.datasetCahe.find((ele) => ele.id === id);
      this.selectDatasets.push(dataset);
      this.activeDataset = this.activeDataset.filter((ele) => ele !== id);
      this.selectDatasetsCahe = this.selectDatasetsCahe.filter(
        (ele) => ele.id !== id
      );
    },
    search() {
      this.userDrawer = false;
      this.$emit("search", this.formatCondition(), this.formatText());
    },
    formatText() {
      this.filterTextMap = [];
      const params = [];
      if (this.activeDataset.length) {
        let str = `${this.$t("dataset.datalist")}:${this.activeDataset.reduce(
          (pre, next) =>
            (this.datasetCahe.find((ele) => ele.id === next) || {}).name +
            "、" +
            pre,
          ""
        )}`;
        params.push(str.slice(0, str.length - 1));
        this.filterTextMap.push([
          "activeDataset",
          "selectDatasets",
          "selectDatasetsCahe",
          "datasetCahe",
        ]);
      }
      [
        "dataset.execute_rate",
        "dataset.task.task_status",
        "dataset.task.last_exec_status",
      ].forEach((ele, index) => {
        const { activeType: type, list } =
          this.filterDataset[index];
        if (this.active[type].length) {
          params.push(
            `${this.$t(ele)}:${this.active[type]
              .map((item) => this.$t(list.find((itx) => itx.value === item).name))
              .join("、")}`
          );
          this.filterTextMap.push([`active.${type}`]);
        }
      });
      if (this.dataRange.length) {
        params.push(
          `${this.$t("dedaterange.label")}:${this.dataRange
            .map((ele) => {
              return dateFormat("YYYY-mm-dd", ele);
            })
            .join("-")}`
        );
        this.filterTextMap.push(["dataRange"]);
      }
      return params;
    },
    formatCondition() {
      const fildMap = {
        "dataset_table_task.rate": this.active.rate,
        "dataset_table_task.status": this.active.status,
        "dataset_table_task.last_exec_status": this.active.execStatus,
        "dataset_table.id": this.activeDataset,
      };
      const conditions = [];
      Object.keys(fildMap).forEach((ele) => {
        if (fildMap[ele].length) {
          conditions.push({
            field: ele,
            operator: "in",
            value: fildMap[ele],
          });
        }
      });
      const [min, max] = this.dataRange;
      if (min && max) {
        conditions.push({
          field: "dataset_table_task.last_exec_time",
          operator: "between",
          value: [+min, +max],
        });
      }
      return conditions;
    },
    init() {
      this.userDrawer = true;
    },
    reset() {
      this.clearFilter();
      this.userDrawer = false;
    },
  },
};
</script>

<style lang="scss">
.user-drawer-task {

  .el-drawer__body-cont {
    height: 100%;
    box-sizing: border-box;
    overflow: hidden;
    width: 100%;
    padding-bottom: 80px;
  }
  .el-drawer__header {
    padding: 16px 24px;
    margin: 0;
    font-family: PingFang SC;
    font-size: 16px;
    font-weight: 500;
    line-height: 24px;
    color: #1f2329;
    position: relative;
    box-sizing: border-box;
    height: 57px;
    mix-blend-mode: normal;
    border-bottom: 1px solid rgba(187, 191, 196, 0.5);

    .el-drawer__close-btn {
      position: absolute;
      right: 24px;
      top: 16px;
      padding: 4px;
      border-radius: 4px;
      display: flex;
      align-items: center;
      justify-content: center;
    }

    .el-drawer__close-btn:hover {
      background: #e9e9ea;
    }
  }

  .el-drawer__body {
    padding: 12px 24px 24px 24px;
    position: relative;
  }

  .filter {
    display: flex;
    min-height: 46px;
    > :nth-child(1) {
      color: #1f2329;
      font-family: "PingFang SC";
      font-style: normal;
      font-weight: 400;
      font-size: 14px;
      line-height: 24px;
      white-space: nowrap;
      width: 116px;
    }
    .filter-item {
      flex: 1;
      .item,
      .more {
        font-family: PingFang SC;
        white-space: nowrap;
        font-size: 14px;
        font-weight: 400;
        line-height: 24px;
        margin-right: 12px;
        text-align: center;
        padding: 1px 6px;
        background: #f5f6f7;
        border-radius: 2px;
        cursor: pointer;
        display: inline-block;
        margin-bottom: 12px;
      }

      .active,
      .more:hover {
        background: rgba(51, 112, 255, 0.1);
        color: #0c296e;
      }
      .more {
        white-space: nowrap;
      }
    }
  }
  .btn {
    border-radius: 4px;
    padding: 5px 26px 5px 26px;
    font-family: PingFang SC;
    font-size: 14px;
    font-weight: 400;
    line-height: 20px;
    letter-spacing: 0px;
    text-align: center;
    border: none;
    box-sizing: border-box;
  }

  .normal {
    color: #1f2329;
    border: 1px solid #bbbfc4;
    margin-left: 12px;
  }

  .foot {
    position: absolute;
    height: 80px;
    width: 100%;
    padding: 24px;
    right: 0;
    bottom: 0;
    text-align: right;
    background: #FFFFFF;
    box-shadow: 0px -1px 4px rgba(0, 0, 0, 0.05);
  }
}
.user-popper {
  padding: 0;
  background: #fff;
  .popper__arrow {
    display: none;
  }
}
.tree-select {
  .el-select-dropdown__empty,
  .el-scrollbar__wrap,
  .popper__arrow {
    display: none !important;
  }
}

.user-popper.dept {
  height: 400px;
  overflow: auto;
}
</style>