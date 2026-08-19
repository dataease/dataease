import { reactive } from "vue";
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

export const filterOption = reactive([
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
    option: [],
    field: "oid",
    title: t("sysuser.org"),
    operate: "in",
    property: {
      multiple: false,
      checkStrictly: true,
      showCheckbox: false,
      placeholder: t("auth.sysParams_type.dept"),
    },
  },

  {
    type: "select",
    option: [],
    field: "roleIdList",
    title: t("commons.role"),
    operate: "in",
    disabled: true,
    property: {
      customPlaceholder: t("org.select_org_first_top"),
    },
  },
]);

export const groupBy = (list: any[]) => {
  const map = new Map();
  list.forEach((item) => {
    const root = item.root;
    let arr = map.get(root);
    if (!arr) {
      arr = [];
    }
    arr.push({
      id: item.id,
      name: item.name,
      root: item.root,
      readonly: item.readonly,
    });
    map.set(root, arr);
  });
  return map;
};
