<template>
  <el-dialog
    v-loading="loading"
    :title="dialogTitle"
    class="de-dialog-form"
    :visible.sync="createDataset"
    width="600px"
    :before-close="resetForm"
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
        <el-input v-model="datasetForm.name"/>
      </el-form-item>
      <el-form-item
        :label="$t('deDataset.folder')"
        prop="id"
      >
        <el-popover
          placement="bottom"
          popper-class="user-popper dataset-filed"
          width="552"
          trigger="click"
        >
          <el-tree
            ref="tree"
            :data="tData"
            node-key="id"
            class="de-tree"
            :expand-on-click-node="false"
            highlight-current
            :filter-node-method="filterNode"
            @node-click="nodeClick"
          >
            <span
              slot-scope="{ data }"
              class="custom-tree-node-dataset"
            >
              <span v-if="data.modelInnerType === 'group'">
                <svg-icon icon-class="scene"/>
              </span>
              <span
                style="
                  margin-left: 6px;
                  white-space: nowrap;
                  overflow: hidden;
                  text-overflow: ellipsis;
                "
                :title="data.name"
              >{{ data.name }}</span>
            </span>
          </el-tree>
          <el-select
            slot="reference"
            v-model="datasetForm.id"
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
    <div
      slot="footer"
      class="dialog-footer"
    >
      <deBtn
        secondary
        @click="resetForm"
      >{{ $t('dataset.cancel') }}
      </deBtn>
      <deBtn
        type="primary"
        @click="saveDataset"
      >{{ $t('dataset.confirm') }}
      </deBtn>
    </div>
  </el-dialog>
</template>

<script>
import { post } from '@/api/dataset/dataset'
import { datasetTypeMap } from './options'
import { formatDatasetTreeFolder, getCacheTree } from '@/components/canvas/utils/utils'

export default {
  data() {
    return {
      selectDatasets: [],
      createDataset: false,
      tData: [],
      loading: false,
      nameList: [],
      datasetForm: {
        id: '',
        name: ''
      },
      datasetType: '',
      dialogTitle: '',
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
        ],
        id: [
          {
            required: true,
            message: this.$t('fu.search_bar.please_select'),
            trigger: 'blur'
          }
        ]
      }
    }
  },
  methods: {
    saveDataset() {
      this.$refs.datasetForm.validate((result) => {
        if (result) {
          const { name, id } = this.datasetForm
          this.createDataset = false
          this.$router.push({
            name: 'dataset-form',
            params: {
              datasetType: this.datasetType,
              sceneId: id,
              fromGroup: true,
              name
            }
          })
        }
      })
    },
    resetForm() {
      this.$refs.datasetForm.clearValidate()
      this.datasetForm = {
        id: '',
        name: ''
      }
      this.createDataset = false
    },
    init(type) {
      this.tree()
      if (['db', 'excel', 'api'].includes(type)) {
        this.$delete(this.datasetFormRules, 'name')
      }
      this.datasetType = type
      this.createDataset = true
      this.dialogTitle =
        this.$t('commons.create') +
        this.$t(datasetTypeMap[type]) +
        this.$t('auth.datasetAuth')
    },
    filterNode(value, data) {
      if (!value) return true
      return data.name.indexOf(value) !== -1
    },
    filterMethod(val) {
      if (!val) this.$refs.tree.filter(val)
      this.$refs.tree.filter(val)
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
    getDatasetNameFromGroup(sceneId) {
      post(`/dataset/table/getDatasetNameFromGroup/${sceneId}`, null).then(
        (res) => {
          this.nameList = res.data
        }
      )
    },
    tree() {
      this.tData = getCacheTree('dataset-tree')
      formatDatasetTreeFolder(this.tData)
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
    }
  }
}
</script>

<style lang="scss">
.dataset-filed {
  height: 400px;
  overflow-y: auto;
}

.tree-select-dataset {
  display: none;
}
</style>
