<template>
  <de-container
    v-loading="$store.getters.loadingMap[$store.getters.currentPath]"
  >
    <de-aside-container type="dataset">
      <group
        :current-node-id="currentNodeId"
        :save-status="saveStatus"
        @switchComponent="switchComponent"
      />
    </de-aside-container>
    <de-main-container>
      <component
        :is="component"
        ref="dynamic_component"
        :param="param"
        @switchComponent="switchComponent"
        @saveSuccess="saveSuccess"
      />
    </de-main-container>
  </de-container>
</template>

<script>
import DeMainContainer from '@/components/dataease/DeMainContainer'
import DeContainer from '@/components/dataease/DeContainer'
import DeAsideContainer from '@/components/dataease/DeAsideContainer'
import Group from './group/Group'

import noSelect from './data/NoSelect'
import ViewTable from './data/ViewTable'
import FieldEdit from './data/FieldEdit'
import { removeClass } from '@/utils'
import { checkCustomDs } from '@/api/dataset/dataset'
export default {
  name: 'DataSet',
  components: {
    DeMainContainer,
    DeContainer,
    DeAsideContainer,
    Group,
    noSelect,
    ViewTable
  },
  data() {
    return {
      component: noSelect,
      param: {},
      saveStatus: null
    }
  },
  computed: {
    currentNodeId() {
      return this.param?.id
    }
  },
  mounted() {
    removeClass(document.body, 'showRightPanel')
  },
  created() {
    this.initDs()
    this.$store.dispatch('app/toggleSideBarHide', true)
    const routerParam = this.$router.currentRoute.params
    this.toMsgShare(routerParam)
  },
  methods: {
    initDs() {
      localStorage.setItem('reloadDsData', 'false')
      checkCustomDs().then((res) => {
        this.$store.dispatch('dataset/setHideCustomDs', res.data)
      })
    },
    switchComponent(c) {
      this.param = c.param
      switch (c.name) {
        case 'ViewTable':
          this.component = ViewTable
          break
        case 'FieldEdit':
          this.component = FieldEdit
          break
        default:
          this.component = noSelect
          break
      }
    },

    saveSuccess(val) {
      this.saveStatus = val
    },

    toMsgShare(routerParam) {
      if (routerParam !== null && routerParam.msgNotification) {
        const panelShareTypeIds = [4, 5, 6]
        // 说明是从消息通知跳转过来的
        if (panelShareTypeIds.includes(routerParam.msgType)) {
          // 是数据集同步
          if (routerParam.sourceParam) {
            try {
              const msgParam = JSON.parse(routerParam.sourceParam)
              this.param = msgParam.tableId
              this.component = ViewTable
              this.$nextTick(() => {
                this.$refs.dynamic_component &&
                  this.$refs.dynamic_component.msg2Current &&
                  this.$refs.dynamic_component.msg2Current(
                    routerParam.sourceParam
                  )
              })
            } catch (error) {
              console.error(error)
            }
          }
        }
      }
    }
  }
}
</script>

<style scoped>
.ms-aside-container {
  height: calc(100vh - 56px);
  padding: 0 0;
  min-width: 260px;
  max-width: 460px;
}

.ms-main-container {
  height: calc(100vh - 56px);
  padding: 10px 15px 0 15px;
}
</style>
