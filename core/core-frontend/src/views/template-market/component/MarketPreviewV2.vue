<template>
  <el-row style="width: 100%">
    <el-row style="display: table; width: 100%">
      <el-col style="float: left" :class="state.asideActive ? 'aside-active' : 'aside-inActive'">
        <el-tooltip
          class="box-item"
          effect="dark"
          :content="t('relation.expand')"
          placement="right"
        >
          <el-icon v-show="!state.asideActive" class="insert" @click="asideActiveChange(true)">
            <Icon name="market-expand"><marketExpand class="svg-icon" /></Icon>
          </el-icon>
        </el-tooltip>
        <el-row v-show="state.asideActive" style="padding: 24px 12px 0">
          <el-row style="display: flex; align-items: center">
            <span class="custom-breadcrumb-item" @click="closePreview()">{{
              t('template_manage.template_center')
            }}</span>
            <el-icon style="color: #8f959e"><ArrowRight /></el-icon>
            <span class="custom-breadcrumb-item-to">{{ t('template_manage.preview') }}</span>

            <el-tooltip
              class="box-item"
              effect="dark"
              :content="t('relation.retract')"
              placement="right"
            >
              <div @click="asideActiveChange(false)" class="insert-retract">
                <el-icon>
                  <Icon name="icon_left_outlined"><icon_left_outlined class="svg-icon" /></Icon>
                </el-icon>
              </div>
            </el-tooltip>
          </el-row>
          <el-row class="margin-top16 search-area">
            <el-input
              v-model="state.searchText"
              prefix-icon="Search"
              class="title-name-search"
              :placeholder="t('visualization.enter_template_name_tips')"
              clearable
            />
            <el-icon
              class="insert-filter filter-icon-span"
              :class="state.extFilterActive ? 'filter-icon-active' : ''"
              @click="extFilterActiveChange()"
            >
              <iconFilter />
            </el-icon>
          </el-row>
          <el-row v-show="state.extFilterActive">
            <el-select
              v-model="state.templateType"
              style="margin-top: 8px"
              :placeholder="t('common.selectText')"
            >
              <el-option
                v-for="item in state.templateTypeOptions"
                :key="item.value"
                :label="item.label"
                :value="item.value"
              />
            </el-select>
          </el-row>
          <el-row v-show="state.extFilterActive">
            <el-select
              v-model="state.templateSourceType"
              style="margin-top: 8px"
              :placeholder="t('common.selectText')"
            >
              <el-option
                v-for="item in state.templateSourceOptions"
                :key="item.value"
                :label="item.label"
                :value="item.value"
              />
            </el-select>
          </el-row>
          <el-divider class="mp-divider" />
        </el-row>

        <el-main
          v-show="state.asideActive"
          class="aside-list"
          :class="state.extFilterActive ? 'aside-list-filter-active' : ''"
        >
          <el-collapse v-show="state.hasResult" v-model="activeCategories" class="market-collapse">
            <el-collapse-item
              themes="light"
              v-for="(categoryTemplate, index) in state.marketTemplatePreviewShowList"
              v-show="categoryTemplate['showFlag']"
              :name="categoryTemplate['category'].label"
              :key="index"
              :title="categoryTemplate['category'].label"
            >
              <template-market-preview-item
                v-for="templateItem in categoryTemplate['contents']"
                v-show="templateItem.showFlag"
                :key="templateItem.id"
                :template="templateItem"
                :base-url="state.baseUrl"
                :active="active(templateItem)"
                @previewTemplate="previewTemplate"
              />
            </el-collapse-item>
          </el-collapse>
          <el-row v-show="!state.hasResult" class="custom-position">
            <div style="text-align: center">
              <Icon name="no_result">
                <no_result style="margin-bottom: 16px; font-size: 75px" class="svg-icon" />
              </Icon>
              <br />
              <span>{{ t('work_branch.relevant_templates_found') }}</span>
            </div>
          </el-row>
        </el-main>
      </el-col>
      <el-col
        style="float: left"
        class="main-area"
        :class="state.asideActive ? 'main-area-active' : ''"
      >
        <el-row v-if="state.curTemplate" style="padding: 24px 24px 0">
          <span class="template-title">{{ state.curTemplate.title }}</span>
          <div style="flex: 1; text-align: right">
            <el-button
              style="float: right"
              type="primary"
              :disabled="!createAuth[state.curTemplate?.templateType]"
              @click="templateApply(state.curTemplate)"
            >
              {{ t('visualization.apply_this_template') }}
            </el-button>
          </div>
        </el-row>
        <el-row v-if="state.curTemplate" class="img-main">
          <img :src="imgUrlTrans(state.templatePreviewUrl)" alt="" />
        </el-row>
      </el-col>
    </el-row>
  </el-row>
</template>

<script setup lang="ts">
import marketExpand from '@/assets/svg/market-expand.svg'
import icon_left_outlined from '@/assets/svg/icon_left_outlined.svg'
import iconFilter from '@/assets/svg/icon-filter.svg'
import no_result from '@/assets/svg/no_result.svg'
import { searchMarketPreview } from '@/api/templateMarket'
import { onMounted, reactive, watch, ref } from 'vue'
import { useI18n } from '@/hooks/web/useI18n'
import TemplateMarketPreviewItem from '@/views/template-market/component/TemplateMarketPreviewItem.vue'
import { deepCopy, getActiveCategories } from '@/utils/utils'
import { imgUrlTrans } from '@/utils/imgUtils'

const { t } = useI18n()

const props = defineProps({
  previewId: {
    type: String,
    default: null
  },
  templateShowList: {
    type: Array,
    default: () => []
  },
  createAuth: {
    type: Object,
    default() {
      return {
        PANEL: false,
        SCREEN: false
      }
    }
  }
})

const emits = defineEmits(['templateApply', 'closeDialog', 'closePreview'])
const activeCategories = ref([])

const state = reactive({
  hasResult: true,
  extFilterActive: false,
  asideActive: true,
  previewVisible: false,
  templatePreviewUrl: null,
  marketTabs: null,
  marketActiveTab: null,
  searchText: null,
  panelGroupList: [],
  curApplyTemplate: null,
  folderSelectShow: false,
  baseUrl: 'https://dataease.io/templates',
  marketTemplatePreviewShowList: [],
  categories: [],
  networkStatus: true,
  curTemplate: null,
  templateSourceType: 'all',
  templateSourceOptions: [
    {
      value: 'all',
      label: t('work_branch.all_source')
    },
    {
      value: 'market',
      label: t('work_branch.template_market_official')
    },
    {
      value: 'manage',
      label: t('template_manage.name')
    }
  ],
  templateType: 'all',
  templateTypeOptions: [
    {
      value: 'all',
      label: t('work_branch.all_types')
    },
    {
      value: 'PANEL',
      label: t('work_branch.dashboard')
    },
    {
      value: 'SCREEN',
      label: t('work_branch.big_screen')
    }
  ]
})

watch(
  () => state.templateType,
  () => {
    initTemplateShow()
  }
)

watch(
  () => state.searchText,
  () => {
    initTemplateShow()
  }
)

watch(
  () => props.previewId,
  () => {
    state.marketTemplatePreviewShowList.forEach(categoryTemplates => {
      categoryTemplates.contents.forEach(template => {
        if (props.previewId === template.id) {
          previewTemplate(template)
        }
      })
    })
  }
)

watch(
  () => state.templateSourceType,
  () => {
    initTemplateShow()
  }
)

const initMarketTemplate = () => {
  searchMarketPreview()
    .then(rsp => {
      state.baseUrl = rsp.data.baseUrl
      state.marketTemplatePreviewShowList = rsp.data.contents
      state.hasResult = true
      state.categories = rsp.data.categories
      initTemplateShow()
      const activeCategoriesShow = getActiveCategories(state.currentMarketTemplateShowList)
      state.categories = rsp.data.categories.filter(category =>
        activeCategoriesShow.has(category.label)
      )
      activeCategories.value = deepCopy(state.categories)
      if (props.previewId) {
        state.marketTemplatePreviewShowList.forEach(categoryTemplates => {
          categoryTemplates.contents.forEach(template => {
            if (props.previewId === template.id) {
              previewTemplate(template)
            }
          })
        })
      }
    })
    .catch(() => {
      state.networkStatus = false
    })
}

const templateApply = template => {
  emits('templateApply', template)
}

const initTemplateShow = () => {
  state.hasResult = false
  state.marketTemplatePreviewShowList.forEach(categoryTemplates => {
    categoryTemplates.showFlag = categoryShow(categoryTemplates.category.source)
    categoryTemplates.contents.forEach(template => {
      template.showFlag = templateShow(template)
      if (template.showFlag) {
        state.hasResult = true
      }
    })
  })
  activeCategories.value = deepCopy(state.categories)
}

const categoryShow = sourceMatch => {
  return (
    state.templateSourceType === 'all' ||
    sourceMatch === state.templateSourceType ||
    sourceMatch === 'public'
  )
}

const templateShow = templateItem => {
  let templateTypeMarch = false
  let searchMarch = false
  let templateSourceTypeMarch = false
  if (state.templateType === 'all' || templateItem.templateType === state.templateType) {
    templateTypeMarch = true
  }
  if (!state.searchText || templateItem.title.indexOf(state.searchText) > -1) {
    searchMarch = true
  }
  if (state.templateSourceType === 'all' || templateItem.source === state.templateSourceType) {
    templateSourceTypeMarch = true
  }
  return templateTypeMarch && searchMarch && templateSourceTypeMarch
}

const previewTemplate = template => {
  state.curTemplate = template
  if (
    template.thumbnail.indexOf('http') > -1 ||
    template.thumbnail.indexOf('static-resource') > -1
  ) {
    state.templatePreviewUrl = template.thumbnail
  } else {
    state.templatePreviewUrl = state.baseUrl + template.thumbnail
  }
}

const asideActiveChange = prop => {
  state.asideActive = prop
}

const extFilterActiveChange = () => {
  state.extFilterActive = !state.extFilterActive
  state.marketActiveTab = state.marketTabs[0]
}
const closePreview = () => {
  emits('closePreview')
}

const active = template => {
  return state.curTemplate && state.curTemplate.id === template.id
}

onMounted(() => {
  initMarketTemplate()
})
</script>

<style lang="less" scoped>
.market-collapse {
  width: 100%;
  border: 0;
  :deep(.ed-collapse-item__content) {
    padding: 8px 0 !important;
    border: unset !important;
  }
  :deep(.ed-collapse-item__header) {
    border: unset !important;
  }
  :deep(.ed-collapse-item__wrap) {
    border: unset !important;
    background-color: rgba(245, 246, 247, 1) !important;
  }
}
.aside-list {
  padding: 0 12px 12px 12px;
  width: 100%;
  height: calc(100vh - 200px);
  overflow-x: hidden;
  //overflow-y: auto;
  :deep(.ed-collapse) {
    --ed-collapse-header-font-size: 14px !important;
  }
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
  padding: 24px;
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
  height: 70vh;
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  flex-flow: row nowrap;
  color: #646a73;
  font-weight: 400;
  width: 100%;
}

.aside-active {
  width: 224px;
  height: calc(100vh - 56px);
  background-color: rgba(245, 246, 247, 1);
}

.aside-inActive {
  position: relative;
  width: 0px;
}

.main-area-active {
  width: calc(100% - 224px) !important;
  background: #ffffff;
}

.main-area {
  width: 100%;
  //padding: 24px;
  text-align: center;
  height: calc(100vh - 56px);
  transition: 0.5s;
}

.title-name-search {
  flex: 1;
  float: left;
}

.icon20 {
  font-size: 20px !important;
}

.main-title {
  width: 135px;
  margin-left: 8px;
  font-weight: 500;
  font-size: 16px;
  line-height: 24px;
  color: var(--TextPrimary, #1f2329);
}

.insert-filter {
  display: inline-block;
  font-weight: 400 !important;
  font-family: var(--de-custom_font, 'PingFang');
  line-height: 1;
  white-space: nowrap;
  cursor: pointer;
  color: var(--TextPrimary, #1f2329);
  background: rgba(255, 255, 255, 1);
  -webkit-appearance: none;
  text-align: center;
  box-sizing: border-box;
  outline: 0;
  margin: 0;
  transition: 0.1s;
  border-radius: 3px;

  &:hover {
    background-color: rgba(245, 246, 247, 1);
    border-color: rgba(187, 191, 196, 1);
  }

  &:active {
    background-color: rgba(239, 240, 241, 1);
    border-color: rgba(187, 191, 196, 1);
  }
}

.arrow-side-tree {
  position: absolute;
  border: 1px solid #dee0e3;
  background: #fff;
  cursor: pointer;
  z-index: 10;
  &:hover {
    .ed-icon {
      color: var(--ed-color-primary);
    }
  }
  .ed-icon {
    font-size: 12px;
  }
}

.insert-retract {
  position: absolute;
  left: 199px;
  top: 2px;
  border: 1px solid #dee0e3;
  background: #fff;
  cursor: pointer;
  height: 24px;
  width: 24px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0px 5px 10px 0px #1f23291a;

  &:hover {
    .ed-icon {
      color: var(--ed-color-primary);
    }
  }
  .ed-icon {
    font-size: 12px;
  }
}

.insert {
  font-size: 34px;
  margin-top: 24px;
  margin-left: -8px;
  display: inline-block;
  font-weight: 400 !important;
  cursor: pointer;
  color: #646a73;
  transition: 0.1s;
  z-index: 9999;
  &:active {
    color: #000;
    border-color: #3a8ee6;
    outline: 0;
  }

  &:hover {
    margin-left: -6px;
    color: #3a8ee6;
  }
}

.template-title {
  float: left;
  font-weight: 500;
  font-size: 20px;
  line-height: 28px;
  margin-bottom: 24px;
  color: var(--TextPrimary, #1f2329);
}

.margin-top16 {
  margin-top: 16px;
}
.img-main {
  display: inherit;
  //border-radius: 4px;
  background: #0f1114;
  overflow-x: auto;
  overflow-y: hidden;
  width: 100%;
  height: calc(100% - 76px) !important;
}

.img-main img {
  width: 100%;
  height: 100%;
  object-fit: contain; /* 保持图片比例，不裁剪 */
}

.open-button {
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
.open-button:hover {
  color: #3a8ee6;
}
.filter-icon-span {
  float: left;
  border: 1px solid #bbbfc4;
  width: 32px;
  height: 32px;
  border-radius: 4px;
  padding: 7px;
  margin-left: 8px;
}

.filter-icon-active {
  border: 1px solid var(--ed-color-primary) !important;
  color: var(--ed-color-primary);
  &:hover {
    background-color: rgba(225, 234, 255, 1);
  }
}

.search-area {
  width: 100%;
  position: relative;
  display: flex;
}

.custom-breadcrumb-item {
  font-size: 14px;
  cursor: pointer;
  font-weight: 400;
  color: #646a73;
  height: 22px;
  line-height: 22px;
  display: flex;
  align-items: center;
  white-space: nowrap;
  position: relative;
  margin-right: 4px;

  &::after {
    content: '';
    position: absolute;
    top: 50%;
    left: 50%;
    width: calc(100% + 8px);
    height: 100%;
    transform: translate(-50%, -50%);
    display: none;
    border-radius: 4px;
  }

  &:hover {
    color: #3370ff;
    &::after {
      background: #3370ff1a;
      display: block;
    }
  }

  &:active {
    color: #245bdb;
    &::after {
      background: #3370ff33;
      display: block;
    }
  }
}

.custom-breadcrumb-item-to {
  font-size: 14px;
  font-weight: 400;
  color: #1f2329;
  cursor: default;
  margin-left: 4px;
}
.mp-divider {
  border-color: rgba(31, 35, 41, 0.15);
  margin-top: 16px;
  margin-bottom: 8px;
}
</style>
