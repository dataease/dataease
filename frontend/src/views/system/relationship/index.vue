<template>
  <div class="consanguinity">
    <div class="route-title">{{ $t('commons.consanguinity') }}</div>
    <div class="container-wrapper">
      <el-form
        :rules="rules"
        :inline="true"
        ref="form"
        :model="formInline"
        class="de-form-inline"
      >
        <el-form-item
          prop="queryType"
          :label="$t('commons.adv_search.search') + $t('table.type')"
        >
          <el-select
            v-model="formInline.queryType"
            @change="queryTypeChange"
            :placeholder="$t('fu.search_bar.please_select')"
          >
            <el-option
              v-for="item in queryTypeNameList"
              :key="item.value"
              :label="$t(item.label)"
              :value="item.value"
            >
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item prop="dataSourceName" :label="queryTypeTitle">
          <el-select
            v-model="formInline.dataSourceName"
            filterable
            :placeholder="$t('fu.search_bar.please_select')"
          >
            <el-option
              v-for="item in dataSourceNameList"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            >
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item style="float: right">
          <de-btn type="primary" @click="onSubmit">{{
            $t('commons.adv_search.search')
          }}</de-btn>
        </el-form-item>
      </el-form>
      <div class="select-icon">
        <i
          @click="activeIcon = 'date'"
          :class="[activeIcon === 'date' ? 'active-icon' : '']"
          class="el-icon-date"
        ></i>
        <svg-icon
          icon-class="sys-relationship"
          :class="[activeIcon === 'share' ? 'active-icon' : '']"
          @click="activeIcon = 'share'"
        />
      </div>
      <div v-show="activeIcon === 'share'" id="consanguinity">
        <consanguinity
          :chartSize="chartSize"
          :current="current"
          ref="consanguinity"
        />
      </div>
      <div v-show="activeIcon === 'date'" class="consanguinity-table">
        <grid-table
          v-loading="loading"
          :table-data="tableData"
          :pagination="paginationConfig"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        >
          <el-table-column :label="$t('table.id')" type="index" width="50" />
          <el-table-column
            prop="datasource"
            :formatter="formatter"
            show-overflow-tooltip
            :label="$t('commons.datasource') + $t('commons.name')"
          />
          <el-table-column
            prop="dataset"
            show-overflow-tooltip
            :formatter="formatter"
            :label="$t('dataset.name')"
          />
          <el-table-column
            prop="panel"
            show-overflow-tooltip
            :formatter="formatter"
            :label="$t('app_template.panel_name')"
          />
        </grid-table>
      </div>
    </div>
  </div>
</template>

<script>
import {
  getDatasourceRelationship,
  getDatasetRelationship,
  getPanelRelationship
} from '@/api/chart/chart.js'
import {
  listDatasource,
  getDatasetList,
  getPanelGroupList
} from '@/api/dataset/dataset'
import _ from 'lodash'
import GridTable from '@/components/gridTable/index.vue'
import consanguinity from './consanguinity.vue'
import { log } from '@antv/g2plot/lib/utils'
export default {
  name: 'Consanguinity',
  components: { GridTable, consanguinity },
  data() {
    return {
      formInline: {
        queryType: 'datasource',
        dataSourceName: ''
      },
      rules: {
        queryType: [{ required: true, trigger: 'blur' }],
        dataSourceName: [{ required: true, trigger: 'blur', message: this.$t('chart.name_can_not_empty') }]
      },
      dataSourceNameList: [],
      queryTypeNameList: [
        {
          label: 'commons.datasource',
          value: 'datasource'
        },
        {
          label: 'dataset.datalist',
          value: 'dataset'
        },
        {
          label: 'panel.panel',
          value: 'panel'
        }
      ],
      chartSize: {
        height: 0,
        width: 0
      },
      treeData: [],
      loading: false,
      activeIcon: 'date',
      paginationConfig: {
        currentPage: 1,
        pageSize: 10,
        total: 0
      }
    }
  },
  computed: {
    current() {
      const { queryType = '', dataSourceName } = this.formInline
      const obj =
        this.dataSourceNameList.find((ele) => dataSourceName === ele.value) ||
        {}
      return {
        queryType,
        num: obj.value,
        label: obj.label
      }
    },
    queryTypeTitle() {
      return (
        this.$t(
          this.queryTypeNameList.find(
            (ele) => ele.value === this.formInline.queryType
          ).label
        ) + this.$t('commons.name')
      )
    },
    tableData() {
      const { currentPage, pageSize } = this.paginationConfig
      return this.treeData.slice(
        (currentPage - 1) * pageSize,
        currentPage * pageSize
      )
    }
  },
  created() {
    this.listDatasource()
  },
  beforeDestroy() {
    window.removeEventListener('resize', this.getChartSize)
  },
  mounted() {
    window.addEventListener('resize', this.getChartSize)
    this.getChartSize()
  },
  methods: {
    getChartData() {
      const { queryType, dataSourceName: id } = this.formInline
      switch (queryType) {
        case 'datasource':
          this.getDatasourceRelationship(id)
          break
        case 'dataset':
          this.getDatasetRelationship(id)
          break
        case 'panel':
          this.getPanelRelationship(id)
          break
        default:
          break
      }
    },
    getDatasourceRelationship(id) {
      getDatasourceRelationship(id).then((res) => {
        const arr = res.data || []
        this.treeData = []
        this.dfsTree(arr, this.current)
        this.initTable()
      })
    },
    getDatasetRelationship(id) {
      getDatasetRelationship(id).then((res) => {
        const arr = res.data ? [res.data] : []
        this.treeData = []
        this.dfsTree(arr, this.current)
        this.initTable()
      })
    },
    getPanelRelationship(id) {
      getPanelRelationship(id).then((res) => {
        const arr = res.data || []
        this.treeData = []
        this.dfsTreeFlip(arr, this.current)
        this.initTable()
      })
    },
    formatter(row, column, cellValue) {
      return cellValue ? cellValue : '-'
    },
    initTable() {
      this.paginationConfig.total = this.treeData.length
    },
    dfsTreeFlip(arr = [], { queryType, label }) {
      arr.forEach((ele) => {
        const { name, type, subRelation = [] } = ele
        if (subRelation.length) {
          this.dfsTreeFlip(subRelation, { label: name })
        } else if (type === 'dataset') {
          const obj = {}
          obj[type] = name
          obj.datasource = label
          obj.panel = this.current.label
          this.treeData.push(obj)
        }
      })
    },
    dfsTree(arr = [], { queryType, label }, item) {
      arr.forEach((ele) => {
        const { name, type, subRelation = [] } = ele
        const obj = {}
        obj[type] = name
        obj[queryType] = label
        if (subRelation.length) {
          this.dfsTree(subRelation, { queryType: type, label: name }, obj)
        } else {
          this.treeData.push({ ...item, ...obj })
        }
      })
    },
    getChartSize: _.debounce(function () {
      const dom = document.querySelector(
        this.activeIcon === 'date' ? '.consanguinity-table' : '#consanguinity'
      )
      this.chartSize = {
        height: dom.offsetHeight + 'px',
        width: dom.offsetWidth + 'px'
      }
    }, 200),
    listDatasource() {
      listDatasource().then((res) => {
        const arr = res?.data || []
        this.dataSourceNameList = arr.map((ele) => ({
          value: ele.id,
          label: ele.name
        }))
      })
    },
    getDatasetList() {
      getDatasetList().then((res) => {
        const arr = res?.data || []
        this.dataSourceNameList = arr.map((ele) => ({
          value: ele.id,
          label: ele.name
        }))
      })
    },
    getPanelGroupList() {
      getPanelGroupList().then((res) => {
        const arr = res?.data || []
        this.dataSourceNameList = arr.map((ele) => ({
          value: ele.id,
          label: ele.name
        }))
      })
    },
    queryTypeChange(val) {
      this.formInline.dataSourceName = ''
      switch (val) {
        case 'datasource':
          this.listDatasource()
          break
        case 'dataset':
          this.getDatasetList()
          break
        case 'panel':
          this.getPanelGroupList()
          break
        default:
          break
      }
    },
    onSubmit() {
      this.$refs.form.validate((valid) => {
        if (valid) {
          if (this.activeIcon === 'date') {
            this.getChartData()
          } else {
            this.$refs.consanguinity.getChartData(
              this.formInline.dataSourceName
            )
          }
        }
      })
    },
    handleSizeChange(pageSize) {
      this.paginationConfig.currentPage = 1
      this.paginationConfig.pageSize = pageSize
      this.onSubmit()
    },
    handleCurrentChange(currentPage) {
      this.paginationConfig.currentPage = currentPage
      this.onSubmit()
    },
    initTree() {
      const chartDom = document.getElementById('consanguinity')
      this.treeChart = this.$echarts.init(chartDom)
      const data = {
        name: 'flare',
        children: [
          {
            name: 'data',
            children: [
              {
                name: 'converters',
                children: [
                  { name: 'Converters', value: 721 },
                  { name: 'DelimitedTextConverter', value: 4294 }
                ]
              },
              {
                name: 'DataUtil',
                value: 3322
              }
            ]
          },
          {
            name: 'display',
            children: [
              { name: 'DirtySprite', value: 8833 },
              { name: 'LineSprite', value: 1732 },
              { name: 'RectSprite', value: 3623 }
            ]
          },
          {
            name: 'flex',
            children: [{ name: 'FlareVis', value: 4116 }]
          },
          {
            name: 'scale',
            children: [
              { name: 'IScaleMap', value: 2105 },
              { name: 'LinearScale', value: 1316 },
              { name: 'LogScale', value: 3151 },
              { name: 'OrdinalScale', value: 3770 },
              { name: 'QuantileScale', value: 2435 },
              { name: 'QuantitativeScale', value: 4839 },
              { name: 'RootScale', value: 1756 },
              { name: 'Scale', value: 4268 },
              { name: 'ScaleType', value: 1821 },
              { name: 'TimeScale', value: 5833 }
            ]
          }
        ]
      }

      const option = {
        tooltip: {
          trigger: 'item',
          triggerOn: 'mousemove'
        },
        series: [
          {
            type: 'tree',
            id: 0,
            name: 'tree1',
            data: [data],
            top: '10%',
            left: '8%',
            bottom: '22%',
            right: '20%',
            symbolSize: 0,
            edgeShape: 'polyline',
            edgeForkPosition: '50%',
            initialTreeDepth: 3,
            lineStyle: {
              width: 2
            },
            label: {
              formatter: function (a, b) {
                return [`{a|${a.data.name}a}`].join('\n')
              },

              rich: {
                a: {
                  color: 'red',
                  backgroundColor: '#fff',
                  lineHeight: 10,
                  borderColor: '#DCDFE6',
                  borderWidth: '1',
                  borderRadius: 2,
                  padding: [5, 10]
                }
              }
            },
            leaves: {
              label: {
                position: 'right',
                verticalAlign: 'middle',
                align: 'left'
              }
            },
            expandAndCollapse: true,
            animationDuration: 550,
            animationDurationUpdate: 750
          }
        ]
      }

      this.treeChart.setOption(option)
    }
  }
}
</script>

<style lang="less">
.consanguinity {
  .active-icon {
    color: var(--primary, #3370ff);
  }
  box-sizing: border-box;
  background-color: var(--MainBG, #f5f6f7);
  overflow: hidden;
  padding: 24px 24px 24px 24px;
  height: 100%;
  display: flex;
  flex-direction: column;
  position: relative;

  .consanguinity-table {
    height: calc(100% - 100px);
    width: 100%;
  }

  .route-title {
    font-family: PingFang SC;
    font-size: 20px;
    font-weight: 500;
    line-height: 28px;
    text-align: left;
    color: var(--TextPrimary, #1f2329);
    width: 100%;
    margin: 0;
  }

  .container-wrapper {
    width: 100%;
    overflow: auto;
    background-color: var(--ContentBG, #ffffff);
    margin-top: 24px;
    padding: 24px;
    flex: 1;

    .select-icon {
      margin-bottom: 16px;
      i,
      svg {
        font-size: 16px;
        cursor: pointer;
        margin-right: 10px;
      }
    }
  }
}
</style>