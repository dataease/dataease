<template>
  <el-row style="height: 100%;overflow-y: hidden;width: 100%;">
    <el-row style="display: flex;height: 100%">

      <el-col class="panel-design">
        <!--TODO 仪表盘设计公共设置区域-->
        <el-row class="panel-design-head">
          <span style="float: left;line-height: 40px; color: gray">
            <span>名称：{{ panelInfo.name || '测试仪表板' }}</span>
          </span>
          <span style="float: right;line-height: 40px;">
            <el-tooltip content="预览">
              <el-button class="el-icon-view" size="mini" circle @click="preViewShow" />
            </el-tooltip>
          </span>
        </el-row>
        <!--TODO 仪表盘预览区域-->
<!--        <Preview />-->
      </el-col>
    </el-row>
  </el-row>
</template>
<script>
import bus from '@/utils/bus'
import Preview from '@/components/canvas/components/Editor/Preview'

export default {
  name: 'PanelViewShow',
  components: { Preview },
  data() {
    return {

    }
  },
  computed: {
    panelInfo() {
      return this.$store.state.panel.panelInfo
    }
  },
  methods: {

    preViewShow() {
      bus.$emit('panel-drawing-preview')
    },
    savePanel() {
      bus.$emit('panel-drawing-save')
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
