<template>
  <div>
    <el-tabs v-model="editableTabsValue" type="card" addable @edit="handleTabsEdit">
      <el-tab-pane
        v-for="(item, index) in tabList"
        :key="item.name+index"
        :name="item.name"
      >

        <el-dropdown slot="label" :disabled="!isEdit" class="de-tab-drop" trigger="click" @command="handleCommand">
          <span class="el-dropdown-link">

            <span>{{ item.title }}</span>
            <i v-if="isEdit" class="de-tab-i el-icon-arrow-down el-icon--right" />
          </span>

          <el-dropdown-menu slot="dropdown">
            <el-dropdown-item :command="beforeHandleCommond('editTitle', item)">
              编辑标题
            </el-dropdown-item>

            <el-dropdown-item :command="beforeHandleCommond('selectView', item)">
              选择视图
            </el-dropdown-item>

            <el-dropdown-item :command="beforeHandleCommond('deleteCur', item)">
              删除
            </el-dropdown-item>

          </el-dropdown-menu>
        </el-dropdown>

        <div class="de-tab-content">
          <user-view v-if="item.viewInfo" :element="item.viewInfo" />
        </div>

      </el-tab-pane>
    </el-tabs>

    <el-dialog
      title="编辑标题"
      :append-to-body="true"
      :visible.sync="dialogVisible"
      width="30%"
      center
    >
      <el-input
        v-model="textarea"
        type="textarea"
        :rows="2"
        placeholder="请输入内容"
      />
      <span slot="footer" class="dialog-footer">
        <el-button @click="dialogVisible = false">取 消</el-button>
        <el-button type="primary" @click="sureCurTitle">确 定</el-button>
      </span>
    </el-dialog>

    <el-dialog
      title="选择视图"
      :append-to-body="true"
      :visible.sync="viewDialogVisible"
      width="20%"
      height="400px"
      center
    >
      <div style="width: 100%;min-height: 250px; max-height: 400px;">
        <view-select ref="viewSelect" :select-model="true" />
      </div>

      <span slot="footer" class="dialog-footer">
        <el-button @click="viewDialogVisible = false">取 消</el-button>
        <el-button type="primary" @click="sureViewSelector">确 定</el-button>
      </span>
    </el-dialog>

  </div>

</template>

<script>

import ViewSelect from '@/views/panel/ViewSelect'
import { uuid } from 'vue-uuid'
import componentList from '@/components/canvas/custom-component/component-list'
export default {
  name: 'DeTabls',
  components: { ViewSelect },
  props: {
    element: {
      type: Object,
      default: null
    }
  },
  data() {
    return {

      editableTabsValue: '1',
      editableTabs: [{
        title: 'Tab 1',
        name: '1',
        content: ''
      }],
      tabIndex: 1,
      isEdit: true,
      dialogVisible: false,
      textarea: '',
      curItem: null,
      viewDialogVisible: false,
      curViewInfo: null,
      tabList: [],
      options: null
    }
  },
  created() {
    this.options = this.element.options
    this.tabList = this.options && this.options.tabList || this.editableTabs
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
    sureViewSelector() {
      const nodes = this.$refs.viewSelect.getCurrentSelected()
      const node = nodes[0]

      let component
      const newComponentId = uuid.v1()
      const componentInfo = {
        type: 'view',
        id: node.id
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
      this.curItem.viewInfo = component
      this.curItem.name = newComponentId
      this.viewDialogVisible = false
    },

    editCurTitle(param) {
      this.curItem = param
      this.textarea = param.title
      this.dialogVisible = true
    },
    sureCurTitle() {
      this.curItem.title = this.textarea
      this.dialogVisible = false
    },
    deleteCur(param) {

    },
    selectView(param) {
      this.curItem = param
      this.viewDialogVisible = true
    },

    handleTabsEdit(targetName, action) {
      if (action === 'add') {
        const newTabName = ++this.tabIndex + ''
        this.editableTabs.push({
          title: 'New Tab',
          name: newTabName,
          content: 'New Tab content'
        })
        this.editableTabsValue = newTabName
      }
      if (action === 'remove') {
        const tabs = this.editableTabs
        let activeName = this.editableTabsValue
        if (activeName === targetName) {
          tabs.forEach((tab, index) => {
            if (tab.name === targetName) {
              const nextTab = tabs[index + 1] || tabs[index - 1]
              if (nextTab) {
                activeName = nextTab.name
              }
            }
          })
        }

        this.editableTabsValue = activeName
        this.editableTabs = tabs.filter(tab => tab.name !== targetName)
      }
    }
  }
}
</script>

<style lang="scss" scoped>
  .de-tab-i {
    transition: 0.3s;
    opacity: 0;
    transform:  translateY(100%);
  }

  .de-tab-drop:hover .de-tab-i {
    opacity: 1;
    transform:  translateY(0);
  }
  .de-tab-content {
      width: 100%;
      height: 100%;
  }

</style>
