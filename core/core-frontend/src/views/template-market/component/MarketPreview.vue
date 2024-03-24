<template>
  <el-row style="width: 100%">
    <el-row style="display: table; width: 100%">
      <el-col style="float: left" :class="state.asideActive ? 'aside-active' : 'aside-inActive'">
        <el-icon v-show="!state.asideActive" class="insert" @click="asideActiveChange(true)"
          ><DArrowRight
        /></el-icon>
        <el-row v-show="state.asideActive" style="padding: 12px 12px 0">
          <el-row style="align-items: center">
            <el-icon class="insert" @click="closePreview()"><Close /></el-icon>
            <span class="main-title">{{ t('visualization.template_preview') }}</span>
            <el-icon class="insert" @click="asideActiveChange(false)"><DArrowLeft /></el-icon>
          </el-row>
          <el-row class="margin-top16 search-area">
            <el-input
              v-model="state.searchText"
              size="small"
              prefix-icon="Search"
              class="title-name-search"
              :placeholder="t('visualization.enter_template_name_tips')"
              clearable="true"
            />
            <el-icon
              class="insert-filter filter-icon-span"
              :class="state.extFilterActive ? 'filter-icon-active' : ''"
              @click="extFilterActiveChange()"
              ><Filter
            /></el-icon>
          </el-row>
          <el-row v-show="state.extFilterActive">
            <el-select
              v-model="state.marketActiveTab"
              class="margin-top16"
              size="small"
              placeholder="请选择"
            >
              <el-option v-for="item in state.marketTabs" :key="item" :label="item" :value="item" />
            </el-select>
          </el-row>
          <el-divider />
        </el-row>

        <el-row
          v-show="state.asideActive"
          class="aside-list"
          :class="state.extFilterActive ? 'aside-list-filter-active' : ''"
        >
          <template-market-preview-item
            v-for="templateItem in state.currentMarketTemplateShowList"
            v-show="templateItem.showFlag"
            :key="templateItem.id"
            :template="templateItem"
            :base-url="state.baseUrl"
            :active="active(templateItem)"
            @previewTemplate="previewTemplate"
          />
          <el-row v-show="!state.hasResult" class="custom-position">
            <div style="text-align: center">
              <Icon name="no_result" style="margin-bottom: 16px; font-size: 75px"></Icon>
              <br />
              <span>{{ t('commons.no_result') }}</span>
            </div>
          </el-row>
        </el-row>
      </el-col>
      <el-col
        style="float: left"
        class="main-area"
        :class="state.asideActive ? 'main-area-active' : ''"
      >
        <el-row>
          <span v-if="state.curTemplate" class="template-title">{{ state.curTemplate.title }}</span>
          <div style="flex: 1; text-align: right">
            <el-button
              style="float: right"
              type="primary"
              size="small"
              @click="templateApply(state.curTemplate)"
              >{{ t('visualization.apply_this_template') }}</el-button
            >
          </div>
        </el-row>
        <el-row class="img-main">
          <img style="height: 100%" :src="state.templatePreviewUrl" alt="" />
        </el-row>
      </el-col>
    </el-row>
  </el-row>
</template>

<script setup lang="ts">
import { searchMarket, getCategories } from '@/api/templateMarket'
import { onMounted, reactive, watch } from 'vue'
import { useI18n } from '@/hooks/web/useI18n'
import TemplateMarketPreviewItem from '@/views/template-market/component/TemplateMarketPreviewItem.vue'
const { t } = useI18n()

const props = defineProps({
  previewId: {
    type: String,
    default: null
  }
})

const emits = defineEmits(['templateApply', 'closeDialog', 'closePreview'])

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
  currentMarketTemplateShowList: [],
  networkStatus: true,
  curTemplate: null
})

watch(
  () => state.marketActiveTab,
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
  value => {
    state.currentMarketTemplateShowList.forEach(template => {
      if (value === template.id) {
        previewTemplate(template)
      }
    })
  }
)

const initMarketTemplate = async () => {
  await searchMarket()
    .then(rsp => {
      state.baseUrl = rsp.data.baseUrl
      state.currentMarketTemplateShowList = rsp.data.contents
      state.hasResult = true
      if (props.previewId) {
        state.currentMarketTemplateShowList.forEach(template => {
          if (props.previewId === template.id) {
            previewTemplate(template)
          }
        })
      }
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

const templateApply = template => {
  emits('templateApply', template)
}

const initTemplateShow = () => {
  state.hasResult = false
  state.currentMarketTemplateShowList.forEach(template => {
    template.showFlag = templateShow(template)
    if (template.showFlag) {
      state.hasResult = true
    }
  })
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

const previewTemplate = template => {
  state.curTemplate = template
  if (template.thumbnail.indexOf('http') > -1) {
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
}

.aside-active {
  width: 206px;
  height: calc(100vh - 56px);
  overflow-x: hidden;
  overflow-y: auto;
  background-color: var(--ContentBG, #ffffff);
}

.aside-inActive {
  position: relative;
  width: 0px;
}

.main-area-active {
  width: calc(100% - 206px) !important;
}

.main-area {
  width: 100%;
  padding: 24px;
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
  font-family: '阿里巴巴普惠体 3.0 55 Regular L3';
  line-height: 1;
  white-space: nowrap;
  cursor: pointer;
  color: var(--TextPrimary, #1f2329);
  -webkit-appearance: none;
  text-align: center;
  box-sizing: border-box;
  outline: 0;
  margin: 0;
  transition: 0.1s;
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
  font-family: '阿里巴巴普惠体 3.0 55 Regular L3';
  line-height: 1;
  white-space: nowrap;
  cursor: pointer;
  color: #646a73;
  -webkit-appearance: none;
  text-align: center;
  box-sizing: border-box;
  outline: 0;
  margin: 0;
  transition: 0.1s;
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
  border-radius: 4px;
  background: #0f1114;
  overflow-x: auto;
  overflow-y: hidden;
  height: calc(100% - 50px) !important;
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
  border: 1px solid #dcdfe6;
  width: 32px;
  height: 32px;
  border-radius: 4px;
  padding: 7px;
  margin-left: 8px;
}

.filter-icon-active {
  border: 1px solid var(--ed-color-primary);
  color: var(--ed-color-primary);
}

.filter-icon-active {
  border: 1px solid var(--ed-color-primary);
  color: var(--ed-color-primary);
}

.search-area {
  width: 100%;
  position: relative;
}
</style>
