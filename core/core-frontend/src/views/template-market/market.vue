<template>
  <el-row class="outer-body">
    <!--预览模式-->
    <market-preview
      v-show="state.previewModel"
      :preview-id="state.templatePreviewId"
      @closePreview="state.previewModel = false"
      @templateApply="templateApply"
    />
    <!--列表模式-->
    <el-row v-show="!state.previewModel" class="market-main">
      <el-row>
        <el-col :span="12">
          <span class="title-left">{{ t('visualization.template_market') }}</span>
        </el-col>
        <el-col :span="12">
          <el-input
            v-model="state.searchText"
            prefix-icon="el-icon-search"
            size="small"
            class="title-right"
            :placeholder="t('visualization.enter_template_name_tips')"
            :clearable="true"
          />
        </el-col>
      </el-row>
      <el-row style="display: inherit">
        <el-tabs v-model="state.marketActiveTab" @tab-click="handleClick">
          <el-tab-pane
            v-for="tabItem in state.marketTabs"
            :key="tabItem"
            :label="tabItem"
            :name="tabItem"
          />
        </el-tabs>
      </el-row>
      <el-row
        v-show="state.networkStatus && state.hasResult"
        id="template-main"
        class="template-main"
      >
        <el-col
          v-for="templateItem in state.currentMarketTemplateShowList"
          v-show="templateItem.showFlag"
          :key="templateItem.id"
          style="padding: 24px 12px 0; text-align: center"
          :style="{ width: state.templateSpan }"
        >
          <template-market-item
            :template="templateItem"
            :base-url="state.baseUrl"
            :width="state.templateCurWidth"
            @templateApply="templateApply"
            @templatePreview="templatePreview"
          />
        </el-col>
      </el-row>
      <el-row
        v-show="state.networkStatus && !state.hasResult"
        class="custom-position template-main"
      >
        <div style="text-align: center">
          <svg-icon icon-class="no_result" style="margin-bottom: 16px; font-size: 75px" />
          <br />
          <span>{{ t('commons.no_result') }}</span>
        </div>
      </el-row>
      <el-row v-show="!state.networkStatus" class="custom-position template-main">
        {{ t('visualization.market_network_tips') }}
      </el-row>
    </el-row>
    <el-dialog
      :title="t('visualization.apply_template')"
      v-model="state.folderSelectShow"
      width="600px"
      class="market-dialog-css"
      :append-to-body="true"
      :destroy-on-close="true"
    >
      <el-form ref="panelForm" :model="state.panelForm" :rules="rule" label-width="80px">
        <el-form-item :label="t('visualization.name')" prop="name">
          <el-input
            v-model="state.panelForm.name"
            :clearable="true"
            :placeholder="t('visualization.enter_name_tips')"
          />
        </el-form-item>
        <el-form-item :label="t('commons.folder')" prop="pid">
          <treeselect
            v-model="state.panelForm.pid"
            :clearable="false"
            :options="state.panelGroupList"
            :normalizer="normalizer"
            :placeholder="t('chart.select_group')"
            :no-children-text="t('commons.treeselect.no_children_text')"
            :no-options-text="t('commons.treeselect.no_options_text')"
            :no-results-text="t('commons.treeselect.no_results_text')"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer dialog-footer-self">
          <el-button size="mini" @click="state.folderSelectShow = false"
            >{{ t('commons.cancel') }}
          </el-button>
          <el-button
            size="mini"
            type="primary"
            :disabled="!state.panelForm.name || !state.panelForm.pid"
            @click="apply"
            >{{ t('commons.confirm') }}
          </el-button>
        </div>
      </template>
    </el-dialog>
  </el-row>
</template>

<script setup lang="ts">
import { getCategories, searchMarket } from '@/api/templateMarket'
import elementResizeDetectorMaker from 'element-resize-detector'
import { nextTick, reactive, watch, onMounted } from 'vue'
import { useI18n } from '@/hooks/web/useI18n'
import { ElMessage } from 'element-plus-secondary'
import MarketPreview from '@/views/template-market/component/MarketPreview.vue'
import TemplateMarketItem from '@/views/template-market/component/TemplateMarketItem.vue'
const { t } = useI18n()

const emits = defineEmits(['closeDialog'])

const state = reactive({
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
    panelStyle: {},
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
        message: t('visualization.template_name_tips'),
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
})

watch(
  () => state.marketActiveTab,
  value => {
    initTemplateShow()
  }
)

watch(
  () => state.searchText,
  value => {
    initTemplateShow()
  }
)

const initMarketTemplate = () => {
  searchMarket({})
    .then(rsp => {
      state.baseUrl = rsp.data.baseUrl
      state.currentMarketTemplateShowList = rsp.data.contents
    })
    .catch(() => {
      state.networkStatus = false
    })
  getCategories()
    .then(rsp => {
      state.marketTabs = rsp.data
      state.marketActiveTab = state.marketTabs[0]
    })
    .catch(() => {
      state.networkStatus = false
    })
}

const getGroupTree = () => {
  // do getGroupTree
  // groupTree({ nodeType: 'folder' }).then(res => {
  //   state.panelGroupList = res.data
  // })
}
const normalizer = node => {
  // 去掉children=null的属性
  if (node.children === null || node.children === 'null') {
    delete node.children
  }
}

const templateApply = template => {
  state.curApplyTemplate = template
  state.panelForm.name = template.title
  state.panelForm.templateUrl = state.baseUrl + template.metas.theme_repo
  state.folderSelectShow = true
}

const apply = () => {
  if (state.panelForm.name.length > 50) {
    ElMessage.warning(t('commons.char_can_not_more_50'))
    return false
  }

  if (!state.panelForm.templateUrl) {
    ElMessage.warning('未获取模板下载链接请联系模板市场官方')
    return false
  }
  // panelSave(state.panelForm)
  //   .then(response => {
  //     state.$message({
  //       message: state.t('commons.save_success'),
  //       type: 'success',
  //       showClose: true
  //     })
  //     state.folderSelectShow = false
  //     state.$router.push({ name: 'panel', params: response.data })
  //   })
  //   .catch(() => {
  //     state.loading = false
  //   })
}
const closeDialog = () => {
  emits('closeDialog')
}
const handleClick = item => {
  // do handleClick
}
const initTemplateShow = () => {
  let tempHasResult = false
  state.currentMarketTemplateShowList.forEach(template => {
    template.showFlag = templateShow(template)
    if (template.showFlag) {
      tempHasResult = true
    }
  })
  if (state.currentMarketTemplateShowList.length > 0) {
    state.hasResult = tempHasResult
  }
}

const templateShow = templateItem => {
  let categoryMarch = false
  let searchMarch = false
  templateItem.categories.forEach(category => {
    if (category.name === state.marketActiveTab) {
      categoryMarch = true
    }
  })
  if (!state.searchText || templateItem.title.indexOf(state.searchText) > -1) {
    searchMarch = true
  }
  return categoryMarch && searchMarch
}

const templatePreview = previewId => {
  state.templatePreviewId = previewId
  state.previewModel = true
}
const newPanel = () => {
  // do newPanel
}

onMounted(() => {
  initMarketTemplate()
  getGroupTree()
  const erd = elementResizeDetectorMaker()
  const templateMainDom = document.getElementById('template-main')
  // 监听div变动事件
  if (templateMainDom) {
    erd.listenTo(templateMainDom, element => {
      nextTick(() => {
        const curSeparator = Math.trunc(templateMainDom.offsetWidth / state.templateMiniWidth)
        state.templateSpan =
          100 / Math.trunc(templateMainDom.offsetWidth / state.templateMiniWidth) + '%'
        state.templateCurWidth = Math.trunc(templateMainDom.offsetWidth / curSeparator) - 33
      })
    })
  }
})
</script>

<style lang="less" scoped>
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
  padding: 24px;
  display: inherit;
}

.title-left {
  float: left;
  font-size: 20px;
  font-weight: 500;
  line-height: 28px;
  color: var(--TextPrimary, #1f2329);
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
  transition: 0.1s;
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
  color: #646a73;
  font-weight: 400;
}

.outer-body {
  display: inherit;
  width: 100%;
  height: calc(100vh - 56px);
  background-color: var(--MainBG, #f5f6f7);
}

.market-dialog-css {
  ::v-deep(.ed-form-item__label) {
    width: 100% !important;
    text-align: left;
  }

  ::v-deep(.ed-form-item.is-required:not(.is-no-asterisk) > .ed-form-item__label:before) {
    display: none;
  }

  ::v-deep(.ed-form-item.is-required:not(.is-no-asterisk) > .ed-form-item__label::after) {
    content: '*';
    color: #f54a45;
    margin-left: 2px;
  }

  ::v-deep(.ed-form-item__content) {
    margin-left: 0 !important;
  }

  ::v-deep(.vue-treeselect__input) {
    vertical-align: middle;
  }
}
</style>
