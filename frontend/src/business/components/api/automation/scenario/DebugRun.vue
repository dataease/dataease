<template>
  <div></div>
</template>
<script>
  import {getUUID, getCurrentProjectID} from "@/common/js/utils";
  import {createComponent} from "../../definition/components/jmeter/components";

  export default {
    name: 'MsDebugRun',
    components: {},
    props: {
      environment: String,
      debug: Boolean,
      reportId: String,
      runData: Object,
    },
    data() {
      return {
        result: {},
        loading: false,
        runId: "",
        reqNumber: 0,
      }
    },

    watch: {
      // 初始化
      reportId() {
        this.run()
      }
    },
    methods: {
      setFiles(item, bodyUploadFiles, obj) {
        if (item.body) {
          if (item.body.kvs) {
            item.body.kvs.forEach(param => {
              if (param.files) {
                param.files.forEach(item => {
                  if (item.file) {
                    if (!item.id) {
                      let fileId = getUUID().substring(0, 12);
                      item.name = item.file.name;
                      item.id = fileId;
                    }
                    obj.bodyUploadIds.push(item.id);
                    bodyUploadFiles.push(item.file);
                  }
                });
              }
            });
          }
          if (item.body.binary) {
            item.body.binary.forEach(param => {
              if (param.files) {
                param.files.forEach(item => {
                  if (item.file) {
                    if (!item.id) {
                      let fileId = getUUID().substring(0, 12);
                      item.name = item.file.name;
                      item.id = fileId;
                    }
                    obj.bodyUploadIds.push(item.id);
                    bodyUploadFiles.push(item.file);
                  }
                });
              }
            });
          }
        }
        if (item && item.files) {
          item.files.forEach(fileItem => {
            if (fileItem.file) {
              if (!fileItem.id) {
                let fileId = getUUID().substring(0, 12);
                fileItem.name = fileItem.file.name;
                fileItem.id = fileId;
              }
              obj.bodyUploadIds.push(fileItem.id);
              bodyUploadFiles.push(fileItem.file);
            }
          });
        }
      },
      recursiveFile(arr, bodyUploadFiles, obj) {
        arr.forEach(item => {
          this.setFiles(item, bodyUploadFiles, obj);
          if (item.hashTree != undefined && item.hashTree.length > 0) {
            this.recursiveFile(item.hashTree, bodyUploadFiles, obj);
          }
        });
      },
      getBodyUploadFiles(obj) {
        let bodyUploadFiles = [];
        obj.bodyUploadIds = [];
        let request = this.runData;
        request.hashTree.forEach(item => {
          this.setFiles(item, bodyUploadFiles, obj);
          if (item.hashTree != undefined && item.hashTree.length > 0) {
            this.recursiveFile(item.hashTree, bodyUploadFiles, obj);
          }
        })
        if (request.variables) {
          request.variables.forEach(item => {
            this.setFiles(item, bodyUploadFiles, obj);
          })
        }
        return bodyUploadFiles;
      },
      run() {
        let testPlan = createComponent('TestPlan');
        let threadGroup = createComponent('ThreadGroup');
        threadGroup.hashTree = [];
        threadGroup.name = this.reportId;
        threadGroup.enableCookieShare = this.runData.enableCookieShare;
        threadGroup.hashTree.push(this.runData);
        testPlan.hashTree.push(threadGroup);
        let reqObj = {id: this.reportId, reportId: this.reportId, scenarioName: this.runData.name, scenarioId: this.runData.id, environmentId: this.environment, testElement: testPlan, projectId: getCurrentProjectID()};
        let bodyFiles = this.getBodyUploadFiles(reqObj);
        let url = "/api/automation/run/debug";
        this.$fileUpload(url, null, bodyFiles, reqObj, response => {
          this.runId = response.data;
          this.$emit('runRefresh', {});
        }, erro => {
        });
      }
    }
  }
</script>
