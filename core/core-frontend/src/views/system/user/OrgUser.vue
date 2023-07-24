<script lang="ts" setup>
import { ref, shallowRef } from 'vue'
import { useI18n } from '@/hooks/web/useI18n'
const { t } = useI18n()
const dialogVisible = ref(false)
const userKeyword = ref('')
const selectUserList = ref()
const filterUserList = ref(
  Array(10)
    .fill(1)
    .map((ele, index) => ({
      name: '123123' + ele,
      id: 123123 + index,
      phone: 'oooo',
      check: false,
      colorName: '123123' + ele
    }))
)

selectUserList.value = [...filterUserList.value]
const checkAll = ref(false)
const isIndeterminate = ref(false)
const checkedUsers = shallowRef([])
const triggerFilterUser = (val: string) => {
  selectUserList.value = filterUserList.value.filter(ele => {
    if (ele.name.includes(val)) {
      ele.colorName = ele.name.replace(val, `<span style="color: #307eff">${val}</span>`)
      return true
    }
    return false
  })
  handleCheckedUsersChange()
  if (!selectUserList.value.length) {
    checkAll.value = false
    isIndeterminate.value = false
  }
}
const handleCheckAllChange = (val: boolean) => {
  if (userKeyword.value) {
    const checkedUserIds = checkedUsers.value
      .filter(ele => ele.name.includes(userKeyword.value))
      .map(ele => ele.id)
    checkedUsers.value = val
      ? [
          ...checkedUsers.value,
          ...selectUserList.value.filter(ele => !checkedUserIds.includes(ele.id))
        ]
      : checkedUsers.value.filter(ele => !checkedUserIds.includes(ele.id))
    checkedUsers.value.forEach(ele => {
      ele.check = val
    })
  } else {
    selectUserList.value.forEach(ele => {
      ele.check = val
    })
    checkedUsers.value = val ? selectUserList.value : []
  }
  isIndeterminate.value = false
}
const handleCheckedUsersChange = () => {
  checkedUsers.value = filterUserList.value.filter(ele => ele.check)
  const checkedCount = selectUserList.value.filter(ele => ele.check).length
  checkAll.value = checkedCount === selectUserList.value.length
  isIndeterminate.value = checkedCount > 0 && checkedCount < selectUserList.value.length
}

const init = () => {
  dialogVisible.value = true
}

const handleClearUser = item => {
  item.check = false
  checkedUsers.value = checkedUsers.value.filter(ele => ele.id !== item.id)
}

const clearAll = () => {
  handleCheckAllChange(false)
  checkAll.value = false
}

defineExpose({
  init
})
</script>

<template>
  <el-dialog v-model="dialogVisible" title="添加组织用户" width="600px" class="add-org-user-dialog">
    <div class="add-org-user">
      <div class="select-user-list">
        <el-input v-model="userKeyword" clearable @input="triggerFilterUser">
          <template #prefix>
            <el-icon>
              <Icon name="icon_search-outline_outlined"></Icon>
            </el-icon>
          </template>
        </el-input>
        <template v-if="!!selectUserList.length">
          <el-checkbox
            v-model="checkAll"
            :indeterminate="isIndeterminate"
            @change="handleCheckAllChange"
            >{{ t('chart.result_mode_all') }}</el-checkbox
          >
          <el-checkbox
            @change="handleCheckedUsersChange"
            v-model="user.check"
            v-for="user in selectUserList"
            :key="user.id"
            :label="user.id"
          >
            <div class="user-label">
              <span v-html="user.colorName"></span>
              <span>{{ user.phone }}</span>
            </div>
          </el-checkbox>
        </template>
        <span class="no-result" v-else> 没有找到相关内容 </span>
      </div>
      <div class="selected-user-list">
        <div class="select-num">
          <span>已选: {{ checkedUsers.length }} 个用户</span>
          <el-button @click="clearAll" text>{{ t('commons.clear') }}</el-button>
        </div>
        <div class="user-list" v-for="user in checkedUsers" :key="user.id">
          <div class="info">
            <span>{{ user.name }}</span>
            <span>{{ user.phone }}</span>
          </div>
          <el-icon @click="handleClearUser(user)" class="hover-icon">
            <Icon name="icon_close_outlined"></Icon>
          </el-icon>
        </div>
      </div>
    </div>
    <template #footer>
      <span class="dialog-footer">
        <el-button secondary @click="dialogVisible = false">{{ t('chart.cancel') }}</el-button>
        <el-button
          :type="!!checkedUsers.length ? 'primary' : 'info'"
          @click="dialogVisible = false"
        >
          {{ t('common.add') }}
        </el-button>
      </span>
    </template>
  </el-dialog>
</template>

<style lang="less">
.add-org-user-dialog {
  .add-org-user {
    height: 428px;
    width: 100%;
    border: 1px solid #dee0e3;
    border-radius: 4px;
    margin: 24px 0;
    display: flex;
    .ed-checkbox {
      width: 100%;
      height: 42px;
      margin: 0 0 8px 0;
      padding-left: 24px;

      &:hover {
        background: rgba(31, 35, 41, 0.1);
      }
    }
    .user-label {
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
      }

      :nth-child(2) {
        color: #8d9199;
        font-size: 12px;
        line-height: 20px;
        max-width: 220px;
      }
    }

    .selected-user-list,
    .select-user-list {
      height: 100%;
      width: 50%;
      overflow-y: auto;
      position: relative;
      .no-result {
        position: absolute;
        top: 50%;
        left: 50%;
        transform: translate(-50%, -50%);
      }
    }
    .select-user-list {
      padding-top: 24px;
      .ed-input {
        margin: 0 24px;
        margin-bottom: 8px;
        width: calc(100% - 48px);
      }
    }

    .selected-user-list {
      border-left: 1px solid #dee0e3;
      .select-num {
        display: flex;
        align-items: center;
        justify-content: space-between;
        padding: 21px 12px 16px 16px;
      }
      .user-list {
        display: flex;
        align-items: center;
        justify-content: space-between;
        padding: 0 12px 0 16px;
        &:hover {
          background: rgba(31, 35, 41, 0.1);
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
          }

          :nth-child(2) {
            color: #8d9199;
            font-size: 12px;
            line-height: 20px;
            max-width: 220px;
          }

          .hover-icon {
            cursor: pointer;
            font-size: 16px;
          }
        }
      }
    }
  }
}
</style>
