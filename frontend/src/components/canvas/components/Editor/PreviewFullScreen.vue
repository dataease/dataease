<template>
  <div style="width: 100%;height: 100%;background-color: #f7f8fa">
    <span style="line-height: 35px; position: absolute; top:10px;right: 20px;z-index:100000">
      <el-button size="mini" @click="toDir">
        关闭
      </el-button>
    </span>
    <Preview />
  </div>
</template>

<script>

import Preview from './Preview'
import bus from '@/utils/bus'

export default {
  components: { Preview },

  methods: {
    toDir() {
      this.$router.replace('/panel/index')
      bus.$emit('PanelSwitchComponent', { name: 'PanelEdit' })
    }
  }
}
</script>

