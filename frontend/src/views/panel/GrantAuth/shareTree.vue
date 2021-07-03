<template>
  <div>

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
</template>

<script>
import { loadTree } from '@/api/panel/share'
import { uuid } from 'vue-uuid'
import { get } from '@/api/panel/panel'
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
      expandNodes: []
    }
  },
  created() {
    this.initData().then(res => {
      this.datas = res.data
      if (this.msgPanelIds && this.msgPanelIds.length > 0) {
        this.expandMsgNode(this.msgPanelIds)
      }
    })
  },

  methods: {
    initData() {
      const param = {}
      return loadTree(param)
    },
    handleNodeClick(data) {
      get('panel/group/findOne/' + data.id).then(response => {
        this.$store.commit('setComponentData', this.resetID(JSON.parse(response.data.panelData)))
        this.$store.commit('setCanvasStyle', JSON.parse(response.data.panelStyle))

        this.$store.dispatch('panel/setPanelInfo', data)
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
      console.log(panelIds)
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
.msg-node-class {
  color: red;
  >>> i{
    color: red;
  }
}
</style>
