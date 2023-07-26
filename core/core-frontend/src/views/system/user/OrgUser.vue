<script lang="ts" setup>
import { ref, shallowRef, reactive } from 'vue'
import { useI18n } from '@/hooks/web/useI18n'
import { userOptionForRoleApi, mountUserApi } from '@/api/user'
import { setColorName } from '@/utils/utils'
import { ElMessage } from 'element-plus-secondary'
const { t } = useI18n()
const dialogVisible = ref(false)
const loading = ref(false)
const userKeyword = ref('')
const props = defineProps({
  rid: {
    type: String
  }
})
const state = reactive({
  selectUserList: []
})
const checkAll = ref(false)
const isIndeterminate = ref(false)
const checkedUsers = shallowRef([])
const triggerFilterUser = (val: string) => {
  state.selectUserList.forEach(item => {
    setColorName(item, val)
    item['hidden'] = val && !item.name.toLocaleLowerCase().includes(val.toLocaleLowerCase())
  })

  handleCheckedUsersChange()
  if (!state.selectUserList.length) {
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
          ...state.selectUserList.filter(ele => !checkedUserIds.includes(ele.id))
        ]
      : checkedUsers.value.filter(ele => !checkedUserIds.includes(ele.id))
    checkedUsers.value.forEach(ele => {
      ele.check = val
    })
  } else {
    state.selectUserList.forEach(ele => {
      ele.check = val
    })
    checkedUsers.value = val ? state.selectUserList : []
  }
  isIndeterminate.value = false
}
const handleCheckedUsersChange = () => {
  checkedUsers.value = state.selectUserList.filter(ele => ele.check)
  const checkedCount = state.selectUserList.filter(ele => ele.check).length
  checkAll.value = checkedCount === state.selectUserList.length
  isIndeterminate.value = checkedCount > 0 && checkedCount < state.selectUserList.length
}

const init = () => {
  dialogVisible.value = true
  checkAll.value = false
  isIndeterminate.value = false
  checkedUsers.value = []
  userKeyword.value = ''
  optionSearch(props.rid)
}

const handleClearUser = item => {
  item.check = false
  checkedUsers.value = checkedUsers.value.filter(ele => ele.id !== item.id)
  const checkedCount = state.selectUserList.filter(ele => ele.check).length
  checkAll.value = checkedCount === state.selectUserList.length
  isIndeterminate.value = checkedCount > 0 && checkedCount < state.selectUserList.length
}

const clearAll = () => {
  handleCheckAllChange(false)
  checkAll.value = false
}

const optionSearch = (rid?: string) => {
  const param = { rid }
  if (rid) {
    loading.value = true
    userOptionForRoleApi(param).then(res => {
      if (res?.data?.length) {
        state.selectUserList = res.data
      } else {
        state.selectUserList = []
      }
      loading.value = false
    })
  }
}
const emits = defineEmits(['refresh-grid'])

const bindUsers = () => {
  const uids = checkedUsers.value.map(user => user.id)
  const param = { rid: props.rid, uids }
  loading.value = true
  mountUserApi(param).then(() => {
    ElMessage({
      message: t('role.bind_success'),
      type: 'success'
    })
    emits('refresh-grid')
    loading.value = false
    dialogVisible.value = false
  })
}

defineExpose({
  init
})
</script>

<template>
  <el-dialog
    v-model="dialogVisible"
    title="添加组织用户"
    width="600px"
    class="add-org-user-dialog"
    v-if="dialogVisible"
  >
    <div class="add-org-user" v-loading="loading">
      <div class="select-user-list">
        <el-input v-model="userKeyword" clearable @change="triggerFilterUser">
          <template #prefix>
            <el-icon>
              <Icon name="icon_search-outline_outlined"></Icon>
            </el-icon>
          </template>
        </el-input>
        <template v-if="!!state.selectUserList.length">
          <el-checkbox
            v-model="checkAll"
            :indeterminate="isIndeterminate"
            @change="handleCheckAllChange"
            >{{ t('chart.result_mode_all') }}</el-checkbox
          >
          <el-checkbox
            @change="handleCheckedUsersChange"
            v-model="user.check"
            v-for="user in state.selectUserList"
            :key="user.id"
            :label="user.id"
            v-show="!user.hidden"
          >
            <div class="user-label" :title="user.name + '(' + user.account + ')'">
              <span v-html="user.colorName || user.name"></span>
              <span>{{ '(' + user.account + ')' }}</span>
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
          <div class="info" :title="user.name + '(' + user.account + ')'">
            <span>{{ user.name }}</span>
            <span>{{ '(' + user.account + ')' }}</span>
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
          :disabled="loading || !checkedUsers.length"
          :type="!!checkedUsers.length ? 'primary' : 'info'"
          @click="bindUsers"
        >
          {{ t('common.add') }}
        </el-button>
      </span>
    </template>
  </el-dialog>
</template>

<style lang="less">
.add-org-user-dialog {
  .ed-dialog__body {
    padding: 0 24px !important;
  }
  .add-org-user {
    height: 428px;
    width: 100%;
    border: 1px solid #dee0e3;
    border-radius: 4px;
    margin: 24px 0;
    display: flex;
    .ed-checkbox {
      width: 100%;
      // height: 42px;
      margin: 0 0 8px 0;
      padding-left: 24px;

      &:hover {
        background: rgba(31, 35, 41, 0.1);
      }
    }
    .user-label {
      // display: flex;
      // flex-direction: row;
      height: 22px;
      font-family: PingFang SC;
      font-weight: 400;
      font-style: normal;
      overflow: hidden;
      text-overflow: ellipsis;
      -o-text-overflow: ellipsis;
      -webkit-text-overflow: ellipsis;
      -moz-text-overflow: ellipsis;
      white-space: nowrap;
      width: 210px;

      :nth-child(1) {
        font-size: 14px;
        line-height: 22px;
      }

      :nth-child(2) {
        color: #8d9199;
        font-size: 12px;
        line-height: 20px;
        margin-top: 1px;
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
        height: 34px;
        &:hover {
          background: rgba(31, 35, 41, 0.1);
        }

        .info {
          font-family: PingFang SC;
          font-weight: 400;
          font-style: normal;
          overflow: hidden;
          text-overflow: ellipsis;
          -o-text-overflow: ellipsis;
          -webkit-text-overflow: ellipsis;
          -moz-text-overflow: ellipsis;
          white-space: nowrap;
          width: 210px;
          :nth-child(1) {
            font-size: 14px;
            line-height: 22px;
          }

          :nth-child(2) {
            color: #8d9199;
            font-size: 12px;
            line-height: 20px;
            margin-top: 1px;
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
