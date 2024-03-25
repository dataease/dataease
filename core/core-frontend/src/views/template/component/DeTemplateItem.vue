<template>
  <div
    :style="classBackground"
    class="de-card-model"
    :class="{ 'de-card-model-active': batchState, 'de-card-model-checked': model.checked }"
  >
    <el-checkbox class="custom-item-checkbox" v-model="model.checked" />
    <div class="card-img-model" :style="classImg">
      <img :src="imgUrlTrans(model.snapshot)" alt="" />
    </div>
    <div class="card-info">
      <div style="display: flex; align-items: center; width: calc(100% - 24px)">
        <el-tooltip class="item" effect="dark" :content="model.name" placement="top">
          <span class="de-model-text">{{ model.name }}</span>
        </el-tooltip>
      </div>

      <el-dropdown size="medium" trigger="click" @command="handleCommand">
        <el-icon class="el-icon-more"><MoreFilled /></el-icon>
        <template #dropdown>
          <el-dropdown-menu class="de-card-dropdown">
            <slot>
              <el-dropdown-item command="templateEdit">
                <el-icon><EditPen /></el-icon>
                编辑
              </el-dropdown-item>
              <el-dropdown-item command="delete">
                <el-icon><Delete /></el-icon>
                {{ $t('chart.delete') }}
              </el-dropdown-item>
            </slot>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </div>
  </div>
</template>

<script setup lang="ts">
import { imgUrlTrans } from '@/utils/imgUtils'
import { computed, toRefs } from 'vue'
const emits = defineEmits(['command'])

const props = defineProps({
  model: {
    type: Object
  },
  width: {
    type: Number
  },
  batchState: {
    type: Boolean
  }
})

const { model, batchState } = toRefs(props)

const classBackground = computed(() => {
  return {
    width: props.width + 'px',
    height: props.width * 0.714 + 'px'
  }
})
const classImg = computed(() => {
  return {
    width: props.width + 'px',
    height: props.width * 0.576 + 'px'
  }
})

const handleCommand = key => {
  emits('command', key)
}
</script>

<style lang="less">
.de-card-model-checked {
  border: 1px solid var(--ed-color-primary) !important;
}
.de-card-model-active {
  .custom-item-checkbox {
    display: inline !important;
  }
}
.de-card-model {
  box-sizing: border-box;
  position: relative;
  background: #ffffff;
  border: 1px solid #dee0e3;
  border-radius: 4px;
  margin: 0 16px 16px 0;
  overflow: hidden;
  .custom-item-checkbox {
    position: absolute;
    display: none;
    right: 8px;
    top: 8px;
  }
  .card-img-model {
    border-bottom: 1px solid var(--deCardStrokeColor, #dee0e3);
    height: 144px;
    width: 100%;
    padding: 4px 6px 0 4px;
    overflow: hidden;

    img {
      width: 100%;
      height: 100%;
      border-top-left-radius: 4px;
      border-top-right-radius: 4px;
    }
  }

  .card-info {
    width: 100%;
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 4px 12px 9px 0px;
    box-sizing: border-box;

    .el-icon-more {
      width: 24px;
      height: 24px;
      line-height: 24px;
      text-align: center;
      font-size: 12px;
      color: #646a73;
      cursor: pointer;
    }

    .el-icon-more:hover {
      background: rgba(31, 35, 41, 0.1);
      border-radius: 4px;
    }

    .el-icon-more:active {
      background: rgba(31, 35, 41, 0.2);
      border-radius: 4px;
    }
  }

  .de-model-text {
    margin-left: 8px;
    font-family: '阿里巴巴普惠体 3.0 55 Regular L3';
    font-style: normal;
    font-weight: 400;
    font-size: 14px;
    line-height: 22px;
    color: #1f2329;
    display: inline-block;
    width: 90%;
    text-overflow: ellipsis;
    white-space: nowrap;
    overflow: hidden;
    margin-right: 10px;
  }
}

.de-card-model:hover {
  .custom-item-checkbox {
    display: inline;
  }
}

.de-card-model:hover {
  box-shadow: 0px 6px 24px rgba(31, 35, 41, 0.08);
}

.de-card-dropdown {
  margin-top: 0 !important;
  .popper__arrow {
    display: none !important;
  }
}

.icon-screen-new {
  background: var(--ed-color-primary);
  border-radius: 4px;
  color: #fff;
  padding: 3px;
}
</style>
