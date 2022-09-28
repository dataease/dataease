<template>
  <de-layout-content :header="$t('log.title')">
    <el-row class="top-operate">
      <el-col :span="12">
        <deBtn v-permission="['log:export']" secondary @click="exportConfirm">{{
          $t("zip.export")
        }}</deBtn>
        &nbsp; &nbsp;
      </el-col>
      <el-col :span="12" class="right-user">
        <el-input
          ref="search"
          v-model="nickName"
          :placeholder="$t('system_parameter_setting.by_event_details')"
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
    <div v-if="filterTexts.length" class="filter-texts">
      <span class="sum">{{ paginationConfig.total }}</span>
      <span class="title">{{ $t("user.result_one") }}</span>
      <el-divider direction="vertical" />
      <i
        v-if="showScroll"
        class="el-icon-arrow-left arrow-filter"
        @click="scrollPre"
      />
      <div class="filter-texts-container">
        <p v-for="(ele, index) in filterTexts" :key="ele" class="text">
          {{ ele }} <i class="el-icon-close" @click="clearOneFilter(index)" />
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
          :label="$t('log.optype')"
          width="140"
        >
          <template v-slot:default="{ row }">
            <span>{{ row.opType + row.sourceType }}</span>
          </template>
        </el-table-column>
        <el-table-column
          show-overflow-tooltip
          prop="detail"
          :label="$t('log.detail')"
        />
        <el-table-column
          show-overflow-tooltip
          prop="user"
          :label="$t('log.user')"
          width="100"
        />
        <el-table-column
          show-overflow-tooltip
          prop="time"
          sortable="custom"
          :label="$t('log.time')"
          width="180"
        >
          <template v-slot:default="scope">
            <span>{{ scope.row.time | timestampFormatDate }}</span>
          </template>
        </el-table-column>
      </grid-table>
    </div>
    <keep-alive>
      <filterUser ref="filterUser" @search="filterDraw" />
    </keep-alive>
  </de-layout-content>
</template>

<script>
import DeLayoutContent from '@/components/business/DeLayoutContent'
import GridTable from '@/components/gridTable/index.vue'
import filterUser from './filterUser'
import _ from 'lodash'
import keyEnter from '@/components/msgCfm/keyEnter.js'
import {
  addOrder,
  formatOrders
} from '@/utils/index'
import { logGrid, exportExcel } from '@/api/system/log'
export default {
  components: { GridTable, DeLayoutContent, filterUser },
  mixins: [keyEnter],
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
    }
  },
  mounted() {
    this.search()
    this.resizeObserver()
  },
  methods: {
    exportConfirm() {
      this.$confirm(this.$t('log.confirm'), '', {
        confirmButtonText: this.$t('commons.confirm'),
        cancelButtonText: this.$t('commons.cancel'),
        type: 'warning'
      })
        .then(() => {
          this.exportData()
        })
        .catch(() => {
          // this.$info(this.$t('commons.delete_cancel'))
        })
    },
    exportData() {
      const param = {
        orders: formatOrders(this.orderConditions),
        conditions: [...this.cacheCondition]
      }
      if (this.nickName) {
        param.keyWord = this.nickName
      }

      exportExcel(param).then((res) => {
        const blob = new Blob([res], { type: 'application/vnd.ms-excel' })
        const link = document.createElement('a')
        link.style.display = 'none'
        link.href = URL.createObjectURL(blob)
        link.download = 'DataEase操作日志.xls' // 下载的文件名
        document.body.appendChild(link)
        link.click()
        document.body.removeChild(link)
      })
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
      const param = {
        orders: formatOrders(this.orderConditions),
        conditions: [...this.cacheCondition]
      }
      if (this.nickName) {
        param.keyWord = this.nickName
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
.filter-texts {
  display: flex;
  align-items: center;
  margin: 17px 0;
  font-family: "PingFang SC";
  font-weight: 400;

  .sum {
    color: #1f2329;
  }

  .title {
    color: #999999;
    margin-left: 8px;
  }

  .text {
    max-width: 280px;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
    padding: 1px 22px 1px 6px;
    display: inline-block;
    align-items: center;
    color: #0c296e;
    font-size: 14px;
    line-height: 22px;
    background: rgba(51, 112, 255, 0.1);
    border-radius: 2px;
    margin: 0;
    margin-right: 8px;
    position: relative;
    i {
      position: absolute;
      right: 2px;
      top: 50%;
      transform: translateY(-50%);
      cursor: pointer;
    }
  }

  .clear-btn {
    color: #646a73;
  }

  .clear-btn:hover {
    color: #3370ff;
  }

  .filter-texts-container::-webkit-scrollbar {
    display: none;
  }

  .arrow-filter {
    font-size: 16px;
    width: 24px;
    height: 24px;
    cursor: pointer;
    color: #646a73;
    display: flex;
    justify-content: center;
    align-items: center;
  }
  .arrow-filter:hover {
    background: rgba(31, 35, 41, 0.1);
    border-radius: 4px;
  }

  .el-icon-arrow-right.arrow-filter {
    margin-left: 5px;
  }

  .el-icon-arrow-left.arrow-filter {
    margin-right: 5px;
  }
  .filter-texts-container {
    flex: 1;
    overflow-x: auto;
    white-space: nowrap;
    height: 24px;
  }
}
.top-operate {
  margin-bottom: 16px;
  .right-user {
    text-align: right;
    display: flex;
    align-items: center;
    justify-content: flex-end;

    .de-button {
      margin-left: 12px;
    }

    .el-input--medium .el-input__icon {
      line-height: 32px;
    }
  }

  .name-email-search {
    width: 240px;
  }
}
</style>
