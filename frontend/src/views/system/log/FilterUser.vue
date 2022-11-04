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
        <span>{{ $t("log.optype") }}</span>
        <div class="filter-item">
          <span
            v-for="item in types"
            :key="item.label"
            class="item"
            :class="[activeType.includes(item.value) ? 'active' : '']"
            @click="statusChange(item.value)"
          >{{ $t(item.label) }}</span>
        </div>
      </div>
      <div class="filter">
        <span>{{ $t("log.user") }}</span>
        <div class="filter-item">
          <span
            v-for="ele in usersValueCopy"
            :key="ele.id"
            class="item"
            :class="[activeUser.includes(ele.id) ? 'active' : '']"
            @click="activeUserChange(ele.id)"
          >{{ ele.username }}</span>
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
              value-key="id"
              @change="changeUser"
              @remove-tag="changeUser"
            >
              <el-option
                v-for="item in usersComputed"
                :key="item.username"
                :label="item.username"
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
      <div class="filter">
        <span>{{ $t("dedaterange.label") }}</span>
        <div class="filter-item">
          <DeDatePick v-model="dataRange" />
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
import { dateFormat } from '@/views/system/task/options.js'
import { opTypes } from '@/api/system/log'
import { post } from '@/api/dataset/dataset'
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
      userCache: [],
      activeType: [],
      userDrawer: false
    }
  },
  computed: {
    usersComputed() {
      return this.users.filter((ele) => !this.activeUser.includes(ele.id))
    },
    usersValueCopy() {
      return this.userCache.filter((ele) => this.activeUser.includes(ele.id))
    }
  },
  mounted() {
    this.loadUsers()
    this.loadType()
  },
  methods: {
    loadType() {
      opTypes().then((res) => {
        this.types = res.data.map((item) => {
          return { label: item.name, value: item.id }
        })
      })
    },
    loadUsers() {
      post('/api/user/userLists', {}, false).then((res) => {
        this.users = res.data
      })
    },
    changeUser() {
      if (
        this.userCache.length >
        this.usersValue.length + this.activeUser.length
      ) {
        this.userCache = this.userCache.filter((ele) =>
          this.usersValue
            .map((ele) => ele.id)
            .concat(this.activeUser)
            .includes(ele.id)
        )
        return
      }
      const userIdx = this.usersValue.findIndex(
        (ele) =>
          !this.userCache
            .map((ele) => ele.id)
            .concat(this.activeUser)
            .includes(ele.id)
      )
      if (userIdx === -1) return
      this.activeUser.push(this.usersValue[userIdx].id)
      this.userCache.push(this.usersValue[userIdx])
      this.usersValue.splice(userIdx, 1)
    },
    activeUserChange(id) {
      const userIndex = this.activeUser.findIndex((ele) => ele === id)
      if (userIndex === -1) {
        this.activeUser.push(id)
        this.usersValue = this.usersValue.filter((ele) => ele.id !== id)
      } else {
        this.activeUser.splice(userIndex, 1)
        const user = this.userCache.find((ele) => ele.id === id)
        this.usersValue.push(user)
      }
    },
    clearFilter() {
      this.dataRange = []
      this.usersValue = []
      this.activeUser = []
      this.activeType = []
      this.userCache = []
      this.$emit('search', [], [])
    },
    clearOneFilter(index) {
      (this.filterTextMap[index] || []).forEach((ele) => {
        this[ele] = []
      })
    },
    statusChange(value, type) {
      const statusIndex = this.activeType.findIndex((ele) => ele === value)
      if (statusIndex === -1) {
        this.activeType.push(value)
      } else {
        this.activeType.splice(statusIndex, 1)
      }
    },
    search() {
      this.userDrawer = false
      this.$emit('search', this.formatCondition(), this.formatText())
    },
    formatText() {
      this.filterTextMap = []
      const params = []
      if (this.activeType.length) {
        params.push(
          `${this.$t('log.optype')}:${this.activeType
            .map((item) =>
              this.types.find((itx) => itx.value === item).label
            )
            .join('、')}`
        )
        this.filterTextMap.push([`activeType`])
      }
      if (this.activeUser.length) {
        const str = `${this.$t('log.user')}:${this.activeUser.reduce(
          (pre, next) =>
            (this.users.find((ele) => ele.id === next) || {}).username +
            '、' +
            pre,
          ''
        )}`
        params.push(str.slice(0, str.length - 1))
        this.filterTextMap.push([
          'usersValue',
          'activeUser',
          'userCache'
        ])
      }

      if (this.dataRange.length) {
        params.push(
          `${this.$t('dedaterange.label')}:${this.dataRange
            .map((ele) => {
              return dateFormat('YYYY-mm-dd', ele)
            })
            .join('-')}`
        )
        this.filterTextMap.push(['dataRange'])
      }
      return params
    },
    formatCondition() {
      const fildMap = {
        optype: this.activeType,
        'user_id': this.activeUser
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
      // eslint-disable-next-line
      let [min, max] = this.dataRange
      if (min && max) {
        if (+min === +max) {
          max = +max + 24 * 3600 * 1000
        }
        conditions.push({
          field: 'time',
          operator: 'between',
          value: [+min, +max]
        })
      }
      return conditions
    },
    init() {
      this.userDrawer = true
    },
    reset() {
      this.clearFilter()
      this.userDrawer = false
    }
  }
}
</script>
