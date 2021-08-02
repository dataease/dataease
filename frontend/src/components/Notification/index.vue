<template>

  <el-popover

    v-model="visible"
    width="350"
    trigger="click"
    placement="top-end"
    style="display: flex;align-items: center;"
    class="international"
  >
    <div style="height: 30px;">
      <div style="float: left;font-size:16px;font-weight:bold;">
        <span>{{ $t('webmsg.web_msg') }}</span>
      </div>
      <div v-if="showSetting" style="float: right;">
        <a href="#" style="text-detext-decoratext-decoration:none;cursor:point;" @click="msgSetting">消息规则</a>
      </div>

    </div>
    <el-divider class="msg-line-class" />
    <el-table
      class="de-msg-data-table"
      :data="data"
      :show-header="false"
      :highlight-current-row="true"
      style="width: 100%"
    >
      <el-table-column prop="content" :label="$t('commons.name')">
        <template slot-scope="scope">
          <div class="start-item">
            <div class="filter-db-row star-item-content" @click="showDetail(scope.row)">
              <!-- <svg-icon icon-class="panel" class="ds-icon-scene" /> -->
              <div class="title-div"><span>【{{ getTypeName(scope.row.typeId) }}】&nbsp;&nbsp;{{ scope.row.content }}</span></div>
              <div class="title-div"><span>{{ scope.row.createTime | timestampFormatDate }}</span></div>
            </div>
            <!-- <div class="star-item-close">
              <i class="el-icon-delete " @click="remove(scope.row)" />
            </div> -->
          </div>
        </template>
      </el-table-column>
    </el-table>
    <div class="msg-foot-class" @click="showMore">
      <el-row style="padding: 5px 0;margin-bottom: -5px;cursor:point;" @click="showMore">
        <span>{{ $t('webmsg.show_more') }}</span>
      </el-row>
    </div>

    <div slot="reference">
      <div>
        <svg-icon
          class-name="notification"
          icon-class="notification"
        />
        <span v-if="paginationConfig.total" class="msg-number">{{ paginationConfig.total }}</span>
      </div>
    </div>
  </el-popover>
</template>

<script>
import { query, updateStatus } from '@/api/system/msg'
import { getTypeName, loadMsgTypes } from '@/utils/webMsg'
import { mapGetters } from 'vuex'
import bus from '@/utils/bus'
import { getToken } from '@/utils/auth'
export default {
  data() {
    return {
      showSetting: false,
      data: [],
      visible: false,
      paginationConfig: {
        currentPage: 1,
        pageSize: 5,
        total: 0
      },
      timer: null
    }
  },
  computed: {
    ...mapGetters([
      'permission_routes'
    ])
  },
  created() {
    // 先加载消息类型
    loadMsgTypes()
    this.search()
    // 每30s定时刷新拉取消息
    this.timer = setInterval(() => {
      this.search()
    }, 30000)
  },
  mounted() {
    bus.$on('refresh-top-notification', () => {
      this.search()
    })
  },
  beforeDestroy() {
    this.timer && clearInterval(this.timer)
  },
  destroyed() {
    this.timer && clearInterval(this.timer)
  },
  methods: {
    // handClick(lang) {
    //   console.log(lang)
    // },
    showDetail(row) {
      const param = { ...{ msgNotification: true, msgType: row.typeId, sourceParam: row.param }}
      this.visible = false
      //   if (this.$route && this.$route.name && this.$route.name.includes('panel') && row.type === 0) {
      //     bus.$emit('to-msg-share', param)
      //   } else if (this.$route && this.$route.name && this.$route.name.includes('dataset') && row.type === 1) {
      //     bus.$emit('to-msg-dataset', param)
      //   } else {
      //     this.$router.push({ name: row.router, params: param })
      //   }
      if (this.$route && this.$route.name && this.$route.name === row.router) {
        // 如果当前路由就是目标路由 那么使用router.push页面不会刷新 这时候要使用事件方式
        row.callback && bus.$emit(row.callback, param)
        row.status || this.setReaded(row.msgId)
      } else {
        if (this.hasPermissionRoute(row.router)) {
          this.$router.push({ name: row.router, params: param })
          row.status || this.setReaded(row.msgId)
          return
        }
        this.$warning(this.$t('commons.no_target_permission'))
      }
    },
    remove(row) {

    },
    msgSetting() {

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
    showMore() {
      const routerName = 'sys-msg-web-all'
      this.visible = false
      this.$router.push({ name: routerName })
      this.openSystem()
    },
    openSystem() {
      const path = '/system'
      let route = this.permission_routes.find(
        item => item.path === '/' + path.split('/')[1]
      )
      // 如果找不到这个路由，说明是首页
      if (!route) {
        route = this.permission_routes.find(item => item.path === '/')
      }
      this.$store.commit('permission/SET_CURRENT_ROUTES', route)
      // this.setSidebarHide(route)
    },
    search() {
      const param = {
        status: false,
        orders: [' create_time desc ']
      }
      const { currentPage, pageSize } = this.paginationConfig
      query(currentPage, pageSize, param).then(response => {
        this.data = response.data.listObject
        this.paginationConfig.total = response.data.itemCount
      }).catch(() => {
        const token = getToken()
        if (!token || token === 'null' || token === 'undefined') {
          this.timer && clearInterval(this.timer)
        }
      })
    },
    getTypeName(value) {
      return this.$t('webmsg.' + getTypeName(value))
    },
    open() {
      this.visible = true
    },
    // 设置已读
    setReaded(msgId) {
      updateStatus(msgId).then(res => {
        this.search()
      })
    }
  }
}
</script>

<style lang="scss" scoped>
.msg-number {
    min-width: 14px;
    text-align: center;
    line-height: 14px;
    display: inline-block;
    position: fixed;
    right: 178px;
    top: 8px;
    background: red;
    color: #fff;
    border-radius: 17px;
    padding: 4px 7px;
    font-size: 16px;
    transform: scale(.7);
    font-family: Tahoma!important;
}
.title-div {

    width: 100%;
    span {
      overflow: hidden;
      text-overflow: ellipsis;
      -o-text-overflow: ellipsis;
      white-space:nowrap;
      width:290px;
      height:24px;
      display:block;
      font-size: 10px !important;
    }
}
.msg-line-class {
    margin: 0 0 !important;
}
.msg-foot-class {
  padding-top: 5px;
  :hover {
    cursor: pointer;
    background-color: #f4f4f5;
  }
}
</style>
