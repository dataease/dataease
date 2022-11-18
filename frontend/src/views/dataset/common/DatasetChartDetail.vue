<template>
  <el-col>
    <el-row>
      <span style="font-weight: 500;">{{ $t('dataset.detail') }}</span>
    </el-row>
    <el-col>
      <el-tabs
        v-if="tabStatus"
        v-model="tabActive"
        class="info-tab"
      >
        <el-tab-pane
          v-if="type === 'chart' && detail.chart"
          :label="$t('chart.datalist')"
          name="chart"
        >
          <el-col class="info-item">
            <p class="info-title">{{ $t('commons.name') }}</p>
            <p class="info-content">{{ detail.chart.name }}</p>
          </el-col>
          <el-col class="info-item">
            <p class="info-title">{{ $t('chart.chart_type') }}</p>
            <svg-icon
              v-if="detail.chart.type"
              :icon-class="detail.chart.type"
            />
          </el-col>
          <el-col class="info-item">
            <p class="info-title">{{ $t('chart.title') }}</p>
            <p class="info-content">{{ detail.chart.title || '-' }}</p>
          </el-col>
          <el-col class="info-item">
            <p class="info-title">{{ $t('dataset.create_by') }}</p>
            <p class="info-content">{{ detail.chart.createBy }}</p>
          </el-col>
          <el-col class="info-item">
            <p class="info-title">{{ $t('dataset.create_time') }}</p>
            <p class="info-content">{{ detail.chart.createTime | timestampFormatDate }}</p>
          </el-col>
        </el-tab-pane>

        <el-tab-pane
          v-if="detail.table"
          :label="$t('dataset.datalist')"
          name="table"
        >
          <el-col class="info-item">
            <p class="info-title">{{ $t('commons.name') }}</p>
            <p class="info-content">{{ detail.table.name }}</p>
          </el-col>
          <el-col class="info-item">
            <p class="info-title">{{ $t('dataset.type') }}</p>
            <p
              v-if="detail.table.type === 'db'"
              class="info-content"
            >{{ $t('dataset.db_data') }}</p>
            <p
              v-if="detail.table.type === 'sql'"
              class="info-content"
            >{{ $t('dataset.sql_data') }}</p>
            <p
              v-if="detail.table.type === 'excel'"
              class="info-content"
            >{{ $t('dataset.excel_data') }}</p>
            <p
              v-if="detail.table.type === 'custom'"
              class="info-content"
            >{{ $t('dataset.custom_data') }}</p>
            <p
              v-if="detail.table.type === 'union'"
              class="info-content"
            >{{ $t('dataset.union_data') }}</p>
          </el-col>
          <el-col
            v-show="detail.table.type === 'db'"
            class="info-item"
          >
            <p class="info-title">{{ $t('dataset.table') }}</p>
            <p class="info-content">{{ info.table }}</p>
          </el-col>
          <el-col
            v-if="detail.table.type === 'db' || detail.table.type === 'sql'"
            class="info-item"
          >
            <p class="info-title">{{ $t('dataset.mode') }}</p>
            <p
              v-if="detail.table.mode === 0"
              class="info-content"
            >{{ $t('dataset.direct_connect') }}</p>
            <p
              v-if="detail.table.mode === 1"
              class="info-content"
            >{{ $t('dataset.sync_data') }}</p>
          </el-col>
          <el-col class="info-item">
            <p class="info-title">{{ $t('dataset.create_by') }}</p>
            <p class="info-content">{{ detail.table.createBy }}</p>
          </el-col>
          <el-col class="info-item">
            <p class="info-title">{{ $t('dataset.create_time') }}</p>
            <p class="info-content">{{ detail.table.createTime | timestampFormatDate }}</p>
          </el-col>
        </el-tab-pane>

        <el-tab-pane
          v-if="detail.datasource"
          :label="$t('datasource.datasource')"
          name="datasource"
        >
          <el-col class="info-item">
            <p class="info-title">{{ $t('commons.name') }}</p>
            <p class="info-content">{{ detail.datasource.name }}</p>
          </el-col>
          <el-col class="info-item">
            <p class="info-title">{{ $t('commons.description') }}</p>
            <p class="info-content">{{ detail.datasource.desc || '-' }}</p>
          </el-col>
          <el-col class="info-item">
            <p class="info-title">{{ $t('datasource.type') }}</p>
            <p class="info-content">{{ detail.datasource.type }}</p>
          </el-col>
          <el-col class="info-item">
            <p class="info-title">{{ $t('dataset.create_time') }}</p>
            <p class="info-content">{{ detail.datasource.createTime | timestampFormatDate }}</p>
          </el-col>
        </el-tab-pane>
      </el-tabs>
    </el-col>
  </el-col>
</template>

<script>
import { post } from '@/api/dataset/dataset'
export default {
  name: 'DatasetChartDetail',
  props: {
    type: {
      type: String,
      required: true
    },
    data: {
      type: Object,
      required: true
    },
    tabStatus: {
      type: Boolean,
      required: true
    }
  },
  data() {
    return {
      tabActive: 'chart',
      detail: {
        chart: {},
        table: {},
        datasource: {}
      },
      info: {}
    }
  },
  computed: {
    panelInfo() {
      return this.$store.state.panel.panelInfo
    }
  },
  watch: {
    'data': function() {
      this.init()
    },
    'type': function() {
      this.typeChange()
    },
    'tabStatus': function() {
      this.typeChange()
    }
  },
  mounted() {
    this.init()
  },
  methods: {
    init() {
      if (this.data.id) {
        if (this.type === 'dataset') {
          post('/dataset/table/datasetDetail/' + this.data.id, null, false).then(res => {
            this.detail = res.data
            this.info = JSON.parse(res.data.table.info)
          })
        } else if (this.type === 'chart') {
          post('/chart/view/chartDetail/' + this.data.id + '/' + this.panelInfo.id, null, false).then(res => {
            this.detail = res.data
            this.info = JSON.parse(res.data.table.info)
          })
        }
      }
    },
    typeChange() {
      if (this.type === 'dataset') {
        this.tabActive = 'table'
      } else if (this.type === 'chart') {
        this.tabActive = 'chart'
      }
    }
  }
}
</script>

<style scoped>
  .info-tab ::v-deep .el-tabs__item{
    font-weight: 400;
    font-size: 12px;
  }
  .info-item{
     margin: 6px 0;
   }
  .info-title{
    margin: 0!important;
    font-weight: 600;
    font-size: 12px;
  }
  .info-content{
    margin: 0!important;
    font-size: 12px;
  }
</style>
