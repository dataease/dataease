<template xmlns:el-col="http://www.w3.org/1999/html">
  <layout-content :header="$t('webmsg.receive_manage')">
    <el-col>
      <el-row class="tree-head">
        <span style="float: left;padding-left: 10px">{{ $t('webmsg.type') }}</span>
        <span v-for="channel in msg_channels" :key="channel.msgChannelId" class="auth-span">
          {{ channel.channelName }}
        </span>
      </el-row>
      <el-row style="margin-top: 5px">
        <el-tree
          :props="defaultProps"
          :data="treeData"
          default-expand-all
          :node-key="defaultProps.id"
          :highlight-current="highlightCurrent"
          @node-click="nodeClick"
        >
          <span slot-scope="{ node, data }" class="custom-tree-node">
            <span>
              <span style="margin-left: 6px" v-html="data.name" />
            </span>
            <span @click.stop>
              <!-- <div v-if="setting_data[data.id]">
              <span v-for="channel in setting_data[data.id]" :key="channel.channelId" class="auth-span">
                <a href="javascript:;" @click="clickAuth(data.id,channel)">
                  <svg-icon style="width: 25px;height: 25px" :icon-class="channel.enable ? 'lock_open' : 'lock_closed'" />
                </a>
              </span>
            </div> -->
              <div>
                <span v-for="channel in msg_channels" :key="channel.msgChannelId" class="auth-span">
                  <a href="javascript:;" @click="clickAuth(node,channel)">
                    <svg-icon style="width: 25px;height: 25px" :icon-class="checkBoxStatus(node, channel)?'lock_open':'lock_closed'" />
                  </a>
                </span>
              </div></span>
          </span>
        </el-tree>
      </el-row>
    </el-col>
  </layout-content>
</template>

<script>
import LayoutContent from '@/components/business/LayoutContent'
import { treeList, channelList, settingList, updateSetting } from '@/api/system/msg'
export default {
  name: 'LazyTree',
  components: { LayoutContent },

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
  computed: {

  },
  mounted() {

  },
  created() {
    this.loadChannelData()
    this.loadSettingData(this.loadTreeData)
  },

  methods: {
    // 加载树节点数据
    loadTreeData() {
      treeList().then(res => {
        this.treeData = res.data
      })
    },
    // 加载消息渠道
    loadChannelData() {
      channelList().then(res => {
        this.msg_channels = res.data
      })
    },
    // 加载用户设置信息
    loadSettingData(callBack) {
      // this.setting_data = {}
      const temp_setting_data = {}
      settingList().then(res => {
        const lists = res.data
        lists.forEach(item => {
          const key = item.typeId
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
      const nodeId = node.data.id
      return this.setting_data[nodeId] && this.setting_data[nodeId].some(item => item.channelId === channel.msgChannelId && item.enable)
    },
    clickAuth(node, channel) {
      // const status = this.checkBoxStatus(nodeId, channel)
      const param = {
        typeId: node.data.id,
        channelId: channel.msgChannelId
      }
      updateSetting(param).then(res => {
        this.loadSettingData()
        // node.checked = true
      })
    },
    nodeClick(data, node) {
      console.log(data)
    }
  }
}
</script>

<style scoped>
  .custom-tree-node {
    flex: 1;
    display: flex;
    align-items: center;
    justify-content: space-between;
    font-size: 14px;
    padding-left: 8px;
  }
  .tree-main{
    height:  calc(100vh - 210px);
    border: 1px solid #e6e6e6;
    overflow-y: auto;
  }
  .tree-head{
    height: 30px;
    line-height: 30px;
    border-bottom: 1px solid #e6e6e6;
    background-color: #f7f8fa;
    font-size: 12px;
    color: #3d4d66 ;
  }

  .auth-span{
    float: right;
    width:50px;
    margin-right: 30px
  }
  .highlights-text {
    color: #faaa39 !important;
  }

</style>
