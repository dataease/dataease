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
        <node-item :current-node="dataset[0]" :node-index="0" @deleteNode="deleteNode" @notifyParent="calc" />
        <div v-if="dataset.length > 0">
          <union-node v-for="(item,index) in dataset[0].childrenDs" :key="index" :node-index="index" :children-node="item" :children-list="dataset[0].childrenDs" @notifyParent="calc" />
        </div>
      </div>
    </div>

    <!--选择数据集-->
    <el-dialog v-dialogDrag :title="$t('chart.select_dataset')" :visible="selectDsDialog" :show-close="false" width="30%" class="dialog-css" destroy-on-close>
      <dataset-group-selector-tree :fix-height="true" show-mode="union" :custom-type="customType" @getTable="firstDs" />
      <div slot="footer" class="dialog-footer">
        <el-button size="mini" @click="closeSelectDs()">{{ $t('dataset.cancel') }}</el-button>
        <el-button type="primary" size="mini" @click="confirmSelectDs()">{{ $t('dataset.confirm') }}</el-button>
      </div>
    </el-dialog>

  </el-row>
</template>

<script>
import UnionNode from '@/views/dataset/add/union/UnionNode'
import NodeItem from '@/views/dataset/add/union/NodeItem'
import DatasetGroupSelectorTree from '@/views/dataset/common/DatasetGroupSelectorTree'
export default {
  name: 'AddUnion',
  components: { DatasetGroupSelectorTree, NodeItem, UnionNode },
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
              unionType: '', // left join,right join,inner join
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
          unionType: '',
          unionFields: []
        },
        allChildCount: 0
      },
      name: '',
      customType: ['db', 'sql', 'excel'],
      selectDsDialog: false,
      // 弹框临时选中的数据集
      tempDs: {}
    }
  },
  mounted() {
  },
  methods: {
    save() {

    },
    cancel() {

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
