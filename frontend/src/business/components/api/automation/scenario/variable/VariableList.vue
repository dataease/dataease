<template>
  <el-dialog title="场景变量" :close-on-click-modal="false"
             :visible.sync="visible" class="visible-dialog" width="60%"
             @close="close" v-loading="loading">
    <el-tabs v-model="activeName">
      <el-tab-pane :label="$t('api_test.scenario.variables')" name="variable">
        <div style="margin-top: 10px">
          <el-row style="margin-bottom: 10px">
            <el-col :span="8">
              <el-input placeholder="变量名称搜索" v-model="selectVariable" size="small" @change="filter" @keyup.enter="filter">
                <el-select v-model="searchType" slot="prepend" placeholder="类型" style="width: 90px" @change="filter">
                  <el-option value="CONSTANT" label="常量"></el-option>
                  <el-option value="LIST" label="列表"></el-option>
                  <el-option value="CSV" label="CSV"></el-option>
                  <el-option value="COUNTER" label="计数器"></el-option>
                  <el-option value="RANDOM" label="随机数"></el-option>
                </el-select>
              </el-input>
            </el-col>

            <el-col :span="6">
              <el-dropdown split-button type="primary" @command="handleClick" @click="handleClick('CONSTANT')" size="small" style="margin-left: 10px">
                {{$t('commons.add')}}
                <el-dropdown-menu slot="dropdown">
                  <el-dropdown-item command="CONSTANT">常量</el-dropdown-item>
                  <el-dropdown-item command="LIST">列表</el-dropdown-item>
                  <el-dropdown-item command="CSV">CSV</el-dropdown-item>
                  <el-dropdown-item command="COUNTER">计数器</el-dropdown-item>
                  <el-dropdown-item command="RANDOM">随机数</el-dropdown-item>
                </el-dropdown-menu>
              </el-dropdown>
              <el-button size="small" style="margin-left: 10px" @click="deleteVariable">{{$t('commons.delete')}}</el-button>

            </el-col>
          </el-row>
          <el-row>
            <el-col :span="12">
              <div style="border:1px #DCDFE6 solid; min-height: 400px;border-radius: 4px ;width: 100% ;">
                <el-table ref="table" border :data="variables" class="adjust-table" @select-all="select" @select="select"
                          v-loading="loading" @row-click="edit" height="400px" :row-class-name="tableRowClassName">
                  <el-table-column type="selection" width="38"/>
                  <el-table-column prop="num" label="ID" sortable/>
                  <el-table-column prop="name" :label="$t('api_test.variable_name')" sortable show-overflow-tooltip/>
                  <el-table-column prop="type" :label="$t('test_track.case.type')">
                    <template v-slot:default="scope">
                      <span>{{types.get(scope.row.type)}}</span>
                    </template>
                  </el-table-column>
                  <el-table-column prop="value" :label="$t('api_test.value')" show-overflow-tooltip/>
                </el-table>
              </div>
            </el-col>
            <el-col :span="12">
              <ms-edit-constant v-if="editData.type=='CONSTANT'" ref="parameters" :editData.sync="editData"/>
              <ms-edit-counter v-if="editData.type=='COUNTER'" ref="counter" :editData.sync="editData"/>
              <ms-edit-random v-if="editData.type=='RANDOM'" ref="random" :editData.sync="editData"/>
              <ms-edit-list-value v-if="editData.type=='LIST'" ref="listValue" :editData="editData"/>
              <ms-edit-csv v-if="editData.type=='CSV'" ref="csv" :editData.sync="editData"/>
            </el-col>
          </el-row>
        </div>


      </el-tab-pane>
      <el-tab-pane :label="$t('api_test.scenario.headers')" name="headers">
        <!-- 请求头-->
        <el-tooltip class="item-tabs" effect="dark" :content="$t('api_test.request.headers')" placement="top-start" slot="label">
          <span>{{$t('api_test.request.headers')}}
            <div class="el-step__icon is-text ms-api-col ms-variable-header" v-if="headers.length>1">
              <div class="el-step__icon-inner">{{headers.length-1}}</div>
            </div>
          </span>
        </el-tooltip>
        <el-row>
          <el-link class="ms-variable-link" @click="batchAdd" style="color: #783887"> {{$t("commons.batch_add")}}</el-link>
        </el-row>
        <div style="min-height: 400px">
          <ms-api-key-value :items="headers"/>
          <batch-add-parameter @batchSave="batchSave" ref="batchAddParameter"/>
        </div>
      </el-tab-pane>
    </el-tabs>
    <template v-slot:footer>
      <div>
        <el-button type="primary" @click="save">{{$t('commons.confirm')}}</el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script>
  import MsEditConstant from "./EditConstant";
  import MsDialogFooter from "../../../../common/components/MsDialogFooter";
  import MsTableHeader from "@/business/components/common/components/MsTableHeader";
  import MsTablePagination from "@/business/components/common/pagination/TablePagination";
  import MsEditCounter from "./EditCounter";
  import MsEditRandom from "./EditRandom";
  import MsEditListValue from "./EditListValue";
  import MsEditCsv from "./EditCsv";
  import {getUUID} from "@/common/js/utils";
  import MsApiKeyValue from "../../../definition/components/ApiKeyValue";
  import BatchAddParameter from "../../../definition/components/basis/BatchAddParameter";
  import {KeyValue} from "../../../definition/model/ApiTestModel";

  export default {
    name: "MsVariableList",
    components: {
      MsEditConstant,
      MsDialogFooter,
      MsTableHeader,
      MsTablePagination,
      MsEditCounter,
      MsEditRandom,
      MsEditListValue,
      MsEditCsv,
      MsApiKeyValue,
      BatchAddParameter
    },
    data() {
      return {
        variables: [],
        headers: [],
        activeName: "variable",
        searchType: "",
        selectVariable: "",
        condition: {},
        types: new Map([
          ['CONSTANT', '常量'],
          ['LIST', '列表'],
          ['CSV', 'CSV'],
          ['COUNTER', '计数器'],
          ['RANDOM', '随机数']
        ]),
        visible: false,
        selection: [],
        loading: false,
        currentPage: 1,
        editData: {},
        pageSize: 10,
        total: 0,
      }
    },
    methods: {
      batchAdd() {
        this.$refs.batchAddParameter.open();
      },
      batchSave(data) {
        if (data) {
          let params = data.split("\n");
          let keyValues = [];
          params.forEach(item => {
            let line = item.split(/，|,/);
            let required = false;
            if (line[1] === '必填' || line[1] === 'true') {
              required = true;
            }
            keyValues.push(new KeyValue({name: line[0], required: required, value: line[2], description: line[3], type: "text", valid: false, file: false, encode: true, enable: true, contentType: "text/plain"}));
          })
          keyValues.forEach(item => {
            this.headers.unshift(item);
          })
        }
      },
      handleClick(command) {
        this.editData = {};
        this.editData.type = command;
        this.addParameters(this.editData);
      },
      edit(row) {
        this.editData = row;
      },
      tableRowClassName(row) {
        if (row.row.hidden) {
          return 'ms-variable-hidden-row';
        }
        return '';
      },
      addParameters(v) {
        v.id = getUUID();
        if (v.type === 'CSV') {
          v.delimiter = ",";
        }
        this.variables.push(v);
        let index = 1;
        this.variables.forEach(item => {
          item.num = index;
          index++;
        })
      },
      select(selection) {
        this.selection = selection.map(s => s.id);
      },
      isSelect(row) {
        return this.selection.includes(row.id)
      },
      open: function (variables, headers) {
        this.variables = variables;
        this.headers = headers;
        this.visible = true;
        this.editData = {type: "CONSTANT"};
        this.addParameters(this.editData);
      },
      save() {
        this.visible = false;
      },
      close() {
        this.visible = false;
        let saveVariables = [];
        this.variables.forEach(item => {
          item.hidden = undefined;
          if (item.name && item.name != "") {
            saveVariables.push(item);
          }
        })
        this.selectVariable = "";
        this.searchType = "";
        this.$emit('setVariables', saveVariables, this.headers);
      },
      deleteVariable() {
        let ids = Array.from(this.selection);
        if (ids.length == 0) {
          this.$warning("请选择一条数据删除");
          return;
        }
        ids.forEach(row => {
          const index = this.variables.findIndex(d => d.id === row);
          this.variables.splice(index, 1);
        })
        this.selection = [];
      },
      filter() {
        let datas = [];
        this.variables.forEach(item => {
          if (this.searchType && this.searchType != "" && this.selectVariable && this.selectVariable != "") {
            if ((item.type && item.type.toLowerCase().indexOf(this.searchType.toLowerCase()) == -1) || (item.name && item.name.toLowerCase().indexOf(this.selectVariable.toLowerCase()) == -1)) {
              item.hidden = true;
            } else {
              item.hidden = undefined;
            }
          }
          else if (this.selectVariable && this.selectVariable != "") {
            if (item.name && item.name.toLowerCase().indexOf(this.selectVariable.toLowerCase()) == -1) {
              item.hidden = true;
            } else {
              item.hidden = undefined;
            }
          }
          else if (this.searchType && this.searchType != "") {
            if (item.type && item.type.toLowerCase().indexOf(this.searchType.toLowerCase()) == -1) {
              item.hidden = true;
            } else {
              item.hidden = undefined;
            }
          } else {
            item.hidden = undefined;
          }
          datas.push(item);
        })
        this.variables = datas;
      },
      createFilter(queryString) {
        return item => {
          return (item.type && item.type.toLowerCase().indexOf(queryString.toLowerCase()) !== -1);
        };
      },
    }
  }
</script>

<style>
  .ms-variable-hidden-row {
    display: none;
  }

  .ms-variable-header {
    background: #783887;
    color: white;
    height: 18px;
    border-radius: 42%;
  }

  .ms-variable-link {
    float: right;
    margin-right: 45px;
  }
</style>
