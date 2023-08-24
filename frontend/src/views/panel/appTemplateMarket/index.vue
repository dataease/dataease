<template>
  <el-row class="outer-body">
    <!--预览模式-->
    <AppMarketPreview
      v-if="previewModel"
      :template-info="previewItem"
      @closePreview="previewModel=false"
      @appApply="appApply"
    />
    <!--列表模式-->
    <el-row
      v-show="!previewModel"
      class="market-main"
    >
      <el-row>
        <el-col :span="12">
          <span class="title-left">{{ $t('app_template.app_manager') }}</span>
        </el-col>
      </el-row>
      <el-row>
        <el-tabs
          v-model="marketActiveTab"
          @tab-click="handleClick"
        >
          <el-tab-pane
            v-for="(tabItem,index) in marketTabs"
            :key="index"
            :label="tabItem.label"
            :name="tabItem.name"
          />
        </el-tabs>
      </el-row>
      <el-row v-show="marketActiveTab==='apps'">
        <app-template-content
          :ref="'appTemplateContent'"
          class="template-main-content"
          :show-position="'market-manage'"
          @previewApp="previewApp"
          @applyNew="applyNew"
        />
      </el-row>
      <el-row
        v-if="marketActiveTab==='apply_logs'"
        class="main-log-area template-main"
      >
        <app-template-log class="log-area" />
      </el-row>
    </el-row>

    <el-dialog
      v-loading="$store.getters.loadingMap[$store.getters.currentPath]"
      :title="$t('panel.apply_template')"
      :visible.sync="folderSelectShow"
      width="80%"
      top="5vh"
      class="market-dialog-css"
      :append-to-body="true"
      :destroy-on-close="true"
    >
      <el-form
        ref="panelForm"
        :model="panelForm"
        :rules="rule"
        label-width="80px"
      >
        <el-form-item
          :label="$t('panel.name')"
          prop="name"
        >
          <el-input
            v-model="panelForm.name"
            :clearable="true"
            :placeholder="$t('panel.enter_name_tips')"
          />
        </el-form-item>
        <el-form-item
          :label="$t('commons.folder')"
          prop="pid"
        >
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
      <div
        slot="footer"
        class="dialog-footer dialog-footer-self"
      >
        <el-button
          size="mini"
          @click="folderSelectShow=false"
        >{{ $t('commons.cancel') }}
        </el-button>
        <el-button
          size="mini"
          type="primary"
          :disabled="!panelForm.name || !panelForm.pid"
          @click="apply"
        >{{ $t('commons.confirm') }}
        </el-button>
      </div>
    </el-dialog>

    <keep-alive>
      <app-template-apply
        ref="templateApply"
      />
    </keep-alive>
  </el-row>
</template>

<script>
import { searchAppTemplate } from '@/api/appTemplateMarket'
import { groupTree, panelSave } from '@/api/panel/panel'
import { DEFAULT_COMMON_CANVAS_STYLE_STRING } from '@/views/panel/panel'
import elementResizeDetectorMaker from 'element-resize-detector'
import AppTemplateLog from '@/views/panel/appTemplateMarket/log'
import AppTemplateContent from '@/views/panel/appTemplate/AppTemplateContent'
import AppMarketPreview from '@/views/panel/appTemplateMarket/component/AppMarketPreview'
import AppTemplateApply from '@/views/panel/appTemplate/component/AppTemplateApply'

export default {
  name: 'AppTemplateMarket',
  components: { AppTemplateApply, AppMarketPreview, AppTemplateContent, AppTemplateLog },
  data() {
    return {
      previewItem: null,
      hasResult: true,
      templateMiniWidth: 330,
      templateCurWidth: 310,
      templateSpan: '25%',
      previewModel: false,
      previewVisible: false,
      templatePreviewId: '',
      currentApp: null,
      marketTabs: [{ label: 'Apps', name: 'apps' }, { label: this.$t('app_template.apply_logs'), name: 'apply_logs' }],
      marketActiveTab: 'apps',
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
      currentAppShowList: [],
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
  computed: {},
  watch: {},
  mounted() {
    this.initMarketTemplate()
    this.getGroupTree()
    const _this = this
    const erd = elementResizeDetectorMaker()
    const templateMainDom = document.getElementById('template-main')
    // 监听div变动事件
    if (templateMainDom) {
      erd.listenTo(templateMainDom, element => {
        _this.$nextTick(() => {
          const curSeparator = Math.trunc(templateMainDom.offsetWidth / _this.templateMiniWidth)
          _this.templateSpan = (100 / Math.trunc(templateMainDom.offsetWidth / _this.templateMiniWidth)) + '%'
          _this.templateCurWidth = Math.trunc(templateMainDom.offsetWidth / curSeparator) - 33
        })
      })
    }
  },
  methods: {
    applyNew(item) {
      const datasourceInfo = JSON.parse(item.datasourceInfo)[0]
      const param = {
        datasourceType: datasourceInfo.type,
        appTemplateId: item.id,
        appTemplateName: item.name,
        panelName: item.name,
        datasetGroupName: item.name
      }
      this.$refs.templateApply.init(param)
    },
    appApply() {
      this.applyNew(this.previewItem)
    },
    previewApp(item) {
      this.previewModel = true
      this.previewItem = item
    },
    initMarketTemplate() {
      searchAppTemplate({ nodeType: 'folder' }).then(rsp => {
        this.currentAppShowList = rsp.data
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
    newPanel() {

    },
    appPreview(appTemplate) {
      this.templatePreviewId = appTemplate.id
      this.currentApp = appTemplate
      this.previewModel = true
    }
  }
}
</script>

<style lang="scss" scoped>
.template-main-content {
  height: calc(100vh - 190px) !important;
}

.template-main {
  text-align: center;
  border-radius: 4px;
  padding: 0 12px 24px 12px;
  height: calc(100vh - 190px) !important;
  overflow-x: hidden;
  overflow-y: auto;
  background-color: var(--ContentBG, #ffffff);
}

.market-main {
  padding: 24px
}

.title-left {
  float: left;
  font-size: 20px;
  font-weight: 500;
  line-height: 28px;
  color: var(--TextPrimary, #1F2329);
}

.title-right {
  float: right;
  width: 320px;
}

.dialog-footer-self {
  text-align: right;
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

.outer-body {
  width: 100%;
  height: calc(100vh - 56px);
  background-color: var(--MainBG, #f5f6f7);
}

.market-dialog-css {
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

  ::v-deep .vue-treeselect__input {
    vertical-align: middle;
  }
}

.main-log-area {
  position: relative;
  padding: 24px;
}

.log-area {
  height: calc(100vh - 240px);
}

</style>
