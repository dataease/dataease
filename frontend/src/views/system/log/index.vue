<template>
  <layout-content :header="$t('log.title')">
    <complex-table
      v-loading="$store.getters.loadingMap[$store.getters.currentPath]"
      :data="data"
      :columns="columns"
      :search-config="searchConfig"
      :pagination-config="paginationConfig"
      @select="select"
      @search="search"
      @sort-change="sortChange"
    >
      <template #toolbar>
        <el-button v-permission="['log:export']" icon="el-icon-download" size="mini" @click="exportConfirm">{{ $t('log.export') }}</el-button>
      </template>

      <el-table-column :show-overflow-tooltip="true" prop="opType" :label="$t('log.optype')" width="140">
        <template v-slot:default="{row}">
          <span>{{ row.opType + row.sourceType }}</span>
        </template>
      </el-table-column>
      <el-table-column :show-overflow-tooltip="true" prop="detail" :label="$t('log.detail')" />
      <el-table-column :show-overflow-tooltip="true" prop="user" :label="$t('log.user')" width="100" />
      <el-table-column :show-overflow-tooltip="true" prop="time" sortable="custom" :label="$t('log.time')" width="180">
        <template v-slot:default="scope">
          <span>{{ scope.row.time | timestampFormatDate }}</span>
        </template>
      </el-table-column>
    </complex-table>
  </layout-content>
</template>

<script>
import LayoutContent from '@/components/business/LayoutContent'
import ComplexTable from '@/components/business/complex-table'
import { formatCondition, formatQuickCondition, addOrder, formatOrders } from '@/utils/index'
import { logGrid, opTypes, exportExcel } from '@/api/system/log'
export default {

  components: { ComplexTable, LayoutContent },
  data() {
    return {
      columns: [],

      searchConfig: {
        useQuickSearch: false,
        useComplexSearch: true,
        quickPlaceholder: this.$t('log.search_by_key'),
        components: [
          {
            field: 'optype',
            label: this.$t('log.optype'),
            component: 'FuComplexMixSelect',
            options: [],
            multiple: true,
            class: 'de-log-filter',
            defaultOperator: 'in',
            filterable: true,
            'reserve-keyword': true
          },
          { field: 'nick_name', label: this.$t('log.user'), component: 'DeComplexInput', class: 'de-log-filter' },

          { field: 'time', label: this.$t('log.time'), component: 'FuComplexDateTime', defaultOperator: 'between', class: 'de-log-filter' }

        ]
      },

      paginationConfig: {
        currentPage: 1,
        pageSize: 10,
        total: 0
      },
      data: [],
      types: [],

      orderConditions: [],
      last_condition: null

    }
  },

  created() {
    this.types = []
    opTypes().then(res => {
      const datas = res.data
      datas.forEach(item => {
        this.types.push({ 'label': item.name, 'value': item.id })
      })
      this.searchConfig.components[0].options = this.types
    })
  },
  mounted() {
    this.search()
  },

  methods: {

    exportConfirm() {
      this.$confirm(this.$t('log.confirm'), '', {
        confirmButtonText: this.$t('commons.confirm'),
        cancelButtonText: this.$t('commons.cancel'),
        type: 'warning'
      }).then(() => {
        this.exportData()
      }).catch(() => {
        // this.$info(this.$t('commons.delete_cancel'))
      })
    },
    exportData() {
      let condition = this.last_condition
      condition = formatQuickCondition(condition, 'key')
      const temp = formatCondition(condition)
      const param = temp || {}
      param['orders'] = formatOrders(this.orderConditions)

      exportExcel(param).then(res => {
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
        this.search(this.last_condition)
        return
      }

      this.orderConditions = []
      addOrder({ field: prop, value: order }, this.orderConditions)
      this.search(this.last_condition)
    },
    select(selection) {

    },

    search(condition) {
      this.last_condition = condition
      condition = formatQuickCondition(condition, 'key')
      const temp = formatCondition(condition)
      const param = temp || {}
      param['orders'] = formatOrders(this.orderConditions)
      const { currentPage, pageSize } = this.paginationConfig
      logGrid(currentPage, pageSize, param).then(response => {
        this.data = response.data.listObject
        this.paginationConfig.total = response.data.itemCount
      })
    }

  }
}
</script>

<style scoped>

</style>
