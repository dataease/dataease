<template>

  <el-drawer
    :before-close="handleClose"
    :visible.sync="showDialog"
    :with-header="false"
    size="100%"
    :modal-append-to-body="false"
    ref="drawer"
    v-loading="result.loading">
    <template v-slot:default="scope">

      <template-component-edit-header :template="template" @cancel="handleClose" @save="handleSave"/>

      <div class="container">
        <el-aside>
          <div class="description">
            <span class="title">{{$t('test_track.plan_view.component_library')}}</span>
            <span>{{$t('test_track.plan_view.component_library_tip')}}</span>
          </div>
          <draggable
            class="dragArea list-group"
            :list="components"
            :group="{ name: 'component', pull: 'clone', put: false }"
            :clone="clonePreview">
            <transition-group>
              <template-component-bar  v-for="item in components" :key="item" :component="componentMap.get(item)"/>
            </transition-group>
          </draggable>
        </el-aside>

        <el-main>
          <draggable
                class="dragArea list-group"
                :list="previews"
                @change="change"
                group="component">
            <transition-group>
              <div class="preview" v-for="item in previews" :key="item.id">
                <template-component :is-report-view="false" :is-report="isReport" :metric="metric" :preview="item"/>
                <i class="el-icon-error" @click="handleDelete(item)"/>
              </div>
            </transition-group>
          </draggable>
        </el-main>
      </div>
    </template>
  </el-drawer>
</template>

<script>

  import draggable from 'vuedraggable';
  import TemplateComponentBar from "./TemplateComponentBar";
  import TemplateComponentEditHeader from "./TemplateComponentEditHeader";
  import {WORKSPACE_ID} from '../../../../../../../common/js/constants';
  import {jsonToMap, mapToJson} from "../../../../../../../common/js/utils";
  import TemplateComponent from "./TemplateComponent/TemplateComponent";

    export default {
      name: "TestCaseReportTemplateEdit",
      components: {
        TemplateComponent,
        TemplateComponentEditHeader,
        TemplateComponentBar,
        draggable
      },
      data() {
        return {
          showDialog: false,
          result: {},
          name: '',
          type: 'edit',
          componentMap: new Map(
            [
              [1, { name: this.$t('test_track.plan_view.base_info'), id: 1 , type: 'system'}],
              [2, { name: this.$t('test_track.plan_view.test_result'), id: 2 , type: 'system'}],
              [3, { name: this.$t('test_track.plan_view.result_distribution'), id: 3 ,type: 'system'}],
              [4, { name: this.$t('test_track.plan_view.failure_case'), id: 4 ,type: 'system'}],
              [5, { name: this.$t('test_track.plan_view.defect_list'), id: 5 ,type: 'system'}],
              [6, { name: this.$t('test_track.plan_view.custom_component'), id:6,type: 'custom'}]
            ]
          ),
          components: [6],
          previews: [],
          template: {},
          isReport: false
        }
      },
      props: {
        metric: {
          type: Object
        }
      },
      methods: {
        listenGoBack() {
          //监听浏览器返回操作，关闭该对话框
          if (window.history && window.history.pushState) {
            history.pushState(null, null, document.URL);
            window.addEventListener('popstate', this.goBack, false);
          }
        },
        goBack() {
          this.handleClose();
        },
        open(id, isReport) {
          if (isReport) {
            this.isReport = isReport;
          }
          this.template = {
            name: '',
            content: {
              components: [1,2,3,4,5,6],
              customComponent: new Map()
            }
          };
          this.previews = [];
          this.components = [6];
          if (id) {
            this.type = 'edit';
            this.getTemplateById(id);
          } else {
            this.type = 'add';
            this.initComponents();
          }
          this.showDialog = true;
          this.listenGoBack();
        },
        initComponents() {
          this.componentMap.forEach((value, key) => {
            if (this.template.content.components.indexOf(key) < 0 && this.components.indexOf(key) < 0) {
              this.components.push(key);
            }
          });
          this.template.content.components.forEach(item => {
            let preview = this.componentMap.get(item);
            if (preview && preview.type != 'custom') {
              this.previews.push(preview);
            } else {
              if (this.template.content.customComponent) {
                let customComponent = this.template.content.customComponent.get(item.toString());
                if (customComponent) {
                  this.previews.push({id: item, title: customComponent.title, content: customComponent.content});
                }
              }
            }
          });

        },
        handleClose() {
          window.removeEventListener('popstate', this.goBack, false);
          this.showDialog = false;
        },
        change(evt) {
          if (evt.added) {
            let preview = evt.added.element;
            if ( preview.type == 'system') {
              for (let i = 0; i < this.components.length; i++) {
                this.deleteComponentById(preview.id);
              }
            }
          }
        },
        clonePreview(componentId) {
          let component = this.componentMap.get(componentId);
          let id = componentId;
          if (component.type != 'system') {
            id = this.generateComponentId();
          }
          return {
            id: id,
            name: component.name,
            type: component.type,
          };
        },
        handleDelete(preview) {
          if (this.previews.length == 1) {
            this.$warning(this.$t('test_track.plan_view.delete_component_tip'));
            return;
          }
          for (let i = 0; i < this.previews.length; i++) {
            if (this.previews[i].id == preview.id) {
              this.previews.splice(i,1);

              if (preview.type == 'system') {
                this.components.push(preview.id);
              }
              break;
            }
          }
        },
        generateComponentId() {
          return Date.parse(new Date()) + Math.ceil(Math.random()*100000);
        },
        deleteComponentById(id) {
          for (let i = 0; i < this.components.length; i++) {
            if (this.components[i] == id) {
              this.components.splice(i,1);
              break;
            }
          }
        },
        getTemplateById(id) {
          let url = '/case/report/template/get/';
          if (this.isReport) {
            url = '/case/report/get/';
          }
          this.$get(url + id, (response) => {
            this.template = response.data;
            this.template.content = JSON.parse(response.data.content);
            if (this.template.content.customComponent) {
              this.template.content.customComponent = jsonToMap(this.template.content.customComponent);
              if (this.template.startTime) {
                this.metric.startTime = new Date(this.template.startTime);
              }
              if (this.template.endTime) {
                this.metric.endTime = new Date(this.template.endTime);
              }
            }
            this.initComponents();
          });
        },
        handleSave() {
          let pattern = new RegExp("[`~!@#$^&*=|{}':;',<>/?~！@#￥……&*（）——|{}【】‘；：”“'。，、？]");
          if (this.template.name == null || this.template.name == '') {
            this.$warning(this.$t('test_track.plan_view.input_template_name'));
            return;
          } else if (this.template.name.length > 64) {
            this.$warning(this.$t('commons.name') + this.$t('test_track.length_less_than') + '64');
            return;
          } else if (pattern.test(this.template.name)) {
            this.$warning(this.$t('test_track.plan_view.template_special_characters'));
            return;
          }
          let param = {};
          this.buildParam(param);
          let url = '/case/report/template/';
          if (this.isReport) {
            url = '/case/report/';
          }
          this.$post(url + this.type, param, () => {
            this.$success(this.$t('commons.save_success'));
            this.handleClose();
            this.$emit('refresh');
          });
        },
        buildParam(param) {
          let content = {};
          content.components = [];
          content.customComponent = new Map();
          this.previews.forEach(item => {
            content.components.push(item.id);
            if (!this.componentMap.get(item.id)) {
              content.customComponent.set(item.id, {title: item.title, content: item.content})
            }
          });
          param.name = this.template.name;
          if (content.customComponent) {
            content.customComponent = mapToJson(content.customComponent);
          }
          param.content = JSON.stringify(content);
          if (this.type == 'edit') {
            param.id = this.template.id;
          } else {
            param.workspaceId = localStorage.getItem(WORKSPACE_ID);
          }
          if (this.template.workspaceId) {
            param.workspaceId = localStorage.getItem(WORKSPACE_ID);
          }
          if (this.metric && this.metric.startTime) {
            param.startTime = this.metric.startTime.getTime();
          }
          if (this.metric && this.metric.endTime) {
            param.endTime = this.metric.endTime.getTime();
          }
        }
      }
    }
</script>

<style scoped>

  .el-aside {
    border: 1px solid #EBEEF5;
    box-sizing: border-box;
    min-height: calc(100vh - 70px);
    padding: 20px 20px;
    border-radius: 3px;
    background: white;
    position: absolute;
    width: 250px;
    box-shadow: 0 0 2px 0 rgba(31,31,31,0.15), 0 1px 2px 0 rgba(31,31,31,0.15);
  }

  .el-main {
    height: calc(100vh - 70px);
    width: calc(100vw - 320px);
    margin-left: 300px;
    margin-top: 0;
    margin-bottom: 0;
    position: absolute;
  }

  .description > span {
    display: block;
    padding-bottom: 5px;
  }

  .description {
    margin-bottom: 10px;
  }

  .container {
    height: 100vh;
    background: #F5F5F5;
  }

  .preview {
    position: relative;
  }

  .el-icon-error {
    position: absolute;
    right: 11%;
    top: 13px;
    color: gray;
    display:none;
    font-size: 20px;
  }

  .el-icon-error:hover {
    display: inline;
    color: red;
  }

  .preview:hover+i {
    display: inline;
  }

  .preview:hover i{
    display: inline;
  }


</style>
