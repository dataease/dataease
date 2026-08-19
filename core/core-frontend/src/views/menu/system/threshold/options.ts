import { useI18n } from "@/hooks/web/useI18n";
const { t } = useI18n();

export const filterOption = [
  {
    type: "enum",
    option: [
      {
        id: "dashboard",
        name: t("auth.panel"),
      },
      {
        id: "dataV",
        name: t("auth.screen"),
      },
    ],
    field: "resourceTypeList",
    title: t("org.resource_type"),
    operate: "in",
  },
  {
    type: "enum",
    option: [
      {
        id: 1,
        name: t('chart.open'),
      },
      {
        id: 0,
        name: t('chart.close'),
      },
    ],
    field: "enableList",
    title: t('threshold_warn.warn_status'),
    operate: "in",
  },
  {
    type: "time",
    option: [],
    property: {
      showType: "datetimerange",
      format: "YYYY-MM-DD HH:mm:ss",
      valueFormat: "x",
      rangeSeparator: "-",
      startPlaceholder: t('datasource.start_time'),
      endPlaceholder: t('datasource.end_time'),
    },
    field: "timeList",
    title: t('common.create_time'),
    operate: "between",
  },
];

export interface TaskParam {
  taskId?: string;
  taskName?: string;
}
