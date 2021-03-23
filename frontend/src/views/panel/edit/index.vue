<template>
  <el-container>
    <!-- <de-header>Header</de-header> -->
    <el-header class="de-header">
      <el-row class="panel-design-head">
        <span style="float: left;line-height: 35px; color: gray">

          <span>名称：测试仪表板</span>

        </span>
        <span style="float: right;line-height: 35px;">

          <el-tooltip content="返回目录">
            <el-button class="el-icon-refresh-left" size="mini" circle @click="toDir" />
          </el-tooltip>

          <el-tooltip content="背景图">
            <el-button class="el-icon-full-screen" size="mini" circle />
          </el-tooltip>

          <el-tooltip content="保存">
            <el-button class="el-icon-circle-check" size="mini" circle @click="saveDrawing" />
          </el-tooltip>

          <el-tooltip content="预览">
            <el-button class="el-icon-view" size="mini" circle @click="save" />
          </el-tooltip>

        </span>
      </el-row>
    </el-header>
    <de-container>
      <de-aside-container class="ms-aside-container">
        <div style="width: 60px; left: 0px; top: 0px; bottom: 0px; position: absolute">

          <div style="width: 60px;height: 100%;overflow: hidden auto;position: relative;margin: 0px auto;">
            <!-- 视图图表 -->
            <div class="button-div-class" style=" width: 24px;height: 24px;text-align: center;line-height: 1;position: relative;margin: 32px auto 0px;font-size:150%;">
              <el-button circle class="el-icon-circle-plus-outline" size="mini" @click="showPanel(0)" />
            </div>
            <!-- 视图文字 -->
            <div style="position: relative; margin: 18px auto 30px">
              <div style="max-width: 100%;text-align: center;white-space: nowrap;text-overflow: ellipsis;position: relative;flex-shrink: 0;">
                视图
              </div>
            </div>
            <!-- 视图分割线 -->
            <div style="height: 1px; position: relative; margin: 0px auto;background-color:#E6E6E6;">
              <div style="width: 60px;height: 1px;line-height: 1px;text-align: center;white-space: pre;text-overflow: ellipsis;position: relative;flex-shrink: 0;" />
            </div>
            <!-- 过滤组件 -->
            <div tabindex="-1" style="position: relative; margin: 20px auto">

              <div style="height: 60px; position: relative">

                <div class="button-div-class" style=" text-align: center;line-height: 1;position: absolute;inset: 0px 0px 45px; ">

                  <!-- <i class="el-icon-s-tools" style="width: 24px; height: 24px;position: relative;flex-shrink: 0;font-size:150%;" /> -->
                  <el-button circle class="el-icon-s-tools" size="mini" @click="showPanel(1)" />

                </div>
                <div style=" position: absolute;left: 0px;right: 0px;bottom: 10px; height: 16px;">

                  <div style=" max-width: 100%;text-align: center;white-space: nowrap;text-overflow: ellipsis;position: relative;flex-shrink: 0;">

                    组件
                  </div>
                </div>
              </div>
            </div>

          </div>
        </div>

        <div ref="leftPanel" :class="{show:show}" class="leftPanel-container">
          <div class="leftPanel-background" />
          <div v-if="show" class="leftPanel">

            <div class="leftPanel-items">
              <view-select v-if="show && showIndex===0" />
              <filter-group v-if="show && showIndex===1" />
            </div>
          </div>
        </div>

      </de-aside-container>
      <de-main-container class="ms-main-container">
        <drawing-board />
      </de-main-container>
    </de-container>
  </el-container>
</template>

<script>
import DeMainContainer from '@/components/dataease/DeMainContainer'
import DeContainer from '@/components/dataease/DeContainer'
import DeAsideContainer from '@/components/dataease/DeAsideContainer'
import { addClass, removeClass } from '@/utils'
import FilterGroup from '../filter'
import ViewSelect from '../ViewSelect'
import DrawingBoard from '../DrawingBoard'
import bus from '@/utils/bus'
export default {
  components: {
    DeMainContainer,
    DeContainer,
    DeAsideContainer,
    FilterGroup,
    ViewSelect,
    DrawingBoard

  },
  data() {
    return {
      show: false,
      clickNotClose: false,
      showIndex: -1
    }
  },
  watch: {
    show(value) {
      if (value && !this.clickNotClose) {
        this.addEventClick()
      }
      if (value) {
        addClass(document.body, 'showRightPanel')
      } else {
        removeClass(document.body, 'showRightPanel')
      }
    }
  },
  mounted() {
    this.insertToBody()
  },
  beforeDestroy() {
    const elx = this.$refs.rightPanel
    elx.remove()
  },
  methods: {
    save() {

    },
    toDir() {
      this.$router.replace('/panel/index')
    },
    showPanel(type) {
      this.show = !this.show
      this.showIndex = type
    },
    addEventClick() {
      window.addEventListener('click', this.closeSidebar)
    },
    closeSidebar(evt) {
      const parent = evt.target.closest('.button-div-class')
      const self = evt.target.closest('.leftPanel')
      if (!parent && !self) {
        this.show = false
        window.removeEventListener('click', this.closeSidebar)
        this.showIndex = -1
      }
    },
    insertToBody() {
      this.$nextTick(() => {
        const elx = this.$refs.leftPanel
        const body = document.querySelector('body')
        body.insertBefore(elx, body.firstChild)
      })
    },
    saveDrawing() {
      bus.$emit('panel-drawing-save')
    }

  }
}
</script>

<style scoped>
  .ms-aside-container {
    height: calc(100vh - 91px);
    padding: 15px;
    min-width: 60px;
    max-width: 60px;
    border: none;
  }

  .ms-main-container {
    height: calc(100vh - 91px);
  }

  .de-header {
    height: 35px !important;
    border-bottom: 1px solid #E6E6E6;
  }

  .showLeftPanel {
  overflow: hidden;
  position: relative;
  width: calc(100% - 15px);
}
</style>

<style lang="scss" scoped>
.leftPanel-background {
  position: fixed;
  top: 0;
  left: 0;
  opacity: 0;
  transition: opacity .3s cubic-bezier(.7, .3, .1, 1);
  background: rgba(0, 0, 0, .2);
  z-index: -1;
}

.leftPanel {
  width: 100%;
  max-width: 260px;
  height: calc(100vh - 91px);
  position: fixed;
  top: 91px;
  left: 60px;
  box-shadow: 0px 0px 15px 0px rgba(0, 0, 0, .05);
  transition: all .25s cubic-bezier(.7, .3, .1, 1);
  transform: translate(100%);
  background: #fff;
  z-index: 40000;
}

.show {
  transition: all .3s cubic-bezier(.7, .3, .1, 1);

  .leftPanel-background {
    z-index: 20000;
    opacity: 1;
    width: 100%;
    height: 100%;
  }

  .leftPanel {
    transform: translate(0);
  }

}

</style>
