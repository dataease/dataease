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
        <span>{{ $t("dataset.datalist") }}</span>
        <div class="filter-item">
          <span
            v-for="ele in selectDatasetsCache"
            :key="ele.id"
            class="item"
            :class="[activeDataset.includes(ele.id) ? 'active' : '']"
            @click="activeDatasetChange(ele.id)"
          >{{ ele.name }}</span>
          <el-popover
            placement="bottom"
            popper-class="user-popper"
            width="200"
            trigger="click"
          >
            <el-popover
              v-loading="treeLoading"
              placement="bottom"
              popper-class="user-popper dept"
              width="200"
              trigger="click"
            >
              <el-tree
                ref="datasetTreeRef"
                current-node-key="id"
                :data="datasetComputed"
                node-key="id"
                highlight-current
                :filter-node-method="filterNode"
                :expand-on-click-node="true"
                @node-click="nodeClick"
              >
                <span
                  slot-scope="{ data }"
                  class="custom-tree-node"
                >
                  <span v-if="data.modelInnerType === 'group'">
                    <svg-icon icon-class="scene" />
                    <span
                      style="
                        margin-left: 6px;
                        white-space: nowrap;
                        overflow: hidden;
                        text-overflow: ellipsis;
                      "
                      :title="data.name"
                    >{{ data.name }}</span>
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
                    >{{ data.name }}</span>
                  </span>
                </span>
              </el-tree>

              <el-input
                slot="reference"
                v-model="selectDatasets"
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
      <div
        v-for="ele in filterDataset"
        :key="ele.name"
        class="filter"
      >
        <span>{{ $t(ele.name) }}</span>
        <div class="filter-item">
          <span
            v-for="item in ele.list"
            :key="item.name"
            class="item"
            :class="[
              active[ele.activeType].includes(item.value) ? 'active' : '',
            ]"
            @click="statusChange(item.value, ele.activeType)"
          >{{ $t(item.name) }}</span>
        </div>
      </div>

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
import { filterDatasetRecord, dateFormat } from './options'
import { queryAuthModel } from '@/api/authModel/authModel'
import DeDatePick from '@/components/deCustomCm/DeDatePick.vue'
import _ from 'lodash'
export default {
  components: {
    DeDatePick
  },
  data() {
    return {
      treeLoading: false,
      filterTextMap: [],
      dataRange: [],
      selectDatasets: '',
      activeDataset: [],
      selectDatasetsCache: [],
      treeData: [],
      filterDataset: [filterDatasetRecord],
      active: {
        execStatus: []
      },
      userDrawer: false
    }
  },
  computed: {
    datasetComputed() {
      return this.dfs(_.cloneDeep(this.treeData), this.activeDataset)
    }
  },
  watch: {
    selectDatasets(val) {
      this.$refs.selectDatasets.filter(val)
    }
  },
  mounted() {
    this.treeNode()
  },
  methods: {
    treeNode() {
      this.treeLoading = true
      queryAuthModel(
        {
          modelType: 'dataset',
          privileges: 'manage',
          datasetMode: 1,
          clearEmptyDir: true,
          mode: 1,
          modelInnerTypeArray: ['db', 'sql', 'api', 'group']
        },
        true
      )
        .then((res) => {
          this.treeData = res.data
        })
        .finally(() => {
          this.treeLoading = false
        })
    },
    dfs(arr, target) {
      return arr.reduce((pre, ele) => {
        if (!target.includes(ele.id)) {
          if (ele.children?.length) {
            ele.children = this.dfs(ele.children, target)
          }
          pre.push(ele)
        }
        return pre
      }, [])
    },
    nodeClick(data) {
      const { id, name, modelInnerType: type } = data
      if (type === 'group') return
      this.handleNodeClick(id, name)
    },
    filterNode(value, data) {
      if (!value) return true
      return data.label.indexOf(value) !== -1
    },
    clearFilter() {
      this.active = {
        execStatus: []
      }
      this.dataRange = []
      this.activeDataset = []
      this.selectDatasets = ''
      this.selectDatasetsCache = []
      this.$refs.datasetTreeRef.filter()
      this.$emit('search', [], [])
    },
    clearOneFilter(index) {
      (this.filterTextMap[index] || []).forEach((ele) => {
        const eleKey = ele.split('.')
        if (eleKey.length === 2) {
          const [p, c] = eleKey
          this[p][c] = []
        } else {
          this[ele] = []
        }
        if (ele === 'activeDataset') {
          this.$refs.datasetTreeRef.filter()
        }
      })
    },
    statusChange(value, type) {
      const statusIndex = this.active[type].findIndex((ele) => ele === value)
      if (statusIndex === -1) {
        this.active[type].push(value)
      } else {
        this.active[type].splice(statusIndex, 1)
      }
    },
    handleNodeClick(id, name) {
      this.activeDataset.push(id)
      this.selectDatasetsCache.push({ id, name })
      this.selectDatasets = ''
    },
    activeDatasetChange(id) {
      this.activeDataset = this.activeDataset.filter((ele) => ele !== id)
      this.selectDatasetsCache = this.selectDatasetsCache.filter(
        (ele) => ele.id !== id
      )
    },
    search() {
      this.userDrawer = false
      this.$emit('search', this.formatCondition(), this.formatText())
    },
    formatText() {
      this.selectDatasets = ''
      this.filterTextMap = []
      const params = []
      if (this.activeDataset.length) {
        const str = `${this.$t('dataset.datalist')}:${this.activeDataset.reduce(
          (pre, next) =>
            (this.selectDatasetsCache.find((ele) => ele.id === next) || {}).name +
            '、' +
            pre,
          ''
        )}`
        params.push(str.slice(0, str.length - 1))
        this.filterTextMap.push([
          'activeDataset',
          'selectDatasetsCache'
        ])
      }
      ['dataset.task.last_exec_status'].forEach((ele, index) => {
        const { activeType: type, list } = this.filterDataset[index]
        if (this.active[type].length) {
          params.push(
            `${this.$t(ele)}:${this.active[type]
              .map((item) =>
                this.$t(list.find((itx) => itx.value === item).name)
              )
              .join('、')}`
          )
          this.filterTextMap.push([`active.${type}`])
        }
      })
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
        'dataset_table_task.last_exec_status': this.active.execStatus,
        'dataset_table.id': this.activeDataset
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
          field: 'dataset_table_task.last_exec_time',
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
