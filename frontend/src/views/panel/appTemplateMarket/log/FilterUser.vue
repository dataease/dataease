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
        <span>{{ $t("dedaterange.label") }}</span>
        <div class="filter-item">
          <DeDatePick v-model="dataRange" />
        </div>
      </div>
    </div>
    <div class="foot">
      <el-button
        class="btn normal"
        @click="reset"
      >{{
        $t("commons.reset")
      }}</el-button>
      <el-button
        type="primary"
        class="btn"
        @click="search"
      >{{
        $t("commons.adv_search.search")
      }}</el-button>
    </div>
  </el-drawer>
</template>

<script>
import { dateFormat } from '@/views/system/task/options.js'
import DeDatePick from '@/components/deCustomCm/DeDatePick.vue'
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
      activeType: [],
      userDrawer: false
    }
  },
  computed: {
  },
  mounted() {
  },
  methods: {
    clearFilter() {
      this.dataRange = []
      this.$emit('search', [], [])
    },
    clearOneFilter(index) {
      (this.filterTextMap[index] || []).forEach((ele) => {
        this[ele] = []
      })
    },
    search() {
      this.userDrawer = false
      this.$emit('search', this.formatCondition(), this.formatText())
    },
    formatText() {
      this.filterTextMap = []
      const params = []
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
      const conditions = []
      // eslint-disable-next-line
      let [min, max] = this.dataRange
      if (min && max) {
        if (+min === +max) {
          max = +max + 24 * 3600 * 1000
        }
        conditions.push({
          field: 'apply_time',
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
