<template>
  <el-drawer
    title="筛选条件"
    :visible.sync="userDrawer"
    custom-class="user-drawer"
    size="680px"
    direction="rtl"
  >
    <div class="filter">
      <span>状态</span>
      <div class="filter-item">
        <span
          @click="statusChange(ele.id)"
          :class="[activeStatus.includes(ele.id) ? 'active' : '']"
          :key="ele.id"
          v-for="ele in status"
          >{{ ele.label }}</span>
      </div>
    </div>
    <div class="filter">
      <span>组织</span>
      <div class="filter-item">
        <span
          @click="activeDeptChange(ele.id)"
          :class="[activeDept.includes(ele.id) ? 'active' : '']"
          :key="ele.id"
          v-for="ele in selectDepts"
          >{{ ele.label }}</span>
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
          >
            <el-tree
              :load="loadNode"
              :lazy="true"
              :expand-on-click-node="false"
              :data="depts"
              :props="defaultProps"
              @node-click="handleNodeClick"
            ></el-tree>

            <el-select
              ref="roleSelect"
              v-model="selectDepts"
              slot="reference"
              popper-class="tree-select"
              multiple
              :placeholder="$t('commons.please_select')"
              @change="changeRole"
              @remove-tag="changeRole"
              value-key="id"
            >
            <el-option
              v-for="item in selectDepts"
              :key="item.label"
              :label="item.label"
              :value="item"
            />
            </el-select>
          </el-popover>
          <span slot="reference">+ 更多</span>
        </el-popover>
      </div>
    </div>
    <div class="filter">
      <span>角色</span>
      <div class="filter-item">
        <span
          @click="activeRoleChange(ele.id)"
          :class="[activeRole.includes(ele.id) ? 'active' : '']"
          :key="ele.id"
          v-for="ele in rolesValue"
          >{{ ele.name }}</span
        >
        <el-popover
          placement="bottom"
          popper-class="user-popper"
          width="200"
          trigger="click"
        >
          <el-select
            ref="roleSelect"
            v-model="rolesValue"
            multiple
            :placeholder="$t('commons.please_select')"
            @change="changeRole"
            @remove-tag="changeRole"
            value-key="id"
          >
            <el-option
              v-for="item in roles"
              :key="item.name"
              :label="item.name"
              :value="item"
            />
          </el-select>

          <span slot="reference">+ 更多</span>
        </el-popover>
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
// import bus from '@/utils/bus'
import { allRoles } from "@/api/system/user";
import { getDeptTree, treeByDeptId } from "@/api/system/dept";

export default {
  data() {
    return {
      value: [],
      roles: [],
      status: [{
        id: 1,
        label: '启用'
      },{
        id: 0,
        label: '禁用'
      }],
      activeStatus: [],
      rolesValue: [],
      activeRole: [],
      depts: [],
      selectDepts: [],
      activeDept: [],
      defaultProps: {
        children: "children",
        label: "label",
        isLeaf: "leaf",
      },
      userDrawer: false
    };
  },
  mounted() {
    this.initRoles();
  },
  methods: {
    // 获取弹窗内部门数据
    treeByDeptId() {
      treeByDeptId(0).then((res) => {
        this.depts = res.data || [];
      });
    },
    changeRole() {
      const roles = this.rolesValue.map((item) => item.id);
      this.activeRole = this.activeRole.filter((ele) => roles.includes(ele));
    },
    activeRoleChange(id) {
      const roleIndex = this.activeRole.findIndex((ele) => ele === id);
      if (roleIndex === -1) {
        this.activeRole.push(id);
      } else {
        this.activeRole.splice(roleIndex, 1);
      }
    },
    handleNodeClick({ id, label }) {
      const deptIndex = this.selectDepts.findIndex((ele) => ele.id === id);
      if (deptIndex === -1) {
        this.selectDepts.push({ id, label });
      } else {
        this.selectDepts.splice(deptIndex, 1);
        this.changeDepts();
      }
    },
    activeDeptChange(id) {
        const deptIndex = this.activeDept.findIndex((ele) => ele === id);
      if (deptIndex === -1) {
        this.activeDept.push(id);
      } else {
        this.activeDept.splice(deptIndex, 1);
      }
    },
    statusChange(id) {
        const statusIndex = this.activeStatus.findIndex((ele) => ele === id);
      if (statusIndex === -1) {
        this.activeStatus.push(id);
      } else {
        this.activeStatus.splice(statusIndex, 1);
      }
    },
    changeDepts() {
        const depts = this.selectDepts.map((item) => item.id);
        this.activeDept = this.activeDept.filter((ele) => depts.includes(ele));
    },
    loadNode(node, resolve) {
      if (!this.depts.length) {
        this.treeByDeptId();
        return;
      }
      getDeptTree(node.data.id).then((res) => {
        resolve(
          res.data.map((dept) => {
            return this.normalizer(dept);
          })
        );
      });
    },
    normalizer(node) {
      return {
        id: node.deptId,
        label: node.name,
        leaf: !node.hasChildren,
      };
    },
    initRoles() {
      allRoles().then((res) => {
        this.roles = res.data;
      });
    },
    search() {
      this.userDrawer = false;
      this.$emit('search', this.formatCondition())
    },
    formatCondition() {
      const fildMap = {'r.role_id': this.activeRole, 'd.dept_id': this.activeDept, 'u.enabled': this.activeStatus}
      const conditions = []
      Object.keys(fildMap).forEach(ele => {
        if (fildMap[ele].length) {
          conditions.push({
            field: ele,
            operator: 'in',
            value: fildMap[ele]
          })
        }
      })
      return conditions;
    },
    init() {
      this.userDrawer = true;
    },
    reset() {
      this.activeStatus = [];
      this.activeRole = [];
      this.activeDept = [];
      this.search()
    },
  },
};
</script>

<style lang="scss">
.user-drawer {
  .el-drawer__header {
    padding: 16px 21px 16px 24px;
    font-family: PingFang SC;
    font-size: 16px;
    font-weight: 500;
    line-height: 24px;
    letter-spacing: 0px;
    text-align: left;
    color: #1f2329;
    border-bottom: 1px solid rgba(187, 191, 196, 0.5);
    margin: 0;
  }

  .el-drawer__body {
    padding: 12px 24px 24px 24px;
    position: relative;
  }

  .filter {
    display: flex;
    align-items: center;
    height: 46px;
    > :nth-child(1) {
      margin-right: 88px;
      color: #1f2329;
      font-family: "PingFang SC";
      font-style: normal;
      font-weight: 400;
      font-size: 14px;
    }
    .filter-item {
      span {
        font-family: PingFang SC;
        font-size: 14px;
        font-weight: 400;
        line-height: 24px;
        margin-right: 12px;
        text-align: center;
        padding: 1px 6px;
        background: #f5f6f7;
        border-radius: 2px;
        cursor: pointer;
        span {
          margin-right: 0;
          padding: 0;
          span {
            margin-right: 0;
            padding: 0;
          }
        }
      }

      .active {
        background: rgba(51, 112, 255, 0.1);
        color: #0c296e;
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
    right: 24px;
    bottom: 24px;
    text-align: right;
  }
}
.user-popper {
  background: transparent;
  padding: 0;
  .popper__arrow {
    display: none;
  }
}
.tree-select {
  .el-select-dropdown__empty,
  .popper__arrow {
    display: none;
  }
}

.user-popper.dept {
  height: 400px;
  overflow: auto;
}
</style>