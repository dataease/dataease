<template>
  <div>
    <div class="ds-node" @click="nodeClick">
      <svg-icon v-if="currentNode.currentDs.modelInnerType === 'db'" icon-class="ds-db" class="ds-icon-db" />
      <svg-icon v-else-if="currentNode.currentDs.modelInnerType === 'sql'" icon-class="ds-sql" class="ds-icon-sql" />
      <svg-icon v-else-if="currentNode.currentDs.modelInnerType === 'excel'" icon-class="ds-excel" class="ds-icon-excel" />

      <span class="node-name" :title="currentNode.currentDs.name">{{ currentNode.currentDs.name }}</span>

      <span class="node-menu" @click.stop>
        <el-dropdown trigger="click" size="small" @command="nodeMenuClick">
          <span class="el-dropdown-link">
            <el-button icon="el-icon-more" type="text" size="small" />
          </span>
          <el-dropdown-menu slot="dropdown">
            <el-dropdown-item icon="el-icon-copy-document" :command="beforeNodeMenuClick('union',currentNode)">
              <span style="font-size: 12px;">{{ $t('dataset.union') }}</span>
            </el-dropdown-item>
            <el-dropdown-item icon="el-icon-edit-outline" :command="beforeNodeMenuClick('edit',currentNode)">
              <span style="font-size: 12px;">{{ $t('dataset.edit') }}</span>
            </el-dropdown-item>
            <el-dropdown-item icon="el-icon-delete" :command="beforeNodeMenuClick('delete',currentNode)">
              <span style="font-size: 12px;">{{ $t('dataset.delete') }}</span>
            </el-dropdown-item>
          </el-dropdown-menu>
        </el-dropdown>
      </span>
    </div>

    <!--选择数据集-->
    <el-dialog v-dialogDrag :title="$t('chart.select_dataset')" :visible="selectDsDialog" :show-close="false" width="360px" class="dialog-css" destroy-on-close>
      <dataset-group-selector-tree :fix-height="true" show-mode="union" :custom-type="customType" @getTable="firstDs" />
      <div slot="footer" class="dialog-footer">
        <el-button size="mini" @click="closeSelectDs()">{{ $t('dataset.cancel') }}</el-button>
        <el-button :disabled="!tempDs.id" type="primary" size="mini" @click="confirmSelectDs()">{{ $t('dataset.confirm') }}</el-button>
      </div>
    </el-dialog>

    <!--编辑单个数据集字段-->
    <el-dialog v-if="editField" v-dialogDrag :title="$t('dataset.field_select')" :visible="editField" :show-close="false" width="360px" class="dialog-css" destroy-on-close>
      <union-field-edit :node="currentNode" />
      <div slot="footer" class="dialog-footer">
        <el-button size="mini" @click="closeEditField()">{{ $t('dataset.cancel') }}</el-button>
        <el-button type="primary" size="mini" @click="confirmEditField()">{{ $t('dataset.confirm') }}</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import DatasetGroupSelectorTree from '@/views/dataset/common/DatasetGroupSelectorTree'
import UnionFieldEdit from '@/views/dataset/add/union/UnionFieldEdit'
export default {
  name: 'NodeItem',
  components: { UnionFieldEdit, DatasetGroupSelectorTree },
  props: {
    currentNode: {
      type: Object,
      required: true
    },
    nodeIndex: {
      type: Number,
      required: true
    }
  },
  data() {
    return {
      unionItem: {
        currentDs: {},
        currentDsField: [],
        childrenDs: [],
        unionToParent: {
          unionType: 'left',
          unionFields: []
        },
        allChildCount: 0
      },
      customType: ['db', 'sql', 'excel'],
      selectDsDialog: false,
      // 弹框临时选中的数据集
      tempDs: {},
      // 父级数据集
      tempParentDs: {},
      editField: false
    }
  },
  methods: {
    nodeClick() {
      this.editField = true
    },
    nodeMenuClick(param) {
      switch (param.type) {
        case 'union':
          this.unionNode(param)
          break
        case 'edit':
          this.editNode(param)
          break
        case 'delete':
          this.deleteNode(param)
          break
      }
    },
    beforeNodeMenuClick(type, item) {
      return {
        'type': type,
        'item': item
      }
    },

    unionNode(param) {
      this.tempParentDs = param.item
      this.selectDs()
    },
    editNode(param) {
      this.nodeClick()
    },
    deleteNode(param) {
      this.$emit('deleteNode', this.nodeIndex)
      this.notifyFirstParent('delete')
    },

    selectDs() {
      this.selectDsDialog = true
    },
    // 弹框中选择数据集
    firstDs(val) {
      this.tempDs = val
    },
    closeSelectDs() {
      this.selectDsDialog = false
      this.tempDs = {}
    },
    confirmSelectDs() {
      const ds = JSON.parse(JSON.stringify(this.unionItem))
      ds.currentDs = this.tempDs
      this.tempParentDs.childrenDs.push(ds)
      this.closeSelectDs()
      this.notifyFirstParent('union')

      // 构建新建关联关系传递的参数
      const param = {
        type: 'add',
        nodeIndex: this.nodeIndex,
        node: ds,
        parent: this.tempParentDs
      }
      this.$emit('editUnion', param)
    },
    // 增加与删除事件向父级传递
    notifyFirstParent(type) {
      this.$emit('notifyParent', { type: type, grandParentAdd: true, grandParentSub: true, subCount: this.currentNode.allChildCount })
    },
    closeEditField() {
      this.editField = false
    },
    confirmEditField() {
      this.editField = false
    }
  }
}
</script>

<style scoped>
.ds-node{
  width:160px;
  height: 26px;
  line-height: 26px;
  border: #dcdfe6 solid 1px;
  min-width: 160px;
  color: var(--TextPrimary,#606266);
  font-size: 14px;
  display: flex;
  align-items: center;
  padding: 0 6px;
}
.node-name{
  flex: 1;
  text-overflow: ellipsis;
  white-space: pre;
  overflow: hidden;
  padding: 0 2px;
}
.ds-node .node-menu{
  visibility: hidden;
}
.ds-node:hover .node-menu{
  visibility: visible;
}
.ds-node:hover{
  cursor: pointer;
  border: var(--Main,#2681ff) solid 1px;
}
.dialog-css >>> .el-dialog__body {
  padding: 0 20px;
}
</style>
