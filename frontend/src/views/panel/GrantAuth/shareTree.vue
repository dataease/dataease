<template>
  <el-col style="padding: 0 5px 0 5px;">
    <el-row>
      <span class="header-title">分享给我</span>
      <div class="block" style="margin-top:8px;">
        <el-tree :data="datas" :props="defaultProps" node-key="name" :default-expanded-keys="expandNodes" @node-click="handleNodeClick">
          <span slot-scope="{ data }" class="custom-tree-node">
            <span :class="!!data.msgNode ? 'msg-node-class': ''">
              <span v-if="!!data.id">
                <el-button
                  icon="el-icon-picture-outline"
                  type="text"
                />
              </span>
              <span style="margin-left: 6px">{{ data.name }}</span>
            </span>
          </span>
        </el-tree>
      </div>
    </el-row>

    <el-row>
      <span class="header-title">我分享的</span>
      <div class="block" style="margin-top:8px;">
        <el-tree :data="outDatas" :props="defaultProps" node-key="name" :default-expand-all="true" @node-click="handleNodeClick">
          <span slot-scope="{ data }" class="custom-tree-node">
            <span>
              <span v-if="!!data.id">
                <el-button
                  icon="el-icon-picture-outline"
                  type="text"
                />
              </span>
              <span style="margin-left: 6px">{{ data.name }}</span>
            </span>
          </span>
        </el-tree>
      </div>
    </el-row>
  </el-col>
</template>

<script>
import { loadTree, loadShareOutTree } from '@/api/panel/share'
import { uuid } from 'vue-uuid'
import { get } from '@/api/panel/panel'
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
      datas: [],
      defaultProps: {
        children: 'children',
        label: 'name'
      },
      expandNodes: [],
      outDatas: []
    }
  },
  created() {
    this.initData().then(res => {
      this.datas = res.data
      if (this.msgPanelIds && this.msgPanelIds.length > 0) {
        this.expandMsgNode(this.msgPanelIds)
      }
    })
    this.initOutData().then(res => {
      this.outDatas = res.data
    })
  },

  methods: {
    initData() {
      const param = {}
      return loadTree(param)
    },
    initOutData() {
      return loadShareOutTree()
    },
    handleNodeClick(data) {
      get('panel/group/findOne/' + data.id).then(response => {
        this.$store.commit('setComponentData', this.resetID(JSON.parse(response.data.panelData)))
        this.$store.commit('setCanvasStyle', JSON.parse(response.data.panelStyle))

        this.$store.dispatch('panel/setPanelInfo', data)
        bus.$emit('set-panel-is-share')
      })
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
      // console.log(panelIds)
      this.$nextTick(() => {
        this.getMsgNodes(panelIds)
      })
    },
    getMsgNodes(panelIds) {
      this.datas.forEach(item => {
        if (item.children && item.children.length > 0) {
          item.children.forEach(node => {
            if (panelIds.includes(node.id)) {
              node.msgNode = true
              this.expandNodes.push(item.name)
            }
          })
        }
      })
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
  >>> i{
    color: red;
  }
}
</style>
