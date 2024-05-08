<template>
  <el-container>
    <el-main>
      <el-col style="height: 400px;overflow-y: auto;margin-bottom: 10px;">
        <el-tree
          :data="tData"
          node-key="id"
          :expand-on-click-node="false"
          highlight-current
          @node-click="nodeClick"
        >
          <span
            slot-scope="{ node, data }"
            :class="treeClass(data,node)"
          >
            <span style="display: flex;flex: 1;width: 0;">
              <span
                style="margin-left: 6px;white-space:nowrap;overflow:hidden;text-overflow:ellipsis;"
                :title="data.name"
              >{{ data.name }}</span>
            </span>
          </span>
        </el-tree>
      </el-col>
    </el-main>
    <el-footer class="de-footer">
      <el-button @click="closeSave">{{ $t("commons.cancel") }}</el-button>
      <el-button
        :disabled="targetGroup.id === undefined"
        type="primary"
        @click="doSave"
      >{{ $t("commons.confirm") }}
      </el-button>
    </el-footer>
  </el-container>
</template>

<script>
import { listForm, moveForm } from '@/views/dataFilling/form/dataFilling'
import { hasDataPermission } from '@/utils/permission'
import { filter } from 'lodash-es'

export default {
  name: 'DataFillingFormMoveSelector',
  props: {
    item: {
      type: Object,
      required: true
    },
    showSelector: {
      type: Boolean,
      required: true
    }
  },
  data() {
    return {
      tData: [],
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
    'item': function() {
      this.tree(this.groupForm)
    }
  },
  mounted() {
    this.tree(this.groupForm)
  },
  methods: {
    tree(group) {
      listForm({ nodeType: 'folder' }).then(res => {
        const formList = this.filterListDeep(res.data) || []
        if (this.item.nodeType === 'folder') {
          this.tData = [
            {
              id: '0',
              name: this.$t('data_fill.form.form_list_name'),
              label: this.$t('data_fill.form.form_list_name'),
              level: -1,
              children: formList
            }
          ]
        } else {
          this.tData = formList
        }
      })
    },
    filterListDeep(list) {
      return filter(list, item => {
        let hasChildren = item.children && item.children.length > 0
        if (item.children) {
          hasChildren = this.filterListDeep(item.children)
        }
        return hasDataPermission('manage', item.privileges) || hasChildren
      })
    },
    nodeClick(data, node) {
      this.$nextTick(() => {
        if (hasDataPermission('manage', data.privileges)) {
          this.targetGroup = data
        } else {
          this.targetGroup = {}
        }
      })
    },
    treeClass(data, node) {
      if (data.id === this.item.id) {
        node.visible = false
      }
      return 'custom-tree-node'
    },
    closeSave() {
      this.$emit('update:showSelector', false)
    },
    doSave() {
      if (this.targetGroup.id) {
        const data = {
          pid: this.targetGroup.id,
          id: this.item.id
        }
        moveForm(data).then(res => {
          this.$emit('moveSuccess')
        })
      }
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
    padding-right:8px;
  }
  .de-footer {
    display: flex;
    flex-direction: row;
    align-items: center;
    justify-content: flex-end
  }
</style>
