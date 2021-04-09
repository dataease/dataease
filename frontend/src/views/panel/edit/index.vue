<template>
  <el-container>
    <el-header class="de-header">
      <el-row class="panel-design-head">
        <span style="float: left;line-height: 35px; color: gray">
          名称：{{ panelInfo.name || '测试仪表板' }}
        </span>
        <!--横向工具栏-->
        <Toolbar />
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
          <div />
          <div v-show="show" class="leftPanel">

            <div class="leftPanel-items">
              <view-select v-show=" showIndex===0" />
              <filter-group v-show="show && showIndex===1" />
            </div>
          </div>
        </div>
      </de-aside-container>

      <!--画布区域-->
      <de-main-container class="ms-main-container">
        <div
          class="content"
          @drop="handleDrop"
          @dragover="handleDragOver"
          @mousedown="handleMouseDown"
          @mouseup="deselectCurComponent"
        >
          <Editor />
        </div>
      </de-main-container>
    </de-container>

    <el-dialog
      v-if="filterVisible"
      title="过滤组件"
      :visible.sync="filterVisible"
      custom-class="de-filter-dialog"
    >
      <filter-dialog v-if="filterVisible" :component-info="currentComponent" :widget-id="currentWidgetId" @re-fresh-component="reFreshComponent">
        <de-drawing-widget
          v-if="filterVisible && currentComponent"
          :id="'component' + currentComponent.id"
          style="width: 100% !important;"
          class="component"
          :element="currentComponent"
          :item="currentComponent"
        />
      </filter-dialog>
      <!-- <div slot="footer" class="dialog-footer">
        <el-button type="text" @click="editPasswordVisible = false">{{ $t('commons.cancel') }}</el-button>
        <el-button type="primary" @click="editUserPassword('editPasswordForm')">确认</el-button>
      </div> -->
      <div style="text-align: end !important;margin: 0 15px !important;">
        <span slot="footer">
          <el-button @click="cancelFilter">取 消</el-button>
          <el-button type="primary" @click="sureFilter">确 定</el-button>
        </span>
      </div>
    </el-dialog>

  </el-container>
</template>

<script>
import DeMainContainer from '@/components/dataease/DeMainContainer'
import DeContainer from '@/components/dataease/DeContainer'
import DeAsideContainer from '@/components/dataease/DeAsideContainer'
import { addClass, removeClass } from '@/utils'
import FilterGroup from '../filter'
import ViewSelect from '../ViewSelect'
import bus from '@/utils/bus'
import Editor from '@/components/canvas/components/Editor/index'
import { deepCopy } from '@/components/canvas/utils/utils'
import componentList from '@/components/canvas/custom-component/component-list' // 左侧列表数据
import { listenGlobalKeyDown } from '@/components/canvas/utils/shortcutKey'
import { mapState } from 'vuex'
import { uuid } from 'vue-uuid'
import Toolbar from '@/components/canvas/components/Toolbar'
import { get } from '@/api/panel/panel'

// 引入样式
import '@/components/canvas/assets/iconfont/iconfont.css'
import '@/components/canvas/styles/animate.css'
import 'element-ui/lib/theme-chalk/index.css'
import '@/components/canvas/styles/reset.css'
import { ApplicationContext } from '@/utils/ApplicationContext'
import FilterDialog from '../filter/filterDialog'
export default {
  name: 'PanelEdit',
  components: {
    DeMainContainer,
    DeContainer,
    DeAsideContainer,
    FilterGroup,
    ViewSelect,
    Editor,
    Toolbar,
    FilterDialog
  },
  data() {
    return {
      show: false,
      editView: false,
      clickNotClose: false,
      showIndex: -1,
      activeName: 'attr',
      reSelectAnimateIndex: undefined,
      filterVisible: false,
      currentWidgetId: null,
      currentWidget: null,
      currentComponent: null
    }
  },

  computed: {
    panelInfo() {
      return this.$store.state.panel.panelInfo
    },
    ...mapState([
      'componentData',
      'curComponent',
      'isClickComponent',
      'canvasStyleData'
    ])
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
    },
    panelInfo(newVal, oldVal) {
      this.init(newVal.id)
    }
  },
  created() {
    this.init(this.$store.state.panel.panelInfo.id)
    // this.restore()
    // 全局监听按键事件
    listenGlobalKeyDown()
  },
  mounted() {
    this.insertToBody()
    bus.$on('component-on-drag', () => {
      this.show = false
    })
  },
  beforeDestroy() {
    const elx = this.$refs.rightPanel
    elx && elx.remove()
  },
  methods: {
    init(panelId) {
      // 如果临时画布有数据 则使用临时画布数据（视图编辑的时候 会保存临时画布数据）
      const componentDataTemp = this.$store.state.panel.componentDataTemp
      const canvasStyleDataTemp = this.$store.state.panel.canvasStyleDataTemp
      if (componentDataTemp && canvasStyleDataTemp) {
        this.$store.commit('setComponentData', this.resetID(JSON.parse(componentDataTemp)))
        this.$store.commit('setCanvasStyle', JSON.parse(canvasStyleDataTemp))
      } else if (panelId) {
        get('panel/group/findOne/' + panelId).then(response => {
          this.$store.commit('setComponentData', this.resetID(response.data.panelData))
          this.$store.commit('setCanvasStyle', response.data.panelStyle)
        })
      }
    },
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

    resetID(data) {
      data.forEach(item => {
        item.id = uuid.v1()
      })

      return data
    },
    handleDrop(e) {
      e.preventDefault()
      e.stopPropagation()
      let component
      const newComponentId = uuid.v1()
      const componentInfo = JSON.parse(e.dataTransfer.getData('componentInfo'))

      // 用户视图设置 复制一个模板
      if (componentInfo.type === 'view') {
        componentList.forEach(componentTemp => {
          if (componentTemp.type === 'view') {
            component = deepCopy(componentTemp)
            const propValue = {
              id: newComponentId,
              viewId: componentInfo.id
            }
            component.propValue = propValue
          }
        })
      } else {
        this.currentWidget = ApplicationContext.getService(componentInfo.id)
        if (this.currentWidget.filterDialog) {
          this.show = false
          this.currentComponent = deepCopy(this.currentWidget)
          this.currentComponent.style.top = e.offsetY
          this.currentComponent.style.left = e.offsetX
          this.currentComponent.id = newComponentId
          this.openFilterDiolog()
          return
        }
        component = deepCopy(this.currentWidget)
      }

      component.style.top = e.offsetY
      component.style.left = e.offsetX
      component.id = newComponentId
      this.$store.commit('addComponent', { component })
      this.$store.commit('recordSnapshot')
    },

    handleDragOver(e) {
      e.preventDefault()
      e.dataTransfer.dropEffect = 'copy'
    },

    handleMouseDown() {
      console.log('handleMouseDown123')

      this.$store.commit('setClickComponentStatus', false)
    },

    deselectCurComponent(e) {
      console.log('deselectCurComponent123')

      if (!this.isClickComponent) {
        this.$store.commit('setCurComponent', { component: null, index: null })
      }

      // 0 左击 1 滚轮 2 右击
      if (e.button !== 2) {
        this.$store.commit('hideContextMenu')
      }
    },
    openFilterDiolog() {
      this.currentWidgetId = this.currentComponent.name
      this.filterVisible = true
    },
    cancelFilter() {
      this.filterVisible = false
      this.currentWidgetId = null
      this.currentWidget = null
      this.currentComponent = null
    },
    sureFilter() {
      const component = deepCopy(this.currentComponent)
      this.$store.commit('addComponent', { component })
      this.$store.commit('recordSnapshot')
      this.cancelFilter()
    },
    reFreshComponent(component) {
      this.currentComponent = component
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
  max-width: 240px;
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
