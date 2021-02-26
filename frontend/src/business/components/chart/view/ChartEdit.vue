<template>
<!--TODO 慢慢完善，写个DEMO-->
<el-row style="height: 100%;overflow-y: hidden;width: 100%;">
  <span v-show="false">{{tableId}}</span>
  <el-row style="height: 30px;">
    <span>{{tableId}}</span>
    <span style="float: right;">
      <el-button size="mini">
        取消
      </el-button>
      <el-button type="primary" size="mini">
        确认
      </el-button>
    </span>
  </el-row>
  <el-row style="display: flex;height: 100%">
    <el-col style="height: 100%;width: 20%;min-width: 180px;max-width:220px;border: 1px solid #E6E6E6;">
      <div style="height: 50%;border-bottom: 1px solid #E6E6E6;overflow:auto">
        纬度
        <draggable v-model="arr1" @end="end1" :options="{group:{name: 'itxst',pull:'clone'},sort: true}" animation="300"
                   :move="onMove">
          <transition-group>
            <div class="item" v-for="item in arr1" :key="item.id">{{item.name}}</div>
          </transition-group>
        </draggable>
      </div>
      <div style="height: 50%;overflow:auto">
        指标
        <draggable v-model="arr2" @end="end2" group="itxst" animation="300" :move="onMove">
          <transition-group>
            <div class="item" v-for="item in arr2" :key="item.id">{{item.name}}</div>
          </transition-group>
        </draggable>
      </div>
    </el-col>

    <el-col
      style="height: 100%;width: 25%;min-width: 200px;max-width:240px;border: 1px solid #E6E6E6;border-left: 0 solid;">
      <div style="border-bottom: 1px solid #E6E6E6;overflow-y:hidden;">
        <el-row>
          <span>标题</span>
          <el-checkbox v-model="checked" style="float: right;">显示</el-checkbox>
        </el-row>
        <el-form>
          <el-form-item class="form-item">
            <el-input
              size="mini"
              placeholder="标题"
              prefix-icon="el-icon-search"
              v-model="title"
              clearable>
            </el-input>
          </el-form-item>
        </el-form>
      </div>
      <div style="height: 30%;overflow:auto">
        <span>图标类型</span>
        <el-row>
          <div class="chart-type">
            <el-radio v-model="radio1" label="1">折线图</el-radio>
            <el-radio v-model="radio1" label="2">柱状图</el-radio>
          </div>
        </el-row>
      </div>
      <div style="height: 50%;overflow:auto;border-top: 1px solid #e6e6e6">
        <el-tabs type="card">
          <el-tab-pane label="图形属性">图形属性</el-tab-pane>
          <el-tab-pane label="组件样式">组件样式</el-tab-pane>
        </el-tabs>
      </div>
      <div style="height: 50%;overflow:auto;border-top: 1px solid #e6e6e6">
        <span>结果过滤器</span>

      </div>
    </el-col>

    <el-col style="height: 100%;min-width: 500px;border-top: 1px solid #E6E6E6;">
      <el-row style="width: 100%;height: 100%;">
        <el-form ref="form" :model="form" label-width="80px">
          <el-form-item label="横轴">
            <el-input v-model="form.x_axis" size="mini"></el-input>
          </el-form-item>
          <el-form-item label="纵轴">
            <el-input v-model="form.y_axis" size="mini"></el-input>
          </el-form-item>
        </el-form>
        <div class="Echarts" style="height: 100%;display: flex;">
          <div id="echart" style="width: 100%;height: 80%">

          </div>
        </div>
      </el-row>
    </el-col>
  </el-row>
</el-row>
</template>

<script>
import draggable from "vuedraggable";

export default {
  name: "ChartEdit",
  components: {draggable},
  data() {
    return {
      form: {
        x_axis: '',
        y_axis: ''
      },
      checked: true,
      radio1: '2',
      title: '',
      //定义要被拖拽对象的数组
      arr1: [
        {id: 1, name: 'id'},
        {id: 2, name: '名称'},
        {id: 3, name: '类型'},
        {id: 5, name: '状态'},
        {id: 4, name: '指标指标指标'}
      ],
      arr2: [
        {id: 11, name: '容量'},
      ],
      moveId: -1
    }
  },
  created() {
  },
  mounted() {
    this.myEcharts();
  },
  activated() {
  },
  computed: {
    tableId() {
      console.log(this.$store.state.chart.tableId);
      return this.$store.state.chart.tableId;
    },
  },
  methods: {
    //左边往右边拖动时的事件
    end1(e) {
      console.log(e)
      var that = this;
      var items = this.arr2.filter(function (m) {
        return m.id == that.moveId
      })
      //如果左边
      if (items.length < 2) return;
      this.arr2.splice(e.newDraggableIndex, 1)
    },
    //右边往左边拖动时的事件
    end2(e) {
      console.log(e)
      var that = this;
      var items = this.arr1.filter(function (m) {
        return m.id == that.moveId
      })
      //如果左边
      if (items.length < 2) return;
      this.arr1.splice(e.newDraggableIndex, 1)
    },
    //move回调方法
    onMove(e, originalEvent) {
      console.log('onmove');
      this.moveId = e.relatedContext.element.id;
      //不允许停靠
      if (e.relatedContext.element.id == 1) return false;
      //不允许拖拽
      if (e.draggedContext.element.id == 4) return false;
      if (e.draggedContext.element.id == 11) return false;
      return true;
    },

    myEcharts() {
      // 基于准备好的dom，初始化echarts实例
      var myChart = this.$echarts.init(document.getElementById('echart'));

      // 指定图表的配置项和数据
      var option = {
        title: {
          text: 'ECharts示例'
        },
        tooltip: {},
        legend: {
          data: ['销量']
        },
        xAxis: {
          data: ["衬衫", "羊毛衫", "雪纺衫", "裤子", "高跟鞋", "袜子"]
        },
        yAxis: {},
        series: [{
          name: '销量',
          type: 'bar',
          data: [5, 20, 36, 10, 10, 20]
        }]
      };
      myChart.setOption(option);
      window.onresize = function () {
        myChart.resize();
      }
    }
  },
  watch: {}
}
</script>

<style scoped>
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
    margin: 3px 10px 0px 10px;
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

  .item2 {
    padding: 6px 12px;
    margin: 0px 10px 0px 10px;
    border: solid 1px #eee;
    background-color: pink;
    text-align: left;
  }

  .item2 + .item2 {
    border-top: none;
    margin-top: 6px;
  }

  .item2:hover {
    outline: solid 1px #ddd;
    cursor: move;
  }

  .el-form-item {
    margin-bottom: 0;
  }
</style>
