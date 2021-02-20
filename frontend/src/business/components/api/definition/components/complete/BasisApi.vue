<template>
  <div v-loading="loading">
    <el-form :model="basicForm" label-position="right" label-width="80px" size="small" :rules="rule" ref="basicForm" style="margin-right: 20px">
      <!-- 基础信息 -->
      <el-row>
        <el-col :span="12">
          <el-form-item :label="$t('commons.name')" prop="name">
            <el-input class="ms-http-input" size="small" v-model="basicForm.name"/>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item :label="$t('test_track.module.module')" prop="moduleId">
            <el-select class="ms-http-input" size="small" v-model="basicForm.moduleId" style="width: 100%" @change="reload">
              <div v-if="moduleOptions.length>0">
                <el-option v-for="item in moduleOptions" :key="item.id" :label="item.path" :value="item.id"/>
              </div>
              <div v-else>
                <el-option :key="0" :value="''">
                  <div style="margin-left: 40px">
                        <span style="font-size: 14px;color: #606266;font-weight: 48.93">{{ $t('api_test.definition.select_comp.no_data') }},
                        </span>
                    <el-link type="primary" @click="createModules">{{ $t('api_test.definition.select_comp.add_data') }}</el-link>
                  </div>
                </el-option>
              </div>
            </el-select>
          </el-form-item>
        </el-col>
      </el-row>
      <el-row>
        <el-col :span="12">
          <el-form-item :label="$t('commons.status')" prop="status">
            <el-select class="ms-http-input" size="small" v-model="basicForm.status" style="width: 100%">
              <el-option v-for="item in options" :key="item.id" :label="item.label" :value="item.id"/>
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item :label="$t('api_test.definition.request.responsible')" prop="userId">
            <el-select v-model="basicForm.userId"
                       :placeholder="$t('api_test.definition.request.responsible')" filterable size="small"
                       class="ms-http-input" style="width: 100%">
              <el-option
                v-for="item in maintainerOptions"
                :key="item.id"
                :label="item.id + ' (' + item.name + ')'"
                :value="item.id">
              </el-option>
            </el-select>

          </el-form-item>
        </el-col>
      </el-row>

      <el-row>
        <el-col :span="12">
          <el-form-item :label="$t('commons.tag')" prop="tag">
            <ms-input-tag :currentScenario="basicForm" ref="tag"/>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item :label="$t('commons.description')" prop="description">
            <el-input class="ms-http-textarea"
                      v-model="basicForm.description"
                      type="textarea"
                      :autosize="{ minRows: 2, maxRows: 10}"
                      :rows="2" size="small"/>
          </el-form-item>
        </el-col>
      </el-row>
    </el-form>
  </div>
</template>

<script>
import {API_STATUS} from "../../model/JsonData";
import {WORKSPACE_ID} from '../../../../../../common/js/constants';
import MsInputTag from "@/business/components/api/automation/scenario/MsInputTag";

export default {
  name: "MsBasisApi",
  components: {MsInputTag},
  props: {
    currentProtocol: {
      type: String,
      default: "HTTP"
    },
    moduleOptions: Array,
    basisData: {},
  },
  created() {
    this.getMaintainerOptions();
    this.basicForm = this.basisData;
  },
  data() {
    return {
      basicForm: {},
      httpVisible: false,
      currentModule: {},
      maintainerOptions: [],
      loading: false,
      rule: {
        name: [
          {required: true, message: this.$t('test_track.case.input_name'), trigger: 'blur'},
          {max: 50, message: this.$t('test_track.length_less_than') + '50', trigger: 'blur'}
        ],
        userId: [{required: true, message: this.$t('test_track.case.input_maintainer'), trigger: 'change'}],
        moduleId: [{required: true, message: this.$t('test_track.case.input_module'), trigger: 'change'}],
        status: [{required: true, message: this.$t('commons.please_select'), trigger: 'change'}],
      },
      value: API_STATUS[0].id,
      options: API_STATUS,
    }
  },
  methods: {
    getMaintainerOptions() {
      let workspaceId = localStorage.getItem(WORKSPACE_ID);
      this.$post('/user/ws/member/tester/list', {workspaceId: workspaceId}, response => {
        this.maintainerOptions = response.data;
      });
    },
    reload() {
      this.loading = true
      this.$nextTick(() => {
        this.loading = false
      })
    },
    validate() {
      this.$refs['basicForm'].validate((valid) => {
        if (valid) {
          this.$emit('callback');
        }
      })
    },
    createModules() {
      this.$emit("createRootModelInTree");
    },
  }
}
</script>

<style scoped>
</style>
