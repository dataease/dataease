<template>
  <div style="height: 100%;">
    <link-error
      v-if="showIndex===0"
      :resource-id="resourceId"
    />
    <link-pwd
      v-if="showIndex===1"
      :resource-id="resourceId"
      :user="userId"
      @fresh-token="refreshToken"
    />
    <link-view
      v-if="showIndex===2"
      :resource-id="resourceId"
      :user="userId"
      :ticket-args="ticketArgs"
    />
    <link-expire
      v-if="showIndex===3"
      :resource-id="resourceId"
      :user="userId"
    />
    <ticket-error
      v-if="showIndex === 4 || showIndex === 5"
      :show-index="showIndex"
    />
  </div>
</template>
<script>
import { getQueryVariable } from '@/utils/index'
import { validate } from '@/api/link'
import LinkView from './view'
import LinkError from './error'
import LinkPwd from './pwd'
import LinkExpire from './overtime'
import TicketError from './ticket'
export default {
  components: {
    LinkError, LinkPwd, LinkView, LinkExpire, TicketError
  },

  data() {
    return {
      resourceId: null,
      userId: null,
      PARAMKEY: 'link',
      link: null,
      user: null,
      showIndex: -1,
      ticket: null,
      ticketArgs: null
    }
  },
  created() {
    this.loadInit()
  },
  methods: {

    loadInit() {
      this.$store.commit('setPublicLinkStatus', true)
      this.link = this.$route.query.link
      this.user = this.$route.query.user
      this.ticket = this.$route.query.ticket
      if (!this.link) {
        this.link = getQueryVariable(this.PARAMKEY)
      }
      if (!this.user) {
        this.user = getQueryVariable('user')
      }
      if (!this.link) {
        this.showError()
        return
      }
      const params = this.user ? { link: encodeURIComponent(this.link), user: encodeURIComponent(this.user) } : { link: encodeURIComponent(this.link) }
      if (this.ticket) {
        params['ticket'] = this.ticket
      }
      this.ticketArgs = null
      validate(params).then(res => {
        const { resourceId, valid, enablePwd, passPwd, expire, userId } = res.data
        const ticketDto = res.data.ticket
        this.resourceId = resourceId
        this.userId = userId
        this.ticketArgs = ticketDto?.args
        // 如果链接无效 直接显示无效页面
        if (!valid || !resourceId) {
          this.showError()
          return
        }

        if (expire) {
          this.showExpire()
          return
        }

        if (enablePwd && !passPwd) {
          this.showPwd()
          return
        }

        if (!ticketDto?.ticketValid) {
          this.showTicketError()
          return
        }
        if (ticketDto?.ticketExp) {
          this.showTicketExpire()
          return
        }

        this.showView()
      }).catch(() => {
        this.showError()
      })
    },
    refreshToken() {
      this.loadInit()
    },

    // 显示无效链接
    showError() {
      this.showIndex = 0
    },
    // 显示密码框
    showPwd() {
      this.showIndex = 1
    },
    // 显示仪表板
    showView() {
      this.showIndex = 2
    },
    showExpire() {
      this.showIndex = 3
    },
    showTicketError() {
      this.showIndex = 4
    },
    showTicketExpire() {
      this.showIndex = 5
    }
  }
}
</script>
