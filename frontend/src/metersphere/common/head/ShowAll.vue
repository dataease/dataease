<template>
  <el-menu-item :index="this.index" @click="changeRoute">
    <font-awesome-icon :icon="['fa', 'list-ul']"/>
    <span>{{ $t('commons.show_all') }}</span>
  </el-menu-item>
</template>

<script>
export default {
  name: "MsShowAll",
  props: {
    index: String
  },
  methods: {
    changeRoute() {
      // 解决在列表页面点击 显示全部 无效的问题（点击显示全部后改变路由）
      if (this.$route.path === this.index) {
        this.$router.replace({path: this.index, query: {type: 'all'}});
      }
    }
  }
}
</script>

<style scoped>
svg + span {
  padding-left: 5px;
}
</style>
