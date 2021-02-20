<template>
  <div class="request-container">
    <draggable :list="this.scenario.requests" group="Request" class="request-draggable" ghost-class="request-ghost"
               :disabled="isReference">
      <div class="request-item" v-for="(request, index) in this.scenario.requests" :key="index" @click="select(request)"
           :class="{'selected': isSelected(request), 'disable-request': isEnable(request)}">
        <ms-condition-label :request="request"/>
        <el-row type="flex" align="middle">
          <div class="request-type">
            {{ request.showType() }}
          </div>
          <div class="request-method" :style="{'color': getColor(request.enable, request.showMethod())}">
            {{ request.showMethod() }}
          </div>
          <div class="request-name">
            {{ request.name }}
          </div>
          <div class="request-btn">
            <el-dropdown trigger="click" @command="handleCommand">
              <span class="el-dropdown-link el-icon-more"/>
              <el-dropdown-menu slot="dropdown">
                <el-dropdown-item :disabled="isReadOnly" :command="{type: 'copy', index: index}">
                  {{ $t('api_test.request.copy') }}
                </el-dropdown-item>
                <el-dropdown-item :disabled="isReadOnly" :command="{type: 'delete', index: index}">
                  {{ $t('api_test.request.delete') }}
                </el-dropdown-item>
                <el-dropdown-item v-if="request.enable" :disabled="isReadOnly"
                                  :command="{type: 'disable', index: index}">
                  {{ $t('api_test.scenario.disable') }}
                </el-dropdown-item>
                <el-dropdown-item v-if="!request.enable" :disabled="isReadOnly"
                                  :command="{type: 'enable', index: index}">
                  {{ $t('api_test.scenario.enable') }}
                </el-dropdown-item>
                <el-dropdown-item :disabled="isReadOnly" :command="{type: 'controller', index: index}">
                  {{ $t('api_test.request.condition') }}
                </el-dropdown-item>
                <el-dropdown-item :disabled="isReadOnly" :command="{type: 'wait', index: index}">
                  {{ $t('api_test.request.wait') }}
                </el-dropdown-item>
              </el-dropdown-menu>
            </el-dropdown>
          </div>
        </el-row>
      </div>
    </draggable>
    <el-popover placement="top" v-model="visible">
      <el-radio-group v-model="type" @change="createRequest">
        <el-radio :label="types.HTTP">HTTP</el-radio>
        <el-radio :label="types.DUBBO">DUBBO</el-radio>
        <el-radio :label="types.SQL">SQL</el-radio>
        <el-radio :label="types.TCP">TCP</el-radio>
      </el-radio-group>
      <el-button slot="reference" :disabled="isReadOnly"
                 class="request-create" type="primary" size="mini" icon="el-icon-plus" plain/>
    </el-popover>
    <ms-if-controller ref="controller"/>
    <ms-constant-timer ref="timer"/>
  </div>
</template>

<script>
import {RequestFactory} from "../../model/ScenarioModel";
import draggable from 'vuedraggable';
import MsIfController from "@/business/components/api/test/components/request/condition/IfController";
import MsConstantTimer from "@/business/components/api/test/components/request/condition/ConstantTimer";
import MsConditionLabel from "@/business/components/api/test/components/request/condition/ConditionLabel";

export default {
  name: "MsApiRequestConfig",

  components: {MsConditionLabel, MsConstantTimer, MsIfController, draggable},

  props: {
    scenario: Object,
    isReadOnly: {
      type: Boolean,
      default: false
    }
  },

  data() {
    return {
      selected: 0,
      visible: false,
      types: RequestFactory.TYPES,
      type: "",
      methodColorMap: new Map([
        ['GET', "#61AFFE"], ['POST', '#49CC90'], ['PUT', '#fca130'],
        ['PATCH', '#E2EE11'], ['DELETE', '#f93e3d'], ['OPTIONS', '#0EF5DA'],
        ['HEAD', '#8E58E7'], ['CONNECT', '#90AFAE'],
        ['DUBBO', '#C36EEF'],['SQL', '#0AEAD4'],['TCP', '#0A52DF'],
      ])
    }
  },

  computed: {
    isSelected() {
      return function (request) {
        return this.selected === request;
      }
    },
    isReference() {
      return this.scenario.isReference();
    }
  },

  methods: {
    createRequest(type) {
      let request = new RequestFactory({type: type});
      if (this.scenario.environmentId) {
        request.useEnvironment = true;
      }
      this.scenario.requests.push(request);
      this.type = "";
      this.visible = false;
    },
    copyRequest(index) {
      let request = this.scenario.requests[index];
      let item = new RequestFactory(request);
      if (item.body && item.body.kvs) {
        item.body.kvs.forEach(kv => {
          let files = [];
          if (kv.files) {
            kv.files.forEach(file => {
              let fileCopy = {};
              Object.assign(fileCopy, file);
              files.push(fileCopy);
            })
          }
          kv.files = files;
        });
      }
      this.scenario.requests.push(item);
    },
    disableRequest(index) {
      this.scenario.requests[index].enable = false;
    },
    enableRequest(index) {
      this.scenario.requests[index].enable = true;
    },
    deleteRequest(index) {
      this.scenario.requests.splice(index, 1);
      if (this.scenario.requests.length === 0) {
        this.createRequest();
      }
    },
    addController(index) {
      let request = this.scenario.requests[index];
      this.$refs.controller.open(request);
    },
    addTimer(index) {
      let request = this.scenario.requests[index];
      this.$refs.timer.open(request);
    },
    handleCommand(command) {
      switch (command.type) {
        case "copy":
          this.copyRequest(command.index);
          break;
        case "delete":
          this.deleteRequest(command.index);
          break;
        case "disable":
          this.disableRequest(command.index);
          break;
        case "enable":
          this.enableRequest(command.index);
          break;
        case "controller":
          this.addController(command.index);
          break;
        case "wait":
          this.addTimer(command.index);
          break;
      }
    },
    getColor(requestEnable, method) {
      if (this.scenario.enable && requestEnable) {
        return this.methodColorMap.get(method);
      } else {
        return '#909399';
      }
    },
    select(request) {
      if (!request) {
        return;
      }
      request.environment = this.scenario.environment;
      if (!request.useEnvironment) {
        request.useEnvironment = false;
      }
      request.dubboConfig = this.scenario.dubboConfig;
      this.selected = request;
      this.$emit("select", request, this.scenario);
    },
    isEnable(request) {
      return !request.enable || !this.scenario.isEnable();
    },
  },

  created() {
    this.select(this.scenario.requests[0]);
  }
}
</script>

<style scoped>
.request-item {
  border-left: 5px solid #1E90FF;
  border-top: 1px solid #EBEEF5;
  cursor: pointer;
}

.request-item:first-child {
  border-top: 0;
}

.request-item:hover, .request-item.selected:hover {
  background-color: #ECF5FF;
}

.request-item.selected {
  background-color: #F5F5F5;
}

.request-type {
  background-color: #409eff;
  color: #fff;
  margin-left: 5px;
  padding: 4px 8px;
  border-radius: 20px;
  white-space: nowrap;
  font-size: 12px;
  display: inline-block;
  line-height: 1;
  text-align: center;
}

.request-method {
  padding: 0 5px;
  color: #1E90FF;
}

.request-name {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  font-size: 14px;
  width: 100%;
}

.request-btn {
  float: right;
  text-align: center;
  height: 40px;
}

.request-btn .el-icon-more {
  padding: 13px;
}

.request-create {
  width: 100%;
}

.request-ghost {
  opacity: 0.5;
  background-color: #909399;
}

.request-item.disable-request {
  border-left-color: #909399;
}

.disable-request .request-type {
  background-color: #909399;
}

.disable-request .request-method {
  color: #909399;
}

</style>
