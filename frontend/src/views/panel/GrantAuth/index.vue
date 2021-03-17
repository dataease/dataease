<template>
  <div>
    <el-button v-show="!showSearchInput" class="de-icon" icon="el-icon-search" circle size="mini" @click="showSearchWidget" />
    <div v-show="showSearchInput" class="de-input">
      <el-input v-model="key">
        <el-button slot="append" icon="el-icon-close" @click="closeSearchWidget" />
      </el-input>
    </div>

    <el-tabs v-model="activeName" :class="{'de-search-header': showSearchInput}" @tab-click="handleClick">
      <el-tab-pane :lazy="true" class="de-tab" label="部门" name="1"><grant-dept :resource-id="resourceId" /></el-tab-pane>
      <el-tab-pane :lazy="true" class="de-tab" label="角色" name="2"><grant-role :resource-id="resourceId" /></el-tab-pane>
      <el-tab-pane :lazy="true" class="de-tab" label="用户" name="3"><grant-user :resource-id="resourceId" /></el-tab-pane>
    </el-tabs>
  </div>

</template>

<script>
import GrantDept from './dept'
import GrantRole from './role'
import GrantUser from './user'
export default {
  name: 'GrantAuth',
  components: { GrantDept, GrantRole, GrantUser },
  props: {
    resourceId: {
      type: String,
      default: null
    }
  },
  data() {
    return {
      activeName: '1',
      showSearchInput: false,
      key: null
    }
  },

  methods: {
    handleClick(tab, event) {
      console.log(tab, event)
    },
    showSearchWidget() {
      this.showSearchInput = true
    },
    closeSearchWidget() {
      this.showSearchInput = false
    }
  }
}
</script>

<style lang="scss" scoped>
.de-tab {
    border:1px solid #E6E6E6;
    min-height:200px !important;
    max-height:300px !important;
    overflow:auto;
}
.de-icon {
    position: absolute;
    right: 20px;
    top: 50px;
    z-index: 99;
}
.el-input-group__append{
    background-color: #ffffff;
}
.el-input__inner{
    border-right: none;
}

// ::-webkit-scrollbar {

// }
</style>
