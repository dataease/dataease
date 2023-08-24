<template xmlns:el-col="http://www.w3.org/1999/html">
  <de-layout-content :header="$t('消息接收管理')">
    <el-col>
      <el-row class="tree-head">
        <span style="float: left;">{{
          $t("webmsg.type")
        }}</span>
        <span
          v-for="channel in msg_channels"
          :key="channel.msgChannelId"
          class="auth-span"
        >
          {{ $t(channel.channelName) }}
        </span>
      </el-row>
      <el-row
        class="msg-setting"
        style="margin-top: 5px"
      >
        <el-tree
          :props="defaultProps"
          :data="treeData"
          default-expand-all
          :node-key="defaultProps.id"
          :highlight-current="highlightCurrent"
          @node-click="nodeClick"
        >
          <span
            slot-scope="{ node, data }"
            class="custom-tree-node"
          >
            <span>
              <span style="margin-left: 6px">{{
                $t("webmsg." + data.name)
              }}</span>
            </span>
            <span @click.stop>
              <div>
                <span
                  v-for="channel in msg_channels"
                  :key="channel.msgChannelId"
                  class="auth-span-check"
                >
                  <el-checkbox
                    v-if="data.children && data.children.length > 0"
                    v-model="data.check_all_map[channel.msgChannelId]"
                    :indeterminate="
                      data.indeterminate_map[channel.msgChannelId]
                    "
                    @change="parentBoxChange(node, channel)"
                  />
                  <el-checkbox
                    v-else
                    v-model="data.check_map[channel.msgChannelId]"
                    @change="childBoxChange(node, channel)"
                  />
                </span></div></span>
          </span>
        </el-tree>
      </el-row>
    </el-col>
  </de-layout-content>
</template>

<script>
import DeLayoutContent from '@/components/business/DeLayoutContent'
import {
  treeList,
  channelList,
  settingList,
  updateSetting,
  batchUpdate
} from '@/api/system/msg'
export default {
  name: 'LazyTree',
  components: { DeLayoutContent },
  data() {
    return {
      treeData: [],
      defaultProps: {
        children: 'children',
        label: 'name',
        id: 'id'
      },
      highlightCurrent: true,

      msg_channels: [],
      setting_data: {}
    }
  },
  computed: {},
  mounted() {},
  beforeCreate() {
    channelList().then((res) => {
      this.msg_channels = res.data
    })
  },
  created() {
    this.loadSettingData(this.loadTreeData)
  },

  methods: {
    // 加载树节点数据
    loadTreeData() {
      treeList().then((res) => {
        const data = res.data
        data.forEach((data) => this.formatTreeNode(data))
        this.treeData = data
      })
    },
    formatTreeNode(node) {
      if (node.children && node.children.length > 0) {
        node.check_all_map = {}
        node.indeterminate_map = {}
        node.indeterminate_number_map = {}
        const kidSize = node.children.length
        node.children.forEach((kid) => {
          this.formatTreeNode(kid)
          const isLeaf = !kid.children || kid.children.length === 0
          const tempMap = isLeaf ? kid.check_map : kid.indeterminate_map
          for (const key in tempMap) {
            if (Object.hasOwnProperty.call(tempMap, key)) {
              const element = tempMap[key]
              node.indeterminate_number_map[key] =
                node.indeterminate_number_map[key] || 0
              if (element) {
                node.indeterminate_number_map[key]++
              }

              if (
                node.indeterminate_number_map[key] === kidSize &&
                (isLeaf || kid.check_all_map[key])
              ) {
                node.check_all_map[key] = true
                node.indeterminate_map[key] = false
              } else if (node.indeterminate_number_map[key] > 0) {
                node.check_all_map[key] = false
                node.indeterminate_map[key] = true
              }
            }
          }
        })
      } else {
        node.check_map = {}
        this.msg_channels.forEach((channel) => {
          node.check_map[channel.msgChannelId] = this.checkBoxStatus(
            node,
            channel
          )
        })
        // this.checkBoxStatus(node, )
      }
    },
    // 加载用户设置信息
    loadSettingData(callBack) {
      // this.setting_data = {}
      const temp_setting_data = {}
      settingList().then((res) => {
        const lists = res.data
        lists.forEach((item) => {
          const key = item.typeId + ''
          if (!Object.keys(temp_setting_data).includes(key)) {
            temp_setting_data[key] = []
          }
          temp_setting_data[key].push(item)
        })
        this.setting_data = temp_setting_data
        callBack && callBack()
      })
    },
    checkBoxStatus(node, channel) {
      // const nodeId = node.data.id
      const nodeId = node.id
      return (
        this.setting_data[nodeId] &&
        this.setting_data[nodeId].some(
          (item) => item.channelId === channel.msgChannelId && item.enable
        )
      )
    },

    nodeClick(data, node) {},
    getAllKidId(node, ids) {
      if (node.children && node.children.length > 0) {
        node.children.forEach((item) => this.getAllKidId(item, ids))
      } else {
        ids.push(node.id)
      }
    },
    parentBoxChange(node, channel) {
      const typeIds = []
      this.getAllKidId(node.data, typeIds)
      const channelId = channel.msgChannelId

      const data = node.data
      const enable = data.check_all_map && data.check_all_map[channelId]
      node.data.check_all_map[channelId] = enable
      node.data.indeterminate_map[channelId] = false
      node.data.children.forEach((item) => {
        item.check_map = item.check_map || {}
        item.check_map[channelId] = enable
      })

      const param = {
        typeIds: typeIds,
        channelId: channelId,
        enable
      }
      batchUpdate(param).then((res) => {
        this.loadSettingData(this.loadTreeData)
      })
    },
    childBoxChange(node, channel) {
      const channelId = channel.msgChannelId
      const parent = node.parent
      if (parent) {
        const data = parent.data
        const kids = data.children
        const kidSize = kids.length
        let index = 0
        kids.forEach((kid) => {
          if (kid.check_map[channelId]) {
            index++
          }
        })
        if (index === kidSize) {
          node.parent.data.check_all_map[channelId] = true
          node.parent.data.indeterminate_map[channelId] = false
        } else if (index > 0) {
          node.parent.data.check_all_map[channelId] = false
          node.parent.data.indeterminate_map[channelId] = true
        } else {
          node.parent.data.check_all_map[channelId] = false
          node.parent.data.indeterminate_map[channelId] = false
        }
        // this.formatTreeNode(node.parent.data)
      }

      const param = {
        typeId: node.data.id,
        channelId: channelId
      }
      updateSetting(param).then((res) => {
        this.loadSettingData(this.loadTreeData)
      })
    }
  }
}
</script>

<style scoped lang="scss">
.custom-tree-node {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-size: 14px;
  padding-left: 8px;
  padding-right: 32px;
}
.tree-main {
  overflow-y: auto;
}
.tree-head {
  height: 46px;
  line-height: 46px;
  border-bottom: 1px solid var(--TableBorderColor, #e6e6e6);
  border-top: 1px solid var(--TableBorderColor, #e6e6e6);
  background-color: var(--SiderBG, #f7f8fa);
  font-size: 12px;
  color: var(--TableColor, #3d4d66);
  font-family: PingFang SC;
  font-size: 14px;
  font-weight: 500;
  padding: 0 12px
}

.auth-span {
  float: right;
  margin-left: 24px;
}

.auth-span-check {
  float: right;
  margin-left: 64px;
}
</style>
<style lang="scss">
.msg-setting {
  .el-tree-node__content {
    height: 46px;
    border-bottom: 1px solid rgba(31, 35, 41, 0.15);
    &:hover {
      background-color: var(--deWhiteHover, #3370ff) !important;
      .custom-tree-node {
        color: var(--primary, #3370ff);
      }
    }
  }
}
</style>
