<template>
  <el-row class="preview-outer-body market-main" v-loading="$store.getters.loadingMap[$store.getters.currentPath]">
    <el-row style="position: absolute;left: 5px;top: 5px">
      <span class="icon iconfont icon-close icon20 insert" @click="closePreview()" />
    </el-row>
    <el-row>
      <el-col span="12">
        <span class="title-left">{{currentApp.name}}</span>
      </el-col>
    </el-row>
    <el-row v-if="currentTemplateShowList&&currentTemplateShowList.length>0" class="template-main-preview">
      <el-row class="preview-slider">
        <el-row class="top-list" :style="topSlideStyle">
          <template-market-preview-item
            v-for="(templateItem) in currentTemplateShowList"
            :key="templateItem.id"
            :template="templateItem"
            :active="active(templateItem)"
            @previewTemplate="previewTemplate"
          />
          <el-row v-show="!hasResult" class="custom-position">
            <div style="text-align: center">
              <svg-icon icon-class="no_result" style="font-size: 75px;margin-bottom: 16px" />
              <br>
              <span>{{ $t('commons.no_result') }}</span>
            </div>
          </el-row>
        </el-row>
      </el-row>
      <el-row style="margin-top: 24px">
        <app-template-log :app-template-id="curTemplate.id" :position="'templateLog'" @applyNew="applyNew" class="log-area"></app-template-log>
      </el-row>
    </el-row>
    <el-row v-else class="template-main-preview">
      <el-empty
        :image="noneImg"
        :description="$t('app_template.no_apps')"
      ></el-empty>
    </el-row>
    <el-dialog
      v-loading="$store.getters.loadingMap[$store.getters.currentPath]"
      :title="$t('panel.apply_template')"
      :visible.sync="applyNewVisible"
      width="80%"
      height="90vh"
      top="5vh"
      class="market-dialog-css"
      append-to-body="true"
      :destroy-on-close="true"
    >
      <el-row class="ds-from-main">
        <DsAppForm :params="dsParams" v-if="applyNewVisible" :t-data="this.tData" :ds-types="dsTypes" :opt-type="'appApply'" :attach-params="attachParams"></DsAppForm>
      </el-row>
    </el-dialog>
  </el-row>
</template>

<script>
import { searchAppTemplate, getCategories } from '@/api/appTemplateMarket'
import { groupTree } from '@/api/panel/panel'
import { DEFAULT_COMMON_CANVAS_STYLE_STRING } from '@/views/panel/panel'
import TemplateMarketPreviewItem from '@/views/panel/appTemplateMarket/component/TemplateMarketPreviewItem'
import AppTemplateLog from "@/views/panel/appTemplateMarket/log";
import {listDatasourceType} from "@/api/system/datasource";
import DsAppForm from "@/views/system/datasource/DsAppForm";

export default {
  name: 'MarketPreview',
  components: {DsAppForm, AppTemplateLog, TemplateMarketPreviewItem },
  props: {
    previewId: {
      type: String,
      default: null
    },
    currentApp: {
      type: Object,
      default: null
    }
  },
  data() {
    return {
      noneImg: require('@/assets/None.png'),
      dsParams:{},
      attachParams:{},
      tData: [],
      dsTypes: [],
      applyNewVisible: false,
      hasResult: true,
      extFilterActive: false,
      asideActive: true,
      previewVisible: false,
      templatePreviewUrl: null,
      marketTabs: null,
      marketActiveTab: null,
      searchText: null,
      panelForm: {
        name: null,
        pid: null,
        nodeType: 'panel',
        templateUrl: null,
        newFrom: 'new_market_template',
        panelType: 'self',
        panelStyle: JSON.stringify(DEFAULT_COMMON_CANVAS_STYLE_STRING),
        panelData: '[]'
      },
      panelGroupList: [],
      curApplyTemplate: null,
      folderSelectShow: false,
      baseUrl: 'https://dataease.io/templates',
      currentTemplateShowList: [],
      networkStatus: true,
      curTemplate: {}
    }
  },
  computed: {
    topSlideStyle(){
      return {
        width: (275*this.currentTemplateShowList.length +50)+'px'
      }
    }
  },
  watch: {
    marketActiveTab() {
      this.initTemplateShow()
    },
    searchText() {
      this.initTemplateShow()
    },
    previewId(val) {
      const _this = this
      _this.currentTemplateShowList.forEach(template => {
        if (val === template.id) {
          _this.previewTemplate(template)
        }
      })
    }
  },
  mounted() {
    this.initMarketTemplate()
    this.getGroupTree()
    this.datasourceTypes()
  },
  methods: {
    datasourceTypes() {
      listDatasourceType().then(res => {
        this.dsTypes = res.data
      })
    },
    applyNew(){
      this.applyNewVisible = true
      this.attachParams={
        appTemplateId:this.curTemplate.id,
        name:this.curTemplate.name,
      }
    },
    initMarketTemplate() {
      searchAppTemplate({pid:this.previewId}).then(rsp => {
        this.currentTemplateShowList = rsp.data
        if(rsp.data){
          this.curTemplate=rsp.data[0]
        }
        this.hasResult = true
      }).catch(() => {
        this.networkStatus = false
      })
    },
    getGroupTree() {
      groupTree({ nodeType: 'folder' }).then(res => {
        this.panelGroupList = res.data
      })
    },
    normalizer(node) {
      // 去掉children=null的属性
      if (node.children === null || node.children === 'null') {
        delete node.children
      }
    },
    appTemplately(template) {
      this.$emit('appTemplately', template)
    },
    closeDialog() {
      this.$emit('closeDialog')
    },
    handleClick(item) {

    },
    initTemplateShow() {
      this.hasResult = false
      this.currentTemplateShowList.forEach(template => {
        template.showFlag = this.templateShow(template)
        if (template.showFlag) {
          this.hasResult = true
        }
      })
    },
    templateShow(templateItem) {
      let categoryMarch = false
      let searchMarch = false
      templateItem.categories.forEach(category => {
        if (category.name === this.marketActiveTab) {
          categoryMarch = true
        }
      })
      if (!this.searchText || templateItem.title.indexOf(this.searchText) > -1) {
        searchMarch = true
      }
      return categoryMarch && searchMarch
    },
    previewTemplate(template) {
      this.curTemplate = template
      if (template.thumbnail.indexOf('http') > -1) {
        this.templatePreviewUrl = template.thumbnail
      } else {
        this.templatePreviewUrl = this.baseUrl + template.thumbnail
      }
    },
    asideActiveChange(prop) {
      this.asideActive = prop
    },
    extFilterActiveChange() {
      this.extFilterActive = !this.extFilterActive
      this.marketActiveTab = this.marketTabs[0]
    },
    closePreview() {
      this.$emit('closePreview')
    },
    active(template) {
      return this.curTemplate && this.curTemplate.id === template.id
    }
  }
}
</script>

<style lang="scss" scoped>
.top-list {
  display:inline-block !important;
  height: 210px;
}

.aside-list-filter-active {
  height: calc(100vh - 250px);
}

.template-main-preview {
  margin-top: 12px;
  border-radius: 4px;
  box-shadow: 0 0 2px 0 rgba(31, 31, 31, 0.15), 0 1px 2px 0 rgba(31, 31, 31, 0.15);
  border: solid 2px #fff;
  padding: 24px;
  min-height: calc(100vh - 147px);
  background-color: var(--ContentBG,#ffffff);
}

.preview-slider{
  width: calc(100% - 24px);
  overflow-x: auto;
  overflow-y: hidden;
}

.market-main {
  padding: 24px
}

.title-left {
  float: left;
  font-size: 20px;
  font-weight: 500;
  line-height: 28px;
}

.title-right {
  float: right;
  width: 320px;
}

.dialog-footer-self {
  text-align: center;
}

.search-button-self {
  text-align: left;
  padding-left: 10px;
}

.topbar-icon-active {
  cursor: pointer;
  transition: .1s;
  border-radius: 3px;
  font-size: 22px;
  background-color: rgb(245, 245, 245);

  &:active {
    color: #000;
    border-color: #3a8ee6;
    background-color: red;
    outline: 0;
  }

  &:hover {
    background-color: rgba(31, 35, 41, 0.1);
    color: #3a8ee6;
  }
}

.custom-position {
  height: 70vh;
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-size: 14px;
  flex-flow: row nowrap;
  color: #646A73;
  font-weight: 400;
}

.aside-active {
  width: 206px;
  height: calc(100vh - 56px);
  overflow-x: hidden;
  overflow-y: auto;
  background-color: var(--ContentBG,#ffffff);
}

.aside-inActive{
  position: relative;
  width: 0px;
}

.main-area-active {
  width: calc(100% - 206px)!important;
}

.main-area {
  width: 100%;
  padding:24px;
  text-align: center;
  height: calc(100vh - 56px);
  transition: 0.5s;
}

.title-name-search {
  width: 140px;
  float: left;
}

.icon20 {
  font-size: 20px !important;
}

.main-title {
  margin-left: 8px;
  font-weight: 500;
  font-size: 16px;
  line-height: 24px;
  color: var(--TextPrimary, #1F2329);
}

.insert-filter {
  display: inline-block;
  font-weight: 400 !important;
  font-family: PingFang SC;
  line-height: 1;
  white-space: nowrap;
  cursor: pointer;
  color: var(--TextPrimary, #1F2329);
  -webkit-appearance: none;
  text-align: center;
  box-sizing: border-box;
  outline: 0;
  margin: 0;
  transition: .1s;
  border-radius: 3px;

  &:active {
    color: #000;
    border-color: #3a8ee6;
    background-color: red;
    outline: 0;
  }

  &:hover {
    background-color: rgba(31, 35, 41, 0.1);
    color: #3a8ee6;
  }
}

.insert {
  display: inline-block;
  font-weight: 400 !important;
  font-family: PingFang SC;
  line-height: 1;
  white-space: nowrap;
  cursor: pointer;
  color: #646A73;
  -webkit-appearance: none;
  text-align: center;
  box-sizing: border-box;
  outline: 0;
  margin: 0;
  transition: .1s;
  border-radius: 3px;

  &:active {
    color: #000;
    border-color: #3a8ee6;
    background-color: red;
    outline: 0;
  }

  &:hover {
    background-color: rgba(31, 35, 41, 0.1);
    color: #3a8ee6;
  }
}

.template-title{
  float: left;
  font-weight: 500;
  font-size: 20px;
  line-height: 28px;
  margin-bottom: 24px;
  color: var(--TextPrimary, #1F2329);
}

.margin-top16 {
  margin-top: 16px;
}
.img-main{
  border-radius: 4px;
  background: #0F1114;
  overflow-x: auto;
  overflow-y: hidden;
  height: calc(100% - 50px)!important;
}
.open-button{
  cursor: pointer;
  font-size: 30px;
  position: absolute;
  left: 0;
  top: 16px;
  z-index: 2;
}

.open-button:hover{
  color: #3a8ee6;
}
.filter-icon-span{
  float: left;
  border: 1px solid #DCDFE6;
  width: 32px;
  height: 32px;
  border-radius: 4px;
  padding: 7px;
  margin-left: 8px;
}

.filter-icon-active{
  border: 1px solid #3370FF;
  color: #3370FF;
}

.filter-icon-active{
  border: 1px solid #3370FF;
  color: #3370FF;
}

.search-area{
  width: 100%;
  position: relative;
}

.preview-outer-body{
  width: 100%;
  height: calc(100vh - 56px);
  background-color: var(--MainBG,#f5f6f7);
}

.market-main{
  padding:24px
}

.title-left{
  float: left;
  font-size: 20px;
  font-weight: 500;
  line-height: 28px;
  color: var(--TextPrimary, #1F2329);
}
.log-area{
  height: calc(100vh - 420px);
}

.ds-from-main {
  overflow-y: auto;
  overflow-x: hidden;
  border-radius: 4px;
  box-shadow: 0 0 2px 0 rgba(31, 31, 31, 0.15), 0 1px 2px 0 rgba(31, 31, 31, 0.15);
  border: solid 2px #fff;
  height: 77vh;
  background-color: var(--ContentBG,#ffffff);
}
</style>
