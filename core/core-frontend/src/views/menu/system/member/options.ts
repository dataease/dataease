import { useI18n } from "@/hooks/web/useI18n";
const { t } = useI18n();

export interface Tree {
  id: string;
  name: string;
  readonly: boolean;
  children?: Tree[];
  disabled: boolean;
  root: boolean;
}

export const columnNames = [
  {
    label: "ID",
    props: "account",
  },
  {
    label: "user.name",
    props: "name",
  },
  {
    label: "user.role",
    props: "roleItems",
  },
  {
    label: "common.email",
    props: "email",
  },

  {
    label: "user.state",
    props: "enable",
  },
  {
    label: "common.create_time",
    props: "createTime",
  },
];

export const filterOption = [
  {
    type: "enum",
    option: [
      {
        id: true,
        name: t("commons.enable"),
      },
      {
        id: false,
        name: t("commons.disable"),
      },
    ],
    field: "statusList",
    title: t("user.state"),
    operate: "in",
  },

  {
    type: "select",
    option: [
      { id: "0", name: "LOCAL" },
      { id: 1, name: "LDAP" },
      { id: 2, name: "OIDC" },
      { id: 3, name: "CAS" },
      { id: 9, name: "OAuth2" },
      { id: 10, name: "Saml2" },
      { id: 4, name: t("user.feishu") },
      { id: 5, name: t("user.dingtalk") },
      { id: 6, name: t("user.wechat_for_business") },
      { id: 7, name: t("user.international_feishu") },
    ],
    field: "originList",
    title: t("auth.sysParams_type.user_source"),
    operate: "in",
    property: {
      placeholder: t("auth.sysParams_type.user_source"),
    },
  },

  {
    type: "tree-select",
    option: [
      {
        value: "admin",
        label: t("role.org_admin"),
        children: [],
        disabled: false,
      },
      {
        value: "readonly",
        label: t("role.average_role"),
        children: [],
        disabled: false,
      },
    ],
    field: "roleIdList",
    title: t("commons.role"),
    operate: "in",
  },
];

export const groupBy = (list: Tree[]) => {
  const map = new Map();
  list.forEach((item) => {
    const readonly = item.readonly;
    let arr = map.get(readonly);
    if (!arr) {
      arr = [];
    }
    arr.push({ value: item.id, label: item.name, disabled: false });
    map.set(readonly, arr);
  });
  return map;
};
