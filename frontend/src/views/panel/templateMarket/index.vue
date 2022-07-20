<template>
  <el-row>
    <el-row>
      <el-col span="4" style="text-align: left">
        <svg-icon
          icon-class="icon_left_outlined"
          class="topbar-icon-active"
          @click="closeDialog"
        />
      </el-col>
      <el-col v-if="networkStatus" span="18">
        <el-row>
          <el-col span="20">
            <el-input v-model="searchText" :placeholder="$t('panel.enter_template_name_tips')" clearable="true" />
          </el-col>
        </el-row>
        <el-row>
          <el-col span="20" style="margin-top: 15px">
            <el-tabs v-model="marketActiveTab" @tab-click="handleClick">
              <el-tab-pane v-for="tabItem in marketTabs" :key="tabItem" :label="tabItem" :name="tabItem" />
            </el-tabs>
          </el-col>
        </el-row>
      </el-col>
    </el-row>
    <el-row v-if="networkStatus">
      <el-row v-loading="$store.getters.loadingMap[$store.getters.currentPath]" style="text-align: center;">
        <template-market-item
          v-for="(templateItem) in currentMarketTemplateShowList"
          v-show="templateItem.showFlag"
          :key="templateItem.id"
          :template="templateItem"
          :base-url="baseUrl"
          @templateApply="templateApply"
          @templatePreview="templatePreview"
        />
        <el-dialog
          v-loading="$store.getters.loadingMap[$store.getters.currentPath]"
          :title="$t('panel.apply_template')"
          :visible.sync="folderSelectShow"
          width="500px"
          class="dialog-css"
          append-to-body="true"
          :destroy-on-close="true"
        >
          <el-form ref="panelForm" :model="panelForm" label-width="80px">
            <el-form-item :label="$t('panel.name')">
              <el-input v-model="panelForm.name" :placeholder="$t('panel.enter_name_tips')" />
            </el-form-item>
            <el-form-item :label="$t('commons.folder')">
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
        <!--预览-->
        <el-dialog top="5vh" width="80%" append-to-body="true" :visible.sync="previewVisible">
          <img width="100%" :src="templatePreviewUrl" alt="">
        </el-dialog>
      </el-row>
    </el-row>
    <el-row v-else class="custom-position">
      {{ $t('panel.market_network_tips') }}
    </el-row>

  </el-row>

</template>

<script>
import { searchMarket, getCategories } from '@/api/templateMarket'
import TemplateMarketItem from '@/views/panel/templateMarket/component/TemplateMarketItem'
import { groupTree, panelSave } from '@/api/panel/panel'
import bus from '@/utils/bus'
import { DEFAULT_COMMON_CANVAS_STYLE_STRING } from '@/views/panel/panel'

export default {
  name: 'TemplateMarket',
  components: { TemplateMarketItem },
  data() {
    return {
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
      networkStatus: true
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
        bus.$emit('newPanelFromMarket', response.data)
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
      this.currentMarketTemplateShowList.forEach(template => {
        template.showFlag = this.templateShow(template)
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
    templatePreview(url) {
      this.templatePreviewUrl = url
      this.previewVisible = true
    },
    newPanel() {

    }
  }
}
</script>

<style lang="scss" scoped>
  .dialog-footer-self{
    text-align: center;
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
    color: #9ea6b2;
  }

</style>
