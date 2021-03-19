<template>
  <el-row style="height: 100%;overflow-y: hidden;width: 100%;">
    <el-row style="display: flex;height: 100%">
      <el-col class="view-list">
        <el-tabs v-model="ViewActiveName">
          <!--视图展示操作-->
          <el-tab-pane name="Views" class="view-list-thumbnails-outline">
            <span slot="label"><i class="el-icon-s-data" />视图</span>
            <draggable
              v-model="panelDetails.viewsUsable"
              :options="{group:{name: 'itxst',pull:'clone'},sort: true}"
              animation="300"
              :move="onMove"
              style="height: 100%;overflow:auto"
              @end="end1"
            >
              <transition-group>
                <div v-for="item in panelDetails.viewsUsable" :key="item.name" @dblclick="panelViewAdd(item)">
                  <span style="color: gray">{{ item.name }}</span>
                  <img class="view-list-thumbnails" :src="'/common-files/images/'+item.id+'/VIEW_DEFAULT_IMAGE'" alt="">
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
            <el-button type="primary" size="mini" @click="preViewShow">
              预览
            </el-button>
          </span>
        </el-row>
        <el-row class="panel-design-show">
          <div class="container" :style="panelDetails.gridStyle">
            <vue-drag-resize-rotate
              v-for="panelDesign in panelDetails.panelDesigns"
              v-show="panelDesign.keepFlag"
              :key="panelDesign.id"
              :panel-design-id="panelDesign.id"
              :parent="true"
              @newStyle="newStyle"
              @removeView="removeView"
            >
              <!--视图显示 panelDesign.componentType==='view'-->
              <chart-component v-if="panelDesign.componentType==='view'" :ref="panelDesign.id" :chart-id="panelDesign.id" :chart="panelDesign.chartView" />

              <!--组件显示（待开发）-->

            </vue-drag-resize-rotate>
          </div>
        </el-row>

      </el-col>
    </el-row>
  </el-row>
</template>

<script>
import { get } from '@/api/panel/panel'
import draggable from 'vuedraggable'
import ChartComponent from '../../chart/components/ChartComponent'
import VueDragResizeRotate from '@/components/vue-drag-resize-rotate'
import { uuid } from 'vue-uuid'

export default {
  name: 'PanelViewShow',
  components: { draggable, ChartComponent, VueDragResizeRotate },
  data() {
    return {
      panelDetails: {
        viewsUsable: [],
        panelDesigns: [],
        gridStyle: null
      },
      gridStyleDefault: {
        position: 'relative',
        height: '100%',
        width: '100%',
        backgroundColor: '#808080',
        background: 'linear-gradient(-90deg, rgba(0, 0, 0, .1) 1px, transparent 1px), linear-gradient(rgba(0, 0, 0, .1) 1px, transparent 1px)',
        backgroundSize: '20px 20px, 20px 20px'
      },
      ViewActiveName: 'Views'
    }
  },
  computed: {
    panelInfo() {
      return this.$store.state.panel.panelInfo
    }
  },
  watch: {
    panelInfo(newVal, oldVal) {
      this.panelDesign(newVal.id)
    }
  },
  created() {
    // this.get(this.$store.state.chart.viewId);
  },
  mounted() {
    const panelId = this.$store.state.panel.panelInfo.id
    if (panelId) {
      this.panelDesign(panelId)
    }
  },
  activated() {
  },
  methods: {
    // 加载公共组件

    // 加载panel design
    panelDesign(panelId) {
      get('panel/group/findOne/' + panelId).then(res => {
        const panelDetailsInfo = res.data
        if (panelDetailsInfo) {
          this.panelDetails = panelDetailsInfo
        }
        if (!panelDetailsInfo.gridStyle) {
          this.panelDetails.gridStyle = this.gridStyleDefault
        }
      })
    },
    panelViewAdd(view) {
      const panelDesigns = this.panelDetails.panelDesigns
      this.panelDetails.viewsUsable.forEach(function(item, index) {
        if (item.id === view.id) {
          const newComponent = {
            id: uuid.v1(),
            keepFlag: true,
            chartView: item,
            componentType: 'view'
          }
          panelDesigns.push(newComponent)
        }
      })
    },
    removeView(panelDesignId) {
      this.panelDetails.panelDesigns.forEach(function(panelDesign, index) {
        if (panelDesign.id === panelDesignId) {
          panelDesign.keepFlag = false
        }
      })
    },
    newStyle(viewId, newStyleInfo) {
      this.$nextTick(() => {
        this.$refs[viewId][0].chartResize()
      })

      console.log(viewId)
      console.log(JSON.stringify(newStyleInfo))
    },

    // 左边往右边拖动时的事件
    start1(e) {
      console.log(e)
    },
    end1(e) {
      console.log(e)
    },
    // 右边往左边拖动时的事件
    start2(e) {
      console.log(e)
    },
    end2(e) {
      console.log(e)
    },
    // move回调方法
    onMove(e, originalEvent) {
      console.log(e)
      return true
    },
    preViewShow() {

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
