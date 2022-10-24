<template>
  <el-row
    style="text-align: left"
    class="de-search-table"
  >
    <el-row class="top-operate">
      <el-col :span="12">
        <deBtn
          v-show="position==='templateLog'"
          type="primary"
          icon="el-icon-plus"
          @click="applyNew()"
        >{{ $t('commons.create') }}</deBtn>
        <span>&nbsp;</span>
      </el-col>
      <el-col
        :span="12"
        class="right-user"
      >
        <el-input
          ref="search"
          v-model="nickName"
          :placeholder="$t('app_template.search_by_keyword')"
          prefix-icon="el-icon-search"
          class="name-email-search"
          size="small"
          clearable
          @blur="initSearch"
          @clear="initSearch"
        />
        <deBtn
          :secondary="!cacheCondition.length"
          :plain="!!cacheCondition.length"
          icon="iconfont icon-icon-filter"
          @click="filterShow"
        >{{ $t("user.filter")
        }}<template v-if="filterTexts.length">
          ({{ cacheCondition.length }})
        </template>
        </deBtn>
      </el-col>
    </el-row>
    <div
      v-if="filterTexts.length"
      class="filter-texts"
    >
      <span class="sum">{{ paginationConfig.total }}</span>
      <span class="title">{{ $t("user.result_one") }}</span>
      <el-divider direction="vertical" />
      <i
        v-if="showScroll"
        class="el-icon-arrow-left arrow-filter"
        @click="scrollPre"
      />
      <div class="filter-texts-container">
        <p
          v-for="(ele, index) in filterTexts"
          :key="ele"
          class="text"
        >
          {{ ele }} <i
            class="el-icon-close"
            @click="clearOneFilter(index)"
          />
        </p>
      </div>
      <i
        v-if="showScroll"
        class="el-icon-arrow-right arrow-filter"
        @click="scrollNext"
      />
      <el-button
        type="text"
        class="clear-btn"
        icon="el-icon-delete"
        @click="clearFilter"
      >{{ $t("user.clear_filter") }}</el-button>
    </div>
    <div
      id="resize-for-filter"
      class="table-container"
      :class="[filterTexts.length ? 'table-container-filter' : '']"
    >
      <grid-table
        v-loading="$store.getters.loadingMap[$store.getters.currentPath]"
        :table-data="data"
        :columns="[]"
        :pagination="paginationConfig"
        @sort-change="sortChange"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      >
        <el-table-column
          show-overflow-tooltip
          prop="opType"
          :label="'数据源'"
        >
          <template #default="{ row }">
            <span>{{ row.datasourceName }}</span>
          </template>
        </el-table-column>
        <el-table-column
          show-overflow-tooltip
          prop="datasetGroupName"
          :label="'数据集分组'"
        />
        <el-table-column
          show-overflow-tooltip
          prop="panelName"
          :label="'仪表板'"
        />
        <el-table-column
          show-overflow-tooltip
          prop="appName"
          :label="'APPS'"
        />
        <el-table-column
          show-overflow-tooltip
          prop="applyTime"
          sortable="custom"
          :label="'应用时间'"
        >
          <template #default="scope">
            <span>{{ scope.row.applyTime | timestampFormatDate }}</span>
          </template>
        </el-table-column>
      </grid-table>
    </div>
    <keep-alive>
      <filterUser
        ref="filterUser"
        @search="filterDraw"
      />
    </keep-alive>
  </el-row>
</template>

<script>
import GridTable from '@/components/gridTable/index.vue'
import filterUser from './filterUser'
import _ from 'lodash'
import keyEnter from '@/components/msgCfm/keyEnter.js'
import {
  addOrder,
  formatOrders
} from '@/utils/index'
import { logGrid } from '@/api/appTemplateMarket/log'
export default {
  name: 'AppTemplateLog',
  components: { GridTable, filterUser },
  mixins: [keyEnter],
  props: {
    appTemplateId: {
      type: String,
      required: false
    },
    position: {
      type: String,
      required: true,
      default: 'allLog'
    }
  },
  data() {
    return {
      columns: [],
      paginationConfig: {
        currentPage: 1,
        pageSize: 10,
        total: 0
      },
      data: [],
      orderConditions: [],
      nickName: '',
      showScroll: false,
      filterTexts: [],
      cacheCondition: []
    }
  },
  watch: {
    filterTexts: {
      handler() {
        this.getScrollStatus()
      },
      deep: true
    },
    appTemplateId: {
      handler() {
        this.search()
      }
    }
  },
  mounted() {
    this.search()
    this.resizeObserver()
  },
  methods: {
    applyNew() {
      this.$emit('applyNew')
    },
    sortChange({ column, prop, order }) {
      this.orderConditions = []
      if (!order) {
        this.initSearch()
        return
      }
      this.orderConditions = []
      addOrder({ field: prop, value: order }, this.orderConditions)
      this.initSearch()
    },
    getScrollStatus() {
      this.$nextTick(() => {
        const dom = document.querySelector('.filter-texts-container')
        this.showScroll = dom && dom.scrollWidth > dom.offsetWidth
      })
    },
    resizeObserver() {
      this.resizeForFilter = new ResizeObserver((entries) => {
        if (!this.filterTexts.length) return
        this.layoutResize()
      })
      this.resizeForFilter.observe(
        document.querySelector('#resize-for-filter')
      )
    },
    layoutResize: _.debounce(function() {
      this.getScrollStatus()
    }, 200),
    scrollPre() {
      const dom = document.querySelector('.filter-texts-container')
      dom.scrollLeft -= 10
      if (dom.scrollLeft <= 0) {
        dom.scrollLeft = 0
      }
    },
    scrollNext() {
      const dom = document.querySelector('.filter-texts-container')
      dom.scrollLeft += 10
      const width = dom.scrollWidth - dom.offsetWidth
      if (dom.scrollLeft > width) {
        dom.scrollLeft = width
      }
    },
    handleSizeChange(pageSize) {
      this.paginationConfig.currentPage = 1
      this.paginationConfig.pageSize = pageSize
      this.search()
    },
    handleCurrentChange(currentPage) {
      this.paginationConfig.currentPage = currentPage
      this.search()
    },
    initSearch() {
      this.handleCurrentChange(1)
    },
    clearFilter() {
      this.$refs.filterUser.clearFilter()
    },
    clearOneFilter(index) {
      this.$refs.filterUser.clearOneFilter(index)
      this.$refs.filterUser.search()
    },
    filterDraw(condition, filterTexts = []) {
      this.cacheCondition = condition
      this.filterTexts = filterTexts
      this.initSearch()
    },
    filterShow() {
      this.$refs.filterUser.init()
    },
    search() {
      if (this.position === 'templateLog' && !this.appTemplateId) {
        return
      }
      const param = {
        orders: formatOrders(this.orderConditions),
        conditions: [...this.cacheCondition]
      }
      if (this.nickName) {
        param.keyWord = this.nickName
      }
      if (this.appTemplateId) {
        param.conditions.push({
          field: 'app_template_id',
          operator: 'eq',
          value: this.appTemplateId
        })
      }
      const { currentPage, pageSize } = this.paginationConfig
      logGrid(currentPage, pageSize, param).then((response) => {
        this.data = response.data.listObject
        this.paginationConfig.total = response.data.itemCount
      })
    }
  }
}
</script>
<style lang="scss" scoped>
.table-container {
  height: calc(100% - 50px);
}

.table-container-filter {
  height: calc(100% - 110px);
}
</style>
