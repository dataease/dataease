<template>
  <common-component :title="$t('test_track.plan_view.defect_list')">
    <template>
      <el-table
              row-key="id"
              :data="defectList"
        >
        <el-table-column
          prop="id"
          :label="$t('commons.id')"
          show-overflow-tooltip>
        </el-table-column>
        <el-table-column
          prop="model"
          :label="$t('test_track.module.module')"
          show-overflow-tooltip>
        </el-table-column>
        <el-table-column
          prop="projectName"
          :label="$t('test_track.module.project_name')"
          show-overflow-tooltip>
        </el-table-column>
        <el-table-column
          prop="title"
          :label="$t('test_track.module.title')"
          show-overflow-tooltip>
        </el-table-column>
        <el-table-column
          prop="description"
          :label="$t('test_track.module.describe')"
          show-overflow-tooltip>
          <template v-slot:default="scope">
            <el-popover
              placement="left"
              width="400"
              trigger="hover"
            >
              <ckeditor :editor="editor" disabled :config="readConfig"
                        v-model="scope.row.description"/>
              <el-button slot="reference" type="text">{{$t('test_track.issue.preview')}}</el-button>
            </el-popover>
          </template>
        </el-table-column>

        <el-table-column
                prop="status"
                :label="$t('test_track.module.status')"
                show-overflow-tooltip>
        </el-table-column>
        <el-table-column
          prop="currentOwner"
          :label="$t('test_track.module.current_owner')"
          show-overflow-tooltip>
        </el-table-column>

        <el-table-column
          prop="createTime"
          :label="$t('test_track.module.creation_time')"
          show-overflow-tooltip>
          <template v-slot:default="scope">
            <span>{{ scope.row.createTime | timestampFormatDate }}</span>
          </template>
        </el-table-column>
      </el-table>
    </template>

  </common-component>
</template>

<script>
  import CommonComponent from "./CommonComponent";
  import PriorityTableItem from "../../../../../common/tableItems/planview/PriorityTableItem";
  import TypeTableItem from "../../../../../common/tableItems/planview/TypeTableItem";
  import MethodTableItem from "../../../../../common/tableItems/planview/MethodTableItem";
  import StatusTableItem from "../../../../../common/tableItems/planview/StatusTableItem";
  import ClassicEditor from "@ckeditor/ckeditor5-build-classic";
    export default {
      name: "DefectListComponent",
      components: {StatusTableItem, MethodTableItem, TypeTableItem, PriorityTableItem, CommonComponent},
      props: {
        defectList: {
          type: Array,
          default() {
            return [
              {
                id: "1023",
                module: "模块e",
                title: 'testCase1',
                description: "第一个模块测试",
                status: "接受/处理",
                reporter: "Andy",
                createTime: "2010.3.3",
              },
            ]
          }
        }
      },
      data() {
        return {
          editor: ClassicEditor,
          readConfig: {toolbar: []},

        }
      }
    }
</script>

<style scoped>

</style>
