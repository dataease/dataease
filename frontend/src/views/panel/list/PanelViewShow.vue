<template>
  <el-row style="height: 100%;overflow-y: hidden;width: 100%;">
    <el-row style="display: flex;height: 100%">
      <el-col class="view-list">
        <el-tabs v-model="ViewActiveName">
          <!--视图展示操作-->
          <el-tab-pane name="Views" class="view-list-thumbnails-outline">
            <span slot="label"><i class="el-icon-s-data" />视图</span>
            <draggable
              v-model="thumbnails"
              :options="{group:{name: 'itxst',pull:'clone'},sort: true}"
              animation="300"
              :move="onMove"
              style="height: 100%;overflow:auto"
              @end="end1"
            >
              <transition-group>
                <div v-for="item in thumbnails" :key="item.name" @dblclick="panelViewAdd(item)">
                  <span style="color: gray">{{ item.name }}</span>
                  <img class="view-list-thumbnails" :src="'/common-files/images/'+item.id" alt="">
                </div>
              </transition-group>
            </draggable>
          </el-tab-pane>

          <!--通用组件操作-->
          <el-tab-pane name="PublicTools">
            <span slot="label"><i class="el-icon-s-grid" />组件</span>
            开发中...
          </el-tab-pane>
        </el-tabs>
      </el-col>

      <el-col class="panel-design">
        <!--TODO 仪表盘设计公共设置区域-->
        <el-row class="panel-design-head">
          <span style="float: left;line-height: 40px; color: gray">名称：{{ panelInfo.name }}</span>
          <span style="float: right;line-height: 40px;">
            <el-button size="mini">
              背景图
            </el-button>
            <el-button type="primary" size="mini" @click="save">
              预览
            </el-button>
          </span>
        </el-row>
        <el-row class="panel-design-show">
          <div class="container" style="overflow-y: auto">

            <div :style="gridStyle">
              <!--              <draggable-->
              <!--                v-model="thumbnailsTmp"-->
              <!--                :options="{group:{name: 'itxst',pull:'clone'},sort: true}"-->
              <!--                animation="300"-->
              <!--                :move="onMove"-->
              <!--                style="height: 100%;overflow:auto"-->
              <!--                @end="end1"-->
              <!--              >-->
              <vdrr v-for="item in thumbnailsTmp" v-show="item.keepFlag" :key="item.name" :view-id="item.id" :parent="true" @newStyle="newStyle" @removeView="removeView">
                <img class="view-list-thumbnails" :src="'/common-files/images/'+item.id" alt="">
              </vdrr>
              <!--            </draggable>-->

            </div>
          </div>

          <!--          <div class="Echarts" style="height: 100%;display: flex;margin-top: 10px;">-->
          <!--            <div id="echart" style="width: 100%;height: 80vh;"/>-->
          <!--          </div>-->
        </el-row>

      </el-col>
    </el-row>
  </el-row>
</template>

<script>
import { post } from '@/api/dataset/dataset'
import draggable from 'vuedraggable'
import { BASE_BAR } from '../chart/chart'

export default {
  name: 'PanelViewShow',
  components: { draggable },
  data() {
    return {
      gridStyle: {
        position: 'relative',
        height: '2000px',
        width: '2000px',
        backgroundColor: '#808080',
        background: 'linear-gradient(-90deg, rgba(0, 0, 0, .1) 1px, transparent 1px), linear-gradient(rgba(0, 0, 0, .1) 1px, transparent 1px)',
        backgroundSize: '20px 20px, 20px 20px'
      },
      ViewActiveName: 'Views',
      table: {},
      thumbnailsTmp: [
        { id: 'b4e3fd39-1424-4f22-bbac-07885829fb59', name: 'TEST1', keepFlag: true, style: '' },
        { id: 'bf91a1dc-10c1-4383-87ae-9ab1d6e57918', name: 'TEST2', keepFlag: true, style: '' }
      ],
      thumbnails: [
        { id: 'e70d7955-44dc-4158-9002-7b48ed0d5d80', name: 'TEST1' },
        { id: 'bf91a1dc-10c1-4383-87ae-9ab1d6e57918', name: 'TEST2' },
        { id: 'aebc8346-c3f2-44ad-97d3-1e36a10dd0fa', name: 'TEST3' },
        { id: 'b4e3fd39-1424-4f22-bbac-07885829fb59', name: 'TEST4' }
      ],
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
    panelInfo() {
      return this.$store.state.panel.panelInfo
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
    panelViewAdd(item) {
      const pushItem = {
        id: item.id,
        name: item.name,
        keepFlag: true
      }
      debugger
      this.thumbnailsTmp.push(pushItem)
      console.log(this.thumbnailsTmp)
    },
    removeView(viewId) {
      this.thumbnailsTmp.forEach(function(item, index) {
        if (item.id === viewId) {
          item.keepFlag = false
        }
      })
    },
    newStyle(viewId, newStyleInfo) {
      console.log(viewId)
      console.log(JSON.stringify(newStyleInfo))
    },
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
        this.thumbnails = response.data.thumbnails
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
      this.view.xaxis.splice(index, 1)
    },
    clear2(index) {
      this.view.yaxis.splice(index, 1)
    },
    get(id) {
      if (id) {
        post('/chart/view/get/' + id, null).then(response => {
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
      post('/chart/view/save', view).then(response => {
        // this.get(response.data.id);
        this.getData(response.data.id)
        this.$store.dispatch('chart/setChartSceneData', null)
        this.$store.dispatch('chart/setChartSceneData', this.sceneId)
      })
    },
    getData(id) {
      if (id) {
        post('/chart/view/getData/' + id, null).then(response => {
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
      setTimeout(myChart.setOption(option, true), 500)
      window.onresize = function() {
        myChart.resize()
      }
    }
  }
}
</script>

<style scoped>
  .view-list {
    height: 100%;
    width: 20%;
    min-width: 180px;
    max-width: 220px;
    border: 1px solid #E6E6E6;
    border-left: 0 solid;
    overflow-y: auto;
  }

  .view-list-thumbnails-outline {
    height: 100%;
    overflow-y: auto;
  }

  .view-list-thumbnails {
    width: 100%;
    padding: 0px 15px 15px 0px;
  }

  .panel-design {
    height: 100%;
    min-width: 500px;
    border-top: 1px solid #E6E6E6;
  }

  .panel-design-head {
    height: 40px;
  }

  .panel-design-show {
    height: 100%;
    width: 100%;
    border-top: 1px solid #E6E6E6;
  }

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
    display: block;
  }

  .item + .item {
    border-top: none;
    margin-top: 3px;
  }

  .item:hover {
    background-color: #fdfdfd;
    cursor: pointer;
  }

  .item-axis {
    padding: 2px 12px;
    margin: 3px 3px 0 3px;
    border: solid 1px #eee;
    background-color: #f1f1f1;
    text-align: left;
  }

  .item-axis:hover {
    background-color: #fdfdfd;
    cursor: pointer;
  }

  .el-form-item {
    margin-bottom: 0;
  }

  .container {
    width: 100%;
    height: 600px;
    border: 1px solid #000;
    position: relative;
    box-sizing: border-box;
  }

  span {
    font-size: 12px;
  }
</style>
