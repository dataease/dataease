<template>
  <ms-container>

    <ms-aside-container>
      <group />
    </ms-aside-container>

    <ms-main-container>
      <keep-alive>
        <router-view />
      </keep-alive>
    </ms-main-container>
  </ms-container>
</template>

<script>
import MsMainContainer from '@/metersphere/common/components/MsMainContainer'
import MsContainer from '@/metersphere/common/components/MsContainer'
import MsAsideContainer from '@/metersphere/common/components/MsAsideContainer'
import Group from './group/Group'

export default {
  name: 'DataSet',
  components: { MsMainContainer, MsContainer, MsAsideContainer, Group },
  data() {
    return {}
  }
}
</script>

<style scoped>
  .ms-aside-container {
    height: calc(100vh - 40px);
    padding: 15px;
    min-width: 300px;
    max-width: 500px;
  }

  .ms-main-container {
    height: calc(100vh - 40px);
  }

</style>
