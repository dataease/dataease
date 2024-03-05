<template>
  <InfoTemplate
    ref="infoTemplate"
    setting-key="basic"
    showValidate
    style="padding-bottom: 0"
    setting-title="引擎设置"
    :setting-data="templateList"
    @edit="edit"
    @check="validateById"
  />
  <div>
    <span class="de-expand-engine" @click="showPriority = !showPriority">
      {{ t('datasource.priority') }}
      <el-icon>
        <Icon :name="showPriority ? 'icon_down_outlined' : 'icon_down_outlined-1'"></Icon>
      </el-icon>
    </span>
  </div>
  <InfoTemplate
    v-if="showPriority"
    ref="infoTemplateTime"
    style="padding-top: 0"
    hide-head
    :setting-data="templateListTime"
  />
</template>

<script lang="ts" setup>
import { ref, nextTick } from 'vue'
import { SettingRecord } from '@/views/system/common/SettingTemplate'
import { ElMessage } from 'element-plus-secondary'
import { useI18n } from '@/hooks/web/useI18n'
import InfoTemplate from '@/views/system/common/InfoTemplate.vue'
import { dsTypes } from '@/views/visualized/data/datasource/form/option'
import { getDeEngine } from '@/api/datasource'
import request from '@/config/axios'
const { t } = useI18n()
const typeMap = dsTypes.reduce((pre, next) => {
  pre[next.type] = next.name
  return pre
}, {})
const showPriority = ref(true)
let nodeInfoId
const infoTemplate = ref()
const infoTemplateTime = ref()
const templateList = ref<SettingRecord[]>([])
const templateListTime = ref<SettingRecord[]>([])
const getEngine = () => {
  getDeEngine().then(res => {
    let { id, type, configuration } = res.data
    if (configuration) {
      configuration = JSON.parse(configuration)
    }

    nodeInfoId = id

    templateListTime.value = [
      {
        pkey: 'datasource.initial_pool_size',
        pval: configuration?.initialPoolSize || 5,
        type: '',
        sort: 0
      },
      {
        pkey: 'datasource.min_pool_size',
        pval: configuration?.minPoolSize || 5,
        type: '',
        sort: 0
      },
      {
        pkey: 'datasource.max_pool_size',
        pval: configuration?.maxPoolSize || 5,
        type: '',
        sort: 0
      },
      {
        pkey: 'datasource.query_timeout',
        pval: `${configuration?.queryTimeout || 30}${t('common.second')}`,
        type: '',
        sort: 0
      }
    ]

    templateList.value = [
      {
        pkey: '引擎类型',
        pval: typeMap[type],
        type: '',
        sort: 0
      },
      {
        pkey: 'datasource.host',
        pval: configuration?.host,
        type: '',
        sort: 0
      },
      {
        pkey: 'datasource.port',
        pval: configuration?.port,
        type: '',
        sort: 0
      },
      {
        pkey: 'datasource.data_base',
        pval: configuration?.dataBase,
        type: '',
        sort: 0
      },
      {
        pkey: 'datasource.user_name',
        pval: configuration?.username,
        type: '',
        sort: 0
      },
      {
        pkey: 'datasource.extra_params',
        pval: configuration?.extraParams,
        type: '',
        sort: 0
      }
    ]
    nextTick(() => {
      infoTemplate.value.init()
      infoTemplateTime.value.init()
    })
  })
}
getEngine()

defineExpose({
  getEngine
})
const emits = defineEmits(['edit'])
const edit = () => {
  emits('edit')
}

const validateById = async () => {
  request.post({ url: '/engine/validate/' + nodeInfoId }).then(res => {
    if (res !== undefined) {
      ElMessage.success(t('datasource.validate_success'))
    }
  })
}
</script>
<style lang="less">
.de-expand-engine {
  font-family: '阿里巴巴普惠体 3.0 55 Regular L3';
  font-size: 14px;
  font-weight: 400;
  line-height: 22px;
  color: var(--ed-color-primary);
  cursor: pointer;
  margin: 0 24px 10px 24px;
  display: inline-flex;
  align-items: center;

  .ed-icon {
    margin-left: 4px;
  }
}
</style>
