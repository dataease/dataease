<template>

  <de-container
    v-loading="$store.getters.loadingMap[$store.getters.currentPath]"
    class="de-dialog-container"
  >
    <de-aside-container
      :show-drag-bar="false"
      class="ms-aside-container"
    >
      <el-tabs
        v-model="activeName"
        class="filter-dialog-tabs"
      >
        <el-tab-pane
          class="de-tab"
          :label="$t('panel.select_by_table')"
          name="dataset"
        >
          <div class="component-header filter-common">
            <el-breadcrumb separator-class="el-icon-arrow-right">
              <el-breadcrumb-item
                v-for="bread in dataSetBreads"
                :key="bread.label"
              >
                <a
                  v-if="bread.link"
                  :class="{'link-text' : bread.link}"
                  @click="backToLink(bread)"
                >
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
                  :data="tempTreeData || treeData"
                  :props="defaultProps"

                  @node-click="handleNodeClick"
                >
                  <span
                    slot-scope="{ node, data }"
                    style="display: flex;flex: 1;width: 0%;"
                  >
                    <span>
                      <svg-icon
                        v-if="data.modelInnerType === 'db'"
                        icon-class="ds-db"
                        class="ds-icon-db"
                      />
                      <svg-icon
                        v-if="data.modelInnerType === 'sql'"
                        icon-class="ds-sql"
                        class="ds-icon-sql"
                      />
                      <svg-icon
                        v-if="data.modelInnerType === 'excel'"
                        icon-class="ds-excel"
                        class="ds-icon-excel"
                      />
                      <svg-icon
                        v-if="data.modelInnerType === 'custom'"
                        icon-class="ds-custom"
                        class="ds-icon-custom"
                      />
                      <svg-icon
                        v-if="data.modelInnerType === 'union'"
                        icon-class="ds-union"
                        class="ds-icon-union"
                      />
                      <svg-icon
                        v-if="data.modelInnerType === 'api'"
                        icon-class="ds-api"
                        class="ds-icon-api"
                      />
                    </span>
                    <span v-if="data.modelInnerType === 'db' || data.modelInnerType === 'sql'">
                      <span
                        v-if="data.mode === 0"
                        style="margin-left: 6px"
                      ><i class="el-icon-s-operation" /></span>
                      <span
                        v-if="data.mode === 1"
                        style="margin-left: 6px"
                      ><i class="el-icon-alarm-clock" /></span>
                    </span>

                    <span
                      style="margin-left: 6px;white-space:nowrap;overflow:hidden;text-overflow:ellipsis;"
                      :title="node.label"
                    >{{ node.label }}</span>

                  </span>
                </el-tree>

                <div v-if="showDomType === 'field'">
                  <draggable
                    v-model="fieldData"
                    :options="{group:{name: 'dimension',pull:'clone'},sort: true}"
                    animation="300"
                    :move="onMove"
                    class="drag-list"
                    @end="endDs"
                  >
                    <transition-group>
                      <div
                        v-for="item in fieldData"
                        :key="item.id"
                        :class="myAttrs && myAttrs.fieldId && myAttrs.fieldId.includes(item.id) ? 'filter-db-row-checked' : 'filter-db-row'"
                        class="filter-db-row"
                        style="margin: 5px 0;"
                      >
                        <span style="display: flex;flex: 1;">
                          <span>
                            <i class="el-icon-s-data" />
                          </span>

                          <span
                            style="margin-left: 6px;white-space:nowrap;overflow:hidden;text-overflow:ellipsis;"
                            :title="item.name"
                          >{{ item.name }}</span>
                        </span>
                      </div>
                    </transition-group>
                  </draggable>
                </div>
              </el-row>
            </el-col>
          </div>
        </el-tab-pane>
        <el-tab-pane
          class="de-tab"
          :label="$t('panel.select_by_module')"
          name="assembly"
        >
          <div class="component-header filter-common">
            <el-breadcrumb separator-class="el-icon-arrow-right">
              <el-breadcrumb-item
                v-for="bread in componentSetBreads"
                :key="bread.label"
              >
                <a
                  v-if="bread.link"
                  :class="{'link-text' : bread.link}"
                  @click="comBackLink(bread)"
                >
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
                  <el-table-column
                    prop="name"
                    :label="$t('commons.name')"
                  >
                    <template
                      v-if="comShowDomType === 'view'"
                      slot-scope="scope"
                    >
                      <div
                        class="filter-db-row"
                        @click="comShowFieldData(scope.row)"
                      >
                        <span style="display: flex;flex: 1;">
                          <span>
                            <i class="el-icon-s-data" />
                          </span>

                          <span
                            style="margin-left: 6px;white-space:nowrap;overflow:hidden;text-overflow:ellipsis;"
                            :title="scope.row.name"
                          >{{ scope.row.name }}</span>
                        </span>
                      </div>
                    </template>
                  </el-table-column>
                </el-table>

                <div v-else-if="comShowDomType === 'field'">
                  <draggable
                    v-model="comFieldData"
                    :options="{group:{name: 'dimension',pull:'clone'},sort: true}"
                    animation="300"
                    :move="onMove"
                    class="drag-list"
                    @end="endVw"
                  >
                    <transition-group>
                      <div
                        v-for="item in comFieldData"
                        :key="item.id"
                        :class="myAttrs && myAttrs.fieldId && myAttrs.fieldId.includes(item.id) ? 'filter-db-row-checked' : 'filter-db-row'"
                        class="filter-db-row"
                        style="margin: 5px 0;"
                      >
                        <span style="display: flex;flex: 1;">
                          <span>
                            <i class="el-icon-s-data" />
                          </span>

                          <span
                            style="margin-left: 6px;white-space:nowrap;overflow:hidden;text-overflow:ellipsis;"
                            :title="item.name"
                          >{{ item.name }}</span>
                        </span>

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
        <filter-head
          :element="currentElement"
        />

        <filter-control
          :element="currentElement"
          :widget="widget"
          :control-attrs="myAttrs"
          :child-views="childViews"
          :dataset-params="datasetParams"
          :active-name="activeName"
        />

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
import { queryAuthModel } from '@/api/authModel/authModel'
import {
  mapState
} from 'vuex'
import { groupTree, fieldListWithPermission, datasetParams } from '@/api/dataset/dataset'
import {
  paramsWithIds,
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
      treeData: [],
      sceneData: [],
      fieldData: [],
      originFieldData: [],
      comFieldData: [],
      originComFieldData: [],
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
      defaultData: [],
      keyWord: '',
      timer: null,
      expandedArray: [],
      viewKeyWord: '',
      titlePopovervisible: false,
      fieldsParent: null,

      myAttrs: null,

      childViews: {
        viewInfos: [],
        datasetParams: []
      },
      datasetParams: [],
      currentElement: null,
      tempTreeData: null,
      showTips: false
    }
  },
  computed: {
    isTree() {
      return this.widget && this.widget.isTree
    },
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
        let results = this.originFieldData
        if (val) {
          results = this.originFieldData.filter(item => item.name.toLocaleLowerCase().includes(val.toLocaleLowerCase()))
        }
        this.fieldData = JSON.parse(JSON.stringify(results))
        return
      }
      if (this.timer) {
        clearTimeout(this.timer)
      }
      this.timer = setTimeout(() => {
        this.getTreeData(val)
      }, (val && val !== '') ? 1000 : 0)
    },

    viewKeyWord(val) {
      if (this.comShowDomType === 'field') {
        let results = this.originComFieldData
        if (val) {
          results = this.originComFieldData.filter(item => item.name.toLocaleLowerCase().includes(val.toLocaleLowerCase()))
        }
        this.comFieldData = JSON.parse(JSON.stringify(results))
      }
    }
  },
  created() {
    this.widget = this.widgetInfo
    this.currentElement = JSON.parse(JSON.stringify(this.element))
    this.myAttrs = this.currentElement.options.attrs
    this.treeNode(this.groupForm)
    this.loadViews()
    if (this.myAttrs && this.myAttrs.dragItems) {
      this.enableSureButton()
    }

    this.initWithField()

    this.ProhibitMultiple()
  },
  mounted() {
    bus.$on('valid-values-change', this.validateFilterValue)
  },
  beforeDestroy() {
    bus.$off('valid-values-change', this.validateFilterValue)
  },
  methods: {
    async checkSuperior(list, anotherTableIds) {
      let fieldValid = false
      const fieldId = this.myAttrs?.fieldId
      if (fieldId && list?.length) {
        const stack = [...list]
        while (stack.length) {
          const item = stack.pop()
          if (fieldId.includes(item.id)) {
            fieldValid = true
            break
          }
          if (item.children?.length) {
            item.children.forEach(kid => stack.push(kid))
          }
        }
      }
      if (!fieldValid && anotherTableIds?.length) {
        const ps = await Promise.all(anotherTableIds.map(id => fieldListWithPermission(id)))
        let anotherList = []
        ps.forEach(p => {
          anotherList = [...anotherList, ...p.data]
        })

        if (anotherList?.length && this.checkSuperior(anotherList, null)) {
          fieldValid = true
        }
      }
      if (!fieldValid) {
        this.myAttrs.fieldId = null
        this.myAttrs.dragItems = []
        this.myAttrs.fieldsParent = null
      }
      return fieldValid
    },

    treeNode(cache) {
      const modelInfo = localStorage.getItem('dataset-tree')
      const userCache = (modelInfo && cache)
      if (userCache) {
        this.tData = JSON.parse(modelInfo)
        const results = this.buildTree(this.tData)
        this.defaultData = JSON.parse(JSON.stringify(results))
        this.treeData = JSON.parse(JSON.stringify(results))
        return
      }
      queryAuthModel({ modelType: 'dataset' }, !userCache).then(res => {
        localStorage.setItem('dataset-tree', JSON.stringify(res.data))
        if (!userCache) {
          this.tData = res.data
          const results = this.buildTree(this.tData)
          this.defaultData = JSON.parse(JSON.stringify(results))
          this.treeData = JSON.parse(JSON.stringify(results))
        }
      })
    },
    initWithField() {
      if (this.myAttrs && this.myAttrs.activeName) {
        this.activeName = this.myAttrs.activeName
        if (this.myAttrs.fieldsParent) {
          this.fieldsParent = this.myAttrs.fieldsParent
          this.$nextTick(() => {
            this.activeName === 'dataset' && this.showFieldData(this.fieldsParent, true)
            this.activeName !== 'dataset' && this.comShowFieldData(this.fieldsParent, true)
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
        this.treeData = this.buildTree(res.data)
      })
    },
    buildTree(arrs) {
      const idMapping = arrs.reduce((acc, el, i) => {
        acc[el[this.defaultProps.id]] = i
        return acc
      }, {})
      const roots = []
      arrs.forEach(el => {
        el.type = el.modelInnerType
        el.isLeaf = el.leaf
        if (el[this.defaultProps.parentId] === null || el[this.defaultProps.parentId] === 0 || el[this
          .defaultProps.parentId] === '0') {
          roots.push(el)
          return
        }
        const parentEl = arrs[idMapping[el[this.defaultProps.parentId]]]
        parentEl.children = [...(parentEl.children || []), el]

        if (parentEl.children.length > 0) {
          this.expandedArray.push(parentEl[this.defaultProps.id])
        }
      })
      return roots
    },
    getNode(id, roots) {
      for (let index = 0; index < roots.length; index++) {
        const node = roots[index]
        if (node.id === id) return node

        if (node && node.children && node.children.length) {
          const temp = this.getNode(id, node.children)
          if (temp) return temp
        }
      }
      return null
    },

    loadViews() {
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
        const data = res.data

        this.viewInfos = data
        this.updateParentName()
        this.childViews.viewInfos = data
      })
      var type = 'TEXT'
      if (this.widgetInfo.name.indexOf('time') !== -1) {
        type = 'DATE'
      }
      if (this.widgetInfo.name === 'numberSelectWidget') {
        type = 'NUM'
      }
      viewIds && viewIds.length > 0 && paramsWithIds(type, viewIds).then(res => {
        const data = res.data

        this.childViews.datasetParams = data
      })
    },
    updateParentName() {
      if (this.fieldsParent && this.viewInfos?.length && this.activeName !== 'dataset') {
        this.viewInfos.forEach(info => {
          if (info.id === this.fieldsParent.id && info.name !== this.fieldsParent.name) {
            this.fieldsParent.name = info.name
            this.comBackLink(this.fieldsParent)
            this.comShowFieldData(this.fieldsParent)
          }
        })
      }
    },
    handleNodeClick(data) {
      if (data.modelInnerType !== 'group') {
        this.showFieldData(data)
      } else {
        if (!data.children || !data.children.length) {
          const name = data.name
          const msg = `[${name}]` + this.$t('panel.be_empty_dir')
          this.$warning(msg)
          return
        }
        this.showNextGroup(data)
      }
    },

    loadDataSetTree() {
      groupTree({}).then(res => {
        const data = res.data

        this.treeData = data
      })
    },

    setTailLink(node) {
      const tail = this.dataSetBreads[this.dataSetBreads.length - 1]
      tail.type = node.modelInnerType
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
        type: node.modelInnerType,
        id: node.id
      }
      this.dataSetBreads.push(tail)
    },
    addQueue(node) {
      this.dataSetBreads = this.dataSetBreads.slice(0, 1)
      const root = {
        id: null,
        children: JSON.parse(JSON.stringify(this.treeData))
      }
      this.getPathById(node.id, root, res => {
        if (res.length > 1) {
          for (let index = 1; index < res.length; index++) {
            const node = res[index]
            const temp = {
              link: true,
              label: node.label || node.name,
              type: node.modelInnerType,
              id: node.id
            }
            this.dataSetBreads.push(temp)
            this.dataSetBreads[0].link = true
          }

          this.dataSetBreads[this.dataSetBreads.length - 1].link = false
        }
      })
    },
    getPathById(id, catalog, callback) {
      var temppath = []
      try {
        const getNodePath = function(node) {
          temppath.push(node)
          if (node.id === id) {
            // eslint-disable-next-line no-throw-literal
            throw ('GOT IT!')
          }
          if (node.children && node.children.length > 0) {
            for (var i = 0; i < node.children.length; i++) {
              getNodePath(node.children[i])
            }
            temppath.pop()
          } else {
            temppath.pop()
          }
        }
        getNodePath(catalog)
      } catch (e) {
        callback(temppath)
      }
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
      if (!bread?.id) {
        this.dataSetBreads = this.dataSetBreads.slice(0, 1)
        this.dataSetBreads[this.dataSetBreads.length - 1]['link'] = false
        return
      }
      for (let index = 0; index < this.dataSetBreads.length; index++) {
        const element = this.dataSetBreads[index]
        if (element.type === bread.type && element.id === bread.id) {
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
        if (bread?.id) {
          const node = this.getNode(bread.id, this.treeData)
          if (node) {
            this.tempTreeData = node.children
          }
        } else {
          this.tempTreeData = null
        }

        this.treeData = JSON.parse(JSON.stringify(this.defaultData))
      })
    },
    comBackLink(bread) {
      this.comShowDomType = 'view'
      this.viewKeyWord = ''
      this.comRemoveTail()
    },
    anotherTableInfo(tableId) {
      if (this.myAttrs?.dragItems?.length) {
        return this.myAttrs.dragItems.filter(item => item.tableId !== tableId).map(item => item.tableId)
      }
      return null
    },
    async loadField(tableId, init) {
      const res = await fieldListWithPermission(tableId)
      let data = res.data || []
      if (init && !this.checkSuperior(data, this.anotherTableInfo(tableId))) {
        this.backToLink()
      }
      if (this.widget && this.widget.filterFieldMethod) {
        data = this.widget.filterFieldMethod(data)
      }
      this.originFieldData = data
      this.fieldData = JSON.parse(JSON.stringify(data))
    },
    loadDatasetParams(tableId) {
      var type = 'TEXT'
      if (this.widgetInfo.name.indexOf('time') !== -1) {
        type = 'DATE'
      }
      if (this.widgetInfo.name === 'numberSelectWidget') {
        type = 'NUM'
      }
      datasetParams(tableId, type).then(res => {
        this.datasetParams = res.data || []
      })
    },
    async comLoadField(tableId, init) {
      const res = await fieldListWithPermission(tableId)
      let data = res.data || []
      if (init && !this.checkSuperior(data, this.anotherTableInfo(tableId))) {
        this.comBackLink()
      }
      if (this.widget && this.widget.filterFieldMethod) {
        data = this.widget.filterFieldMethod(data)
      }
      this.originComFieldData = data
      this.comFieldData = JSON.parse(JSON.stringify(data))
    },
    showFieldData(row, init) {
      this.keyWord = ''
      this.showDomType = 'field'
      this.addQueue(row)
      this.fieldsParent = row
      this.loadField(row.id, init)
      this.loadDatasetParams(row.id)
    },
    showNextGroup(row) {
      this.tempTreeData = JSON.parse(JSON.stringify(row.children))
      this.keyWord = ''
      this.showDomType = 'tree'
      this.addQueue(row)
    },
    comShowFieldData(row, init) {
      this.viewKeyWord = ''
      this.comShowDomType = 'field'
      this.comSetTailLink(row)
      this.comAddTail(row)
      this.fieldsParent = row
      this.comLoadField(row.tableId, init)
    },
    onMove(e, originalEvent) {
      this.showTips = false
      this.moveId = e.draggedContext.element.id
      if (this.isTree) return true
      const tableId = e.draggedContext.element.tableId
      const prohibit = this.currentElement.options.attrs.dragItems.some(item => item.tableId === tableId)
      if (prohibit) {
        this.showTips = true
      }
      return !prohibit
    },

    endDs(e) {
      this.refuseMove(e, this.fieldData)
      this.removeCheckedKey(e)
    },
    endVw(e) {
      this.refuseMove(e, this.comFieldData)
      this.removeCheckedKey(e)
    },

    refuseMove(e, data) {
      const that = this
      const xItems = data.filter(function(m) {
        return m.id === that.moveId
      })

      if (xItems && xItems.length > 1) {
        this.treeData.splice(e.newDraggableIndex, 1)
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
      this.ProhibitMultiple()
    },

    ProhibitMultiple() {
      if (this.isTree) return
      const sourceLen = this.currentElement.options.attrs.dragItems.length
      if (!sourceLen) return
      const res = new Map()

      const result = this.currentElement.options.attrs.dragItems.filter(item => !res.has(item.tableId) && res.set(item.tableId), 1)
      this.currentElement.options.attrs.dragItems = result
      const newLen = result.length
      if (sourceLen > newLen || this.showTips) this.$warning(this.$t('panel.prohibit_multiple'))
    },

    enableSureButton() {
      let valid = true

      const enable =
      this.currentElement.options.attrs.dragItems && this.currentElement.options.attrs.dragItems
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

    ::v-deep div.el-tabs__content {
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
