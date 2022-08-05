<template>
  <el-row>
    <el-col :class="asideActive?'aside-active':'aside-inActive'">
      <svg-icon v-show="!asideActive" icon-class="button_right" class="open-button" @click="asideActiveChange(true)" />
      <el-row v-show="asideActive" style="padding: 12px 12px 0 12px ">
        <el-row>
          <span class="icon iconfont icon-close icon20 insert" @click="closePreview()" />
          <span class="main-title">{{ $t('panel.template_preview') }}</span>
          <span style="float: right" class="icon iconfont icon-icon_up-left_outlined insert icon20" @click="asideActiveChange(false)" />
        </el-row>
        <el-row class="margin-top16 search-area">
          <el-input
            v-model="searchText"
            size="small"
            prefix-icon="el-icon-search"
            class="title-name-search"
            :placeholder="$t('panel.enter_template_name_tips')"
            clearable="true"
          />
          <span class="icon iconfont icon-icon-filter insert-filter filter-icon-span" :class="extFilterActive?'filter-icon-active':''" @click="extFilterActiveChange()" />
        </el-row>
        <el-row v-show="extFilterActive">
          <el-select v-model="marketActiveTab" class="margin-top16" size="small" placeholder="请选择">
            <el-option
              v-for="item in marketTabs"
              :key="item"
              :label="item"
              :value="item"
            />
          </el-select>
        </el-row>
        <el-divider />
      </el-row>

      <el-row v-show="asideActive" class="aside-list" :class="extFilterActive?'aside-list-filter-active':''">
        <template-market-preview-item
          v-for="(templateItem) in currentMarketTemplateShowList"
          v-show="templateItem.showFlag"
          :key="templateItem.id"
          :template="templateItem"
          :base-url="baseUrl"
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
    </el-col>
    <el-col class="main-area" :class="asideActive ? 'main-area-active': ''">
      <el-row>
        <span v-if="curTemplate" class="template-title">{{ curTemplate.title }}</span>
        <el-button style="float: right" type="primary" size="small" @click="templateApply(curTemplate)">{{ $t('panel.apply_this_template') }}</el-button>
      </el-row>
      <el-row class="img-main">
        <img height="100%" :src="templatePreviewUrl" alt="">
      </el-row>
    </el-col>
  </el-row>
</template>

<script>
import { searchMarket, getCategories } from '@/api/templateMarket'
import { groupTree } from '@/api/panel/panel'
import { DEFAULT_COMMON_CANVAS_STYLE_STRING } from '@/views/panel/panel'
import TemplateMarketPreviewItem from '@/views/panel/templateMarket/component/TemplateMarketPreviewItem'

export default {
  name: 'MarketPreview',
  components: { TemplateMarketPreviewItem },
  props: {
    previewId: {
      type: String,
      default: null
    }
  },
  data() {
    return {
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
      currentMarketTemplateShowList: [],
      networkStatus: true,
      curTemplate: null
    }
  },
  computed: {
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
      _this.currentMarketTemplateShowList.forEach(template => {
        if (val === template.id) {
          _this.previewTemplate(template)
        }
      })
    }
  },
  mounted() {
    this.initMarketTemplate()
    this.getGroupTree()
  },
  methods: {
    initMarketTemplate() {
      searchMarket({}).then(rsp => {
        this.baseUrl = rsp.data.baseUrl
        this.currentMarketTemplateShowList = rsp.data.contents
        this.hasResult = true
      }).catch(() => {
        this.networkStatus = false
      })
      getCategories().then(rsp => {
        this.marketTabs = rsp.data
        this.marketActiveTab = this.marketTabs[0]
      }).catch(() => {
        this.networkStatus = false
      })
      if (this.previewId) {
        const _this = this
        _this.currentMarketTemplateShowList.forEach(template => {
          if (_this.previewId === template.id) {
            _this.previewTemplate(template)
          }
        })
      }
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
    templateApply(template) {
      this.$emit('templateApply', template)
    },
    closeDialog() {
      this.$emit('closeDialog')
    },
    handleClick(item) {

    },
    initTemplateShow() {
      this.hasResult = false
      this.currentMarketTemplateShowList.forEach(template => {
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
.aside-list {
  padding: 0px 12px 12px 12px;
  width: 100%;
  height: calc(100vh - 200px);
  overflow-x: hidden;
  overflow-y: auto;
}

.aside-list-filter-active {
  height: calc(100vh - 250px);
}

.template-main {
  border-radius: 4px;
  box-shadow: 0 0 2px 0 rgba(31, 31, 31, 0.15), 0 1px 2px 0 rgba(31, 31, 31, 0.15);
  border: solid 2px #fff;
  padding-bottom: 24px;
  min-height: calc(100vh - 190px);
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

//.open-button:hover{
//  transition: 0.5s;
// width: 50px;
//}
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
</style>
