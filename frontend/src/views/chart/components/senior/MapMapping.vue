<template>
  <div style="width: 100%;display: block !important;">
    <el-table
      :data="currentDatas"
      size="mini"
      :span-method="mergeCellMethod"
      style="width: 100%"
      :row-style="{height:0+'px'}"
    >
      <el-table-column
        :label="$t('map_mapping.map')"
        prop="mapArea"
        width="160"
        show-overflow-tooltip
      />
      <el-table-column
        :label="$t('map_mapping.attr')"
        width="50"
      >
        <template slot-scope="scope">
          <el-input
            v-if="scope.row.isEdit"
            :ref="scope.row.mapArea + scope.$index + '-widget'"
            v-model="scope.row.attrArea"
            size="mini"
            @blur="finishEdit(scope.row)"
            @keyup.enter.native="$event.target.blur()"
          >

            <i slot="suffix" class="el-icon-success el-input__icon map-mapping-ok" @click="finishEdit(scope.row)" />
          </el-input>

          <el-button v-else size="mini" plain @click="triggerEdit(scope)">
            <span class="mapping-span">{{ scope.row.attrArea }}</span>
            <i class="el-icon-edit el-icon--right" />
          </el-button>

        </template>
      </el-table-column>
      <el-table-column align="right">

        <template slot="header" slot-scope="scope">
          <el-input
            v-model="keyWord"
            size="mini"
            placeholder="输入关键字搜索"
          />
        </template>

      </el-table-column>

      <el-empty slot="empty" :description="!!currentAreaCode ? $t('map_mapping.empty'): $t('map_mapping.please_select_map')" />
    </el-table>
    <div class="mapping-pagination">
      <el-pagination
        small
        layout="prev, pager, next"
        :page-size="pageSize"
        :total="total"
        :current-page="currentPage"
        @current-change="searchPager"
        @prev-click="searchPager"
        @next-click="searchPager"
      />
    </div>
  </div>
</template>

<script>
import {
  geoJson
} from '@/api/map/map'
export default {
  name: 'FunctionCfg',
  props: {
    chart: {
      type: Object,
      required: true
    },
    dynamicAreaCode: {
      type: String,
      default: null
    }
  },
  data() {
    return {
      mappingForm: {},

      keyWord: '',
      gridList: [],
      currentPage: 1,
      pageSize: 10,
      total: 0,
      currentDatas: [],
      usePage: true
    }
  },
  computed: {
    chartId() {
      return this.chart.id
    },
    currentAreaCode() {
      if (this.dynamicAreaCode) {
        return this.dynamicAreaCode
      }
      const customAttr = this.chart.customAttr
      if (!customAttr) return ''
      let attr = null
      if ((typeof customAttr) === 'string') {
        attr = JSON.parse(customAttr)
      } else {
        attr = JSON.parse(JSON.stringify(customAttr))
      }
      return attr.areaCode
    }

  },
  watch: {
    'chart': {
      handler: function() {
        this.initData()
      }
    },
    'keyWord': {
      handler: function() {
        this.currentPage = 1
        this.buildGridList()
      }
    },
    'dynamicAreaCode': {
      handler: function(val, old) {
        if (val !== old) {
          this.$nextTick(() => {
            this.initData()
          })
        }
      }
    }
  },
  mounted() {
    this.initData()
  },
  methods: {
    triggerEdit(scope) {
      this.$set(scope.row, 'isEdit', true)
      this.$nextTick(() => {
        this.$refs[scope.row.mapArea + scope.$index + '-widget'] && this.$refs[scope.row.mapArea + scope.$index + '-widget'].focus()
      })
    },
    finishEdit(row) {
      row.isEdit = false
      this.mappingForm[this.currentAreaCode][row.mapArea] = row.attrArea
      this.changeMapping()
    },

    mergeCellMethod({ row, column, rowIndex, columnIndex }) {
      if (columnIndex === 1) {
        return [1, 2]
      }
      if (columnIndex === 2) {
        return [0, 0]
      }
    },

    initData() {
      const chart = JSON.parse(JSON.stringify(this.chart))
      if (chart.senior) {
        let senior = null
        if (Object.prototype.toString.call(chart.senior) === '[object Object]') {
          senior = JSON.parse(JSON.stringify(chart.senior))
        } else {
          senior = JSON.parse(chart.senior)
        }
        if (senior.mapMapping && senior.mapMapping[this.currentAreaCode]) {
          this.mappingForm = senior.mapMapping
        } else {
          this.initMapping()
        }
        this.buildGridList()
      }
    },
    buildGridList() {
      this.currentDatas = []
      if (!this.currentAreaCode || !this.mappingForm[this.currentAreaCode]) return
      this.gridList = Object.keys(this.mappingForm[this.currentAreaCode]).map(key => {
        return {
          mapArea: key,
          attrArea: this.mappingForm[this.currentAreaCode][key] || key
        }
      })
      const baseDatas = JSON.parse(JSON.stringify(this.gridList))
      const tempDatas = baseDatas.filter(data => !this.keyWord || data.mapArea.toLowerCase().includes(this.keyWord.toLowerCase()) || (data.attrArea && data.attrArea.toLowerCase().includes(this.keyWord.toLowerCase())))
      if (this.usePage) {
        const start = (this.currentPage - 1) * this.pageSize
        let end = this.currentPage * this.pageSize
        if (end >= tempDatas.length) end = tempDatas.length
        this.currentDatas = tempDatas.slice(start, end)
      } else {
        this.currentDatas = tempDatas
      }
      this.total = tempDatas.length
    },
    initMapping() {
      const innerCallBack = (json, cCode) => {
        const features = json.features
        if (!this.mappingForm) {
          this.mappingForm = {}
        }
        if (!this.mappingForm[cCode]) {
          this.mappingForm[cCode] = {}
        }

        features.forEach(feature => {
          this.mappingForm[cCode][feature.properties.name || feature.properties.NAME] = null
        })
      }
      const cCode = this.currentAreaCode
      if(!cCode) return
      if (this.$store.getters.geoMap[cCode]) {
        const json = this.$store.getters.geoMap[cCode]
        json && innerCallBack(json, cCode)
      } else {
        geoJson(cCode).then(res => {
          this.$store.dispatch('map/setGeo', {
            key: cCode,
            value: res
          }).then(() => {
            res && innerCallBack(res, cCode)
          })
        })
      }
    },
    changeMapping() {
      this.$emit('onMapMappingChange', this.mappingForm)
    },
    searchPager(pageNumber) {
      this.currentPage = pageNumber
      this.buildGridList()
    }
  }
}
</script>

<style lang="scss" scoped>
.mapping-span {
  display: inline-block;
  width: 90px;
  white-space: nowrap;
  text-overflow: ellipsis;
  overflow: hidden;
  text-align: left
}

.map-mapping-ok {
  cursor: pointer;
}
.map-mapping-ok:hover {
  color: #409eff;
}

</style>
