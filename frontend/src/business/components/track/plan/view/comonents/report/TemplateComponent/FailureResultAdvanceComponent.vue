<template>

  <common-component :title="$t('test_track.plan_view.failure_case')">
    <functional-failure-cases-list v-if="showFunctional" :functional-test-cases="failureTestCases.functionalTestCases"/>
    <api-failure-cases-list v-if="showApi" :api-test-cases="failureTestCases.apiTestCases"/>
    <scenario-failure-cases-list v-if="showScenario" :scenario-test-cases="failureTestCases.scenarioTestCases"/>
    <load-failure-cases-list v-if="showLoad" :load-test-cases="failureTestCases.loadTestCases"/>
  </common-component>

</template>

<script>
    import CommonComponent from "./CommonComponent";
    import PriorityTableItem from "../../../../../common/tableItems/planview/PriorityTableItem";
    import TypeTableItem from "../../../../../common/tableItems/planview/TypeTableItem";
    import MethodTableItem from "../../../../../common/tableItems/planview/MethodTableItem";
    import StatusTableItem from "../../../../../common/tableItems/planview/StatusTableItem";
    import {hub} from "@/business/components/track/plan/event-bus";
    import FunctionalFailureCasesList from "./component/FunctionalFailureCasesList";
    import ApiFailureCasesList from "./component/ApiFailureCasesList";
    import ScenarioFailureCasesList from "./component/ScenarioFailureCasesList";
    import LoadFailureCasesList
      from "@/business/components/track/plan/view/comonents/report/TemplateComponent/component/LoadFailureCasesList";
    export default {
      name: "FailureResultAdvanceComponent",
      components: {
        ScenarioFailureCasesList,
        ApiFailureCasesList,
        FunctionalFailureCasesList,
        StatusTableItem, MethodTableItem, TypeTableItem, PriorityTableItem, CommonComponent, LoadFailureCasesList},
      props: {
        failureTestCases: {
          type: Object,
          default() {
            return {
              functionalTestCases: [
                  {
                  name: 'testCase1',
                  priority: 'P1',
                  type: 'api',
                  method: 'auto',
                  nodePath: '/module1/module2',
                  executorName: "Tom",
                  status: "Failure",
                  updateTime: new Date(),
                },
                {
                  name: 'testCase2',
                  priority: 'P0',
                  type: 'functional',
                  method: 'manual',
                  nodePath: '/module1',
                  executorName: "Micheal",
                  status: "Failure",
                  updateTime: new Date()
                }
              ],
              apiTestCases: [
                {
                  name: 'testCase3',
                  priority: 'P2',
                  path: '/module1/module2',
                  createUser: "Tom",
                  lastResult: "Failure",
                  updateTime: new Date(),
                }
              ],
              scenarioTestCases: [
                {
                  name: 'testCase4',
                  level: 'P3',
                  modulePath: '/module1/module2',
                  stepTotal: 10,
                  passRate: '80%',
                  userId: "Tom",
                  lastResult: "Failure",
                  updateTime: new Date(),
                }
              ],
              loadTestCases: [
                {
                  caseName: 'testCase5',
                  projectName: '测试项目',
                  userName: 'Tom',
                  createTime:  new Date(),
                  caseStatus: 'error',
                }
              ]
            }
          }
        }
      },
      computed: {
        showFunctional() {
          return this.failureTestCases.functionalTestCases.length > 0
            || (this.failureTestCases.apiTestCases.length <= 0 && this.failureTestCases.scenarioTestCases.length <= 0 && this.failureTestCases.loadTestCases.length <= 0);
        },
        showApi() {
          return this.failureTestCases.apiTestCases.length > 0;
        },
        showScenario() {
          return this.failureTestCases.scenarioTestCases.length > 0;
        },
        showLoad() {
          return this.failureTestCases.loadTestCases.length > 0;
        },
      },
      methods: {
        goFailureTestCase(row) {
          hub.$emit("openFailureTestCase", row);
        }
      }
    }
</script>

<style scoped>

  /deep/ .failure-cases-list-header {
    font-size: 14px;
    font-weight: 600;
    margin-bottom: 10px;
  }

  .failure-cases-list {
    margin-bottom: 40px;
  }

</style>
