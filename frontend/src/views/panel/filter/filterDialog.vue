<template>

  <de-container v-loading="$store.getters.loadingMap[$store.getters.currentPath]" class="de-dialog-container">
    <de-aside-container :show-drag-bar="false" class="ms-aside-container">
      <el-tabs v-model="activeName" class="filter-dialog-tabs">
        <el-tab-pane class="de-tab" :label="$t('panel.select_by_table')" name="dataset">
          <div class="component-header filter-common">
            <el-breadcrumb separator-class="el-icon-arrow-right">
              <el-breadcrumb-item v-for="bread in dataSetBreads" :key="bread.label">
                <a v-if="bread.link" :class="{'link-text' : bread.link}" @click="backToLink(bread)"> {{ bread.label }}</a>
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
                    <el-button v-if="data.type === 'db'" icon="el-icon-s-data" type="text" size="mini" />
                    <span class="label-span">{{ node.label }}</span>
                  </div>
                </el-tree>

                <div v-if="showDomType === 'field'">
                  <draggable
                    v-model="fieldDatas"
                    :disabled="selectField.length !== 0"
                    :options="{group:{name: 'dimension',pull:'clone'},sort: true}"
                    animation="300"
                    :move="onMove"
                    class="drag-list"
                    @end="end1"
                    @start="start1"
                  >
                    <transition-group>
                      <div v-for="item in fieldDatas.filter(item => !keyWord || (item.name && item.name.toLocaleLowerCase().includes(keyWord)))" :key="item.id" :class="componentInfo && componentInfo.options.attrs.fieldId === item.id ? 'filter-db-row-checked' : 'filter-db-row'" class="filter-db-row">
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
                <a v-if="bread.link" :class="{'link-text' : bread.link}" @click="comBackLink(bread)"> {{ bread.label }}</a>
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
                    :disabled="selectField.length !== 0"
                    :options="{group:{name: 'dimension',pull:'clone'},sort: true}"
                    animation="300"
                    :move="onMove"
                    class="drag-list"
                    @end="end1"
                    @start="start1"
                  >
                    <transition-group>
                      <div v-for="item in comFieldDatas.filter(item => !viewKeyWord || item.name.toLocaleLowerCase().includes(viewKeyWord))" :key="item.id" :class="componentInfo && componentInfo.options.attrs.fieldId === item.id ? 'filter-db-row-checked' : 'filter-db-row'" class="filter-db-row">
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
      <div>
        <el-row>
          <el-col :span="24">
            <div class="filter-field">
              <div class="field-content">
                <!-- <div class="field-content-left">
                  <div class="field-content-text">{{ $t('panel.field') }} </div>
                </div> -->

                <div class="field-content-right">
                  <el-row style="display:flex;height: 32px;">
                    <draggable
                      v-model="selectField"
                      group="dimension"
                      animation="300"
                      :move="onMove"
                      style="width:100%;height: 100%;margin:0 10px;border-radius: 4px;overflow-x: auto;display: flex;align-items: center;background-color: white;"
                      @end="end2"
                    >
                      <transition-group class="list-group" :data-value="$t('panel.drag_here')">
                        <drag-item v-for="(item,index) in selectField" :key="item.id" :item="item" :index="index" @closeItem="closeItem" />
                      </transition-group>
                    </draggable>
                  </el-row>
                </div>
              </div>
            </div>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="8">
            <div class="filter-options-left">
              <el-switch
                v-if="widget.showSwitch"
                v-model="componentInfo.options.attrs.multiple"
                :active-text="$t('panel.multiple_choice')"
                :inactive-text="$t('panel.single_choice')"
                @change="multipleChange"
              />
            </div>
          </el-col>
          <el-col :span="16"><div class="filter-options-right">
            <span style="padding-right: 10px;">
              <el-checkbox v-model="componentInfo.options.attrs.showTitle" @change="showTitleChange">显示标题</el-checkbox>
              <el-popover
                v-model="titlePopovervisible"
                placement="bottom-end"
                :disabled="!componentInfo.options.attrs.showTitle"
                width="200"
              >
                <div style="width: 100%;overflow-y: auto;overflow-x: hidden;word-break: break-all;position: relative;">
                  <el-input v-model="componentInfo.options.attrs.title" placeholder="请输入标题" type="textarea" maxlength="15" show-word-limit />
                </div>

                <i slot="reference" :class="{'i-filter-active': componentInfo.options.attrs.showTitle, 'i-filter-inactive': !componentInfo.options.attrs.showTitle}" class="el-icon-setting i-filter" />
              </el-popover>
            </span>
            <span style="padding-left: 10px;">
              <el-checkbox v-model="componentInfo.options.attrs.enableRange" @change="enableRangeChange"><span>  {{ $t('panel.custom_scope') }} </span> </el-checkbox>

              <el-popover
                v-model="popovervisible"
                placement="bottom-end"
                :disabled="!componentInfo.options.attrs.enableRange"
                width="200"
              >
                <div class="view-container-class">
                  <el-checkbox-group v-model="componentInfo.options.attrs.viewIds" @change="checkedViewsChange">
                    <el-checkbox v-for="(item ) in viewInfos" :key="item.id" :label="item.id">
                      <span>
                        <svg-icon :icon-class="item.type" class="chart-icon" />
                        <span style="margin-left: 6px">{{ item.name }}</span>
                      </span>
                    </el-checkbox>
                  </el-checkbox-group>
                </div>

                <i slot="reference" :class="{'i-filter-active': componentInfo.options.attrs.enableRange, 'i-filter-inactive': !componentInfo.options.attrs.enableRange}" class="el-icon-setting i-filter" />
              </el-popover>
            </span>
          </div>

          </el-col>
        </el-row>
        <el-row>
          <el-col :span="24">
            <div class="filter-content">
              <el-card class="box-card">
                <div style="margin-bottom: 10px;">
                  <span> {{ widget.label }}</span>
                </div>
                <div class="custom-component-class">
                  <slot />
                </div>
              </el-card>

            </div>
          </el-col>
        </el-row>
      </div>
    </de-main-container>
  </de-container>

</template>
<script>
import DeMainContainer from '@/components/dataease/DeMainContainer'
import DeContainer from '@/components/dataease/DeContainer'
import DeAsideContainer from '@/components/dataease/DeAsideContainer'
import draggable from 'vuedraggable'
import DragItem from '@/components/DragItem'
import { mapState } from 'vuex'
// import { ApplicationContext } from '@/utils/ApplicationContext'
import { groupTree, fieldList, fieldValues, post } from '@/api/dataset/dataset'
import { viewsWithIds } from '@/api/panel/view'
import { authModel } from '@/api/system/sysAuth'
export default {
  name: 'FilterDialog',
  components: {
    DeMainContainer,
    DeContainer,
    DeAsideContainer,
    draggable,
    DragItem
  },
  props: {

    widgetInfo: {
      type: Object,
      default: null
    },
    componentInfo: {
      type: Object,
      default: null
    }
  },

  data() {
    return {
      activeName: 'dataset',
      showDomType: 'tree',
      comShowDomType: 'view',
      dataSetBreads: [
        { label: this.$t('panel.data_list'), link: false, type: 'root' }
      ],
      componentSetBreads: [
        { label: this.$t('panel.component_list'), link: false, type: 'root' }
      ],
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
      selectField: [],
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
      fieldsParent: null
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
    selectField(values) {
      if (values && values.length > 0) {
        const value = values[0]
        const fieldId = value.id
        if (this.widget && this.widget.optionDatas) {
          fieldValues(fieldId).then(res => {
            this.componentInfo.options.attrs.datas = this.widget.optionDatas(res.data)
            this.componentInfo.options.attrs.fieldId = fieldId
            this.componentInfo.options.attrs.dragItems = values
            this.componentInfo.options.attrs.activeName = this.activeName
            this.componentInfo.options.attrs.fieldsParent = this.fieldsParent
            this.$emit('re-fresh-component', this.componentInfo)
          })
        } else {
          this.componentInfo.options.attrs.fieldId = fieldId
          this.componentInfo.options.attrs.dragItems = values
          this.componentInfo.options.attrs.activeName = this.activeName
          this.componentInfo.options.attrs.fieldsParent = this.fieldsParent
          this.$emit('re-fresh-component', this.componentInfo)
        }
      } else if (this.componentInfo && this.componentInfo.options.attrs.fieldId) {
        this.componentInfo.options.attrs.fieldId = null
        this.componentInfo.options.attrs.activeName = null
        this.$emit('re-fresh-component', this.componentInfo)
      }
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
    // this.widget = ApplicationContext.getService(this.widgetId)
    this.widget = this.widgetInfo
    this.treeNode(this.groupForm)

    if (this.componentInfo && this.componentInfo.options.attrs.dragItems) {
      this.selectField = this.componentInfo.options.attrs.dragItems
    }
    this.initWithField()
    this.loadViews()
  },

  methods: {
    initWithField() {
      if (this.componentInfo && this.componentInfo.options.attrs.activeName) {
        this.activeName = this.componentInfo.options.attrs.activeName
        if (this.componentInfo.options.attrs.fieldsParent) {
          this.fieldsParent = this.componentInfo.options.attrs.fieldsParent
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
        if (el[this.defaultProps.parentId] === null || el[this.defaultProps.parentId] === 0 || el[this.defaultProps.parentId] === '0') {
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
      const viewIds = this.componentData
        .filter(item => item.type === 'view' && item.propValue && item.propValue.viewId)
        .map(item => item.propValue.viewId)
      viewIds && viewIds.length > 0 && viewsWithIds(viewIds).then(res => {
        const datas = res.data
        this.viewInfos = datas
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
      const tail = { link: false, label: node.label || node.name, type: node.type }
      this.dataSetBreads.push(tail)
    },
    comAddTail(node) {
      const tail = { link: false, label: node.label || node.name, type: node.type }
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
    //   this.dataSetBreads = this.dataSetBreads.slice(0, this.dataSetBreads.length - 1)
    //   this.dataSetBreads[this.dataSetBreads.length - 1]['link'] = false
    },
    comRemoveTail() {
      this.componentSetBreads = this.componentSetBreads.slice(0, this.componentSetBreads.length - 1)
      this.componentSetBreads[this.componentSetBreads.length - 1]['link'] = false
    },
    backToLink(bread) {
    //   if (bread.type === 'field') {
    //     this.showDomType = 'db'
    //   } else {
    //     this.showDomType = 'tree'
    //   }
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
    // loadTable(sceneId) {
    //   loadTable({ sceneId: sceneId, sort: 'type asc,create_time desc,name asc' }).then(res => {
    //     res && res.data && (this.sceneDatas = res.data.map(tb => {
    //       tb.type = 'db'
    //       return tb
    //     }))
    //   })
    // },

    loadField(tableId) {
      fieldList(tableId).then(res => {
        let datas = res.data
        if (this.widget && this.widget.filterFieldMethod) {
          datas = this.widget.filterFieldMethod(datas)
        }
        this.fieldDatas = datas
      })
    },
    comLoadField(tableId) {
      fieldList(tableId).then(res => {
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
    start1() {

    },
    end1(e) {
      this.refuseMove(e)
      this.removeCheckedKey(e)
      this.save()
    },
    save() {

    },
    end2(e) {
      this.refuseMove(e)
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
      const xItems = this.selectField.filter(function(m) {
        return m.id === that.moveId
      })

      if (xItems && xItems.length > 1) {
        this.selectField.splice(e.newDraggableIndex, 1)
      }
    },
    closeItem(tag) {
      const index = tag.index
      this.selectField.splice(index, 1)
    },

    multipleChange(value) {
      // this.componentInfo.options.attrs.multiple = value
    //   this.componentInfo.options.value = null
      this.$emit('re-fresh-component', this.componentInfo)
    },

    checkedViewsChange(values) {
      // this.componentInfo.options.attrs.viewIds = values
      this.$emit('re-fresh-component', this.componentInfo)
    },
    enableRangeChange(value) {
      if (!value) {
        this.componentInfo.options.attrs.viewIds = []
      }
      // this.componentInfo.options.attrs.enableRange = value
      this.$emit('re-fresh-component', this.componentInfo)
    },
    showTitleChange(value) {
      if (!value) {
        this.componentInfo.options.attrs.title = ''
      }
      this.$emit('re-fresh-component', this.componentInfo)
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

  .filter-field {
    //   background: #99a9bf;
      border-radius: 4px;
      height: 45px;
      .field-content {
        position: relative;
        display: table;
        width: 100%;
        height: 100%;
        white-space: nowrap;

        .field-content-left {
            width: 50px;
            max-width: 50px;
            position: relative;
            display: table-cell;
            vertical-align: middle;
            margin: 0px;
            padding: 8px;
            height: 100%;
            border-right: none;
            border: 1px solid #E6E6E6;
            .field-content-text {
                box-sizing: border-box;
                overflow: hidden;
                overflow-x: hidden;
                overflow-y: hidden;
                word-break: break-all;
            }
        }
        .field-content-right {
            border-left: none;
            color: #9ea6b2;
            border: 1px solid #E6E6E6;
            width: 0%;
            max-width: 0%;
            position: relative;
            display: table-cell;
            vertical-align: middle;
            margin: 0px;
            padding: 0 0 0 0;
            height: 100%;
        }
    }

  }
  .filter-options-left {
      align-items: center;
      display: flex;
      flex-direction: row;
      justify-content: flex-start;
      flex-wrap: nowrap;
      height: 50px;
  }
  .filter-options-right {
      align-items: center;
      display: flex;
      flex-direction: row;
      justify-content: flex-end;
      flex-wrap: nowrap;
      height: 50px;
  }

  .filter-content {
      height: calc(50vh - 120px);
      top: 160px;

  }

  .filter-dialog-tabs {
      border: 1px solid #E6E6E6;
      padding: 10px;
      height: 100%;
      >>> div.el-tabs__content {
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
      font-weight: 450  !important;
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
      background-color: #f5f7fa !important;
      cursor: pointer;
  }
  .filter-db-row-checked:hover {
      background-color: #f5f7fa !important;
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
  .box-card {
      width: 100%;
      height: 100%;
  }
  .i-filter {
    text-align: center;
    margin-left: 5px;
    margin-top: 1px;
  }
  .i-filter-inactive {
      color: #9ea6b2!important;
      cursor: not-allowed!important;
  }
  .i-filter-active {
      cursor: pointer!important;
  }

  .view-container-class {

      min-height: 150px;
      max-height: 200px;
      width: 100%;
      overflow-y: auto;
      overflow-x: hidden;
      word-break:break-all;
      position: relative;
        >>> label {
            width: 100%;
            margin-left: 0px !important;
        }
  }

  .list-group:empty,
  .list-group > div:empty {
    display: inline-block;
    width: 100%;
    height: calc(100% - 13px);
  }

  .list-group:empty:before,
  .list-group > div:empty:before {
    content: attr(data-value);
  }

</style>
