<template>
  <de-container>
    <de-main-container>
      <el-tabs v-model="authorityType" @tab-click="handleClick">
        <el-tab-pane name="authConfig">
          <span slot="label">权限配置</span>
          <auth-config />
        </el-tab-pane>
        <el-tab-pane name="authQuickConfig">
          <span slot="label">权限快捷配置</span>
          <auth-quick-config />
        </el-tab-pane>
      </el-tabs>
    </de-main-container>
  </de-container>
</template>
<script>
import DeMainContainer from '@/components/dataease/DeMainContainer'
import DeContainer from '@/components/dataease/DeContainer'
import AuthConfig from './authConfig'
import AuthQuickConfig from './authQuickConfig'

export default {
  name: 'Authority',
  components: { DeContainer, DeMainContainer, AuthConfig, AuthQuickConfig },
  data() {
    return {
      authorityType: 'authConfig'
    }
  },
  watch: {
  },
  mounted() {
  },
  methods: {
    handleClick() {
      console.log('===>handleClick')
    }

  }
}
</script>

<style scoped>
</style>
