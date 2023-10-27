<template>
  <el-container class="online-map-container">
    <el-aside width="200px" class="online-map-aside">
      <div class="geo-title">
        <span>{{ t('online_map.onlinemap') }}</span>
      </div>
      <div class="online-form-item">
        <div class="map-item">
          <div class="map-item-label">
            <span class="form-label">Key</span>
          </div>
        </div>
        <div class="map-item">
          <el-input v-model="key" />
        </div>
      </div>
      <el-button type="primary" :disabled="!key" @click="saveHandler">{{
        t('commons.save')
      }}</el-button>
    </el-aside>
    <el-main>
      <div class="de-map-container" :id="domId" />
    </el-main>
  </el-container>
</template>

<script lang="ts" setup>
import { ref } from 'vue'
import { useI18n } from '@/hooks/web/useI18n'
const { t } = useI18n()
const key = ref('')

const domId = ref('de-map-container')
const saveHandler = () => {
  console.log(111)
}
</script>

<style lang="less" scoped>
.de-map-container {
  height: 100%;
  width: 100%;
}
.online-map-container {
  height: 100%;
  .online-map-aside {
    width: 280px !important;
    border-right: 1px solid #1f232926;
    padding: 16px;
    .geo-title {
      height: 24px;
      line-height: 24px;
      margin-bottom: 16px;
      span:first-child {
        font-size: 16px;
        font-weight: 500;
        line-height: 24px;
      }
    }
    .online-form-item {
      height: 64px;
      margin-bottom: 16px;
      .map-item {
        height: 32px;
        .map-item-label {
          height: 22px;
          line-height: 22px;
          font-size: 14px;
          font-weight: 400;
          color: #1f2329;
        }
      }
    }
  }
}
</style>
