<script setup lang="ts">
import { propTypes } from '@/utils/propTypes'
import { ElSelect, ElPopover, ElOption, ElIcon } from 'element-plus-secondary'
import { computed, reactive, nextTick, ref } from 'vue'
import { Icon } from '@/components/icon-custom'

const props = defineProps({
  statusList: propTypes.arrayOf(
    propTypes.shape({
      id: propTypes.string,
      name: propTypes.string
    })
  ),
  title: propTypes.string
})

const state = reactive({
  currentStatus: [],
  activeStatus: []
})

const elPopoverU = ref(null)
const more = ref(null)

const statusChange = (id: string | number) => {
  state.activeStatus = state.activeStatus.filter(ele => ele.id !== id)
}
const selectStatus = ids => {
  const [item] = ids
  state.activeStatus.push(item)
  state.currentStatus = []
  nextTick(() => {
    elPopoverU.value.hide()
    more.value.click()
  })
}

const statusListNotSelect = computed(() => {
  return props.statusList.filter(ele => !state.activeStatus.map(t => t.id).includes(ele.id))
})

defineExpose({
  activeStatus: state.activeStatus
})
</script>

<template>
  <div class="filter">
    <span>{{ title }}</span>
    <div class="filter-item">
      <span
        v-for="ele in state.activeStatus"
        :key="ele.id"
        class="item active"
        @click="statusChange(ele.id)"
        >{{ $t(ele.name) }}</span
      >
      <slot v-if="!!statusListNotSelect.length">
        <el-popover
          :show-arrow="false"
          ref="elPopoverU"
          placement="bottom"
          popper-class="filter-popper"
          width="200"
          trigger="click"
        >
          <el-select
            :teleported="false"
            style="width: 100%"
            v-model="state.currentStatus"
            value-key="id"
            filterable
            multiple
            @change="selectStatus"
          >
            <el-option
              v-for="item in statusListNotSelect"
              :key="item.name"
              :label="item.name"
              :value="item"
            />
          </el-select>
          <template #reference>
            <span ref="more" class="more">
              <el-icon>
                <Icon name="icon_add_outlined"> </Icon>
              </el-icon>
              更多
            </span>
          </template>
        </el-popover>
      </slot>
    </div>
  </div>
</template>
<style lang="less" scope>
.filter {
  display: flex;
  min-height: 46px;

  > :nth-child(1) {
    color: var(--deTextSecondary, #1f2329);
    font-family: 'PingFang SC';
    font-style: normal;
    font-weight: 400;
    font-size: 14px;
    line-height: 24px;
    white-space: nowrap;
    width: 116px;
  }

  .filter-item {
    flex: 1;

    .item,
    .more {
      font-family: PingFang SC;
      white-space: nowrap;
      font-size: 14px;
      font-weight: 400;
      line-height: 24px;
      margin-right: 12px;
      text-align: center;
      padding: 1px 6px;
      background: var(--deTextPrimary5, #f5f6f7);
      color: var(--deTextPrimary, #1f2329);
      border-radius: 2px;
      cursor: pointer;
      display: inline-block;
      margin-bottom: 12px;
    }

    .active,
    .more:hover {
      background: var(--primary10, rgba(51, 112, 255, 0.1));
      color: var(--primaryselect, #0c296e);
    }

    .more {
      white-space: nowrap;
      display: inline-flex;
      align-items: center;
      i {
        margin-right: 5px;
      }
    }
  }
}
</style>
<style lang="less">
.filter-popper {
  padding: 0 !important;
  background: #fff !important;
}
</style>
