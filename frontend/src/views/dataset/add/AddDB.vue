<template>
  <el-row style="display: flex;flex-direction: column;height: 100%">
    <el-row style="height: 26px;" class="title-text">
      <span style="line-height: 26px;">
        {{ $t('dataset.add_db_table') }}
      </span>
      <el-row style="float: right">
        <el-button size="mini" @click="cancel">
          {{ $t('dataset.cancel') }}
        </el-button>
        <el-button size="mini" type="primary" :disabled="checkTableList.length < 1" @click="save">
          {{ $t('dataset.confirm') }}
        </el-button>
      </el-row>
    </el-row>
    <el-divider/>
    <el-row>
      <el-form :inline="true">
        <el-form-item class="form-item">
          <el-select v-model="dataSource" filterable :placeholder="$t('dataset.pls_slc_data_source')" size="mini">
            <el-option
              v-for="item in options"
              :key="item.id"
              :label="item.name"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item class="form-item">
          <el-select v-model="mode" filterable :placeholder="$t('dataset.connect_mode')" size="mini">
            <el-option :label="$t('dataset.direct_connect')" value="0"/>
            <el-option :label="$t('dataset.sync_data')" value="1"
                       :disabled="disabledSync"/>
          </el-select>
        </el-form-item>

        <el-form-item v-if="mode === '1'" class="form-item">
          <el-select v-model="syncType" filterable :placeholder="$t('dataset.connect_mode')" size="mini">
            <el-option :label="$t('dataset.sync_now')" value="sync_now"/>
            <el-option :label="$t('dataset.sync_latter')" value="sync_latter"/>
          </el-select>
        </el-form-item>

        <el-form-item class="form-item" style="float: right;">
          <el-input
            v-model="searchTable"
            size="mini"
            :placeholder="$t('dataset.search')"
            prefix-icon="el-icon-search"
            clearable
          />
        </el-form-item>
      </el-form>
    </el-row>
    <el-col style="overflow-y: auto;">
      <el-checkbox-group v-model="checkTableList" size="small">
        <el-tooltip v-for="t in tableData" :key="t.name" :disabled="t.enableCheck" effect="dark"
                    :content="$t('dataset.table_already_add_to')+': '+t.datasetPath" placement="bottom">
          <el-checkbox
            border
            :label="t.name"
            :disabled="!t.enableCheck"
          >{{ showTableNameWithComment(t) }}
          </el-checkbox>
        </el-tooltip>
      </el-checkbox-group>
    </el-col>
  </el-row>
</template>

<script>
import {listDatasource, post, isKettleRunning, disabledSyncDs} from '@/api/dataset/dataset'
import {engineMode} from "@/api/system/engine";

export default {
  name: 'AddDB',
  props: {
    param: {
      type: Object,
      default: null
    }
  },
  data() {
    return {
      searchTable: '',
      options: [],
      dataSource: '',
      tables: [],
      checkTableList: [],
      mode: '0',
      syncType: 'sync_now',
      tableData: [],
      kettleRunning: false,
      selectedDatasource: {},
      engineMode: 'local',
      disabledSync: true,
      disabledSyncDs: disabledSyncDs
    }
  },
  watch: {
    dataSource(val) {
      if (val) {
        post('/datasource/getTables/' + val, {}).then(response => {
          this.tables = response.data
          this.tableData = JSON.parse(JSON.stringify(this.tables))
        })
        for (let i = 0; i < this.options.length; i++) {
          if (this.options[i].id === val) {
            this.selectedDatasource = this.options[i]
            this.mode = '0'
            if (this.engineMode === 'simple' || (!this.kettleRunning || this.disabledSyncDs.indexOf(this.selectedDatasource.type) !== -1 )) {
              this.disabledSync = true
            } else {
              this.disabledSync = false
            }
          }
        }
      }
    },
    searchTable(val) {
      if (val && val !== '') {
        this.tableData = JSON.parse(JSON.stringify(this.tables.filter(ele => {
          return ele.name.toLocaleLowerCase().includes(val.toLocaleLowerCase())
        })))
      } else {
        this.tableData = JSON.parse(JSON.stringify(this.tables))
      }
    }
  },
  mounted() {
    this.initDataSource()
  },
  activated() {
    this.initDataSource()
  },
  created() {
    this.kettleState()
    engineMode().then(res => {
      this.engineMode = res.data
    })
  },
  methods: {
    initDataSource() {
      listDatasource().then(response => {
        this.options = response.data.filter(item => item.type !== 'api')
      })
    },
    kettleState() {
      isKettleRunning().then(res => {
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
      let ds = {}
      this.options.forEach(ele => {
        if (ele.id === this.dataSource) {
          ds = ele
        }
      })
      const sceneId = this.param.id
      const dataSourceId = this.dataSource
      const tables = []
      const mode = this.mode
      const syncType = this.syncType
      this.checkTableList.forEach(function (name) {
        tables.push({
          name: ds.name + '_' + name,
          sceneId: sceneId,
          dataSourceId: dataSourceId,
          type: 'db',
          syncType: syncType,
          mode: parseInt(mode),
          info: JSON.stringify({table: name})
        })
      })
      post('/dataset/table/batchAdd', tables).then(response => {
        this.$emit('saveSuccess', tables[0])
        this.cancel()
      })
    },

    cancel() {
      this.dataReset()
      this.$emit('switchComponent', {name: ''})
    },

    dataReset() {
      this.searchTable = ''
      this.options = []
      this.dataSource = ''
      this.tables = []
      this.checkTableList = []
    }
  }

}
</script>

<style scoped>
.el-divider--horizontal {
  margin: 12px 0;
}

.form-item {
  margin-bottom: 6px;
}

.el-checkbox {
  margin-bottom: 14px;
  margin-left: 0;
  margin-right: 14px;
}

.el-checkbox.is-bordered + .el-checkbox.is-bordered {
  margin-left: 0;
}

span {
  font-size: 14px;
}
</style>
