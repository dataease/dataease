<template>
  <div>
    <div>
      <el-button size="mini" @click="openController"
                 class="condition"
                 :class="controllerClass"
                 v-if="request.controller && request.controller.isValid()">
        <el-row type="flex" align="middle">
          <font-awesome-icon :icon="['fas', 'random']"/>
          <div class="condition-label">{{ request.controller.getLabel() }}</div>
        </el-row>
      </el-button>
    </div>

    <div>
      <el-button size="mini" @click="openTimer"
                 class="condition"
                 :class="timerClass"
                 v-if="request.timer && request.timer.isValid()">
        <el-row type="flex" align="middle">
          <font-awesome-icon :icon="['fas', 'clock']"/>
          <div class="condition-label">{{ request.timer && request.timer.getLabel() }}</div>
        </el-row>
      </el-button>
    </div>
    <ms-if-controller ref="controller"/>
    <ms-constant-timer ref="timer"/>
  </div>
</template>

<script>
import {Request} from "@/business/components/api/test/model/ScenarioModel";
import MsIfController from "@/business/components/api/test/components/request/condition/IfController";
import MsConstantTimer from "@/business/components/api/test/components/request/condition/ConstantTimer";

export default {
  name: "MsConditionLabel",
  components: {MsConstantTimer, MsIfController},
  props: {
    request: Request
  },
  methods: {
    openController() {
      this.$refs.controller.open(this.request);
    },
    openTimer() {
      this.$refs.timer.open(this.request);
    }
  },
  computed: {
    controllerClass() {
      let disabled = this.request.controller.enable === false;
      return {'is-disabled': disabled, 'click-cursor': disabled}
    },
    timerClass() {
      let disabled = this.request.timer.enable === false;
      return {'is-disabled': disabled, 'click-cursor': disabled}
    },
  }
}
</script>

<style scoped>
.condition {
  width: 100%;
}

.condition-label {
  padding-left: 5px;
}

.click-cursor {
  cursor: pointer !important;
}
</style>
