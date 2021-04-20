<template xmlns:el-col="http://www.w3.org/1999/html">
  <el-col>
    <el-row>
      <el-button icon="el-icon-folder-add" type="primary" size="mini" @click="add()">
        添加分类
      </el-button>
    </el-row>
    <el-row style="margin-top: 5px">
      <el-row>
        <el-input
          v-model="systemTemplateFilterText"
          placeholder="输入关键字进行过滤"
          size="mini"
          clearable
          prefix-icon="el-icon-search"
        />
      </el-row>
      <el-row style="margin-top: 5px">
        <el-tree
          ref="systemTemplateTree"
          :default-expanded-keys="defaultExpandedKeys"
          :data="systemTemplateList"
          node-key="id"
          :expand-on-click-node="true"
          :filter-node-method="filterNode"
          highlight-current
          @node-click="nodeClick"
        >
          <span slot-scope="{ data }" class="custom-tree-node">
            <span>
              <span>
                <el-button
                  icon="el-icon-collection"
                  type="text"
                />
              </span>
              <span style="margin-left: 6px">{{ data.name }}</span>
            </span>
          </span>
        </el-tree>
      </el-row>
    </el-row>
  </el-col>
</template>

<script>
export default {
  name: 'SystemTemplateList',
  components: { },
  props: {
    systemTemplateList: {
      type: Array,
      default: function() {
        return []
      }
    }
  },
  data() {
    return {
      systemTemplateFilterText: '',
      defaultExpandedKeys: [],
      currentTemplateShowList: []
    }
  },
  computed: {

  },
  watch: {
    systemTemplateFilterText(val) {
      this.$refs.systemTemplateTree.filter(val)
    }
  },
  methods: {
    filterNode(value, data) {
      if (!value) return true
      return data.label.indexOf(value) !== -1
    },
    nodeClick(data, node) {
      console.log('nodeClick')
      debugger
      this.$emit('showCurrentTemplate', data.id)
    },
    add() {
      this.$emit('showTemplateEditDialog', 'new')
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
