<template>
  <div class="ticket">
    <div class="ticket-model">
      <div class="ticket-model-start">
        <el-tooltip class="item" effect="dark" :content="$t('link_ticket.back')" placement="top">
          <span class="back-tips">
            <el-icon class="custom-el-icon back-icon" @click.stop="close">
              <Icon class="toolbar-icon" name="icon_left_outlined" />
            </el-icon>
          </span>
        </el-tooltip>
        <span class="ticket-title">{{ 'Ticket ' + $t('commons.setting') }}</span>
      </div>
      <div class="ticket-model-end">
        <el-checkbox
          v-model="ticketRequire"
          @change="requireTicketChange"
          :label="t('link_ticket.require')"
        />
      </div>
    </div>
    <div class="ticket-add">
      <el-button @click.stop="addRow" text>
        <template #icon>
          <icon name="icon_add_outlined"></icon>
        </template>
        {{ t('commons.create') }}
      </el-button>
    </div>
    <div class="ticket-table">
      <el-table :data="state.tableData" style="width: 100%" size="small">
        <el-table-column prop="ticket" label="Ticket" width="130">
          <template v-slot="scope">
            <div class="ticket-row">
              <span :title="scope.row.ticket">{{ scope.row.ticket }}</span>
              <el-tooltip class="item" effect="dark" :content="$t('commons.copy')" placement="top">
                <el-button text @click.stop="copyTicket(scope.row.ticket)">
                  <template #icon>
                    <Icon name="de-copy"></Icon>
                  </template>
                </el-button>
              </el-tooltip>
              <el-tooltip
                class="item"
                effect="dark"
                :content="`${$t('link_ticket.refresh')} ticket`"
                placement="top"
              >
                <el-button text @click.stop="refreshTicket(scope.row)">
                  <template #icon>
                    <Icon name="icon_refresh_outlined"></Icon>
                  </template>
                </el-button>
              </el-tooltip>
            </div>
          </template>
        </el-table-column>

        <el-table-column prop="exp" :label="$t('visualization.over_time')" width="100">
          <template v-slot:header>
            <div class="ticket-exp-head">
              <span>{{ $t('visualization.over_time') }}</span>
              <el-tooltip
                class="item"
                effect="dark"
                :content="$t('link_ticket.time_tips')"
                placement="top"
              >
                <Icon name="dv-info"></Icon>
              </el-tooltip>
            </div>
          </template>
          <template v-slot="scope">
            <el-input
              v-if="scope.row.isEdit"
              :ref="el => setExpRef(el, scope.$index)"
              v-model="scope.row.exp"
              type="number"
              :placeholder="$t('commons.input_content')"
              min="0"
              max="1440"
              size="small"
              @input="v => handleInput(v, scope.$index)"
              @change="val => validateExp(val, scope.$index)"
            />
            <span v-else>
              {{ scope.row.exp }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="args" :label="$t('dataset.param')">
          <template v-slot="scope">
            <el-input
              v-if="scope.row.isEdit"
              :ref="el => setArgRef(el, scope.$index)"
              v-model="scope.row.args"
              type="text"
              :placeholder="$t('commons.input_content')"
              maxlength="200"
              size="small"
              @change="val => validateArgs(val, scope.$index)"
            />
            <span v-else>
              {{ scope.row.args || '-' }}
            </span>
          </template>
        </el-table-column>
        <el-table-column :label="$t('commons.operating')" width="80">
          <template v-slot="scope">
            <div class="ticket-row">
              <el-tooltip
                class="item"
                effect="dark"
                :content="$t('commons.delete')"
                placement="top"
              >
                <el-button text @click.stop="deleteTicket(scope.row, scope.$index)">
                  <template #icon>
                    <Icon name="icon_delete-trash_outlined"></Icon>
                  </template>
                </el-button>
              </el-tooltip>
              <el-tooltip
                class="item"
                effect="dark"
                :content="scope.row.isEdit ? $t('commons.save') : $t('commons.edit')"
                placement="top"
              >
                <el-button v-if="!scope.row.isEdit" text @click.stop="editRow(scope.row)">
                  <template #icon>
                    <Icon name="icon_edit_outlined"></Icon>
                  </template>
                </el-button>
                <el-button v-else text @click.stop="saveRow(scope.row, scope.$index)">
                  <template #icon>
                    <Icon name="edit-done"></Icon>
                  </template>
                </el-button>
              </el-tooltip>
            </div>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <div class="ticket-btn">
      <el-button type="primary" @click.stop="finish"> 完成 </el-button>
    </div>
  </div>
</template>

<script lang="ts" setup>
import { ref, reactive, onMounted, toRefs } from 'vue'
import { propTypes } from '@/utils/propTypes'
import { useI18n } from '@/hooks/web/useI18n'
import request from '@/config/axios'
import { ElMessage, ElMessageBox } from 'element-plus-secondary'
import useClipboard from 'vue-clipboard3'
import { useEmbedded } from '@/store/modules/embedded'
import { SHARE_BASE } from './option'

const embeddedStore = useEmbedded()
const { toClipboard } = useClipboard()
const { t } = useI18n()
const props = defineProps({
  uuid: propTypes.string.def(''),
  resourceId: propTypes.string.def(null),
  ticketRequire: propTypes.bool
})

const { ticketRequire } = toRefs(props)
const expRefs = ref({})
const argRefs = ref({})

const state = reactive({
  tableData: []
})
const emits = defineEmits(['close', 'requireChange'])

const close = () => {
  emits('close')
}

const setExpRef = (el, index) => {
  if (el) {
    expRefs.value[index] = el
  }
}

const setArgRef = (el, index) => {
  if (el) {
    argRefs.value[index] = el
  }
}

const requireTicketChange = val => {
  const url = '/ticket/enableTicket'
  const data = {
    resourceId: props.resourceId,
    require: val
  }
  request.post({ url, data }).then(() => {
    emits('requireChange', val)
  })
}
const createLimit = (count?: number) => {
  const realCount = count ? count : state.tableData.length || 0
  if (realCount > 4) {
    ElMessageBox.confirm('提示', {
      confirmButtonType: 'primary',
      type: 'warning',
      confirmButtonText: t('common.roger_that'),
      cancelButtonText: t('dataset.cancel'),
      autofocus: false,
      showClose: false,
      showCancelButton: false,
      tip: '最多支持创建5个Ticket'
    })
    return false
  }
  return true
}
const addRow = () => {
  if (!createLimit()) {
    return
  }
  const row = {
    ticket: '',
    exp: 30,
    args: '',
    uuid: props.uuid
  }
  const url = '/ticket/saveTicket'
  request.post({ url, data: row }).then(res => {
    row.ticket = res.data
    row['isEdit'] = false
    state.tableData.splice(0, 0, row)
  })
}
const formatLinkAddr = () => {
  return formatLinkBase() + props.uuid
}
const formatLinkBase = () => {
  let prefix = '/'
  if (embeddedStore.baseUrl) {
    prefix = embeddedStore.baseUrl + '#'
  } else {
    const href = window.location.href
    prefix = href.substring(0, href.indexOf('#') + 1)
  }
  if (prefix.includes('oidcbi/') || prefix.includes('casbi/')) {
    prefix = prefix.replace('oidcbi/', '')
    prefix = prefix.replace('casbi/', '')
  }
  return prefix + SHARE_BASE
}
const copyTicket = async ticket => {
  const url = `${formatLinkAddr()}?ticket=${ticket}`
  try {
    await toClipboard(url)
    ElMessage.success(t('common.copy_success'))
  } catch (e) {
    ElMessage.warning(t('common.copy_unsupported'), e)
  }
}
const refreshTicket = row => {
  const url = '/ticket/saveTicket'
  const param = JSON.parse(JSON.stringify(row))
  param['generateNew'] = true
  request.post({ url, data: param }).then(res => {
    row.ticket = res.data
  })
}
const handleInput = (val, index) => {
  if (val === null || val === '') {
    return
  }
  state.tableData[index]['exp'] = val.replace(/[^\d]/g, '')
}
const validateExp = (val, index) => {
  const cref = expRefs.value[index]
  const e = cref.input
  if (val === null || val === '' || typeof val === 'undefined') {
    state.tableData[index]['exp'] = 0
    return true
  }
  if (val > 1440 || val < 0) {
    e.style.color = 'red'
    e.parentNode.setAttribute('style', 'box-shadow: 0 0 0 1px red inset;')
    return false
  } else {
    e.style.color = null
    e.parentNode.removeAttribute('style')
    return true
  }
}

const validateArgs = (val, index) => {
  const cref = argRefs.value[index]
  const e = cref.input
  if (val === null || val === '' || typeof val === 'undefined') {
    e.style.color = null
    e.parentNode.removeAttribute('style')
    const child = e.parentNode.querySelector('.error-msg')
    if (child) {
      e.parentNode.removeChild(child)
    }
    return true
  }
  try {
    JSON.parse(val)
    e.style.color = null
    e.parentNode.removeAttribute('style')
    const child = e.parentNode.querySelector('.error-msg')
    if (child) {
      e.parentNode.removeChild(child)
    }
    return true
  } catch (error) {
    e.style.color = 'red'
    e.parentNode.setAttribute('style', 'box-shadow: 0 0 0 1px red inset;')
    const child = e.parentNode.querySelector('.error-msg')
    if (!child) {
      const errorDom = document.createElement('div')
      errorDom.className = 'error-msg'
      errorDom.innerText = '格式错误'
      e.parentNode.appendChild(errorDom)
    }
    return false
  }
}

const deleteTicket = (row, index) => {
  const param = { ticket: row.ticket }
  const url = '/ticket/delTicket'
  request.post({ url, data: param }).then(() => {
    state.tableData.splice(index, 1)
  })
}

const saveRow = (row, index) => {
  const url = '/ticket/saveTicket'
  validateExp(row.exp, index) &&
    validateArgs(row.args, index) &&
    request.post({ url, data: row }).then(() => {
      row.isEdit = false
    })
}
const editRow = row => {
  row.isEdit = true
}

const finish = () => {
  close()
}

const loadTicketData = () => {
  const resourceId = props.resourceId
  const url = `/ticket/query/${resourceId}`
  request.get({ url }).then(res => {
    state.tableData = res.data || []
  })
}
onMounted(() => {
  loadTicketData()
})
</script>

<style lang="less" scoped>
.ticket {
  min-height: 280px;
  .ticket-model {
    display: flex;
    height: 22px;
    justify-content: space-between;
    padding: 0;
    .ticket-model-start {
      display: flex;
      align-items: center;
      color: #1f2329;
      font-family: '阿里巴巴普惠体 3.0 55 Regular L3';
      font-weight: 500;
      font-size: 14px;
      .ticket-title {
        font-size: 14px;
      }
      .back-tips {
        i {
          width: 16px;
          height: 16px;
        }

        width: 22px;
        margin-right: 4px;
        display: flex;
        align-items: center;
        justify-content: center;
        &:hover {
          background-color: rgba(51, 112, 255, 0.1);
          color: var(--primary);
          cursor: pointer;
        }
      }
    }
    .ticket-model-end {
      display: flex;
      label {
        height: 22px;
        margin-right: 8px;
      }
    }
  }
  .ticket-add {
    margin: 16px 0;
    height: 22px;
    button {
      height: 22px;
      line-height: 22px;
    }
  }
  .ticket-table {
    border-top: 1px solid #d5d7d8;
    min-height: 156px;
    padding: 0 0;
    height: 50px;
    overflow-y: overlay;
    position: relative;
    height: calc(100% - 124px);
    :deep(.error-msg) {
      color: red;
      position: fixed;
      z-index: 9;
      font-size: 10px;
      height: 10px;
      margin-bottom: 12px;
      margin-right: -80px;
    }
    :deep(.ticket-exp-head) {
      display: flex;
      line-height: 22px;
      height: 22px;
      align-items: center;
      svg {
        margin-left: 4px;
        width: 16px;
        height: 16px;
        cursor: pointer;
      }
    }
    :deep(.ticket-row) {
      display: flex;
      align-items: center;
      height: 22px;
      span {
        width: 66px;
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

    :deep(.ed-table__header) {
      background-color: #f5f6f7;
      thead {
        tr {
          th {
            background-color: #f5f6f7 !important;
            .cell {
              line-height: 22px;
              padding: 4px 8px;
              font-size: 14px;
              font-weight: 500;
            }
          }
        }
      }
      height: 38px;
    }
    :deep(.ed-table__row) {
      height: 39px;
      td {
        .cell {
          line-height: 22px;
          height: 22px;
          font-size: 14px;
          font-weight: 400;
        }
      }
    }
    :deep(.ed-input__inner) {
      height: 18px;
      line-height: 18px;
    }
  }
  .ticket-btn {
    margin: 16px 0;
    float: right;
  }
}
</style>
