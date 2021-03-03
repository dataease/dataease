<template>
  <el-dialog :title="$t('api_test.request.parameters_advance')"
             :visible.sync="itemValueVisible"
             class="advanced-item-value"
             width="70%">
    <el-tabs tab-position="top" style="height: 50vh;" @tab-click="selectTab">
      <el-tab-pane :label="$t('api_test.request.parameters_advance_mock')">
        <el-row type="flex" :gutter="20">
          <el-col :span="6" class="col-height">
            <div>
              <el-input size="small" v-model="filterText"
                        :placeholder="$t('api_test.request.parameters_mock_filter_tips')"/>
              <el-tree class="filter-tree" ref="tree" :data="mockFuncs" :props="treeProps"
                       default-expand-all @node-click="selectVariable"
                       :filter-node-method="filterNode"></el-tree>
            </div>
          </el-col>
          <el-col :span="6" v-for="(itemFunc, itemIndex) in mockVariableFuncs" :key="itemIndex">
            <div v-for="(func, funcIndex) in funcs"
                 :key="`${itemIndex}-${funcIndex}`">
              <el-row>
                <el-col :span="12">
                  <el-radio size="mini" v-model="itemFunc.name" :label="func.name"
                            @change="methodChange(itemFunc, func)"/>
                </el-col>
                <el-col :span="12" v-if="itemFunc.name === func.name">
                  <div v-for="(p, pIndex) in itemFunc.params" :key="`${itemIndex}-${funcIndex}-${pIndex}`">
                    <el-input :placeholder="p.name" size="mini" v-model="p.value" @change="showPreview"/>
                  </div>
                </el-col>
              </el-row>
            </div>
          </el-col>
        </el-row>
      </el-tab-pane>
      <el-tab-pane :label="$t('api_test.variable')">
        <el-row>
          <el-col :span="6" class="col-height">
            <div v-if="preRequestParams">
              <p>{{ $t('api_test.request.parameters_pre_request') }}</p>
              <el-tree :data="preRequestParams" :props="treeProps" @node-click="selectVariable"></el-tree>
            </div>
          </el-col>
          <el-col :span="18" class="col-height">
            <div>
              <h1>{{ $t('api_test.request.jmeter_func') }}</h1>
              <el-table border :data="jmeterFuncs" class="adjust-table table-content" height="400">
                <el-table-column prop="type" label="Type" width="150"/>
                <el-table-column prop="name" label="Functions" width="250"/>
                <el-table-column prop="description" label="Description"/>
              </el-table>
            </div>
          </el-col>
        </el-row>
      </el-tab-pane>
    </el-tabs>
    <el-form>
      <el-form-item>
        <el-input :placeholder="valueText" size="small"
                  v-model="itemValue"/>
      </el-form-item>
    </el-form>
    <div style="padding-top: 10px;">
      <el-row type="flex" align="middle">
        <el-col :span="12">
          <el-button size="small" type="primary" plain @click="saveAdvanced()">
            {{ $t('commons.save') }}
          </el-button>
          <el-button size="small" type="info" plain @click="addFunc()" v-if="currentTab === 0">
            {{ $t('api_test.request.parameters_advance_add_func') }}
          </el-button>
          <el-button size="small" type="success" plain @click="showPreview()" v-if="currentTab === 0">
            {{ $t('api_test.request.parameters_preview') }}
          </el-button>
        </el-col>
        <el-col>
          <div> {{ itemValuePreview }}</div>
        </el-col>
      </el-row>
    </div>
  </el-dialog>
</template>

<script>
  import {calculate} from "./calculate";
  import {JMETER_FUNC, MOCKJS_FUNC} from "@/common/js/constants";

  export default {
    name: "MsApiVariableAdvance",
    props: {
      parameters: Array,
      currentItem: Object,
    },
    data() {
      return {
        itemValueVisible: false,
        filterText: '',
        environmentParams: [],
        scenarioParams: [],
        Scenario: {},
        preRequests: [],
        preRequestParams: [],
        treeProps: {children: 'children', label: 'name'},
        currentTab: 0,
        itemValue: null,
        itemValuePreview: null,
        funcs: [
          {name: "md5"},
          {name: "base64"},
          {name: "unbase64"},
          {
            name: "substr",
            params: [{name: "start"}, {name: "length"}]
          },
          {
            name: "concat",
            params: [{name: "suffix"}]
          },
          {name: "lconcat", params: [{name: "prefix"}]},
          {name: "sha1"},
          {name: "sha224"},
          {name: "sha256"},
          {name: "sha384"},
          {name: "sha512"},
          {name: "lower"},
          {name: "upper"},
          {name: "length"},
          {name: "number"}
        ],
        mockFuncs: MOCKJS_FUNC.map(f => {
          return {name: f.name, value: f.name}
        }),
        jmeterFuncs: JMETER_FUNC,
        mockVariableFuncs: [],
        jmeterVariableFuncs: [],
      }
    },
    computed: {
      valueText() {
        return this.valuePlaceholder || this.$t("api_test.value");
      }
    },
    mounted() {
      this.prepareData();
    },
    watch: {
      filterText(val) {
        this.$refs.tree.filter(val);
      },
    },
    methods: {
      open() {
        this.itemValueVisible = true;
      },
      prepareData() {

      },
      filterNode(value, data) {
        if (!value) return true;
        return data.name.indexOf(value) !== -1;
      },
      selectVariable(node) {
        this.itemValue = node.value;
      },
      selectTab(tab) {
        this.currentTab = +tab.index;
        this.itemValue = null;
        this.itemValuePreview = null;
      },
      showPreview() {
        // 找到变量本身
        if (!this.itemValue) {
          return;
        }
        let index = this.itemValue.indexOf("|");
        if (index > -1) {
          this.itemValue = this.itemValue.substring(0, index).trim();
        }

        this.mockVariableFuncs.forEach(f => {
          if (!f.name) {
            return;
          }
          this.itemValue += "|" + f.name;
          if (f.params) {
            this.itemValue += ":" + f.params.map(p => p.value).join(",");
          }
        });

        this.itemValuePreview = calculate(this.itemValue);
      },
      methodChange(itemFunc, func) {
        let index = this.mockVariableFuncs.indexOf(itemFunc);
        this.mockVariableFuncs = this.mockVariableFuncs.slice(0, index);
        // 这里要用 deep copy
        this.mockVariableFuncs.push(JSON.parse(JSON.stringify(func)));
        this.showPreview();
      },
      addFunc() {
        if (this.mockVariableFuncs.length > 4) {
          this.$info(this.$t('api_test.request.parameters_advance_add_func_limit'));
          return;
        }
        if (this.mockVariableFuncs.length > 0) {
          let func = this.mockVariableFuncs[this.mockVariableFuncs.length - 1];
          if (!func.name) {
            this.$warning(this.$t('api_test.request.parameters_advance_add_func_error'));
            return;
          }
          if (func.params) {
            for (let j = 0; j < func.params.length; j++) {
              if (!func.params[j].value) {
                this.$warning(this.$t('api_test.request.parameters_advance_add_param_error'));
                return;
              }
            }
          }
        }
        this.mockVariableFuncs.push({name: '', params: []});
      },
      saveAdvanced() {
        this.currentItem.mock = this.itemValue;
        this.itemValueVisible = false;
        this.mockVariableFuncs = [];
        this.$emit('advancedRefresh', this.itemValue);
      }
    }
  }
</script>

<style scoped>
  .col-height {
    height: 40vh;
    overflow: auto;
  }
</style>
