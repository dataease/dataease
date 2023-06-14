<template>
  <div
    class="de-tabs-div"
    :class="headClass"
  >
    <div
      v-if="maskShow"
      class="frame-mask edit-mask"
    />
    <dataease-tabs
      v-model="activeTabName"
      type="card"
      style-type="radioGroup"
      class="de-tabs-height"
      :class="isCurrentEdit ? 'de-tabs-edit' : ''"
      :font-color="fontColor"
      :active-color="activeColor"
      :border-color="borderColor"
      :border-active-color="borderActiveColor"
      :addable="isEdit && !mobileLayoutStatus"
      @tab-add="addTab"
      @tab-click="handleClick"
    >

      <el-tab-pane
        v-for="(item, index) in element.options.tabList"
        :key="item.name+index"
        :lazy="true"
        :name="item.name"
      >
        <span slot="label">
          <span :style="titleStyle(item.name)">{{ item.title }}</span>
          <el-dropdown
            v-if="dropdownShow"
            slot="label"
            class="de-tab-drop"
            trigger="click"
            @command="handleCommand"
          >
            <span class="el-dropdown-link">
              <i
                v-if="isEdit"
                class="de-tab-i el-icon-arrow-down el-icon--right"
              />
            </span>

            <el-dropdown-menu slot="dropdown">
              <el-dropdown-item :command="beforeHandleCommand('editTitle', item)">
                {{ $t('detabs.eidttitle') }}
              </el-dropdown-item>

              <el-dropdown-item
                v-if=" element.options.tabList.length > 1"
                :command="beforeHandleCommand('deleteCur', item)"
              >
                {{ $t('table.delete') }}
              </el-dropdown-item>

            </el-dropdown-menu>
          </el-dropdown>
        </span>
        <de-canvas-tab
          v-if="item.content && item.content.type==='canvas' && isEdit && !mobileLayoutStatus"
          :ref="'canvasTabRef-'+item.name"
          :parent-forbid="true"
          :canvas-style-data="canvasStyleData"
          :component-data="tabCanvasComponentData(item.name)"
          :canvas-id="element.id+'-'+item.name"
          class="tab_canvas"
          :class="moveActive ? 'canvas_move_in':''"
          @canvasScroll="canvasScroll"
        />
        <div
          v-if="item.content && item.content.type==='canvas' && (!isEdit || mobileLayoutStatus)"
          style="width: 100%;height:100%"
        >
          <Preview
            :component-data="tabCanvasComponentData(item.name)"
            :ref="'canvasTabRef-'+item.name"
            :canvas-style-data="canvasStyleData"
            :canvas-id="element.id+'-'+item.name"
            :panel-info="panelInfo"
            :in-screen="inScreen"
            :show-position="showPosition"
          />
        </div>

        <component
          :is="item.content.component"
          v-if="item.content && item.content.type!=='view'"
          :ref="item.name"
          :in-tab="true"
          :is-edit="isEdit"
          :active="active"
          :element="item.content"
          :filters="filterMap[item.content.propValue && item.content.propValue.viewId] || []"
          :out-style="outStyle"
          :edit-mode="editMode"
          :h="tabH"
        />
        <div class="de-tab-content">
          <user-view
            v-if="item.content && item.content.type==='view' && item.content.propValue && item.content.propValue.viewId"
            :ref="item.name"
            :in-tab="true"
            :is-edit="isEdit"
            :active="active"
            :element="item.content"
            :filters="filterMap[item.content.propValue && item.content.propValue.viewId] || []"
            :out-style="outStyle"
            :canvas-style-data="canvasStyleData"
            :show-position="showPosition"
          />
        </div>

      </el-tab-pane>
    </dataease-tabs>

    <el-dialog
      :title="$t('detabs.eidttitle')"
      :append-to-body="true"
      :visible.sync="dialogVisible"
      width="30%"
      :show-close="false"
      :close-on-click-modal="false"
      center
    >
      <el-input
        v-model="textarea"
        type="textarea"
        :rows="2"
        maxlength="10"
        show-word-limit
        :placeholder="$t('dataset.input_content')"
      />
      <span
        slot="footer"
        class="dialog-footer"
      >
        <el-button @click="dialogVisible = false">{{ $t('table.cancel') }}</el-button>
        <el-button
          :disabled="!titleValid"
          type="primary"
          @click="sureCurTitle"
        >{{ $t('table.confirm') }}</el-button>
      </span>
    </el-dialog>

    <el-dialog
      :title="$t('detabs.selectview')"
      :append-to-body="true"
      :visible.sync="viewDialogVisible"
      width="20%"
      height="400px"
      :show-close="false"
      :close-on-click-modal="false"
      center
    >
      <div style="width: 100%;min-height: 250px; max-height: 300px; overflow-y: auto;">
        <view-select
          v-if="viewDialogVisible"
          ref="viewSelect"
          :select-model="true"
        />
      </div>

      <span
        slot="footer"
        class="dialog-footer"
      >
        <el-button @click="viewDialogVisible = false">{{ $t('table.cancel') }}</el-button>
        <el-button
          type="primary"
          @click="sureViewSelector"
        >{{ $t('table.confirm') }}</el-button>
      </span>
    </el-dialog>

    <el-dialog
      :title="$t('detabs.availableComponents')"
      :append-to-body="true"
      :visible.sync="otherComponentDialogVisible"
      width="300px"
      height="250px"
      :show-close="false"
      :close-on-click-modal="false"
      center
    >
      <tab-use-list
        v-if="otherComponentDialogVisible"
        ref="otherComponentSelect"
      />
      <span
        slot="footer"
        class="dialog-footer"
      >
        <el-button @click="otherComponentDialogVisible = false">{{ $t('table.cancel') }}</el-button>
        <el-button
          type="primary"
          @click="sureOtherComponentSelector"
        >{{ $t('table.confirm') }}</el-button>
      </span>
    </el-dialog>

    <text-attr
      v-if="showAttr && curComponent.canvasId === activeCanvasId && isEdit"
      :canvas-id="curComponent.canvasId"
      :scroll-left="scrollLeft"
      :scroll-top="scrollTop"
    />

  </div>

</template>

<script>
import DataeaseTabs from '@/components/dataeaseTabs'
import ViewSelect from '@/views/panel/viewSelect'
import { uuid } from 'vue-uuid'
import bus from '@/utils/bus'
import componentList from '@/components/canvas/customComponent/component-list'
import { mapState } from 'vuex'
import { chartCopy } from '@/api/chart/chart'
import { buildFilterMap } from '@/utils/conditionUtil'
import TabUseList from '@/views/panel/assistComponent/TabUseList'
import { findPanelElementInfo } from '@/api/panel/panel'
import { getNowCanvasComponentData } from '@/components/canvas/utils/utils'
import DeCanvasTab from '@/components/canvas/DeCanvas'
import Preview from '@/components/canvas/components/editor/Preview'
import TextAttr from '@/components/canvas/components/TextAttr'

export default {
  name: 'DeTabs',
  components: { TextAttr, Preview, DeCanvasTab, TabUseList, ViewSelect, DataeaseTabs },
  props: {
    inScreen: {
      type: Boolean,
      required: false,
      default: true
    },
    canvasId: {
      type: String,
      default: 'canvas-main'
    },
    element: {
      type: Object,
      default: null
    },
    isEdit: {
      type: Boolean,
      default: true
    },
    active: {
      type: Boolean,
      default: false
    },
    outStyle: {
      type: Object,
      required: false,
      default: function() {
        return {}
      }
    },
    editMode: {
      type: String,
      require: false,
      default: 'edit'
    },
    h: {
      type: Number,
      default: 200
    },
    showPosition: {
      type: String,
      required: false,
      default: 'NotProvided'
    }
  },
  data() {
    return {
      tabsAreaScroll: false,
      timer: null,
      scrollLeft: 50,
      scrollTop: 10,
      // 需要展示属性设置的组件类型
      showAttrComponent: [
        'custom',
        'v-text',
        'picture-add',
        'de-tabs',
        'rect-shape',
        'de-show-date',
        'de-video',
        'de-stream-media',
        'de-frame'
      ],
      activeTabName: null,
      tabIndex: 1,
      dialogVisible: false,
      textarea: '',
      curItem: null,
      viewDialogVisible: false,
      otherComponentDialogVisible: false,
      url: '/api/pluginCommon/component/dataease-tabs'

    }
  },
  computed: {
    activeCanvasId() {
      return this.element.id + '-' + this.activeTabName
    },
    maskShow() {
      return Boolean(this.$store.state.dragComponentInfo)
    },
    headClass() {
      if(this.tabsAreaScroll){
        return 'tab-head-left'
      }else{
        return 'tab-head-' + this.element.style.headPosition
      }
    },
    curCanvasScaleSelf() {
      return this.curCanvasScaleMap[this.canvasId]
    },
    showAttr() {
      if (this.mobileLayoutStatus) {
        return false
      } else if (this.curComponent && this.showAttrComponent.includes(this.curComponent.type)) {
        // 过滤组件有标题才显示
        if (this.curComponent.type === 'custom' && (!this.curComponent.options.attrs.showTitle || !this.curComponent.options.attrs.title)) {
          return false
        } else {
          return true
        }
      } else {
        return false
      }
    },
    moveActive() {
      return this.tabMoveInActiveId && this.tabMoveInActiveId === this.element.id
    },
    tabH() {
      return this.h - 50
    },
    dropdownShow() {
      return this.isEdit && !this.mobileLayoutStatus
    },
    panelInfo() {
      return this.$store.state.panel.panelInfo
    },
    filterMap() {
      const map = buildFilterMap(this.componentData)
      return map
    },
    ...mapState([
      'componentData',
      'curComponent',
      'mobileLayoutStatus',
      'canvasStyleData',
      'tabMoveInActiveId',
      'curCanvasScaleMap',
      'pcComponentData'
    ]),
    fontColor() {
      if (this.element && this.element.style && this.element.style.headFontColor && typeof this.element.style.headFontColor === 'string') {
        return this.element.style.headFontColor
      } else {
        return 'none'
      }
    },
    activeColor() {
      if (this.element && this.element.style && this.element.style.headFontActiveColor && typeof this.element.style.headFontActiveColor === 'string') {
        return this.element.style.headFontActiveColor
      } else {
        return 'none'
      }
    },
    borderColor() {
      if (this.element && this.element.style && this.element.style.headBorderColor && typeof this.element.style.headBorderColor === 'string') {
        return this.element.style.headBorderColor
      } else {
        return 'none'
      }
    },
    borderActiveColor() {
      if (this.element && this.element.style && this.element.style.headBorderActiveColor && typeof this.element.style.headBorderActiveColor === 'string') {
        return this.element.style.headBorderActiveColor
      } else {
        return 'none'
      }
    },
    titleValid() {
      return !!this.textarea && !!this.textarea.trim()
    },
    isCurrentEdit() {
      return this.isEdit && this.curComponent && this.curComponent.id === this.element.id
    },
    themeStyle() {
      return this.element.commonBackground
    }
  },
  watch: {
    'element.style.carouselEnable': {
      handler(newVal, oldVla) {
        this.initCarousel()
      }
    },
    'element.style.switchTime': {
      handler(newVal, oldVla) {
        this.initCarousel()
      }
    },
    'element': {
      handler(newVal, oldVla) {
        this.calcTabLength()
      }
    },
    activeTabName: {
      handler(newVal, oldVla) {
        this.$store.commit('setTabActiveTabNameMap', { tabId: this.element.id, activeTabName: this.activeTabName })
        const _this = this
        _this.$nextTick(() => {
          try {
            const targetRef = _this.$refs['canvasTabRef-' + _this.activeTabName]
            if (targetRef) {
              targetRef[0]?.restore()
            }
            _this.$refs[this.activeTabName][0].resizeChart()
          } catch (e) {
            // ignore
          }
        })
        bus.$emit('tab-canvas-change', this.activeCanvasId)
      }
    },
    active: {
      handler(newVal, oldVla) {
        let activeTabInner
        this.element.options.tabList.forEach(item => {
          if (item && item.name === this.activeTabName && item.content) {
            activeTabInner = item.content
          }
        })
        if (newVal && activeTabInner && activeTabInner.type === 'view') {
          this.$store.commit('setCurActiveTabInner', activeTabInner)
          this.$store.dispatch('chart/setViewId', activeTabInner.propValue.viewId)
        } else {
          this.$store.commit('setCurActiveTabInner', null)
        }
      }
    },
    'themeStyle.color'(value, old) {
      if (value !== old) {
        this.setContentThemeStyle()
      }
    },
    'themeStyle.backgroundColorSelect'(value, old) {
      if (value !== old) {
        this.setContentThemeStyle()
      }
    }
  },
  created() {
    bus.$on('add-new-tab', this.addNewTab)
    this.$nextTick(() => {
      this.activeTabName = this.element.options.tabList[0].name
    });
    this.$store.commit('setTabActiveTabNameMap', { tabId: this.element.id, activeTabName: this.activeTabName })
    this.setContentThemeStyle()
  },
  mounted() {
    this.initCarousel()
    this.calcTabLength()
  },
  beforeDestroy() {
    bus.$off('add-new-tab', this.addNewTab)
  },
  methods: {
    calcTabLength(){
      this.$nextTick(()=>{
        if(this.element.options.tabList.length>1){
          const containerDom = document.getElementById("tab-"+this.element.options.tabList[this.element.options.tabList.length -1].name)
          this.tabsAreaScroll = containerDom.parentNode.scrollWidth > containerDom.parentNode.parentNode.scrollWidth
        }else{
          this.tabsAreaScroll = false
        }
      })
    },
    getType() {
      return this.element.type
    },
    getWrapperChildRefs() {
      let refsSubAll = []
      const _this = this
      this.element.options.tabList.forEach(tabItem => {
        const refsSub = _this.$refs['canvasTabRef-' + tabItem.name]
        if (refsSub && refsSub.length) {
          const refsSubArray = refsSub[0].getWrapperChildRefs()
          if (refsSubArray && refsSubArray.length > 0) {
            refsSubAll.push.apply(refsSubAll, refsSubArray)
          }
        }
      })
      return refsSubAll
    },
    titleStyle(itemName) {
      if (this.activeTabName === itemName) {
        return {
          fontSize: (this.element.style.activeFontSize || 18) + 'px'
        }
      } else {
        return {
          fontSize: (this.element.style.fontSize || 16) + 'px'
        }
      }
    },
    initCarousel() {
      const _this = this
      _this.timer && clearInterval(_this.timer)
      if (_this.element.style.carouselEnable) {
        const switchTime = (_this.element.style.switchTime || 5) * 1000
        let switchCount = 1
        // 轮播定时器
        _this.timer = setInterval(() => {
          const nowIndex = switchCount % _this.element.options.tabList.length
          switchCount++
          _this.$nextTick(() => {
            _this.activeTabName = _this.element.options.tabList[nowIndex].name
            const targetRef = _this.$refs['canvasTabRef-' + _this.activeTabName]
            if (targetRef) {
              targetRef[0]?.restore()
            }
          });
        }, switchTime)
      }
    },
    initScroll() {
      this.scrollLeft = 50
      this.scrollTop = 10
    },
    canvasScroll(scrollInfo) {
      this.scrollLeft = scrollInfo.scrollLeft + 50
      this.scrollTop = scrollInfo.scrollTop + 10
      bus.$emit('onScroll')
    },
    tabCanvasComponentData(tabName) {
      const tabCanvasId = this.element.id + '-' + tabName
      if (this.mobileLayoutStatus) {
        return this.pcComponentData.filter(item => item.canvasId === tabCanvasId)
      } else {
        return getNowCanvasComponentData(tabCanvasId, this.showPosition)
      }
    },
    setContentThemeStyle() {
      this.element.options.tabList.forEach(tab => {
        if (tab.content && tab.content.type === 'view') {
          tab.content.commonBackground = this.themeStyle ? JSON.parse(JSON.stringify(this.themeStyle)) : null
        }
      })
    },
    beforeHandleCommand(item, param) {
      return {
        'command': item,
        'param': param
      }
    },
    handleCommand(command) {
      switch (command.command) {
        case 'editTitle':
          this.editCurTitle(command.param)
          break
        case 'deleteCur':
          this.deleteCur(command.param)
          break
        case 'selectView':
          this.selectView(command.param)
          break
        case 'selectOthers':
          this.selectOthers(command.param)
          break
      }
    },
    selectView(param) {
      this.activeTabName = param.name
      this.curItem = param
      this.viewDialogVisible = true
    },
    selectOthers(param) {
      this.activeTabName = param.name
      this.curItem = param
      this.otherComponentDialogVisible = true
    },
    sureOtherComponentSelector() {
      const curSelectedId = this.$refs.otherComponentSelect.getCurSelectedComponent()
      if (curSelectedId) {
        let component
        const newComponentId = uuid.v1()
        componentList.forEach(componentTemp => {
          if (componentTemp.id === curSelectedId) {
            component = JSON.parse(JSON.stringify(componentTemp))
            component.style.width = '100%'
            component.style.height = '100%'
            this.curItem.content = component
            this.curItem.name = newComponentId
            this.activeTabName = newComponentId
            this.$store.commit('setCurActiveTabInner', component)
            this.styleChange()
          }
        })
        this.otherComponentDialogVisible = false
        return
      }
      this.$warning(this.$t('detabs.please') + this.$t('detabs.selectOthers'))
    },
    sureViewSelector() {
      const nodes = this.$refs.viewSelect.getCurrentSelected()
      if (!nodes || nodes.length === 0) {
        this.$warning(this.$t('detabs.please') + this.$t('detabs.selectview'))

        return
      }
      const node = nodes[0]

      let component
      const newComponentId = uuid.v1()
      const componentInfo = {
        type: 'view',
        id: node.innerId
      }

      componentList.forEach(componentTemp => {
        if (componentTemp.type === 'view') {
          component = JSON.parse(JSON.stringify(componentTemp))
          const propValue = {
            id: newComponentId,
            viewId: componentInfo.id,
            textValue: ''
          }
          component.propValue = propValue
          component.filters = []
          component.linkageFilters = []
          if (this.themeStyle) {
            component.commonBackground = JSON.parse(JSON.stringify(this.themeStyle))
          }
        }
      })
      component.id = newComponentId
      component.style.width = '100%'
      component.style.height = '100%'
      chartCopy(node.innerId, this.panelInfo.id).then(res => {
        component.propValue.viewId = res.data
        this.curItem.content = component
        this.curItem.name = newComponentId
        this.viewDialogVisible = false
        this.activeTabName = newComponentId
        if (node.modelInnerType === 'richTextView') {
          findPanelElementInfo(node.innerId).then(viewElement => {
            if (viewElement.data) {
              this.curItem.content.propValue.textValue = viewElement.data.propValue.textValue
            }
            this.$store.dispatch('chart/setViewId', component.propValue.viewId)
            this.styleChange()
          })
        } else {
          this.$store.dispatch('chart/setViewId', component.propValue.viewId)
          this.styleChange()
        }
      })
    },

    setComponentInfo() {
    },

    editCurTitle(param) {
      this.activeTabName = param.name
      this.curItem = param
      this.textarea = param.title
      this.dialogVisible = true
    },
    sureCurTitle() {
      this.curItem.title = this.textarea
      this.dialogVisible = false
      this.styleChange()
    },
    deleteCur(param) {
      this.curItem = param
      let len = this.element.options.tabList.length
      while (len--) {
        if (this.element.options.tabList[len].name === param.name) {
          this.element.options.tabList.splice(len, 1)
          this.$store.commit('deleteComponentsWithCanvasId', this.element.id + '-' + param.name)
          const activeIndex = (len - 1 + this.element.options.tabList.length) % this.element.options.tabList.length
          this.activeTabName = this.element.options.tabList[activeIndex].name
        }
      }
      this.$store.dispatch('chart/setViewId', null)
      this.styleChange()
    },
    addTab() {
      this.addNewTab(this.element.id)
    },

    addNewTab(componentId) {
      if (!componentId || componentId !== this.element.id) return
      const curName = uuid.v1()
      const tab = {
        title: 'NewTab',
        name: curName,
        content: { type: 'canvas' }
      }
      // 的Tab都是画布

      this.element.options.tabList.push(tab)

      this.styleChange()
    },
    styleChange() {
      this.$store.commit('canvasChange')
    },
    chartResize() {
    },
    handleClick(tab) {
      const name = tab.name
      this.element.options.tabList.forEach(item => {
        if (item && item.name === name && item.content) {
          this.$store.commit('setCurActiveTabInner', item.content)
          if (item.content.propValue && item.content.propValue.viewId) {
            this.filterMap[item.content.propValue.viewId] = item.content.filters
            this.$store.dispatch('chart/setViewId', item.content.propValue.viewId)
          }
        }
      })
    }
  }
}
</script>

<style lang="scss" scoped>

.de-tabs-div {
  height: 100%;
  overflow: hidden;
}

.de-tabs-height {
  height: 100%;
}

.de-tab-content {
  width: 100%;
  height: 100%;
}

.tab_canvas {
  height: calc(100% - 5px);
  border: 2px dotted transparent;
}

.canvas_move_in {
  border-color: blueviolet;
}

::v-deep .el-tabs__nav-prev {
  line-height: 25px;
}

::v-deep .el-tabs__nav-next {
  line-height: 25px;
}

.tab-head-left ::v-deep .el-tabs__nav-scroll {
  display: flex;
  justify-content: flex-start;
}

.tab-head-right ::v-deep .el-tabs__nav-scroll {
  display: flex;
  justify-content: flex-end;
}

.tab-head-center ::v-deep .el-tabs__nav-scroll {
  display: flex;
  justify-content: center;
}

.frame-mask {
  display: flex;
  opacity: 0;
  position: absolute;
  top: 0px;
  z-index: 1;
  display: flex;
  align-items: center;
  justify-content: center;
}

.edit-mask {
  left: 0px;
  height: 100% !important;
  width: 100% !important;
}

::v-deep .el-tabs__nav-wrap.is-scrollable {
padding: 0 45px!important;
}

::v-deep .el-tabs__nav-prev {
left: 25px!important
}

::v-deep .el-tabs__nav-next {
right: 25px!important;
}

</style>
