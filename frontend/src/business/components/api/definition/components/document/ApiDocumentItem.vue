<template>
  <el-container>
    <el-main>
        <div ref="apiDocInfoDiv">
          <div style="margin-bottom: 50px">
            <div style="font-size: 17px">
              {{apiInfo.name}}
              <span class="apiStatusTag">
              <api-status :value="apiInfo.status"/>
            </span>
            </div>
            <!--api请求信息-->
            <el-row class="apiInfoRow">
              <div class="tip">请求信息</div>
            </el-row>
            <el-row class="apiInfoRow">
              <div class="simpleFontClass">
                <el-tag size="medium"
                        :style="{'background-color': getColor(apiInfo.method), border: getColor(apiInfo.method),borderRadius:'0px', marginRight:'20px'}">
                  {{ apiInfo.method }}
                </el-tag>
                {{apiInfo.uri}}
              </div>
            </el-row>
            <!--api请求头-->
            <el-row class="apiInfoRow">
              <div class="blackFontClass">
                请求头：
                <div v-if="getJsonArr(apiInfo.requestHead).length==0">
                  无
                </div>
                <div v-else>
                  <el-table border :show-header="false"
                            :data="getJsonArr(apiInfo.requestHead)" row-key="name" class="test-content adjust-table">
                    <el-table-column  prop="name"
                                      label="名称"
                                      show-overflow-tooltip/>
                    <el-table-column  prop="value"
                                      label="值"
                                      show-overflow-tooltip/>
                  </el-table>
                </div>
              </div>
            </el-row>
            <!--URL参数-->
            <el-row class="apiInfoRow">
              <div class="blackFontClass">
                URL参数：
                <div v-if="getJsonArr(apiInfo.urlParams).length==0">
                  无
                </div>
                <div v-else>
                  <el-table border
                            :data="getJsonArr(apiInfo.urlParams)" row-key="name" class="test-content adjust-table">
                    <el-table-column  prop="name"
                                      label="名称"
                                      min-width="120px"
                                      show-overflow-tooltip/>
                    <el-table-column  prop="isEnable"
                                      label="是否必填"
                                      min-width="80px"
                                      show-overflow-tooltip/>
                    <el-table-column  prop="value"
                                      label="值"
                                      min-width="120px"
                                      show-overflow-tooltip/>
                    <el-table-column  prop="description"
                                      label="描述"
                                      min-width="450px"
                                      show-overflow-tooltip/>
                  </el-table>
                </div>
              </div>
            </el-row>
            <!--api请求体 以及表格-->
            <el-row class="apiInfoRow">
              <div class="blackFontClass">
                请求体
              </div>
              <div class="smallFontClass">
                类型:{{apiInfo.requestBodyParamType}}
              </div>
              <div>
                <el-table border v-if="apiInfo.requestBodyParamType=='kv'"
                          :data="getJsonArr(apiInfo.requestBodyFormData)" row-key="id" class="test-content adjust-table">
                  <el-table-column  prop="name"
                                    label="名称"
                                    min-width="120px"
                                    show-overflow-tooltip/>
                  <el-table-column  prop="contentType"
                                    label="类型"
                                    min-width="80px"
                                    show-overflow-tooltip/>
                  <el-table-column  prop="description"
                                    label="描述"
                                    min-width="450px"
                                    show-overflow-tooltip/>
                  <el-table-column  label="必需"
                                    min-width="80px"
                                    show-overflow-tooltip>
                    <template v-slot:default="scope">
                      <div v-if="scope.enable">是</div>
                      <div v-else-if="!scope.enable">否</div>
                    </template>
                  </el-table-column>
                  <el-table-column  prop="value"
                                    label="默认值"
                                    min-width="120px"
                                    show-overflow-tooltip/>
                </el-table>
              </div>
            </el-row>
            <!--范例展示-->
            <el-row class="apiInfoRow">
              <div class="blackFontClass">
                范例展示
              </div>
              <div class="showDataDiv">
                <br/>
                <p style="margin: 0px 20px;">
                  {{ apiInfo.requestBodyStrutureData }}
                </p>
                <br/>
              </div>
            </el-row>
            <!--响应信息-->
            <el-row class="apiInfoRow">
              <div class="tip">响应信息</div>
            </el-row>
            <el-row class="apiInfoRow">

            </el-row>
            <!--响应头-->
            <el-row class="apiInfoRow">
              <div class="blackFontClass">
                响应头:
                <el-table border :show-header="false"
                          :data="getJsonArr(apiInfo.responseHead)" row-key="name" class="test-content adjust-table">
                  <el-table-column  prop="name"
                                    label="名称"
                                    show-overflow-tooltip/>
                  <el-table-column  prop="value"
                                    label="值"
                                    show-overflow-tooltip/>
                </el-table>
              </div>
            </el-row>
            <!--响应体-->
            <el-row class="apiInfoRow">
              <div class="blackFontClass">
                响应体
              </div>
              <div class="smallFontClass">
                类型:{{apiInfo.responseBodyParamType}}
              </div>
              <div>
                <el-table border v-if="apiInfo.responseBodyParamType=='kv'"
                          :data="getJsonArr(apiInfo.responseBodyFormData)" row-key="id" class="test-content adjust-table">
                  <el-table-column  prop="name"
                                    label="名称"
                                    min-width="120px"
                                    show-overflow-tooltip/>
                  <el-table-column  prop="contentType"
                                    label="类型"
                                    min-width="80px"
                                    show-overflow-tooltip/>
                  <el-table-column  prop="description"
                                    label="描述"
                                    min-width="450px"
                                    show-overflow-tooltip/>
                  <el-table-column  label="必需"
                                    min-width="80px"
                                    show-overflow-tooltip>
                    <template v-slot:default="scope">
                      <div v-if="scope.enable">是</div>
                      <div v-else-if="!scope.enable">否</div>
                    </template>
                  </el-table-column>
                  <el-table-column  prop="value"
                                    label="默认值"
                                    min-width="120px"
                                    show-overflow-tooltip/>
                </el-table>
              </div>
            </el-row>
            <!--响应状态码-->
            <el-row class="apiInfoRow">
              <div class="blackFontClass">
                响应状态码:
                <el-table border :show-header="false"
                          :data="getJsonArr(apiInfo.responseCode)" row-key="name" class="test-content adjust-table">
                  <el-table-column  prop="name"
                                    label="名称"
                                    show-overflow-tooltip/>
                  <el-table-column  prop="value"
                                    label="值"
                                    show-overflow-tooltip/>
                </el-table>
              </div>
            </el-row>
          </div>
        </div>
    </el-main>
    <!-- 右侧列表 -->
    <el-aside width="200px">
      <div ref="apiDocList">
        <el-steps style="height: 40%" direction="vertical" :active="apiStepIndex">
            <el-step v-for="(apiInfo) in apiSimpleInfoArray" :key="apiInfo.id" @click.native="clickStep(apiInfo.id)">
              <el-link slot="title">{{apiInfo.name}}</el-link>
            </el-step>
        </el-steps>
      </div>
    </el-aside>
  </el-container>
</template>

<script>
import MsAnchor from "./Anchor";
import {API_METHOD_COLOUR} from "@/business/components/api/definition/model/JsonData";
import {jsonToMap} from "@/common/js/utils";
import ApiStatus from "@/business/components/api/definition/components/list/ApiStatus";
import {buildNodePath} from "@/business/components/api/definition/model/NodeTree";
export default {
  name: "ApiDocumentItem",
  components: {
    MsAnchor,ApiStatus,
  },
  data() {
    return {
      apiStepIndex:0,
      apiSimpleInfoArray:[],
      apiInfo:{
          method:"无",
          uri:"无",
          name:"无",
          id:"",
          requestHead:"无",
          urlParams:"无",
          requestBodyParamType:"无",
          requestBodyFormData:'[]',
          requestBodyStrutureData:"",
          responseHead:"无",
          responseBody:"",
          responseBodyParamType:"无",
          responseBodyFormData:"无",
          responseBodyStrutureData:"无",
          responseCode:"无",
      },
      methodColorMap: new Map(API_METHOD_COLOUR),
      clientHeight:'',//坚挺浏览器高度
    }
  },
  props: {
    projectId:String,
    moduleIds:Array,
  },
  activated() {
    this.initApiDocSimpleList();
    this.clientHeight = `${document.documentElement.clientHeight}`;//获取浏览器可视区域高度
    let that = this;
    window.onresize = function(){
      this.clientHeight =  `${document.documentElement.clientHeight}`;
      this.changeFixed(this.clientHeight);
    }
  },
  created: function () {
    this.initApiDocSimpleList();
    this.clientHeight = `${document.documentElement.clientHeight}`;//获取浏览器可视区域高度
    let that = this;
    window.onresize = function(){
      this.clientHeight =  `${document.documentElement.clientHeight}`;
      this.changeFixed(this.clientHeight);
    }
  },
  mounted() {
    let that = this;
    window.onresize = function(){
      this.clientHeight =  `${document.documentElement.clientHeight}`;
      if(that.$refs.apiDocInfoDiv){
        that.$refs.apiDocInfoDiv.style.minHeight = this.clientHeight - 300 + 'px';
        that.$refs.apiDocList.style.minHeight = this.clientHeight - 300 + 'px';

      }
    }
  },
  computed: {
    documentId: function () {
      return this.$route.params.documentId;
    }
  },
  watch: {
    '$route.params.documentId'() {
    },
    moduleIds(){
      this.initApiDocSimpleList();
    },
    clientHeight(){     //如果clientHeight 发生改变，这个函数就会运行
      this.changeFixed(this.clientHeight);
    }
  },
  methods: {
    changeFixed(clientHeight){
      if(this.$refs.apiDocInfoDiv){
        this.$refs.apiDocInfoDiv.style.height = clientHeight -300 + 'px';
        this.$refs.apiDocInfoDiv.style.overflow = 'auto';
        this.$refs.apiDocList.style.height = clientHeight -300 + 'px';

      }
    },
    initApiDocSimpleList(){
      let simpleRequest = {};
      if(this.projectId!=null && this.projectId!= ""){
        simpleRequest.projectId=this.projectId;
      }
      if(this.documentId!=null && this.documentId!= ""){
        simpleRequest.documentId=this.documentId;
      }
      if(this.moduleIds.length>0){
        simpleRequest.moduleIds=this.moduleIds;
      }

      let simpleInfoUrl = "/api/document/selectApiSimpleInfo";
      this.$post(simpleInfoUrl, simpleRequest, response => {
        this.apiSimpleInfoArray = response.data;
        this.apiStepIndex = 0;
        if(this.apiSimpleInfoArray.length>0){
          this.selectApiInfo(this.apiSimpleInfoArray[0].id);
        }
      });
    },
    selectApiInfo(apiId){
      let simpleInfoUrl = "/api/document/selectApiInfoById/"+apiId;
      this.$get(simpleInfoUrl, response => {
        this.apiInfo = response.data;
      });
    },
    clickStep(apiId){
      for (let index = 0; index < this.apiSimpleInfoArray.length; index++) {
        if(apiId == this.apiSimpleInfoArray[index].id){
          this.apiStepIndex = index;
          break;
        }
      }
      this.selectApiInfo(apiId);
    },
    stepClick(stepIndex){
      this.apiStepIndex = stepIndex;
    },
    getColor(enable, method) {
      return this.methodColorMap.get(method);
    },
    getJsonArr(jsonString){
      let returnJsonArr = [];
      if(jsonString == '无'){
        return returnJsonArr;
      }

      let jsonArr = JSON.parse(jsonString);
      //遍历，把必填项空的数据去掉
      for(var index = 0;index < jsonArr.length;index++){
        var item = jsonArr[index];
        if(item.name!="" && item.name!=null){
          returnJsonArr.push(item);
        }
      }
      return returnJsonArr;
    }
  },
}
</script>

<style scoped>
.simpleFontClass{
  font-size: 14px;
}
.blackFontClass{
  font-weight: bold;
  font-size: 14px;
}
.smallFontClass{
  font-size: 13px;
  margin: 20px 0px;
}
.tip {
  padding: 3px 5px;
  font-size: 14px;
  border-radius: 4px;
  border-left: 4px solid #783887;
}
.apiInfoRow{
  margin: 20px 10px;
}
.apiStatusTag{
  margin: 20px 5px;
}
.showDataDiv{
  background-color: #F5F7F9;
  margin: 20px 0px;
}

/*
步骤条中，已经完成后的节点样式和里面a标签的样式
*/
/deep/ .el-step__head.is-finish {
  color: #C0C4CC;
  border-color: #C0C4CC;
}
/deep/ .el-step__title.is-finish /deep/ .el-link.el-link--default{
  color: #C0C4CC;
}
/*
步骤条中，当前节点样式和当前a标签的样式
*/
/deep/ .el-step__head.is-process {
  color: #783887;
  border-color: #783887;
}
/deep/ .el-step__title.is-process /deep/ .el-link.el-link--default {
  color: #783887;
}

</style>
