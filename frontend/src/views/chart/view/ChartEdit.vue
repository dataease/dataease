<template>
  <!--TODO 慢慢完善，写个DEMO-->
  <el-row style="height: 100%;overflow-y: hidden;width: 100%;">
    <span v-show="false">{{ tableId }}</span>
    <span v-show="false">{{ sceneId }}</span>
    <span v-show="false">{{ vId }}</span>
    <el-row style="height: 40px;" class="padding-lr">
      <span style="line-height: 40px;">{{ view.name }}</span>
      <span style="float: right;line-height: 40px;">
        <el-button size="mini">
          {{ $t('chart.cancel') }}
        </el-button>
        <el-button type="primary" size="mini" @click="save">
          {{ $t('chart.confirm') }}
        </el-button>
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
            :options="{group:{name: 'itxst',pull:'clone'},sort: true}"
            animation="300"
            :move="onMove"
            style="height: 90%;overflow:auto"
            @end="end1"
          >
            <transition-group>
              <div v-for="item in dimension" :key="item.id" class="item" @click="click1(item)">{{ item.name }}</div>
            </transition-group>
          </draggable>
        </div>
        <div style="height: 45%;" class="padding-lr">
          <span>{{ $t('chart.quota') }}</span>
          <draggable
            v-model="quota"
            :options="{group:{name: 'itxst',pull:'clone'},sort: true}"
            animation="300"
            :move="onMove"
            style="height: 90%;overflow:auto"
            @end="end2"
          >
            <transition-group>
              <div v-for="item in quota" :key="item.id" class="item" @click="click2(item)">{{ item.name }}</div>
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
              />
            </el-form-item>
          </el-form>
        </div>
        <div style="height: 25%;overflow:auto" class="padding-lr">
          <span>{{ $t('chart.chart_type') }}</span>
          <el-row>
            <div class="chart-type">
              <!--TODO 这里要替换好看点的图标-->
              <el-radio v-model="view.type" label="line">折线图</el-radio>
              <el-radio v-model="view.type" label="bar">柱状图</el-radio>
            </div>
          </el-row>
        </div>
        <div style="height: 45%;overflow:auto;border-top: 1px solid #e6e6e6">
          <el-tabs type="card">
            <el-tab-pane :label="$t('chart.shape_attr')" class="padding-lr">TODO</el-tab-pane>
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
            <el-row style="display:flex;height: 30px;">
              <span style="line-height: 30px;width: 60px;text-align: right;">{{ $t('chart.x_axis') }}</span>
              <draggable
                v-model="view.xaxis"
                group="itxst"
                animation="300"
                :move="onMove"
                style="width:100%;height: 100%;margin:0 10px;border-radius: 4px;border: 1px solid #DCDFE6;"
                @end="end3"
              >
                <transition-group style="width: 100%;height: 100%;">
                  <el-tag
                    v-for="(item,index) in view.xaxis"
                    :key="index"
                    size="small"
                    class="item"
                    closable
                    @close="clear1(index)"
                  >
                    {{ item.name }}
                  </el-tag>
                </transition-group>
              </draggable>
            </el-row>
            <el-row style="display:flex;height: 30px;margin-top: 10px;">
              <span style="line-height: 30px;width: 60px;text-align: right;">{{ $t('chart.y_axis') }}</span>
              <draggable
                v-model="view.yaxis"
                group="itxst"
                animation="300"
                :move="onMove"
                style="width:100%;height: 100%;margin:0 10px;border-radius: 4px;border: 1px solid #DCDFE6;"
                @end="end4"
              >
                <transition-group style="width:100%;height: 100%;">
                  <el-tag
                    v-for="(item,index) in view.yaxis"
                    :key="index"
                    size="small"
                    class="item"
                    closable
                    @close="clear2(index)"
                  >
                    {{ item.name }}
                  </el-tag>
                </transition-group>
              </draggable>
            </el-row>
          </el-row>

          <div class="Echarts" style="height: 100%;display: flex;margin-top: 10px;">
            <div id="echart" style="width: 100%;height: 80vh;" />
          </div>
        </el-row>
      </el-col>
    </el-row>
  </el-row>
</template>

<script>
import draggable from 'vuedraggable'
import { BASE_BAR } from '../chart/chart'

export default {
  name: 'ChartEdit',
  components: { draggable },
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
        title: ''
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
      moveId: -1
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
  watch: {},
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
        this.$post('/dataset/table/get/' + id, null, response => {
          this.table = response.data
          this.initTableField(id)
        })
      }
    },
    initTableField(id) {
      this.$post('/dataset/table/getFieldsFromDE', this.table, response => {
        this.dimension = response.data.dimension
        this.quota = response.data.quota
      })
    },
    click1(item) {
      // console.log(item);
      const c = this.view.xaxis.filter(function(ele) {
        return ele.id === item.id
      })
      // console.log(c);
      if (c && c.length === 0) {
        this.view.xaxis.push(item)
      }
    },
    click2(item) {
      // console.log(item);
      const c = this.view.yaxis.filter(function(ele) {
        return ele.id === item.id
      })
      // console.log(c);
      if (c && c.length === 0) {
        this.view.yaxis.push(item)
      }
    },
    clear1(index) {
      this.view.xaxis.splice(this.view.xaxis.indexOf(index), 1)
    },
    clear2(index) {
      this.view.yaxis.splice(this.view.yaxis.indexOf(index), 1)
    },
    get(id) {
      if (id) {
        this.$post('/chart/view/get/' + id, null, response => {
          this.view = response.data
          this.view.xaxis = this.view.xaxis ? JSON.parse(this.view.xaxis) : []
          this.view.yaxis = this.view.yaxis ? JSON.parse(this.view.yaxis) : []
        })
      } else {
        this.view = {}
      }
    },
    save() {
      const view = JSON.parse(JSON.stringify(this.view))
      view.id = this.view.id
      view.sceneId = this.sceneId
      view.name = this.table.name
      view.tableId = this.$store.state.chart.tableId
      view.xaxis = JSON.stringify(view.xaxis)
      view.yaxis = JSON.stringify(view.yaxis)
      this.$post('/chart/view/save', view, response => {
        // this.get(response.data.id);
        this.getData(response.data.id)
        this.$store.commit('setChartSceneData', null)
        this.$store.commit('setChartSceneData', this.sceneId)
      })
    },
    getData(id) {
      if (id) {
        this.$post('/chart/view/getData/' + id, null, response => {
          this.view = response.data
          this.view.xaxis = this.view.xaxis ? JSON.parse(this.view.xaxis) : []
          this.view.yaxis = this.view.yaxis ? JSON.parse(this.view.yaxis) : []

          const chart = response.data
          const chart_option = JSON.parse(JSON.stringify(BASE_BAR))
          // console.log(chart_option);
          if (chart.data) {
            chart_option.title.text = chart.title
            chart_option.xAxis.data = chart.data.x
            chart.data.series.forEach(function(y) {
              chart_option.legend.data.push(y.name)
              chart_option.series.push(y)
            })
          }
          // console.log(chart_option);
          this.myEcharts(chart_option)
        })
      } else {
        this.view = {}
      }
    },

    // 左边往右边拖动时的事件
    end1(e) {
      // console.log(e)
      // var that = this;
      // var items = this.arr2.filter(function (m) {
      //   return m.id == that.moveId
      // })
      // //如果左边
      // if (items.length < 2) return;
      // this.arr2.splice(e.newDraggableIndex, 1)
    },
    // 右边往左边拖动时的事件
    end2(e) {
      // console.log(e)
      // var that = this;
      // var items = this.yAxisData.filter(function (m) {
      //   return m.id == that.moveId
      // })
      // //如果左边
      // if (items.length < 2) return;
      // this.yAxisData.splice(e.newDraggableIndex, 1)
    },
    end3(e) {

    },
    end4(e) {

    },
    // move回调方法
    onMove(e, originalEvent) {
      console.log(e)
      // this.moveId = e.relatedContext.element.id;
      // //不允许停靠
      // if (e.relatedContext.element.id == 1) return false;
      // //不允许拖拽
      // if (e.draggedContext.element.id == 4) return false;
      // if (e.draggedContext.element.id == 11) return false;
      return true
    },

    myEcharts(option) {
      // 基于准备好的dom，初始化echarts实例
      var myChart = this.$echarts.init(document.getElementById('echart'))
      // 指定图表的配置项和数据
      myChart.setOption(option, true)
      window.onresize = function() {
        myChart.resize()
      }
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
    padding: 2px 12px;
    margin: 3px 3px 0 3px;
    border: solid 1px #eee;
    background-color: #f1f1f1;
    text-align: left;
  }

  .item + .item {
    border-top: none;
    margin-top: 3px;
  }

  .item:hover {
    background-color: #fdfdfd;
    cursor: pointer;
  }

  .el-form-item {
    margin-bottom: 0;
  }
</style>
