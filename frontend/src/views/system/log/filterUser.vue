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
      let [min, max] = this.dataRange;
      if (min && max) {
        if (+min === +max) {
          max = +max + 24 * 3600 * 1000;
        }
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