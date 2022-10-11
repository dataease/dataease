<template xmlns:el-col="http://www.w3.org/1999/html">
  <el-col>
    <el-row style="margin-top: 5px">
      <el-row>
        <el-input
          v-model="templateFilterText"
          :placeholder="$t('panel.filter_keywords')"
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
          <span
            slot-scope="{ data }"
            class="custom-tree-node"
          >
            <span style="display: flex; flex: 1 1 0%; width: 0px;">
              <span v-if="data.nodeType==='template'">
                <svg-icon
                  icon-class="panel"
                  class="ds-icon-scene"
                />
              </span>
              <span v-if="data.nodeType==='folder'">
                <i class="el-icon-folder" />
              </span>
              <span
                :title="data.name"
                style="margin-left: 6px;white-space:nowrap;overflow:hidden;text-overflow:ellipsis;"
              >{{ data.name }}</span>
            </span>
          </span>
        </el-tree>
      </el-row>
    </el-row>
  </el-col>
</template>

<script>
import { findOne } from '@/api/system/template'
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
      findOne(data.id).then(res => {
        this.$emit('showCurrentTemplateInfo', res.data)
      })
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
