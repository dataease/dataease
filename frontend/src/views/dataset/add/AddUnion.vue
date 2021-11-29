<template>
  <el-row>
    <el-row style="height: 26px;" class="title-text">
      <span style="line-height: 26px;font-size: 14px;">
        {{ param.tableId?$t('dataset.edit_union'):$t('dataset.add_union_table') }}
      </span>
      <el-row style="float: right">
        <el-button size="mini" @click="cancel">
          {{ $t('dataset.cancel') }}
        </el-button>
        <el-button size="mini" type="primary" @click="save">
          {{ $t('dataset.confirm') }}
        </el-button>
      </el-row>
    </el-row>
    <el-divider />
    <div>
      <el-form :inline="true">
        <el-form-item class="form-item">
          <el-input v-model="name" size="mini" :placeholder="$t('commons.name')" />
        </el-form-item>
      </el-form>
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
            v-for="(item,index) in dataset[0].childrenDs"
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

    <!--选择数据集-->
    <el-dialog v-dialogDrag :title="$t('chart.select_dataset')" :visible="selectDsDialog" :show-close="false" width="360px" class="dialog-css" destroy-on-close>
      <dataset-group-selector-tree :fix-height="true" show-mode="union" :custom-type="customType" @getTable="firstDs" />
      <div slot="footer" class="dialog-footer">
        <el-button size="mini" @click="closeSelectDs()">{{ $t('dataset.cancel') }}</el-button>
        <el-button :disabled="!tempDs.id" type="primary" size="mini" @click="confirmSelectDs()">{{ $t('dataset.confirm') }}</el-button>
      </div>
    </el-dialog>

    <!--编辑关联关系-->
    <el-dialog v-if="editUnion" v-dialogDrag top="5vh" :title="unionParam.type === 'add' ? $t('dataset.add_union_relation') : $t('dataset.edit_union_relation')" :visible="editUnion" :show-close="false" width="600px" class="dialog-css">
      <union-edit :union-param="unionParam" />
      <div slot="footer" class="dialog-footer">
        <el-button size="mini" @click="closeEditUnion()">{{ $t('dataset.cancel') }}</el-button>
        <el-button type="primary" size="mini" @click="confirmEditUnion()">{{ $t('dataset.confirm') }}</el-button>
      </div>
    </el-dialog>
  </el-row>
</template>

<script>
import UnionNode from '@/views/dataset/add/union/UnionNode'
import NodeItem from '@/views/dataset/add/union/NodeItem'
import DatasetGroupSelectorTree from '@/views/dataset/common/DatasetGroupSelectorTree'
import UnionEdit from '@/views/dataset/add/union/UnionEdit'
export default {
  name: 'AddUnion',
  components: { UnionEdit, DatasetGroupSelectorTree, NodeItem, UnionNode },
  props: {
    param: {
      type: Object,
      required: true
    }
  },
  data() {
    return {
      // mock data，结构比较复杂，需要这个结构多看看...
      datasetMock: [{
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
      }],
      // union data
      dataset: [],
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
      name: '',
      customType: ['db', 'sql', 'excel'],
      selectDsDialog: false,
      // 弹框临时选中的数据集
      tempDs: {},
      editUnion: false,
      unionParam: {}
    }
  },
  mounted() {
  },
  methods: {
    save() {

    },
    cancel() {
      if (this.param.tableId) {
        this.$emit('switchComponent', { name: 'ViewTable', param: this.param.table })
      } else {
        this.$emit('switchComponent', { name: '' })
      }
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
        this.calc({ type: 'delete', grandParentAdd: true, grandParentSub: true, subCount: 0 })
      }
    },
    confirmEditUnion() {
      // todo 校验关联关系与字段，必填
      this.editUnion = false
    },
    cancelUnion(val) {
      this.dataset = val
    }
  }
}
</script>

<style scoped>
.el-divider--horizontal {
  margin: 12px 0;
}
.union-container{
  display: flex;
  width:100%;
  height:400px;
  overflow: auto;
}
.form-item{
  margin-bottom: 10px!important;
}
.dialog-css >>> .el-dialog__body {
  padding: 0 20px;
}
</style>
