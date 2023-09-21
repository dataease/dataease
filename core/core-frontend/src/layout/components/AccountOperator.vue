<script lang="ts" setup>
import { computed, ref, unref } from 'vue'
import { Icon } from '@/components/icon-custom'
import { useUserStoreWithOut } from '@/store/modules/user'
import { logoutApi } from '@/api/login'
import { logoutHandler } from '@/utils/logout'
import { XpackComponent } from '@/components/plugin'
import { useI18n } from '@/hooks/web/useI18n'
import { useEmitt } from '@/hooks/web/useEmitt'
import AboutPage from '@/views/about/index.vue'
import LangSelector from './LangSelector.vue'
import router from '@/router'
const userStore = useUserStoreWithOut()
const { t } = useI18n()

interface LinkItem {
  id: number
  label: string
  link?: string
  method?: string
}
const linkList = ref([{ id: 5, label: t('common.about'), method: 'toAbout' }] as LinkItem[])

const logout = async () => {
  await logoutApi()
  logoutHandler()
}

const linkLoaded = items => {
  items.forEach(item => linkList.value.push(item))
  linkList.value.sort(compare('id'))
}

const compare = (property: string) => {
  return (a, b) => a[property] - b[property]
}

const toAbout = () => {
  useEmitt().emitter.emit('open-about-dialog')
}

const executeMethod = (item: LinkItem) => {
  if (item?.method) {
    toAbout()
  }

  if (item.link) {
    router.push(item.link)
  }
}

const name = computed(() => userStore.getName)
const uid = computed(() => userStore.getUid)

const buttonRef = ref()
const popoverRef = ref()

const divLanguageRef = ref()
const popoverLanguageRef = ref()

const openLanguage = () => {
  unref(popoverLanguageRef).popperRef?.delayHide?.()
}

const openPopover = () => {
  unref(popoverRef).popperRef?.delayHide?.()
}
</script>

<template>
  <div class="top-info-container" ref="buttonRef" v-click-outside="openPopover">
    <el-icon class="main-color">
      <Icon name="user-img" />
    </el-icon>
    <span class="uname-span">{{ name }}</span>
    <el-icon class="el-icon-animate">
      <Icon name="icon_expand-down_filled" />
    </el-icon>
  </div>
  <el-popover
    ref="popoverRef"
    :virtual-ref="buttonRef"
    trigger="click"
    title=""
    virtual-triggering
    placement="bottom-start"
    popper-class="uinfo-popover"
    width="224"
  >
    <div class="uinfo-container">
      <div class="uinfo-header de-container">
        <span class="uinfo-name">{{ name }}</span>
        <span class="uinfo-id">{{ uid }}</span>
      </div>
      <el-divider />
      <div class="uinfo-main">
        <div
          class="uinfo-main-item de-container"
          v-for="link in linkList"
          :key="link.id"
          @click="executeMethod(link)"
        >
          <span>{{ link.label }}</span>
        </div>

        <div class="uinfo-main-item de-container">
          <div class="about-parent" ref="divLanguageRef" v-click-outside="openLanguage">
            <span>语言</span>
            <el-icon class="el-icon-animate">
              <ArrowRight />
            </el-icon>
          </div>
          <el-popover
            ref="popoverLanguageRef"
            :virtual-ref="divLanguageRef"
            trigger="click"
            title=""
            virtual-triggering
            placement="left"
            width="224"
            popper-class="language-popover"
          >
            <LangSelector />
          </el-popover>
        </div>
      </div>
      <el-divider />
      <div class="uinfo-footer">
        <div class="uinfo-main-item de-container" @click="logout">
          <span>{{ t('common.exit_system') }}</span>
        </div>
      </div>
    </div>
  </el-popover>

  <AboutPage />
  <XpackComponent jsname="dWNlbnRlci1oYW5kbGVy" @loaded="linkLoaded" />
  <XpackComponent
    v-if="uid === '1'"
    jsname="c2V0dGluZy9zZXR0aW5nLWhhbmRsZXI="
    @loaded="linkLoaded"
  />
</template>

<style lang="less" scoped>
.el-icon-animate {
  width: 12px;
  height: 12px;
  font-size: 14px !important;
}
.top-info-container {
  height: 32px;
  display: flex;
  align-items: center;
  border-radius: 4px;
  overflow: hidden;
  cursor: pointer;
  &:hover {
    background-color: #1e2738;
  }
  .main-color {
    background: #3370ff;
    width: 24px;
    height: 24px;
    border-radius: 50%;
  }
  .uname-span {
    font-size: 14px;
    color: rgba(255, 255, 255, 0.8);
  }
  .ed-icon {
    margin: 0 5px;
  }
}
.uinfo-container {
  width: 100%;
  height: 100%;
  .de-container {
    padding: 0 13px 10px;
  }
  .ed-divider--horizontal {
    margin: 0 0 !important;
    color: #1f2329;
    opacity: 0.35;
  }
  .uinfo-header {
    span {
      display: block;
    }
    .uinfo-name {
      font-size: 14px;
      font-weight: 500;
      color: #1f2329;
    }
    .uinfo-id {
      font-size: 14px;
      font-weight: 400;
      color: #646a73;
      margin-top: 5px;
    }
  }
  .uinfo-main,
  .uinfo-footer {
    width: 100%;
    .uinfo-main-item {
      width: 100%;
      height: 40px;
      line-height: 40px;
      cursor: pointer;
      &:hover {
        background-color: #f2f2f2;
        color: var(--ed-color-primary);
      }
      .about-parent {
        display: flex;
        align-items: center;
        justify-content: space-between;
      }
    }
  }
}
.uinfo-popover {
  max-height: 372px;
  .ed-popper__arrow {
    display: none;
  }
  .ed-popover__title {
    display: none;
  }
  padding-left: 0 !important;
  padding-right: 0 !important;
  padding-bottom: 0 !important;
}
.language-popover {
  max-height: 112px;
  .ed-popper__arrow {
    display: none;
  }
  padding: var(--ed-popover-padding) 0 !important;
}
</style>
