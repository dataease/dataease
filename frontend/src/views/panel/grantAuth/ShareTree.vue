<template>
  <el-col style="padding: 0 5px 0 5px;">
    <el-row class="de-tree">
      <span class="header-title">{{ $t('panel.share_in') }}</span>
      <div
        class="block"
        style="margin-top:8px;"
      >
        <el-tree
          ref="topTree"
          :data="treeData"
          :props="defaultProps"
          :highlight-current="true"
          node-key="name"
          :default-expanded-keys="expandNodes"
          @node-click="handleNodeClick"
        >
          <span
            slot-scope="{ data }"
            class="custom-tree-node father"
          >
            <span
              style="display: flex; flex: 1 1 0%; width: 0px;"
              :class="!!data.msgNode ? 'msg-node-class': ''"
            >
              <span v-if="!!data.id">
                <svg-icon
                  :icon-class="'panel-'+data.status"
                  class="ds-icon-scene"
                />
              </span>
              <span
                :class="data.status"
                style="margin-left: 6px;white-space:nowrap;overflow:hidden;text-overflow:ellipsis;"
              >{{ data.name }}</span>
            </span>

          </span>
        </el-tree>
      </div>
    </el-row>

    <el-row class="de-tree">
      <span class="header-title">{{ $t('panel.share_out') }}</span>
      <div
        class="block"
        style="margin-top:8px;"
      >
        <el-tree
          ref="botTree"
          :data="outData"
          :props="defaultProps"
          :highlight-current="true"
          node-key="name"
          :default-expand-all="true"
        >
          <span
            slot-scope="{ data }"
            class="custom-tree-node father"
          >
            <span
              style="display: flex; flex: 1 1 0%; width: 0px;"
              @click="viewMyShare(data)"
            >
              <span v-if="!!data.id">
                <svg-icon
                  :icon-class="'panel-'+data.status"
                  class="ds-icon-scene"
                />
              </span>
              <span
                :class="data.status"
                style="margin-left: 6px;white-space:nowrap;overflow:hidden;text-overflow:ellipsis;"
              >{{ data.name }}</span>
            </span>

            <span class="child">
              <span class="el-dropdown-link">
                <el-button
                  icon="el-icon-delete"
                  type="text"
                  size="small"
                  @click="removeCurrent(data)"
                />
              </span>
            </span>
          </span>

        </el-tree>
      </div>
    </el-row>
  </el-col>
</template>

<script>
import { loadShareOutTree, loadTree, removePanelShares } from '@/api/panel/share'
import { uuid } from 'vue-uuid'
import { initPanelData, viewPanelLog } from '@/api/panel/panel'
import { proxyInitPanelData } from '@/api/panel/shareProxy'
import bus from '@/utils/bus'

export default {
  name: 'ShareTree',
  props: {
    msgPanelIds: {
      type: Array,
      default: null
    }
  },
  data() {
    return {
      treeData: [],
      defaultProps: {
        children: 'children',
        label: 'name'
      },
      expandNodes: [],
      outData: []
    }
  },
  computed: {
    panelInfo() {
      return this.$store.state.panel.panelInfo
    }
  },
  created() {
    bus.$on('refresh-my-share-out', this.refreshMyShareOut)
    this.initData().then(res => {
      this.treeData = res.data
      if (this.msgPanelIds && this.msgPanelIds.length > 0) {
        this.expandMsgNode(this.msgPanelIds)
      }
    })
    this.initOutData().then(res => {
      this.outData = res.data
    })
  },
  beforeDestroy() {
    bus.$off('refresh-my-share-out', this.refreshMyShareOut)
  },
  methods: {
    refreshMyShareOut() {
      this.initOutData().then(res => {
        this.outData = res.data
      })
    },
    initData() {
      const param = {}
      return loadTree(param)
    },
    initOutData() {
      return loadShareOutTree()
    },
    handleNodeClick(data) {
      if (!data || !data.userId || !data.id) {
        return
      }
      const param = { userId: data.userId }
      proxyInitPanelData(data.id, param, function() {
        viewPanelLog({ panelId: data.id }).then(res => {
          bus.$emit('set-panel-show-type', 1)
          bus.$emit('set-panel-share-user', data.userId)
        })
      })
      this.$refs['botTree'].setCurrentKey(null)
    },
    viewMyShare(data) {
      initPanelData(data.id, false, function() {
        viewPanelLog({ panelId: data.id }).then(res => {
          bus.$emit('set-panel-show-type', 2)
        })
      })
      this.$refs['topTree'].setCurrentKey(null)
    },
    resetID(data) {
      if (data) {
        data.forEach(item => {
          item.type !== 'custom' && (item.id = uuid.v1())
        })
      }

      return data
    },
    expandMsgNode(panelIds) {
      this.$nextTick(() => {
        this.getMsgNodes(panelIds)
      })
    },
    getMsgNodes(panelIds) {
      this.treeData.forEach(item => {
        if (item.children && item.children.length > 0) {
          item.children.forEach(node => {
            if (panelIds.includes(node.id)) {
              node.msgNode = true
              this.expandNodes.push(item.name)
            }
          })
        }
      })
    },
    removeCurrent(node) {
      this.$confirm(this.$t('panel.remove_share_confirm'), '', {
        confirmButtonText: this.$t('commons.confirm'),
        cancelButtonText: this.$t('commons.cancel'),
        type: 'warning'
      }).then(() => {
        removePanelShares(node.id).then(res => {
          this.panelInfo && this.panelInfo.id && node.id === this.panelInfo.id && this.setMainNull()
          this.initOutData().then(res => {
            this.outData = res.data
          })
          this.$success(this.$t('commons.delete_success'))
        })
      }).catch(() => {
        this.$message({
          type: 'info',
          message: this.$t('commons.delete_cancelled')
        })
      })
    },
    setMainNull() {
      this.$store.dispatch('panel/setPanelInfo', { id: null, name: '', preStyle: null })
    }

  }
}
</script>

<style lang="scss" scoped>
.header-title {
  font-size: 14px;
  flex: 1;
  color: var(--TextPrimary, #606266);
  font-weight: bold;
  display: block;
  height: 100%;
  /*line-height: 36px;*/
}

.msg-node-class {
  color: red;

  ::v-deep i {
    color: red;
  }
}

.custom-tree-node {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-size: 14px;
  padding-right: 8px;
}

.custom-tree-node-list {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-size: 14px;
  padding: 0 8px;
}

.father .child {
  /*display: none;*/
  visibility: hidden;
}

.father:hover .child {
  /*display: inline;*/
  visibility: visible;
}

.unpublished {
  color: #b2b2b2
}

.publish {
}

</style>
