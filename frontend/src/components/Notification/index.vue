<template>

  <el-popover
    v-model="visible"
    width="350"
    trigger="click"
    placement="top-end"
    style="display: flex;align-items: center;"
    class="international"
  >
    <div v-loading="loading">
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
    </div>
    <div slot="reference">
      <el-badge :value="visible && !loading ? paginationConfig.total : count" :hidden="!count && !paginationConfig.total" :max="99" class="item">
        <svg-icon class-name="notification" icon-class="notification" />
      </el-badge>

    </div></el-popover>
</template>

<script>
import { query, updateStatus, unReadCount } from '@/api/system/msg'
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
      timer: null,
      count: 0,
      loading: false
    }
  },
  computed: {
    ...mapGetters([
      'permission_routes',
      'user'
    ])
  },
  watch: {
    'visible': function(newV, oldV) {
      if (newV && !oldV) {
        this.search()
      }
    }
  },
  created() {
    // 先加载消息类型
    loadMsgTypes()
    this.queryCount()
  },
  mounted() {
    this.initEvents()
  },
  beforeDestroy() {
    this.timer && clearInterval(this.timer)
    bus.$off('refresh-top-notification', this.refreshTopNotification)
    bus.$off('web-msg-topic-call', this.webMsgTopicCall)
  },
  destroyed() {
    this.timer && clearInterval(this.timer)
  },
  methods: {
    refreshTopNotification() {
      if (this.visible) this.search()
      else this.queryCount()
    },
    webMsgTopicCall() {
      this.count = (this.count || this.paginationConfig.total) + 1
    },
    initEvents() {
      bus.$on('refresh-top-notification', this.refreshTopNotification)
      bus.$on('web-msg-topic-call', this.webMsgTopicCall)
    },
    showDetail(row) {
      const param = { ...{ msgNotification: true, msgType: row.typeId, sourceParam: row.param }}
      this.visible = false

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
    queryCount() {
      const token = getToken()

      if (!token || token === 'null' || token === 'undefined' || !this.user || !this.user.userId) {
        this.timer && clearInterval(this.timer)
        const message = this.$t('login.tokenError')
        this.$alert(message, {
          confirmButtonText: this.$t('login.re_login'),
          showClose: false,
          callback: function(action, instance) {
            if (action === 'confirm') {
              this.$store.dispatch('user/logout').then(() => {
                location.reload()
              })
            }
          }.bind(this)
        })
      }
      /* const param = {
        userId: this.user.userId
      } */
      unReadCount().then(res => {
        this.count = res.data
      })
    },
    search() {
      this.loading = true
      const param = {
        status: false,
        orders: [' create_time desc ']
      }
      const { currentPage, pageSize } = this.paginationConfig
      query(currentPage, pageSize, param).then(response => {
        this.data = response.data.listObject
        this.paginationConfig.total = response.data.itemCount
        this.count = this.paginationConfig.total
        this.loading = false
      }).catch(() => {
        this.loading = false
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
    color: var(--TextActive, #fff);
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

.blackTheme .msg-foot-class {
  padding-top: 5px;
  :hover {
    cursor: pointer;

    background-color: #171422;
  }
}

.item {
  margin-top: 0px;
  margin-right: 5px;
}
</style>
