<template>

  <de-container class="de-dialog-container">
    <de-aside-container :show-drag-bar="false" class="ms-aside-container">
      <el-tabs v-model="activeName" class="filter-dialog-tabs">
        <el-tab-pane :lazy="true" class="de-tab" label="按表选择" name="dataset">
          <div class="component-header filter-common">
            <el-breadcrumb separator-class="el-icon-arrow-right">
              <el-breadcrumb-item v-for="bread in dataSetBreads" :key="bread.label">
                <a v-if="bread.link" :class="{'link-text' : bread.link}" @click="backToLink(bread)"> {{ bread.label }}</a>
                <span v-else>{{ bread.label }}</span>
              </el-breadcrumb-item>
            </el-breadcrumb>
          </div>
          <div class="component-search filter-common">
            <el-input
              placeholder="请输入内容"
              prefix-icon="el-icon-search"
            />
          </div>

          <!-- <div class="component-result-content filter-common" @dragstart="handleDragStart" @dragend="handleDragEnd"> -->
          <div class="component-result-content filter-common">
            <el-tree
              v-if="showDomType === 'tree'"
              :data="data"
              :props="defaultProps"
              :render-content="renderNode"
              @node-click="handleNodeClick"
            />

            <el-table
              v-else-if="showDomType === 'db'"
              class="de-filter-data-table"
              :data="sceneDatas"
              :show-header="false"
              size="mini"
              :highlight-current-row="true"
              style="width: 100%"
            >
              <el-table-column prop="name" label="名称">
                <template v-if="showDomType === 'db'" :id="scope.row.id" slot-scope="scope">
                  <div class="filter-db-row" @click="showFieldDatas(scope.row)">
                    <i class="el-icon-s-data" />
                    <span> {{ scope.row.name }}</span>
                  </div>
                </template>
              </el-table-column>
            </el-table>

            <div v-else-if="showDomType === 'field'">
              <draggable
                v-model="fieldDatas"
                :options="{group:{name: 'dimension',pull:'clone'},sort: true}"
                animation="300"
                :move="onMove"
                class="drag-list"
                @end="end1"
                @start="start1"
              >
                <transition-group>
                  <div v-for="item in fieldDatas" :key="item.id" class="filter-db-row">
                    <i class="el-icon-s-data" />
                    <span> {{ item.name }}</span>
                  </div>
                </transition-group>
              </draggable>
            </div>
          </div>
        </el-tab-pane>
        <el-tab-pane :lazy="true" class="de-tab" label="按组件选择" name="assembly">按组件选择</el-tab-pane>
      </el-tabs>
    </de-aside-container>

    <de-main-container class="ms-main-container">
      <div>
        <el-row>
          <el-col :span="24">
            <div class="filter-field">
              <div class="field-content">
                <div class="field-content-left">
                  <div class="field-content-text">字段</div>
                </div>

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
                      <transition-group class="draggable-group">
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
                active-text="单选"
                inactive-text="多选"
              />
            </div>
          </el-col>
          <el-col :span="16"><div class="filter-options-right">
            <el-checkbox disabled>备选项1</el-checkbox>
            <el-checkbox disabled>备选项</el-checkbox>
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
import { ApplicationContext } from '@/utils/ApplicationContext'
import { groupTree, loadTable, fieldList, fieldValues } from '@/api/dataset/dataset'
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
    widgetId: {
      type: String,
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
      dataSetBreads: [
        { label: '数据列表', link: false, type: 'root' }
      ],
      data: [],
      sceneDatas: [],
      fieldDatas: [],
      defaultProps: {
        children: 'children',
        label: 'label'
      },
      selectField: [],
      widget: null,
      fieldValues: []
    }
  },
  watch: {
    selectField(values) {
      if (values && values.length > 0) {
        const value = values[0]
        const fieldId = value.id
        this.componentInfo && this.componentInfo.setOptionDatas && fieldValues(fieldId).then(res => {
          const datas = res.data.map(item => {
            return { id: item, text: item }
          })
          this.componentInfo.setOptionDatas(datas)
          this.$emit('re-fresh-component', this.componentInfo)
        })
      }
    }
  },
  created() {
    this.widget = ApplicationContext.getService(this.widgetId)
    this.loadDataSetTree()
  },

  methods: {
    handleNodeClick(data) {
      if (data.type === 'scene') {
        this.showSceneTable(data)
      }
    },
    loadDataSetTree() {
      groupTree({}).then(res => {
        const datas = res.data

        this.data = datas
      })
    },
    renderNode(h, { node, data, store }) {
      return (
        <div class='custom-tree-node' >

          { data.type === 'scene' ? (
            <el-button icon='el-icon-folder' type='text' size='mini' />
          ) : (
            ''
          )}
          <span class='label-span' >{node.label}</span>
        </div>
      )
    },
    showSceneTable(node) {
      this.showDomType = 'db'
      this.setTailLink(node)
      this.addTail(node)
      this.loadTable(node.id)
    },
    setTailLink(node) {
      const tail = this.dataSetBreads[this.dataSetBreads.length - 1]
      tail.type = node.type
      tail.link = true
    },
    addTail(node) {
      const tail = { link: false, label: node.label || node.name, type: node.type }
      this.dataSetBreads.push(tail)
    },

    removeTail() {
      this.dataSetBreads = this.dataSetBreads.slice(0, this.dataSetBreads.length - 1)
      this.dataSetBreads[this.dataSetBreads.length - 1]['link'] = false
    },
    backToLink(bread) {
      if (bread.type === 'db') {
        this.showDomType = 'db'
      } else {
        this.showDomType = 'tree'
      }

      this.removeTail()
    },
    loadTable(sceneId) {
      loadTable({ sceneId: sceneId, sort: 'type asc,create_time desc,name asc' }).then(res => {
        this.sceneDatas = res.data
      })
    },

    loadField(tableId) {
      fieldList(tableId).then(res => {
        let datas = res.data
        if (this.widget && this.widget.filterFieldMethod) {
          datas = this.widget.filterFieldMethod(datas)
        }
        this.fieldDatas = datas
      })
    },
    showFieldDatas(row) {
      this.showDomType = 'field'
      this.setTailLink(row)
      this.addTail(row)
      this.loadField(row.id)
    },
    test(row) {},
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
    }
  }
}
</script>

<style lang="scss" scoped>
  .de-dialog-container {
    height: 50vh !important;

  }
  .ms-aside-container {
    width: 40% !important;
    min-width: 230px !important;
    max-width: 260px !important;
    height: 100%;
    min-height: 390px;
    padding: 5px;
    border: none;
    padding-bottom: 20px !important;
  }

  .ms-main-container {
    height: 100%;
    min-height: 400px;

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
      height: calc(50vh - 130px);
      top: 160px;

  }

  .filter-dialog-tabs {
      border: 1px solid #E6E6E6;
  }

  .filter-common {
      margin: 10px 10px;

  }

  .component-header {
      margin: 20px 10px !important;
  }

  .link-text {
      font-weight: 450  !important;
      color: #409EFF;
  }
  .filter-db-row {
      :hover {
          cursor: pointer;
      }
      i {
          color: #409EFF;
      }
    // background-color: #3685f2;
    // color: #fff;
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

</style>
