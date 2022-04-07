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
            :data="linkJumpInfoArray"
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
              <el-col v-if="linkJumpInfo.linkType==='outer'" :span="9">
                <el-checkbox v-model="linkJumpInfo.attachParams">附加点击参数</el-checkbox>
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
                          <span style="float: left">
                            <svg-icon :icon-class="item.type" style="width: 14px;height: 14px" />
                          </span>
                          <span style="float: left; font-size: 12px"> {{ item.name }}</span>
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
                            <svg-icon v-if="viewField.deType === 2 || viewField.value === 3" icon-class="field_value" class="field-icon-value" />
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
                <el-button size="mini" type="success" icon="el-icon-plus" round @click="addLinkJumpField">{{ $t('panel.add_jump_field') }}</el-button>
              </el-row>

              <!--    <el-button slot="reference">T</el-button>-->
              <i slot="reference" class="icon iconfont icon-edit slot-class" />
            </el-row>
            <el-row v-if="linkJumpInfo.linkType==='outer'" style="height: 300px">
              <el-input
                v-model="linkJumpInfo.content"
                :autosize="{ minRows: 14}"
                type="textarea"
                :placeholder="$t('panel.input_jump_link')"
              />
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
import { deepCopy } from '@/components/canvas/utils/utils'
import { checkAddHttp } from '@/utils/urlUtils'

export default {
  components: { },
  props: {
    viewId: {
      type: String,
      required: true
    }
  },
  data() {
    return {
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
      linkJumpInfoArray: null,
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
      viewIdFieldArrayMap: {}

    }
  },
  computed: {
    classBackground() {
      if (this.importTemplateInfo.snapshot) {
        return {
          background: `url(${this.importTemplateInfo.snapshot}) no-repeat`
        }
      } else {
        return {}
      }
    },
    panelInfo() {
      return this.$store.state.panel.panelInfo
    },
    ...mapState([
      'componentData',
      'canvasStyleData'
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
      // 获取可关联的仪表板
      groupTree({}).then(rsp => {
        this.panelList = rsp.data
      })
      // 获取当前视图的关联信息
      queryWithViewId(this.panelInfo.id, this.viewId).then(rsp => {
        this.linkJump = rsp.data
        this.linkJumpInfoArray = this.linkJump.linkJumpInfoArray
        this.linkJumpInfoArray.forEach(linkJumpInfo => {
          this.mapJumpInfoArray[linkJumpInfo.sourceFieldId] = linkJumpInfo
        })
        const firstNode = this.linkJumpInfoArray[0]
        this.$nextTick(() => {
          this.$refs.linkJumpInfoTree.setCurrentKey(firstNode.sourceFieldId)
          this.nodeClick(firstNode)
        })
      })
      // 获取当前视图的字段信息
      // getTableFieldWithViewId(this.viewId).then(rsp => {
      //   this.sourceViewFields = rsp.data
      // })
    },
    handleExceed(file) {
    },
    cancel() {
      this.$emit('closeJumpSetDialog')
    },
    save() {
      this.linkJumpInfo.content = checkAddHttp(this.linkJumpInfo.content)
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
        })
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
      // console.log('panelNodeClick:' + JSON.stringify(data))
      this.linkJumpInfo.targetViewInfoList = []
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
      this.$nextTick(() => {
        this.$refs.linkJumpInfoTree.setCurrentKey(data.sourceFieldId)
        this.nodeClick(data)
      })
    }
  }
}
</script>

<style scoped>

.my_table >>> .el-table__row>td{
  /* 去除表格线 */
  border: none;
  padding: 0 0;
}
.my_table >>> .el-table th.is-leaf {
  /* 去除上边框 */
    border: none;
}
.my_table >>> .el-table::before{
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
  margin-top: 20px;
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
>>>.el-popover{
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
</style>
