<template>
  <div class="ticket">
    <div class="ticket-model">
      <div class="ticket-model-start">
        <el-tooltip class="item" effect="dark" :content="$t('link_ticket.back')" placement="top">
          <span class="back-tips">
            <el-icon class="custom-el-icon back-icon" @click="close">
              <Icon class="toolbar-icon" name="icon_left_outlined" />
            </el-icon>
          </span>
        </el-tooltip>
        <span class="ticket-title">{{ 'Ticket ' + $t('commons.setting') }}</span>
      </div>
      <div class="ticket-model-end">
        <el-checkbox v-model="requireTicket" @change="requireTicketChange" />
        <span>{{ $t('link_ticket.require') }}</span>
      </div>
    </div>
    <div class="ticket-table">
      <!-- <div class="text-add-ticket">
        <el-button class="de-text-btn mr2" type="text" icon="el-icon-plus" @click="addRow">{{
          $t('commons.create')
        }}</el-button>
      </div>

      <el-table :data="tableData" style="width: 100%" size="mini">
        <el-table-column prop="ticket" label="ticket" width="120">
          <template v-slot="scope">
            <div class="ticket-row">
              <span>{{ scope.row.ticket }}</span>
              <el-tooltip class="item" effect="dark" :content="$t('commons.copy')" placement="top">
                <span
                  v-clipboard:copy="`${props.linkUrl}?ticket=${scope.row.ticket}`"
                  v-clipboard:success="onCopy"
                  v-clipboard:error="onError"
                  class="copy-i"
                >
                  <svg-icon icon-class="de-icon-copy" />
                </span>
              </el-tooltip>
              <el-tooltip
                class="item"
                effect="dark"
                :content="`${$t('link_ticket.refresh')} ticket`"
                placement="top"
              >
                <span class="refresh-i">
                  <i class="el-icon-refresh-right" @click="refreshTicket(scope.row)" />
                </span>
              </el-tooltip>
            </div>
          </template>
        </el-table-column>

        <el-table-column prop="exp" :label="$t('panel.over_time')" width="100">
          <template v-slot:header>
            <span>{{ $t('panel.over_time') }}</span>
            <el-tooltip
              class="item"
              effect="dark"
              :content="$t('link_ticket.time_tips')"
              placement="top"
            >
              <span class="check-tips">
                <svg-icon icon-class="de-icon-info" @click="closeTicket" />
              </span>
            </el-tooltip>
          </template>
          <template v-slot="scope">
            <el-input
              v-if="scope.row.isEdit"
              v-model="scope.row.exp"
              type="number"
              :placeholder="$t('commons.input_content')"
              min="0"
              max="1440"
              size="small"
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
            <div class="ticket-op">
              <el-tooltip
                class="item"
                effect="dark"
                :content="$t('commons.delete')"
                placement="top"
              >
                <span>
                  <i class="el-icon-delete" @click="deleteTicket(scope.row, scope.$idnex)" />
                </span>
              </el-tooltip>
              <el-tooltip
                class="item"
                effect="dark"
                :content="scope.row.isEdit ? $t('commons.save') : $t('commons.edit')"
                placement="top"
              >
                <span>
                  <i v-if="!scope.row.isEdit" class="el-icon-edit" @click="editRow(scope.row)" />
                  <i
                    v-else
                    class="el-icon-circle-check"
                    @click="saveRow(scope.row, scope.$index)"
                  />
                </span>
              </el-tooltip>
            </div>
          </template>
        </el-table-column>
      </el-table> -->
    </div>
  </div>
</template>

<script lang="ts" setup>
import { ref, reactive } from 'vue'
import { propTypes } from '@/utils/propTypes'

const props = defineProps({
  linkUrl: propTypes.string.def(null)
})

const requireTicket = ref(false)
const tableData = reactive([])
const emits = defineEmits(['close'])

const close = () => {
  emits('close')
}

const requireTicketChange = val => {
  console.log(val)
}

const addRow = () => {
  console.log(11)
}

const refreshTicket = row => {
  console.log(row)
}
const closeTicket = () => {
  console.log(11)
}

const validateExp = (val, index) => {
  console.log(val)
  console.log(index)
}

const validateArgs = (val, index) => {
  console.log(val)
  console.log(index)
}

const deleteTicket = (val, index) => {
  console.log(val)
  console.log(index)
}

const saveRow = (val, index) => {
  console.log(val)
  console.log(index)
}
const editRow = row => {
  console.log(row)
}
const onCopy = e => {
  console.log(e)
}
const onError = e => {
  console.log(e)
}
</script>

<style lang="less" scoped>
.ticket {
  height: 261px;
  .ticket-model {
    display: flex;
    justify-content: space-between;
    padding: 16px 0;
    .ticket-model-start {
      display: flex;
      color: #1f2329;
      font-family: PingFang SC;
      font-weight: 500;
      font-size: 14px;
      .ticket-title {
        font-size: 14px;
      }
      .back-tips {
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

    label {
      margin-right: 8px;
    }
  }
  .ticket-table {
    padding: 0 0;
    // height: 260px;
    height: 50px;
    overflow-y: overlay;
    position: relative;
    ::v-deep .error-msg {
      color: red;
      position: fixed;
      z-index: 9;
      font-size: 10px;
      height: 10px;
    }
    ::v-deep .check-tips {
      margin-left: 4px;
    }
    .text-add-ticket {
      width: 48px;
      height: 22px;
      gap: 4px;
    }
  }
}
</style>
