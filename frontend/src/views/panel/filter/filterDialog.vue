<template>

  <de-container v-loading="$store.getters.loadingMap[$store.getters.currentPath]" class="de-dialog-container">
    <de-aside-container :show-drag-bar="false" class="ms-aside-container">
      <el-tabs v-model="activeName" class="filter-dialog-tabs">
        <el-tab-pane class="de-tab" :label="$t('panel.select_by_table')" name="dataset">
          <div class="component-header filter-common">
            <el-breadcrumb separator-class="el-icon-arrow-right">
              <el-breadcrumb-item v-for="bread in dataSetBreads" :key="bread.label">
                <a v-if="bread.link" :class="{'link-text' : bread.link}" @click="backToLink(bread)">
                  {{ bread.label }}</a>
                <span v-else>{{ bread.label }}</span>
              </el-breadcrumb-item>
            </el-breadcrumb>
          </div>
          <div class="component-result-content filter-common">
            <el-col>
              <el-row>
                <el-form>
                  <el-form-item class="my-form-item">
                    <el-input
                      v-model="keyWord"
                      size="mini"
                      :placeholder="$t('dataset.search')"
                      prefix-icon="el-icon-search"
                      clearable
                    />
                  </el-form-item>
                </el-form>
              </el-row>
              <el-row>
                <el-tree
                  v-if="showDomType === 'tree'"
                  :default-expanded-keys="expandedArray"
                  node-key="id"
                  :data="datas"
                  :props="defaultProps"
                  lazy
                  :load="loadTree"
                  @node-click="handleNodeClick"
                >
                  <div slot-scope="{ node, data }" class="custom-tree-node">
                    <span>
                      <svg-icon v-if="data.type === 'db'" icon-class="ds-db" class="ds-icon-db" />
                      <svg-icon v-if="data.type === 'sql'" icon-class="ds-sql" class="ds-icon-sql" />
                      <svg-icon v-if="data.type === 'excel'" icon-class="ds-excel" class="ds-icon-excel" />
                      <svg-icon v-if="data.type === 'custom'" icon-class="ds-custom" class="ds-icon-custom" />
                      <svg-icon v-if="data.type === 'union'" icon-class="ds-union" class="ds-icon-union" />
                      <svg-icon v-if="data.type === 'api'" icon-class="ds-api" class="ds-icon-api" />
                    </span>
                    <el-tooltip class="item" effect="dark" placement="top">
                      <div slot="content">{{ node.label }}</div>
                      <span class="label-span">{{ node.label }}</span>
                    </el-tooltip>

                  </div>
                </el-tree>

                <div v-if="showDomType === 'field'">
                  <draggable
                    v-model="fieldDatas"
                    :options="{group:{name: 'dimension',pull:'clone'},sort: true}"
                    animation="300"
                    :move="onMove"
                    class="drag-list"
                    @end="end"
                  >
                    <transition-group>
                      <div
                        v-for="item in fieldDatas.filter(item => !keyWord || (item.name && item.name.toLocaleLowerCase().includes(keyWord)))"
                        :key="item.id"
                        :class="myAttrs && myAttrs.fieldId && myAttrs.fieldId.includes(item.id) ? 'filter-db-row-checked' : 'filter-db-row'"
                        class="filter-db-row"
                      >
                        <i class="el-icon-s-data" />
                        <span> {{ item.name }}</span>
                      </div>
                    </transition-group>
                  </draggable>
                </div>
              </el-row>
            </el-col>
          </div>
        </el-tab-pane>
        <el-tab-pane class="de-tab" :label="$t('panel.select_by_module')" name="assembly">
          <div class="component-header filter-common">
            <el-breadcrumb separator-class="el-icon-arrow-right">
              <el-breadcrumb-item v-for="bread in componentSetBreads" :key="bread.label">
                <a v-if="bread.link" :class="{'link-text' : bread.link}" @click="comBackLink(bread)">
                  {{ bread.label }}</a>
                <span v-else>{{ bread.label }}</span>
              </el-breadcrumb-item>
            </el-breadcrumb>
          </div>

          <div class="component-result-content filter-common">

            <el-col>
              <el-row>
                <el-form>
                  <el-form-item class="my-form-item">
                    <el-input
                      v-model="viewKeyWord"
                      size="mini"
                      :placeholder="$t('dataset.search')"
                      prefix-icon="el-icon-search"
                      clearable
                    />
                  </el-form-item>
                </el-form>
              </el-row>
              <el-row>
                <el-table
                  v-if="comShowDomType === 'view'"
                  class="de-filter-data-table"
                  :data="viewInfos.filter(item => !viewKeyWord || item.name.toLocaleLowerCase().includes(viewKeyWord))"
                  :show-header="false"
                  size="mini"
                  :highlight-current-row="true"
                  style="width: 100%"
                >
                  <el-table-column prop="name" :label="$t('commons.name')">
                    <template v-if="comShowDomType === 'view'" :id="scope.row.id" slot-scope="scope">
                      <div class="filter-db-row" @click="comShowFieldDatas(scope.row)">
                        <i class="el-icon-s-data" />
                        <span> {{ scope.row.name }}</span>
                      </div>
                    </template>
                  </el-table-column>
                </el-table>

                <div v-else-if="comShowDomType === 'field'">
                  <draggable
                    v-model="comFieldDatas"
                    :options="{group:{name: 'dimension',pull:'clone'},sort: true}"
                    animation="300"
                    :move="onMove"
                    class="drag-list"
                    @end="end"
                  >
                    <transition-group>
                      <div
                        v-for="item in comFieldDatas.filter(item => !viewKeyWord || item.name.toLocaleLowerCase().includes(viewKeyWord))"
                        :key="item.id"
                        :class="myAttrs && myAttrs.fieldId && myAttrs.fieldId.includes(item.id) ? 'filter-db-row-checked' : 'filter-db-row'"
                        class="filter-db-row"
                      >
                        <i class="el-icon-s-data" />
                        <span> {{ item.name }}</span>
                      </div>
                    </transition-group>
                  </draggable>
                </div>
              </el-row>
            </el-col>
          </div>
        </el-tab-pane>
      </el-tabs>
    </de-aside-container>

    <de-main-container class="ms-main-container">
      <div v-if="currentElement.options && currentElement.options.attrs">
        <filter-head :element="currentElement" />

        <filter-control :element="currentElement" :widget="widget" :control-attrs="myAttrs" :child-views="childViews" />

        <filter-foot :element="currentElement" />

      </div>
    </de-main-container>
  </de-container>

</template>
<script>
import DeMainContainer from '@/components/dataease/DeMainContainer'
import DeContainer from '@/components/dataease/DeContainer'
import DeAsideContainer from '@/components/dataease/DeAsideContainer'
import draggable from 'vuedraggable'
import FilterHead from './filterMain/FilterHead'
import FilterControl from './filterMain/FilterControl'
import FilterFoot from './filterMain/FilterFoot'
import bus from '@/utils/bus'
import {
  mapState
} from 'vuex'
import {
  groupTree,
  fieldListWithPermission,
  post
} from '@/api/dataset/dataset'
import {
  viewsWithIds
} from '@/api/panel/view'
import {
  authModel
} from '@/api/system/sysAuth'
export default {
  name: 'FilterDialog',
  components: {
    DeMainContainer,
    DeContainer,
    DeAsideContainer,
    draggable,
    FilterHead,
    FilterControl,
    FilterFoot
  },
  props: {

    widgetInfo: {
      type: Object,
      default: null
    },

    element: {
      type: Object,
      default: () => {}
    }
  },

  data() {
    return {
      activeName: 'dataset',
      showDomType: 'tree',
      comShowDomType: 'view',
      dataSetBreads: [{
        label: this.$t('panel.data_list'),
        link: false,
        type: 'root'
      }],
      componentSetBreads: [{
        label: this.$t('panel.component_list'),
        link: false,
        type: 'root'
      }],
      datas: [],
      sceneDatas: [],
      //   viewDatas: [],
      fieldDatas: [],
      comFieldDatas: [],
      defaultProps: {
        label: 'name',
        children: 'children',
        isLeaf: 'isLeaf',
        id: 'id',
        parentId: 'pid'
      },
      widget: null,
      fieldValues: [],
      popovervisible: false,
      viewInfos: [],
      groupForm: {
        name: '',
        pid: '0',
        level: 0,
        type: '',
        children: [],
        sort: 'type desc,name asc'
      },
      isTreeSearch: false,
      defaultDatas: [],
      keyWord: '',
      timer: null,
      expandedArray: [],
      viewKeyWord: '',
      titlePopovervisible: false,
      fieldsParent: null,

      myAttrs: null,

      childViews: {
        viewInfos: []
      },
      currentElement: null
    }
  },
  computed: {

    ...mapState([
      'componentData'
    ])
  },

  watch: {
    'myAttrs.dragItems'(values) {
      if (values && values.length > 0) {
        const fieldIds = values.map(val => val.id)
        this.myAttrs.fieldId = fieldIds.join()
        // this.myAttrs.dragItems = values
        this.myAttrs.activeName = this.activeName
        this.myAttrs.fieldsParent = this.fieldsParent
      } else if (this.myAttrs && this.myAttrs.fieldId) {
        this.myAttrs.fieldId = null
        this.myAttrs.activeName = null
      }
      this.enableSureButton()
    },
    keyWord(val) {
      this.expandedArray = []
      if (this.showDomType === 'field') {
        return
      }
      if (this.timer) {
        clearTimeout(this.timer)
      }
      this.timer = setTimeout(() => {
        this.getTreeData(val)
      }, (val && val !== '') ? 1000 : 0)
    }
  },
  created() {
    this.widget = this.widgetInfo
    this.currentElement = JSON.parse(JSON.stringify(this.element))
    this.myAttrs = this.currentElement.options.attrs
    this.treeNode(this.groupForm)

    if (this.myAttrs && this.myAttrs.dragItems) {
      this.enableSureButton()
    }
    this.initWithField()
    this.loadViews()
  },
  mounted() {
    bus.$on('valid-values-change', valid => {
      this.validateFilterValue(valid)
    })
  },

  methods: {
    initWithField() {
      if (this.myAttrs && this.myAttrs.activeName) {
        this.activeName = this.myAttrs.activeName
        if (this.myAttrs.fieldsParent) {
          this.fieldsParent = this.myAttrs.fieldsParent
          this.$nextTick(() => {
            this.activeName === 'dataset' && this.showFieldDatas(this.fieldsParent)
            this.activeName !== 'dataset' && this.comShowFieldDatas(this.fieldsParent)
          })
        }
      }
    },
    getTreeData(val) {
      if (val) {
        this.isTreeSearch = true
        this.searchTree(val)
      } else {
        this.isTreeSearch = false
        this.treeNode(this.groupForm)
      }
    },
    searchTree(val) {
      this.expandedArray = []
      const queryCondition = {
        withExtend: 'parent',
        modelType: 'dataset',
        name: val
      }
      authModel(queryCondition).then(res => {
        this.datas = this.buildTree(res.data)
      })
    },
    buildTree(arrs) {
      const idMapping = arrs.reduce((acc, el, i) => {
        acc[el[this.defaultProps.id]] = i
        return acc
      }, {})
      const roots = []
      arrs.forEach(el => {
        // 判断根节点 ###
        el.type = el.modelInnerType
        el.isLeaf = el.leaf
        if (el[this.defaultProps.parentId] === null || el[this.defaultProps.parentId] === 0 || el[this
          .defaultProps.parentId] === '0') {
          roots.push(el)
          return
        }
        // 用映射表找到父元素
        const parentEl = arrs[idMapping[el[this.defaultProps.parentId]]]
        // 把当前元素添加到父元素的`children`数组中
        parentEl.children = [...(parentEl.children || []), el]

        // 设置展开节点 如果没有子节点则不进行展开
        if (parentEl.children.length > 0) {
          this.expandedArray.push(parentEl[this.defaultProps.id])
        }
      })
      return roots
    },
    loadViews() {
      /* const viewIds = this.componentData
        .filter(item => item.type === 'view' && item.propValue && item.propValue.viewId)
        .map(item => item.propValue.viewId) */
      let viewIds = []; let tabViewIds = []
      for (let index = 0; index < this.componentData.length; index++) {
        const element = this.componentData[index]
        if (element.type && element.propValue && element.propValue.viewId && element.type === 'view') {
          viewIds.push(element.propValue.viewId)
        }

        if (element.type && element.type === 'de-tabs') {
          tabViewIds = element.options.tabList.filter(item => item.content && item.content.type === 'view' && item.content.propValue && item.content.propValue.viewId).map(item => item.content.propValue.viewId)
        }
        viewIds = [...viewIds, ...tabViewIds]
      }
      viewIds && viewIds.length > 0 && viewsWithIds(viewIds).then(res => {
        const datas = res.data
        /* datas.forEach(item => {
          if (tabViewIds.includes(item.id)) {
            item.name = 'tabs(' + item.name + ')'
          }
        }) */
        this.viewInfos = datas
        this.childViews.viewInfos = datas
      })
    },
    handleNodeClick(data) {
      if (data.type !== 'group') {
        this.showFieldDatas(data)
      }
    },
    loadTree(node, resolve) {
      if (!this.isTreeSearch) {
        if (node.level > 0) {
          if (node.data.id) {
            post('/dataset/table/listAndGroup', {
              sort: 'type asc,name asc,create_time desc',
              sceneId: node.data.id
            }).then(res => {
              resolve(res.data)
            })
          }
        }
      } else {
        node.data.children && resolve(node.data.children)
      }
    },
    treeNode(group) {
      post('/dataset/group/treeNode', group).then(res => {
        this.defaultDatas = res.data
        this.datas = res.data
      })
    },
    loadDataSetTree() {
      groupTree({}).then(res => {
        const datas = res.data

        this.data = datas
      })
    },

    setTailLink(node) {
      const tail = this.dataSetBreads[this.dataSetBreads.length - 1]
      tail.type = node.type
      tail.link = true
    },
    comSetTailLink(node) {
      const tail = this.componentSetBreads[this.componentSetBreads.length - 1]
      tail.type = node.type
      tail.link = true
    },
    addTail(node) {
      const tail = {
        link: false,
        label: node.label || node.name,
        type: node.type
      }
      this.dataSetBreads.push(tail)
    },
    comAddTail(node) {
      const tail = {
        link: false,
        label: node.label || node.name,
        type: node.type
      }
      this.componentSetBreads.push(tail)
    },

    removeTail(bread) {
      for (let index = 0; index < this.dataSetBreads.length; index++) {
        const element = this.dataSetBreads[index]
        if (element.type === bread.type) {
          this.dataSetBreads = this.dataSetBreads.slice(0, index + 1)
          this.dataSetBreads[this.dataSetBreads.length - 1]['link'] = false
          return
        }
      }
    },
    comRemoveTail() {
      this.componentSetBreads = this.componentSetBreads.slice(0, this.componentSetBreads.length - 1)
      this.componentSetBreads[this.componentSetBreads.length - 1]['link'] = false
    },
    backToLink(bread) {
      this.showDomType = 'tree'

      this.removeTail(bread)
      this.$nextTick(() => {
        this.expandedArray = []
        this.keyWord = ''
        this.isTreeSearch = false
        this.datas = JSON.parse(JSON.stringify(this.defaultDatas))
      })
    },
    comBackLink(bread) {
      this.comShowDomType = 'view'
      this.viewKeyWord = ''
      this.comRemoveTail()
    },

    loadField(tableId) {
      fieldListWithPermission(tableId).then(res => {
        let datas = res.data
        if (this.widget && this.widget.filterFieldMethod) {
          datas = this.widget.filterFieldMethod(datas)
        }
        this.fieldDatas = datas
      })
    },
    comLoadField(tableId) {
      fieldListWithPermission(tableId).then(res => {
        let datas = res.data
        if (this.widget && this.widget.filterFieldMethod) {
          datas = this.widget.filterFieldMethod(datas)
        }
        this.comFieldDatas = datas
      })
    },
    showFieldDatas(row) {
      this.keyWord = ''
      this.showDomType = 'field'
      this.setTailLink(row)
      this.addTail(row)
      this.fieldsParent = row
      this.loadField(row.id)
    },
    comShowFieldDatas(row) {
      this.viewKeyWord = ''
      this.comShowDomType = 'field'
      this.comSetTailLink(row)
      this.comAddTail(row)
      this.fieldsParent = row
      this.comLoadField(row.tableId)
    },
    onMove(e, originalEvent) {
      this.moveId = e.draggedContext.element.id
      return true
    },

    end(e) {
      this.refuseMove(e)
      this.removeCheckedKey(e)
    },

    refuseMove(e) {
      const that = this
      const xItems = this.fieldDatas.filter(function(m) {
        return m.id === that.moveId
      })

      if (xItems && xItems.length > 1) {
        this.fieldDatas.splice(e.newDraggableIndex, 1)
      }
    },
    removeCheckedKey(e) {
      const that = this
      if (!this.currentElement.options.attrs.dragItems) return
      const xItems = this.currentElement.options.attrs.dragItems.filter(function(m) {
        return m.id === that.moveId
      })

      if (xItems && xItems.length > 1) {
        this.currentElement.options.attrs.dragItems.splice(e.newDraggableIndex, 1)
      }
    },

    enableSureButton() {
      let valid = true
      const enable = this.currentElement.options.attrs.dragItems && this.currentElement.options.attrs.dragItems
        .length > 0
      if (this.widget.validDynamicValue) {
        valid = this.widget.validDynamicValue(this.currentElement)
      }
      this.$emit('sure-button-status', enable && valid)
    },

    getElementInfo() {
      return this.currentElement
    },

    validateFilterValue(valid) {
      const enable = this.currentElement.options.attrs.dragItems && this.currentElement.options.attrs.dragItems
        .length > 0
      this.$emit('sure-button-status', enable && valid)
    }

  }
}

</script>

<style lang="scss" scoped>
  .my-form-item {
    cursor: text;
  }

  .de-dialog-container {
    height: 50vh !important;

  }

  .ms-aside-container {
    width: 40% !important;
    min-width: 230px !important;
    max-width: 260px !important;
    height: 100%;
    min-height: 100px;
    padding: 5px;
    border: none;
    padding-bottom: 20px !important;
  }

  .ms-main-container {
    height: 100%;
    min-height: 400px;
    padding: 5px 10px;
  }

  .filter-dialog-tabs {
    border: 1px solid var(--TableBorderColor, #E6E6E6);
    padding: 10px;
    height: 100%;

    >>>div.el-tabs__content {
      height: calc(100% - 55px);
    }
  }

  .filter-common {
    margin: 10px 5px;

  }

  .component-header {
    margin: 5px 5px 15px;
  }

  .component-result-content {
    height: calc(50vh - 150px);
    overflow-y: auto;
  }

  .link-text {
    font-weight: 450 !important;
    color: #409EFF;
  }

  .filter-db-row {
    i {
      color: #3685f2;
    }

    // background-color: #3685f2;
    // color: #fff;
  }

  .filter-db-row:hover {
    background-color: var(--background-color-base, #f5f7fa) !important;
    cursor: pointer;
  }

  .filter-db-row-checked:hover {
    background-color: var(--background-color-base, #f5f7fa) !important;
    color: inherit;
    cursor: pointer;

    i {
      background-color: inherit;
      color: #3685f2;
    }
  }

  .filter-db-row-checked {
    background-color: #3685f2 !important;
    color: #fff;

    i {
      background-color: #3685f2;
      color: #fff;
    }
  }

  .draggable-group {
    display: inline-block;
    width: 100%;
    height: calc(100% - 6px);
  }

</style>
