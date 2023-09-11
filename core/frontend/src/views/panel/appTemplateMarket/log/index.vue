<template>
  <el-row
    v-loading="$store.getters.loadingMap[$store.getters.currentPath]"
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
        >{{ $t('commons.create') }}
        </deBtn>
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
        >{{
           $t('user.filter')
         }}
          <template v-if="filterTexts.length">
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
      <span class="title">{{ $t('user.result_one') }}</span>
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
      >{{ $t('user.clear_filter') }}
      </el-button>
    </div>
    <div
      id="resize-for-filter"
      class="table-container"
      :class="[filterTexts.length ? 'table-container-filter' : '']"
    >
      <grid-table
        :ref="'grid-table'"
        :table-data="data"
        :columns="[]"
        :pagination="paginationConfig"
        @sort-change="sortChange"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      >
        <el-table-column
          show-overflow-tooltip
          prop="datasourceName"
          :label="$t('app_template.datasource')"
        >
          <template #default="{ row }">
            <span>{{ row.datasourceName }}</span>
          </template>
        </el-table-column>
        <el-table-column
          show-overflow-tooltip
          prop="datasetGroupName"
          :label="$t('app_template.dataset_group')"
        />
        <el-table-column
          show-overflow-tooltip
          prop="panelName"
          :label="$t('app_template.panel')"
        >
          <template #default="{ row }">
            <span
              v-if="row.panelId && hasDataPermission('use',row.panelPrivileges)"
              class="link-span"
              @click="goPanel(row)"
            >{{ row.panelName }}</span>
            <span v-else>{{ row.panelName }}</span>
          </template>
        </el-table-column>
        <el-table-column
          show-overflow-tooltip
          prop="appName"
          :label="'APPS'"
        />
        <el-table-column
          show-overflow-tooltip
          prop="applyTime"
          sortable="custom"
          :label="$t('app_template.execution_time')"
        >
          <template #default="scope">
            <span>{{ scope.row.applyTime | timestampFormatDate }}</span>
          </template>
        </el-table-column>
        <el-table-column
          v-if="optShow"
          slot="__operation"
          :label="$t('commons.operating')"
          fixed="right"
          :width="operateWidth"
        >
          <template slot-scope="scope">
            <el-button
              v-permission="['appLog:edit']"
              class="de-text-btn mr2"
              type="text"
              @click="editApply(scope.row)"
            >{{ $t('commons.edit') }}
            </el-button>
            <el-button
              v-if="scope.row.id !== 1"
              v-permission="['appLog:del']"
              class="de-text-btn"
              type="text"
              @click="del(scope.row)"
            >{{ $t('commons.delete') }}
            </el-button>
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
    <keep-alive>
      <app-template-apply
        ref="templateEditApply"
        @closeDraw="closeDraw"
      />
    </keep-alive>

    <!--导入templatedialog-->
    <el-dialog
      v-loading="$store.getters.loadingMap[$store.getters.currentPath]"
      :title="$t('app_template.log_delete_tips')"
      :visible.sync="deleteConfirmDialog"
      :show-close="true"
      width="420px"
    >
      <el-row>
        <el-checkbox
          v-model="deleteItemInfo.deleteResource"
          :disabled="!(hasDataPermission('manage',deleteItemInfo.panelPrivileges) &&hasDataPermission('manage',deleteItemInfo.datasetPrivileges) &&hasDataPermission('manage',deleteItemInfo.datasourcePrivileges))"
        />
        {{ $t('app_template.log_resource_delete_tips') }}
      </el-row>
      <span slot="footer">
        <el-button
          size="mini"
          @click="closeDel"
        >{{ $t('commons.cancel') }}</el-button>
        <el-button
          type="danger"
          size="mini"
          @click="confirmDel"
        >{{ $t('commons.confirm') }}</el-button>
      </span>
    </el-dialog>
  </el-row>
</template>

<script>
import GridTable from '@/components/gridTable/index.vue'
import filterUser from './FilterUser'
import _ from 'lodash'
import keyEnter from '@/components/msgCfm/keyEnter.js'
import { addOrder, formatOrders } from '@/utils/index'
import { deleteLogAndResource, logGrid } from '@/api/appTemplateMarket/log'
import { findOneWithParent } from '@/api/panel/panel'
import AppTemplateApply from '@/views/panel/appTemplate/component/AppTemplateApply'
import { hasDataPermission } from '@/utils/permission'

export default {
  name: 'AppTemplateLog',
  components: { AppTemplateApply, GridTable, filterUser },
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
      optShow: false,
      deleteConfirmDialog: false,
      deleteItemInfo: {
        deleteResource: false
      },
      operateWidth: 168,
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
    closeDel() {
      this.deleteItemInfo = {
        deleteResource: false
      }
      this.deleteConfirmDialog = false
    },
    confirmDel() {
      const _this = this
      deleteLogAndResource(_this.deleteItemInfo).then(() => {
        if (_this.deleteItemInfo.deleteResource) {
          _this.clearLocalStorage()
        }
        _this.closeDel()
        _this.search()
      })
    },
    clearLocalStorage() {
      const clearParams = [
        'panel-main-tree',
        'panel-default-tree',
        'chart-tree',
        'dataset-tree'
      ]
      clearParams.forEach(item => {
        localStorage.removeItem(item)
      })
    },
    closeDraw() {
      this.search()
    },
    editApply(item) {
      const param = {
        datasourceFrom: item.datasourceFrom,
        datasourceHistoryId: item.datasourceFrom === 'history' ? item.datasourceId : null,
        datasourceType: item.datasourceType,
        logId: item.id,
        panelId: item.panelId,
        panelGroupPid: item.panelGroupPid,
        datasourceId: item.datasourceId,
        datasetGroupPid: item.datasetGroupPid,
        datasetGroupId: item.datasetGroupId,
        datasetGroupName: item.datasetGroupName,
        panelName: item.panelName,
        datasourcePrivileges: item.datasourcePrivileges,
        panelPrivileges: item.panelPrivileges,
        datasetPrivileges: item.datasetPrivileges,
        appMarketEdit: hasDataPermission('manage', item.datasourcePrivileges)
      }
      this.$refs.templateEditApply.init(param)
    },
    goToDatasource(row) {

    },
    goPanel(row) {
      findOneWithParent(row.panelId).then(rsp => {
        this.$router.push({ name: 'panel', params: rsp.data })
      })
    },
    edit() {

    },
    del(item) {
      this.deleteItemInfo = {
        ...item,
        deleteResource: false
      }
      this.deleteConfirmDialog = true
    },
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
        const _this = this
        _this.optShow = false
        this.$nextTick(() => {
          _this.optShow = true
        })
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

.link-span {
  color: #3370FF;
  cursor: pointer;

  &:hover {
    text-decoration: underline;
  }
}
</style>
