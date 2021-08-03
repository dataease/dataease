<template>
  <el-row style="display: flex;flex-direction: column;height: 100%">
    <el-row style="height: 26px;">
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
    <el-divider />
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
            <el-option :label="$t('dataset.direct_connect')" value="0" />
            <el-option :label="$t('dataset.sync_data')" value="1" :disabled="!kettleRunning" />
          </el-select>
        </el-form-item>

        <el-form-item v-if="mode === '1'" class="form-item">
          <el-select v-model="syncType" filterable :placeholder="$t('dataset.connect_mode')" size="mini">
            <el-option :label="$t('dataset.sync_now')" value="sync_now" />
            <el-option :label="$t('dataset.sync_latter')" value="sync_latter" />
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
        <el-tooltip v-for="t in tableData" :key="t.name" :disabled="t.enableCheck" effect="dark" :content="$t('dataset.table_already_add_to')+': '+t.datasetPath" placement="bottom">
          <el-checkbox
            border
            :label="t.name"
            :disabled="!t.enableCheck"
          />
        </el-tooltip>
      </el-checkbox-group>
    </el-col>
  </el-row>
</template>

<script>
import { listDatasource, post, isKettleRunning } from '@/api/dataset/dataset'

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
      kettleRunning: false
    }
  },
  watch: {
    dataSource(val) {
      if (val) {
        post('/datasource/getTables', { id: val }).then(response => {
          this.tables = response.data
          this.tableData = JSON.parse(JSON.stringify(this.tables))
        })
      }
    },
    searchTable(val) {
      if (val && val !== '') {
        this.tableData = JSON.parse(JSON.stringify(this.tables.filter(ele => { return ele.name.toLocaleLowerCase().includes(val.toLocaleLowerCase()) })))
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
  },
  methods: {
    initDataSource() {
      listDatasource().then(response => {
        this.options = response.data
      })
    },
    kettleState() {
      isKettleRunning().then(res => {
        this.kettleRunning = res.data
      })
    },
    save() {
      // console.log(this.checkTableList);
      // console.log(this.scene);
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
      this.checkTableList.forEach(function(name) {
        tables.push({
          name: ds.name + '_' + name,
          sceneId: sceneId,
          dataSourceId: dataSourceId,
          type: 'db',
          syncType: syncType,
          mode: parseInt(mode),
          info: JSON.stringify({ table: name })
        })
      })
      post('/dataset/table/batchAdd', tables).then(response => {
        // this.$store.dispatch('dataset/setSceneData', new Date().getTime())
        this.$emit('saveSuccess', tables[0])
        this.cancel()
      })
    },

    cancel() {
      this.dataReset()
      // this.$router.push('/dataset/home')
      this.$emit('switchComponent', { name: '' })
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

  span{
    font-size: 14px;
  }
</style>
