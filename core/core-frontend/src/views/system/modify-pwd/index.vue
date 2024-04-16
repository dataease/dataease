<template>
  <div class="user-center flex-align-center">
    <div class="user-center-container">
      <div class="user-tabs">
        <div class="tabs-title flex-align-center">用户中心</div>
        <el-divider />
        <div class="list-item_primary active">
          {{ t('user.change_password') }}
        </div>
      </div>
      <div class="user-info">
        <div class="base-info">
          <div class="info-title flex-align-center">
            <span class="title">
              {{ t('user.change_password') }}
            </span>
          </div>
          <update-pwd />
        </div>
      </div>
    </div>
  </div>
</template>
<script lang="ts" setup>
import UpdatePwd from './UpdatePwd.vue'
import { useI18n } from '@/hooks/web/useI18n'
const { t } = useI18n()
</script>
<style lang="less" scoped>
.user-center {
  width: 100%;
  flex-direction: column;
  padding-top: 24px;
  .user-center-container {
    display: flex;
    font-style: normal;
  }

  .user-tabs {
    width: 200px;
    height: 201px;
    border-radius: 4px;
    background: #fff;
    padding: 16px;
    .list-item_primary {
      padding: 9px 8px;
    }

    .ed-divider {
      margin: 4px 0;
      border-color: rgba(31, 35, 41, 0.15);
    }

    .tabs-title {
      padding-left: 8px;
      color: #8d9199;
      font-size: 14px;
      font-style: normal;
      font-weight: 500;
      line-height: 22px;
      height: 40px;
    }
  }
  .user-info {
    margin-left: 16px;
    width: 864px;
    height: 326px;

    .base-info {
      & + .base-info {
        margin-top: 12px;
        .bind-info {
          margin-top: 16px;
          display: flex;
          align-items: center;
          width: 100%;
          padding: 16px 24px 16px 24px;
          border-radius: 4px;
          border: 1px solid #dee0e3;
          .bind {
            font-size: 48px;
            margin-right: 12px;
          }

          .info {
            font-style: normal;
            font-weight: 400;
            width: 80%;

            .name {
              color: #1f2329;
              font-size: 16px;
              line-height: 24px;
              font-weight: 500;
              width: 100%;
            }

            .tip {
              color: #646a73;
              font-size: 14px;
              line-height: 22px;
              max-width: 600px;
              white-space: pre-wrap;
            }
          }

          .delete {
            margin-left: auto;
          }
        }
      }
      padding: 20px 24px 24px 24px;
      border-radius: 4px;
      background: #fff;
      .role {
        & + .role {
          margin-left: 4px;
        }
        display: inline-flex;
        height: 20px;
        padding: 0 6px;
        align-items: center;
        font-size: 12px;
        color: var(--ed-color-primary-dark-2, #2b5fd9);
        border-radius: 2px;
        background: var(--ed-color-primary-33, rgba(51, 112, 255, 0.2));
      }
      .info-title {
        .ed-button {
          margin-left: auto;
        }

        .title {
          color: #1f2329;
          font-size: 16px;
          font-weight: 500;
          line-height: 24px;
        }
      }
    }
  }
  .base-info-item {
    margin-top: 16px;
    font-size: 14px;
    font-weight: 400;
    line-height: 22px;
    width: 100%;
    .label {
      color: #646a73;
    }
    .value {
      margin-top: 4px;
      color: #1f2329;
    }
  }
  .mr12 {
    margin-top: 12px;
  }
}
</style>
<style lang="less">
.qr-code-dialog {
  font-style: normal;
  .ed-dialog__body {
    display: flex;
    flex-direction: column;
    align-items: center;
  }
  .qr-code-img {
    margin-top: 16px;
    display: inline-block;
    padding: 8px 12px;
    img {
      width: 184px;
      height: 184px;
    }
    border-radius: 8px;
    border: 1px solid #bbbfc4;
  }

  .refresh-login {
    margin: 0 auto;
    margin-top: 9px;
    color: #646a73;
    font-size: 14px;
    font-weight: 400;
    line-height: 22px;

    .ed-icon {
      margin-left: 4px;
    }
  }
}
</style>
