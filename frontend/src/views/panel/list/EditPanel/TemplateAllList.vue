<template xmlns:el-col="http://www.w3.org/1999/html">
  <el-col>
    <el-row style="margin-top: 5px">
      <el-row>
        <el-input
          v-model="templateFilterText"
          placeholder="输入关键字进行过滤"
          size="mini"
          clearable
          prefix-icon="el-icon-search"
        />
      </el-row>
      <el-row style="margin-top: 5px">
        <el-tree
          ref="templateTree"
          :default-expanded-keys="defaultExpandedKeys"
          :data="templateList"
          node-key="id"
          :expand-on-click-node="true"
          :filter-node-method="filterNode"
          :highlight-current="true"
          @node-click="nodeClick"
        >
          <span slot-scope="{ node, data }" class="custom-tree-node">
            <span>
              <span v-if="data.nodeType==='template'">
                <el-button
                  icon="el-icon-picture-outline"
                  type="text"
                />
              </span>
              <span v-if="data.nodeType==='folder'">
                <el-button
                  icon="el-icon-folder"
                  type="text"
                />
              </span>
              <span style="margin-left: 6px">{{ data.name }}</span>
            </span>
          </span></el-tree>
      </el-row>
    </el-row>
  </el-col>
</template>

<script>
export default {
  name: 'TemplateAllList',
  components: { },
  props: {
    templateList: {
      type: Array,
      default: function() {
        return []
      }
    }
  },
  data() {
    return {
      templateFilterText: '',
      defaultExpandedKeys: [],
      currentTemplateShowList: []
    }
  },
  computed: {

  },
  watch: {
    templateFilterText(val) {
      this.$refs.templateTree.filter(val)
    }
  },
  methods: {
    clickMore(param) {
      console.log(param)
      switch (param.type) {
        case 'edit':
          this.templateEdit(param.data)
          break
        case 'delete':
          this.templateDelete(param.data)
          break
      }
    },
    beforeClickMore(type, data, node) {
      return {
        'type': type,
        'data': data,
        'node': node
      }
    },

    filterNode(value, data) {
      if (!value) return true
      return data.label.indexOf(value) !== -1
    },
    nodeClick(data, node) {
      this.$emit('showCurrentTemplateInfo', data)
    }
  }
}
</script>

<style scoped>
  .custom-tree-node {
    flex: 1;
    display: flex;
    align-items: center;
    justify-content: space-between;
    font-size: 14px;
    padding-right: 8px;
  }
</style>
