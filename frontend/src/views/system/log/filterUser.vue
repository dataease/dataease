<template>
  <el-drawer
    :title="$t('user.filter_method')"
    :visible.sync="userDrawer"
    custom-class="user-drawer-task"
    size="680px"
    v-closePress
    direction="rtl"
  >
    <div class="filter">
      <span>{{ $t("log.optype") }}</span>
      <div class="filter-item">
        <span
          class="item"
          @click="statusChange(item.value)"
          :class="[activeType.includes(item.value) ? 'active' : '']"
          :key="item.label"
          v-for="item in types"
          >{{ $t(item.label) }}</span
        >
      </div>
    </div>
    <div class="el-drawer__body-cont">
      <div class="filter">
        <span>{{ $t("log.user") }}</span>
        <div class="filter-item">
          <span
            @click="activeUserChange(ele.id)"
            class="item"
            :class="[activeUser.includes(ele.id) ? 'active' : '']"
            :key="ele.id"
            v-for="ele in usersValueCopy"
            >{{ ele.username }}</span
          >
          <el-popover
            placement="bottom"
            popper-class="user-popper"
            width="200"
            trigger="click"
          >
            <el-select
              ref="userSelect"
              v-model="usersValue"
              multiple
              filterable
              :placeholder="$t('commons.please_select')"
              @change="changeUser"
              @remove-tag="changeUser"
              value-key="id"
            >
              <el-option
                v-for="item in usersComputed"
                :key="item.username"
                :label="item.username"
                :value="item"
              />
            </el-select>
            <span class="more" slot="reference">+ {{ $t("panel.more") }}</span>
          </el-popover>
        </div>
      </div>
      <div class="filter">
        <span>{{ $t("dedaterange.label") }}</span>
        <div class="filter-item">
          <DeDatePick  v-model="dataRange"></DeDatePick>
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
import { dateFormat } from "@/views/system/task/options.js";
import { opTypes } from "@/api/system/log";
import { post } from "@/api/dataset/dataset";
import DeDatePick from '@/components/deCustomCm/deDatePick.vue'
export default {
  components: {
    DeDatePick
  },
  data() {
    return {
      types: [],
      treeLoading: false,
      filterTextMap: [],
      dataRange: [],
      usersValue: [],
      activeUser: [],
      users: [],
      userCahe: [],
      activeType: [],
      userDrawer: false,
    };
  },
  computed: {
    usersComputed() {
      return this.users.filter((ele) => !this.activeUser.includes(ele.id));
    },
    usersValueCopy() {
      return this.userCahe.filter((ele) => this.activeUser.includes(ele.id));
    },
  },
  mounted() {
    this.loadUsers();
    this.loadType();
  },
  methods: {
    loadType() {
      opTypes().then((res) => {
        this.types = res.data.map((item) => {
          return { label: item.name, value: item.id };
        });
      });
    },
    loadUsers() {
      post("/api/user/userLists", {}, false).then((res) => {
        this.users = res.data;
      });
    },
    changeUser() {
      if (
        this.userCahe.length >
        this.usersValue.length + this.activeUser.length
      ) {
        this.userCahe = this.userCahe.filter((ele) =>
          this.usersValue
            .map((ele) => ele.id)
            .concat(this.activeUser)
            .includes(ele.id)
        );
        return;
      }
      const userIdx = this.usersValue.findIndex(
        (ele) =>
          !this.userCahe
            .map((ele) => ele.id)
            .concat(this.activeUser)
            .includes(ele.id)
      );
      if (userIdx === -1) return;
      this.activeUser.push(this.usersValue[userIdx].id);
      this.userCahe.push(this.usersValue[userIdx]);
      this.usersValue.splice(userIdx, 1);
    },
    activeUserChange(id) {
      const userIndex = this.activeUser.findIndex((ele) => ele === id);
      if (userIndex === -1) {
        this.activeUser.push(id);
        this.usersValue = this.usersValue.filter((ele) => ele.id !== id);
      } else {
        this.activeUser.splice(userIndex, 1);
        const user = this.userCahe.find((ele) => ele.id === id);
        this.usersValue.push(user);
      }
    },
    clearFilter() {
      this.dataRange = [];
      this.usersValue = [];
      this.activeUser = [];
      this.activeType = [];
      this.userCahe = [];
      this.$emit("search", [], []);
    },
    clearOneFilter(index) {
      (this.filterTextMap[index] || []).forEach((ele) => {
        this[ele] = [];
      });
    },
    statusChange(value, type) {
      const statusIndex = this.activeType.findIndex((ele) => ele === value);
      if (statusIndex === -1) {
        this.activeType.push(value);
      } else {
        this.activeType.splice(statusIndex, 1);
      }
    },
    search() {
      this.userDrawer = false;
      this.$emit("search", this.formatCondition(), this.formatText());
    },
    formatText() {
      this.filterTextMap = [];
      const params = [];
      if (this.activeType.length) {
          params.push(
            `${this.$t('log.optype')}:${this.activeType
              .map((item) =>
                this.types.find((itx) => itx.value === item).label
              )
              .join("、")}`
          );
          this.filterTextMap.push([`activeType`]);
        }
      if (this.activeUser.length) {
        let str = `${this.$t("log.user")}:${this.activeUser.reduce(
          (pre, next) =>
            (this.users.find((ele) => ele.id === next) || {}).username +
            "、" +
            pre,
          ""
        )}`;
        params.push(str.slice(0, str.length - 1));
        this.filterTextMap.push([
          "usersValue",
          "activeUser",
          "users",
          "userCahe",
        ]);
      }
      
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
        optype: this.activeType,
        "user_id": this.activeUser,
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
          field: "time",
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
    background: #ffffff;
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