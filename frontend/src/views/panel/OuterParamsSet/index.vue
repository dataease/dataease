<template>
  <el-row v-loading="$store.getters.loadingMap[$store.getters.currentPath]" style="height: 430px">
    <el-row>
      <span style="font-weight:600;margin-right: 20px">{{ $t('panel.outer_param_set') }}</span>
      <el-checkbox v-model="outerParams.checked">{{ $t('panel.enable_outer_param_set') }}</el-checkbox>
    </el-row>
    <el-row v-loading="loading">
      <el-row class="preview">
        <el-col :span="8" style="height:100%;overflow-y: hidden">
          <el-row class="tree-head">
            <span style="float: left;margin-left: 30px">{{ $t('panel.param_name') }}</span>
            <span style="float: right;margin-right: 10px">{{ $t('panel.enable_param') }}</span>
          </el-row>
          <el-row class="tree-content">
            <el-tree
              ref="outerParamsInfoTree"
              :data="outerParamsInfoArray"
              node-key="id"
              highlight-current
              :props="treeProp"
              @node-click="nodeClick"
            >
              <span slot-scope="{ node, data }" class="custom-tree-node">
                <span>
                  <span style="margin-left: 6px"><el-input
                    v-model="data.paramName"
                    size="mini"
                    :placeholder="$t('panel.input_param_name')"
                  /></span>
                </span>
                <span @click.stop>
                  <div>
                    <span class="auth-span">
                      <el-checkbox
                        v-model="data.checked"
                        style="margin-right: 10px"
                        @change="sourceFieldCheckedChange(data)"
                      />
                      <el-button
                        icon="el-icon-delete"
                        type="text"
                        size="small"
                        @click="removeOuterParamsInfo(node,data)"
                      />
                    </span>
                  </div>

                </span>
              </span>
            </el-tree>
          </el-row>
          <el-row class="tree-bottom">
            <el-button size="mini" type="success" icon="el-icon-plus" round @click="addOuterParamsInfo">{{ $t('panel.add_param') }}</el-button>
          </el-row>
        </el-col>
        <el-col :span="16" class="preview-show">
          <el-row v-if="outerParamsInfo">
            <el-row class="top_border">
              <el-row style="margin-top: 10px">
                <el-col :span="11">
                  <div class="ellip">{{ $t('panel.link_view') }}</div>
                </el-col>
                <el-col :span="11">
                  <div class="ellip">{{ $t('panel.link_view_field') }}</div>
                </el-col>
              </el-row>
              <el-row style="height: 266px;overflow-y: auto">
                <el-row v-for="(targetViewInfo,index) in outerParamsInfo.targetViewInfoList" :key="index">
                  <el-col :span="11">
                    <div class="select-filed">
                      <el-select
                        v-model="targetViewInfo.targetViewId"
                        style="width: 100%"
                        size="mini"
                        :placeholder="$t('fu.search_bar.please_select')"
                        @change="viewInfoOnChange(targetViewInfo)"
                      >
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
                          <span style="float: left; font-size: 12px"> {{ item.name }}</span>
                        </el-option>
                      </el-select>
                    </div>
                  </el-col>
                  <el-col :span="11">
                    <div class="select-filed">
                      <el-select
                        v-model="targetViewInfo.targetFieldId"
                        style="width: 100%"
                        size="mini"
                        :placeholder="$t('fu.search_bar.please_select')"
                      >
                        <el-option
                          v-for="viewField in viewIdFieldArrayMap[targetViewInfo.targetViewId]"
                          :key="viewField.id"
                          :label="viewField.name"
                          :value="viewField.id"
                        >
                          <span style="float: left">
                            <svg-icon v-if="viewField.deType === 0" icon-class="field_text" class="field-icon-text" />
                            <svg-icon v-if="viewField.deType === 1" icon-class="field_time" class="field-icon-time" />
                            <svg-icon
                              v-if="viewField.deType === 2 || viewField.deType === 3"
                              icon-class="field_value"
                              class="field-icon-value"
                            />
                            <svg-icon
                              v-if="viewField.deType === 5"
                              icon-class="field_location"
                              class="field-icon-location"
                            />
                          </span>
                          <span style="float: left;font-size: 12px">{{ viewField.name }}</span>
                        </el-option>
                      </el-select>
                    </div>
                  </el-col>
                  <el-col :span="2">
                    <div>
                      <el-button
                        icon="el-icon-delete"
                        type="text"
                        size="small"
                        style="float: left"
                        @click="deleteOuterParamsField(index)"
                      />
                    </div>
                  </el-col>
                </el-row>
              </el-row>

              <el-row class="bottom">
                <el-button size="mini" type="success" icon="el-icon-plus" round @click="addOuterParamsField">{{
                  $t('panel.add_param_link_field') }}
                </el-button>
              </el-row>

              <!--    <el-button slot="reference">T</el-button>-->
              <i slot="reference" class="icon iconfont icon-edit slot-class" />
            </el-row>
            <el-row v-if="outerParamsInfo.linkType==='outer'" style="height: 300px">
              <el-input
                v-model="outerParamsInfo.content"
                :autosize="{ minRows: 14}"
                type="textarea"
                :placeholder="$t('panel.input_jump_link')"
              />
            </el-row>
          </el-row>
          <el-row v-else style="height: 100%; background-color: var(--MainContentBG);" class="custom-position">
            {{ $t('panel.select_param') }}
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
import { detailList } from '@/api/panel/panelView'
import { mapState } from 'vuex'
import { queryWithPanelId, updateOuterParamsSet } from '@/api/panel/outerParams'
import { uuid } from 'vue-uuid'
import { deepCopy } from '@/components/canvas/utils/utils'
import { checkRepeat } from '@/utils/check'

export default {
  name: 'OuterParamsSet',
  components: {},
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
        id: 'paramsInfoId',
        label: 'paramName',
        children: 'children'
      },
      outerParams: {},
      outerParamsInfoArray: null,
      mapOuterParamsInfoArray: {},
      panelList: [],
      outerParamsInfo: null,
      currentFiledTreeNode: null,
      defaultOuterParamsInfo: {
        paramName: '',
        checked: true,
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
    panelInfo() {
      return this.$store.state.panel.panelInfo
    },
    ...mapState([
      'componentData',
      'canvasStyleData'
    ])
  },
  watch: {},
  created() {
  },
  mounted() {
    this.init()
  },
  destroyed() {
  },
  methods: {
    init() {
      // 获取当前仪表板外部跳转蚕食信息
      queryWithPanelId(this.panelInfo.id).then(rsp => {
        this.outerParams = rsp.data
        this.outerParamsInfoArray = this.outerParams.outerParamsInfoArray
        if (this.outerParamsInfoArray.length > 0) {
          this.outerParamsInfoArray.forEach(outerParamsInfo => {
            this.mapOuterParamsInfoArray[outerParamsInfo.paramsInfoId] = outerParamsInfo
          })
          const firstNode = this.outerParamsInfoArray[0]
          this.$nextTick(() => {
            this.$refs.outerParamsInfoTree.setCurrentKey(firstNode.paramsInfoId)
            this.nodeClick(firstNode)
          })
        }
      })

      this.getPanelViewList(this.panelInfo.id)
    },
    handleExceed(file) {
    },
    cancel() {
      this.$emit('outerParamsSetVisibleChange', false)
    },
    save() {
      if (checkRepeat(this.outerParams.outerParamsInfoArray, 'paramName')) {
        this.$message({
          message: this.$t('panel.repeat_params'),
          type: 'warn',
          showClose: true
        })
        return
      }
      updateOuterParamsSet(this.outerParams).then(rsp => {
        this.$message({
          message: this.$t('commons.save_success'),
          type: 'success',
          showClose: true
        })
        this.cancel()
      })
    },
    nodeClick(data, node) {
      this.outerParamsInfo = this.mapOuterParamsInfoArray[data.paramsInfoId]
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
      this.outerParamsInfo.targetViewInfoList = []
      this.getPanelViewList(data.id)
    },
    inputVal(value) {
      if (!value) {
        this.outerParamsInfo.targetViewInfoList = []
        this.viewIdFieldArrayMap = {}
        this.currentLinkPanelViewArray = []
      }
    },
    addOuterParamsField() {
      this.outerParamsInfo.targetViewInfoList.push({
        targetViewId: '',
        targetFieldId: ''
      })
    },
    deleteOuterParamsField(index) {
      this.outerParamsInfo.targetViewInfoList.splice(index, 1)
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
        this.outerParams.checked = true
      }
      this.$nextTick(() => {
        this.$refs.outerParamsInfoTree.setCurrentKey(data.paramsInfoId)
        this.nodeClick(data)
      })
    },
    addOuterParamsInfo() {
      this.outerParams.checked = true
      const outerParamsInfo = deepCopy(this.defaultOuterParamsInfo)
      outerParamsInfo['paramsInfoId'] = uuid.v1()
      this.outerParamsInfoArray.push(outerParamsInfo)
      this.mapOuterParamsInfoArray[outerParamsInfo.paramsInfoId] = outerParamsInfo
    },
    removeOuterParamsInfo(node, data) {
      const parent = node.parent
      const children = parent.data.children || parent.data
      const index = children.findIndex(d => d.paramsInfoId === data.paramsInfoId)
      children.splice(index, 1)
      if (data.paramsInfoId === this.outerParamsInfo.paramsInfoId) {
        delete this.mapOuterParamsInfoArray[data.paramsInfoId]
        this.outerParamsInfo = null
      }
    }
  }
}
</script>

<style scoped>

  .my_table ::v-deep .el-table__row > td {
    /* 去除表格线 */
    border: none;
    padding: 0 0;
  }

  .my_table ::v-deep .el-table th.is-leaf {
    /* 去除上边框 */
    border: none;
  }

  .my_table ::v-deep .el-table::before {
    /* 去除下边框 */
    height: 0;
  }

  .root-class {
    margin: 15px 0px 5px;
    text-align: center;
  }

  .preview {
    margin-top: 5px;
    border: 1px solid #E6E6E6;
    height: 350px !important;
    overflow: hidden;
    background-size: 100% 100% !important;
  }

  .preview-show {
    border-left: 1px solid #E6E6E6;
    height: 350px;
    background-size: 100% 100% !important;
  }

  .slot-class {
    color: white;
  }

  .bottom {
    margin-top: 15px;
    text-align: center;
  }

  .ellip {
    /*width: 100%;*/
    margin-left: 10px;
    margin-right: 10px;
    overflow: hidden; /*超出部分隐藏*/
    white-space: nowrap; /*不换行*/
    text-overflow: ellipsis; /*超出部分文字以...显示*/
    text-align: center;
    background-color: #f7f8fa;
    color: #3d4d66;
    font-size: 12px;
    line-height: 24px;
    height: 24px;
    border-radius: 3px;
  }

  .select-filed {
    /*width: 100%;*/
    margin-left: 10px;
    margin-right: 10px;
    overflow: hidden; /*超出部分隐藏*/
    white-space: nowrap; /*不换行*/
    text-overflow: ellipsis; /*超出部分文字以...显示*/
    color: #3d4d66;
    font-size: 12px;
    line-height: 35px;
    height: 35px;
    border-radius: 3px;
  }

  ::v-deep .el-popover {
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

  /deep/ .vue-treeselect__control {
    height: 28px;
  }

  /deep/ .vue-treeselect__single-value {
    color: #606266;
    line-height: 28px !important;
  }

  .custom-tree-node {
    flex: 1;
    display: flex;
    align-items: center;
    justify-content: space-between;
    font-size: 14px;
  }

  .auth-span {
    float: right;
    width: 40px;
    margin-right: 5px
  }

  .tree-head {
    height: 30px;
    line-height: 30px;
    border-bottom: 1px solid var(--TableBorderColor, #e6e6e6);
    background-color: var(--SiderBG, #f7f8fa);
    font-size: 12px;
    color: var(--TableColor, #3d4d66);
  }

  .tree-content {
    height: calc(100% - 70px);
    overflow-y: auto;
  }

  .tree-bottom {
    margin-top: 7px;
    text-align: center;
  }

  /deep/ .vue-treeselect__placeholder {
    line-height: 28px
  }

  /deep/ .el-tree--highlight-current .el-tree-node.is-current > .el-tree-node__content {
    background-color: #8dbbef !important;
  }

  .tree-content ::v-deep .el-input__inner {
    background: transparent;
    border: 0px !important;
  }
</style>
