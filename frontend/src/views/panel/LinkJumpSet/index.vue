<template>
  <el-row v-if="linkJump" v-loading="$store.getters.loadingMap[$store.getters.currentPath]" style="height: 430px">
    <el-row>
      <span style="font-weight:600;margin-right: 20px">{{ $t('panel.jump_set') }}</span>
      <el-checkbox v-model="linkJump.checked">{{ $t('panel.enable_jump') }}</el-checkbox>
    </el-row>
    <el-row v-loading="loading">
      <el-row class="preview">
        <el-col :span="8" style="height:100%;overflow-y: auto">
          <el-row class="tree-head">
            <span style="float: left;margin-left: 30px">{{ $t('panel.column_name') }}</span>
            <span style="float: right;margin-right: 10px">{{ $t('panel.enable_column') }}</span>
          </el-row>
          <el-tree
            ref="linkJumpInfoTree"
            :data="linkJumpInfoXArray"
            node-key="sourceFieldId"
            highlight-current
            :props="treeProp"
            @node-click="nodeClick"
          >
            <span slot-scope="{ node, data }" class="custom-tree-node">
              <span>
                <span style="margin-left: 6px">{{ data.sourceFieldName }}</span>
              </span>
              <span @click.stop>
                <div>
                  <span class="auth-span">
                    <el-checkbox v-model="data.checked" @change="sourceFieldCheckedChange(data)" />
                  </span>
                </div>
              </span>
            </span>
          </el-tree>
        </el-col>
        <el-col :span="16" class="preview-show">
          <el-row v-if="linkJumpInfo">
            <el-row style="margin-top: 10px;height: 30px;">
              <el-col :span="4" style="margin-left: 20px">
                {{ $t('panel.link_type') }}：
              </el-col>
              <el-col :span="10">
                <el-radio-group v-model="linkJumpInfo.linkType" size="mini">
                  <el-radio label="outer">{{ $t('panel.link_outer') }}</el-radio>
                  <el-radio label="inner">{{ $t('panel.link_panel') }}</el-radio>
                </el-radio-group>
              </el-col>
              <el-col v-if="linkJumpInfo.linkType==='inner'" :span="9">
                <treeselect
                  v-model="linkJumpInfo.targetPanelId"
                  :options="panelList"
                  :disable-branch-nodes="true"
                  :normalizer="normalizer"
                  :placeholder="$t('panel.select_jump_panel')"
                  :no-children-text="$t('commons.treeselect.no_children_text')"
                  :no-options-text="$t('commons.treeselect.no_options_text')"
                  :no-results-text="$t('commons.treeselect.no_results_text')"
                  style="margin-right: 10px"
                  @select="panelNodeClick"
                  @input="inputVal"
                />
              </el-col>
            </el-row>
            <el-row style="margin-top: 10px;height: 30px">
              <el-col :span="4" style="margin-left: 20px">
                {{ $t('panel.open_model') }}：
              </el-col>
              <el-col :span="10">
                <el-radio-group v-model="linkJumpInfo.jumpType" size="mini">
                  <el-radio label="_self">{{ $t('panel.now_window') }}</el-radio>
                  <el-radio label="_blank">{{ $t('panel.new_window') }}</el-radio>
                </el-radio-group>
              </el-col>
            </el-row>
            <el-row v-if="linkJumpInfo.linkType==='inner'" style="margin-top: 5px;" class="top_border">
              <el-row style="margin-top: 10px">
                <el-col :span="11">
                  <div class="ellip">{{ $t('panel.link_view') }}</div>
                </el-col>
                <el-col :span="11">
                  <div class="ellip">{{ $t('panel.link_view_field') }}</div>
                </el-col>
              </el-row>
              <el-row style="height: 180px;overflow-y: auto">
                <el-row v-for="(targetViewInfo,index) in linkJumpInfo.targetViewInfoList" :key="index">
                  <el-col :span="11">
                    <div class="select-filed">
                      <el-select v-model="targetViewInfo.targetViewId" style="width: 100%" size="mini" :placeholder="$t('fu.search_bar.please_select')" @change="viewInfoOnChange(targetViewInfo)">
                        <el-option
                          v-for="item in currentLinkPanelViewArray"
                          :key="item.id"
                          :label="item.name"
                          :value="item.id"
                        >
                          <span v-if="item.isPlugin" style="float: left">
                            <svg-icon :icon-class="item.type !== 'buddle-map' ? ('/api/pluginCommon/staticInfo/' + item.type + '/svg') : item.type" style="width: 14px;height: 14px" />
                          </span>
                          <span v-else style="float: left">
                            <svg-icon :icon-class="item.type" style="width: 14px;height: 14px" />
                          </span>
                          <span style="float: left; font-size: 12px">{{ item.name }}</span>
                        </el-option>
                      </el-select>
                    </div>
                  </el-col>
                  <el-col :span="11">
                    <div class="select-filed">
                      <el-select v-model="targetViewInfo.targetFieldId" style="width: 100%" size="mini" :placeholder="$t('fu.search_bar.please_select')">
                        <el-option
                          v-for="viewField in viewIdFieldArrayMap[targetViewInfo.targetViewId]"
                          :key="viewField.id"
                          :label="viewField.name"
                          :value="viewField.id"
                        >
                          <span style="float: left">
                            <svg-icon v-if="viewField.deType === 0" icon-class="field_text" class="field-icon-text" />
                            <svg-icon v-if="viewField.deType === 1" icon-class="field_time" class="field-icon-time" />
                            <svg-icon v-if="viewField.deType === 2 || viewField.deType === 3" icon-class="field_value" class="field-icon-value" />
                            <svg-icon v-if="viewField.deType === 5" icon-class="field_location" class="field-icon-location" />
                          </span>
                          <span style="float: left;font-size: 12px">{{ viewField.name }}</span>
                        </el-option>
                      </el-select>
                    </div>
                  </el-col>
                  <el-col :span="2">
                    <div>
                      <el-button icon="el-icon-delete" type="text" size="small" style="float: left" @click="deleteLinkJumpField(index)" />
                    </div>
                  </el-col>
                </el-row>
              </el-row>

              <el-row class="bottom">
                <el-button :disabled="!linkJumpInfo.targetPanelId" size="mini" type="success" icon="el-icon-plus" round @click="addLinkJumpField">{{ $t('panel.add_jump_field') }}</el-button>
              </el-row>
              <i slot="reference" class="icon iconfont icon-edit slot-class" />
            </el-row>
            <el-row v-if="linkJumpInfo.linkType==='outer'" style="border-top: 1px solid #E6E6E6;padding:14px">
              <el-row style="height: 230px;border: 1px solid #E6E6E6">
                <el-col :span="18" style="height: 100%">
                  <el-row>
                    <span>
                      {{ $t('panel.target_url') }}
                      <el-tooltip class="item" effect="dark" placement="bottom">
                        <div slot="content">
                          {{ $t('panel.target_url_tips') }}
                        </div>
                        <i class="el-icon-info" style="cursor: pointer;" />
                      </el-tooltip>
                    </span>
                    <codemirror
                      v-show="codemirrorShow"
                      ref="myCm"
                      v-model="linkJumpInfo.content"
                      class="codemirror"
                      :options="cmOption"
                      @ready="onCmReady"
                      @focus="onCmFocus"
                      @input="onCmCodeChange"
                    />
                  </el-row>
                </el-col>
                <el-col :span="6" style="height: 100%;border-left: 1px solid #E6E6E6;">
                  <el-col :span="24" style="height: 100%" class="padding-lr">
                    <span>
                      {{ $t('panel.select_world') }}
                      <el-tooltip class="item" effect="dark" placement="bottom">
                        <div slot="content">
                          引用字段以 "[" 开始， "]" 结束
                          <br>
                          请勿修改引用内容，否则将引用失败
                          <br>
                          若输入与引用字段相同格式的内容，将被当作引用字段处理
                        </div>
                        <i class="el-icon-info" style="cursor: pointer;" />
                      </el-tooltip>
                    </span>
                    <el-input
                      v-model="searchField"
                      size="mini"
                      :placeholder="$t('dataset.search')"
                      prefix-icon="el-icon-search"
                      clearable
                    />
                    <div class="field-height">
                      <el-divider />
                      <draggable
                        v-model="linkJumpInfoArray"
                        :options="{group:{name: 'drag',pull:'clone'},sort: true}"
                        animation="300"
                        class="drag-list"
                        :disabled="true"
                      >
                        <transition-group>
                          <span
                            v-for="item in linkJumpInfoArray"
                            :key="item.sourceFieldId"
                            class="item-dimension"
                            :title="item.sourceFieldName"
                            @click="insertFieldToCodeMirror('['+item.sourceFieldName+']')"
                          >
                            <svg-icon v-if="item.deExtractType === 0" icon-class="field_text" class="field-icon-text" />
                            <svg-icon v-if="item.deExtractType === 1" icon-class="field_time" class="field-icon-time" />
                            <svg-icon
                              v-if="item.deExtractType === 2 || item.deExtractType === 3"
                              icon-class="field_value"
                              class="field-icon-value"
                            />
                            <svg-icon v-if="item.deExtractType === 5" icon-class="field_location" class="field-icon-location" />
                            {{ item.sourceFieldName }}
                          </span>
                        </transition-group>
                      </draggable>
                    </div>
                  </el-col>
                </el-col>
              </el-row>
            </el-row>
          </el-row>
          <el-row v-else style="height: 100%; background-color: var(--MainContentBG);" class="custom-position">
            {{ $t('panel.select_dimension') }}
          </el-row>
        </el-col>
      </el-row>
    </el-row>
    <el-row class="root-class">
      <el-button size="mini" @click="cancel()">{{ $t('commons.cancel') }}</el-button>
      <el-button type="primary" size="mini" @click="save()">{{ $t('commons.confirm') }}</el-button>
    </el-row>
  </el-row>
</template>

<script>
import { queryPanelJumpInfo, queryWithViewId, updateJumpSet } from '@/api/panel/linkJump'
import { groupTree } from '@/api/panel/panel'
import { detailList } from '@/api/panel/panelView'
import { mapState } from 'vuex'

import draggable from 'vuedraggable'
import { codemirror } from 'vue-codemirror'
// 核心样式
import 'codemirror/lib/codemirror.css'
// 引入主题后还需要在 options 中指定主题才会生效
import 'codemirror/theme/solarized.css'
import 'codemirror/mode/sql/sql.js'
// require active-line.js
import 'codemirror/addon/selection/active-line.js'
// closebrackets
import 'codemirror/addon/edit/closebrackets.js'
// keyMap
import 'codemirror/mode/clike/clike.js'
import 'codemirror/addon/edit/matchbrackets.js'
import 'codemirror/addon/comment/comment.js'
import 'codemirror/addon/dialog/dialog.js'
import 'codemirror/addon/dialog/dialog.css'
import 'codemirror/addon/search/searchcursor.js'
import 'codemirror/addon/search/search.js'
import 'codemirror/keymap/emacs.js'
// 引入代码自动提示插件
import 'codemirror/addon/hint/show-hint.css'
import 'codemirror/addon/hint/sql-hint'
import 'codemirror/addon/hint/show-hint'
import {imgUrlTrans} from "@/components/canvas/utils/utils";

export default {
  components: { codemirror, draggable },
  props: {
    viewId: {
      type: String,
      required: true
    }
  },
  data() {
    return {
      codemirrorShow: true,
      name2Auto: [],
      searchField: '',
      searchFunction: '',
      loading: false,
      inputType: 'self',
      fieldName: 'name',
      tableRadio: null,
      keyWordSearch: '',
      columnLabel: this.$t('panel.belong_to_category'),
      templateList: [],
      importTemplateInfo: {
        snapshot: ''
      },
      sourceViewFields: [],
      treeProp: {
        id: 'sourceFieldId',
        label: 'sourceFieldName',
        children: 'children'
      },
      linkJump: null,
      linkJumpInfoArray: [],
      linkJumpInfoXArray: [],
      mapJumpInfoArray: {},
      panelList: [],
      linkJumpInfo: null,
      currentFiledTreeNode: null,
      defaultLinkJumpInfo: {
        linkType: 'inner',
        jumpType: '_self',
        targetViewInfoList: []
      },
      defaultTargetViewInfo: {
        targetViewId: null,
        targetFieldId: null
      },
      currentLinkPanelViewArray: [],
      viewIdFieldArrayMap: {},
      cmOption: {
        tabSize: 2,
        styleActiveLine: true,
        lineNumbers: true,
        line: true,
        mode: 'text/x-textile',
        theme: 'solarized',
        hintOptions: { // 自定义提示选项
          completeSingle: false // 当匹配只有一项的时候是否自动补全
        }
      }
    }
  },
  computed: {
    classBackground() {
      if (this.importTemplateInfo.snapshot) {
        return {
          background: `url(${imgUrlTrans(this.importTemplateInfo.snapshot)}) no-repeat`
        }
      } else {
        return {}
      }
    },
    panelInfo() {
      return this.$store.state.panel.panelInfo
    },
    codemirror() {
      return this.$refs.myCm.codemirror
    },
    ...mapState([
      'componentData',
      'canvasStyleData',
      'panelViewDetailsInfo'
    ])
  },
  watch: {

  },
  created() {
  },
  mounted() {
    this.init()
  },
  destroyed() {
  },
  methods: {
    init() {
      const chartDetails = JSON.parse(this.panelViewDetailsInfo[this.viewId])
      const checkAllAxisStr = chartDetails.xaxis + chartDetails.xaxisExt + chartDetails.yaxis + chartDetails.yaxisExt + chartDetails.drillFields
      let checkJumpStr
      if (chartDetails.type === 'table-pivot') {
        checkJumpStr = chartDetails.yaxis + chartDetails.yaxisExt + chartDetails.drillFields
      }else if(chartDetails.type === 'table-info') {
        checkJumpStr = chartDetails.xaxis + chartDetails.drillFields
      }else {
        checkJumpStr = checkAllAxisStr
      }
      // 获取可关联的仪表板
      groupTree({}).then(rsp => {
        this.panelList = rsp.data
      })
      // 获取当前视图的关联信息
      queryWithViewId(this.panelInfo.id, this.viewId).then(rsp => {
        this.linkJump = rsp.data
        this.linkJumpInfoArray = []
        this.linkJumpInfoXArray = []
        this.linkJump.linkJumpInfoArray.forEach(linkJumpInfo => {
          if (checkJumpStr.indexOf(linkJumpInfo.sourceFieldId) > -1) {
            this.mapJumpInfoArray[linkJumpInfo.sourceFieldId] = linkJumpInfo
            this.linkJumpInfoArray.push(linkJumpInfo)
            this.linkJumpInfoXArray.push(linkJumpInfo)
          } else if (checkAllAxisStr.indexOf(linkJumpInfo.sourceFieldId) > -1) {
            this.linkJumpInfoArray.push(linkJumpInfo)
          }
        })
        this.linkJumpInfoArray.forEach(linkJumpInfo => {
          linkJumpInfo.content = this.setNameIdTrans('sourceFieldId', 'sourceFieldName', linkJumpInfo.content, this.name2Auto)
        })
        const firstNode = this.linkJumpInfoArray[0]
        this.$nextTick(() => {
          this.$refs.linkJumpInfoTree.setCurrentKey(firstNode.sourceFieldId)
          this.nodeClick(firstNode)
        })
      })
    },
    handleExceed(file) {
    },
    cancel() {
      this.$emit('closeJumpSetDialog')
    },
    save() {
      this.codemirrorShow = false
      this.linkJumpInfoArray.forEach(jumpInfo => {
        jumpInfo.content = this.setNameIdTrans('sourceFieldName', 'sourceFieldId', jumpInfo.content)
      })
      updateJumpSet(this.linkJump).then(rsp => {
        this.$message({
          message: '保存成功',
          type: 'success',
          showClose: true
        })
        // 刷新跳转信息
        queryPanelJumpInfo(this.panelInfo.id).then(rsp => {
          this.$store.commit('setNowPanelJumpInfo', rsp.data)
          this.cancel()
          this.codemirrorShow = true
        }).catch(() => {
          this.codemirrorShow = true
        })
      }).catch(() => {
        this.codemirrorShow = true
      })
    },
    nodeClick(data, node) {
      this.linkJumpInfo = this.mapJumpInfoArray[data.sourceFieldId]
      if (!this.linkJumpInfo.linkType) {
        this.linkJumpInfo.linkType = 'inner'
      }
      if (!this.linkJumpInfo.jumpType) {
        this.linkJumpInfo.jumpType = '_blank'
      }
      if (!this.linkJumpInfo.content) {
        this.linkJumpInfo.content = 'http://'
      }
      if (!this.linkJumpInfo.attachParams) {
        this.linkJumpInfo.attachParams = false
      }
      if (this.linkJumpInfo.targetPanelId) {
        this.getPanelViewList(this.linkJumpInfo.targetPanelId)
      }
      setTimeout(() => {
        this.matchToAuto()
      }, 500)
    },
    // 获取当前视图字段 关联仪表板的视图信息列表
    getPanelViewList(panelId) {
      detailList(panelId).then(rsp => {
        this.viewIdFieldArrayMap = {}
        this.currentLinkPanelViewArray = rsp.data
        if (this.currentLinkPanelViewArray) {
          this.currentLinkPanelViewArray.forEach(view => {
            this.viewIdFieldArrayMap[view.id] = view.tableFields
          })
        }
      })
    },
    panelNodeClick(data, node) {
      this.linkJumpInfo.targetViewInfoList = []
      this.addLinkJumpField()
      this.getPanelViewList(data.id)
    },
    inputVal(value) {
      if (!value) {
        this.linkJumpInfo.targetViewInfoList = []
        this.viewIdFieldArrayMap = {}
        this.currentLinkPanelViewArray = []
      }
    },
    addLinkJumpField() {
      this.linkJumpInfo.targetViewInfoList.push({
        targetViewId: '',
        targetFieldId: ''
      })
    },
    deleteLinkJumpField(index) {
      this.linkJumpInfo.targetViewInfoList.splice(index, 1)
    },
    normalizer(node) {
      // 去掉children=null的属性
      if (node.children === null || node.children === 'null') {
        delete node.children
      }
    },
    viewInfoOnChange(targetViewInfo) {
      targetViewInfo.targetFieldId = null
    },
    sourceFieldCheckedChange(data) {
      if (data.checked) {
        this.linkJump.checked = true
      }
      this.$nextTick(() => {
        this.$refs.linkJumpInfoTree.setCurrentKey(data.sourceFieldId)
        this.nodeClick(data)
      })
    },
    onCmReady(cm) {
      this.codemirror.setSize('-webkit-fill-available', 'auto')
    },
    onCmFocus(cm) {
    },
    onCmCodeChange(newCode) {
      // this.fieldForm.originName = newCode
    },
    insertFieldToCodeMirror(param) {
      const pos1 = this.$refs.myCm.codemirror.getCursor()
      const pos2 = {}
      pos2.line = pos1.line
      pos2.ch = pos1.ch
      this.$refs.myCm.codemirror.replaceRange(param, pos2)
      this.$refs.myCm.codemirror.markText(pos2, { line: pos2.line, ch: param.length + pos2.ch }, { atomic: true, selectRight: true })
    },
    matchToAuto() {
      if (!this.name2Auto.length) return
      this.name2Auto.forEach(ele => {
        const search = this.$refs.myCm.codemirror.getSearchCursor(ele, { line: 0, ch: 0 })
        if (search.find()) {
          const { from, to } = search.pos
          this.$refs.myCm.codemirror.markText({ line: from.line, ch: from.ch - 1 }, { line: to.line, ch: to.ch + 1 }, { atomic: true, selectRight: true })
        }
      })
    },
    setNameIdTrans(from, to, originName, name2Auto) {
      if (!originName) {
        return originName
      }
      let name2Id = originName
      const nameIdMap = this.linkJumpInfoArray.reduce((pre, next) => {
        pre[next[from]] = next[to]
        return pre
      }, {})
      const on = originName.match(/\[(.+?)\]/g)
      if (on) {
        on.forEach(itm => {
          const ele = itm.slice(1, -1)
          if (name2Auto) {
            name2Auto.push(nameIdMap[itm])
          }
          name2Id = name2Id.replace(itm, '[' + nameIdMap[ele] + ']')
        })
      }
      return name2Id
    }
  }
}
</script>

<style scoped>

.my_table ::v-deep .el-table__row>td{
  /* 去除表格线 */
  border: none;
  padding: 0 0;
}
.my_table ::v-deep .el-table th.is-leaf {
  /* 去除上边框 */
    border: none;
}
.my_table ::v-deep .el-table::before{
  /* 去除下边框 */
  height: 0;
}

  .root-class {
    margin: 15px 0px 5px;
    text-align: center;
  }
  .preview {
    margin-top: 5px;
    border:1px solid #E6E6E6;
    height:350px !important;
    overflow:hidden;
    background-size: 100% 100% !important;
  }
  .preview-show {
    border-left:1px solid #E6E6E6;
    height:350px;
    background-size: 100% 100% !important;
  }
.top_border {
  border-top:1px solid #E6E6E6;
}

.slot-class{
  color: white;
}

.bottom {
  margin-top: 10px;
  text-align: center;

}
.ellip{
  /*width: 100%;*/
  margin-left: 10px;
  margin-right: 10px;
  overflow: hidden;/*超出部分隐藏*/
  white-space: nowrap;/*不换行*/
  text-overflow:ellipsis;/*超出部分文字以...显示*/
  text-align: center;
  background-color: #f7f8fa;
  color: #3d4d66;
  font-size: 12px;
  line-height: 24px;
  height: 24px;
  border-radius: 3px;
}

.select-filed{
  /*width: 100%;*/
  margin-left: 10px;
  margin-right: 10px;
  overflow: hidden;/*超出部分隐藏*/
  white-space: nowrap;/*不换行*/
  text-overflow:ellipsis;/*超出部分文字以...显示*/
  color: #3d4d66;
  font-size: 12px;
  line-height: 35px;
  height: 35px;
  border-radius: 3px;
}
::v-deep .el-popover{
  height: 200px;
  overflow: auto;
}
.custom-position {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-size: 14px;
  flex-flow: row nowrap;
  color: #9ea6b2;
}
.tree-style {
  padding: 10px 15px;
  height: 100%;
  overflow-y: auto;
}
/deep/ .vue-treeselect__control{
  height: 28px;
}
/deep/ .vue-treeselect__single-value{
  color:#606266;
  line-height: 28px!important;
}

.custom-tree-node {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-size: 14px;
}
.auth-span{
  float: right;
  width:30px;
  margin-right: 5px
}
.tree-head{
  height: 30px;
  line-height: 30px;
  border-bottom: 1px solid var(--TableBorderColor, #e6e6e6);
  background-color: var(--SiderBG, #f7f8fa);
  font-size: 12px;
  color: var(--TableColor, #3d4d66) ;
}
/deep/ .vue-treeselect__placeholder{
    line-height:28px
  }

/deep/ .el-tree--highlight-current .el-tree-node.is-current >.el-tree-node__content {
  background-color: #8dbbef !important;
}
.codemirror {
  height: 190px;
  overflow-y: auto;
  font-size: 12px;
}

.codemirror ::v-deep .CodeMirror-scroll {
  height: 200px;
  overflow-y: auto;
  font-size: 12px;
}

.padding-lr {
  padding: 0 4px;
}

.field-height {
  height: calc(100% - 25px);
  margin-top: 4px;
}

.drag-list {
  height: calc(100% - 26px);
  overflow: auto;
}

.item-dimension {
  padding: 2px 10px;
  margin: 2px 2px 0 2px;
  border: solid 1px #eee;
  text-align: left;
  color: #606266;
  /*background-color: rgba(35,46,64,.05);*/
  background-color: white;
  display: block;
  word-break: break-all;
  overflow: hidden;
  white-space: nowrap;
  text-overflow: ellipsis;
}

.blackTheme .item-dimension {
  border: solid 1px;
  border-color: #495865;
  color: #F2F6FC;
  background-color: var(--MainBG);
}

.item-dimension + .item-dimension {
  margin-top: 2px;
}

.item-dimension:hover {
  color: #1890ff;
  background: #e8f4ff;
  border-color: #a3d3ff;
  cursor: pointer;
}

.blackTheme .item-dimension:hover {
  /* color: var(--Main); */
  background: var(--ContentBG);
  /* cursor: pointer; */
}

.item-quota {
  padding: 2px 10px;
  margin: 2px 2px 0 2px;
  border: solid 1px #eee;
  text-align: left;
  color: #606266;
  /*background-color: rgba(35,46,64,.05);*/
  background-color: white;
  display: block;
  word-break: break-all;
  overflow: hidden;
  white-space: nowrap;
  text-overflow: ellipsis;
}

.blackTheme .item-quota {

  border: solid 1px;
  border-color: #495865;
  color: #F2F6FC;
  background-color: var(--MainBG);

}

.item-quota + .item-quota {
  margin-top: 2px;
}

.item-quota:hover {
  color: #67c23a;
  background: #f0f9eb;
  border-color: #b2d3a3;
  cursor: pointer;
}

.blackTheme .item-quota:hover {
  background: var(--ContentBG);
}

span {
  font-size: 12px;
}

.field-height ::v-deep .el-divider--horizontal{
  margin: 2px 0!important;
}

::v-deep .CodeMirror {
  height: 190px!important;
}

</style>
