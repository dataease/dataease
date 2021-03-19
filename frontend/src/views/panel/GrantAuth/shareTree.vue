<template>
  <div>

    <el-tree :data="datas" :props="defaultProps" @node-click="handleNodeClick">
      <span slot-scope="{ data }" class="custom-tree-node">
        <span>
          <span v-if="!!data.id">
            <el-button
              icon="el-icon-picture-outline"
              type="text"
            />
          </span>
          <span style="margin-left: 6px">{{ data.name }}</span>
        </span>
      </span>
    </el-tree>
  </div>
</template>

<script>
import { loadTree } from '@/api/panel/share'
export default {
  name: 'ShareTree',
  data() {
    return {
      datas: [],
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
        this.datas = res.data
      })
    },
    handleNodeClick(data) {
      console.log(data)
    }
  }
}
</script>
