<template>
  <div class="dataset-union" @mouseup="mouseupDrag">
    <!-- <el-form :inline="true" style="display: flex;align-items: center;justify-content: space-between;">
        <el-form-item class="form-item" :label="$t('commons.name')">
          <el-input v-model="name" size="mini" :placeholder="$t('commons.name')" clearable />
        </el-form-item>
        <el-form-item class="form-item">
          <el-button :disabled="dataset.length === 0" size="mini" @click="previewData">
            {{ $t('dataset.preview_result') }}
          </el-button>
        </el-form-item>
      </el-form> -->
    <div :style="{ height: unionHeight + 'px' }" class="unio-editer-container">
      <!--添加第一个数据集按钮-->
      <div v-if="dataset.length === 0">
        <el-button type="primary" size="mini" @click="selectDs">
          {{ $t('chart.select_dataset') }}
        </el-button>
      </div>
      <!--数据集关联树型结构-->
      <div v-else class="union-container">
        <node-item
          :current-node="dataset[0]"
          :node-index="0"
          :origin-data="dataset"
          @deleteNode="deleteNode"
          @notifyParent="calc"
          @editUnion="unionConfig"
          @cancelUnionEdit="cancelUnion"
        />
        <div v-if="dataset.length > 0">
          <union-node
            v-for="(item, index) in dataset[0].childrenDs"
            :key="index"
            :node-index="index"
            :children-node="item"
            :children-list="dataset[0].childrenDs"
            :parent-node="dataset[0]"
            :origin-data="dataset"
            @notifyParent="calc"
            @cancelUnionEdit="cancelUnion"
          />
        </div>
      </div>
    </div>
    <div class="preview-container">
      <div class="sql-title">
        {{ $t('deDataset.data_preview') }}
        <span class="result-num">{{
          `(${$t('dataset.preview_show')} 1000 ${$t('dataset.preview_item')})`
        }}</span>
        <span @mousedown="mousedownDrag" class="drag"></span>
      </div>
      <union-preview :table="previewTable" :dataset="dataset" />
    </div>
    <!--选择数据集-->
    <el-drawer
      v-if="selectDsDialog"
      :title="$t('chart.select_dataset')"
      :visible.sync="selectDsDialog"
      custom-class="user-drawer sql-dataset-drawer"
      size="600px"
      v-closePress
      direction="rtl"
    >
      <dataset-tree
        :fix-height="true"
        show-mode="union"
        :custom-type="customType"
        clear-empty-dir="true"
        @getTable="firstDs"
      />
      <div class="de-foot">
        <deBtn secondary @click="closeSelectDs()">{{
          $t('dataset.cancel')
        }}</deBtn>
        <deBtn
          :disabled="!tempDs.id"
          type="primary"
          @click="confirmSelectDs()"
          >{{ $t('dataset.confirm') }}</deBtn
        >
      </div>
    </el-drawer>

    <!--编辑关联关系-->
    <el-dialog
      v-if="editUnion"
      v-dialogDrag
      top="5vh"
      :title="
        unionParam.type === 'add'
          ? $t('dataset.add_union_relation')
          : $t('dataset.edit_union_relation')
      "
      :visible="editUnion"
      :show-close="false"
      width="600px"
      class="dialog-css"
    >
      <union-edit :union-param="unionParam" />
      <div slot="footer" class="dialog-footer">
        <el-button size="mini" @click="closeEditUnion()">{{
          $t('dataset.cancel')
        }}</el-button>
        <el-button type="primary" size="mini" @click="confirmEditUnion()">{{
          $t('dataset.confirm')
        }}</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import UnionNode from '@/views/dataset/add/union/UnionNode'
import NodeItem from '@/views/dataset/add/union/NodeItem'
import datasetTree from '@/views/dataset/common/datasetTree'
import UnionEdit from '@/views/dataset/add/union/UnionEdit'
import { post } from '@/api/dataset/dataset'
import UnionPreview from '@/views/dataset/add/union/UnionPreview'
export default {
  name: 'AddUnion',
  components: {
    UnionPreview,
    UnionEdit,
    datasetTree,
    NodeItem,
    UnionNode
  },
  props: {
    param: {
      type: Object,
      required: true
    }
  },
  data() {
    return {
      // mock data，结构比较复杂，需要这个结构多看看...
      datasetMock: [
        {
          currentDs: {},
          currentDsField: [],
          childrenDs: [
            {
              currentDs: {},
              currentDsField: [],
              childrenDs: [],
              unionToParent: {
                unionType: 'left', // left join,right join,inner join
                unionFields: [
                  {
                    parentField: {},
                    currentField: {}
                  }
                ]
              },
              allChildCount: 0
            }
          ],
          unionToParent: {},
          allChildCount: 0
        }
      ],
      // union data
      dataset: [],
      unionHeight: 298,
      // union item
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
      editUnion: false,
      unionParam: {},
      previewTable: {}
    }
  },
  watch: {
    'param.tableId': function () {
      this.resetComponent()
      this.initTableData()
    }
  },
  mounted() {
    this.initTableData()
  },
  methods: {
    mousedownDrag() {
      document
        .querySelector('.dataset-union')
        .addEventListener('mousemove', this.caculateHeight)
    },
    mouseupDrag() {
      document
        .querySelector('.dataset-union')
        .removeEventListener('mousemove', this.caculateHeight)
    },
    caculateHeight(e) {
      this.unionHeight = e.pageY - 56
    },
    save() {
      if (!this.param.name || this.param.name === '') {
        this.openMessageSuccess('dataset.pls_input_name', 'error')
        return
      }
      if (this.param.name.length > 50) {
        this.openMessageSuccess('dataset.char_can_not_more_50', 'error')
        return
      }
      const table = {
        id: this.param.tableId,
        name: this.param.name,
        sceneId: this.param.id,
        dataSourceId: this.dataset[0].currentDs.dataSourceId,
        type: 'union',
        mode: this.dataset[0].currentDs.mode,
        info: '{"union":' + JSON.stringify(this.dataset) + '}'
      }
      post('/dataset/table/update', table).then((response) => {
        this.$emit('saveSuccess', table)
        this.cancel()
      })
    },
    cancel() {
      this.$router.back()
    },
    selectDs() {
      this.selectDsDialog = true
    },
    firstDs(val) {
      this.tempDs = val
    },
    closeSelectDs() {
      this.selectDsDialog = false
      this.tempDs = {}
    },
    confirmSelectDs() {
      if (this.tempDs.mode === 0 && this.tempDs.modelInnerType === 'sql') {
        this.openMessageSuccess('deDataset.sql_ds_union_error')
        return
      }
      const ds = JSON.parse(JSON.stringify(this.unionItem))
      ds.currentDs = this.tempDs
      this.dataset.push(ds)
      this.closeSelectDs()
      this.calc('union')
    },
    deleteNode(index) {
      this.dataset.splice(index, 1)
      this.calc('delete')
    },
    calc(param) {
      if (param.type === 'union') {
        if (param.grandParentAdd) {
          this.dataset[0] && this.dataset[0].allChildCount++
        }
      } else if (param.type === 'delete') {
        if (param.grandParentSub) {
          if (param.subCount > 1) {
            this.dataset[0] && (this.dataset[0].allChildCount -= param.subCount)
          } else {
            this.dataset[0] && this.dataset[0].allChildCount--
          }
        }
      }
    },

    unionConfig(param) {
      this.unionParam = param
      this.editUnion = true
    },
    closeEditUnion() {
      this.editUnion = false
      // 添加关联的时候，如果关闭关联关系设置的界面，则删除子节点，同时向父级传递消息
      if (this.unionParam.type === 'add') {
        this.dataset[0].childrenDs.pop()
        this.calc({
          type: 'delete',
          grandParentAdd: true,
          grandParentSub: true,
          subCount: 0
        })
      }
    },
    confirmEditUnion() {
      // 校验关联关系与字段，必填
      if (this.checkUnion()) {
        this.editUnion = false
      } else {
        this.openMessageSuccess('deDataset.union_error')
      }
    },
    cancelUnion(val) {
      this.dataset = val
    },

    checkUnion() {
      const union = this.unionParam.node.unionToParent
      if (!union.unionType) {
        return false
      }
      if (!union.unionFields || union.unionFields.length < 1) {
        return false
      }
      for (let i = 0; i < union.unionFields.length; i++) {
        const ele = union.unionFields[i]
        if (
          !ele.parentField ||
          !ele.parentField.id ||
          !ele.currentField ||
          !ele.currentField.id
        ) {
          return false
        }
      }
      return true
    },

    initTableData() {
      if (this.param.tableId) {
        post('/dataset/table/get/' + this.param.tableId, null).then(
          (response) => {
            const table = JSON.parse(JSON.stringify(response.data))
            this.dataset = JSON.parse(table.info).union
            this.previewData()
          }
        )
      }
    },

    previewData() {
      this.previewTable = {
        id: this.param.tableId,
        name: this.param.name,
        sceneId: this.param.id,
        dataSourceId: this.dataset[0].currentDs.dataSourceId,
        type: 'union',
        mode: this.dataset[0].currentDs.mode,
        info: '{"union":' + JSON.stringify(this.dataset) + '}'
      }
    },

    resetComponent() {
      this.dataset = []
      this.param.name = '关联数据集'
    }
  }
}
</script>

<style lang="scss" scoped>
.dataset-union {
  height: 100%;
  display: flex;
  flex-direction: column;
  width: 100%;

  .unio-editer-container {
    min-height: 298px;
    width: 100%;
  }

  .preview-container {
    flex: 1;
    font-family: PingFang SC;
    font-size: 14px;
    overflow-y: auto;
    box-sizing: border-box;
    flex: 1;
    .sql-title {
      user-select: none;
      height: 54px;
      display: flex;
      align-items: center;
      padding: 16px 24px;
      font-weight: 500;
      position: relative;
      color: var(--deTextPrimary, #1f2329);
      border-bottom: 1px solid rgba(31, 35, 41, 0.15);

      .result-num {
        font-weight: 400;
        color: var(--deTextSecondary, #646a73);
        margin-left: 12px;
      }
      .drag {
        position: absolute;
        top: 0;
        left: 50%;
        transform: translateX(-50%);
        height: 7px;
        width: 100px;
        border-radius: 3.5px;
        background: rgba(31, 35, 41, 0.1);
        cursor: row-resize;
      }
    }
  }

  .union-container {
    display: flex;
    width: 100%;
    overflow: auto;
    height: 100%;
  }
}
</style>
