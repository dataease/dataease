<script lang="ts" setup>
import icon_sync_datasource from '@/assets/svg/icon_sync_datasource.svg'
import icon_sync_task_number from '@/assets/svg/icon_sync_task_number.svg'
import icon_sync_log_number from '@/assets/svg/icon_sync_log_number.svg'
import LogPie from '@/views/menu/system/sync/summary/LogPie.vue'
import LogLine from '@/views/menu/system/sync/summary/LogLine.vue'
import { computed, onMounted, ref } from 'vue'
import { getResourceCount } from '@/api/sync/syncSummary'
import { Icon } from '@/components/icon-custom'
import { useI18n } from '@/hooks/web/useI18n'
const jobCount = ref('0')
const dataSourceCount = ref('0')
const jobLogCount = ref('0')
const { t } = useI18n()
/**
 * 格式化数字,添加千位逗号
 */
const addCommas = (num: number) => {
  if (num === null || num === undefined) {
    return '0'
  }
  return `${num}`.replace(/\B(?=(\d{3})+(?!\d))/g, ',')
}
const getCount = () => {
  getResourceCount().then(res => {
    jobCount.value = addCommas(res.jobCount)
    dataSourceCount.value = addCommas(res.datasourceCount)
    jobLogCount.value = addCommas(res.jobLogCount)
  })
}

const cardList = computed(() => {
  return [
    {
      title: t('sync_summary.data_source_number'),
      icon: icon_sync_datasource,
      count: dataSourceCount.value,
      color: 'red'
    },
    {
      title: t('sync_summary.task_number'),
      icon: icon_sync_task_number,
      count: jobCount.value,
      color: 'green'
    },
    {
      title: t('sync_summary.execution_count'),
      icon: icon_sync_log_number,
      count: jobLogCount.value,
      color: 'green'
    }
  ]
})
onMounted(() => {
  getCount()
})
</script>
<template>
  <el-row :gutter="24" class="row-class">
    <el-col :span="8" :key="item.color" v-for="item in cardList">
      <el-card shadow="never">
        <div class="item-creation">
          <div class="icon">
            <el-icon>
              <Icon :color="item.color" :width="40" :height="40"
                ><component
                  :is="item.icon"
                  class="svg-icon"
                  :style="{ color: item.color, width: '40px', height: '40px' }"
                ></component
              ></Icon>
            </el-icon>
          </div>
          <div class="item">
            <span class="title">
              {{ item.title }}
            </span>
            <span class="count">
              {{ item.count }}
            </span>
          </div>
        </div>
      </el-card>
    </el-col>
  </el-row>
  <el-row :gutter="24">
    <el-col :span="16">
      <el-card shadow="never" class="box-card">
        <LogLine />
      </el-card>
    </el-col>
    <el-col :span="8">
      <el-card shadow="never" class="box-card">
        <LogPie />
      </el-card>
    </el-col>
  </el-row>
</template>
<style scoped lang="less">
:deep(.ed-card__body) {
  padding: 24px;
}
.row-class {
  margin-top: 12px;
}
.ed-card {
  margin-bottom: 24px;
  text-align: center;
  border: none;
}

.item-creation {
  display: flex;
  flex-wrap: wrap;
  align-items: end;
  .icon {
    height: 54px;
    font-size: 40px;
    margin-right: 12px;
    margin-top: auto;
  }
  .item {
    font-family: var(--de-custom_font, 'PingFang');
    font-style: normal;
    display: flex;
    flex-direction: column;
    .title {
      display: flex;
      justify-content: flex-start;
      color: #646a73;
      font-weight: 400;
      line-height: 22px;
    }

    .count {
      display: flex;
      justify-content: flex-start;
      margin-top: 4px;
      color: #1f2329;
      font-size: 20px;
      font-weight: 500;
      line-height: 28px;
      letter-spacing: -0.2px;
    }
  }
}
</style>
