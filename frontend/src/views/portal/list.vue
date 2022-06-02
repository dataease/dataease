<template>
  <div class="portal-container">
    <div class="header">
      <el-button
        plain
        icon="el-icon-circle-plus-outline"
        @click="handleAddPortalDrawer"
        >新建数据门户</el-button
      >
    </div>
    <div class="content">
      <el-table :data="list">
        <el-table-column label="名称" prop="name"></el-table-column>
        <el-table-column label="创建者" prop="created"></el-table-column>
        <el-table-column label="修改人" prop="updated"></el-table-column>
        <el-table-column label="修改时间"></el-table-column>
        <el-table-column label="操作">
          <div class="table-option">
            <i class="el-icon-edit"></i>
            <i class="el-icon-table-lamp"></i>
            <i class="el-icon-delete"></i>
          </div>
        </el-table-column>
      </el-table>
    </div>
    <div class="bottom"></div>
    <div class="other">
      <PortalDrawerComponent
        :open-type="openType"
        :visible.sync="showPortalDrawer"
      ></PortalDrawerComponent>
    </div>
  </div>
</template>

<script>
import PortalDrawerComponent from "./components/PortalDrawerComponent.vue";
export default {
  components: {
    PortalDrawerComponent,
  },
  data() {
    return {
      list: [
        {
          name: "123",
          created: "123",
          updated: "123",
        },
      ],
      showPortalDrawer: false,
      openType: "add", // edit
    };
  },

  methods: {
    handleAddPortalDrawer() {
      this.openType = 'add'
      this.showPortalDrawer = true;
    },
  },
};
</script>

<style lang="scss" scoped>
.portal-container {
  padding: 10px;
  box-sizing: border-box;
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
}
</style>
