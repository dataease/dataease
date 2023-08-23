<template>
  <div>
    <div
      class="ds-node"
      @click="nodeClick"
    >
      <svg-icon
        v-if="currentNode.currentDs.modelInnerType === 'db'"
        icon-class="ds-db"
        class="ds-icon-db"
      />
      <svg-icon
        v-else-if="currentNode.currentDs.modelInnerType === 'sql'"
        icon-class="ds-sql"
        class="ds-icon-sql"
      />
      <svg-icon
        v-else-if="currentNode.currentDs.modelInnerType === 'excel'"
        icon-class="ds-excel"
        class="ds-icon-excel"
      />
      <svg-icon
        v-else-if="currentNode.currentDs.modelInnerType === 'api'"
        icon-class="ds-api"
        class="ds-icon-api"
      />

      <span
        class="node-name"
        :title="currentNode.currentDs.name"
      >{{
        currentNode.currentDs.name
      }}</span>

      <span
        class="node-menu"
        @click.stop
      >
        <el-dropdown
          trigger="click"
          size="small"
          @command="nodeMenuClick"
        >
          <span class="el-dropdown-link">
            <el-button
              icon="el-icon-more"
              type="text"
              size="small"
            />
          </span>
          <el-dropdown-menu slot="dropdown">
            <el-dropdown-item
              :disabled="
                currentNode.currentDs.mode === 0 &&
                  currentNode.currentDs.modelInnerType === 'sql'
              "
              icon="el-icon-copy-document"
              :command="beforeNodeMenuClick('union', currentNode)"
            >
              <span style="font-size: 12px">{{ $t('dataset.union') }}</span>
            </el-dropdown-item>
            <el-dropdown-item
              icon="el-icon-edit-outline"
              :command="beforeNodeMenuClick('edit', currentNode)"
            >
              <span style="font-size: 12px">{{ $t('dataset.edit') }}</span>
            </el-dropdown-item>
            <el-dropdown-item
              icon="el-icon-delete"
              :command="beforeNodeMenuClick('delete', currentNode)"
            >
              <span style="font-size: 12px">{{ $t('dataset.delete') }}</span>
            </el-dropdown-item>
          </el-dropdown-menu>
        </el-dropdown>
      </span>
    </div>

    <!--选择数据集-->
    <el-drawer
      v-if="selectDsDialog"
      v-closePress
      :title="$t('chart.select_dataset')"
      :visible.sync="selectDsDialog"
      custom-class="de-user-drawer sql-dataset-drawer"
      size="600px"
      direction="rtl"
    >
      <dataset-tree
        :fix-height="true"
        show-mode="union"
        :custom-type="customType"
        :clear-empty-dir="true"
        :mode="currentNode.currentDs.mode"
        @getTable="firstDs"
      />
      <div class="de-foot">
        <deBtn
          secondary
          @click="closeSelectDs()"
        >{{
          $t('dataset.cancel')
        }}</deBtn>
        <deBtn
          :disabled="!tempDs.id"
          type="primary"
          @click="confirmSelectDs()"
        >{{ $t('dataset.confirm') }}</deBtn>
      </div>
    </el-drawer>

    <!--编辑单个数据集字段-->
    <!-- <el-drawer
      v-if="editField"
      :title="$t('dataset.field_select')"
      :visible.sync="editField"
      custom-class="de-user-drawer sql-dataset-drawer"
      size="840px"
      v-closePress
      direction="rtl"
    >
      <union-field-edit :node="currentNode" />
      <div class="de-foot">
        <deBtn size="mini" @click="closeEditField()">{{
          $t("dataset.cancel")
        }}</deBtn>
        <deBtn type="primary" size="mini" @click="confirmEditField()">{{
          $t("dataset.confirm")
        }}</deBtn>
      </div>
    </el-drawer> -->
    <el-dialog
      v-if="editField"
      v-dialogDrag
      :title="$t('dataset.field_select')"
      :visible="editField"
      :show-close="false"
      width="400px"
      class="dialog-css"
    >
      <union-field-edit :node="currentNode" />
      <div
        slot="footer"
        class="dialog-footer"
      >
        <el-button
          size="mini"
          @click="closeEditField()"
        >{{
          $t('dataset.cancel')
        }}</el-button>
        <el-button
          type="primary"
          size="mini"
          @click="confirmEditField()"
        >{{
          $t('dataset.confirm')
        }}</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import datasetTree from '@/views/dataset/common/DatasetTree'
import UnionFieldEdit from '@/views/dataset/add/union/UnionFieldEdit'
export default {
  name: 'NodeItem',
  components: { UnionFieldEdit, datasetTree },
  props: {
    currentNode: {
      type: Object,
      required: true
    },
    nodeIndex: {
      type: Number,
      required: true
    },
    originData: {
      type: Array,
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
      customType: ['db', 'sql', 'excel', 'api'],
      selectDsDialog: false,
      // 弹框临时选中的数据集
      tempDs: {},
      // 父级数据集
      tempParentDs: {},
      editField: false,
      tempData: []
    }
  },
  methods: {
    nodeClick() {
      this.tempData = JSON.parse(JSON.stringify(this.originData))
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
        type: type,
        item: item
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
      // 根据父级node，过滤不同的数据集
      if (this.currentNode.currentDs.mode === 1) {
        this.customType = ['db', 'sql', 'excel', 'api']
      } else if (this.currentNode.currentDs.mode === 0) {
        if (this.currentNode.currentDs.modelInnerType === 'db') {
          this.customType = ['db']
        } else {
          this.customType = []
        }
      }
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
      // 校验当前数据集是否已被关联，以及直连模式是否属于同一数据源
      if (this.tempDs.mode === 0) {
        if (
          this.tempDs.dataSourceId !== this.tempParentDs.currentDs.dataSourceId
        ) {
          this.$message({
            type: 'error',
            message: this.$t('dataset.can_not_union_diff_datasource'),
            showClose: true
          })
          return
        }
      }
      const arr = this.getAllDs()
      if (arr.indexOf(this.tempDs.id) > -1) {
        this.$message({
          type: 'error',
          message: this.$t('dataset.union_repeat'),
          showClose: true
        })
        return
      }
      // check over
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
      this.$emit('notifyParent', {
        type: type,
        grandParentAdd: true,
        grandParentSub: true,
        subCount: this.currentNode.allChildCount
      })
    },
    closeEditField() {
      this.editField = false
      this.$emit('cancelUnionEdit', this.tempData)
    },
    confirmEditField() {
      this.editField = false
    },

    getAllDs() {
      const arr = []
      for (let i = 0; i < this.originData.length; i++) {
        arr.push(this.originData[0].currentDs.id)
        if (
          this.originData[0].childrenDs &&
          this.originData[0].childrenDs.length > 0
        ) {
          this.getDs(this.originData[0].childrenDs, arr)
        }
      }
      return arr
    },
    getDs(dsList, arr) {
      for (let i = 0; i < dsList.length; i++) {
        arr.push(dsList[i].currentDs.id)
        if (dsList[i].childrenDs && dsList[i].childrenDs.length > 0) {
          this.getDs(dsList[i].childrenDs, arr)
        }
      }
    }
  }
}
</script>

<style scoped>
.ds-node {
  width: 160px;
  height: 26px;
  line-height: 26px;
  border: #dcdfe6 solid 1px;
  background: #fff;
  min-width: 160px;
  color: var(--TextPrimary, #606266);
  font-size: 14px;
  display: flex;
  align-items: center;
  padding: 0 6px;
}
.node-name {
  flex: 1;
  text-overflow: ellipsis;
  white-space: pre;
  overflow: hidden;
  padding: 0 2px;
}
.ds-node .node-menu {
  visibility: hidden;
}
.ds-node:hover .node-menu {
  visibility: visible;
}
.ds-node:hover {
  cursor: pointer;
  border: var(--Main, #2681ff) solid 1px;
}
.dialog-css ::v-deep .el-dialog__body {
  padding: 0 20px;
}
</style>
