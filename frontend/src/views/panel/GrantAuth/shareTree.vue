<template>
  <div>

    <el-tree :data="data" :props="defaultProps" @node-click="handleNodeClick" />
  </div>
</template>

<script>
import { loadTree } from '@/api/panel/share'
export default {
  name: 'ShareTree',
  data() {
    return {
      data: [],
      defaultProps: {
        children: 'children',
        label: 'name'
      }
    }
  },
  created() {
    this.initData()
  },
  methods: {
    initData() {
      const param = {}
      loadTree(param).then(res => {
        this.data = res.data
      })
    },
    handleNodeClick(data) {
      console.log(data)
    }
  }
}
</script>
