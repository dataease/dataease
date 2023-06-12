<template>
  <div class="de-dataset-form">
    <div class="top" v-loading="loading">
      <span class="name">
        <i
          class="el-icon-arrow-left"
          @click="logOutTips"
        />
        <svg-icon
          style="margin: 0 9.5px 0 16.2px"
          :icon-class="`de-${datasetType}-new`"
        />
        <template v-if="showInput">
          <el-input
            ref="editerName"
            v-model="table.name"
            @blur="nameBlur"
          />
          <div
            v-if="nameExist"
            style="left: 55px"
            class="el-form-item__error"
          >
            {{ $t('deDataset.already_exists') }}
          </div>
        </template>
        <template v-else>
          <span>{{ datasetName }}</span>
          <i
            v-if="['sql', 'union'].includes(datasetType)"
            style="margin-left: 5px"
            class="el-icon-edit"
            @click="handleClick"
          />
        </template>
      </span>
      <span class="oprate">
        <span
          v-if="['db', 'excel', 'api'].includes(datasetType)"
          class="table-num"
        >{{ $t('deDataset.selected') }} {{ tableNum }}
          {{ ['excel'].includes(datasetType) ? $t('deDataset.table') : $t('deDataset.item') }}</span>
        <deBtn
          :disabled="['db', 'excel', 'api', 'union'].includes(datasetType) && !tableNum"
          type="primary"
          @click="datasetSave"
        >{{
          $t('commons.save')
        }}</deBtn>
      </span>
    </div>
    <div class="container">
      <component
        :is="component"
        ref="addDataset"
        :param="table"
        :origin-name="originName"
        :name-list="nameList"
        @setTableNum="(val) => (tableNum = val)"
        @datasourceLoading="(val) => loading = val"
      />
    </div>
  </div>
</template>

<script>
import AddDB from './add/AddDB'
import AddApi from './add/AddApi'
import AddSQL from './add/AddSQL'
import AddExcel from './add/AddExcel'
import AddUnion from '@/views/dataset/add/AddUnion'
import { post } from '@/api/dataset/dataset'
import { datasetTypeMap } from './group/options'
import msgCfm from '@/components/msgCfm/index'
export default {
  name: 'DatasetForm',
  components: { AddDB, AddSQL, AddExcel, AddApi, AddUnion },
  mixins: [msgCfm],
  data() {
    return {
      originName: '',
      tableNum: 0,
      loading: false,
      showInput: false,
      editType: '',
      selectDatasets: [],
      tData: [],
      datasetType: '',
      component: '',
      table: {},
      nameExist: false,
      nameList: [],
      datasetForm: {
        id: '',
        name: ''
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
    const fromGroup = this.$route.params.fromGroup
    const routeInfo = fromGroup ? this.$route.params : this.$route.query
    const { datasetType, sceneId, id, editType, name: label } = routeInfo
    this.datasetType = datasetType
    this.editType = editType
    if (id) {
      this.initTable(id)
    } else {
      const name = label || this.$t('commons.create') + this.$t(datasetTypeMap[datasetType]) + this.$t('auth.datasetAuth')
      this.table = {
        name,
        id: sceneId
      }
      this.getDatasetNameFromGroup(sceneId, name)
    }
    this.switchComponent(datasetType)
  },
  methods: {
    back() {
      this.$router.push('/dataset/index')
    },
    logOutTips() {
      const options = {
        title: 'role.tips',
        confirmButtonText: this.$t('commons.confirm'),
        content: 'system_parameter_setting.sure_to_exit',
        type: 'primary',
        cb: () => {
          this.back()
        }
      }
      this.handlerConfirm(options)
    },
    nameBlur() {
      this.nameExistValidator()
      this.showInput = this.nameExist
    },
    getDatasetNameFromGroup(sceneId, name) {
      post(`/dataset/table/getDatasetNameFromGroup/${sceneId}`, null).then(
        (res) => {
          this.nameList = res.data
          if (name && ['sql', 'union'].includes(this.datasetType)) {
            this.nameBlur()
          }
        }
      )
    },
    datasetSave() {
      if (['sql', 'union'].includes(this.datasetType)) {
        this.nameExistValidator()
        if (this.nameExist) {
          return
        }
      }
      this.$refs.addDataset.save()
    },
    handleClick() {
      if (['sql', 'union'].includes(this.datasetType)) {
        this.showInput = true
        this.$nextTick(() => {
          this.$refs.editerName.focus()
        })
      }
    },
    nameRepeat(value) {
      if (!this.nameList || this.nameList.length === 0) {
        return false
      }
      return this.nameList.some((name) => name === value)
    },
    nameValidator(rule, value, callback) {
      if (this.nameRepeat(value)) {
        callback(new Error(this.$t('deDataset.already_Exists')))
      } else {
        callback()
      }
    },
    nameExistValidator() {
      if (!this.nameList || this.nameList.length === 0) {
        this.nameExist = false
        return
      }
      this.nameExist = this.nameList.some(
        (name) => name === this.table.name && name !== this.originName
      )
    },
    initTable(id) {
      this.loading = true
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
        .catch(() => { })
        .finally(() => {
          this.loading = false
        })
    },
    switchComponent(c) {
      switch (c) {
        case 'db':
          this.component = AddDB
          break
        case 'sql':
          this.component = AddSQL
          break
        case 'excel':
          this.component = AddExcel
          break
        case 'union':
          this.component = AddUnion
          break
        case 'api':
          this.component = AddApi
          break
        default:
          break
      }
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
