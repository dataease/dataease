import { useI18n } from '@/hooks/web/useI18n'
const { t } = useI18n()
import { reactive } from 'vue'


const pixelValidator = (rule, value, callback) => {
  if (!value || value.length === 0) {
    callback(new Error(t("commons.cannot_be_null")));
  } else if (!validPixel(value)) {
    callback(new Error(t("report.form.pixel_error")));
  } else {
    callback();
  }
}
  
const validPixel = (val) => {
  const arr = val.split("*");
  if (!arr || arr.length !== 2) return false;
  try {
      const x = parseInt(arr[0].trim());
      const y = parseInt(arr[1].trim());
      return x < 10000 && x > 800 && y < 6250 && y > 500;
  } catch (error) {
      return false;
  }
}

export const pixelOptions = [
  {
    label: "Windows(16:9)",
    options: [
      {
        value: "1920 * 1080",
        label: "1920 * 1080",
      },
      {
        value: "1600 * 900",
        label: "1600 * 900",
      },
      {
        value: "1280 * 720",
        label: "1280 * 720",
      },
    ],
  },
  {
    label: "MacOS(16:10)",
    options: [
      {
        value: "2560 * 1600",
        label: "2560 * 1600",
      },
      {
        value: "1920 * 1200",
        label: "1920 * 1200",
      },
      {
        value: "1680 * 1050",
        label: "1680 * 1050",
      },
    ],
  },
]

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
  title: [
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
  rid: [
    {
      required: true,
      trigger: ['blur', 'change'],
      message: t("commons.cannot_be_null"),
    }
  ],
  pixel: [
    {
      required: false,
      trigger: ['blur', 'change'],
      validator: pixelValidator,
    },
    {
      required: true,
      trigger: ['blur', 'change'],
      message: t("commons.cannot_be_null"),
    }
  ]
})

export const reciRules = reactive({
  reciFlagList: [
    {
      required: true,
      trigger: ['blur', 'change'],
      message: t("commons.cannot_be_null"),
    }
  ]
})

export const startPickerOptions = {
  disabledDate: (time) => {
    return time.getTime() < Date.now() - 8.64e7;
  }
}

export interface SelectOption {
  value: number
  label: string
}

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

export const timeType = [{
  label: 'report.once_a_day',
  value: 1
}, {
  label: 'report.once_a_week',
  value: 2
}, {
  label: 'report.once_a_month',
  value: 3
},]

export const simpleTimeType = [{
  label: 'report.hour',
  value: -1
}, {
  label: 'report.day',
  value: 0
}, {
  label: 'report.week',
  value: 1
}, {
  label: 'report.month',
  value: 2
}]

export const weekType = Array(7).fill(1).map((ele, index) => {
  return {
      label: `report.week_${['mon', 'tue', 'wed', 'thu', 'fri', 'sat', 'sun'][index]}`,
      value: index + 1
  }
})

export const monthType = Array(31).fill(1).map((ele, index) => {
  return {
      label: `${index + 1}`,
      value: index + 1
  }
})

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
