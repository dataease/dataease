<template>
  <div class="dataset-sql" @mouseup="mouseupDrag">
    <!-- <el-input v-model="name" size="mini" :placeholder="$t('commons.name')"/> -->

    <!-- <el-button size="mini" @click="cancel">
            {{ $t('dataset.cancel') }}
          </el-button>
          <el-button size="mini" type="primary" @click="save">
            {{ $t('dataset.confirm') }}
          </el-button> -->

    <div class="sql-editer" :style="{ height: sqlHeight + 'px' }">
      <el-row>
        <el-col :span="12">
          <el-select
            v-model="dataSource"
            filterable
            style="width: 215px"
            :placeholder="$t('dataset.pls_slc_data_source')"
            size="small"
            @change="changeDatasource()"
          >
            <el-option
              v-for="item in options"
              :key="item.id"
              :label="item.name"
              :value="item.id"
            />
          </el-select>
          <el-select
            v-if="!param.tableId"
            v-model="mode"
            style="width: 120px; margin: 0 12px"
            filterable
            :placeholder="$t('dataset.connect_mode')"
            size="small"
          >
            <el-option :label="$t('dataset.direct_connect')" value="0" />
            <el-option
              :label="$t('dataset.sync_data')"
              value="1"
              :disabled="disabledSync"
            />
          </el-select>
          <el-select
            v-if="mode === '1'"
            v-model="syncType"
            style="width: 120px"
            filterable
            :placeholder="$t('dataset.connect_mode')"
            size="small"
          >
            <el-option
              :label="$t('dataset.sync_now')"
              value="sync_now"
              :disabled="engineMode === 'simple'"
            />
            <el-option :label="$t('dataset.sync_latter')" value="sync_latter" />
          </el-select>
        </el-col>
        <el-col style="text-align: right" :span="12">
          <el-button
            icon="el-icon-s-operation"
            type="text"
            size="small"
            class="de-text-btn"
            @click="variableMgm"
          >
            {{ $t("sql_variable.variable_mgm") }}
          </el-button>
          <el-button
            icon="el-icon-video-play"
            class="de-text-btn"
            type="text"
            size="small"
            @click="getSQLPreview"
          >
            {{ $t("deDataset.run_a_query") }}
          </el-button>
        </el-col>
      </el-row>
      <div class="code-container">
        <codemirror
          ref="myCm"
          v-model="sql"
          class="codemirror"
          :options="sqlOption"
          @ready="onCmReady"
          @focus="onCmFocus"
          @input="onCmCodeChange"
        />
      </div>
    </div>
    <div class="sql-result">
      <div class="sql-title">
        {{ $t("deDataset.running_results") }}
        <span class="result-num">{{
          `(${$t("dataset.preview_show")} 1000 ${$t("dataset.preview_item")})`
        }}</span>

        <span @mousedown="mousedownDrag" class="drag"></span>
      </div>
      <div class="table-sql">
        <el-empty
          :image-size="60"
          v-if="errMsg"
          :image="errImg"
          :description="$t('deDataset.run_failed')"
        ></el-empty>
        <ux-grid
          v-else
          ref="plxTable"
          size="mini"
          style="width: 100%"
          :height="height"
          :checkbox-config="{ highlight: true }"
          :width-resize="true"
        >
          <ux-table-column
            v-for="field in fields"
            :key="field.fieldName"
            min-width="200px"
            :field="field.fieldName"
            :title="field.remarks"
            :resizable="true"
          />
        </ux-grid>
      </div>
      <el-drawer
        :title="dialogTitle"
        :visible.sync="showVariableMgm"
        custom-class="user-drawer sql-dataset-drawer"
        size="840px"
        v-closePress
        direction="rtl"
      >
        <div class="content">
          <i class="el-icon-info"></i> {{ $t("dataset.sql_variable_limit_1")
          }}<br />
          {{ $t("dataset.sql_variable_limit_2") }}<br />
        </div>
        <el-table :data="variablesTmp">
          <el-table-column prop="variableName" :label="$t('panel.param_name')">
          </el-table-column>
          <el-table-column width="200" :label="$t('deDataset.parameter_type')">
            <template slot-scope="scope">
              <el-cascader
                v-model="scope.row.type"
                size="mini"
                class="select-type"
                :options="fieldOptions"
                @change="variableTypeChange(scope.row)"
              >
              </el-cascader>
              <span class="select-svg-icon">
                <svg-icon
                  v-if="scope.row.type[0] === 'TEXT'"
                  icon-class="field_text"
                  class="field-icon-text"
                />
                <svg-icon
                  v-if="
                    [
                      'DATETIME-YEAR',
                      'DATETIME-YEAR-MONTH',
                      'DATETIME',
                      'DATETIME-YEAR-MONTH-DAY',
                    ].includes(scope.row.type[0])
                  "
                  icon-class="field_time"
                  class="field-icon-time"
                />
                <svg-icon
                  v-if="['LONG', 'DOUBLE'].includes(scope.row.type[0])"
                  icon-class="field_value"
                  class="field-icon-value"
                />
                <!-- <svg-icon
                    v-if="scope.row.deType === 5"
                    icon-class="field_location"
                    class="field-icon-location"
                  /> -->
              </span>
            </template>
          </el-table-column>
          <el-table-column
            min-width="350"
            prop="defaultValue"
            :label="$t('commons.params_value')"
          >
            <template slot-scope="scope">
              <el-input
                size="small"
                v-if="scope.row.type[0] === 'TEXT'"
                type="text"
                :placeholder="$t('fu.search_bar.please_input')"
                v-model="scope.row.defaultValue"
              />
              <el-input
                size="small"
                v-if="
                  scope.row.type[0] === 'LONG' || scope.row.type[0] === 'DOUBLE'
                "
                :placeholder="$t('fu.search_bar.please_input')"
                type="number"
                v-model="scope.row.defaultValue"
              />

              <el-date-picker
                v-if="scope.row.type[0] === 'DATETIME-YEAR'"
                v-model="scope.row.defaultValue"
                type="year"
                size="small"
                value-format="yyyy"
                :placeholder="$t('dataset.select_year')"
              >
              </el-date-picker>

              <el-date-picker
                v-if="scope.row.type[0] === 'DATETIME-YEAR-MONTH'"
                v-model="scope.row.defaultValue"
                type="month"
                size="small"
                :format="scope.row.type[1]"
                :value-format="scope.row.type[1]"
                :placeholder="$t('dataset.select_month')"
              >
              </el-date-picker>

              <el-date-picker
                v-if="scope.row.type[0] === 'DATETIME-YEAR-MONTH-DAY'"
                v-model="scope.row.defaultValue"
                type="date"
                size="small"
                :format="scope.row.type[1]"
                :value-format="scope.row.type[1]"
                :placeholder="$t('dataset.select_date')"
              >
              </el-date-picker>

              <el-date-picker
                v-if="scope.row.type[0] === 'DATETIME'"
                v-model="scope.row.defaultValue"
                type="datetime"
                size="small"
                :format="scope.row.type[1]"
                :value-format="scope.row.type[1]"
                :placeholder="$t('dataset.select_time')"
              >
              </el-date-picker>
            </template>
          </el-table-column>
        </el-table>
        <div class="de-foot">
          <deBtn secondary @click="closeVariableMgm">{{
            $t("dataset.cancel")
          }}</deBtn>
          <deBtn type="primary" @click="saveVariable()">{{
            $t("dataset.confirm")
          }}</deBtn>
        </div>
      </el-drawer>
    </div>
  </div>
</template>

<script>
import { post, listDatasource, isKettleRunning } from "@/api/dataset/dataset";
import { codemirror } from "vue-codemirror";
import { getTable } from "@/api/dataset/dataset";
import { Base64 } from "js-base64";
// 核心样式
import "codemirror/lib/codemirror.css";
// 引入主题后还需要在 options 中指定主题才会生效
import "codemirror/theme/solarized.css";
import "codemirror/mode/sql/sql.js";
// require active-line.js
import "codemirror/addon/selection/active-line.js";
// closebrackets
import "codemirror/addon/edit/closebrackets.js";
// keyMap
import "codemirror/mode/clike/clike.js";
import "codemirror/addon/edit/matchbrackets.js";
import "codemirror/addon/comment/comment.js";
import "codemirror/addon/dialog/dialog.js";
import "codemirror/addon/dialog/dialog.css";
import "codemirror/addon/search/searchcursor.js";
import "codemirror/addon/search/search.js";
import "codemirror/keymap/emacs.js";
// 引入代码自动提示插件
import "codemirror/addon/hint/show-hint.css";
import "codemirror/addon/hint/sql-hint";
import "codemirror/addon/hint/show-hint";
import { engineMode } from "@/api/system/engine";

export default {
  name: "AddSQL",
  components: { codemirror },
  props: {
    param: {
      type: Object,
      default: () => {}
    }
  },
  data() {
    return {
      dataSource: "",
      errMsg: false,
      options: [],
      sql: "",
      sqlOption: {
        tabSize: 2,
        styleActiveLine: true,
        lineNumbers: true,
        line: true,
        mode: "text/x-sql",
        theme: "solarized",
        hintOptions: {
          // 自定义提示选项
          completeSingle: false, // 当匹配只有一项的时候是否自动补全
        },
      },
      data: [],
      errImg: require("@/assets/error.png"),
      sqlHeight: 330,
      fields: [],
      mode: "0",
      syncType: "sync_now",
      height: 500,
      kettleRunning: false,
      selectedDatasource: {},
      engineMode: "local",
      disabledSync: true,
      showVariableMgm: false,
      dialogTitle: "",
      variables: [],
      variablesTmp: [],
      fieldOptions: [
        { label: this.$t("dataset.text"), value: "TEXT" },
        { label: this.$t("dataset.value"), value: "LONG" },
        {
          label:
            this.$t("dataset.value") + "(" + this.$t("dataset.float") + ")",
          value: "DOUBLE",
        },
        { label: this.$t("dataset.time_year"), value: "DATETIME-YEAR" },
        {
          label: this.$t("dataset.time_year_month"),
          value: "DATETIME-YEAR-MONTH",
          children: [
            {
              value: "yyyy-MM",
              label: "YYYY-MM",
            },
            {
              value: "yyyy/MM",
              label: "YYYY/MM",
            },
          ],
        },
        {
          label: this.$t("dataset.time_year_month_day"),
          value: "DATETIME-YEAR-MONTH-DAY",
          children: [
            {
              value: "yyyy-MM-dd",
              label: "YYYY-MM-DD",
            },
            {
              value: "yyyy/MM/dd",
              label: "YYYY/MM/DD",
            },
          ],
        },
        {
          label: this.$t("dataset.time_all"),
          value: "DATETIME",
          children: [
            {
              value: "yyyy-MM-dd HH:mm:ss",
              label: "YYYY-MM-DD HH:MI:SS",
            },
            {
              value: "yyyy/MM/dd HH:mm:ss",
              label: "YYYY/MM/DD HH:MI:SS",
            },
          ],
        },
      ],
    };
  },
  computed: {
    codemirror() {
      return this.$refs.myCm.codemirror;
    },
  },
  watch: {
    "param.tableId": {
      handler: function () {
        this.resetComponent();
        this.initTableInfo();
      },
    },
  },
  mounted() {
    window.onresize = () => {
      this.calHeight();
    };
    this.calHeight();
    this.initDataSource();
    this.$refs.myCm.codemirror.on("keypress", () => {
      this.$refs.myCm.codemirror.showHint();
    });

    this.initTableInfo();
  },
  created() {
    this.kettleState();
    engineMode().then((res) => {
      this.engineMode = res.data;
    });
  },
  methods: {
    mousedownDrag() {
      document
        .querySelector(".dataset-sql")
        .addEventListener("mousemove", this.caculateHeight);
    },
    mouseupDrag() {
      document
        .querySelector(".dataset-sql")
        .removeEventListener("mousemove", this.caculateHeight);
    },
    caculateHeight(e) {
      this.sqlHeight = e.pageY - 56;
    },
    kettleState() {
      isKettleRunning().then((res) => {
        this.kettleRunning = res.data;
      });
    },
    changeDatasource() {
      for (let i = 0; i < this.options.length; i++) {
        if (this.options[i].id === this.dataSource) {
          this.selectedDatasource = this.options[i];
          this.mode = "0";
          if (
            this.engineMode === "simple" ||
            !this.kettleRunning ||
            this.selectedDatasource.calculationMode === "DIRECT"
          ) {
            this.disabledSync = true;
          } else {
            this.disabledSync = false;
          }
        }
      }
    },
    calHeight() {
      const that = this;
      setTimeout(function () {
        const currentHeight = document.documentElement.clientHeight;
        that.height =
          currentHeight - 56 - 30 - 26 - 25 - 43 - 160 - 10 - 37 - 20 - 10 - 16;
      }, 10);
    },
    initDataSource() {
      listDatasource().then((response) => {
        this.options = response.data.filter((item) => item.type !== "api");
      });
    },

    initTableInfo() {
      if (this.param.tableId) {
        getTable(this.param.tableId).then((response) => {
          const table = response.data;
          this.dataSource = table.dataSourceId;
          this.mode = table.mode + "";

          if (JSON.parse(table.info).isBase64Encryption) {
            this.sql = Base64.decode(JSON.parse(table.info).sql);
          } else {
            this.sql = JSON.parse(
              table.info.replace(/\n/g, "\\n").replace(/\r/g, "\\r")
            ).sql;
          }
          this.variables = JSON.parse(table.sqlVariableDetails);
          this.getSQLPreview();
        });
      }
    },

    getSQLPreview() {
      this.errMsg = false;
      if (!this.dataSource || this.datasource === "") {
        this.$message({
          showClose: true,
          message: this.$t("dataset.pls_slc_data_source"),
          type: "error",
        });
        return;
      }
      this.parseVariable();
      post("/dataset/table/sqlPreview", {
        dataSourceId: this.dataSource,
        type: "sql",
        sqlVariableDetails: JSON.stringify(this.variables),
        info: JSON.stringify({
          sql: Base64.encode(this.sql.trim()),
          isBase64Encryption: true,
        }),
      })
        .then((response) => {
          this.fields = response.data.fields;
          this.data = response.data.data;
          const datas = this.data;
          this.$refs.plxTable.reloadData(datas);
        })
        .catch((err) => {
          this.errMsg = true;
        });
    },

    save() {
      if (!this.dataSource || this.datasource === "") {
        this.$message({
          showClose: true,
          message: this.$t("dataset.pls_slc_data_source"),
          type: "error",
        });
        return;
      }
      if (!this.table.name || this.table.name === "") {
        this.$message({
          showClose: true,
          message: this.$t("dataset.pls_input_name"),
          type: "error",
        });
        return;
      }
      if (this.table.name.length > 50) {
        this.$message({
          showClose: true,
          message: this.$t("dataset.char_can_not_more_50"),
          type: "error",
        });
        return;
      }
      this.parseVariable();
      const table = {
        id: this.param.tableId,
        name: this.table.name,
        sceneId: this.param.id,
        dataSourceId: this.dataSource,
        type: "sql",
        syncType: this.syncType,
        mode: parseInt(this.mode),
        sqlVariableDetails: JSON.stringify(this.variables),
        info: JSON.stringify({
          sql: Base64.encode(this.sql.trim()),
          isBase64Encryption: true,
        }),
      };
      post("/dataset/table/update", table).then((response) => {
        this.$emit("saveSuccess", table);
        this.cancel();
      });
    },

    cancel() {
      if (this.param.tableId) {
        this.$emit("switchComponent", {
          name: "ViewTable",
          param: this.param.table,
        });
      } else {
        this.$emit("switchComponent", { name: "" });
      }
    },

    showSQL(val) {
      this.sql = val || "";
    },
    onCmReady(cm) {
      this.codemirror.setSize("-webkit-fill-available", "auto");
    },
    onCmFocus(cm) {},
    onCmCodeChange(newCode) {
      this.sql = newCode;
      this.$emit("codeChange", this.sql);
    },

    resetComponent() {
      this.dataSource = "";
      this.table.name = "";
      this.sql = "";
      this.data = [];
      this.fields = [];
      this.mode = "0";
      this.syncType = "sync_now";
    },

    variableMgm() {
      this.parseVariable();
      this.dialogTitle = this.$t("sql_variable.variable_mgm") + " ";
      this.showVariableMgm = true;
    },
    parseVariable() {
      this.variablesTmp = [];
      var reg = new RegExp("\\${(.*?)}", "gim");
      var match = this.sql.match(reg);
      const names = [];
      if (match !== null) {
        for (let index = 0; index < match.length; index++) {
          var name = match[index].substring(2, match[index].length - 1);
          if (names.indexOf(name) < 0) {
            names.push(name);
            var obj = undefined;
            for (let i = 0; i < this.variables.length; i++) {
              if (this.variables[i].variableName === name) {
                obj = this.variables[i];
              }
            }
            if (obj === undefined) {
              obj = {
                variableName: name,
                alias: "",
                type: [],
                required: false,
                defaultValue: "",
                details: "",
              };
              obj.type.push("TEXT");
            }
            this.variablesTmp.push(obj);
          }
        }
      }
      this.variables = JSON.parse(JSON.stringify(this.variablesTmp)).concat();
    },
    closeVariableMgm() {
      this.showVariableMgm = false;
    },
    saveVariable() {
      this.variables = JSON.parse(JSON.stringify(this.variablesTmp)).concat();
      this.showVariableMgm = false;
    },
    variableTypeChange(row) {
      row.defaultValue = "";
    },
  },
};
</script>

<style scoped>
.codemirror {
  height: 100%;
  overflow-y: auto;
}

.codemirror ::v-deep .CodeMirror-scroll {
  height: 100%;
  overflow-y: auto;
}
</style>

<style lang="scss">
.sql-dataset-drawer {
  .el-drawer__body {
    padding: 16px 24px;
    position: relative;
  }

  .el-date-editor {
    width: 100%;
  }

  .select-type {
    width: 180px;
    .el-input__inner {
      padding-left: 32px;
    }
  }

  .select-svg-icon {
    position: absolute;
    left: 24px;
    top: 50%;
    transform: translateY(-50%);
  }
  .content {
    height: 62px;
    width: 792px;
    border-radius: 4px;
    background: #e1eaff;
    position: relative;
    padding: 9px 19px 9px 40px;
    font-family: PingFang SC;
    font-size: 14px;
    font-weight: 400;

    i {
      position: absolute;
      top: 12.6px;
      left: 16.7px;
      font-size: 14px;
      color: var(--primary, #3370ff);
    }
    margin-bottom: 16px;
  }
}
.dataset-sql {
  height: 100%;
  width: 100%;
  display: flex;
  flex-direction: column;
  .sql-editer {
    min-height: 330px;
    background: #f5f6f7;
    padding: 16px 24px;
    .code-container {
      box-sizing: border-box;
      height: calc(100% - 64px);
      margin-top: 16px;
      color: var(--deTextPrimary, #1f2329);
      .CodeMirror {
        height: 100% !important;
      }
    }
  }
  .sql-result {
    font-family: PingFang SC;
    font-size: 14px;
    overflow-y: auto;
    box-sizing: border-box;
    flex: 1;
    .sql-title {
      user-select: none;
      height: 54px;
      display: flex;
      align-items: center;
      padding: 16px 24px;
      font-weight: 500;
      position: relative;
      color: var(--deTextPrimary, #1f2329);
      border-bottom: 1px solid rgba(31, 35, 41, 0.15);

      .result-num {
        font-weight: 400;
        color: var(--deTextSecondary, #646a73);
        margin-left: 12px;
      }
      .drag {
        position: absolute;
        top: 0;
        left: 50%;
        transform: translateX(-50%);
        height: 7px;
        width: 100px;
        border-radius: 3.5px;
        background: rgba(31, 35, 41, 0.1);
        cursor: row-resize;
      }
    }

    .table-sql {
      height: calc(100% - 64px);
      padding: 18px 25px;
      overflow-y: auto;
      box-sizing: border-box;
    }
  }
}
</style>
