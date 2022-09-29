<template>
  <div class="ds-move-tree">
    <el-input
      :placeholder="$t('deDataset.search_by_name')"
      clearable
      style="margin-bottom: 16px"
      size="small"
      v-model="filterText"
    />
    <div class="tree">
      <el-tree
        :data="tData"
        ref="tree"
        node-key="id"
        class="de-tree"
        :expand-on-click-node="false"
        highlight-current
        :filter-node-method="filterNode"
        @node-click="nodeClick"
      >
        <span slot-scope="{ node, data }" :class="treeClass(data, node)">
          <span style="display: flex; flex: 1; width: 0">
            <span v-if="data.type === 'group'">
              <svg-icon icon-class="scene"/>
            </span>
            <span
              style="
                margin-left: 6px;
                white-space: nowrap;
                overflow: hidden;
                text-overflow: ellipsis;
              "
              v-html="highlights(data.name)"
              :title="data.name"
            ></span>
          </span>
        </span>
      </el-tree>
    </div>
  </div>
</template>

<script>
import { groupTree } from '@/api/dataset/dataset'

export default {
  name: 'GroupMoveSelector',
  props: {
    item: {
      type: Object,
      required: true
    },
    moveDir: {
      type: Boolean,
      default: false
    }
  },
  data() {
    return {
      tData: [],
      filterText: '',
      currGroup: '',
      groupForm: {
        name: '',
        pid: '0',
        level: 0,
        type: 'group',
        children: [],
        sort: 'type desc,name asc'
      },
      targetGroup: {}
    }
  },
  watch: {
    item: function () {
      this.tree(this.groupForm)
    },
    filterText(val) {
      this.$refs.tree.filter(val)
    }
  },
  mounted() {
    this.tree(this.groupForm)
  },
  methods: {
    tree(group) {
      groupTree(group).then((res) => {
        if (this.moveDir) {
          this.tData = [
            {
              id: '0',
              name: this.$t('dataset.dataset_group'),
              pid: '0',
              privileges: 'grant,manage,use',
              type: 'group',
              children: res.data
            }
          ]
          return
        }
        this.tData = res.data
      })
    },
    filterNode(value, data) {
      if (!value) return true
      return data.name.indexOf(value) !== -1
    },
    // 高亮显示搜索内容
    highlights(name) {
      if (!this.filterText) return name
      const replaceReg = new RegExp(this.filterText, 'g') // 匹配关键字正则
      const replaceString =
        '<span class="select-tree-keywords">' + this.filterText + '</span>' // 高亮替换v-html值
      return name.replace(replaceReg, replaceString)
    },
    nodeClick(data, node) {
      this.targetGroup = data
      this.$emit('targetGroup', data)
    },
    treeClass(data, node) {
      if (data.id === this.item.id) {
        node.visible = false
      }
      return 'custom-tree-node'
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
<style lang="scss">
.ds-move-tree {
  height: 100%;
  .tree {
    height: calc(100% - 115px);
    overflow-y: auto;
  }
  .select-tree-keywords {
    color: var(--primary, #3370ff);
  }
}
</style>
