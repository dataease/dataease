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
import { uuid } from 'vue-uuid'
import { get } from '@/api/panel/panel'
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
      get('panel/group/findOne/' + data.id).then(response => {
        this.$store.commit('setComponentData', this.resetID(JSON.parse(response.data.panelData)))
        this.$store.commit('setCanvasStyle', JSON.parse(response.data.panelStyle))

        this.$store.dispatch('panel/setPanelInfo', data)
      })
    },
    resetID(data) {
      data.forEach(item => {
        item.id = uuid.v1()
      })

      return data
    }
  }
}
</script>
