<template>
  <el-tabs v-model="activeName" @tab-click="handleClick">
    <el-tab-pane label="部门" name="first">部门</el-tab-pane>
    <el-tab-pane label="角色" name="second">角色</el-tab-pane>
    <el-tab-pane label="用户" name="third">用户</el-tab-pane>
  </el-tabs>
</template>

<script>
export default {
  name: 'GrantAuth',
  props: {
    resourceId: {
      type: String,
      default: null
    }
  },
  data() {
    return {
      activeName: 'second'
    }
  },

  methods: {
    handleClick(tab, event) {
      console.log(tab, event)
    }
  }
}
</script>
