<template>
  <el-row>
    <el-form ref="form" :model="fieldForm" size="mini" class="row-style">
      <el-form-item>
        <span style="width: 60px;font-size: 12px">{{ $t('dataset.field_name') }}</span>
        <el-input v-model="fieldForm.name" size="mini" :placeholder="$t('dataset.input_name')" />
      </el-form-item>
    </el-form>

    <el-row style="height: 420px;">
      <el-col :span="14" style="height: 100%">
        <el-row>
          <el-row>
            <span>{{ $t('dataset.field_exp') }}</span>
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
                <span style="width: 80px;font-size: 12px">{{ $t('dataset.data_type') }}</span>
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
                      <svg-icon v-if="item.value === 2 || item.value === 3" icon-class="field_value" class="field-icon-value" />
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
      <el-col :span="10" style="height: 100%;border-left: 1px solid #E6E6E6;">
        <el-col :span="12" style="height: 100%" class="padding-lr">
          <span>{{ $t('dataset.click_ref_field') }}</span>
          <el-input
            v-model="searchField"
            size="mini"
            :placeholder="$t('dataset.search')"
            prefix-icon="el-icon-search"
            clearable
          />
          <div class="field-height">
            <span>{{ $t('chart.dimension') }}</span>
            <draggable
              v-model="dimensionData"
              :options="{group:{name: 'drag',pull:'clone'},sort: true}"
              animation="300"
              class="drag-list"
              :disabled="true"
            >
              <transition-group>
                <span v-for="item in dimensionData" :key="item.id" class="item-dimension" :title="item.name" @click="insertParamToCodeMirror('['+item.id+']')">
                  <svg-icon v-if="item.deType === 0" icon-class="field_text" class="field-icon-text" />
                  <svg-icon v-if="item.deType === 1" icon-class="field_time" class="field-icon-time" />
                  <svg-icon v-if="item.deType === 2 || item.deType === 3" icon-class="field_value" class="field-icon-value" />
                  <svg-icon v-if="item.deType === 5" icon-class="field_location" class="field-icon-location" />
                  {{ item.name }}
                </span>
              </transition-group>
            </draggable>
          </div>
          <div class="field-height">
            <span>{{ $t('chart.quota') }}</span>
            <draggable
              v-model="quotaData"
              :options="{group:{name: 'drag',pull:'clone'},sort: true}"
              animation="300"
              class="drag-list"
              :disabled="true"
            >
              <transition-group>
                <span v-for="item in quotaData" :key="item.id" class="item-quota" :title="item.name" @click="insertParamToCodeMirror('['+item.id+']')">
                  <svg-icon v-if="item.deType === 0" icon-class="field_text" class="field-icon-text" />
                  <svg-icon v-if="item.deType === 1" icon-class="field_time" class="field-icon-time" />
                  <svg-icon v-if="item.deType === 2 || item.deType === 3" icon-class="field_value" class="field-icon-value" />
                  <svg-icon v-if="item.deType === 5" icon-class="field_location" class="field-icon-location" />
                  <span>{{ item.name }}</span>
                </span>
              </transition-group>
            </draggable>
          </div>
        </el-col>
        <el-col :span="12" style="height: 100%" class="padding-lr">
          <span>{{ $t('dataset.click_ref_function') }}</span>
          <el-input
            v-model="searchFunction"
            size="mini"
            :placeholder="$t('dataset.search')"
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
              <span slot="reference" class="function-style" :title="item.func" @click="insertParamToCodeMirror(item.func)">{{ item.func }}</span>
            </el-popover>
          </el-row>
        </el-col>
      </el-col>
    </el-row>

    <el-row>
      <div class="dialog-button">
        <el-button size="mini" @click="closeCalcField">{{ $t('dataset.cancel') }}</el-button>
        <el-button :disabled="!fieldForm.name || !fieldForm.originName" type="primary" size="mini" @click="saveCalcField">{{ $t('dataset.confirm') }}</el-button>
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
  name: 'CalcFieldEdit',
  components: { codemirror, draggable },
  props: {
    param: {
      type: Object,
      required: true
    },
    tableFields: {
      type: Object,
      required: true
    },
    field: {
      type: Object,
      required: true
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
        tableId: this.param.id,
        checked: 1,
        columnIndex: this.tableFields.dimensionList.length + this.tableFields.quotaList.length,
        size: 0,
        extField: 2
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
      functionData: []
    }
  },
  computed: {
    codemirror() {
      return this.$refs.myCm.codemirror
    }
  },
  watch: {
    'param': function() {
      this.initFunctions()
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
        this.functionData = JSON.parse(JSON.stringify(this.functions.filter(ele => { return ele.func.toLocaleLowerCase().includes(val.toLocaleLowerCase()) })))
      } else {
        this.functionData = JSON.parse(JSON.stringify(this.functions))
      }
    }
  },
  mounted() {
    this.$refs.myCm.codemirror.on('keypress', () => {
      this.$refs.myCm.codemirror.showHint()
    })
    this.initFunctions()
    this.initField()
    this.dimensionData = JSON.parse(JSON.stringify(this.tableFields.dimensionList)).filter(ele => ele.extField === 0)
    this.quotaData = JSON.parse(JSON.stringify(this.tableFields.quotaList)).filter(ele => ele.extField === 0)
  },
  methods: {
    onCmReady(cm) {
      this.codemirror.setSize('-webkit-fill-available', 'auto')
    },
    onCmFocus(cm) {
      // console.log('the editor is focus!', cm)
    },
    onCmCodeChange(newCode) {
      // console.log(newCode)
      this.fieldForm.originName = newCode
    },
    insertParamToCodeMirror(param) {
      const pos1 = this.$refs.myCm.codemirror.getCursor()
      const pos2 = {}
      pos2.line = pos1.line
      pos2.ch = pos1.ch
      this.$refs.myCm.codemirror.replaceRange(param, pos2)
    },

    initFunctions() {
      post('/dataset/function/listByTableId/' + this.param.id, null).then(response => {
        this.functions = response.data
        this.functionData = JSON.parse(JSON.stringify(this.functions))
      })
    },

    initField() {
      if (this.field.id) {
        this.fieldForm = JSON.parse(JSON.stringify(this.field))
      } else {
        this.fieldForm = JSON.parse(JSON.stringify(this.fieldForm))
      }
    },

    closeCalcField() {
      this.resetField()
      this.$emit('onEditClose', {})
    },

    saveCalcField() {
      if (!this.fieldForm.id) {
        this.fieldForm.type = this.fieldForm.deType
        this.fieldForm.deExtractType = this.fieldForm.deType
        this.fieldForm.tableId = this.param.id
        this.fieldForm.columnIndex = this.tableFields.dimensionList.length + this.tableFields.quotaList.length
      }
      post('/dataset/field/save', this.fieldForm).then(response => {
        this.closeCalcField()
      })
    },

    resetField() {
      this.fieldForm = {
        id: null,
        name: '',
        groupType: 'd',
        deType: 0,
        originName: '',
        tableId: this.param.id,
        checked: 1,
        columnIndex: this.tableFields.dimensionList.length + this.tableFields.quotaList.length,
        size: 0,
        extField: 2
      }
      this.dimensionData = []
      this.quotaData = []
      this.searchField = ''
      this.searchFunction = ''
    }
  }
}
</script>

<style scoped>
  .row-style>>>.el-form-item__label{
    font-size: 12px;
  }
  .row-style>>>.el-form-item--mini.el-form-item{
    margin-bottom: 10px;
  }
  .row-style>>>.el-form-item__content{
    display: flex;
    flex-direction: row;
    width: 100%;
  }

  .codemirror {
    height: 300px;
    overflow-y: auto;
    font-size: 12px;
  }
  .codemirror >>> .CodeMirror-scroll {
    height: 300px;
    overflow-y: auto;
    font-size: 12px;
  }

  .padding-lr {
    padding: 0 4px;
  }
  .field-height{
    height: calc(50% - 25px);
    margin-top: 4px;
  }
  .drag-list {
    height: calc(100% - 26px);
    overflow:auto;
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

  .item-dimension + .item-dimension {
    margin-top: 2px;
  }

  .item-dimension:hover {
    color: #1890ff;
    background: #e8f4ff;
    border-color: #a3d3ff;
    cursor: pointer;
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

  .item-quota + .item-quota {
    margin-top: 2px;
  }

  .item-quota:hover {
    color: #67c23a;
    background: #f0f9eb;
    border-color: #b2d3a3;
    cursor: pointer;
  }
  span{
    font-size: 12px;
  }
  .function-style{
    color: #0a7be0;
    display: block;
    padding: 2px 4px;
    cursor: pointer;
    margin: 4px 0;
    overflow-x: hidden;
    white-space: nowrap;
    text-overflow: ellipsis;
  }
  .function-height{
    height: calc(100% - 50px);
    overflow: auto;
    margin-top: 4px;
  }
  .function-pop>>>.el-popover{
    padding: 6px!important;
  }
  .pop-title{
    margin: 6px 0 0 0;
    font-size: 14px;
    font-weight: 500;
  }
  .pop-info{
    margin: 6px 0 0 0;
    font-size: 10px;
  }
  .dialog-button{
    float: right;
    margin-top: 10px;
  }
</style>
