<template>
  <de-main-container style="height: calc(100vh - 56px)">
    <el-tabs v-model="authorityType" @tab-click="handleClick">
      <el-tab-pane name="authConfig">
        <span slot="label">{{ $t('auth.authConfig') }}</span>
        <auth-config />
      </el-tab-pane>
      <!--      <el-tab-pane name="authQuickConfig">-->
      <!--        <span slot="label">{{$t('auth.authQuickConfig')}}</span>-->
      <!--        <auth-quick-config />-->
      <!--      </el-tab-pane>-->
    </el-tabs>
  </de-main-container>
</template>
<script>
import DeMainContainer from '@/components/dataease/DeMainContainer'
import DeContainer from '@/components/dataease/DeContainer'
import AuthConfig from './authConfig'
import AuthQuickConfig from './authQuickConfig'

export default {
  name: 'Authority',
  // eslint-disable-next-line vue/no-unused-components
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
    }

  }
}
</script>

<style scoped>
</style>
