<script lang="ts" setup>
import topEnterpriseTrial from '@/assets/svg/top-enterprise-trial.svg'
import topHelpDoc from '@/assets/svg/top-help-doc.svg'
import topProductBbs from '@/assets/svg/top-product-bbs.svg'
import topTechnology from '@/assets/svg/top-technology.svg'
import { useI18n } from '@/hooks/web/useI18n'
import docs from '@/assets/svg/icon-maybe_outlined.svg'
import { computed } from 'vue'
import { Icon } from '@/components/icon-custom'
import TopDocCard from '@/layout/components/TopDocCard.vue'
import { useAppearanceStoreWithOut } from '@/store/modules/appearance'
const appearanceStore = useAppearanceStoreWithOut()
const navigateBg = computed(() => appearanceStore.getNavigateBg)
const help = computed(() => appearanceStore.getHelp)
const { t } = useI18n()

const cardInfoList = [
  {
    name: t('api_pagination.help_documentation'),
    url: help.value || 'https://dataease.io/docs/v2/',
    icon: topHelpDoc
  },
  {
    name: t('api_pagination.product_forum'),
    url: 'https://bbs.fit2cloud.com/c/de/6',
    icon: topProductBbs
  },
  {
    name: t('api_pagination.technical_blog'),
    url: 'https://blog.fit2cloud.com/categories/dataease',
    icon: topTechnology
  },
  {
    name: t('api_pagination.enterprise_edition_trial'),
    url: 'https://jinshuju.net/f/TK5TTd',
    icon: topEnterpriseTrial
  }
]
</script>

<template>
  <el-popover
    :show-arrow="false"
    popper-class="top-popover"
    placement="bottom-end"
    width="auto"
    trigger="hover"
  >
    <div :class="cardInfoList.length < 3 ? 'top-doc-card-wrap-small' : 'top-doc-card-wrap'">
      <top-doc-card
        :span="12"
        v-for="(item, index) in cardInfoList"
        :key="index"
        :card-info="item"
      ></top-doc-card>
    </div>
    <template #reference>
      <div
        class="sys-setting"
        :class="{ 'is-light-setting': navigateBg && navigateBg === 'light' }"
      >
        <el-icon>
          <Icon name="docs"><docs class="svg-icon" /></Icon>
        </el-icon>
      </div>
    </template>
  </el-popover>
</template>

<style lang="less" scoped>
.sys-setting {
  margin: 0 10px;
  padding: 5px;
  height: 28px;
  width: 28px;
  border-radius: 6px;
  overflow: hidden;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  &:hover {
    background-color: #1e2738;
  }
}
.is-light-setting {
  &:hover {
    background-color: #1f23291a !important;
  }
}
</style>

<style lang="less">
.top-popover {
  padding: 8px !important;
  min-width: 100px !important;
  .top-doc-card-wrap-small {
    display: flex;
    flex-wrap: wrap;
  }
  .top-doc-card-wrap {
    display: grid;
    grid-template-columns: repeat(3, minmax(96px, max-content));
  }
}
</style>
