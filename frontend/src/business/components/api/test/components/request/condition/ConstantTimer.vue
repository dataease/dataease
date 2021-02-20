<template>
  <el-dialog :visible.sync="visible" width="600px" @close="close">
    <el-row :gutter="10" type="flex" align="middle">
      <el-col :span="2">{{ $t('api_test.request.wait') }}</el-col>
      <el-col :span="10">
        <el-input-number class="width-100" size="small" v-model="timer.delay" :min="0" :step="1000"/>
      </el-col>
      <el-col :span="2">ms</el-col>
      <el-col :span="8">
        <el-switch v-model="timer.enable" :inactive-text="$t('api_test.scenario.enable_disable')"/>
      </el-col>
      <el-col :span="2">
        <el-button size="mini" type="danger" icon="el-icon-delete" circle @click="remove"/>
      </el-col>
    </el-row>
  </el-dialog>
</template>

<script>

import {ConstantTimer} from "@/business/components/api/test/model/ScenarioModel";

export default {
  name: "MsConstantTimer",
  data() {
    return {
      request: {},
      timer: new ConstantTimer(),
      visible: false,
    }
  },
  methods: {
    open(request) {
      this.request = request;
      this.timer = new ConstantTimer(request.timer);
      this.visible = true;
    },
    close() {
      this.request.timer = this.timer
      this.visible = false;
    },
    remove() {
      this.timer = new ConstantTimer();
      this.visible = false;
    }
  }
}
</script>

<style scoped>
.width-100 {
  width: 100%
}
</style>
