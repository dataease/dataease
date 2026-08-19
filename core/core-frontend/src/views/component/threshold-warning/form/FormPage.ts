import { useI18n } from '@/hooks/web/useI18n'
const { t } = useI18n()
import { reactive } from 'vue'

export const rateTypeOptions = [
  { value: 0, label: t('threshold.once_a_hour') },
  { value: 1, label: t('threshold.once_a_day') },
  { value: 2, label: t('threshold.once_a_week') },
  { value: 3, label: t('threshold.once_a_month') },
]

export const weekTypeOptions = Array(7).fill(1).map((ele, index) => {
  return {
    label: `report.week_${['mon', 'tue', 'wed', 'thu', 'fri', 'sat', 'sun'][index]}`,
    value: index + 1
  }
})

export const dateTypeOptions = Array(31).fill(1).map((ele, index) => {
  return {
    label: `${index + 1}`,
    value: index + 1
  }
})

export const baseRules = reactive({
  name: [
    {
      required: true,
      trigger: ['blur', 'change'],
      message: t("commons.cannot_be_null"),
    },
    {
      min: 1,
      max: 50,
      message: t("commons.input_limit", [1, 50]),
      trigger: ['blur', 'change'],
    }
  ],

  rateType: [
    {
      required: true,
      trigger: ['blur', 'change'],
      message: t("commons.cannot_be_null"),
    }
  ],

})

export const reciRules = reactive({
  reciFlagList: [
    {
      required: false,
      trigger: ['blur', 'change'],
      message: t("commons.cannot_be_null"),
    }
  ]
})

export interface LarkGroupItem {
  chat_id: string
  name: string
}

export interface ReciOption {
  id: string
  name: string
  account?: string
  flag: number
}

export interface PlatformCategory {
  lark?: boolean
  dingtalk?: boolean
  wecom?: boolean
  larksuite?: boolean
}

export const platformOptions = [
  { value: 1, name: t('threshold.email'), flag: 'email' },
  { value: 2, name: t('threshold.wecom'), flag: 'wecom' },
  { value: 3, name: t('threshold.dingtalk'), flag: 'dingtalk' },
  { value: 4, name: t('threshold.lark'), flag: 'lark' },
  { value: 5, name: t('user.international_feishu'), flag: 'larksuite' },
]
export const quotaTemplate = '`<span id="changeText-${row.id}" style="background: #00D6B933; color: #04b49c;"><span class="mceNonEditable" contenteditable="false" data-mce-content="[${row.name}]">[${row.name}]</span></span>`'
export const contentTemplate = '`<p><span style="font-family: \'PingFang SC\'; font-size: 14px; background-color: #ffffff;">' + t("cron.to") + ' <span id="changeText-0" style="background: #3370FF33; color: #2b5fd9;"><span class="mceNonEditable" contenteditable="false" data-mce-content="[检测时间]">[检测时间]</span></span><span id="attachValue">&nbsp;</span>, ' + t('threshold.attention_quota_tip') + ' ( ${quota} ) , ' + t('threshold.trigger_alarm') + '： <span id="changeText-1" style="background: #3370FF33; color: #2b5fd9;"><span class="mceNonEditable" contenteditable="false" data-mce-content="[触发告警]">[触发告警]</span></span><span id="attachValue">&nbsp;</span>' + t('threshold.pay_attention_in_time') + '</span></p><p><span id="changeText-2" style="background: #3370FF33; color: #2b5fd9;"><span class="mceNonEditable" contenteditable="false" data-mce-content="[告警数据]">[告警数据]</span></span></p>`'
