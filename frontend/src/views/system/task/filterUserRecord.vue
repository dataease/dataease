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
            :class="[
              active[ele.activeType].includes(item.value) ? 'active' : '',
            ]"
            :key="item.name"
            v-for="item in ele.list"
            >{{ $t(item.name) }}</span
          >
        </div>
      </div>

      <div class="filter">
        <span>{{ $t("dedaterange.label") }}</span>
        <div class="filter-item">
          <DeDatePick v-model="dataRange"></DeDatePick>
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
import { filterDatasetRecord, dateFormat } from "./options";
import { queryAuthModel } from "@/api/authModel/authModel";
import DeDatePick from "@/components/deCustomCm/deDatePick.vue";

export default {
  components: {
    DeDatePick,
  },
  data() {
    return {
      treeLoading: false,
      filterTextMap: [],
      dataRange: [],
      selectDatasets: [],
      datasetCahe: [],
      activeDataset: [],
      selectDatasetsCahe: [],
      treeData: [],
      filterDataset: [filterDatasetRecord],
      active: {
        execStatus: [],
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
      this.datasetCahe = this.datasetCahe.filter(
        (ele) => ele.id !== id
      );
      this.selectDatasetsCahe = this.selectDatasetsCahe.filter(
        (ele) => ele.id !== id
      );
      this.$refs.datasetTreeRef.filter(true);
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
      ["dataset.task.last_exec_status"].forEach((ele, index) => {
        const { activeType: type, list } = this.filterDataset[index];
        if (this.active[type].length) {
          params.push(
            `${this.$t(ele)}:${this.active[type]
              .map((item) =>
                this.$t(list.find((itx) => itx.value === item).name)
              )
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
      let [min, max] = this.dataRange;
      if (min && max) {
        if (+min === +max) {
          max = +max + 24 * 3600 * 1000;
        }
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