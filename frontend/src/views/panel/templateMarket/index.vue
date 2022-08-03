<template>
  <el-row class="outer-body">
    <!--预览模式-->
    <MarketPreview v-show="previewModel" :preview-id="templatePreviewId" @closePreview="previewModel=false" @templateApply="templateApply" />
    <!--列表模式-->
    <el-row v-show="!previewModel" class="market-main">
      <el-row>
        <el-col span="12">
          <span class="title-left">{{ $t('panel.template_market') }}</span>
        </el-col>
        <el-col span="12">
          <el-input v-model="searchText" prefix-icon="el-icon-search" size="small" class="title-right" :placeholder="$t('panel.enter_template_name_tips')" clearable="true" />
        </el-col>
      </el-row>
      <el-row>
        <el-tabs v-model="marketActiveTab" @tab-click="handleClick">
          <el-tab-pane v-for="tabItem in marketTabs" :key="tabItem" :label="tabItem" :name="tabItem" />
        </el-tabs>
      </el-row>
      <el-row v-show="networkStatus && hasResult" id="template-main" v-loading="$store.getters.loadingMap[$store.getters.currentPath]" class="template-main">
        <el-col
          v-for="(templateItem) in currentMarketTemplateShowList"
          v-show="templateItem.showFlag"
          :key="templateItem.id"
          style="text-align: center;padding: 24px 12px 0 12px"
          :style="{width: templateSpan}"
        >
          <template-market-item
            :template="templateItem"
            :base-url="baseUrl"
            :width="templateCurWidth"
            @templateApply="templateApply"
            @templatePreview="templatePreview"
          />
        </el-col>
      </el-row>
      <el-row v-show="networkStatus && !hasResult" class="custom-position template-main">
        <div style="text-align: center">
          <svg-icon icon-class="no_result" style="font-size: 75px;margin-bottom: 16px" />
          <br>
          <span>{{ $t('commons.no_result') }}</span>
        </div>
      </el-row>
      <el-row v-show="!networkStatus" class="custom-position template-main">
        {{ $t('panel.market_network_tips') }}
      </el-row>
    </el-row>
    <el-dialog
      v-loading="$store.getters.loadingMap[$store.getters.currentPath]"
      :title="$t('panel.apply_template')"
      :visible.sync="folderSelectShow"
      width="600px"
      class="market-dialog-css"
      append-to-body="true"
      :destroy-on-close="true"
    >
      <el-form ref="panelForm" :model="panelForm" :rules="rule" label-width="80px">
        <el-form-item :label="$t('panel.name')" prop="name">
          <el-input v-model="panelForm.name" :clearable="true" :placeholder="$t('panel.enter_name_tips')" />
        </el-form-item>
        <el-form-item :label="$t('commons.folder')" prop="pid">
          <treeselect
            v-model="panelForm.pid"
            :clearable="false"
            :options="panelGroupList"
            :normalizer="normalizer"
            :placeholder="$t('chart.select_group')"
            :no-children-text="$t('commons.treeselect.no_children_text')"
            :no-options-text="$t('commons.treeselect.no_options_text')"
            :no-results-text="$t('commons.treeselect.no_results_text')"
          />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer dialog-footer-self">
        <el-button size="mini" @click="folderSelectShow=false">{{ $t('commons.cancel') }}</el-button>
        <el-button size="mini" type="primary" :disabled="!panelForm.name || !panelForm.pid" @click="apply">{{ $t('commons.confirm') }}</el-button>
      </div>
    </el-dialog>
  </el-row>
</template>

<script>
import { searchMarket, getCategories } from '@/api/templateMarket'
import TemplateMarketItem from '@/views/panel/templateMarket/component/TemplateMarketItem'
import { groupTree, panelSave } from '@/api/panel/panel'
import { DEFAULT_COMMON_CANVAS_STYLE_STRING } from '@/views/panel/panel'
import MarketPreview from '@/views/panel/templateMarket/component/MarketPreview'
import elementResizeDetectorMaker from 'element-resize-detector'

export default {
  name: 'TemplateMarket',
  components: { MarketPreview, TemplateMarketItem },
  data() {
    return {
      hasResult: true,
      templateMiniWidth: 330,
      templateCurWidth: 310,
      templateSpan: '25%',
      previewModel: false,
      previewVisible: false,
      templatePreviewId: '',
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
      rule: {
        name: [
          {
            required: true,
            message: this.$t('panel.template_name_tips'),
            trigger: 'blur'
          }
        ],
        pid: [
          {
            required: true,
            message: '',
            trigger: 'blur'
          }
        ]
      }
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
    }
  },
  mounted() {
    this.initMarketTemplate()
    this.getGroupTree()
    const _this = this
    const erd = elementResizeDetectorMaker()
    const templateMainDom = document.getElementById('template-main')
    // 监听div变动事件
    erd.listenTo(templateMainDom, element => {
      _this.$nextTick(() => {
        const curSeparator = Math.trunc(templateMainDom.offsetWidth / _this.templateMiniWidth)
        _this.templateSpan = (100 / Math.trunc(templateMainDom.offsetWidth / _this.templateMiniWidth)) + '%'
        _this.templateCurWidth = Math.trunc(templateMainDom.offsetWidth / curSeparator) - 33
      })
    })
  },
  methods: {
    initMarketTemplate() {
      searchMarket({}).then(rsp => {
        this.baseUrl = rsp.data.baseUrl
        this.currentMarketTemplateShowList = rsp.data.contents
      }).catch(() => {
        this.networkStatus = false
      })
      getCategories().then(rsp => {
        this.marketTabs = rsp.data
        this.marketActiveTab = this.marketTabs[0]
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
    templateApply(template) {
      this.curApplyTemplate = template
      this.panelForm.name = template.title
      this.panelForm.templateUrl = this.baseUrl + template.metas.theme_repo
      this.folderSelectShow = true
    },
    apply() {
      if (this.panelForm.name.length > 50) {
        this.$warning(this.$t('commons.char_can_not_more_50'))
        return false
      }

      if (!this.panelForm.templateUrl) {
        this.$warning('未获取模板下载链接请联系模板市场官方')
        return false
      }
      panelSave(this.panelForm).then(response => {
        this.$message({
          message: this.$t('commons.save_success'),
          type: 'success',
          showClose: true
        })
        this.folderSelectShow = false
        this.$router.push({ name: 'panel', params: response.data })
      }).catch(() => {
        this.loading = false
      })
    },
    closeDialog() {
      this.$emit('closeDialog')
    },
    handleClick(item) {

    },
    initTemplateShow() {
      let tempHasResult = false
      this.currentMarketTemplateShowList.forEach(template => {
        template.showFlag = this.templateShow(template)
        if (template.showFlag) {
          tempHasResult = true
        }
      })
      if (this.currentMarketTemplateShowList.length > 0) {
        this.hasResult = tempHasResult
      }
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
    templatePreview(previewId) {
      this.templatePreviewId = previewId
      this.previewModel = true
    },
    newPanel() {

    }
  }
}
</script>

<style lang="scss" scoped>
  .template-main{
    text-align: center;
    border-radius: 4px;
    padding: 0 12px 24px 12px;
    height: calc(100vh - 190px)!important;
    overflow-x: hidden;
    overflow-y: auto;
    background-color: var(--ContentBG,#ffffff);
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
  .title-right{
    float: right;
    width: 320px;
  }
  .dialog-footer-self{
    text-align: right;
  }
  .search-button-self{
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
    height: 80vh;
    flex: 1;
    display: flex;
    align-items: center;
    justify-content: space-between;
    font-size: 14px;
    flex-flow: row nowrap;
    color: #646A73;
    font-weight: 400;
  }
  .outer-body{
    width: 100%;
    height: calc(100vh - 56px);
    background-color: var(--MainBG,#f5f6f7);
  }

  .market-dialog-css{
    ::v-deep .el-form-item__label {
      width: 100% !important;
      text-align: left;
    }

    ::v-deep
    .el-form-item.is-required:not(.is-no-asterisk)
    > .el-form-item__label:before {
      display: none;
    }

    ::v-deep
    .el-form-item.is-required:not(.is-no-asterisk)
    > .el-form-item__label::after {
      content: "*";
      color: #f54a45;
      margin-left: 2px;
    }

    ::v-deep .el-form-item__content {
      margin-left: 0 !important;
    }

    ::v-deep .vue-treeselect__input{
      vertical-align:middle;
    }
  }

</style>
