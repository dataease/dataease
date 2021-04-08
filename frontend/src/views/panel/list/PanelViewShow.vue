<template>
  <el-row style="height: 100%;width: 100%;">
    <el-col v-if="panelInfo.name.length>0" class="panel-design">
      <el-row class="panel-design-head">
        <!--TODO 仪表盘头部区域-->
        <span>{{ panelInfo.name || '测试仪表板' }}</span>
        <span style="float: right;">
          <el-tooltip content="预览">
            <el-button class="el-icon-view" size="mini" circle @click="clickPreview" />
          </el-tooltip>
        </span>
      </el-row>
      <!--TODO 仪表盘预览区域-->
      <el-row class="panel-design-preview">
        <Preview />
      </el-row>
    </el-col>
    <el-col v-if="panelInfo.name.length===0" style="height: 100%;">
      <el-row style="height: 100%;" class="custom-position">
        请从左侧选择仪表盘
      </el-row>
    </el-col>
  </el-row>
</template>
<script>
import Preview from '@/components/canvas/components/Editor/Preview'
import { mapState } from 'vuex'

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
    },
    ...mapState([
      'componentData',
      'canvasStyleData'
    ])
  },
  mounted() {

  },
  methods: {
    clickPreview() {
      debugger
      localStorage.setItem('canvasData', JSON.stringify(this.componentData))
      localStorage.setItem('canvasStyle', JSON.stringify(this.canvasStyleData))
      const url = '#/preview/' + this.$store.state.panel.panelInfo.id
      window.open(url, '_blank')
    }

  }
}
</script>

<style>
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
    min-height: 400px;
    height: 100%;
    min-width: 500px;
    overflow-y: auto;
    border-top: 1px solid #E6E6E6;
  }

  .panel-design-head {
    height: 40px;
    background-color: white;
    padding: 0 6px;
    line-height: 40px;
  }

  .panel-design-preview {
    width: 100%;
    height: calc(100% - 40px);
    overflow-x: hidden;
    overflow-y: auto;
    padding: 5px;
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

  .custom-position {
    flex: 1;
    display: flex;
    align-items: center;
    justify-content: space-between;
    font-size: 14px;
    flex-flow: row nowrap;
    color: #9ea6b2;
  }
</style>
