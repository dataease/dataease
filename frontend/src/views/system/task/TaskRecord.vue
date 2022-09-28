<template>
  <div class="dataset-on-time">
    <el-row class="top-operate">
      <el-col :span="10">
        <deBtn secondary @click="exportConfirm">{{ $t("zip.export") }}</deBtn>
      </el-col>
      <el-col :span="14" class="right-user">
        <el-input
          ref="search"
          v-model="nickName"
          :placeholder="$t('components.by_task_name')"
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
    <div id="resize-for-filter" class="table-container">
      <grid-table
        v-loading="$store.getters.loadingMap[$store.getters.currentPath]"
        :table-data="data"
        :columns="[]"
        :pagination="paginationConfig"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      >
        <el-table-column prop="name" :label="$t('dataset.task_name')">
          <template slot-scope="scope">
            <span>
              <el-link
                :type="
                  matchLogId && scope.row.id === matchLogId ? 'danger' : ''
                "
                style="font-size: 12px"
                @click="jumpTask(scope.row)"
              >{{ scope.row.name }}</el-link>
            </span>
          </template>
        </el-table-column>
        <el-table-column
          prop="datasetName"
          :label="$t('dataset.task.dataset')"
        />
        <el-table-column prop="startTime" :label="$t('dataset.start_time')">
          <template slot-scope="scope">
            <span>{{ scope.row.startTime | timestampFormatDate }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="endTime" :label="$t('dataset.end_time')">
          <template slot-scope="scope">
            <span>{{ scope.row.endTime | timestampFormatDate }}</span>
          </template>
        </el-table-column>

        <el-table-column prop="status" :label="$t('dataset.status')">
          <template slot-scope="scope">
            <span
              v-if="scope.row.status"
              :class="[`de-${scope.row.status}-pre`, 'de-status']"
              >{{ $t(`dataset.${scope.row.status.toLocaleLowerCase()}`) }}
            <svg-icon style="cursor: pointer;" v-if="scope.row.status === 'Error'"  @click="showErrorMassage(scope.row.info)" icon-class="icon-maybe" class="field-icon-location" />
            </span>
            <span v-else>-</span>
          </template>
        </el-table-column>
      </grid-table>
      <keep-alive>
        <filterUser ref="filterUser" @search="filterDraw" />
      </keep-alive>
    </div>
    <el-dialog
      v-dialogDrag
      :title="$t('dataset.error') + $t('dataset.detail')"
      :visible.sync="show_error_massage"
      width="600px"
      class="de-dialog-form"
    >
      <span class="err-msg">{{ error_massage }}</span>
      <span slot="footer" class="dialog-footer">
        <deBtn secondary @click="show_error_massage = false">{{
          $t("dataset.close")
        }}</deBtn>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import {
  formatCondition,
  formatOrders,
  formatQuickCondition
} from '@/utils/index'
import { exportExcel, post } from '@/api/dataset/dataset'
import GridTable from '@/components/gridTable/index.vue'
import filterUser from './filterUserRecord.vue'
import _ from 'lodash'
import keyEnter from '@/components/msgCfm/keyEnter.js'

export default {
  name: 'TaskRecord',
  components: { GridTable, filterUser },
  mixins: [keyEnter],
  props: {
    param: {
      type: Object,
      default: () => {}
    },
    transCondition: {
      type: Object,
      default: () => {}
    }
  },
  data() {
    return {
      columns: [],
      nickName: '',
      showScroll: false,
      filterTexts: [],
      cacheCondition: [],
      paginationConfig: {
        currentPage: 1,
        pageSize: 10,
        total: 0
      },
      data: [],
      orderConditions: [],
      show_error_massage: false,
      error_massage: '',
      matchLogId: null,
      lastRequestComplete: true
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
  created() {
    const { taskId: id, name: label } = this.transCondition
    if (id) {
      this.nickName = label
    }
    const { taskId, name, logId } = (this.param || {})
    if (this.param !== null && taskId) {
      this.matchLogId = logId || this.matchLogId
      this.transCondition.taskId = taskId
      this.transCondition.name = name
      this.nickName = name
    }
    this.createTimer()
  },
  mounted() {
    this.resizeObserver()
  },
  beforeDestroy() {
    this.destroyTimer()
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
      const { taskId, name } = this.transCondition
      const param = {
        orders: formatOrders(this.orderConditions),
        conditions: [...this.cacheCondition]
      }
      if (this.nickName) {
        param.conditions.push({
          field: `dataset_table_task.name`,
          operator: 'like',
          value: this.nickName
        })
      }
      if (taskId && this.nickName === name) {
        param.conditions.push({
          operator: 'eq',
          value: taskId,
          field: 'dataset_table_task.id'
        })
      }
      exportExcel(param).then((res) => {
        const blob = new Blob([res], { type: 'application/vnd.ms-excel' })
        const link = document.createElement('a')
        link.style.display = 'none'
        link.href = URL.createObjectURL(blob)
        link.download = 'DataEase' + this.$t('dataset.sync_log') + '.xls'
        document.body.appendChild(link)
        link.click()
        document.body.removeChild(link)
      })
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
    createTimer() {
      this.initSearch()
      if (!this.timer) {
        this.timer = setInterval(() => {
          this.timerSearch(false)
        }, 15000)
      }
    },
    destroyTimer() {
      if (this.timer) {
        clearInterval(this.timer)
        this.timer = null
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
    timerSearch(showLoading = true) {
      if (!this.lastRequestComplete) {
        return
      } else {
        this.lastRequestComplete = false
      }
      const { taskId, name } = this.transCondition
      const param = {
        orders: formatOrders(this.orderConditions),
        conditions: [...this.cacheCondition]
      }
      if (this.nickName) {
        param.conditions.push({
          field: `dataset_table_task.name`,
          operator: 'like',
          value: this.nickName
        })
      }
      if (taskId && this.nickName === name) {
        param.conditions.push({
          operator: 'eq',
          value: taskId,
          field: 'dataset_table_task.id'
        })
      }
      post(
        '/dataset/taskLog/list/notexcel/' +
          this.paginationConfig.currentPage +
          '/' +
          this.paginationConfig.pageSize,
        param,
        showLoading
      )
        .then((response) => {
          this.data = response.data.listObject
          this.paginationConfig.total = response.data.itemCount
          this.lastRequestComplete = true
        })
        .catch(() => {
          this.lastRequestComplete = true
        })
    },
    search(condition, showLoading = true) {
      const { taskId, name } = this.transCondition
      const param = {
        orders: formatOrders(this.orderConditions),
        conditions: [...this.cacheCondition]
      }
      if (this.nickName) {
        param.conditions.push({
          field: `dataset_table_task.name`,
          operator: 'like',
          value: this.nickName
        })
      }
      if (taskId && this.nickName === name) {
        param.conditions.push({
          operator: 'eq',
          value: taskId,
          field: 'dataset_table_task.id'
        })
      }
      post(
        '/dataset/taskLog/list/notexcel/' +
          this.paginationConfig.currentPage +
          '/' +
          this.paginationConfig.pageSize,
        param,
        showLoading
      ).then((response) => {
        this.data = response.data.listObject
        this.paginationConfig.total = response.data.itemCount
      })
    },
    showErrorMassage(massage) {
      this.show_error_massage = true
      this.error_massage = massage
    },
    jumpTask(item) {
      this.$emit('jumpTask', item)
    },
    rowClassMethod({ row, rowIndex }) {
      if (this.matchLogId && this.matchLogId === row.id) {
        return 'row-match-class'
      }
      return ''
    }
  }
}
</script>

<style scoped>
.el-divider--horizontal {
  margin: 12px 0;
}

.el-radio {
  margin-right: 10px;
}
.el-radio ::v-deep .el-radio__label {
  font-size: 12px;
}

.dialog-css ::v-deep .el-dialog__header {
  padding: 20px 20px 0;
}

.dialog-css ::v-deep .el-dialog__body {
  padding: 10px 20px 20px;
}

.el-form-item {
  margin-bottom: 10px;
}

.err-msg {
  font-size: 12px;
  word-break: normal;
  width: auto;
  display: block;
  white-space: pre-wrap;
  word-wrap: break-word;
  overflow: hidden;
}

span {
  font-size: 12px;
}
</style>
<style lang="scss" scoped>
.dataset-on-time {
  margin: 0;
  width: 100%;
  overflow: auto;
  background-color: var(--ContentBG, #fff);
  padding: 24px;
  height: 100%;
}
.table-container {
  height: calc(100% - 50px);

  .text-btn {
    font-family: PingFang SC;
    font-size: 14px;
    font-weight: 400;
    line-height: 22px;
    letter-spacing: 0px;
    text-align: center;
    margin-left: 2px;
    border: none;
    padding: 2px 4px;
  }

  .text-btn:hover {
    background: rgba(51, 112, 255, 0.1);
  }

  .mar6 {
    margin-right: 6px;
  }

  .mar3 {
    margin-left: -3px;
  }
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
