<template>
  <el-drawer
    :title="$t('user.filter_method')"
    :visible.sync="userDrawer"
    custom-class="user-drawer"
    size="680px"
    v-closePress
    direction="rtl"
  >
    <div class="filter">
      <span>{{ $t('commons.status')}}</span>
      <div class="filter-item">
        <span
          class="item"
          @click="statusChange(ele.id)"
          :class="[activeStatus.includes(ele.id) ? 'active' : '']"
          :key="ele.id"
          v-for="ele in status"
          >{{ $t(ele.label) }}</span>
      </div>
    </div>
    <div class="filter">
      <span>{{ $t('commons.organization')}}</span>
      <div class="filter-item">
        <span
          class="item"
          @click="activeDeptChange(ele.id)"
          :class="[activeDept.includes(ele.id) ? 'active' : '']"
          :key="ele.id"
          v-for="ele in selectDeptsCahe"
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
              :data="deptsComputed"
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
          <span class="more" slot="reference">+ {{ $t('panel.more')}}</span>
        </el-popover>
      </div>
    </div>
    <div class="filter">
      <span>{{ $t('panel.role')}}</span>
      <div class="filter-item">
        <span
          @click="activeRoleChange(ele.id)"
          class="item"
          :class="[activeRole.includes(ele.id) ? 'active' : '']"
          :key="ele.id"
          v-for="ele in rolesValueCopy"
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
              v-for="item in rolesComputed"
              :key="item.name"
              :label="item.name"
              :value="item"
            />
          </el-select>
          <span class="more" slot="reference">+ {{ $t('panel.more')}}</span>
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
      roleCahe: [],
      deptCahe: [],
      roles: [],
      filterTextMap: [],
      status: [{
        id: 1,
        label: 'commons.enable'
      },{
        id: 0,
        label: 'commons.disable'
      }],
      activeStatus: [],
      rolesValue: [],
      activeRole: [],
      depts: [],
      selectDepts: [],
      selectDeptsCahe: [],
      activeDept: [],
      defaultProps: {
        children: "children",
        label: "label",
        isLeaf: "leaf",
      },
      userDrawer: false
    };
  },
  computed: {
    rolesComputed() {
      return this.roles.filter(ele => !this.activeRole.includes(ele.id))
    },
    rolesValueCopy() {
      return this.roleCahe.filter(ele => this.activeRole.includes(ele.id))
    },
    deptsComputed() {
      return this.depts.filter(ele => !this.activeDept.includes(ele.id))
    }
  },
  mounted() {
    this.initRoles();
  },
  methods: {
    clearFilter() {
     Array(3).fill(1).forEach((_, index) => {
        this.clearOneFilter(index)
      })
      this.$emit('search', [], [])
    },
    clearOneFilter(index) {
      (this.filterTextMap[index] || []).forEach(ele => {
        this[ele] = []
      })
    },
    // 获取弹窗内部门数据
    treeByDeptId() {
      treeByDeptId(0).then((res) => {
        this.depts =  (res.data || []).map(ele =>  {
        return {
            ...ele,
            leaf: !ele.hasChildren,
          }
        })
      });
    },
    changeRole() {
      if (this.roleCahe.length > this.rolesValue.length + this.activeRole.length) {
        this.roleCahe = this.roleCahe.filter(ele => this.rolesValue.map(ele => ele.id).concat(this.activeRole).includes(ele.id));
        return;
      }
      const roleIdx = this.rolesValue.findIndex(ele => !this.roleCahe.map(ele => ele.id).concat(this.activeRole).includes(ele.id));
      if (roleIdx === -1) return
      this.activeRole.push(this.rolesValue[roleIdx].id)
      this.roleCahe.push(this.rolesValue[roleIdx]);
      this.rolesValue.splice(roleIdx, 1)
    },
    activeRoleChange(id) {
      const roleIndex = this.activeRole.findIndex((ele) => ele === id);
      if (roleIndex === -1) {
        this.activeRole.push(id);
        this.rolesValue = this.rolesValue.filter((ele) => ele.id !== id);
      } else {
        this.activeRole.splice(roleIndex, 1);
        const role = this.roleCahe.find((ele) => ele.id === id);
        this.rolesValue.push(role);
      }
    },
    handleNodeClick({ id, label }) {
      const deptIdx = this.selectDepts.findIndex((ele) => ele.id === id);
      if (deptIdx !== -1) {
        this.selectDepts.splice(deptIdx, 1);
        this.selectDeptsCahe = this.selectDeptsCahe.filter(ele => ele.id !== id)
        this.deptCahe = this.deptCahe.filter(ele => ele.id !== id)
        return;
      }
      this.activeDept.push(id)
      this.selectDeptsCahe.push({ id, label });
      this.deptCahe.push({ id, label });
    },
    activeDeptChange(id) {
      const dept = this.deptCahe.find((ele) => ele.id === id)
      this.selectDepts.push(dept);
      this.activeDept = this.activeDept.filter(ele => ele !== id)
      this.selectDeptsCahe = this.selectDeptsCahe.filter(ele => ele.id !== id)
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
        const filterDept = (res.data || []).filter(ele => !this.activeDept.includes(ele.deptId))
        resolve(
          filterDept.map((dept) => {
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
      this.$emit('search', this.formatCondition(), this.formatText())
    },
    formatText() {
      this.filterTextMap = [];
      const params = [];
      if (this.activeStatus.length) {
        let str = `${this.$t('kettle.status')}:${this.activeStatus.reduce((pre,next) => (this.status.find(ele => ele.id === next) || {}).label  + '、' +  pre, '')}`;
        params.push(str.slice(0, str.length - 1 ))
        this.filterTextMap.push(['activeStatus'])
      }
      if (this.activeDept.length) {
        params.push(`${this.$t('panel.org')}:${this.selectDeptsCahe.map(ele => ele.label).join('、')}`)
        this.filterTextMap.push(['activeDept', 'selectDepts', 'selectDeptsCahe', 'deptCahe'])
      }
      if (this.activeRole.length) {
        params.push(`${this.$t('panel.role')}:${this.rolesValueCopy.map(ele => ele.name).join('、')}`)
        this.filterTextMap.push(['rolesValue', 'activeRole', 'roleCahe'])
      }
      return params;
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
      this.userDrawer = false;
      this.clearFilter()
    },
  },
};
</script>

<style lang="scss">
.user-drawer {
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
      justify-content: center
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
      .more:hover{
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
    right: 24px;
    bottom: 24px;
    text-align: right;
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