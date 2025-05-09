<script lang="ts" setup>
import { ref, reactive } from 'vue'
import { ElMessage, ElLoading } from 'element-plus-secondary'
import { useI18n } from '@/hooks/web/useI18n'
import type { FormInstance, FormRules } from 'element-plus-secondary'
import request from '@/config/axios'
import deCopy from '@/assets/svg/de-copy.svg'
import icon_refresh_outlined from '@/assets/svg/icon_refresh_outlined.svg'
import icon_deleteTrash_outlined from '@/assets/svg/icon_delete-trash_outlined.svg'
import icon_add_outlined from '@/assets/svg/icon_add_outlined.svg'
import dvInfo from '@/assets/svg/dv-info.svg'
import useClipboard from 'vue-clipboard3'
import { propTypes } from '@/utils/propTypes'
const { toClipboard } = useClipboard()
const { t } = useI18n()
const dialogVisible = ref(false)
const loadingInstance = ref(null)
const ticketForm = ref<FormInstance>()
const inputRefList = ref<HTMLElement[]>([])
const props = defineProps({
  uuid: propTypes.string.def('')
})
interface TicketItem {
  ticket: string
  exp: number
  args: string
}
interface TicketArg {
  name: string
  val: string
}
const isEdit = ref(false)
const linkUrl = ref('')
const state = reactive({
  form: reactive<TicketItem>({
    ticket: '',
    exp: 30,
    args: ''
  }),
  argList: reactive<TicketArg[]>([{ name: '', val: '' }] as TicketArg[])
})

const rule = reactive<FormRules>({
  exp: [
    {
      required: true,
      message: t('common.require'),
      trigger: 'blur'
    }
  ]
})

const emits = defineEmits(['saved'])
const submitForm = async (formEl: FormInstance | undefined) => {
  if (!formEl) return
  await formEl.validate(valid => {
    if (valid) {
      argList2Args()
      const param = {
        uuid: props.uuid,
        ticket: state.form.ticket,
        exp: state.form.exp,
        args: state.form.args
      }
      showLoading()
      request
        .post({ url: '/ticket/saveTicket', data: param })
        .then(res => {
          if (!res.msg) {
            ElMessage.success(t('common.save_success'))
            emits('saved')
            reset()
          }
          closeLoading()
        })
        .catch(() => {
          closeLoading()
        })
    }
  })
}

const resetForm = (formEl: FormInstance | undefined) => {
  if (!formEl) return
  formEl.resetFields()
  dialogVisible.value = false
}

const reset = () => {
  resetForm(ticketForm.value)
}

const showLoading = () => {
  loadingInstance.value = ElLoading.service({ target: '.ticket-param-drawer' })
}
const closeLoading = () => {
  loadingInstance.value?.close()
}

const edit = (row, link) => {
  resetFormData()
  linkUrl.value = link
  isEdit.value = !!row?.ticket
  if (!isEdit.value) {
    generateTicket()
    dialogVisible.value = true
    return
  }
  for (const key in row) {
    if (state.form.hasOwnProperty(key)) {
      state.form[key] = row[key]
    }
  }
  if (state.form.args) {
    args2ArgList()
  }
  dialogVisible.value = true
}

const generateTicket = () => {
  const url = '/ticket/tempTicket'
  request.get({ url }).then(res => {
    state.form.ticket = res.data
  })
}

const addArgRow = () => {
  const row: TicketArg = { name: '', val: '' }
  state.argList.push(row)
}
const delArgRow = index => {
  const last = state.argList.length === 1
  if (last) {
    state.argList[index]['name'] = ''
    state.argList[index]['val'] = ''
    return
  }
  state.argList.splice(index, 1)
}

const args2ArgList = () => {
  if (!state.form.args) {
    return
  }
  const argObj = JSON.parse(state.form.args)
  for (const key in argObj) {
    const val = argObj[key]
    if (val && Array.isArray(val)) {
      const row = { name: key, val: JSON.stringify(val) }
      state.argList.push(row)
    } else {
      const tempArray = [val]
      const row = { name: key, val: JSON.stringify(tempArray) }
      state.argList.push(row)
    }
  }
  if (state.argList?.length > 1 && !state.argList[0]['name']) {
    state.argList.splice(0, 1)
  }
}

const argList2Args = () => {
  if (!state.argList?.length) {
    state.form.args = ''
    return
  }
  const argObj = {}
  state.argList.forEach(row => {
    if (row.name && row.val) {
      argObj[row.name] = JSON.parse(row.val)
    }
  })
  state.form.args = JSON.stringify(argObj)
}
const setErrorStatus = (index, value, status?: boolean) => {
  let valid = !!status
  if (!value?.length) {
    valid = true
  } else if (status === null || typeof status === 'undefined') {
    try {
      JSON.parse(value)
      valid = true
    } catch (error) {
      valid = false
    }
  }
  const domRef = inputRefList[index]
  const e = domRef.input
  const className = 'link-ticket-error-msg'
  const fullClassName = `.${className}`
  if (valid) {
    e.style = null
    e.style.borderColor = null
    const child = e.parentElement.querySelector(fullClassName)
    if (child) {
      e.parentElement['style'] = null
      e.parentElement.removeChild(child)
    }
  } else {
    const msg = 'JSON格式错误'
    e.style.color = 'red'
    e.style.borderColor = 'red'
    e.parentElement['style']['box-shadow'] = '0 0 0 1px red inset'
    const child = e.parentElement.querySelector(fullClassName)
    if (!child) {
      const errorDom = document.createElement('div')
      errorDom.className = className
      errorDom.innerText = msg
      e.parentElement.appendChild(errorDom)
    } else {
      child.innerText = msg
    }
  }
}
const setInputRef = (el, index) => {
  if (el) {
    inputRefList[index] = el
  }
}
const copyTicket = async ticket => {
  const url = `${linkUrl.value}?ticket=${ticket}`
  try {
    await toClipboard(url)
    ElMessage.success(t('common.copy_success'))
  } catch (e) {
    ElMessage.warning(t('common.copy_unsupported'), e)
  }
}
const refreshTicket = () => {
  if (!isEdit.value) {
    generateTicket()
    return
  }
  const url = '/ticket/saveTicket'
  const param = { ticket: state.form.ticket }
  param['generateNew'] = true
  request.post({ url, data: param }).then(res => {
    state.form.ticket = res.data
    emits('saved')
  })
}
const resetFormData = () => {
  state.form = {
    ticket: '',
    exp: 30,
    args: ''
  }
  state.argList = [{ name: '', val: '' }]
  isEdit.value = false
  linkUrl.value = ''
}

defineExpose({
  edit
})
</script>

<template>
  <el-drawer
    :title="`${t('commons.add')} Ticket`"
    v-model="dialogVisible"
    modal-class="ticket-param-drawer"
    size="600px"
    direction="rtl"
  >
    <el-form
      ref="ticketForm"
      require-asterisk-position="right"
      :model="state.form"
      :rules="rule"
      label-width="80px"
      label-position="top"
    >
      <div class="ticket-tips-label"><span>Ticket</span></div>
      <div class="ticket-row ticket-tips-label">
        <span :title="state.form.ticket">{{ state.form.ticket }}</span>
        <el-tooltip class="item" effect="dark" :content="t('commons.copy')" placement="top">
          <el-button text @click.stop="copyTicket(state.form.ticket)">
            <template #icon>
              <Icon name="de-copy"><deCopy class="svg-icon" /></Icon>
            </template>
          </el-button>
        </el-tooltip>
        <el-tooltip
          class="item"
          effect="dark"
          :content="`${t('link_ticket.refresh')} ticket`"
          placement="top"
        >
          <el-button text @click.stop="refreshTicket">
            <template #icon>
              <Icon name="icon_refresh_outlined">
                <icon_refresh_outlined class="svg-icon" />
              </Icon>
            </template>
          </el-button>
        </el-tooltip>
      </div>
      <el-form-item prop="exp" :label="t('visualization.over_time')">
        <template v-slot:label>
          <div class="ticket-form-info-tips">
            <span class="custom-form-item__label">{{ t('visualization.over_time') }}</span>
            <el-tooltip effect="dark" :content="t('link_ticket.time_tips')" placement="top">
              <el-icon>
                <Icon name="dv-info"><dvInfo class="svg-icon" /></Icon>
              </el-icon>
            </el-tooltip>
          </div>
        </template>
        <el-input-number
          v-model="state.form.exp"
          autocomplete="off"
          step-strictly
          class="text-left edit-all-line"
          :min="0"
          :max="1440"
          :placeholder="t('common.inputText')"
          controls-position="right"
          type="number"
        />
      </el-form-item>

      <el-form-item prop="args" :label="t('dataset.param')" class="last-form-item">
        <template v-slot:label>
          <div class="ticket-form-info-tips">
            <span class="custom-form-item__label">{{ t('dataset.param') }}</span>
            <el-tooltip effect="dark" :content="t('link_ticket.arg_format_tips')" placement="top">
              <el-icon>
                <Icon name="dv-info"><dvInfo class="svg-icon" /></Icon>
              </el-icon>
            </el-tooltip>
          </div>
        </template>
        <div class="args-line" v-for="(row, index) in state.argList" :key="index">
          <el-input v-model="row['name']" :placeholder="t('visualization.input_param_name')" />
          <el-input
            :ref="el => setInputRef(el, index)"
            v-model="row['val']"
            :placeholder="t('link_ticket.arg_val_tips')"
            @blur="setErrorStatus(index, row['val'])"
          />
          <div
            :style="{ opacity: state.argList.length !== 1 ? 1 : 0 }"
            class="arg-del-btn"
            @click.stop="delArgRow(index)"
          >
            <Icon>
              <icon_deleteTrash_outlined class="svg-icon" />
            </Icon>
          </div>
        </div>
      </el-form-item>

      <div class="ticket-add">
        <el-button @click.stop="addArgRow" text>
          <template #icon>
            <icon name="icon_add_outlined"><icon_add_outlined class="svg-icon" /></icon>
          </template>
          {{ t('commons.add') }}
        </el-button>
      </div>
    </el-form>
    <template #footer>
      <span class="dialog-footer">
        <el-button secondary @click="resetForm(ticketForm)">{{ t('common.cancel') }}</el-button>
        <el-button type="primary" @click="submitForm(ticketForm)">
          {{ t('commons.save') }}
        </el-button>
      </span>
    </template>
  </el-drawer>
</template>
<style lang="less">
.ticket-param-drawer {
  .ed-drawer__body {
    padding: 24px !important;
  }
  .ed-drawer__footer {
    box-shadow: 0 -1px 4px #1f232926 !important;
    height: 64px !important;
    padding: 16px 24px !important;
    .dialog-footer {
      height: 32px;
      line-height: 32px;
    }
  }
  .ed-form-item__label {
    line-height: 22px !important;
    height: 22px !important;
    .ticket-form-info-tips {
      width: fit-content;
      display: inline-flex;
      align-items: center;
      column-gap: 4px;
    }
  }
  .ed-form-item {
    &.is-required.asterisk-right {
      .ed-form-item__label:after {
        display: none;
      }
      .ticket-form-info-tips {
        .custom-form-item__label:after {
          content: '*';
          color: var(--ed-color-danger);
          margin-left: 2px;
          font-family: var(--de-custom_font, 'PingFang');
          font-size: 14px;
          font-style: normal;
          font-weight: 400;
        }
      }
    }
  }
}
</style>
<style scoped lang="less">
.ticket-param-drawer {
  .last-form-item {
    margin-bottom: 8px;
  }
  .ed-form-item:not(.last-form-item) {
    margin-bottom: 16px;
  }
  .is-error {
    margin-bottom: 40px !important;
  }
  .edit-all-line {
    width: 552px !important;
  }
  .args-line {
    width: 100%;
    align-items: center;
    display: flex;
    line-height: 32px;
    column-gap: 8px;
    .arg-del-btn {
      color: #646a73;
      padding: 4px;
      width: 24px;
      height: 24px;
      border-radius: 4px;
      display: flex;
      align-items: center;
      justify-content: center;
      cursor: pointer;
      &:hover {
        background-color: #1f23291a;
      }
      svg {
        width: 16px;
        height: 16px;
      }
    }
    &:not(:last-child) {
      margin-bottom: 8px;
    }
    :deep(.link-ticket-error-msg) {
      color: red;
      position: absolute;
      z-index: 9;
      font-size: 10px;
      height: 10px;
      top: 21px;
      width: 350px;
      left: 0px;
    }
  }
  .ticket-tips-label {
    line-height: 22px;
    height: 22px;
    display: flex;
    align-items: center;
    margin-bottom: 8px;
  }
  .ticket-row {
    margin-bottom: 16px !important;
    span {
      margin-right: 8px;
      overflow: hidden;
      white-space: nowrap;
      text-overflow: ellipsis;
    }
    button {
      height: 16px;
      line-height: 16px;
      width: 16px;
    }
    .ed-button + .ed-button {
      margin-left: 8px;
    }
  }
}
</style>
