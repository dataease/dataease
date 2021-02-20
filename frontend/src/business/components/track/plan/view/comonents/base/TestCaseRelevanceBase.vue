<template>
  <relevance-dialog :title="$t('test_track.plan_view.relevance_test_case')"  ref="relevanceDialog">

    <template v-slot:aside>
      <select-menu
        :data="projects"
        width="160px"
        :current-data="currentProject"
        :title="$t('test_track.switch_project')"
        @dataChange="changeProject"/>
      <slot name="aside"></slot>
    </template>

    <slot></slot>

    <template v-slot:footer>
      <ms-dialog-footer @cancel="close" @confirm="save"/>
    </template>

    <template v-slot:footer>
      <ms-dialog-footer @cancel="close" @confirm="save"/>
    </template>

  </relevance-dialog>
</template>

<script>

  import MsDialogFooter from '../../../../../common/components/MsDialogFooter'
  import SelectMenu from "../../../../common/SelectMenu";
  import RelevanceDialog from "./RelevanceDialog";

  export default {
    name: "TestCaseRelevanceBase",
    components: {
      RelevanceDialog,
      SelectMenu,
      MsDialogFooter,
    },
    data() {
      return {
        result: {},
        currentProject: {},
        projectId: '',
        projectName: '',
        projects: [],

      };
    },
    props: {
      planId: {
        type: String
      }
    },
    watch: {

    },
    methods: {

      refreshNode() {
        this.$emit('refresh');
      },

      save() {
        this.$emit('save');
      },

      close() {
        this.$refs.relevanceDialog.close();
      },

      open() {
        this.getProject();
        this.$refs.relevanceDialog.open();
      },

      getProject() {
        if (this.planId) {
          this.result = this.$post("/test/plan/project/", {planId: this.planId}, res => {
            let data = res.data;
            if (data) {
              this.projects = data;
              this.projectId = data[0].id;
              this.projectName = data[0].name;
              this.changeProject(data[0]);
            }
          })
        }
      },

      changeProject(project) {
        this.currentProject = project;
        this.$emit('setProject', project.id);
        // 获取项目时刷新该项目模块
        this.$emit('refreshNode');
      }
    }
  }
</script>

<style scoped>
</style>
