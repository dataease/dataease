<template>
  <el-row>
    <el-header class="de-header">
      <el-col :span="8" style="text-overflow:ellipsis;overflow: hidden;white-space: nowrap;color: #606266;font-size: 16px">
        <span style="line-height: 35px;">
          {{ $t('commons.name') }} ：{{ panelInfo.name || '测试仪表板' }}
        </span>
      </el-col>
      <!--横向工具栏-->
      <el-col :span="16">
        <Toolbar @showPanel="showPanel" @close-left-panel="closeLeftPanel" />
      </el-col>
    </el-header>
    <el-main style="padding: 0px">
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
                  {{ $t('panel.view') }}
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
                      {{ $t('panel.module') }}
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
          <div ref="leftPanel" :class="{show:show}" class="leftPanel-container">
            <div />
            <div v-if="show" class="leftPanel">
              <div style="height:100%;overflow-y: auto">
                <view-select v-show=" show && showIndex===0" />
                <filter-group v-show=" show &&showIndex===1" />
                <subject-setting v-show=" show &&showIndex===2" />
              </div>
            </div>
          </div>
        </de-aside-container>

        <!--画布区域-->
        <de-main-container>
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
    </el-main>

    <el-dialog
      v-if="filterVisible && panelInfo.id"
      :title="$t('panel.module')"
      :visible.sync="filterVisible"
      custom-class="de-filter-dialog"
    >
      <filter-dialog v-if="filterVisible && currentWidget" :widget-info="currentWidget" :component-info="currentFilterCom" @re-fresh-component="reFreshComponent">
        <component
          :is="currentFilterCom.component"
          :id="'component' + currentFilterCom.id"
          class="component"
          :style="currentFilterCom.style"
          :element="currentFilterCom"
          :in-draw="false"
        />
      </filter-dialog>
      <div style="text-align: end !important;margin: 0 15px !important;">
        <span slot="footer">
          <el-button size="mini" @click="cancelFilter">{{ $t('commons.cancel') }}</el-button>
          <el-button :disabled="!currentFilterCom.options.attrs.fieldId" type="primary" size="mini" @click="sureFilter">{{ $t('commons.confirm') }}</el-button>
        </span>
      </div>
    </el-dialog>
  </el-row>
</template>

<script>
import DeMainContainer from '@/components/dataease/DeMainContainer'
import DeContainer from '@/components/dataease/DeContainer'
import DeAsideContainer from '@/components/dataease/DeAsideContainer'
import { addClass, removeClass } from '@/utils'
import FilterGroup from '../filter'
import ViewSelect from '../ViewSelect'
import SubjectSetting from '../SubjectSetting'
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
// import '@/components/canvas/styles/reset.css'

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
    FilterDialog,
    SubjectSetting
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
      currentWidget: null,
      currentFilterCom: null,
      subjectVisible: false
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
      'canvasStyleData',
      'curComponentIndex'
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

    bus.$on('component-dialog-edit', () => {
      this.eidtDialog()
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
          this.$store.commit('setComponentData', this.resetID(JSON.parse(response.data.panelData)))
          const panelStyle = JSON.parse(response.data.panelStyle)
          this.$store.commit('setCanvasStyle', panelStyle)
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
      // 点击样式按钮 排除
      const stick = evt.target.closest('.el-icon-magic-stick')
      if (!parent && !self && !stick) {
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
      if (data) {
        data.forEach(item => {
          item.id = uuid.v1()
        })
      }
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

        this.currentFilterCom = this.currentWidget.getDrawPanel()
        this.currentFilterCom.style.top = e.offsetY
        this.currentFilterCom.style.left = e.offsetX
        this.currentFilterCom.id = newComponentId
        if (this.currentWidget.filterDialog) {
          this.show = false
          this.openFilterDiolog()
          return
        }
        component = deepCopy(this.currentFilterCom)
      }

      component.style.top = e.offsetY
      component.style.left = e.offsetX
      component.id = newComponentId
      this.$store.commit('addComponent', { component })
      this.$store.commit('recordSnapshot')
      this.clearCurrentInfo()
    },
    clearCurrentInfo() {
      this.currentWidget = null
      this.currentFilterCom = null
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
      this.filterVisible = true
    },
    cancelFilter() {
      this.filterVisible = false
      this.currentWidget = null
      this.clearCurrentInfo()
    },
    sureFilter() {
      const component = deepCopy(this.currentFilterCom)
      //   this.$store.commit('addComponent', { component })
      this.$store.commit('setComponentWithId', component)
      this.$store.commit('recordSnapshot')
      this.cancelFilter()
    },
    reFreshComponent(component) {
      this.currentFilterCom = component
      this.$forceUpdate()
    },
    eidtDialog() {
      const serviceName = this.curComponent.serviceName
      this.currentWidget = ApplicationContext.getService(serviceName)
      this.currentFilterCom = this.curComponent
      this.openFilterDiolog()
    },
    closeLeftPanel() {
      this.show = false
      this.beforeDestroy()
    }
  }
}
</script>

<style scoped>
  .ms-aside-container {
    height: calc(100vh - 91px);
    min-width: 40px;
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
  width: 100%;
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
  max-width: 300px;
  height: calc(100vh - 91px);
  position: fixed;
  top: 91px;
  left: 60px;
  box-shadow: 0px 0px 15px 0px rgba(0, 0, 0, .05);
  transition: all .25s cubic-bezier(.7, .3, .1, 1);
  transform: translate(100%);
  background: #fff;
  z-index: 1003;
}

.show {
  transition: all .3s cubic-bezier(.7, .3, .1, 1);

  .leftPanel-background {
    z-index: 1002;
    opacity: 1;
    width: 100%;
    height: 100%;
  }

  .leftPanel {
    transform: translate(0);
  }
}

</style>
