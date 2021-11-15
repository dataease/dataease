<template>
  <de-main-container style="height: calc(100vh - 56px)">
    <auth-config  />
  </de-main-container>
</template>
<script>
import DeMainContainer from '@/components/dataease/DeMainContainer'
import AuthConfig from './authConfig'

export default {
  name: 'RowPermissions',
  components: { DeMainContainer, AuthConfig },
  data () {
    return {
      authorityType: 'authConfig'
    }
  },
  watch: {
  },
  mounted () {
  },
  methods: {
    executeAxios (param) {
      this.$emit('execute-axios', param)
    },
    handleClick () {
      console.log('===>handleClick')
    }

  }
}
</script>

<style scoped>
</style>
