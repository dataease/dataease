import MsTableSearchInput from "./MsTableSearchInput";
import MsTableSearchDateTimePicker from "./MsTableSearchDateTimePicker";
import MsTableSearchDatePicker from "./MsTableSearchDatePicker";
import MsTableSearchSelect from "./MsTableSearchSelect";

export default {
  MsTableSearchInput, MsTableSearchDatePicker, MsTableSearchDateTimePicker, MsTableSearchSelect
}

export const OPERATORS = {
  LIKE: {
    label: "commons.adv_search.operators.like",
    value: "like"
  },
  NOT_LIKE: {
    label: "commons.adv_search.operators.not_like",
    value: "not like"
  },
  IN: {
    label: "commons.adv_search.operators.in",
    value: "in"
  },
  NOT_IN: {
    label: "commons.adv_search.operators.not_in",
    value: "not in"
  },
  GT: {
    label: "commons.adv_search.operators.gt",
    value: "gt"
  },
  GE: {
    label: "commons.adv_search.operators.ge",
    value: "ge"
  },
  LT: {
    label: "commons.adv_search.operators.lt",
    value: "lt"
  },
  LE: {
    label: "commons.adv_search.operators.le",
    value: "le"
  },
  EQ: {
    label: "commons.adv_search.operators.equals",
    value: "eq"
  },
  BETWEEN: {
    label: "commons.adv_search.operators.between",
    value: "between"
  },
  CURRENT_USER: {
    label: "commons.adv_search.operators.current_user",
    value: "current user"
  },
}

export const NAME = {
  key: "name", // 返回结果Map的key
  name: 'MsTableSearchInput', // Vue控件名称
  label: 'commons.name', // 显示名称
  operator: { // 运算符设置
    value: OPERATORS.LIKE.value, // 如果未设置value初始值，则value初始值为options[0]
    options: [OPERATORS.LIKE, OPERATORS.NOT_LIKE] // 运算符候选项
  },
}

export const UPDATE_TIME = {
  key: "updateTime",
  name: 'MsTableSearchDateTimePicker',
  label: 'commons.update_time',
  operator: {
    options: [OPERATORS.BETWEEN, OPERATORS.GT, OPERATORS.GE, OPERATORS.LT, OPERATORS.LE, OPERATORS.EQ]
  },
}
export const PROJECT_NAME = {
  key: "projectName",
  name: 'MsTableSearchInput',
  label: 'commons.adv_search.project',
  operator: {
    options: [OPERATORS.LIKE, OPERATORS.NOT_LIKE]
  },
}
export const TEST_NAME = {
  key: "testName",
  name: 'MsTableSearchInput',
  label: 'commons.adv_search.test',
  operator: {
    options: [OPERATORS.LIKE, OPERATORS.NOT_LIKE]
  },
}
export const TEST_PLAN_NAME = {
  key: "testPlanName",
  name: 'MsTableSearchInput',
  label: 'test_track.report.list.test_plan',
  operator: {
    options: [OPERATORS.LIKE, OPERATORS.NOT_LIKE]
  },
}
export const CREATE_TIME = {
  key: "createTime",
  name: 'MsTableSearchDateTimePicker',
  label: 'commons.create_time',
  operator: {
    options: [OPERATORS.BETWEEN, OPERATORS.GT, OPERATORS.GE, OPERATORS.LT, OPERATORS.LE, OPERATORS.EQ]
  },
}

export const STATUS = {
  key: "status",
  name: 'MsTableSearchSelect',
  label: 'commons.status',
  operator: {
    options: [OPERATORS.IN, OPERATORS.NOT_IN]
  },
  options: [
    {label: "Saved", value: "Saved"}, {label: "Starting", value: "Starting"},
    {label: "Running", value: "Running"}, {label: "Reporting", value: "Reporting"},
    {label: "Completed", value: "Completed"}, {label: "Error", value: "Error"},
    {label: "Success", value: "Success"}
  ],
  props: { // 尾部控件的props，一般为element ui控件的props
    multiple: true
  }
}

export const API_STATUS = {
  key: "status",
  name: 'MsTableSearchSelect',
  label: 'commons.status',
  operator: {
    options: [OPERATORS.IN, OPERATORS.NOT_IN]
  },
  options: [
    {value: 'Prepare', label: '未开始'},
    {value: 'Underway', label: '进行中'},
    {value: 'Completed', label: '已完成'}
  ],
  props: { // 尾部控件的props，一般为element ui控件的props
    multiple: true
  }
}
export const API_CASE_PRIORITY = {
  key: "priority",
  name: 'MsTableSearchSelect',
  label: 'test_track.case.priority',
  operator: {
    options: [OPERATORS.IN, OPERATORS.NOT_IN]
  },
  options: [
    {value: 'P0', label: 'P0'},
    {value: 'P1', label: 'P1'},
    {value: 'P2', label: 'P2'},
    {value: 'P3', label: 'P3'}
  ],
  props: { // 尾部控件的props，一般为element ui控件的props
    multiple: true
  }
}
export const API_CASE_RESULT = {
  key: "status",
  name: 'MsTableSearchSelect',
  label: 'test_track.plan_view.execute_result',
  operator: {
    options: [OPERATORS.IN, OPERATORS.NOT_IN]
  },
  options: [
    {value: 'success', label: 'api_test.automation.success'},
    {value: 'error', label: 'api_test.automation.fail'}
  ],
  props: { // 尾部控件的props，一般为element ui控件的props
    multiple: true
  }
}

export const API_SCENARIO_RESULT = {
  key: "status",
  name: 'MsTableSearchSelect',
  label: 'test_track.plan_view.execute_result',
  operator: {
    options: [OPERATORS.IN, OPERATORS.NOT_IN]
  },
  options: [
    {value: 'Success', label: 'api_test.automation.success'},
    {value: 'Fail', label: 'api_test.automation.fail'}
  ],
  props: { // 尾部控件的props，一般为element ui控件的props
    multiple: true
  }
}

export const API_METHOD = {
  key: "method",
  name: 'MsTableSearchSelect',
  label: 'api_test.definition.api_type',
  operator: {
    options: [OPERATORS.IN, OPERATORS.NOT_IN]
  },
  options: [
    {value: 'GET', label: 'GET'},
    {value: 'POST', label: 'POST'},
    {value: 'PUT', label: 'PUT'},
    {value: 'PATCH', label: 'PATCH'},
    {value: 'DELETE', label: 'DELETE'},
    {value: 'OPTIONS', label: 'OPTIONS'},
    {value: 'HEAD', label: 'HEAD'},
    {value: 'CONNECT', label: 'CONNECT'},
    {value: 'DUBBO', label: 'DUBBO'},
    {value: 'dubbo://', label: 'dubbo://'},
    {value: 'SQL', label: 'SQL'},
    {value: 'TCP', label: 'TCP'}
  ],
  props: { // 尾部控件的props，一般为element ui控件的props
    multiple: true
  }
}

export const API_PATH = {
  key: "path", // 返回结果Map的key
  name: 'MsTableSearchInput', // Vue控件名称
  label: 'api_test.definition.api_path', // 显示名称
  operator: { // 运算符设置
    value: OPERATORS.LIKE.value, // 如果未设置value初始值，则value初始值为options[0]
    options: [OPERATORS.LIKE, OPERATORS.NOT_LIKE] // 运算符候选项
  },
}

export const API_TAGS = {
  key: "tags", // 返回结果Map的key
  name: 'MsTableSearchInput', // Vue控件名称
  label: 'commons.tag', // 显示名称
  operator: { // 运算符设置
    value: OPERATORS.LIKE.value, // 如果未设置value初始值，则value初始值为options[0]
    options: [OPERATORS.LIKE, OPERATORS.NOT_LIKE] // 运算符候选项
  },
}

export const CREATOR = {
  key: "creator",
  name: 'MsTableSearchSelect',
  label: 'api_test.creator',
  operator: {
    options: [OPERATORS.IN, OPERATORS.NOT_IN, OPERATORS.CURRENT_USER],
    change: function (component, value) { // 运算符change事件
      if (value === OPERATORS.CURRENT_USER.value) {
        component.value = value;
      }
    }
  },
  options: { // 异步获取候选项
    url: "/user/list",
    labelKey: "name",
    valueKey: "id",
    showLabel: option => {
      return option.label + "(" + option.value + ")";
    }
  },
  props: {
    multiple: true
  },
  isShow: operator => {
    return operator !== OPERATORS.CURRENT_USER.value;
  }
}

export const EXECUTOR = {
  key: "executor",
  name: 'MsTableSearchSelect',
  label: 'test_track.plan_view.executor',
  operator: {
    options: [OPERATORS.IN, OPERATORS.NOT_IN, OPERATORS.CURRENT_USER],
    change: function (component, value) { // 运算符change事件
      if (value === OPERATORS.CURRENT_USER.value) {
        component.value = value;
      }
    }
  },
  options: { // 异步获取候选项
    url: "/user/list",
    labelKey: "name",
    valueKey: "id",
    showLabel: option => {
      return option.label + "(" + option.value + ")";
    }
  },
  props: {
    multiple: true
  },
  isShow: operator => {
    return operator !== OPERATORS.CURRENT_USER.value;
  }
}

export const TRIGGER_MODE = {
  key: "triggerMode",
  name: 'MsTableSearchSelect',
  label: 'commons.trigger_mode.name',
  operator: {
    options: [OPERATORS.IN, OPERATORS.NOT_IN]
  },
  options: [
    {label: "commons.trigger_mode.manual", value: "MANUAL"},
    {label: "commons.trigger_mode.schedule", value: "SCHEDULE"},
    {label: "commons.trigger_mode.api", value: "API"}
  ],
  props: {
    multiple: true
  }
}

export const PRIORITY = {
  key: "priority",
  name: 'MsTableSearchSelect',
  label: "test_track.case.priority",
  operator: {
    options: [OPERATORS.IN, OPERATORS.NOT_IN]
  },
  options: [
    {label: "P0", value: "P0"},
    {label: "P1", value: "P1"},
    {label: "P2", value: "P2"},
    {label: "P3", value: "P3"},
  ],
  props: {
    multiple: true
  }
}

export const TYPE = {
  key: "type",
  name: 'MsTableSearchSelect',
  label: "test_track.case.type",
  operator: {
    options: [OPERATORS.IN, OPERATORS.NOT_IN]
  },
  options: [
    {label: 'commons.functional', value: 'functional'},
    {label: 'commons.performance', value: 'performance'},
    {label: 'commons.api', value: 'api'}
  ],
  props: {
    multiple: true
  }
}

export const METHOD = {
  key: "method",
  name: 'MsTableSearchSelect',
  label: "test_track.case.method",
  operator: {
    options: [OPERATORS.IN, OPERATORS.NOT_IN]
  },
  options: [
    {label: 'test_track.case.manual', value: 'manual'},
    {label: 'test_track.case.auto', value: 'auto'}
  ],
  props: {
    multiple: true
  }
}

export const MODULE = {
  key: "module",
  name: 'MsTableSearchInput',
  label: "test_track.case.module",
  operator: {
    value: OPERATORS.LIKE.value,
    options: [OPERATORS.LIKE, OPERATORS.NOT_LIKE]
  },
}

export const PRINCIPAL = {
  key: "principal",
  name: 'MsTableSearchSelect',
  label: 'test_track.plan.plan_principal',
  operator: {
    options: [OPERATORS.IN, OPERATORS.NOT_IN, OPERATORS.CURRENT_USER],
    change: function (component, value) { // 运算符change事件
      if (value === OPERATORS.CURRENT_USER.value) {
        component.value = value;
      }
    }
  },
  options: { // 异步获取候选项
    url: "/user/list",
    labelKey: "name",
    valueKey: "id",
    showLabel: option => {
      return option.label + "(" + option.value + ")";
    }
  },
  props: {
    multiple: true
  },
  isShow: operator => {
    return operator !== OPERATORS.CURRENT_USER.value;
  }
};

export const STAGE = {
  key: "stage",
  name: 'MsTableSearchSelect',
  label: "test_track.plan.plan_stage",
  operator: {
    options: [OPERATORS.IN, OPERATORS.NOT_IN]
  },
  options: [
    {label: 'test_track.plan.smoke_test', value: 'smoke'},
    {label: 'test_track.plan.regression_test', value: 'regression'},
    {label: 'test_track.plan.system_test', value: 'system'}
  ],
  props: {
    multiple: true
  }
};

export const TEST_PLAN_STATUS = {
  key: "status",
  name: 'MsTableSearchSelect',
  label: "test_track.plan.plan_status",
  operator: {
    options: [OPERATORS.IN, OPERATORS.NOT_IN]
  },
  options: [
    {label: 'test_track.plan.plan_status_prepare', value: 'Prepare'},
    {label: 'test_track.plan.plan_status_running', value: 'Underway'},
    {label: 'test_track.plan.plan_status_completed', value: 'Completed'}
  ],
  props: {
    multiple: true
  }
};

export const TEST_PLAN_TRIGGER_MODE = {
  key: "triggerMode",
  name: 'MsTableSearchSelect',
  label: "test_track.report.list.trigger_mode",
  operator: {
    options: [OPERATORS.IN, OPERATORS.NOT_IN]
  },
  options: [
    {label: 'test_track.report.trigger_mode.manual', value: 'manual'},
    {label: 'test_track.report.trigger_mode.automation', value: 'automation'},
  ],
  props: {
    multiple: true
  }
};

export const TEST_CONFIGS = [NAME, UPDATE_TIME, CREATE_TIME, STATUS, CREATOR];

export const REPORT_CONFIGS = [NAME, TEST_NAME, CREATE_TIME, STATUS, CREATOR, TRIGGER_MODE];

export const TEST_CASE_CONFIGS = [NAME, API_TAGS, MODULE, PRIORITY, CREATE_TIME, TYPE, UPDATE_TIME, METHOD, CREATOR, EXECUTOR];

export const TEST_PLAN_CONFIGS = [NAME, UPDATE_TIME, CREATE_TIME, PRINCIPAL, TEST_PLAN_STATUS, STAGE];

export const API_DEFINITION_CONFIGS = [NAME, API_METHOD, API_PATH, API_STATUS, API_TAGS, UPDATE_TIME, CREATE_TIME, CREATOR];

export const API_CASE_CONFIGS = [NAME, API_CASE_PRIORITY, API_TAGS, API_CASE_RESULT, UPDATE_TIME, CREATE_TIME, CREATOR];

export const API_SCENARIO_CONFIGS = [NAME, API_CASE_PRIORITY, API_TAGS, API_SCENARIO_RESULT, UPDATE_TIME, CREATE_TIME, CREATOR];

export const TEST_PLAN_REPORT_CONFIGS = [NAME, TEST_PLAN_NAME,CREATOR, CREATE_TIME, TEST_PLAN_TRIGGER_MODE, TEST_PLAN_STATUS];
