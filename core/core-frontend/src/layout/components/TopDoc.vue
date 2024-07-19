<script lang="ts" setup>
import { computed } from 'vue'
import { Icon } from '@/components/icon-custom'
import TopDocCard from '@/layout/components/TopDocCard.vue'
import { useAppearanceStoreWithOut } from '@/store/modules/appearance'
const appearanceStore = useAppearanceStoreWithOut()
const navigateBg = computed(() => appearanceStore.getNavigateBg)
const help = computed(() => appearanceStore.getHelp)

const cardInfoList = [
  { name: '帮助文档', url: help.value || 'https://dataease.io/docs/v2/', icon: 'top-help-doc' },
  { name: '产品论坛', url: 'https://bbs.fit2cloud.com/c/de/6', icon: 'top-product-bbs' },
  {
    name: '技术博客',
    url: 'https://blog.fit2cloud.com/categories/dataease',
    icon: 'top-technology'
  },
  { name: '企业版试用', url: 'https://jinshuju.net/f/TK5TTd', icon: 'top-enterprise-trial' }
]
</script>

<template>
  <el-popover
    :show-arrow="false"
    popper-class="top-popover"
    placement="bottom-end"
    width="208"
    trigger="hover"
  >
    <el-row>
      <top-doc-card
        :span="12"
        v-for="(item, index) in cardInfoList"
        :key="index"
        :card-info="item"
      ></top-doc-card>
    </el-row>
    <template #reference>
      <div
        class="sys-setting"
        :class="{ 'is-light-setting': navigateBg && navigateBg === 'light' }"
      >
        <el-icon>
          <Icon name="docs" />
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
  border-radius: 4px;
  overflow: hidden;
  cursor: pointer;
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
  padding: 0 0 16px 0 !important;
}
</style>
