<template>
  <!--TODO 慢慢完善，写个DEMO-->
  <el-row style="height: 100%;overflow-y: hidden;width: 100%;">
    <span v-show="false">{{ tableId }}</span>
    <span v-show="false">{{ sceneId }}</span>
    <span v-show="false">{{ vId }}</span>
    <el-row style="height: 40px;" class="padding-lr">
      <span style="line-height: 40px;">{{ view.name }}</span>
      <span style="float: right;line-height: 40px;">
        <el-button size="mini" @click="closeEdit">
          {{ $t('chart.close') }}
        </el-button>
        <!--        <el-button type="primary" size="mini" @click="save">-->
        <!--          {{ $t('chart.confirm') }}-->
        <!--        </el-button>-->
      </span>
    </el-row>
    <el-row style="display: flex;height: 100%">
      <el-col
        style="height: 100%;width: 20%;min-width: 180px;max-width:220px;border: 1px solid #E6E6E6;border-left: 0 solid;"
      >
        <div style="height: 45%;border-bottom: 1px solid #E6E6E6;" class="padding-lr">
          <span>{{ $t('chart.dimension') }}</span>
          <draggable
            v-model="dimension"
            :options="{group:{name: 'dimension',pull:'clone'},sort: true}"
            animation="300"
            :move="onMove"
            style="height: 90%;overflow:auto"
            @end="end1"
            @start="start1"
          >
            <transition-group>
              <span v-for="item in dimension" :key="item.id" class="item">{{ item.name }}</span>
            </transition-group>
          </draggable>
        </div>
        <div style="height: 45%;" class="padding-lr">
          <span>{{ $t('chart.quota') }}</span>
          <draggable
            v-model="quota"
            :options="{group:{name: 'quota',pull:'clone'},sort: true}"
            animation="300"
            :move="onMove"
            style="height: 90%;overflow:auto"
            @end="end1"
            @start="start1"
          >
            <transition-group>
              <span v-for="item in quota" :key="item.id" class="item">{{ item.name }}</span>
            </transition-group>
          </draggable>
        </div>
      </el-col>

      <el-col
        style="height: 100%;width: 25%;min-width: 200px;max-width:240px;border: 1px solid #E6E6E6;border-left: 0 solid;"
      >
        <div style="border-bottom: 1px solid #E6E6E6;overflow-y:hidden;" class="padding-lr">
          <el-row>
            <span>{{ $t('chart.title') }}</span>
            <!--          <el-checkbox v-model="view.show" style="float: right;">{{$t('chart.show')}}</el-checkbox>-->
          </el-row>
          <el-form>
            <el-form-item class="form-item">
              <el-input
                v-model="view.title"
                size="mini"
                :placeholder="$t('chart.title')"
                prefix-icon="el-icon-search"
                clearable
                @blur="save"
              />
            </el-form-item>
          </el-form>
        </div>
        <div style="height: 25%;overflow:auto" class="padding-lr">
          <span>{{ $t('chart.chart_type') }}</span>
          <el-row>
            <div class="chart-type">
              <!--TODO 这里要替换好看点的图标-->
              <el-radio v-model="view.type" label="bar"><i class="el-icon-platform-eleme" style="font-size: 20px" /></el-radio>
              <el-radio v-model="view.type" label="line">折线图</el-radio>
            </div>
          </el-row>
        </div>
        <div style="height: 45%;overflow:auto;border-top: 1px solid #e6e6e6">
          <el-tabs type="card" :stretch="true" class="tab-header">
            <el-tab-pane :label="$t('chart.shape_attr')" class="padding-lr">
              <color-selector :chart="chart" @onColorChange="onColorChange" />
            </el-tab-pane>
            <el-tab-pane :label="$t('chart.module_style')" class="padding-lr">TODO</el-tab-pane>
          </el-tabs>
        </div>
        <div style="height: 30%;overflow:auto;border-top: 1px solid #e6e6e6" class="padding-lr">
          <span>{{ $t('chart.result_filter') }}</span>
          <div>TODO</div>
        </div>
      </el-col>

      <el-col style="height: 100%;min-width: 500px;border-top: 1px solid #E6E6E6;">
        <el-row style="width: 100%;height: 100%;" class="padding-lr">
          <el-row style="margin-top: 10px;">
            <el-row style="display:flex;height: 32px;">
              <span style="line-height: 32px;width: 60px;text-align: right;">{{ $t('chart.dimension') }}</span>
              <draggable
                v-model="view.xaxis"
                group="dimension"
                animation="300"
                :move="onMove"
                style="width:100%;height: 100%;margin:0 10px;border-radius: 4px;border: 1px solid #DCDFE6;overflow-x: auto;display: flex;align-items: center;"
                @end="end2"
              >
                <transition-group class="draggable-group">
                  <dimension-item v-for="(item) in view.xaxis" :key="item.id" :item="item" />
                </transition-group>
              </draggable>
            </el-row>
            <el-row style="display:flex;height: 32px;margin-top: 10px;">
              <span style="line-height: 32px;width: 60px;text-align: right;">{{ $t('chart.quota') }}</span>
              <draggable
                v-model="view.yaxis"
                group="quota"
                animation="300"
                :move="onMove"
                style="width:100%;height: 100%;margin:0 10px;border-radius: 4px;border: 1px solid #DCDFE6;overflow-x: auto;display: flex;align-items: center;"
                @end="end2"
              >
                <transition-group class="draggable-group">
                  <quota-item v-for="(item) in view.yaxis" :key="item.id" :item="item" @onQuotaItemChange="quotaItemChange" />
                </transition-group>
              </draggable>
            </el-row>
          </el-row>

          <chart-component :chart="chart" />
        </el-row>
      </el-col>
    </el-row>
  </el-row>
</template>

<script>
import { post } from '@/api/dataset/dataset'
import draggable from 'vuedraggable'
import DimensionItem from '../components/DimensionItem'
import QuotaItem from '../components/QuotaItem'
import ChartComponent from '../components/ChartComponent'
// shape attr
import { DEFAULT_COLOR_CASE } from '../chart/chart'
import ColorSelector from '../components/ColorSelector'

export default {
  name: 'ChartEdit',
  components: { ColorSelector, ChartComponent, QuotaItem, DimensionItem, draggable },
  data() {
    return {
      table: {},
      dimension: [],
      quota: [],
      view: {
        xaxis: [],
        yaxis: [],
        show: true,
        type: 'bar',
        title: '',
        customAttr: {
          color: DEFAULT_COLOR_CASE
        }
      },
      // 定义要被拖拽对象的数组
      arr1: [
        { id: 1, name: 'id' },
        { id: 2, name: '名称' },
        { id: 3, name: '类型' },
        { id: 5, name: '状态' },
        { id: 4, name: '指标指标指标' }
      ],
      arr2: [
        { id: 11, name: '容量' }
      ],
      moveId: -1,
      chart: {}
    }
  },
  computed: {
    tableId() {
      // console.log(this.$store.state.chart.tableId);
      this.initTableData(this.$store.state.chart.tableId)
      return this.$store.state.chart.tableId
    },
    sceneId() {
      // console.log(this.$store.state.chart.sceneId);
      return this.$store.state.chart.sceneId
    },
    vId() {
      // console.log(this.$store.state.chart.viewId);
      this.getData(this.$store.state.chart.viewId)
      return this.$store.state.chart.viewId
    }

  },
  watch: {
    'view.type': {
      handler: function() {
        this.save()
      }
    }
  },
  created() {
    // this.get(this.$store.state.chart.viewId);
  },
  mounted() {
    // this.get(this.$store.state.chart.viewId);
    this.getData(this.$store.state.chart.viewId)
    // this.myEcharts();
  },
  activated() {
  },
  methods: {
    initTableData(id) {
      if (id != null) {
        post('/dataset/table/get/' + id, null).then(response => {
          this.table = response.data
          this.initTableField(id)
        })
      }
    },
    initTableField(id) {
      post('/dataset/table/getFieldsFromDE', this.table).then(response => {
        this.dimension = response.data.dimension
        this.quota = response.data.quota
      })
    },
    save() {
      const view = JSON.parse(JSON.stringify(this.view))
      view.id = this.view.id
      view.sceneId = this.sceneId
      view.name = this.table.name
      view.tableId = this.$store.state.chart.tableId
      view.xaxis.forEach(function(ele) {
        if (!ele.summary || ele.summary === '') {
          ele.summary = 'sum'
        }
      })
      view.yaxis.forEach(function(ele) {
        if (!ele.summary || ele.summary === '') {
          ele.summary = 'sum'
        }
      })
      view.xaxis = JSON.stringify(view.xaxis)
      view.yaxis = JSON.stringify(view.yaxis)
      view.customAttr = JSON.stringify(view.customAttr)
      post('/chart/view/save', view).then(response => {
        // this.get(response.data.id);
        this.getData(response.data.id)
        this.$store.dispatch('chart/setChartSceneData', null)
        this.$store.dispatch('chart/setChartSceneData', this.sceneId)
      })
    },
    closeEdit() {
      this.$emit('switchComponent', { name: '' })
    },
    getData(id) {
      if (id) {
        post('/chart/view/getData/' + id, null).then(response => {
          this.view = JSON.parse(JSON.stringify(response.data))
          this.view.xaxis = this.view.xaxis ? JSON.parse(this.view.xaxis) : []
          this.view.yaxis = this.view.yaxis ? JSON.parse(this.view.yaxis) : []
          this.view.customAttr = this.view.customAttr ? JSON.parse(this.view.customAttr) : {}
          // 将视图传入echart组件
          this.chart = response.data
        })
      } else {
        this.view = {}
      }
    },

    // 左边往右边拖动时的事件
    start1(e) {
      console.log(e)
      e.clone.className = 'item-on-move'
      e.item.className = 'item-on-move'
    },
    end1(e) {
      console.log(e)
      e.clone.className = 'item'
      e.item.className = 'item'
      this.refuseMove(e)
      this.removeCheckedKey(e)
      this.save()
    },
    // 右边往左边拖动时的事件
    start2(e) {
      console.log(e)
    },
    end2(e) {
      console.log(e)
      this.removeDuplicateKey(e)
      this.save()
    },
    removeCheckedKey(e) {
      const that = this
      const xItems = this.view.xaxis.filter(function(m) {
        return m.id === that.moveId
      })
      const yItems = this.view.yaxis.filter(function(m) {
        return m.id === that.moveId
      })
      if (xItems && xItems.length > 1) {
        this.view.xaxis.splice(e.newDraggableIndex, 1)
      }
      if (yItems && yItems.length > 1) {
        this.view.yaxis.splice(e.newDraggableIndex, 1)
      }
    },
    refuseMove(e) {
      const that = this
      const xItems = this.dimension.filter(function(m) {
        return m.id === that.moveId
      })
      const yItems = this.quota.filter(function(m) {
        return m.id === that.moveId
      })
      if (xItems && xItems.length > 1) {
        this.dimension.splice(e.newDraggableIndex, 1)
      }
      if (yItems && yItems.length > 1) {
        this.quota.splice(e.newDraggableIndex, 1)
      }
    },
    removeDuplicateKey(e) {
      const that = this
      const xItems = this.dimension.filter(function(m) {
        return m.id === that.moveId
      })
      const yItems = this.quota.filter(function(m) {
        return m.id === that.moveId
      })
      if (xItems && xItems.length > 0) {
        this.dimension.splice(e.newDraggableIndex, 1)
      }
      if (yItems && yItems.length > 0) {
        this.quota.splice(e.newDraggableIndex, 1)
      }
    },
    // move回调方法
    onMove(e, originalEvent) {
      console.log(e)
      this.moveId = e.draggedContext.element.id
      // //不允许停靠
      // if (e.relatedContext.element.id == 1) return false;
      // //不允许拖拽
      // if (e.draggedContext.element.id == 4) return false;
      // if (e.draggedContext.element.id == 11) return false;
      return true
    },

    quotaItemChange(item) {
      // 更新item
      // this.view.yaxis.forEach(function(ele) {
      //   if (ele.id === item.id) {
      //     ele.summary = item.summary
      //   }
      // })
      this.save()
    },

    onColorChange(val) {
      this.view.customAttr.color = val
      this.save()
    }
  }
}
</script>

<style scoped>
  .padding-lr {
    padding: 0 6px;
  }

  .itxst {
    margin: 10px;
    text-align: left;
  }

  .col {
    width: 40%;
    flex: 1;
    padding: 10px;
    border: solid 1px #eee;
    border-radius: 5px;
    float: left;
  }

  .col + .col {
    margin-left: 10px;
  }

  .item {
    padding: 2px 10px;
    margin: 2px 2px 0 2px;
    border: solid 1px #eee;
    text-align: left;
    color: #606266;
    background-color: rgba(35,46,64,.05);
    display: block;
  }

  .item-on-move {
    padding: 2px 10px;
    margin: 2px 2px 0 2px;
    border: solid 1px #eee;
    text-align: left;
    color: #606266;
    background-color: rgba(35,46,64,.05);
  }

  .item + .item {
    margin-top: 2px;
  }

  .item:hover {
    color: #1890ff;
    background: #e8f4ff;
    border-color: #a3d3ff;
    cursor: pointer;
  }

  .el-form-item {
    margin-bottom: 0;
  }

  span {
    font-size: 12px;
  }

  .tab-header>>>.el-tabs__item{
    font-size: 12px;
  }

  .draggable-group {
    display: inline-block;
    width: 100%;
    height: calc(100% - 6px);
  }

  .chart-type>>>.el-radio__input{
    display: none;
  }
</style>
