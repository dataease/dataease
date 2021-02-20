<template>
  <el-card class="card-content" v-if="isShow">

    <el-button-group v-if="isShowChangeButton">

      <el-tooltip class="item" effect="dark" :content="$t('api_test.definition.api_title')" placement="left">
        <el-button plain style="width: 44px;height: 32px;padding: 5px 8px;"  :class="{active: showApiList}" @click="changeTab('api')">API</el-button>
      </el-tooltip>

      <el-tooltip class="item" effect="dark" :content="$t('api_test.definition.case_title')" placement="top">
        <el-button plain style="width: 44px;height: 32px;padding: 1px;"  :class="{active: showTestCaseList}" @click="changeTab('testCase')">CASE</el-button>
      </el-tooltip>

      <el-tooltip class="item" effect="dark" :content="$t('api_test.definition.doc_title')" placement="right">
        <el-button plain  style="width: 44px;height: 32px;padding: 1px;"  :class="{active: showDocList}" @click="changeTab('doc')">
          {{ $t('api_test.definition.doc_title') }}</el-button>
      </el-tooltip>

    </el-button-group>

    <template v-slot:header>
      <slot name="header"></slot>
    </template>
    <slot></slot>
  </el-card>
</template>

<script>
export default {
  name: "ApiListContainerWithDoc",
  data() {
    return {
      isShow: true,
      showApiList:false,
      showTestCaseList:false,
      showDocList:true,
    }
  },
  props: {
    activeDom: String,
    isShowChangeButton: {
      type: Boolean,
      default: true
    }
  },
  created() {
    this.refreshButtonActiveClass(this.activeDom);
  },
  methods: {
    changeTab(tabType){
      this.refreshButtonActiveClass(tabType);
      this.$emit("activeDomChange",tabType);
    },
    refreshButtonActiveClass(tabType){
      if(tabType === "testCase"){
        this.showApiList = false;
        this.showTestCaseList = true;
        this.showDocList = false;
      }else if(tabType === "doc"){
        this.showApiList = false;
        this.showTestCaseList = false;
        this.showDocList = true;
      }else{
        this.showApiList = true;
        this.showTestCaseList = false;
        this.showDocList = false;
      }
    }
  },
}
</script>

<style scoped>

.active {
  border: solid 1px #6d317c;
  background-color: #7C3985;
  color: #FFFFFF;
}

.case-button {
  border-left: solid 1px #6d317c;
}

.item{
  border: solid 1px #6d317c;
}

</style>
