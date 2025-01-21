<template>
  <div class="ticket">
    <div class="ticket-model">
      <div class="ticket-model-start">
        <span class="ticket-title">{{ 'Ticket ' + t('commons.setting') }}</span>
      </div>
      <div class="ticket-model-end">
        <el-checkbox
          v-model="ticketRequire"
          @change="requireTicketChange"
          :label="t('link_ticket.require')"
        />

        <span class="top-split" />
        <span class="top-close" @click.stop="finish">
          <icon name="icon_close_outlined"><icon_close_outlined class="svg-icon" /></icon>
        </span>
      </div>
    </div>
    <div class="ticket-add">
      <el-button @click.stop="addRow" text>
        <template #icon>
          <icon name="icon_add_outlined"><icon_add_outlined class="svg-icon" /></icon>
        </template>
        {{ t('commons.add') }}
      </el-button>
    </div>
    <div class="ticket-table">
      <grid-table
        ref="multipleTableRef"
        :show-empty-img="false"
        :table-data="state.tableData"
        :pagination="state.paginationConfig"
        class="popper-max-width"
        :class="{ 'popper-max-height': state.tableData?.length >= 10 }"
        @current-change="pageChange"
        @size-change="sizeChange"
      >
        <el-table-column prop="ticket" label="Ticket">
          <template v-slot="scope">
            <div class="ticket-row">
              <span :title="scope.row.ticket">{{ scope.row.ticket }}</span>
              <el-tooltip class="item" effect="dark" :content="$t('commons.copy')" placement="top">
                <el-button text @click.stop="copyTicket(scope.row.ticket)">
                  <template #icon>
                    <Icon name="de-copy"><deCopy class="svg-icon" /></Icon>
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
                    <Icon name="icon_refresh_outlined"
                      ><icon_refresh_outlined class="svg-icon"
                    /></Icon>
                  </template>
                </el-button>
              </el-tooltip>
            </div>
          </template>
        </el-table-column>

        <el-table-column prop="exp" :label="$t('visualization.over_time')">
          <template v-slot:header>
            <div class="ticket-exp-head">
              <span>{{ t('visualization.over_time') }}</span>
              <el-tooltip
                class="item"
                effect="dark"
                :content="t('link_ticket.time_tips')"
                placement="top"
              >
                <span class="exp-tips-span">
                  <Icon name="dv-info"><dvInfo class="svg-icon" /></Icon>
                </span>
              </el-tooltip>
            </div>
          </template>
          <template v-slot="scope">
            <span>
              {{ scope.row.exp }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="args" :label="$t('dataset.param')" width="117">
          <template v-slot="scope">
            <el-tooltip class="box-item" effect="light" :content="scope.row.args" placement="top">
              <span style="color: #3370ff">
                {{ getArgCount(scope.row) }}
              </span>
            </el-tooltip>
          </template>
        </el-table-column>
        <el-table-column :label="$t('commons.operating')" width="80">
          <template v-slot="scope">
            <div class="ticket-row">
              <el-tooltip class="item" effect="dark" :content="$t('commons.edit')" placement="top">
                <el-button text @click.stop="editRow(scope.row)">
                  <template #icon>
                    <Icon name="icon_edit_outlined"><icon_edit_outlined class="svg-icon" /></Icon>
                  </template>
                </el-button>
              </el-tooltip>

              <el-tooltip class="item" effect="dark" :content="t('commons.delete')" placement="top">
                <el-icon
                  style="margin-left: 8px; color: #646a73; cursor: pointer"
                  @click.stop="deleteTicket(scope.row)"
                >
                  <Icon name="icon_delete-trash_outlined"
                    ><icon_deleteTrash_outlined class="svg-icon"
                  /></Icon>
                </el-icon>
              </el-tooltip>
            </div>
          </template>
        </el-table-column>
      </grid-table>
    </div>
  </div>
  <ticket-edit ref="ticketEditor" :uuid="props.uuid" @saved="loadTicketData" />
</template>

<script lang="ts" setup>
import icon_close_outlined from '@/assets/svg/icon_close_outlined.svg'
import deCopy from '@/assets/svg/de-copy.svg'
import icon_refresh_outlined from '@/assets/svg/icon_refresh_outlined.svg'
import dvInfo from '@/assets/svg/dv-info.svg'
import icon_deleteTrash_outlined from '@/assets/svg/icon_delete-trash_outlined.svg'
import icon_edit_outlined from '@/assets/svg/icon_edit_outlined.svg'

import icon_add_outlined from '@/assets/svg/icon_add_outlined.svg'
import { ref, reactive, onMounted, toRefs } from 'vue'
import { propTypes } from '@/utils/propTypes'
import { useI18n } from '@/hooks/web/useI18n'
import request from '@/config/axios'
import { ElMessage, ElMessageBox } from 'element-plus-secondary'
import useClipboard from 'vue-clipboard3'
import { useEmbedded } from '@/store/modules/embedded'
import { SHARE_BASE } from './option'
import GridTable from '@/components/grid-table/src/GridTable.vue'
import TicketEdit from './TicketEdit.vue'
const embeddedStore = useEmbedded()
const { toClipboard } = useClipboard()
const { t } = useI18n()
const props = defineProps({
  uuid: propTypes.string.def(''),
  resourceId: propTypes.string.def(null),
  ticketRequire: propTypes.bool
})
const ticketEditor = ref()
const { ticketRequire } = toRefs(props)
const ticketLimit = ref(0)
const state = reactive({
  tableData: [],
  paginationConfig: {
    currentPage: 1,
    pageSize: 10,
    total: 0
  }
})
const emits = defineEmits(['close', 'requireChange'])

const close = () => {
  emits('close')
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
const createLimit = () => {
  const realCount = state.paginationConfig.total
  if (ticketLimit.value && realCount >= ticketLimit.value) {
    ElMessageBox.confirm(t('chart.tips'), {
      confirmButtonType: 'primary',
      type: 'warning',
      confirmButtonText: t('common.roger_that'),
      cancelButtonText: t('dataset.cancel'),
      autofocus: false,
      showClose: false,
      showCancelButton: false,
      tip: t('work_branch.max_ticket_count')
    })
    return true
  }
  return false
}
const getArgCount = row => {
  const args = row.args
  if (!args) {
    return 0
  }
  try {
    const obj = JSON.parse(args)
    return Object.keys(obj).length
  } catch (error) {
    console.error(error)
    return 0
  }
}
const addRow = () => {
  if (createLimit()) {
    return
  }
  ticketEditor.value.edit(null, formatLinkAddr())
}
const formatLinkAddr = () => {
  return formatLinkBase() + props.uuid
}
const formatLinkBase = () => {
  let prefix = '/'
  if (embeddedStore.baseUrl) {
    prefix = embeddedStore.baseUrl + '#'
  } else {
    prefix = window.location.origin + window.location.pathname + '#'
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

const deleteTicket = row => {
  const param = { ticket: row.ticket }
  const url = '/ticket/delTicket'
  request.post({ url, data: param }).then(() => {
    loadTicketData()
  })
}

const editRow = row => {
  ticketEditor.value.edit(row, formatLinkAddr())
}

const finish = () => {
  close()
}
const loadTicketData = () => {
  const resourceId = props.resourceId
  const url = `/ticket/pager/${resourceId}/${state.paginationConfig.currentPage}/${state.paginationConfig.pageSize}`
  request.post({ url }).then(res => {
    state.tableData = res.data?.records || []
    state.paginationConfig.total = res.data.total
  })
}
const pageChange = index => {
  if (typeof index !== 'number') {
    return
  }
  state.paginationConfig.currentPage = index
  loadTicketData()
}
const sizeChange = size => {
  state.paginationConfig.pageSize = size
  state.paginationConfig.currentPage = 1
  loadTicketData()
}

const getLimit = () => {
  const url = '/ticket/limit'
  request.get({ url }).then(res => {
    const limit = res.data
    ticketLimit.value = limit
  })
}
onMounted(() => {
  getLimit()
  loadTicketData()
})
</script>

<style lang="less" scoped>
.ticket {
  height: auto;
  max-height: 560px;
  .ticket-model {
    display: flex;
    height: 22px;
    justify-content: space-between;
    padding: 0;
    .ticket-model-start {
      display: flex;
      align-items: center;
      color: #1f2329;
      font-family: var(--de-custom_font, 'PingFang');
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
          background-color: var(--ed-color-primary-1a, rgba(51, 112, 255, 0.1));
          color: var(--ed-color-primary);
          cursor: pointer;
        }
      }
    }
    .ticket-model-end {
      display: flex;
      align-items: center;
      label {
        height: 22px;
        margin-right: 8px;
      }
      .top-split {
        height: 18px;
        width: 1px;
        display: inline-block;
        background-color: #bbbfc4;
        margin: 0 20px;
      }
      .top-close {
        height: 22px;
        line-height: 22px;
        display: inline-flex;
        align-items: center;
        justify-content: center;
        color: #646a73;
        &:hover {
          cursor: pointer;
          background-color: #1f23291a;
          border-radius: 4px;
        }
        svg {
          width: 20px;
          height: 20px;
        }
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
    min-height: 156px;
    padding: 0 0;
    height: 50px;
    overflow-y: overlay;
    position: relative;
    height: calc(100% - 124px);

    :deep(.ticket-exp-head) {
      display: flex;
      line-height: 22px;
      height: 22px;
      align-items: center;
      .exp-tips-span {
        height: 18px;
        svg {
          margin-left: 4px;
          width: 16px;
          height: 16px;
          cursor: pointer;
        }
      }
    }
    :deep(.ticket-row) {
      display: flex;
      align-items: center;
      height: 22px;
      span {
        width: 126px;
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
    .popper-max-height {
      :deep(.ed-table--fit) {
        height: 438px !important;
      }
    }
  }
  :deep(.pagination-cont) {
    margin-top: 16px !important;
  }
  :deep(.is-last) {
    display: none;
  }
}
</style>
