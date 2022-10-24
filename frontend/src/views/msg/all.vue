<template>
  <de-layout-content
    v-loading="$store.getters.loadingMap[$store.getters.currentPath]"
    :header="$t('components.message_list')"
  >
    <div class="organization">
      <el-tabs
        v-model="tabActive"
        class="de-tabs"
        @tab-click="changeTab"
      >
        <el-tab-pane
          :label="$t('components.unread_message')"
          name="unread"
        />
        <el-tab-pane
          :label="$t('components.read_message')"
          name="read"
        />
        <el-tab-pane
          :label="$t('components.all_messages')"
          name="allMsg"
        />
      </el-tabs>
      <div class="tabs-container">
        <div class="msg-cont de-search-table">
          <el-row class="top-operate">
            <el-col :span="12">
              <template v-if="tabActive === 'unread'">
                <deBtn
                  secondary
                  @click="allMarkRead"
                >{{
                  $t("webmsg.all_mark_read")
                }}</deBtn>
                <deBtn
                  key="mark_read"
                  secondary
                  :disabled="multipleSelection.length === 0"
                  @click="markRead"
                >{{ $t("webmsg.mark_read") }}</deBtn>
              </template>

              <deBtn
                v-if="tabActive === 'read'"
                key="delete"
                secondary
                :disabled="multipleSelection.length === 0"
                @click="deleteBatch"
              >{{ $t("commons.delete") }}</deBtn>
              &nbsp;
            </el-col>
            <el-col
              class="right-user"
              :span="12"
            >
              <el-select
                v-model="selectType"
                class="name-email-search"
                size="small"
                @change="typeChange"
              >
                <el-option
                  v-for="(item, index) in $store.getters.msgTypes.filter(
                    (type) => type.pid <= 0
                  )"
                  :key="index"
                  :label="$t('webmsg.' + item.typeName)"
                  :value="item.msgTypeId"
                />
              </el-select>
            </el-col>
          </el-row>
          <div
            :key="tabActive"
            class="table-container"
          >
            <grid-table
              :key="tabActive"
              :table-data="data"
              :multiple-selection="multipleSelection"
              :columns="[]"
              :pagination="paginationConfig"
              @selection-change="handleSelectionChange"
              @sort-change="sortChange"
              @size-change="handleSizeChange"
              @current-change="handleCurrentChange"
            >
              <el-table-column
                type="selection"
                width="55"
              />

              <el-table-column
                prop="content"
                :label="$t('webmsg.content')"
              >
                <template slot-scope="scope">
                  <span style="display: flex; flex: 1">
                    <span>
                      <svg-icon
                        v-if="!scope.row.status"
                        icon-class="unread-msg"
                        style="color: red"
                      />
                      <svg-icon
                        v-else
                        icon-class="read-msg"
                      />
                    </span>
                    <span
                      style="margin-left: 6px"
                      class="de-msg-a"
                      @click="toDetail(scope.row)"
                    >
                      {{ scope.row.content }}
                    </span>
                  </span>
                </template>
              </el-table-column>

              <el-table-column
                prop="createTime"
                sortable="custom"
                :label="$t('webmsg.sned_time')"
                width="180"
              >
                <template slot-scope="scope">
                  <span>{{ scope.row.createTime | timestampFormatDate }}</span>
                </template>
              </el-table-column>

              <el-table-column
                prop="typeId"
                sortable="custom"
                :label="$t('webmsg.type')"
                width="140"
              >
                <template slot-scope="scope">
                  <span>{{ getTypeName(scope.row.typeId) }}</span>
                </template>
              </el-table-column>
            </grid-table>
          </div>
        </div>
      </div>
    </div>
  </de-layout-content>
</template>

<script>
import DeLayoutContent from '@/components/business/DeLayoutContent'
import GridTable from '@/components/gridTable/index.vue'
import { query, updateStatus, batchRead, allRead, batchDelete } from '@/api/system/msg'
import { msgTypes, getTypeName, loadMsgTypes } from '@/utils/webMsg'
import bus from '@/utils/bus'
import { addOrder, formatOrders } from '@/utils/index'
import msgCfm from '@/components/msgCfm/index'
import { mapGetters } from 'vuex'
export default {
  components: {
    DeLayoutContent,
    GridTable
  },
  mixins: [msgCfm],
  data() {
    return {
      multipleSelection: [],
      tabActive: 'unread',
      selectType: -1,
      msgTypes: msgTypes,
      data: [],
      allTypes: [
        { name: 'mysql', type: 'jdbc' },
        { name: 'sqlServer', type: 'jdbc' }
      ],

      columns: [],

      paginationConfig: {
        currentPage: 1,
        pageSize: 10,
        total: 0
      },
      orderConditions: []
    }
  },
  computed: {
    ...mapGetters(['permission_routes'])
  },
  mounted() {
    this.search()
  },
  created() {
    // 先加载消息类型
    loadMsgTypes()
  },
  methods: {
    changeTab(val) {
      this.initSearch()
    },
    handleSelectionChange(val) {
      this.multipleSelection = val
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
    allMarkRead() {
      allRead().then(res => {
        this.openMessageSuccess('components.all_read_successfully')
        bus.$emit('refresh-top-notification')
        this.initSearch()
      })
    },
    markRead() {
      const param = this.multipleSelection.map(item => item.msgId)
      batchRead(param).then(res => {
        this.openMessageSuccess('webmsg.mark_success')
        bus.$emit('refresh-top-notification')
        this.initSearch()
      })
    },
    deleteBatch() {
      const param = this.multipleSelection.map(item => item.msgId)
      batchDelete(param).then(res => {
        this.openMessageSuccess('commons.delete_success')
        this.initSearch()
      })
    },
    search() {
      const param = {}

      if (this.selectType >= 0) {
        param.type = this.selectType
      }

      if (this.orderConditions.length === 0) {
        param.orders = ['create_time desc ']
      } else {
        param.orders = formatOrders(this.orderConditions)
      }

      if (this.tabActive !== 'allMsg') {
        param.status = this.tabActive === 'read'
      }

      const { currentPage, pageSize } = this.paginationConfig
      query(currentPage, pageSize, param).then((response) => {
        this.data = response.data.listObject
        this.paginationConfig.total = response.data.itemCount
      })
    },
    getTypeName(value) {
      return this.$t('webmsg.' + getTypeName(value))
    },
    typeChange() {
      this.initSearch()
    },
    toDetail(row) {
      const param = {
        ...{
          msgNotification: true,
          msgType: row.typeId,
          sourceParam: row.param
        }
      }
      if (this.hasPermissionRoute(row.router)) {
        this.$router.push({ name: row.router, params: param })
        row.status || this.setRead(row)
        return
      }
      this.$warning(this.$t('commons.no_target_permission'))
    },
    hasPermissionRoute(name, permission_routes) {
      permission_routes = permission_routes || this.permission_routes
      for (let index = 0; index < permission_routes.length; index++) {
        const route = permission_routes[index]
        if (route.name && route.name === name) return true
        if (route.children && this.hasPermissionRoute(name, route.children)) { return true }
      }
      return false
    },
    // 设置已读
    setRead(row) {
      updateStatus(row.msgId).then((res) => {
        bus.$emit('refresh-top-notification')
        this.search()
      })
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
      if (prop === 'typeId') {
        prop = 'type_id'
      }
      addOrder({ field: prop, value: order }, this.orderConditions)
      this.search()
    }
  }
}
</script>

<style lang="scss" scoped>
.de-msg-a:hover {
  text-decoration: underline !important;
  color: #0a7be0 !important;
  cursor: pointer !important;
}

.table-container {
  height: calc(100% - 50px);
}
</style>
<style scoped lang="scss">
.organization {
  height: 100%;
  background-color: var(--MainBG, #f5f6f7);

  .tabs-container {
    height: calc(100% - 48px);
    background: var(--ContentBG, #ffffff);
    overflow-x: auto;

    .msg-cont {
      padding: 24px;
      height: 100%;
      box-sizing: border-box;
    }
  }
}
</style>
