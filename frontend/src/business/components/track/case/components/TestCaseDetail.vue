<template>
  <el-row :gutter="5">
    <el-col :span="15">
      <el-card class="case-detail-form-card">
        <el-form :model="form" ref="caseFrom" v-loading="result.loading">

          <el-row>
            <el-col :span="10" :offset="1">
              <el-form-item
                :placeholder="$t('test_track.case.input_name')"
                :label="$t('test_track.case.name')"
                :label-width="formLabelWidth"
                prop="name">
                <el-input :disabled="readOnly" v-model="testCase.name"></el-input>
              </el-form-item>
            </el-col>

            <el-col :span="12">
              <el-form-item :label="$t('test_track.case.module')" :label-width="formLabelWidth" prop="module">
                <el-input :disabled="readOnly" v-model="testCase.nodePath"></el-input>
              </el-form-item>
            </el-col>
          </el-row>

          <el-row>
            <el-col :span="10" :offset="1">
              <el-form-item :label="$t('test_track.case.maintainer')" :label-width="formLabelWidth" prop="maintainer">
                <el-select :disabled="readOnly" v-model="testCase.maintainer"
                           :placeholder="$t('test_track.case.input_maintainer')" filterable>
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item :label="$t('test_track.case.priority')" :label-width="formLabelWidth" prop="priority">
                <el-select :disabled="readOnly" v-model="testCase.priority" clearable
                           :placeholder="$t('test_track.case.input_priority')">
                </el-select>
              </el-form-item>
            </el-col>
          </el-row>

          <el-row>
            <el-col :span="10" :offset="1">
              <el-form-item :label="$t('test_track.case.type')" :label-width="formLabelWidth" prop="type">
                <el-select :disabled="readOnly" v-model="testCase.type"
                           :placeholder="$t('test_track.case.input_type')">
                  <el-option :label="$t('commons.functional')" value="functional"></el-option>
                  <el-option :label="$t('commons.performance')" value="performance"></el-option>
                  <el-option :label="$t('commons.api')" value="api"></el-option>
                </el-select>

              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item :label="$t('test_track.case.method')" :label-width="formLabelWidth" prop="method">
                <el-select :disabled="readOnly" v-model="testCase.method" :placeholder="$t('test_track.case.input_method')">
                  <el-option :label="$t('test_track.case.auto')" value="auto"></el-option>
                  <el-option :label="$t('test_track.case.manual')" value="manual"></el-option>
                </el-select>
              </el-form-item>
            </el-col>
          </el-row>

          <el-row v-if="testCase.method && testCase.method == 'auto'">
            <el-col :span="10" :offset="1">
              <el-form-item :label="$t('test_track.case.relate_test')" :label-width="formLabelWidth" prop="testId">
                <el-select filterable :disabled="readOnly" v-model="testCase.testId"
                           :placeholder="$t('test_track.case.input_type')">
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="12" v-if="testCase.testId=='other'">
              <el-form-item :label="$t('test_track.case.test_name')" :label-width="formLabelWidth" prop="testId">
                <el-input v-model="testCase.otherTestName" :placeholder="$t('test_track.case.input_test_case')"
                          :disabled="readOnly"></el-input>
              </el-form-item>
            </el-col>
          </el-row>
          <el-row style="margin-top: 15px;">
            <el-col :offset="1">{{ $t('test_track.case.prerequisite') }}:</el-col>
          </el-row>
          <el-row type="flex" justify="center" style="margin-top: 10px;">
            <el-col :span="22">
              <el-form-item prop="prerequisite">
                <el-input :disabled="readOnly" v-model="testCase.prerequisite"
                          type="textarea"
                          :autosize="{ minRows: 2, maxRows: 4}"
                          :rows="2"
                          :placeholder="$t('test_track.case.input_prerequisite')"></el-input>
              </el-form-item>
            </el-col>
          </el-row>

          <el-row v-if="testCase.method && testCase.method != 'auto'" style="margin-bottom: 10px">
            <el-col :offset="1">{{ $t('test_track.case.steps') }}:</el-col>
          </el-row>

          <el-row v-if="testCase.method && testCase.method != 'auto'" type="flex" justify="center">
            <el-col :span="22">
              <el-table
                v-if="isStepTableAlive"
                :data="JSON.parse(testCase.steps)"
                class="tb-edit"
                border
                size="mini"
                :default-sort="{prop: 'num', order: 'ascending'}"
                highlight-current-row>
                <el-table-column :label="$t('test_track.case.number')" prop="num" min-width="15%"></el-table-column>
                <el-table-column :label="$t('test_track.case.step_desc')" prop="desc" min-width="35%">
                  <template v-slot:default="scope">
                    <el-input
                      class="table-edit-input"
                      size="mini"
                      :disabled="readOnly"
                      type="textarea"
                      :autosize="{ minRows: 1, maxRows: 6}"
                      :rows="2"
                      v-model="scope.row.desc"
                      :placeholder="$t('commons.input_content')"
                      clearable/>
                  </template>
                </el-table-column>
                <el-table-column :label="$t('test_track.case.expected_results')" prop="result" min-width="35%">
                  <template v-slot:default="scope">
                    <el-input
                      class="table-edit-input"
                      size="mini"
                      :disabled="readOnly"
                      type="textarea"
                      :autosize="{ minRows: 1, maxRows: 6}"
                      :rows="2"
                      v-model="scope.row.result"
                      :placeholder="$t('commons.input_content')"
                      clearable/>
                  </template>
                </el-table-column>
              </el-table>
            </el-col>
          </el-row>

          <el-row style="margin-top: 15px;margin-bottom: 10px">
            <el-col :offset="1">{{ $t('commons.remark') }}:</el-col>
          </el-row>
          <el-row type="flex" justify="center">
            <el-col :span="22">
              <el-form-item prop="remark">
                <el-input v-model="testCase.remark"
                          :autosize="{ minRows: 2, maxRows: 4}"
                          type="textarea"
                          :disabled="readOnly"
                          :rows="2"
                          :placeholder="$t('commons.input_content')"></el-input>
              </el-form-item>
            </el-col>
          </el-row>
        </el-form>
      </el-card>
    </el-col>
    <el-col :span="9">
      <case-comment :case-id="testCaseId" :read-only="true" class="case-detail-comment"/>
    </el-col>
  </el-row>

</template>

<script>

import CaseComment from "@/business/components/track/case/components/CaseComment";
export default {
  name: "TestCaseDetail",
  components: {CaseComment},
  data() {
    return {
      result: {},
      testCase: {},
      dialogFormVisible: false,
      readOnly: true,
      form: {
        name: '',
        module: '',
        maintainer: '',
        priority: '',
        type: '',
        method: '',
        prerequisite: '',
        testId: '',
        otherTestName: '',
        steps: [{
          num: 1,
          desc: '',
          result: ''
        }],
        remark: '',
      },
      workspaceId: '',
      formLabelWidth: "80px",

      isStepTableAlive: true,
      methodOptions: [
        {value: 'auto', label: this.$t('test_track.case.auto')},
        {value: 'manual', label: this.$t('test_track.case.manual')}
      ]
    };
  },
  mounted() {
    this.$get('test/case/get/' + this.testCaseId, response => {
      this.testCase = response.data;
    });
  },
  methods: {},
  props: {
    testCaseId: {
      type: String
    }
  },
}
</script>

<style scoped>
.case-detail-form-card {
  height: 600px;
  overflow: auto;
}

.case-detail-form-card >>> .el-card__body {
  padding: 20px 0;
}

.case-detail-comment {
  min-width: 320px;
  height: 600px;
  overflow: auto;
}
</style>
