<template>
  <div
    v-if="tabStatus"
    class="info-tab"
  >
    <div
      v-if="type === 'chart' && detail.chart"
      class="info-card"
    >
      <div class="title-type">
        {{ $t('chart.datalist') }}
      </div>
      <div class="info-item">
        <p class="info-title">
          {{ $t('chart.datalist') + $t('desearchbutton.text') }}
        </p>
        <p class="info-content">{{ detail.chart.name }}</p>
      </div>
      <div class="info-item">
        <p class="info-title">{{ $t('chart.chart_type') }}</p>
        <svg-icon
          v-if="detail.chart.type"
          :icon-class="detail.chart.type"
        />
      </div>
      <div class="info-item">
        <p class="info-title">{{ $t('chart.title') }}</p>
        <p class="info-content">{{ detail.chart.title || 'N/A' }}</p>
      </div>
      <div class="info-item">
        <p class="info-title">{{ $t('dataset.create_by') }}</p>
        <p class="info-content">{{ detail.chart.createBy }}</p>
      </div>
      <div class="info-item">
        <p class="info-title">{{ $t('dataset.create_time') }}</p>
        <p class="info-content">
          {{ detail.chart.createTime | timestampFormatDate }}
        </p>
      </div>
    </div>

    <div
      v-if="detail.table"
      class="info-card"
    >
      <div class="title-type">
        {{ $t('dataset.datalist') }}
      </div>
      <div class="info-item">
        <p class="info-title">
          {{ $t('dataset.datalist') + $t('desearchbutton.text') }}
        </p>
        <p class="info-content">{{ detail.table.name }}</p>
      </div>
      <div class="info-item">
        <p class="info-title">{{ $t('dataset.type') }}</p>
        <p
          v-if="detail.table.type === 'db'"
          class="info-content"
        >
          {{ $t('dataset.db_data') }}
        </p>
        <p
          v-if="detail.table.type === 'sql'"
          class="info-content"
        >
          {{ $t('dataset.sql_data') }}
        </p>
        <p
          v-if="detail.table.type === 'excel'"
          class="info-content"
        >
          {{ $t('dataset.excel_data') }}
        </p>
        <p
          v-if="detail.table.type === 'custom'"
          class="info-content"
        >
          {{ $t('dataset.custom_data') }}
        </p>
        <p
          v-if="detail.table.type === 'union'"
          class="info-content"
        >
          {{ $t('dataset.union_data') }}
        </p>
        <p
          v-if="detail.table.type === 'api'"
          class="info-content"
        >Api</p>
      </div>
      <div
        v-show="detail.table.type === 'db'"
        class="info-item"
      >
        <p class="info-title">{{ $t('dataset.table') }}</p>
        <p class="info-content">{{ info.table }}</p>
      </div>
      <div
        v-if="detail.table.type === 'db' || detail.table.type === 'sql'"
        class="info-item"
      >
        <p class="info-title">{{ $t('dataset.mode') }}</p>
        <p
          v-if="detail.table.mode === 0"
          class="info-content"
        >
          {{ $t('dataset.direct_connect') }}
        </p>
        <p
          v-if="detail.table.mode === 1"
          class="info-content"
        >
          {{ $t('dataset.sync_data') }}
        </p>
      </div>
      <div class="info-item">
        <p class="info-title">{{ $t('dataset.create_by') }}</p>
        <p class="info-content">{{ detail.table.creatorName || 'N/A' }}</p>
      </div>
      <div class="info-item">
        <p class="info-title">{{ $t('dataset.create_time') }}</p>
        <p class="info-content">
          {{ detail.table.createTime | timestampFormatDate }}
        </p>
      </div>
    </div>

    <div
      v-if="detail.datasource"
      class="info-card"
    >
      <div class="title-type">
        {{ $t('datasource.datasource') }}
      </div>
      <div class="info-item">
        <p class="info-title">
          {{ $t('datasource.datasource') + $t('desearchbutton.text') }}
        </p>
        <p class="info-content">{{ detail.datasource.name }}</p>
      </div>
      <div class="info-item">
        <p class="info-title">{{ $t('commons.description') }}</p>
        <p class="info-content">{{ detail.datasource.desc || '-' }}</p>
      </div>
      <div class="info-item">
        <p class="info-title">{{ $t('datasource.type') }}</p>
        <p class="info-content">
          {{ detail.datasource.type }}
        </p>
      </div>
      <div class="info-item">
        <p class="info-title">{{ $t('dataset.create_time') }}</p>
        <p class="info-content">
          {{ detail.datasource.createTime | timestampFormatDate }}
        </p>
      </div>
    </div>
  </div>
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
    data: function() {
      this.init()
    },
    type: function() {
      this.typeChange()
    },
    tabStatus: function() {
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
          post(
            '/dataset/table/datasetDetail/' + this.data.id,
            null,
            false
          ).then((res) => {
            this.detail = res.data
            this.info = JSON.parse(res.data.table.info)
          })
        } else if (this.type === 'chart') {
          post(
            '/chart/view/chartDetail/' + this.data.id + '/' + this.panelInfo.id,
            null,
            false
          ).then((res) => {
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

<style lang="scss" scoped>
.info-tab {
  width: 100%;
  padding: 0 4px;
  font-family: PingFang SC;
  box-sizing: border-box;

  .title-type {
    font-size: 14px;
    font-weight: 500;
    margin-bottom: 8px;
    color: var(--deTextPrimary, #1f2329);
  }

  .info-card {
    padding-bottom: 4px;
    border-bottom: 1px solid rgba(31, 35, 41, 0.15);
    margin-bottom: 12px;
    .info-item {
      font-family: PingFang SC;
      font-weight: 400;
      margin: 6px 0 12px 0;
    }
    .info-title {
      margin: 0!important;
      font-weight: 600;
      font-size: 12px;
    }
    .info-content {
      font-size: 12px;
      margin: 0!important;
    }
  }

  :last-child {
    border: none;
  }
}
</style>
