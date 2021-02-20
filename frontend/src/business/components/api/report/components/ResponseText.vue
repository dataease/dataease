<template>
  <div class="text-container">
    <div @click="active" class="collapse">
      <i class="icon el-icon-arrow-right" :class="{'is-active': isActive}"/>
      {{ $t('api_report.response') }}
    </div>
    <el-collapse-transition>
      <el-tabs v-model="activeName" v-show="isActive">
        <el-tab-pane :class="'body-pane'" label="Body" name="body" class="pane">
          <ms-sql-result-table v-if="isSqlType" :body="response.body"/>
          <ms-code-edit v-if="!isSqlType" :mode="mode" :read-only="true" :data="response.body" :modes="modes" ref="codeEdit"/>
        </el-tab-pane>
        <el-tab-pane label="Headers" name="headers" class="pane">
          <pre>{{ response.headers }}</pre>
        </el-tab-pane>
        <el-tab-pane :label="$t('api_report.assertions')" name="assertions" class="pane assertions">
          <ms-assertion-results :assertions="response.assertions"/>
        </el-tab-pane>

        <el-tab-pane :label="$t('api_test.request.extract.label')" name="label" class="pane">
          <pre>{{response.vars}}</pre>
        </el-tab-pane>
        <el-tab-pane :label="$t('api_test.definition.request.console')" name="console" class="pane">
          <pre>{{response.console}}</pre>
        </el-tab-pane>

        <el-tab-pane v-if="activeName == 'body'" :disabled="true" name="mode" class="pane assertions">
          <template v-slot:label>
            <ms-dropdown v-if="!isSqlType" :commands="modes" :default-command="mode" @command="modeChange"/>
            <ms-dropdown v-if="isSqlType" :commands="sqlModes" :default-command="mode" @command="sqlModeChange"/>
          </template>
        </el-tab-pane>

      </el-tabs>
    </el-collapse-transition>
  </div>
</template>

<script>
import MsAssertionResults from "./AssertionResults";
import MsCodeEdit from "../../../common/components/MsCodeEdit";
import MsDropdown from "../../../common/components/MsDropdown";
import {BODY_FORMAT, RequestFactory, Request, SqlRequest} from "../../test/model/ScenarioModel";
import MsSqlResultTable from "./SqlResultTable";

export default {
  name: "MsResponseText",

  components: {
    MsSqlResultTable,
    MsDropdown,
    MsCodeEdit,
    MsAssertionResults,
  },

  props: {
    requestType: String,
    response: Object
  },

  data() {
    return {
      isActive: true,
      activeName: "body",
      modes: ['text', 'json', 'xml', 'html'],
      sqlModes: ['text', 'table'],
      mode: BODY_FORMAT.TEXT
    }
  },

  methods: {
    active() {
      this.isActive = !this.isActive;
    },
    modeChange(mode) {
      this.mode = mode;
    },
    sqlModeChange(mode) {
      this.mode = mode;
    }
  },

  mounted() {
    if (!this.response.headers) {
      return;
    }
    if (this.response.headers.indexOf("application/json") > 0) {
      this.mode = BODY_FORMAT.JSON;
    }
  },

  computed: {
    isSqlType() {
      return (this.requestType === RequestFactory.TYPES.SQL && this.response.responseCode === '200');
    }
  }
}
</script>

<style scoped>

  .body-pane {
    padding: 10px !important;
    background: white !important;
  }

  .text-container .icon {
    padding: 5px;
  }

  .text-container .collapse {
    cursor: pointer;
  }

  .text-container .collapse:hover {
    opacity: 0.8;
  }

  .text-container .icon.is-active {
    transform: rotate(90deg);
  }

  .text-container .pane {
    background-color: #F5F5F5;
    padding: 0 10px;
    height: 250px;
    overflow-y: auto;
  }

  .text-container .pane.assertions {
    padding: 0;
  }

  pre {
    margin: 0;
  }

</style>
