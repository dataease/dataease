<script lang="ts" setup>
import logo from '@/assets/svg/logo.svg'
import icon_left_outlined from '@/assets/svg/icon_left_outlined.svg'
import { computed } from 'vue'
import { ElHeader } from 'element-plus-secondary'
import { useRouter } from 'vue-router'
import AccountOperator from '@/layout/components/AccountOperator.vue'
import { useAppearanceStoreWithOut } from '@/store/modules/appearance'
import { useI18n } from '@/hooks/web/useI18n'
import { isDesktop } from '@/utils/ModelUtil'
const appearanceStore = useAppearanceStoreWithOut()
const { push } = useRouter()
const { t } = useI18n()
const desktop = isDesktop()
withDefaults(
  defineProps<{
    title: string
  }>(),
  {}
)
const backToMain = () => {
  push('/workbranch/index')
}
const navigateBg = computed(() => appearanceStore.getNavigateBg)
const navigate = computed(() => appearanceStore.getNavigate)
</script>

<template>
  <el-header
    class="header-flex system-header"
    :class="{ 'header-light': navigateBg && navigateBg === 'light' }"
  >
    <img class="logo" v-if="navigate" :src="navigate" alt="" />
    <Icon class="de-logo" v-else className="logo" name="logo">
      <logo class="svg-icon logo de-logo" />
    </Icon>
    <el-divider direction="vertical" />
    <span class="system">{{ title || t('commons.system_setting') }}</span>
    <div class="operate-setting">
      <span @click="backToMain" class="work-bar flex-align-center">
        <el-icon>
          <Icon name="icon_left_outlined"><icon_left_outlined class="svg-icon" /></Icon>
        </el-icon>
        <span class="work">{{ t('work_branch.back_to_work_branch') }}</span>
      </span>

      <AccountOperator v-if="!desktop" />
    </div>
  </el-header>
</template>

<style lang="less" scoped>
.system-header {
  font-family: var(--de-custom_font, 'PingFang');

  .logo {
    width: 134px;
    height: 34px;
  }

  .de-logo {
    color: #ffffff;
  }

  .ed-divider {
    margin: 0 24px;
    border-color: rgba(255, 255, 255, 0.3);
  }
  .system {
    color: #fff;
    font-size: 16px;
    font-style: normal;
    font-weight: 500;
    line-height: 24px;
  }

  .work-bar {
    margin-right: 20px;
    color: rgba(255, 255, 255, 0.8);
    font-size: 14px;
    font-style: normal;
    font-weight: 400;
    line-height: 22px;
    cursor: pointer;
    .ed-icon {
      margin-right: 4px;
      font-size: 16px;
    }
  }

  .avatar {
    margin: 0 -7px 0 20px !important;
  }
}
.header-light {
  background-color: #ffffff !important;
  box-shadow: 0px 0.5px 0px 0px #1f232926 !important;
  :deep(.work-bar) {
    color: var(--ed-color-black) !important;
  }
  .ed-divider {
    border-color: #1f232926 !important;
  }

  .system {
    color: #000 !important;
  }
  .de-logo {
    color: #3371ff !important;
  }
}
.header-flex {
  margin-bottom: 0.5px;
  display: flex;
  align-items: center;
  height: 56px;
  background-color: #050e21;
  padding: 0 24px;
  .operate-setting {
    margin-left: auto;
    display: flex;
    align-items: center;
    &:focus {
      outline: none;
    }
  }
}
</style>

<style lang="less">
.header-flex {
  .operate-setting {
    .ed-icon {
      cursor: pointer;
      color: rgba(255, 255, 255, 0.8);
      font-size: 20px;
    }
  }
}
.header-light {
  .operate-setting {
    .ed-icon {
      color: #646a73 !important;
    }
  }
}
</style>
