<template>
  <el-drawer
    v-closePress
    :title="$t('user.filter_method')"
    :visible.sync="userDrawer"
    custom-class="de-user-drawer"
    size="680px"
    direction="rtl"
  >
    <div class="el-drawer__body-cont">
      <div class="filter">
        <span>{{ $t("commons.status") }}</span>
        <div class="filter-item">
          <span
            v-for="ele in status"
            :key="ele.id"
            class="item"
            :class="[activeStatus.includes(ele.id) ? 'active' : '']"
            @click="statusChange(ele.id)"
          >{{ $t(ele.label) }}</span>
        </div>
      </div>
      <div class="filter">
        <span>{{ $t("commons.organization") }}</span>
        <div class="filter-item">
          <span
            v-for="ele in selectDeptsCache"
            :key="ele.id"
            class="item"
            :class="[activeDept.includes(ele.id) ? 'active' : '']"
            @click="activeDeptChange(ele.id)"
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
                ref="tree"
                :load="loadNode"
                :lazy="true"
                :expand-on-click-node="false"
                :data="deptsComputed"
                :props="defaultProps"
                :filter-node-method="filterNode"
                @node-click="handleNodeClick"
              />

              <el-input
                slot="reference"
                v-model="selectDepts"
                :placeholder="$t('commons.please_select')"
                clearable
              />
            </el-popover>
            <span
              slot="reference"
              class="more"
            >+ {{ $t("panel.more") }}</span>
          </el-popover>
        </div>
      </div>
      <div class="filter">
        <span>{{ $t("panel.role") }}</span>
        <div class="filter-item">
          <span
            v-for="ele in rolesValueCopy"
            :key="ele.id"
            class="item active"
            @click="activeRoleChange(ele.id)"
          >{{ ele.name }}</span>
          <el-popover
            placement="bottom"
            popper-class="user-popper"
            width="200"
            trigger="click"
          >
            <el-select
              ref="select"
              v-model="rolesValue"
              :placeholder="$t('commons.please_select')"
              value-key="id"
              filterable
              multiple
              @change="changeRole"
            >
              <el-option
                v-for="item in rolesComputed"
                :key="item.name"
                :label="item.name"
                :value="item"
              />
            </el-select>
            <span
              slot="reference"
              class="more"
            >+ {{ $t("panel.more") }}</span>
          </el-popover>
        </div>
      </div>
    </div>
    <div class="de-foot">
      <deBtn
        secondary
        @click="reset"
      >{{
        $t("commons.reset")
      }}</deBtn>
      <deBtn
        type="primary"
        @click="search"
      >{{
        $t("commons.adv_search.search")
      }}</deBtn>
    </div>
  </el-drawer>
</template>

<script>
import { allRoles } from '@/api/system/user'
import { getDeptTree, treeByDeptId } from '@/api/system/dept'

export default {
  data() {
    return {
      status: [
        {
          id: 1,
          label: 'commons.enable'
        },
        {
          id: 0,
          label: 'commons.disable'
        }
      ],
      roles: [],
      filterTextMap: [],
      activeStatus: [],
      rolesValue: [],
      activeRole: [],
      depts: [],
      selectDepts: '',
      selectDeptsCache: [],
      activeDept: [],
      defaultProps: {
        children: 'children',
        label: 'label',
        isLeaf: 'leaf'
      },
      userDrawer: false
    }
  },
  computed: {
    rolesComputed() {
      return this.roles.filter((ele) => !this.activeRole.includes(ele.id))
    },
    rolesValueCopy() {
      return this.roles.filter((ele) => this.activeRole.includes(ele.id))
    },
    deptsComputed() {
      return this.dfs(this.depts)
    }
  },
  watch: {
    selectDepts(val) {
      this.$refs.tree.filter(val)
    }
  },
  mounted() {
    this.initRoles()
  },
  methods: {
    filterNode(value, data) {
      if (!value) return true
      return data.label.indexOf(value) !== -1
    },
    clearFilter() {
      Array(3)
        .fill(1)
        .forEach((_, index) => {
          this.clearOneFilter(index)
        })
      this.$refs.tree.filter()
      this.$emit('search', [], [])
    },
    clearOneFilter(index) {
      (this.filterTextMap[index] || []).forEach((ele) => {
        this[ele] = []
        if (ele === 'activeDept') {
          this.$refs.tree.filter()
        }
      })
    },
    // 获取弹窗内部门数据
    treeByDeptId() {
      treeByDeptId(0).then((res) => {
        this.depts = (res.data || []).map((ele) => {
          return {
            ...ele,
            leaf: !ele.hasChildren
          }
        })
      })
    },
    dfs(arr) {
      return arr.reduce((pre, ele) => {
        if (!this.activeDept.includes(ele.id)) {
          if (ele.children?.length) {
            ele.children = this.dfs(ele.children)
          }
          pre.push(ele)
        }
        return pre
      }, [])
    },
    changeRole(list) {
      const [val] = list
      this.activeRole.push(val.id)
      this.rolesValue = []
      const { query } = this.$refs.select
      this.$nextTick(() => {
        this.$refs.select.query = query
        this.$refs.select.handleQueryChange(query)
      })
    },
    activeRoleChange(id) {
      this.activeRole = this.activeRole.filter((ele) => ele !== id)
    },
    handleNodeClick({ id, label }) {
      this.activeDept.push(id)
      this.selectDeptsCache.push({ id, label })
      this.$nextTick(() => {
        this.$refs.tree.filter(this.selectDatasets)
      })
    },
    activeDeptChange(id) {
      this.activeDept = this.activeDept.filter((ele) => ele !== id)
      this.selectDeptsCache = this.selectDeptsCache.filter((ele) => ele.id !== id)
    },
    statusChange(id) {
      const statusIndex = this.activeStatus.findIndex((ele) => ele === id)
      if (statusIndex === -1) {
        this.activeStatus.push(id)
      } else {
        this.activeStatus.splice(statusIndex, 1)
      }
    },
    loadNode(node, resolve) {
      if (!this.depts.length) {
        this.treeByDeptId()
        return
      }
      getDeptTree(node.data.id).then((res) => {
        const filterDept = (res.data || []).filter(
          (ele) => !this.activeDept.includes(ele.deptId)
        )
        resolve(
          filterDept.map((dept) => {
            return this.normalizer(dept)
          })
        )
      })
    },
    normalizer(node) {
      return {
        id: node.deptId,
        label: node.name,
        leaf: !node.hasChildren
      }
    },
    initRoles() {
      allRoles().then((res) => {
        this.roles = res.data
      })
    },
    search() {
      this.userDrawer = false
      this.$emit('search', this.formatCondition(), this.formatText())
    },
    formatText() {
      this.selectDepts = ''
      this.filterTextMap = []
      const params = []
      if (this.activeStatus.length) {
        const str = `${this.$t('kettle.status')}:${this.activeStatus.reduce(
          (pre, next) =>
            this.$t((this.status.find((ele) => ele.id === next) || {}).label) +
            '、' +
            pre,
          ''
        )}`
        params.push(str.slice(0, str.length - 1))
        this.filterTextMap.push(['activeStatus'])
      }
      if (this.activeDept.length) {
        params.push(
          `${this.$t('panel.org')}:${this.selectDeptsCache
            .map((ele) => ele.label)
            .join('、')}`
        )
        this.filterTextMap.push([
          'activeDept',
          'selectDeptsCache'
        ])
      }
      if (this.activeRole.length) {
        params.push(
          `${this.$t('panel.role')}:${this.rolesValueCopy
            .map((ele) => ele.name)
            .join('、')}`
        )
        this.filterTextMap.push(['activeRole', 'rolesValue'])
      }
      return params
    },
    formatCondition() {
      const fildMap = {
        'r.role_id': this.activeRole,
        'd.dept_id': this.activeDept,
        'u.enabled': this.activeStatus
      }
      const conditions = []
      Object.keys(fildMap).forEach((ele) => {
        if (fildMap[ele].length) {
          conditions.push({
            field: ele,
            operator: 'in',
            value: fildMap[ele]
          })
        }
      })
      return conditions
    },
    init() {
      this.userDrawer = true
    },
    reset() {
      this.userDrawer = false
      this.clearFilter()
    }
  }
}
</script>
