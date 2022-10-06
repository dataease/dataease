<template>
  <div class="de-tabs-div">

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
      :addable="isEdit"
      @tab-add="addTab"
      @tab-click="handleClick"
    >

      <el-tab-pane
        v-for="(item, index) in element.options.tabList"
        :key="item.name+index"
        :lazy="false"
        :name="item.name"
      >
        <span slot="label">
          <span>{{ item.title }}</span>

          <el-dropdown v-if="dropdownShow" slot="label" class="de-tab-drop" trigger="click" @command="handleCommand">
            <span class="el-dropdown-link">

              <i v-if="isEdit" class="de-tab-i el-icon-arrow-down el-icon--right" />
            </span>

            <el-dropdown-menu slot="dropdown">
              <el-dropdown-item :command="beforeHandleCommond('editTitle', item)">
                {{ $t('detabs.eidttitle') }}
              </el-dropdown-item>

              <el-dropdown-item :command="beforeHandleCommond('selectView', item)">
                {{ $t('detabs.selectview') }}
              </el-dropdown-item>

              <el-dropdown-item :command="beforeHandleCommond('selectOthers', item)">
                {{ $t('detabs.selectOthers') }}
              </el-dropdown-item>

              <el-dropdown-item
                v-if=" element.options.tabList.length > 1"
                :command="beforeHandleCommond('deleteCur', item)"
              >
                {{ $t('table.delete') }}
              </el-dropdown-item>

            </el-dropdown-menu>
          </el-dropdown>
        </span>
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
      <span slot="footer" class="dialog-footer">
        <el-button @click="dialogVisible = false">{{ $t('table.cancel') }}</el-button>
        <el-button :disabled="!titleValid" type="primary" @click="sureCurTitle">{{ $t('table.confirm') }}</el-button>
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
        <view-select v-if="viewDialogVisible" ref="viewSelect" :select-model="true" />
      </div>

      <span slot="footer" class="dialog-footer">
        <el-button @click="viewDialogVisible = false">{{ $t('table.cancel') }}</el-button>
        <el-button type="primary" @click="sureViewSelector">{{ $t('table.confirm') }}</el-button>
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
      <tab-use-list v-if="otherComponentDialogVisible" ref="otherComponentSelect" />
      <span slot="footer" class="dialog-footer">
        <el-button @click="otherComponentDialogVisible = false">{{ $t('table.cancel') }}</el-button>
        <el-button type="primary" @click="sureOtherComponentSelector">{{ $t('table.confirm') }}</el-button>
      </span>
    </el-dialog>

  </div>

</template>

<script>
import DataeaseTabs from '@/components/dataease-tabs'
import ViewSelect from '@/views/panel/ViewSelect'
import { uuid } from 'vue-uuid'
import bus from '@/utils/bus'
import componentList from '@/components/canvas/custom-component/component-list'
import { mapState } from 'vuex'
import { chartCopy } from '@/api/chart/chart'
import { buildFilterMap } from '@/utils/conditionUtil'
import TabUseList from '@/views/panel/AssistComponent/tabUseList'
import {findPanelElementInfo} from "@/api/panel/panel";

export default {
  name: 'DeTabls',
  components: { TabUseList, ViewSelect, DataeaseTabs },
  props: {
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
      'canvasStyleData'
    ]),
    fontColor() {
      return this.element && this.element.style && this.element.style.headFontColor || 'none'
    },
    activeColor() {
      return this.element && this.element.style && this.element.style.headFontActiveColor || 'none'
    },
    borderColor() {
      return this.element && this.element.style && this.element.style.headBorderColor || 'none'
    },
    borderActiveColor() {
      return this.element && this.element.style && this.element.style.headBorderActiveColor || 'none'
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
    activeTabName: {
      handler(newVal, oldVla) {
        const _this = this
        _this.$nextTick(() => {
          try {
            _this.$refs[this.activeTabName][0].resizeChart()
          } catch (e) {
            // ignore
          }
        })
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
        if (newVal && activeTabInner) {
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
    this.activeTabName = this.element.options.tabList[0].name
    this.setContentThemeStyle()
  },
  beforeDestroy() {
    bus.$off('add-new-tab', this.addNewTab)
  },
  methods: {
    setContentThemeStyle() {
      this.element.options.tabList.forEach(tab => {
        if(tab.content && tab.content.type === 'view') {
          tab.content.commonBackground = this.themeStyle ? JSON.parse(JSON.stringify(this.themeStyle)) : null
        }
      })
    },
    beforeHandleCommond(item, param) {
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
          if(this.themeStyle)
          component.commonBackground = JSON.parse(JSON.stringify(this.themeStyle))
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
        if(node.modelInnerType==='richTextView'){
          findPanelElementInfo(node.innerId).then(viewElement =>{
            if(viewElement.data){
              this.curItem.content.propValue.textValue = viewElement.data.propValue.textValue
            }
            this.$store.dispatch('chart/setViewId', component.propValue.viewId)
            this.styleChange()
          })
        }else{
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

          const activIndex = (len - 1 + this.element.options.tabList.length) % this.element.options.tabList.length
          this.activeTabName = this.element.options.tabList[activIndex].name
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
        content: null
      }
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

</style>
