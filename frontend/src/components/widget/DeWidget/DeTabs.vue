<template>
  <div class="de-tabs-div">
    <async-solt-component
      v-model="activeTabName"
      :url="url"
      type="card"
      style-type="radioGroup"
      class="de-tabs-height"
      :font-color="fontColor"
      :active-color="activeColor"
      :border-color="borderColor"
      :border-active-color="borderActiveColor"
      @tab-click="handleClick"
    >
      <!--  <plugin-com ref="dataease-tabs" v-model="activeTabName" type="card" class="de-tabs" component-name="dataease-tabs" @tab-click="handleClick"> -->
      <!-- <el-tabs v-model="activeTabName" type="card" class="de-tabs" @tab-click="handleClick"> -->
      <el-tab-pane
        v-for="(item, index) in element.options.tabList"
        :key="item.name+index"
        :lazy="true"
        :name="item.name"
      >
        <span slot="label">
          <span>{{ item.title }}</span>

          <el-dropdown v-if="dropdownShow" slot="label" class="de-tab-drop" trigger="click" @command="handleCommand">
            <span class="el-dropdown-link">

              <!-- <span>{{ item.title }}</span> -->
              <i v-if="isEdit" class="de-tab-i el-icon-arrow-down el-icon--right" />
            </span>

            <el-dropdown-menu slot="dropdown">
              <el-dropdown-item :command="beforeHandleCommond('editTitle', item)">
                {{ $t('detabs.eidttitle') }}
              </el-dropdown-item>

              <el-dropdown-item :command="beforeHandleCommond('selectView', item)">
                {{ $t('detabs.selectview') }}
              </el-dropdown-item>

              <el-dropdown-item v-if=" element.options.tabList.length > 1" :command="beforeHandleCommond('deleteCur', item)">
                {{ $t('table.delete') }}
              </el-dropdown-item>

            </el-dropdown-menu>
          </el-dropdown>
        </span>

        <div v-if="activeTabName === item.name" class="de-tab-content">
          <!-- <user-view
            v-if="item.content && item.content.propValue && item.content.propValue.viewId"
            :ref="item.name"
            :in-tab="true"
            :is-edit="isEdit"
            :active="active"
            :element="item.content"
            :filters="item.content.filters"
            :out-style="outStyle"
          /> -->
          <user-view
            v-if="item.content && item.content.propValue && item.content.propValue.viewId"
            :ref="item.name"
            :in-tab="true"
            :is-edit="isEdit"
            :active="active"
            :element="item.content"
            :filters="filterMap[item.content.propValue && item.content.propValue.viewId] || []"
            :out-style="outStyle"
          />
        </div>

      </el-tab-pane>
    </async-solt-component>
    <!-- </el-tabs> -->

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

  </div>

</template>

<script>
import AsyncSoltComponent from '@/components/AsyncSoltComponent'
import ViewSelect from '@/views/panel/ViewSelect'
import { uuid } from 'vue-uuid'
import bus from '@/utils/bus'
import componentList from '@/components/canvas/custom-component/component-list'
import { mapState } from 'vuex'
import { chartCopy } from '@/api/chart/chart'
import { buildFilterMap } from '@/utils/conditionUtil'
export default {
  name: 'DeTabls',
  components: { ViewSelect, AsyncSoltComponent },
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
    }
  },
  data() {
    return {

      activeTabName: null,

      tabIndex: 1,
      // isEdit: true,
      dialogVisible: false,
      textarea: '',
      curItem: null,
      viewDialogVisible: false,
      url: '/api/pluginCommon/component/dataease-tabs'
      /* fontColor: '#999999',
      activeColor: '#f18406',

      borderColor: '#999999',
      borderActiveColor: '#f18406' */

    }
  },
  computed: {
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
      'mobileLayoutStatus'
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
    }
  },
  watch: {
    curComponent: {
      handler(newVal, oldVla) {
      },
      deep: true
    }
  },
  created() {
    bus.$on('add-new-tab', this.addNewTab)
    this.activeTabName = this.element.options.tabList[0].name
  },
  methods: {
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
      }
    },
    selectView(param) {
      this.activeTabName = param.name
      this.curItem = param
      this.viewDialogVisible = true
    },
    sureViewSelector() {
      const nodes = this.$refs.viewSelect.getCurrentSelected()
      if (!nodes || nodes.length === 0) {
        this.viewDialogVisible = false
        return
      }
      const node = nodes[0]

      let component
      const newComponentId = uuid.v1()
      const componentInfo = {
        type: 'view',
        /* id: node.id */
        id: node.innerId
      }

      componentList.forEach(componentTemp => {
        if (componentTemp.type === 'view') {
          component = JSON.parse(JSON.stringify(componentTemp))
          const propValue = {
            id: newComponentId,
            viewId: componentInfo.id
          }
          component.propValue = propValue
          component.filters = []
          component.linkageFilters = []
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
        this.$store.dispatch('chart/setViewId', component.propValue.viewId)
        this.styleChange()
      })
      // this.setComponentInfo()
    },

    setComponentInfo() {
      console.log('aaa')
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
      this.$store.state.styleChangeTimes++
    },
    chartResize() {
      // this.$refs[this.activeTabName]
    },
    handleClick(tab) {
      const name = tab.name
      this.element.options.tabList.forEach(item => {
        if (item && item.name === name && item.content && item.content.propValue && item.content.propValue.viewId) {
          this.filterMap[item.content.propValue.viewId] = item.content.filters
          this.$store.dispatch('chart/setViewId', item.content.propValue.viewId)
        }
      })
      // console.log(tab)
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
