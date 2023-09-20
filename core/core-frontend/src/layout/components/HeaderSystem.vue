<script lang="ts" setup>
import { ElHeader } from 'element-plus-secondary'
import { useRouter } from 'vue-router'
import AccountOperator from '@/layout/components/AccountOperator.vue'
const { push } = useRouter()
const backToMain = () => {
  push('/workbranch/index')
}
</script>

<template>
  <el-header class="header-flex system-header">
    <Icon className="logo" name="logo"></Icon>
    <el-divider direction="vertical" />
    <span class="system">系统管理</span>
    <div class="operate-setting">
      <span @click="backToMain" class="work-bar flex-align-center">
        <el-icon>
          <Icon name="icon_left_outlined"></Icon>
        </el-icon>
        <span class="work">返回工作台</span>
      </span>

      <AccountOperator />
    </div>
  </el-header>
</template>

<style lang="less" scoped>
.system-header {
  font-family: PingFang SC;

  .logo {
    width: 134px;
    height: 34px;
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
.header-flex {
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
      font-size: 18px;
    }
  }
}
</style>
