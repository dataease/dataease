<template>
  <el-col>
    <el-row>
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
            <el-radio v-model="mode" label="0">{{ $t('dataset.direct_connect') }}</el-radio>
            <el-radio v-model="mode" label="1">{{ $t('dataset.sync_data') }}</el-radio>
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
      <el-row style="overflow: auto;height: 600px;">
        <el-checkbox-group v-model="checkTableList" size="small">
          <el-checkbox
            v-for="t in tables"
            :key="t"
            border
            :label="t"
          />
        </el-checkbox-group>
      </el-row>
    </el-row>
  </el-col>
</template>

<script>
export default {
  name: 'AddDB',
  data() {
    return {
      searchTable: '',
      options: [],
      dataSource: '',
      tables: [],
      checkTableList: [],
      scene: null,
      mode: '0'
    }
  },
  watch: {
    dataSource(val) {
      if (val) {
        this.$post('/datasource/getTables', { id: val }, response => {
          this.tables = response.data
        }
        )
      }
    }
  },
  mounted() {
    this.initDataSource()
    this.scene = this.$route.params.scene
  },
  activated() {
    this.initDataSource()
    this.scene = this.$route.params.scene
  },
  methods: {
    initDataSource() {
      this.$get('/datasource/list', response => {
        this.options = response.data
      })
    },

    save() {
      // console.log(this.checkTableList);
      // console.log(this.scene);
      const sceneId = this.scene.id
      const dataSourceId = this.dataSource
      const tables = []
      const mode = this.mode
      this.checkTableList.forEach(function(name) {
        tables.push({
          name: name,
          sceneId: sceneId,
          dataSourceId: dataSourceId,
          type: 'db',
          mode: parseInt(mode)
        })
      })
      this.$post('/dataset/table/batchAdd', tables, response => {
        this.$store.commit('setSceneData', new Date().getTime())
        this.cancel()
      })
    },

    cancel() {
      this.dataReset()
      this.$router.push('/dataset/home')
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
</style>
