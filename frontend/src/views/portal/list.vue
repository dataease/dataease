<template>
  <div class="portal-container">
    <div class="portal-container-table">
      <div class="header">
        <el-button
          plain
          icon="el-icon-circle-plus-outline"
          @click="handleAddPortalDrawer"
        >新建数据门户</el-button>
      </div>
      <div class="content">
        <el-table v-loading="previewLoading" :data="list">
          <el-table-column label="名称" prop="portalName" />
          <el-table-column label="创建者" prop="userName" />
          <el-table-column label="修改人" prop="updateBy" />
          <el-table-column label="修改时间">
            <template slot-scope="{ row }">
              {{ row.createTime || row.updateTime }}
            </template>
          </el-table-column>
          <el-table-column label="操作">
            <template slot-scope="{ row }">
              <div class="table-option">
                <el-button circle icon="el-icon-edit" size="mini" type="primary" @click="handleEditPortal(row)" />
                <el-button circle icon="el-icon-table-lamp" size="mini" @click="handleUpdateTrend(row)" />
                <el-button circle icon="el-icon-delete" size="mini" type="danger" @click="handleDeleteRow(row)" />
              </div>
            </template>
          </el-table-column>
        </el-table>
      </div>
      <div class="bottom">
        <el-pagination
          background
          :current-page="pageValue.page_number"
          :page-sizes="[10, 20, 50, 100]"
          :page-size="pageValue.page_size"
          layout="prev, pager, next"
          :total="total"
          @current-change="handlePageNumberChange"
          @size-change="handlePageSizeChange"
        />
      </div>
    </div>
    <div class="other">
      <PortalDrawerComponent
        :item="currentItem"
        :open-type="openType"
        :visible.sync="showPortalDrawer"
        @preview="handleConfigPreviewTrend"
      />
    </div>
    <div v-if="currentItem" class="portal-panel-view-show">
      <div>333333333</div>
      <PanelViewShow
        ref="panelViewShow"
        :portal="currentItem"
        @update="update"
      />
    </div>
  </div>
</template>

<script>
import PortalDrawerComponent from './components/PortalDrawerComponent.vue'
import PanelViewShow from '@/views/panel/list/PanelViewShow.vue'
import {
  getPortalList,
  deletePortal,
  savePortal,
  updatePortal
} from '@/api/panel/portal'
import { initPanelData } from '@/api/panel/panel'
import bus from '@/utils/bus'
export default {
  components: {
    PortalDrawerComponent,
    PanelViewShow
  },
  data() {
    return {
      list: [],
      total: 0,
      showPortalDrawer: false,
      openType: 'add', // edit
      pageValue: {
        page_number: 1,
        page_size: 10
      },
      currentItem: null,
      previewLoading: false
    }
  },

  mounted() {
    this.handleGetPortalList()
    bus.$on('savePortal', this.handleSavePortal)
    bus.$on('updatePortal', this.handleUpdatePortal)
  },

  destroyed() {
    bus.$off('savePortal', this.handleSavePortal)
    bus.$off('updatePortal', this.handleUpdatePortal)
  },

  methods: {
    // 新建站点
    handleAddPortalDrawer() {
      this.currentItem = null
      this.openType = 'add'
      this.showPortalDrawer = true
    },

    // 点击编辑站点
    handleEditPortal(row) {
      this.openType = 'edit'
      this.currentItem = {
        ...JSON.parse(row.positionJson),
        id: row.id
      }
      this.showPortalDrawer = true
    },

    // 点击配置里的预览
    handleConfigPreviewTrend(item) {
      let trendId = ''
      function getTreedDataFirstTrendId(treeData) {
        for (let i = 0; i < treeData.length; i++) {
          const item = treeData[i]
          if (item.trendId && !trendId) {
            trendId = item.trendId
          } else {
            getTreedDataFirstTrendId(item.children)
          }
        }
      }

      getTreedDataFirstTrendId(item.config.treeData)
      this.update(trendId)
    },

    // 站点预览
    handleUpdateTrend(row) {
      this.currentItem = {
        ...JSON.parse(row.positionJson),
        id: row.id
      }
      this.previewLoading = true
      // this.showPanelView = true;
      // this.$store.commit("setComponentDataCache", null);
      let trendId = ''
      function getTreedDataFirstTrendId(treeData) {
        for (let i = 0; i < treeData.length; i++) {
          const item = treeData[i]
          if (item.trendId && !trendId) {
            trendId = item.trendId
          } else {
            getTreedDataFirstTrendId(item.children)
          }
        }
      }

      const treeData = JSON.parse(row.positionJson).config.treeData
      getTreedDataFirstTrendId(treeData)
      this.update(trendId)
    },

    update(trendId) {
      console.log('触发此处的刷新功能，-------', trendId)
      if (trendId) {
        if (Object.prototype.toString.call(trendId) === '[object Array]') {
          trendId = trendId[trendId.length - 1]
        }
        const that = this
        console.log('此处的trendId', trendId)
        initPanelData(trendId, function(response) {
          console.log('response===', response)
          bus.$emit('set-panel-show-type', 0)
          setTimeout(() => {
            if (that.$refs.panelViewShow) {
              that.$refs.panelViewShow.clickFullscreen()
              that.previewLoading = false
            }
            that.$watch(
              () => that.$refs.panelViewShow.fullscreen,
              (val) => {
                if (!val) {
                  that.currentItem = null
                }
              }
            )
          }, 1000)
        })
      } else {
        this.previewLoading = false
        this.$message.warning('该站点下还没有配置仪表板')
      }
    },

    // 获取站点列表
    async handleGetPortalList() {
      this.previewLoading = true
      const res = await getPortalList({ ...this.pageValue })
      this.previewLoading = false
      if (res.success) {
        this.total = res.data.total
        this.list = res.data.portalDataList.map((item) => {
          return {
            ...item,
            portalName: JSON.parse(item.positionJson).portalName
          }
        })
      }
    },

    // 删除一个站点
    async handleDeleteRow(row) {
      this.$confirm('删除该站点', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      })
        .then(async() => {
          const res = await deletePortal(row.id)
          if (res.success) {
            this.$message.success('操作成功')
            this.handleGetPortalList()
          }
        })
        .catch(() => {})
    },

    handlePageNumberChange(page_number) {
      this.pageValue.page_number = page_number
      this.handleGetPortalList()
    },

    handlePageSizeChange(evt) {
      console.log('handlePageSizeChange evt', evt)
    },

    async handleSavePortal(params) {
      const res = await savePortal(params)
      if (res.success) {
        this.$message.success('操作成功')
      } else {
        this.$message.error(res.message)
      }
      this.handleGetPortalList()
    },
    async handleUpdatePortal(params) {
      const res = await updatePortal(params)
      if (res.success) {
        this.$message.success('操作成功')
      } else {
        this.$message.error(res.message)
      }
      this.handleGetPortalList()
    }
  }
}
</script>

<style lang="scss" scoped>
.portal-container {
  position: relative;
  .portal-container-table {
    padding: 14px;
    box-sizing: border-box;
    position: absolute;
    left: 0;
    right: 0;
    z-index: 2;
  }
  .header {
    margin-bottom: 10px;
  }
  .content {
    .table-option {
      i {
        cursor: pointer;
        margin-right: 10px;
        font-size: 20px;
      }
      i:last-child {
        margin-right: 0;
      }
    }
  }
  .bottom {
    display: flex;
    justify-content: flex-end;
    padding: 10px 0;
    box-sizing: border-box;
  }
  .portal-panel-view-show {
    position: absolute;
    z-index: 1;
    opacity: 0;
    width: 100%;
    min-height: 100vh;
    visibility: visible;
  }
}
</style>
