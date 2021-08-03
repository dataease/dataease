<template>
  <layout-content v-loading="$store.getters.loadingMap[$store.getters.currentPath]">

    <el-radio-group v-model="selectType" style="margin-bottom: 15px;" @change="typeChange">
      <el-radio-button v-for="(item,index) in $store.getters.msgTypes.filter(type => type.pid <= 0)" :key="index" class="de-msg-radio-class" :label="item.msgTypeId">{{ $t('webmsg.' + item.typeName) }}</el-radio-button>

    </el-radio-group>
    <complex-table
      :data="data"
      :columns="columns"
      :hide-columns="true"
      :pagination-config="paginationConfig"
      :search-config="searchConfig"
      @select="select"
      @search="search"
      @selection-change="handleSelectionChange"
      @sort-change="sortChange"
    >
      <template #toolbar>
        <el-button :disabled="multipleSelection.length === 0" @click="deleteBatch">{{ $t('commons.delete') }}</el-button>
      </template>
      <el-table-column
        type="selection"
        width="55"
      />
      <el-table-column prop="content" :label="$t('webmsg.content')">
        <template slot-scope="scope">

          <span style="display: flex;flex: 1;">
            <span>
              <svg-icon v-if="!scope.row.status" icon-class="unread-msg" style="color: red;" />
              <svg-icon v-else icon-class="readed-msg" />
            </span>
            <span style="margin-left: 6px;" class="de-msg-a" @click="toDetail(scope.row)">
              {{ scope.row.content }}
            </span>
          </span>

        </template>
      </el-table-column>

      <el-table-column prop="createTime" sortable="custom" :label="$t('webmsg.sned_time')" width="180">
        <template slot-scope="scope">
          <span>{{ scope.row.createTime | timestampFormatDate }}</span>
        </template>
      </el-table-column>

      <el-table-column prop="readTime" sortable="custom" :label="$t('webmsg.read_time')" width="180">
        <template slot-scope="scope">
          <span>{{ scope.row.readTime | timestampFormatDate }}</span>
        </template>
      </el-table-column>

      <el-table-column prop="typeId" sortable="custom" :label="$t('webmsg.type')" width="140">
        <template slot-scope="scope">
          <span>{{ getTypeName(scope.row.typeId) }}</span>
        </template>
      </el-table-column>

    </complex-table>

  </layout-content>
</template>

<script>

import LayoutContent from '@/components/business/LayoutContent'
import ComplexTable from '@/components/business/complex-table'
import { query, batchDelete } from '@/api/system/msg'
import { msgTypes, getTypeName, loadMsgTypes } from '@/utils/webMsg'
import { addOrder, formatOrders } from '@/utils/index'
import { mapGetters } from 'vuex'
export default {
  components: {
    LayoutContent,
    ComplexTable
  },
  data() {
    return {
      selectType: -1,
      //   msgTypes: [
      //     { value: -1, label: '全部类型' },
      //     { value: 0, label: '仪表板分享' },
      //     { value: 1, label: '数据集同步' }
      //   ],
      msgTypes: msgTypes,
      data: [],
      allTypes: [{ name: 'mysql', type: 'jdbc' }, { name: 'sqlServer', type: 'jdbc' }],

      columns: [],
      orderConditions: [],

      paginationConfig: {
        currentPage: 1,
        pageSize: 10,
        total: 0
      },
      multipleSelection: [],
      searchConfig: {
        useQuickSearch: false,
        useComplexSearch: false
      }
    }
  },
  computed: {
    ...mapGetters([
      'permission_routes'
    ])
  },
  mounted() {
    this.search()
  },
  created() {
    // 先加载消息类型
    loadMsgTypes()
  },
  methods: {
    select(selection) {
    },

    search() {
      const param = {}
      param.status = true
      if (this.selectType >= 0) {
        param.type = this.selectType
      }

      if (this.orderConditions.length === 0) {
        param.orders = [' create_time desc ']
      } else {
        param.orders = formatOrders(this.orderConditions)
      }

      const { currentPage, pageSize } = this.paginationConfig
      query(currentPage, pageSize, param).then(response => {
        this.data = response.data.listObject
        this.paginationConfig.total = response.data.itemCount
      })
    },
    getTypeName(value) {
      return this.$t('webmsg.' + getTypeName(value))
    },
    typeChange(value) {
      this.search()
    },
    toDetail(row) {
      const param = { ...{ msgNotification: true, msgType: row.typeId, sourceParam: row.param }}
      // this.$router.push({ name: row.router, params: param })
      if (this.hasPermissionRoute(row.router)) {
        this.$router.push({ name: row.router, params: param })
        return
      }
      this.$warning(this.$t('commons.no_target_permission'))
    },
    hasPermissionRoute(name, permission_routes) {
      permission_routes = permission_routes || this.permission_routes
      for (let index = 0; index < permission_routes.length; index++) {
        const route = permission_routes[index]
        if (route.name && route.name === name) return true
        if (route.children && this.hasPermissionRoute(name, route.children)) return true
      }
      return false
    },
    sortChange({ column, prop, order }) {
      this.orderConditions = []
      if (!order) {
        this.search()
        return
      }
      if (prop === 'createTime') {
        prop = 'create_time'
      }
      if (prop === 'readTime') {
        prop = 'read_time'
      }
      if (prop === 'typeId') {
        prop = 'type_id'
      }
      addOrder({ field: prop, value: order }, this.orderConditions)
      this.search()
    },
    deleteBatch() {
      if (this.multipleSelection.length === 0) {
        this.$warning(this.$t('webmsg.please_select'))
        return
      }
      const param = this.multipleSelection.map(item => item.msgId)
      batchDelete(param).then(res => {
        this.$success(this.$t('commons.delete_success'))
        this.search()
      })
    },
    handleSelectionChange(val) {
      this.multipleSelection = val
    }

  }

}
</script>

<style lang="scss" scoped>
.de-msg-radio-class {
  padding: 0 5px;
  >>>.el-radio-button__inner {
    border-radius: 4px 4px 4px 4px !important;
    border-left: 1px solid #dcdfe6 !important;
    padding: 10px 10px;
  }

  >>>.el-radio-button__orig-radio:checked+.el-radio-button__inner {
    color: #fff;
    background-color: #0a7be0;
    border-color: #0a7be0;
    -webkit-box-shadow: 0px 0 0 0 #0a7be0;
    box-shadow: 0px 0 0 0 #0a7be0;
  }
}
.de-msg-a:hover {
    text-decoration: underline !important;
    color: #0a7be0 !important;
    cursor: pointer !important;

}

</style>
