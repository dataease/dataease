<template>
  <div
    v-loading="loading"
    class="dataset-api"
    @mouseup="mouseupDrag"
  >
    <p
      v-show="!showLeft"
      class="arrow-right"
      @click="showLeft = true"
    >
      <i class="el-icon-d-arrow-right" />
    </p>
    <div
      v-show="showLeft"
      :style="{ left: LeftWidth + 'px' }"
      class="drag-left"
      @mousedown="mousedownDrag"
    />
    <div
      v-show="showLeft"
      :style="{ width: LeftWidth + 'px' }"
      class="table-list"
    >
      <p class="select-ds">
        {{ $t('deDataset.select_data_source') }}
        <i
          class="el-icon-d-arrow-left"
          @click="showLeft = false"
        />
      </p>
      <el-select
        v-model="dataSource"
        class="ds-list"
        popper-class="db-multiple-select-pop"
        filterable
        :placeholder="$t('dataset.pls_slc_data_source')"
        size="small"
      >
        <el-option
          v-for="item in options"
          :key="item.id"
          :label="item.name"
          :value="item.id"
        />
      </el-select>
      <p class="select-ds">{{ $t('datasource.data_table') }}</p>
      <el-input
        v-model="searchTable"
        size="small"
        class="search"
        :placeholder="$t('deDataset.by_table_name')"
        prefix-icon="el-icon-search"
        clearable
      />
      <div
        v-if="!tableData.length && searchTable !== ''"
        class="el-empty"
      >
        <div
          class="el-empty__description"
          style="margin-top: 80px;color: #5e6d82;"
        >
          没有找到相关内容
        </div>
      </div>
      <div
        v-else
        v-loading="dsLoading"
        class="table-checkbox-list"
      >
        <el-checkbox-group
          v-model="checkTableList"
          size="small"
        >
          <div
            v-for="t in tableData"
            :key="t.name"
            :class="[
              { active: activeName === t.name, 'not-allow': !t.enableCheck }
            ]"
            class="item"
            :title="t.name"
            @click="setActiveName(t)"
          >
            <svg-icon
              v-if="!t.enableCheck"
              icon-class="Checkbox"
              style="margin-right: 8px"
            />
            <el-checkbox
              v-else
              :label="t.name"
            />
            <span class="label">{{ showTableNameWithComment(t) }}</span>
            <span
              v-if="t.nameExist"
              class="error-name-exist"
            >
              <svg-icon
                icon-class="exclamationmark"
                class="ds-icon-scene"
              />
            </span>
          </div>
        </el-checkbox-group>
      </div>
    </div>
    <div class="table-detail">
      <div class="top-table-detail">
        <el-select
          v-model="mode"
          filterable
          :placeholder="$t('dataset.connect_mode')"
          size="small"
        >
          <el-option
            :label="$t('dataset.sync_data')"
            value="1"
            :disabled="!kettleRunning && engineMode !== 'simple'"
          />
        </el-select>
        <el-select
          v-model="syncType"
          filterable
          :placeholder="$t('dataset.connect_mode')"
          size="small"
        >
          <el-option
            :label="$t('dataset.sync_now')"
            value="sync_now"
          />
          <el-option
            :label="$t('dataset.sync_latter')"
            value="sync_latter"
          />
        </el-select>
      </div>
      <el-empty
        v-if="!dataSource"
        style="padding-top: 160px"
        size="125"
        :description="$t('dataset.pls_slc_data_source')"
        :image="noSelectTable"
      />
      <template v-else-if="activeName">
        <div class="dataset">
          <span class="name">{{ $t('dataset.name') }}</span>
          <el-input
            v-model="activeTable.datasetName"
            size="small"
            clearable
            @change="validateName"
          />
          <div
            v-if="activeTable.nameExist"
            style="left: 107px; top: 52px"
            class="el-form-item__error"
          >
            {{ $t('deDataset.already_exists') }}
          </div>
        </div>
        <div
          v-loading="tableLoading"
          class="data"
        >
          <span class="result-num">{{
            `${$t('dataset.preview_show')} 1000 ${$t('dataset.preview_item')}`
          }}</span>
          <div class="table-grid">
            <ux-grid
              ref="plxTable"
              size="mini"
              style="width: 100%"
              :height="height"
              :checkbox-config="{ highlight: true }"
              :width-resize="true"
            >
              <ux-table-column
                v-for="field in fields"
                :key="field.fieldName"
                min-width="200px"
                :field="field.fieldName"
                :title="field.remarks"
                :resizable="true"
              />
            </ux-grid>
          </div>
        </div>
      </template>
      <el-empty
        v-else-if="avilibelTable"
        style="padding-top: 160px"
        size="125"
        :description="$t('deDataset.is_currently_available')"
        :image="noAvilibelTableImg"
      />
      <el-empty
        v-else-if="!activeName"
        style="padding-top: 160px"
        size="125"
        :description="$t('deDataset.left_to_edit')"
        :image="noSelectTable"
      />
    </div>
  </div>
</template>

<script>
import { isKettleRunning, listApiDatasource, post } from '@/api/dataset/dataset'
import { dbPreview, engineMode } from '@/api/system/engine'
import cancelMix from './cancelMix'
import msgCfm from '@/components/msgCfm/index'
import { pySort } from './util'
import { updateCacheTree } from '@/components/canvas/utils/utils'

export default {
  name: 'AddApi',
  mixins: [cancelMix, msgCfm],
  props: {
    param: {
      type: Object,
      default: null
    },
    nameList: {
      type: Array,
      default: () => []
    }
  },
  data() {
    return {
      showLeft: true,
      tableLoading: false,
      loading: false,
      LeftWidth: 240,
      height: 400,
      fields: [],
      fieldsData: [],
      searchTable: '',
      options: [],
      dataSource: '',
      dsLoading: false,
      tables: [],
      checkTableList: [],
      mode: '1',
      syncType: 'sync_now',
      tableData: [],
      kettleRunning: false,
      engineMode: 'local',
      selectedDatasource: {},
      disabledSync: true,
      avilibelTable: false,
      noAvilibelTableImg: require('@/assets/None.png'),
      noSelectTable: require('@/assets/None_Select_ds.png'),
      activeTable: {},
      activeName: ''
    }
  },
  computed: {
    checkDatasetName() {
      return this.tables
        .filter((ele, index) => {
          return this.checkTableList.includes(ele.name)
        })
        .map((ele) => ele.datasetName)
    }
  },
  watch: {
    checkTableList(val) {
      this.validateName()
      this.$emit('setTableNum', val.length)
    },
    dataSource(val) {
      if (val) {
        this.checkTableList = []
        this.activeName = ''
        this.dsLoading = true
        const dsName = this.options.find((ele) => ele.id === val).name
        post('/datasource/getTables/' + val, {}).then((response) => {
          this.tables = response.data
          this.tables.forEach((ele) => {
            this.$set(ele, 'datasetName', dsName + '_' + ele.name)
            this.$set(ele, 'nameExist', false)
          })
          this.tableData = [...this.tables]
          this.avilibelTable = !this.tableData.some((ele) => ele.enableCheck)
        }).finally(() => {
          this.dsLoading = false
        })
        for (let i = 0; i < this.options.length; i++) {
          if (this.options[i].id === val) {
            this.selectedDatasource = this.options[i]
          }
        }
      }
    },
    searchTable(val) {
      this.activeName = ''
      if (val && val !== '') {
        this.tableData = [...this.tables.filter((ele) => {
          return ele.name
            .toLocaleLowerCase()
            .includes(val.toLocaleLowerCase())
        })]
      } else {
        this.tableData = [...this.tables]
      }
    }
  },
  beforeDestroy() {
    window.removeEventListener('resize', this.calHeight)
  },
  mounted() {
    this.initDataSource()
    window.addEventListener('resize', this.calHeight)
    this.calHeight()
    this.$emit('setSaveDisabled', false)
  },
  activated() {
    this.initDataSource()
  },
  created() {
    this.kettleState()
    engineMode().then((res) => {
      this.engineMode = res.data
    })
  },
  methods: {
    mousedownDrag() {
      document
        .querySelector('.dataset-api')
        .addEventListener('mousemove', this.calculateHeight)
    },
    mouseupDrag() {
      document
        .querySelector('.dataset-api')
        .removeEventListener('mousemove', this.calculateHeight)
    },
    calculateHeight(e) {
      if (e.pageX < 240) {
        this.LeftWidth = 240
        return
      }
      if (e.pageX > 500) {
        this.LeftWidth = 500
        return
      }
      this.LeftWidth = e.pageX
    },
    nameExistValidator(ele) {
      ele.nameExist =
        this.nameList
          .concat(this.checkDatasetName)
          .filter((name) => name === ele.datasetName)
          .length > 1
    },
    validateName() {
      this.tables.forEach((ele, index) => {
        if (this.checkTableList.includes(ele.name)) {
          this.nameExistValidator(ele)
        } else {
          ele.nameExist = false
        }
      })
    },
    calHeight() {
      const that = this
      setTimeout(function() {
        const currentHeight = document.documentElement.clientHeight
        that.height = currentHeight - 56 - 64 - 75 - 32 - 24 - 16 - 10
      }, 10)
    },
    setActiveName({ name, datasourceId, enableCheck }) {
      if (!enableCheck) return
      this.activeName = name
      this.activeTable = this.tableData.find((ele) => ele.name === this.activeName) || {}
      this.dbPreview({
        dataSourceId: datasourceId,
        info: JSON.stringify({ table: name })
      })
    },
    dbPreview(data) {
      this.tableLoading = true
      dbPreview(data)
        .then((res) => {
          const { fields, data } = res.data
          this.fields = fields
          this.fieldsData = data
          this.$refs.plxTable.reloadData(data)
        })
        .finally(() => {
          this.tableLoading = false
        })
    },
    initDataSource() {
      listApiDatasource().then((response) => {
        this.options = pySort(response.data)
      })
    },
    kettleState() {
      isKettleRunning().then((res) => {
        this.kettleRunning = res.data
      })
    },
    showTableNameWithComment(t) {
      if (t.remark) {
        return `${t.name}(${t.remark})`
      } else {
        return `${t.name}`
      }
    },
    save() {
      if (this.tableData.some((ele) => ele.nameExist)) {
        this.openMessageSuccess('deDataset.cannot_be_duplicate', 'error')
        return
      }
      if (this.loading) return
      this.loading = true
      const sceneId = this.param.id
      const dataSourceId = this.dataSource
      const tables = []
      const mode = this.mode
      const syncType = this.syncType

      for (let i = 0; i < this.checkTableList.length; i++) {
        const table = this.tables.find(
            (ele) => ele.name === this.checkTableList[i]
        )
        if(table.setKey && table.keys.length === 0 ){
          this.openMessageSuccess(this.checkTableList[i] + this.$t('dataset.no_set_key')  , 'error')
          return
        }
        tables.push({
          name: table.datasetName,
          sceneId: sceneId,
          dataSourceId: dataSourceId,
          type: 'api',
          syncType: syncType,
          mode: parseInt(mode),
          info: JSON.stringify({ table: this.checkTableList[i], setKey: table.setKey, keys: table.keys})
        })
      }
      post('/dataset/table/batchAdd', tables)
        .then((response) => {
          this.openMessageSuccess('deDataset.set_saved_successfully')
          updateCacheTree('batchNew', 'dataset-tree', response.data, JSON.parse(localStorage.getItem('dataset-tree')))
          this.cancel(response.data)
        })
        .finally(() => {
          this.loading = false
        })
    }
  }
}
</script>

<style scoped lang="scss">
.dataset-api {
  display: flex;
  height: 100%;
  position: relative;
  width: 100%;

  .drag-left {
    position: absolute;
    height: calc(100vh - 56px);
    width: 2px;
    top: 0;
    z-index: 5;
    cursor: col-resize;
  }

  .arrow-right {
    position: absolute;
    z-index: 2;
    top: 15px;
    cursor: pointer;
    margin: 0;
    display: flex;
    align-items: center;
    justify-content: center;
    left: 0;
    height: 24px;
    width: 20px;
    box-shadow: 0px 4px 8px rgba(0, 0, 0, 0.1);
    border: 1px solid var(--deCardStrokeColor, #dee0e3);
    border-top-right-radius: 13px;
    border-bottom-right-radius: 13px;
  }

  .table-list {
    p {
      margin: 0;
    }

    height: 100%;
    width: 240px;
    padding: 16px 12px;
    font-family: AlibabaPuHuiTi;
    border-right: 1px solid rgba(31, 35, 41, 0.15);

    .select-ds {
      font-size: 14px;
      font-weight: 500;
      display: flex;
      justify-content: space-between;
      color: var(--deTextPrimary, #1f2329);

      i {
        cursor: pointer;
        font-size: 12px;
        color: var(--deTextPlaceholder, #8f959e);
      }
    }

    .search {
      margin: 12px 0;
    }

    .ds-list {
      margin: 12px 0 24px 0;
      width: 100%;
    }

    .table-checkbox-list {
      height: calc(100% - 190px);
      overflow-y: auto;

      .item {
        height: 40px;
        width: 100%;
        border-radius: 4px;
        display: flex;
        align-items: center;
        box-sizing: border-box;
        padding: 12px;
        font-family: AlibabaPuHuiTi;
        font-size: 14px;
        font-weight: 400;
        color: var(--deTextPrimary, #1f2329);
        position: relative;

        &:hover {
          background: rgba(31, 35, 41, 0.1);
        }

        &.active {
          background-color: var(--deWhiteHover, #3370ff);
          color: var(--primary, #3370ff);
        }

        ::v-deep.el-checkbox__label {
          display: none;
        }

        ::v-deep.el-checkbox {
          margin-right: 8px;
        }

        .label {
          width: 85%;
          overflow: hidden;
          text-overflow: ellipsis;
          white-space: nowrap;
        }

        .error-name-exist {
          position: absolute;
          top: 10px;
          right: 10px;
        }
      }

      .not-allow {
        cursor: not-allowed;
        color: var(--deTextDisable, #bbbfc4);
      }
    }
  }

  .table-detail {
    font-family: AlibabaPuHuiTi;
    flex: 1;
    overflow: hidden;

    .top-table-detail {
      height: 64px;
      width: 100%;
      padding: 16px 24px;
      background: #f5f6f7;
      display: flex;
      align-items: center;

      .el-select {
        width: 120px;
        margin-right: 12px;
      }
    }

    .dataset {
      padding: 21px 24px;
      width: 100%;
      border-bottom: 1px solid rgba(31, 35, 41, 0.15);
      display: flex;
      align-items: center;
      position: relative;

      .name {
        font-size: 14px;
        font-weight: 400;
        color: var(--deTextPrimary, #1f2329);
      }

      .el-input {
        width: 420px;
        margin-left: 12px;
      }

      .el-checkbox{
        margin-left: 12px;
      }
    }

    .data {
      padding: 16px 25px;
      overflow: auto;
      height: calc(100% - 140px);
      width: 100%;
      box-sizing: border-box;

      .result-num {
        font-weight: 400;
        display: inline-block;
        font-family: AlibabaPuHuiTi;
        color: var(--deTextSecondary, #646a73);
        margin-bottom: 16px;
      }

      .table-grid {
        width: 100%;
      }
    }
  }
}
</style>
