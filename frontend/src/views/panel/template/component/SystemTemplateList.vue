<template xmlns:el-col="http://www.w3.org/1999/html">
  <el-col>
    <el-row>
      <div class="block">
        <el-form>
          <el-form-item class="form-item">
            <el-input
              v-model="systemTemplateFilterText"
              placeholder="输入关键字进行过滤"
              size="mini"
              clearable
              prefix-icon="el-icon-search"
            />
          </el-form-item>
        </el-form>
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
                  icon="el-icon-picture-outline"
                  type="text"
                />
              </span>
              <span style="margin-left: 6px">{{ data.name }}</span>
            </span>
          </span>
        </el-tree>
      </div>
    </el-row>
  </el-col>
</template>

<script>
import { get, post } from '@/api/panel/panel'

export default {
  name: 'SystemTemplateList',
  components: { },
  data() {
    return {
      systemTemplateFilterText: '',
      defaultExpandedKeys: [],
      systemTemplateList: [],
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
  mounted() {
    this.getTree()
  },
  methods: {
    filterNode(value, data) {
      if (!value) return true
      return data.label.indexOf(value) !== -1
    },
    getTree() {
      const request = {
        templateType: 'system',
        level: '0'
      }
      post('/template/templateList', request).then(res => {
        this.systemTemplateList = res.data
        if (this.systemTemplateList && this.systemTemplateList.length > 0) {
          const id = this.systemTemplateList[0].id
          // this.currentNodeKey = id
          // this.$refs['systemTemplateTree'].setCurrentKey(id)
          this.$emit('showCurrentTemplate', id)
        }
      })
    },
    nodeClick(data, node) {
      console.log('nodeClick')
      debugger
      this.$emit('showCurrentTemplate', data.id)
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
