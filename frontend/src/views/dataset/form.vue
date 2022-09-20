<template>
  <div class="de-dataset-form">
    <div class="top" v-if="table.id && !createDataset">
      <span class="name">
        <i @click="back" class="el-icon-arrow-left"></i>
        <svg-icon
          style="margin: 0 9.5px 0 16.2px"
          :icon-class="`de-${datasetType}-new`"
        />
        <template v-if="showInput">
          <el-input @blur="nameBlur" v-model="table.name"></el-input>
          <div v-if="nameExsit" style="left: 55px" class="el-form-item__error">
            {{ $t('deDataset.already_exists') }}
          </div>
        </template>
        <span
          :class="[{ 'show-point': ['sql', 'union'].includes(datasetType) }]"
          v-else
          @click="handleClick"
          >{{ datasetName }}</span
        >
      </span>
      <span class="oprate">
        <span
          class="table-num"
          v-if="['db', 'excel', 'api'].includes(datasetType)"
          >{{ $t('deDataset.selected') }} {{ tableNum }}
          {{ $t('deDataset.table') }}</span
        >
        <deBtn @click="datasetSave" type="primary">{{
          $t('commons.save')
        }}</deBtn>
      </span>
    </div>
    <div class="container">
      <component
        @setTableNum="(val) => (tableNum = val)"
        v-if="table.name || !createDataset"
        :param="table"
        :is="component"
        ref="addDataset"
        :nameList="nameList"
      />
    </div>

    <el-dialog
      :title="dialogTitle"
      class="de-dialog-form"
      :visible.sync="createDataset"
      width="600px"
      v-loading="loading"
      :before-close="beforeClose"
    >
      <el-form
        ref="datasetForm"
        class="de-form-item"
        :model="datasetForm"
        :rules="datasetFormRules"
      >
        <el-form-item
          v-if="datasetFormRules.name"
          :label="$t('dataset.name')"
          prop="name"
        >
          <el-input v-model="datasetForm.name" />
        </el-form-item>
        <el-form-item :label="$t('deDataset.folder')" prop="id">
          <el-popover
            placement="bottom"
            popper-class="user-popper dataset-filed"
            width="552"
            trigger="click"
          >
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
              <span slot-scope="{ data }" class="custom-tree-node-dataset">
                <span v-if="data.type === 'group'">
                  <svg-icon icon-class="scene" class="ds-icon-scene" />
                </span>
                <span
                  style="
                    margin-left: 6px;
                    white-space: nowrap;
                    overflow: hidden;
                    text-overflow: ellipsis;
                  "
                  :title="data.name"
                  >{{ data.name }}</span
                >
              </span>
            </el-tree>
            <el-select
              v-model="datasetForm.id"
              slot="reference"
              filterable
              popper-class="tree-select-dataset"
              style="width: 100%"
              :filter-method="filterMethod"
              :placeholder="$t('commons.please_select')"
            >
              <el-option
                v-for="item in selectDatasets"
                :key="item.label"
                :label="item.label"
                :value="item.id"
              />
            </el-select>
          </el-popover>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <deBtn secondary @click="close">{{ $t('dataset.cancel') }}</deBtn>
        <deBtn type="primary" @click="saveDataset"
          >{{ $t('dataset.confirm') }}
        </deBtn>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import AddDB from './add/AddDB'
import AddApi from './add/AddApi'
import AddSQL from './add/AddSQL'
import AddExcel from './add/AddExcel'
import AddUnion from '@/views/dataset/add/AddUnion'
import { post } from '@/api/dataset/dataset'
import { groupTree } from '@/api/dataset/dataset'
export default {
  name: 'DatasetForm',
  components: { AddDB, AddSQL, AddExcel, AddApi, AddUnion },
  data() {
    return {
      sceneId: '',
      originName: '',
      tableNum: 0,
      showInput: false,
      editType: '',
      loading: false,
      selectDatasets: [],
      tData: [],
      filterText: '',
      dialogTitle: '',
      createDataset: false,
      datasetType: '',
      component: '',
      table: {},
      nameExsit: false,
      nameList: [],
      datasetForm: {
        id: '',
        name: '',
        sceneName: ''
      },
      datasetFormRules: {
        name: [
          {
            required: true,
            message: this.$t('commons.input_content'),
            trigger: 'change'
          },
          {
            max: 50,
            message: this.$t('commons.char_can_not_more_50'),
            trigger: 'change'
          },
          { required: true, trigger: 'blur', validator: this.nameValidator }
        ]
      }
    }
  },
  computed: {
    datasetName() {
      if (+this.editType === 0) {
        return this.$t('dataset.excel_replace') + this.$t('chart.chart_data')
      }

      if (+this.editType === 1) {
        return this.$t('dataset.excel_add') + this.$t('chart.chart_data')
      }
      return this.table.name || this.dialogTitle
    }
  },
  created() {
    const { datasetType, sceneId, id, editType } = this.$route.query
    this.datasetType = datasetType
    this.editType = editType
    this.sceneId = sceneId
    if (id) {
      this.initTable(id)
    } else {
      this.tree(sceneId)
      this.createDataset = true
    }
    this.switchComponent(datasetType)
  },
  methods: {
    back() {
      this.$router.back()
    },
    nameBlur() {
      this.nameExsitValidator()
      this.showInput = this.nameExsit
    },
    getDatasetNameFromGroup(sceneId) {
      post(`/dataset/table/getDatasetNameFromGroup/${sceneId}`, null).then(
        (res) => {
          this.nameList = res.data
        }
      )
    },
    datasetSave() {
      if (['sql', 'union'].includes(this.datasetType)) {
        this.nameExsitValidator()
        if (this.nameExsit) {
          return
        }
      }
      this.$refs.addDataset.save()
    },
    handleClick() {
      if (['sql', 'union'].includes(this.datasetType)) {
        this.showInput = true
      }
    },
    nodeClick({ id, label }) {
      this.selectDatasets = [
        {
          id,
          label
        }
      ]
      this.$nextTick(() => {
        this.datasetForm.id = id
      })
      this.getDatasetNameFromGroup(id)
    },
    tree(sceneId) {
      this.loading = true
      groupTree({
        name: '',
        pid: '0',
        level: 0,
        type: 'group',
        children: [],
        sort: 'type desc,name asc'
      }).then((res) => {
        this.tData = res.data
        if (sceneId) {
          this.dfsTree(res.data, sceneId)
        }
        this.loading = false
      })
    },
    dfsTree(arr, sceneId) {
      arr.some((ele) => {
        if (sceneId === ele.id) {
          this.nodeClick(ele)
        } else if (ele.children?.length) {
          this.dfsTree(ele.children, sceneId)
        }
        return false
      })
    },
    filterMethod(val) {
      if (!val) this.$refs.tree.filter(val)
      this.$refs.tree.filter(val)
    },
    filterNode(value, data) {
      if (!value) return true
      return data.name.indexOf(value) !== -1
    },
    beforeClose() {
      this.close()
    },
    nameRepeat(value) {
      if (!this.nameList || this.nameList.length === 0) {
        return false
      }
      return this.nameList.some((name) => name === value)
    },
    nameValidator(rule, value, callback) {
      if (this.nameRepeat(value)) {
        callback(new Error(this.$t('deDataset.already_exists')))
      } else {
        callback()
      }
    },
    nameExsitValidator() {
      if (!this.nameList || this.nameList.length === 0) {
        this.nameExsit = false
        return
      }
      this.nameExsit = this.nameList.some(
        (name) => name === this.table.name && name !== this.originName
      )
    },
    close() {
      this.$router.back()
    },
    saveDataset() {
      this.$refs.datasetForm.validate((result) => {
        if (result) {
          const { name, id } = this.datasetForm
          this.table = {
            id,
            name
          }
          this.createDataset = false
          this.getDatasetNameFromGroup(id)
        }
      })
    },
    initTable(id) {
      post('/dataset/table/getWithPermission/' + id, null)
        .then((response) => {
          const { sceneId: id, id: tableId, name } = response.data || {}
          this.table = {
            id,
            tableId,
            table: response.data,
            name
          }
          this.getDatasetNameFromGroup(id)
          this.originName = name
          if (this.datasetType === 'excel') {
            this.table.editType = +this.editType
          }
        })
        .catch(() => {})
    },
    switchComponent(c) {
      let type = ''
      if (['db', 'excel', 'api'].includes(c)) {
        this.datasetFormRules = {}
      }
      switch (c) {
        case 'db':
          type = 'deDataset.database'
          this.component = AddDB
          break
        case 'sql':
          type = 'SQL'
          this.component = AddSQL
          break
        case 'excel':
          type = 'EXCEL'
          this.component = AddExcel
          break
        case 'union':
          type = 'dataset.union'
          this.component = AddUnion
          break
        case 'api':
          type = 'API'
          this.component = AddApi
          break
        default:
          break
      }
      this.dialogTitle =
        this.$t('commons.create') + this.$t(type) + this.$t('auth.datasetAuth')
    }
  }
}
</script>

<style lang="scss">
.de-dataset-form {
  .top {
    height: 56px;
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 0 24px;
    box-shadow: 0 2px 2px 0 rgb(0 0 0 / 10%);

    .show-point {
      cursor: pointer;
    }

    .name {
      font-family: PingFang SC;
      font-size: 16px;
      font-weight: 500;
      display: flex;
      align-items: center;
      width: 50%;
      position: relative;

      .el-input {
        min-width: 96px;
        .el-input__inner {
          line-height: 24px;
          height: 24px;
        }
      }
      i {
        cursor: pointer;
      }
    }
    .oprate {
      .table-num {
        font-family: PingFang SC;
        font-size: 14px;
        font-weight: 400;
        margin-right: 16px;
        color: var(--deTextPrimary, #1f2329);
      }
    }
  }

  .container {
    width: 100%;
    height: calc(100vh - 56px);
  }
}

.dataset-filed {
  height: 400px;
  overflow-y: auto;
}

.tree-select-dataset {
  display: none;
}
</style>
