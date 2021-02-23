export const TokenKey = 'Admin-Token';
export const LicenseKey = 'License';
export const DEFAULT_LANGUAGE = 'default_language';

export const ROLE_ADMIN = 'admin';
export const ROLE_ORG_ADMIN = 'org_admin';
export const ROLE_TEST_MANAGER = 'test_manager';
export const ROLE_TEST_USER = 'test_user';
export const ROLE_TEST_VIEWER = 'test_viewer';

export const WORKSPACE_ID = 'workspace_id';
export const CURRENT_PROJECT = 'current_project';
export const PROJECT_ID = 'project_id';
export const PROJECT_NAME = 'project_name';

export const REFRESH_SESSION_USER_URL = 'user/refresh';
export const WORKSPACE = 'workspace';
export const ORGANIZATION = 'organization';
export const MENU = 'menu';
export const DEFAULT = 'default';

export const ZH_CN = 'zh_CN';
export const ZH_TW = 'zh_TW';
export const EN_US = 'en_US';

export const TAPD = 'Tapd';
export const JIRA = 'Jira';
export const ZEN_TAO = 'Zentao';

export const SCHEDULE_TYPE = {
  API_TEST: 'API_TEST',
  PERFORMANCE_TEST: 'PERFORMANCE_TEST'
}

export const REQUEST_HEADERS = [
  {value: 'Accept'},
  {value: 'Accept-Charset'},
  {value: 'Accept-Language'},
  {value: 'Accept-Datetime'},
  {value: 'Authorization'},
  {value: 'Cache-Control'},
  {value: 'Connection'},
  {value: 'Cookie'},
  {value: 'Content-Length'},
  {value: 'Content-MD5'},
  {value: 'Content-Type'},
  {value: 'Date'},
  {value: 'Expect'},
  {value: 'From'},
  {value: 'Host'},
  {value: 'If-Match'},
  {value: 'If-Modified-Since'},
  {value: 'If-None-Match'},
  {value: 'If-Range'},
  {value: 'If-Unmodified-Since'},
  {value: 'Max-Forwards'},
  {value: 'Origin'},
  {value: 'Pragma'},
  {value: 'Proxy-Authorization'},
  {value: 'Range'},
  {value: 'Referer'},
  {value: 'TE'},
  {value: 'User-Agent'},
  {value: 'Upgrade'},
  {value: 'Via'},
  {value: 'Warning'}
]

export const MOCKJS_FUNC = [
  {name: '@boolean'},
  {name: '@natural'},
  {name: '@integer'},
  {name: '@float'},
  {name: '@character'},
  {name: '@string'},
  {name: '@range'},
  {name: '@date'},
  {name: '@time'},
  {name: '@datetime'},
  {name: '@now'},
  {name: '@img'},
  {name: '@dataImage'},
  {name: '@color'},
  {name: '@hex'},
  {name: '@rgb'},
  {name: '@rgba'},
  {name: '@hsl'},
  {name: '@paragraph'},
  {name: '@sentence'},
  {name: '@word'},
  {name: '@title'},
  {name: '@cparagraph'},
  {name: '@csentence'},
  {name: '@cword'},
  {name: '@ctitle'},
  {name: '@first'},
  {name: '@last'},
  {name: '@name'},
  {name: '@cfirst'},
  {name: '@clast'},
  {name: '@cname'},
  {name: '@url'},
  {name: '@domain'},
  {name: '@protocol'},
  {name: '@tld'},
  {name: '@email'},
  {name: '@ip'},
  {name: '@region'},
  {name: '@province'},
  {name: '@city'},
  {name: '@county'},
  {name: '@zip'},
  {name: '@capitalize'},
  {name: '@upper'},
  {name: '@lower'},
  {name: '@pick'},
  {name: '@shuffle'},
  {name: '@guid'},
  {name: '@id'},
  {name: '@increment'}
]

export const JMETER_FUNC = [
  {type: "Information", name: "${__threadNum}", description: "get thread number"},
  {type: "Information", name: "${__samplerName}", description: "get the sampler name (label)"},
  {type: "Information", name: "${__machineIP}", description: "get the local machine IP address"},
  {type: "Information", name: "${__machineName}", description: "get the local machine name"},
  {type: "Information", name: "${__time}", description: "return current time in various formats"},
  {type: "Information", name: "${__log}", description: "log (or display) a message (and return the value)"},
  {type: "Information", name: "${__logn}", description: "log (or display) a message (empty return value)"},
  {type: "Input", name: "${__StringFromFile}", description: "read a line from a file"},
  {type: "Input", name: "${__FileToString}", description: "read an entire file"},
  {type: "Input", name: "${__CSVRead}", description: "read from CSV definitioned file"},
  {type: "Input", name: "${__XPath}", description: "Use an XPath expression to read from a file"},
  {type: "Calculation", name: "${__counter}", description: "generate an incrementing number"},
  {type: "Calculation", name: "${__intSum}", description: "add int numbers"},
  {type: "Calculation", name: "${__longSum}", description: "add long numbers"},
  {type: "Calculation", name: "${__Random}", description: "generate a random number"},
  {type: "Calculation", name: "${__RandomString}", description: "generate a random string"},
  {type: "Calculation", name: "${__UUID}", description: "generate a random type 4 UUID"},
  {type: "Scripting", name: "${__BeanShell}", description: "run a BeanShell script"},
  {type: "Scripting", name: "${__javaScript}", description: "process JavaScript (Mozilla Rhino)"},
  {type: "Scripting", name: "${__jexl}", description: "evaluate a Commons Jexl expression"},
  {type: "Scripting", name: "${__jexl2}", description: "evaluate a Commons Jexl expression"},
  {type: "Properties", name: "${__property}", description: "read a property"},
  {type: "Properties", name: "${__P}", description: "read a property (shorthand method)"},
  {type: "Properties", name: "${__setProperty}", description: "set a JMeter property"},
  {type: "Variables", name: "${__split}", description: "Split a string into variables"},
  {type: "Variables", name: "${__V}", description: "evaluate a variable name"},
  {type: "Variables", name: "${__eval}", description: "evaluate a variable expression"},
  {type: "Variables", name: "${__evalVar}", description: "evaluate an expression stored in a variable"},
  {type: "String", name: "${__regexFunction}", description: "parse previous response using a regular expression"},
  {type: "String", name: "${__escapeOroRegexpChars}", description: "quote meta chars used by ORO regular expression"},
  {type: "String", name: "${__char}", description: "generate Unicode char values from a list of numbers"},
  {type: "String", name: "${__unescape}", description: "Process strings containing Java escapes (e.g. & )"},
  {type: "String", name: "${__unescapeHtml}", description: "Decode HTML-encoded strings"},
  {type: "String", name: "${__escapeHtml}", description: "Encode strings using HTML encoding"},
  {type: "String", name: "${__TestPlanName}", description: "Return name of current test plan"},
]
