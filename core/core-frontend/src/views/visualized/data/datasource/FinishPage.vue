<script lang="ts" setup>
import { ref, onUnmounted } from 'vue'
import { propTypes } from '@/utils/propTypes'
import { useCache } from '@/hooks/web/useCache'

defineProps({
  name: propTypes.string.def(''),
  disabled: propTypes.bool.def(false)
})

const { wsCache } = useCache()
const emits = defineEmits(['createDataset', 'backToDatasourceList', 'continueCreating'])
const num = ref(5)
const checked = ref(false)
let time = setInterval(() => {
  num.value -= 1
  if (num.value === 0) {
    emits('backToDatasourceList')
    clearInterval(time)
  }
}, 1000)
onUnmounted(() => {
  clearInterval(time)
})
const createDataset = () => {
  emits('createDataset')
}
const backToDatasourceList = () => {
  emits('backToDatasourceList')
}
const continueCreating = () => {
  emits('continueCreating')
}

checked.value = wsCache.get('ds-create-success') || false
const handleChange = (val: boolean) => {
  wsCache.set('ds-create-success', val)
}
</script>

<template>
  <div class="finish-page-content">
    <div class="finish-page">
      <el-icon class="succeed-icon">
        <Icon name="icon_succeed_colorful"></Icon>
      </el-icon>

      <div class="succeed-text">创建成功</div>

      <div class="countdown-text">
        <span class="num">
          {{ num }}
        </span>
        秒后返回数据源列表
      </div>
      <div class="btn-list">
        <el-button @click="continueCreating" secondary> 继续创建 </el-button>
        <el-button @click="backToDatasourceList" type="primary"> 返回数据源列表 </el-button>
      </div>
      <div class="nolonger-tips">
        <el-checkbox @change="handleChange" v-model="checked" label="下次不再提示" />
      </div>

      <div class="maybe-want">
        <div class="title">您可能还想</div>
        <div class="ds-info">
          <el-icon class="ds">
            <Icon name="icon_dataset"></Icon>
          </el-icon>
          <div class="info">
            <p class="name">{{ name || '-' }}</p>
            <p class="size">为下一步的仪表板或大屏做准备</p>
          </div>
          <el-button class="create" secondary :disabled="disabled" @click="createDataset">
            去创建
          </el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<style lang="less" scoped>
.finish-page-content {
  width: 100%;
  height: 100%;
  background: #fff;
  display: flex;
  justify-content: center;
  position: absolute;
  bottom: 0;
  left: 0;

  .ed-button,
  :deep(.ed-checkbox__label) {
    font-weight: 400;
  }
  .finish-page {
    width: 592px;
    display: flex;
    flex-direction: column;
    align-items: center;
    padding-top: 83px;
    font-family: PingFang SC;
    font-style: normal;
    font-weight: 400;

    .succeed-icon {
      font-size: 58px;
      color: #34c724;
    }

    .succeed-text {
      color: #1f2329;
      font-size: 20px;
      font-weight: 500;
      line-height: 28px;
      margin: 16px 0;
    }

    .countdown-text {
      margin-bottom: 16px;
      font-size: 14px;
      line-height: 22px;
      .num {
        color: #3370ff;
        margin-right: 4px;
      }
    }
    .btn-list {
      margin-bottom: 16px;
    }

    .nolonger-tips {
      margin-bottom: 42px;
    }

    .maybe-want {
      width: 100%;
      .title {
        font-size: 14px;
        font-weight: 500;
        line-height: 22px;
        width: 100%;
        margin-bottom: 8px;
      }

      .ds-info {
        display: flex;
        align-items: center;
        width: 100%;
        height: 82px;
        padding: 0 16px 0 12px;
        border-radius: 4px;
        border: 1px solid #dee0e3;
        .ds {
          font-size: 32px;
          margin-right: 14.67px;
        }

        .info {
          font-family: PingFang SC;
          font-style: normal;
          font-weight: 400;
          .name {
            color: #1f2329;
            font-size: 14px;
            line-height: 22px;
          }

          .size {
            color: #8f959e;
            font-size: 12px;
            line-height: 20px;
          }
        }

        .create {
          cursor: pointer;
          margin-left: auto;
          line-height: 22px;
        }
      }
    }
  }
}
</style>
