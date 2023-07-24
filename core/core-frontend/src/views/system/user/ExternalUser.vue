<script lang="ts" setup>
import { ref, shallowRef, computed, onUnmounted } from 'vue'
import { useI18n } from '@/hooks/web/useI18n'

const { t } = useI18n()
const dialogVisible = ref(false)
const userList = [
  {
    id: 'Beijig',
    name: 'Beijing',
    username: 'Beijing'
  },
  {
    id: 'Beiig',
    name: 'Beijing',
    username: 'Beijing'
  },
  {
    id: 'Beijg',
    name: 'Beijing',
    username: 'Beijing'
  }
]

const users = shallowRef([...userList])
const selectUserList = ref([])
const selectedUserList = ref([])
const selectedUserIdList = computed(() => {
  return selectedUserList.value.map(ele => ele.id)
})
let time = null
const filterMethod = value => {
  if (time) {
    clearTimeout(time)
  }
  time = setTimeout(() => {
    users.value = userList.filter(item => item.name.includes(value))
  }, 300)
}

onUnmounted(() => {
  if (time) {
    clearTimeout(time)
  }
})

const init = () => {
  dialogVisible.value = true
}
defineExpose({
  init
})
</script>

<template>
  <el-dialog
    v-model="dialogVisible"
    title="添加外部用户"
    width="600px"
    class="add-external-user-dialog"
  >
    <el-select
      :filter-method="filterMethod"
      filterable
      clearable
      style="width: 100%; margin-bottom: 4px"
      popper-class="external-user-popper"
      v-model="selectUserList"
    >
      <el-option v-for="item in users" :key="item.id" :value="item.id">
        <div class="user-list" @click.stop>
          <div class="info">
            <span>{{ item.name }}</span>
            <span>{{ item.username }}</span>
          </div>
          <el-button
            @click="selectedUserList.push(item)"
            v-if="!selectedUserIdList.includes(item.id)"
            class="add"
            type="primary"
          >
            {{ t('common.add') }}
          </el-button>
          <span class="added" v-else> 已添加 </span>
        </div>
      </el-option>
      <template #empty>
        <div class="empty-user">暂无结果，用户可能不存在</div>
      </template>
    </el-select>
    <div class="user-content">
      <div class="user-list-selected" v-for="item in selectedUserList" :key="item.id">
        <div class="info">
          <span>{{ item.name }}</span>
          <span>{{ item.username }}</span>
        </div>
        <span class="added"> 已添加 </span>
      </div>
    </div>
    <template #footer>
      <span class="dialog-footer">
        <el-button secondary @click="dialogVisible = false">{{ t('chart.cancel') }}</el-button>
        <el-button
          :type="!!selectedUserList.length ? 'primary' : 'info'"
          @click="dialogVisible = false"
        >
          {{ t('common.sure') }}
        </el-button>
      </span>
    </template>
  </el-dialog>
</template>

<style lang="less">
.add-external-user-dialog {
  .user-content {
    height: 240px;
    overflow-y: auto;
  }
  .user-list-selected {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: 4px;
    .added {
      color: #8d9199;
    }
    .info {
      display: flex;
      flex-direction: column;
      height: 42px;
      font-family: PingFang SC;
      font-weight: 400;
      font-style: normal;
      :nth-child(1) {
        font-size: 14px;
        line-height: 22px;
        max-width: 220px;
        color: #1f2329;
      }

      :nth-child(2) {
        color: #8d9199;
        font-size: 12px;
        line-height: 20px;
        max-width: 220px;
      }
    }
  }
}
.external-user-popper {
  .ed-select-dropdown__item {
    height: 42px !important;
    padding: 0;
  }

  .empty-user {
    height: 48px;
    display: flex;
    align-items: center;
    padding: 0 16px;
    color: #8d9199;
  }
  .user-list {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 0 12px 0 16px;
    .add {
      min-width: 48px !important;
      padding: 3px 8px;
      font-size: 12px;
      height: 28px;
    }

    .added {
      color: #8d9199;
    }
    .info {
      display: flex;
      flex-direction: column;
      height: 42px;
      font-family: PingFang SC;
      font-weight: 400;
      font-style: normal;
      :nth-child(1) {
        font-size: 14px;
        line-height: 22px;
        max-width: 220px;
        color: #1f2329;
      }

      :nth-child(2) {
        color: #8d9199;
        font-size: 12px;
        line-height: 20px;
        max-width: 220px;
      }
    }
  }
}
</style>
