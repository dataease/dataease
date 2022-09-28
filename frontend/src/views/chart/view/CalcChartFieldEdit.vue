<template>
  <el-row>
    <el-form ref="form" :model="fieldForm" size="mini" class="row-style">
      <el-form-item>
        <span style="width: 80px;font-size: 12px">{{ $t('dataset.field_edit_name') }}</span>
        <el-input v-model="fieldForm.name" style="width: 80%;" size="mini" :placeholder="$t('dataset.input_edit_name')" />
      </el-form-item>
    </el-form>

    <el-row :style="mode === 'normal' ? {height: '420px'} : {height: '100px'}">
      <el-col :span="14" style="height: 100%">
        <el-row>
          <el-row v-show="mode === 'normal'">
            <span>
              {{ $t('dataset.field_exp') }}
              <el-tooltip class="item" effect="dark" placement="bottom">
                <div slot="content">
                  表达式语法请遵循该数据源对应的数据库语法。
                  <br>
                  字段类型将使用原始类型，如有需要，请在表达式中自行转换。
                </div>
                <i class="el-icon-info" style="cursor: pointer;" />
              </el-tooltip>
            </span>
            <codemirror
              ref="myCm"
              v-model="fieldForm.originName"
              class="codemirror"
              :options="cmOption"
              @ready="onCmReady"
              @focus="onCmFocus"
              @input="onCmCodeChange"
            />
          </el-row>
          <el-row style="margin-top: 10px;">
            <el-form ref="form" :model="fieldForm" size="mini" class="row-style">
              <el-form-item>
                <span style="width: 80px;font-size: 12px">
                  {{ $t('dataset.data_type') }}
                  <el-tooltip class="item" effect="dark" placement="bottom">
                    <div slot="content">
                      若字段表达式中使用聚合函数，则字段不能设置为维度使用。
                    </div>
                    <i class="el-icon-info" style="cursor: pointer;" />
                  </el-tooltip>
                </span>
                <el-radio-group v-model="fieldForm.groupType" size="mini">
                  <el-radio-button label="d">{{ $t('chart.dimension') }}</el-radio-button>
                  <el-radio-button label="q">{{ $t('chart.quota') }}</el-radio-button>
                </el-radio-group>
              </el-form-item>
              <el-form-item>
                <span style="width: 80px;font-size: 12px">{{ $t('dataset.field_type') }}</span>
                <el-select v-model="fieldForm.deType" size="mini">
                  <el-option
                    v-for="item in fields"
                    :key="item.value"
                    :label="item.label"
                    :value="item.value"
                  >
                    <span style="float: left">
                      <svg-icon v-if="item.value === 0" icon-class="field_text" class="field-icon-text" />
                      <svg-icon v-if="item.value === 1" icon-class="field_time" class="field-icon-time" />
                      <svg-icon
                        v-if="item.value === 2 || item.value === 3"
                        icon-class="field_value"
                        class="field-icon-value"
                      />
                      <svg-icon v-if="item.value === 5" icon-class="field_location" class="field-icon-location" />
                    </span>
                    <span style="float: left; color: #8492a6; font-size: 12px">{{ item.label }}</span>
                  </el-option>
                </el-select>
              </el-form-item>
            </el-form>
          </el-row>
        </el-row>
      </el-col>
      <el-col v-show="mode === 'normal'" :span="10" style="height: 100%;border-left: 1px solid #E6E6E6;">
        <el-col :span="12" style="height: 100%" class="padding-lr">
          <span>
            {{ $t('dataset.click_ref_field') }}
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
            :placeholder="$t('dataset.edit_search')"
            prefix-icon="el-icon-search"
            clearable
          />
          <div class="field-height">
            <span>{{ $t('chart.dimension') }}</span>
            <draggable
              v-if="dimensionData && dimensionData.length > 0"
              v-model="dimensionData"
              :options="{group:{name: 'drag',pull:'clone'},sort: true}"
              animation="300"
              class="drag-list"
              :disabled="true"
            >
              <transition-group>
                <span
                  v-for="item in dimensionData"
                  :key="item.id"
                  class="item-dimension"
                  :title="item.name"
                  @click="insertFieldToCodeMirror('['+item.name+']')"
                >
                  <svg-icon v-if="item.deExtractType === 0" icon-class="field_text" class="field-icon-text" />
                  <svg-icon v-if="item.deExtractType === 1" icon-class="field_time" class="field-icon-time" />
                  <svg-icon
                    v-if="item.deExtractType === 2 || item.deExtractType === 3"
                    icon-class="field_value"
                    class="field-icon-value"
                  />
                  <svg-icon v-if="item.deExtractType === 5" icon-class="field_location" class="field-icon-location" />
                  {{ item.name }}
                </span>
              </transition-group>
            </draggable>
            <div v-else class="class-na">{{ $t('dataset.na') }}</div>
          </div>
          <div class="field-height">
            <span>{{ $t('chart.quota') }}</span>
            <draggable
              v-if="quotaData && quotaData.length > 0"
              v-model="quotaData"
              :options="{group:{name: 'drag',pull:'clone'},sort: true}"
              animation="300"
              class="drag-list"
              :disabled="true"
            >
              <transition-group>
                <span
                  v-for="item in quotaData"
                  :key="item.id"
                  class="item-quota"
                  :title="item.name"
                  @click="insertFieldToCodeMirror('['+item.name+']')"
                >
                  <svg-icon v-if="item.deExtractType === 0" icon-class="field_text" class="field-icon-text" />
                  <svg-icon v-if="item.deExtractType === 1" icon-class="field_time" class="field-icon-time" />
                  <svg-icon
                    v-if="item.deExtractType === 2 || item.deExtractType === 3"
                    icon-class="field_value"
                    class="field-icon-value"
                  />
                  <svg-icon v-if="item.deExtractType === 5" icon-class="field_location" class="field-icon-location" />
                  <span>{{ item.name }}</span>
                </span>
              </transition-group>
            </draggable>
            <div v-else class="class-na">{{ $t('dataset.na') }}</div>
          </div>
        </el-col>
        <el-col :span="12" style="height: 100%" class="padding-lr">
          <span>
            {{ $t('dataset.click_ref_function') }}
            <el-tooltip class="item" effect="dark" placement="bottom">
              <div slot="content">
                使用数据集对应数据库类型所支持的函数，语法同对应数据库
                <br>
                如日期格式化：MySQL使用DATE_FORMAT(date,format)；Oracle使用TO_DATE(X,[,fmt])
                <br>
                非直连模式数据集，使用Doris数据库函数，可参考Doris官网 https://doris.apache.org/zh-CN/
              </div>
              <i class="el-icon-info" style="cursor: pointer;" />
            </el-tooltip>
          </span>
          <el-input
            v-model="searchFunction"
            size="mini"
            :placeholder="$t('dataset.edit_search')"
            prefix-icon="el-icon-search"
            clearable
          />
          <el-row class="function-height">
            <el-popover
              v-for="(item,index) in functionData"
              :key="index"
              class="function-pop"
              placement="right"
              width="200"
              trigger="hover"
              :open-delay="500"
            >
              <p class="pop-title">{{ item.name }}</p>
              <p class="pop-info">{{ item.func }}</p>
              <p class="pop-info">{{ item.desc }}</p>
              <span slot="reference" class="function-style" @click="insertParamToCodeMirror(item.func)">{{
                item.func
              }}</span>
            </el-popover>
          </el-row>
        </el-col>
      </el-col>
    </el-row>

    <el-row>
      <div class="dialog-button">
        <el-button size="mini" @click="closeCalcField">{{ $t('dataset.cancel') }}</el-button>
        <el-button
          :disabled="!fieldForm.name || !fieldForm.originName"
          type="primary"
          size="mini"
          :loading="loading"
          @click="saveCalcField"
        >{{ $t('dataset.confirm') }}
        </el-button>
      </div>
    </el-row>
  </el-row>
</template>

<script>
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
import { post } from '../../../api/dataset/dataset'

export default {
  name: 'CalcChartFieldEdit',
  components: { codemirror, draggable },
  props: {
    param: {// chart
      type: Object,
      required: true
    },
    field: {
      type: Object,
      required: true
    },
    mode: {
      type: String,
      required: false,
      default: 'normal'
    }
  },
  data() {
    return {
      fieldForm: {
        id: null,
        name: '',
        groupType: 'd',
        deType: 0,
        originName: '',
        tableId: this.param.tableId,
        checked: 1,
        columnIndex: 0,
        size: 0,
        extField: 2,
        chartId: this.param.id
      },
      cmOption: {
        tabSize: 2,
        styleActiveLine: true,
        lineNumbers: true,
        line: true,
        mode: 'text/x-sql',
        theme: 'solarized',
        hintOptions: { // 自定义提示选项
          completeSingle: false // 当匹配只有一项的时候是否自动补全
        }
      },
      fields: [
        { label: this.$t('dataset.text'), value: 0 },
        { label: this.$t('dataset.time'), value: 1 },
        { label: this.$t('dataset.value'), value: 2 },
        { label: this.$t('dataset.value') + '(' + this.$t('dataset.float') + ')', value: 3 },
        { label: this.$t('dataset.location'), value: 5 }
      ],
      functions: [],
      searchField: '',
      searchFunction: '',
      dimensionData: [],
      quotaData: [],
      functionData: [],
      tableFields: {},
      name2Auto: [],
      loading: false
    }
  },
  computed: {
    codemirror() {
      return this.$refs.myCm.codemirror
    },
    panelInfo() {
      return this.$store.state.panel.panelInfo
    }
  },
  watch: {
    'param': function() {
      this.initFunctions()
      this.initDsFields()
    },
    'field': {
      handler: function() {
        this.initField()
      },
      deep: true
    },
    'tableFields': function() {
      this.dimensionData = JSON.parse(JSON.stringify(this.tableFields.dimensionList)).filter(ele => ele.extField === 0)
      this.quotaData = JSON.parse(JSON.stringify(this.tableFields.quotaList)).filter(ele => ele.extField === 0)
    },
    'searchField': function(val) {
      if (val && val !== '') {
        this.dimensionData = JSON.parse(JSON.stringify(this.tableFields.dimensionList.filter(ele => ele.name.toLocaleLowerCase().includes(val.toLocaleLowerCase()) && ele.extField === 0)))
        this.quotaData = JSON.parse(JSON.stringify(this.tableFields.quotaList.filter(ele => ele.name.toLocaleLowerCase().includes(val.toLocaleLowerCase()) && ele.extField === 0)))
      } else {
        this.dimensionData = JSON.parse(JSON.stringify(this.tableFields.dimensionList)).filter(ele => ele.extField === 0)
        this.quotaData = JSON.parse(JSON.stringify(this.tableFields.quotaList)).filter(ele => ele.extField === 0)
      }
    },
    'searchFunction': function(val) {
      if (val && val !== '') {
        this.functionData = JSON.parse(JSON.stringify(this.functions.filter(ele => {
          return ele.func.toLocaleLowerCase().includes(val.toLocaleLowerCase())
        })))
      } else {
        this.functionData = JSON.parse(JSON.stringify(this.functions))
      }
    }
  },
  mounted() {
    this.loading = false
    this.$refs.myCm.codemirror.on('keypress', () => {
      this.$refs.myCm.codemirror.showHint()
    })
    this.initFunctions()
    this.initDsFields()
  },
  methods: {
    onCmReady(cm) {
      this.codemirror.setSize('-webkit-fill-available', 'auto')
    },
    onCmFocus(cm) {
    },
    onCmCodeChange(newCode) {
      this.fieldForm.originName = newCode
    },
    insertParamToCodeMirror(param) {
      const pos1 = this.$refs.myCm.codemirror.getCursor()
      const pos2 = {}
      pos2.line = pos1.line
      pos2.ch = pos1.ch
      this.$refs.myCm.codemirror.replaceRange(param, pos2)
    },
    insertFieldToCodeMirror(param) {
      const pos1 = this.$refs.myCm.codemirror.getCursor()
      const pos2 = {}
      pos2.line = pos1.line
      pos2.ch = pos1.ch
      this.$refs.myCm.codemirror.replaceRange(param, pos2)
      this.$refs.myCm.codemirror.markText(pos2, { line: pos2.line, ch: param.length + pos2.ch }, { atomic: true, selectRight: true })
    },

    initFunctions() {
      post('/dataset/function/listByTableId/' + this.param.tableId, null).then(response => {
        this.functions = response.data
        this.functionData = JSON.parse(JSON.stringify(this.functions))
      })
    },

    initField() {
      if (this.field.id || this.mode === 'copy') {
        this.fieldForm = JSON.parse(JSON.stringify(this.field))
        this.name2Auto = []
        this.fieldForm.originName = this.setNameIdTrans('id', 'name', this.fieldForm.originName, this.name2Auto)
        setTimeout(() => {
          this.matchToAuto()
        }, 500)
      } else {
        this.fieldForm = JSON.parse(JSON.stringify(this.fieldForm))
      }
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

    closeCalcField() {
      this.resetField()
      this.$emit('onEditClose', {})
    },

    setNameIdTrans(from, to, originName, name2Auto) {
      let name2Id = originName
      const nameIdMap = [...this.dimensionData, ...this.quotaData].reduce((pre, next) => {
        pre[next[from]] = next[to]
        return pre
      }, {})
      const on = originName.match(/\[(.+?)\]/g)
      if (on) {
        on.forEach(itm => {
          const ele = itm.slice(1, -1)
          if (name2Auto) {
            name2Auto.push(nameIdMap[ele])
          }
          name2Id = name2Id.replace(ele, nameIdMap[ele])
        })
      }
      return name2Id
    },

    saveCalcField() {
      const { id, name = [], deType, originName } = this.fieldForm
      if (name.length > 50) {
        this.$message.error(this.$t('dataset.field_name_less_50'))
        return
      }
      if (!id) {
        this.fieldForm.type = deType
        this.fieldForm.deExtractType = deType
        this.fieldForm.tableId = this.param.tableId
        this.fieldForm.columnIndex = 0
        this.fieldForm.chartId = this.param.id
      }
      this.loading = true
      post('/chart/field/save/' + this.panelInfo.id, { ...this.fieldForm, originName: this.setNameIdTrans('name', 'id', originName) }).then(response => {
        this.closeCalcField()
        this.loading = false
      }).catch(res => {
        this.loading = false
      })
    },

    resetField() {
      this.fieldForm = {
        id: null,
        name: '',
        groupType: 'd',
        deType: 0,
        originName: '',
        tableId: this.param.tableId,
        checked: 1,
        columnIndex: 0,
        size: 0,
        extField: 2,
        chartId: this.param.id
      }
      this.dimensionData = []
      this.quotaData = []
      this.searchField = ''
      this.searchFunction = ''
    },

    initDsFields() {
      post('/dataset/field/listByDQ/' + this.param.tableId, null).then(response => {
        this.tableFields = response.data
        this.tableFields.dimensionListData = JSON.parse(JSON.stringify(this.tableFields.dimensionList))
        this.tableFields.quotaListData = JSON.parse(JSON.stringify(this.tableFields.quotaList))

        this.dimensionData = JSON.parse(JSON.stringify(this.tableFields.dimensionList)).filter(ele => ele.extField === 0)
        this.quotaData = JSON.parse(JSON.stringify(this.tableFields.quotaList)).filter(ele => ele.extField === 0)

        this.initField()
      })
    }
  }
}
</script>

<style scoped>
.row-style ::v-deep .el-form-item__label {
  font-size: 12px;
}

.row-style ::v-deep .el-form-item--mini.el-form-item {
  margin-bottom: 10px;
}

.row-style ::v-deep .el-form-item__content {
  display: flex;
  flex-direction: row;
  width: 100%;
}

.codemirror {
  height: 300px;
  overflow-y: auto;
  font-size: 12px;
}

.codemirror ::v-deep .CodeMirror-scroll {
  height: 300px;
  overflow-y: auto;
  font-size: 12px;
}

.padding-lr {
  padding: 0 4px;
}

.field-height {
  height: calc(50% - 25px);
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

.function-style {
  color: #0a7be0;
  display: block;
  padding: 2px 4px;
  cursor: pointer;
  margin: 4px 0;
  word-break: break-word;
  border: solid 1px #eee;
}

.blackTheme .function-style {
  border: solid 1px;
  border-color: #495865;
  color: #F2F6FC;
  background-color: var(--MainBG);
}

.function-style:hover {
  background: #e8f4ff;
  border-color: #a3d3ff;
  cursor: pointer;
}

.blackTheme .function-style:hover {
  color: #1890ff;
  background: var(--ContentBG);
}

.function-height {
  height: calc(100% - 50px);
  overflow: auto;
  margin-top: 4px;
}

.function-pop ::v-deep .el-popover {
  padding: 6px !important;
}

.pop-title {
  margin: 6px 0 0 0;
  font-size: 14px;
  font-weight: 500;
}

.pop-info {
  margin: 6px 0 0 0;
  font-size: 10px;
}

.dialog-button {
  float: right;
  margin-top: 10px;
}

.class-na {
  margin-top: 8px;
  text-align: center;
  font-size: 14px;
  color: var(--deTextDisable);
}
</style>
