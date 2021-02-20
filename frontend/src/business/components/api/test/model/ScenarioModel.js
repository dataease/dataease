import {
  Arguments,
  ConstantTimer as JMXConstantTimer,
  CookieManager,
  DNSCacheManager,
  DubboSample,
  DurationAssertion,
  Element,
  HashTree,
  HeaderManager,
  HTTPSamplerArguments,
  HTTPsamplerFiles,
  HTTPSamplerProxy,
  IfController as JMXIfController,
  JDBCDataSource,
  JDBCSampler,
  JSONPathAssertion,
  JSONPostProcessor,
  JSR223Assertion,
  JSR223PostProcessor,
  JSR223PreProcessor,
  RegexExtractor,
  ResponseCodeAssertion,
  ResponseDataAssertion,
  ResponseHeadersAssertion,
  TCPSampler,
  TestElement,
  TestPlan,
  ThreadGroup,
  XPath2Assertion,
  XPath2Extractor,
} from "./JMX";
import Mock from "mockjs";
import {funcFilters} from "@/common/js/func-filter";

export const uuid = function () {
  let d = new Date().getTime()
  let d2 = (performance && performance.now && (performance.now() * 1000)) || 0;
  return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function (c) {
    let r = Math.random() * 16;
    if (d > 0) {
      r = (d + r) % 16 | 0;
      d = Math.floor(d / 16);
    } else {
      r = (d2 + r) % 16 | 0;
      d2 = Math.floor(d2 / 16);
    }
    return (c === 'x' ? r : (r & 0x3 | 0x8)).toString(16);
  });
}

export const BODY_FILE_DIR = "/opt/metersphere/data/body"; //存放body文件上传目录

export const calculate = function (itemValue) {
  if (!itemValue) {
    return;
  }
  try {
    if (itemValue.trim().startsWith("${")) {
      // jmeter 内置函数不做处理
      return itemValue;
    }
    let funcs = itemValue.split("|");
    let value = Mock.mock(funcs[0].trim());
    if (funcs.length === 1) {
      return value;
    }
    for (let i = 1; i < funcs.length; i++) {
      let func = funcs[i].trim();
      let args = func.split(":");
      let strings = [];
      if (args[1]) {
        strings = args[1].split(",");
      }
      value = funcFilters[args[0].trim()](value, ...strings);
    }
    return value;
  } catch (e) {
    return itemValue;
  }
}

export const BODY_TYPE = {
  KV: "KeyValue",
  FORM_DATA: "Form Data",
  RAW: "Raw"
}

export const BODY_FORMAT = {
  TEXT: "text",
  JSON: "json",
  XML: "xml",
  HTML: "html",
}

export const ASSERTION_TYPE = {
  TEXT: "Text",
  REGEX: "Regex",
  JSON_PATH: "JSON",
  DURATION: "Duration",
  JSR223: "JSR223",
  XPATH2: "XPath2",
}

export const ASSERTION_REGEX_SUBJECT = {
  RESPONSE_CODE: "Response Code",
  RESPONSE_HEADERS: "Response Headers",
  RESPONSE_DATA: "Response Data"
}

export const EXTRACT_TYPE = {
  REGEX: "Regex",
  JSON_PATH: "JSONPath",
  XPATH: "XPath"
}

export class BaseConfig {

  set(options, notUndefined) {
    options = this.initOptions(options)
    for (let name in options) {
      if (options.hasOwnProperty(name)) {
        if (!(this[name] instanceof Array)) {
          if (notUndefined === true) {
            this[name] = options[name] === undefined ? this[name] : options[name];
          } else {
            this[name] = options[name];
          }
        }
      }
    }
  }

  sets(types, options) {
    options = this.initOptions(options)
    if (types) {
      for (let name in types) {
        if (types.hasOwnProperty(name) && options.hasOwnProperty(name)) {
          options[name].forEach(o => {
            this[name].push(new types[name](o));
          })
        }
      }
    }
  }

  initOptions(options) {
    return options || {};
  }

  isValid() {
    return true;
  }
}

export class Test extends BaseConfig {
  constructor(options) {
    super();
    this.type = "MS API CONFIG";
    this.version = '1.5.0';
    this.id = uuid();
    this.name = undefined;
    this.projectId = undefined;
    this.scenarioDefinition = [];
    this.schedule = {};

    this.set(options);
    this.sets({scenarioDefinition: Scenario}, options);
  }

  export() {
    let obj = {
      type: this.type,
      version: this.version,
      scenarios: this.scenarioDefinition
    };

    return JSON.stringify(obj);
  }

  initOptions(options) {
    options = options || {};
    options.scenarioDefinition = options.scenarioDefinition || [new Scenario()];
    return options;
  }

  isValid() {
    for (let i = 0; i < this.scenarioDefinition.length; i++) {
      let validator = this.scenarioDefinition[i].isValid();
      if (!validator.isValid) {
        return validator;
      }
    }
    if (!this.projectId) {
      return {
        isValid: false,
        info: 'api_test.select_project'
      }
    } else if (!this.name) {
      return {
        isValid: false,
        info: 'api_test.input_name'
      }
    }
    return {isValid: true};
  }

  toJMX() {
    return {
      name: this.name + '.jmx',
      xml: new JMXGenerator(this).toXML()
    };
  }
}

export class Scenario extends BaseConfig {
  constructor(options = {}) {
    super();
    this.id = undefined;
    this.name = undefined;
    this.url = undefined;
    this.variables = [];
    this.headers = [];
    this.requests = [];
    this.environmentId = undefined;
    this.dubboConfig = undefined;
    this.environment = undefined;
    this.enableCookieShare = false;
    this.enable = true;
    this.databaseConfigs = [];
    this.tcpConfig = undefined;
    this.assertions = undefined;
    this.referenceEnable = undefined;

    this.set(options);
    this.sets({
      variables: KeyValue,
      headers: KeyValue,
      requests: RequestFactory,
      databaseConfigs: DatabaseConfig
    }, options);
  }

  initOptions(options = {}) {
    options.id = options.id || uuid();
    options.requests = options.requests || [new RequestFactory()];
    options.databaseConfigs = options.databaseConfigs || [];
    options.dubboConfig = new DubboConfig(options.dubboConfig);
    options.tcpConfig = new TCPConfig(options.tcpConfig);
    options.assertions = new Assertions(options.assertions);
    return options;
  }

  clone() {
    let clone = new Scenario(this);
    clone.id = uuid();
    return clone;
  }

  isValid() {
    if (this.enable) {
      for (let i = 0; i < this.requests.length; i++) {
        let validator = this.requests[i].isValid(this.environmentId, this.environment);
        if (!validator.isValid) {
          return validator;
        }
      }
    }
    return {isValid: true};
  }

  isReference() {
    return this.id.indexOf("#") !== -1
  }

  isEnable() {
    if (this.isReference()) {
      return this.referenceEnable;
    } else {
      return this.enable;
    }
  }
}

class DubboConfig extends BaseConfig {
  constructor(options = {}) {
    super();
    this.configCenter = new ConfigCenter(options.configCenter)
    this.registryCenter = new RegistryCenter(options.registryCenter)
    if (options.consumerAndService === undefined) {
      options.consumerAndService = {
        timeout: undefined,
        version: undefined,
        retries: undefined,
        cluster: undefined,
        group: undefined,
        connections: undefined,
        async: undefined,
        loadBalance: undefined
      }
    }
    this.consumerAndService = new ConsumerAndService(options.consumerAndService)
  }
}

export class RequestFactory {
  static TYPES = {
    HTTP: "HTTP",
    DUBBO: "DUBBO",
    SQL: "SQL",
    TCP: "TCP",
  }

  constructor(options = {}) {
    options.type = options.type || RequestFactory.TYPES.HTTP
    switch (options.type) {
      case RequestFactory.TYPES.DUBBO:
        return new DubboRequest(options);
      case RequestFactory.TYPES.SQL:
        return new SqlRequest(options);
      case RequestFactory.TYPES.TCP:
        return new TCPRequest(options);
      default:
        return new HttpRequest(options);
    }
  }
}

export class Request extends BaseConfig {
  constructor(type, options = {}) {
    super();
    this.type = type;
    this.id = options.id || uuid();
    this.name = options.name;
    this.enable = options.enable === undefined ? true : options.enable;
    this.assertions = new Assertions(options.assertions);
    this.extract = new Extract(options.extract);
    this.jsr223PreProcessor = new JSR223Processor(options.jsr223PreProcessor);
    this.jsr223PostProcessor = new JSR223Processor(options.jsr223PostProcessor);
    this.timer = new ConstantTimer(options.timer);
    this.controller = new IfController(options.controller);
  }

  showType() {
    return this.type;
  }

  showMethod() {
    return "";
  }
}

export class HttpRequest extends Request {
  constructor(options) {
    super(RequestFactory.TYPES.HTTP, options);
    this.url = options.url;
    this.path = options.path;
    this.method = options.method || "GET";
    this.parameters = [];
    this.headers = [];
    this.body = new Body(options.body);
    this.environment = options.environment;
    this.useEnvironment = options.useEnvironment;
    this.debugReport = undefined;
    this.doMultipartPost = options.doMultipartPost;
    this.connectTimeout = options.connectTimeout || 60 * 1000;
    this.responseTimeout = options.responseTimeout;
    this.followRedirects = options.followRedirects === undefined ? true : options.followRedirects;

    this.sets({parameters: KeyValue, headers: KeyValue}, options);
  }

  isValid(environmentId, environment) {
    if (this.enable) {
      if (this.useEnvironment) {
        if (!environmentId) {
          return {
            isValid: false,
            info: 'api_test.request.please_configure_environment_in_scenario'
          }
        }
        if (!environment.config.httpConfig.socket) {
          return {
            isValid: false,
            info: 'api_test.request.please_configure_socket_in_environment'
          }
        }
      } else {
        if (!this.url) {
          return {
            isValid: false,
            info: 'api_test.request.input_url'
          }
        }
        try {
          new URL(this.url)
        } catch (e) {
          return {
            isValid: false,
            info: 'api_test.request.url_invalid'
          }
        }
      }
    }
    return {
      isValid: true
    }
  }

  showType() {
    return this.type;
  }

  showMethod() {
    return this.method.toUpperCase();
  }

}

export class DubboRequest extends Request {
  static PROTOCOLS = {
    DUBBO: "dubbo://",
    RMI: "rmi://",
  }

  constructor(options = {}) {
    super(RequestFactory.TYPES.DUBBO, options);
    this.protocol = options.protocol || DubboRequest.PROTOCOLS.DUBBO;
    this.interface = options.interface;
    this.method = options.method;
    this.configCenter = new ConfigCenter(options.configCenter);
    this.registryCenter = new RegistryCenter(options.registryCenter);
    this.consumerAndService = new ConsumerAndService(options.consumerAndService);
    this.args = [];
    this.attachmentArgs = [];
    // Scenario.dubboConfig
    this.dubboConfig = undefined;
    this.debugReport = undefined;

    this.sets({args: KeyValue, attachmentArgs: KeyValue}, options);
  }

  isValid() {
    if (this.enable) {
      if (!this.interface) {
        return {
          isValid: false,
          info: 'api_test.request.dubbo.input_interface'
        }
      }
      if (!this.method) {
        return {
          isValid: false,
          info: 'api_test.request.dubbo.input_method'
        }
      }
      if (!this.registryCenter.isValid()) {
        return {
          isValid: false,
          info: 'api_test.request.dubbo.input_registry_center'
        }
      }
      if (!this.consumerAndService.isValid()) {
        return {
          isValid: false,
          info: 'api_test.request.dubbo.input_consumer_service'
        }
      }
    }
    return {
      isValid: true
    }
  }

  showType() {
    return "RPC";
  }

  showMethod() {
    // dubbo:// -> DUBBO
    return this.protocol.substr(0, this.protocol.length - 3).toUpperCase();
  }

  clone() {
    return new DubboRequest(this);
  }
}

export class SqlRequest extends Request {

  constructor(options = {}) {
    super(RequestFactory.TYPES.SQL, options);
    this.useEnvironment = options.useEnvironment;
    this.resultVariable = options.resultVariable;
    this.variableNames = options.variableNames;
    this.variables = [];
    this.debugReport = undefined;
    this.dataSource = options.dataSource;
    this.query = options.query;
    // this.queryType = options.queryType;
    this.queryTimeout = options.queryTimeout || 60000;

    this.sets({args: KeyValue, attachmentArgs: KeyValue, variables: KeyValue}, options);
  }

  isValid() {
    if (this.enable) {
      if (!this.name) {
        return {
          isValid: false,
          info: 'api_test.request.sql.name_cannot_be_empty'
        }
      }
      if (!this.dataSource) {
        return {
          isValid: false,
          info: 'api_test.request.sql.dataSource_cannot_be_empty'
        }
      }
    }
    return {
      isValid: true
    }
  }

  showType() {
    return "SQL";
  }

  showMethod() {
    return "SQL";
  }

  clone() {
    return new SqlRequest(this);
  }
}

export class TCPConfig extends BaseConfig {
  static CLASSES = ["TCPClientImpl", "BinaryTCPClientImpl", "LengthPrefixedBinaryTCPClientImpl"]

  constructor(options = {}) {
    super();
    this.classname = options.classname || TCPConfig.CLASSES[0];
    this.server = options.server;
    this.port = options.port;
    this.ctimeout = options.ctimeout; // Connect
    this.timeout = options.timeout; // Response

    this.reUseConnection = options.reUseConnection === undefined ? true : options.reUseConnection;
    this.nodelay = options.nodelay === undefined ? false : options.nodelay;
    this.closeConnection = options.closeConnection === undefined ? false : options.closeConnection;
    this.soLinger = options.soLinger;
    this.eolByte = options.eolByte;

    this.username = options.username;
    this.password = options.password;
  }
}

export class TCPRequest extends Request {
  constructor(options = {}) {
    super(RequestFactory.TYPES.TCP, options);
    this.useEnvironment = options.useEnvironment;
    this.debugReport = undefined;

    //设置TCPConfig的属性
    this.set(new TCPConfig(options));

    this.request = options.request;
  }

  isValid() {
    return {
      isValid: true
    }
  }

  showType() {
    return "TCP";
  }

  showMethod() {
    return "TCP";
  }

  clone() {
    return new TCPRequest(this);
  }
}


export class ConfigCenter extends BaseConfig {
  static PROTOCOLS = ["zookeeper", "nacos", "apollo"];

  constructor(options) {
    super();
    this.protocol = undefined;
    this.group = undefined;
    this.namespace = undefined;
    this.username = undefined;
    this.address = undefined;
    this.password = undefined;
    this.timeout = undefined;

    this.set(options);
  }

  isValid() {
    return !!this.protocol || !!this.group || !!this.namespace || !!this.username || !!this.address || !!this.password || !!this.timeout;
  }
}

export class DatabaseConfig extends BaseConfig {
  static DRIVER_CLASS = ["com.mysql.jdbc.Driver", "com.microsoft.sqlserver.jdbc.SQLServerDriver", "org.postgresql.Driver", "oracle.jdbc.OracleDriver"];

  constructor(options) {
    super();
    this.id = undefined;
    this.name = undefined;
    this.poolMax = 1;
    this.timeout = 100000;
    this.driver = undefined;
    this.dbUrl = undefined;
    this.username = undefined;
    this.password = undefined;

    this.set(options);
  }

  initOptions(options = {}) {
    // options.id = options.id || uuid();
    return options;
  }

  isValid() {
    return !!this.name || !!this.poolMax || !!this.timeout || !!this.driver || !!this.dbUrl || !!this.username || !!this.password;
  }
}

export class RegistryCenter extends BaseConfig {
  static PROTOCOLS = ["none", "zookeeper", "nacos", "apollo", "multicast", "redis", "simple"];

  constructor(options) {
    super();
    this.protocol = undefined;
    this.group = undefined;
    this.username = undefined;
    this.address = undefined;
    this.password = undefined;
    this.timeout = undefined;

    this.set(options);
  }

  isValid() {
    return !!this.protocol || !!this.group || !!this.username || !!this.address || !!this.password || !!this.timeout;
  }
}

export class ConsumerAndService extends BaseConfig {
  static ASYNC_OPTIONS = ["sync", "async"];
  static LOAD_BALANCE_OPTIONS = ["random", "roundrobin", "leastactive", "consistenthash"];

  constructor(options) {
    super();
    this.timeout = "1000";
    this.version = "1.0";
    this.retries = "0";
    this.cluster = "failfast";
    this.group = undefined;
    this.connections = "100";
    this.async = "sync";
    this.loadBalance = "random";

    this.set(options);
  }

  isValid() {
    return !!this.timeout || !!this.version || !!this.retries || !!this.cluster || !!this.group || !!this.connections || !!this.async || !!this.loadBalance;
  }
}

export class Body extends BaseConfig {
  constructor(options) {
    super();
    this.type = undefined;
    this.raw = undefined;
    this.kvs = [];

    this.set(options);
    this.sets({kvs: KeyValue}, options);
  }

  isValid() {
    if (this.isKV()) {
      return this.kvs.some(kv => {
        return kv.isValid();
      })
    } else {
      return !!this.raw;
    }
  }

  isKV() {
    return this.type === BODY_TYPE.KV;
  }
}

export class KeyValue extends BaseConfig {
  constructor(options) {
    options = options || {};
    options.enable = options.enable === undefined ? true : options.enable;

    super();
    this.name = undefined;
    this.value = undefined;
    this.type = undefined;
    this.files = undefined;
    this.enable = undefined;
    this.uuid = undefined;
    this.contentType = undefined;
    this.set(options);
  }

  isValid() {
    return (!!this.name || !!this.value) && this.type !== 'file';
  }

  isFile() {
    return (!!this.name || !!this.value) && this.type === 'file';
  }
}

export class BeanShellProcessor extends BaseConfig {
  constructor(options) {
    super();
    this.script = undefined;
    this.set(options);
  }
}

export class JSR223Processor extends BaseConfig {
  constructor(options) {
    super();
    this.script = undefined;
    this.language = "beanshell";
    this.set(options);
  }
}

export class Assertions extends BaseConfig {
  constructor(options) {
    super();
    this.text = [];
    this.regex = [];
    this.jsonPath = [];
    this.jsr223 = [];
    this.xpath2 = [];
    this.duration = undefined;

    this.set(options);
    this.sets({text: Text, regex: Regex, jsonPath: JSONPath, jsr223: AssertionJSR223, xpath2: XPath2}, options);
  }

  initOptions(options) {
    options = options || {};
    options.duration = new Duration(options.duration);
    return options;
  }
}

export class AssertionType extends BaseConfig {
  constructor(type) {
    super();
    this.type = type;
  }
}

export class AssertionJSR223 extends AssertionType {
  constructor(options) {
    super(ASSERTION_TYPE.JSR223);
    this.variable = undefined;
    this.operator = undefined;
    this.value = undefined;
    this.desc = undefined;

    this.name = undefined;
    this.script = undefined;
    this.language = "beanshell";
    this.set(options);
  }

  isValid() {
    return !!this.script && !!this.language;
  }
}

export class Text extends AssertionType {
  constructor(options) {
    super(ASSERTION_TYPE.TEXT);
    this.subject = undefined;
    this.condition = undefined;
    this.value = undefined;

    this.set(options);
  }
}

export class Regex extends AssertionType {
  constructor(options) {
    super(ASSERTION_TYPE.REGEX);
    this.subject = undefined;
    this.expression = undefined;
    this.description = undefined;
    this.assumeSuccess = false;

    this.set(options);
  }

  isValid() {
    return !!this.subject && !!this.expression;
  }
}

export class JSONPath extends AssertionType {
  constructor(options) {
    super(ASSERTION_TYPE.JSON_PATH);
    this.expression = undefined;
    this.expect = undefined;
    this.description = undefined;

    this.set(options);
  }

  setJSONPathDescription() {
    this.description = this.expression + " expect: " + (this.expect ? this.expect : '');
  }

  isValid() {
    return !!this.expression;
  }
}

export class XPath2 extends AssertionType {
  constructor(options) {
    super(ASSERTION_TYPE.XPATH2);
    this.expression = undefined;
    this.description = undefined;
    this.set(options);
  }

  // setJSONPathDescription() {
  //   this.description = this.expression + " expect: " + (this.expect ? this.expect : '');
  // }

  isValid() {
    return !!this.expression;
  }
}

export class Duration extends AssertionType {
  constructor(options) {
    super(ASSERTION_TYPE.DURATION);
    this.value = undefined;

    this.set(options);
  }

  isValid() {
    return !!this.value;
  }
}

export class Extract extends BaseConfig {
  constructor(options) {
    super();
    this.regex = [];
    this.json = [];
    this.xpath = [];

    this.set(options);
    let types = {
      json: ExtractJSONPath,
      xpath: ExtractXPath,
      regex: ExtractRegex
    }
    this.sets(types, options);
  }
}

export class ExtractType extends BaseConfig {
  constructor(type) {
    super();
    this.type = type;
  }
}

export class ExtractCommon extends ExtractType {
  constructor(type, options) {
    super(type);
    this.variable = undefined;
    this.useHeaders = undefined;
    this.value = ""; // ${variable}
    this.expression = undefined;
    this.description = undefined;
    this.multipleMatching = undefined;

    this.set(options);
  }

  isValid() {
    return !!this.variable && !!this.expression;
  }
}

export class ExtractRegex extends ExtractCommon {
  constructor(options) {
    super(EXTRACT_TYPE.REGEX, options);
  }
}

export class ExtractJSONPath extends ExtractCommon {
  constructor(options) {
    super(EXTRACT_TYPE.JSON_PATH, options);
  }
}

export class ExtractXPath extends ExtractCommon {
  constructor(options) {
    super(EXTRACT_TYPE.XPATH, options);
  }
}

export class Controller extends BaseConfig {
  static TYPES = {
    IF_CONTROLLER: "If Controller",
  }

  constructor(type, options = {}) {
    super();
    this.type = type
    options.id = options.id || uuid();
    options.enable = options.enable === undefined ? true : options.enable;
  }
}

export class IfController extends Controller {
  constructor(options = {}) {
    super(Controller.TYPES.IF_CONTROLLER, options);
    this.variable;
    this.operator;
    this.value;

    this.set(options);
  }

  isValid() {
    if (!!this.operator && this.operator.indexOf("empty") > 0) {
      return !!this.variable && !!this.operator;
    }
    return !!this.variable && !!this.operator && !!this.value;
  }

  getLabel() {
    if (this.isValid()) {
      let label = this.variable;
      if (this.operator) label += " " + this.operator;
      if (this.value) label += " " + this.value;
      return label;
    }
    return "";
  }
}

export class Timer extends BaseConfig {
  static TYPES = {
    CONSTANT_TIMER: "Constant Timer",
  }

  constructor(type, options = {}) {
    super();
    this.type = type;
    options.id = options.id || uuid();
    options.enable = options.enable === undefined ? true : options.enable;
  }
}

export class ConstantTimer extends Timer {
  constructor(options = {}) {
    super(Timer.TYPES.CONSTANT_TIMER, options);
    this.delay;

    this.set(options);
  }

  isValid() {
    return this.delay > 0;
  }

  getLabel() {
    if (this.isValid()) {
      return this.delay + " ms";
    }
    return "";
  }
}

/** ------------------------------------------------------------------------ **/
const JMX_ASSERTION_CONDITION = {
  MATCH: 1,
  CONTAINS: 1 << 1,
  NOT: 1 << 2,
  EQUALS: 1 << 3,
  SUBSTRING: 1 << 4,
  OR: 1 << 5
}

class JMXHttpRequest {
  constructor(request, environment) {
    if (request && request instanceof HttpRequest) {
      this.useEnvironment = request.useEnvironment;
      this.method = request.method;
      if (!request.useEnvironment) {
        if (!request.url.startsWith("http://") && !request.url.startsWith("https://")) {
          request.url = 'http://' + request.url;
        }
        let url = new URL(request.url);
        this.domain = decodeURIComponent(url.hostname);
        this.port = url.port;
        this.protocol = url.protocol.split(":")[0];
        this.path = this.getPostQueryParameters(request, decodeURIComponent(url.pathname));
      } else {
        this.domain = environment.config.httpConfig.domain;
        this.port = environment.config.httpConfig.port;
        this.protocol = environment.config.httpConfig.protocol;
        let url = new URL(environment.config.httpConfig.protocol + "://" + environment.config.httpConfig.socket);
        let envPath = url.pathname === '/' ? '' : url.pathname;
        this.path = this.getPostQueryParameters(request, decodeURIComponent(envPath + (request.path ? request.path : '')));
      }
      this.connectTimeout = request.connectTimeout;
      this.responseTimeout = request.responseTimeout;
      this.followRedirects = request.followRedirects;
      this.doMultipartPost = request.doMultipartPost;
    }
  }

  getPostQueryParameters(request, path) {
    if (this.method.toUpperCase() !== "GET") {
      let parameters = [];
      request.parameters.forEach(parameter => {
        if (parameter.name && parameter.value && parameter.enable === true) {
          parameters.push(parameter);
        }
      });
      if (parameters.length > 0) {
        path += '?';
      }
      for (let i = 0; i < parameters.length; i++) {
        let parameter = parameters[i];
        path += (parameter.name + '=' + parameter.value);
        if (i !== parameters.length - 1) {
          path += '&';
        }
      }
    }
    return path;
  }
}

class JMXDubboRequest {
  constructor(request, dubboConfig) {
    // Request 复制
    let obj = request.clone();
    // 去掉无效的kv
    obj.args = obj.args.filter(arg => {
      return arg.isValid();
    });
    obj.attachmentArgs = obj.attachmentArgs.filter(arg => {
      return arg.isValid();
    });

    // Scenario DubboConfig复制
    this.copy(obj.configCenter, dubboConfig.configCenter);
    this.copy(obj.registryCenter, dubboConfig.registryCenter);
    this.copy(obj.consumerAndService, dubboConfig.consumerAndService);

    return obj;
  }

  copy(target, source) {
    for (let key in source) {
      if (source.hasOwnProperty(key)) {
        if (source[key] !== undefined && !target[key]) {
          target[key] = source[key];
        }
      }
    }
  }
}

class JMXTCPRequest {
  constructor(request, scenario) {
    let obj = request.clone();
    if (request.useEnvironment) {
      obj.set(scenario.environment.config.tcpConfig, true);
      return obj;
    }

    this.copy(this, scenario.tcpConfig);

    return obj;
  }

  copy(target, source) {
    for (let key in source) {
      if (source.hasOwnProperty(key)) {
        if (source[key] !== undefined && !target[key]) {
          target[key] = source[key];
        }
      }
    }
  }
}

class JMeterTestPlan extends Element {
  constructor() {
    super('jmeterTestPlan', {
      version: "1.2", properties: "5.0", jmeter: "5.2.1"
    });

    this.add(new HashTree());
  }

  put(te) {
    if (te instanceof TestElement) {
      this.elements[0].add(te);
    }
  }
}

class JMXGenerator {
  constructor(test) {
    if (!test || !test.id || !(test instanceof Test)) return undefined;

    let testPlan = new TestPlan(test.name);
    this.addScenarios(testPlan, test.id, test.scenarioDefinition);

    this.jmeterTestPlan = new JMeterTestPlan();
    this.jmeterTestPlan.put(testPlan);
  }

  addScenarios(testPlan, testId, scenarios) {
    scenarios.forEach(s => {

      if (s.isEnable()) {
        let scenario = s.clone();

        let threadGroup = new ThreadGroup(scenario.name || "");

        this.addScenarioVariables(threadGroup, scenario);

        this.addScenarioHeaders(threadGroup, scenario);

        this.addScenarioCookieManager(threadGroup, scenario);

        this.addJDBCDataSources(threadGroup, scenario);

        this.addAssertion(threadGroup, scenario);

        scenario.requests.forEach(request => {
          if (request.enable) {
            if (!request.isValid()) return;
            let sampler;
            if (request instanceof DubboRequest) {
              sampler = new DubboSample(request.name || "", new JMXDubboRequest(request, scenario.dubboConfig));
            } else if (request instanceof HttpRequest) {
              sampler = new HTTPSamplerProxy(request.name || "", new JMXHttpRequest(request, scenario.environment));
              this.addRequestHeader(sampler, request, scenario);
              this.addRequestArguments(sampler, request);
              this.addRequestBody(sampler, request, testId);
            } else if (request instanceof SqlRequest) {
              request.dataSource = scenario.databaseConfigMap.get(request.dataSource);
              sampler = new JDBCSampler(request.name || "", request);
            } else if (request instanceof TCPRequest) {
              sampler = new TCPSampler(request.name || "", new JMXTCPRequest(request, scenario));
            }

            this.addRequestVariables(sampler, request, scenario);

            this.addDNSCacheManager(sampler, scenario.environment, request.useEnvironment);

            this.addRequestExtractor(sampler, request);

            this.addAssertion(sampler, request);

            this.addJSR223PreProcessor(sampler, request);

            this.addConstantsTimer(sampler, request);

            if (request.controller && request.controller.isValid() && request.controller.enable) {
              if (request.controller instanceof IfController) {
                let controller = this.getController(sampler, request);
                threadGroup.put(controller);
              }
            } else {
              threadGroup.put(sampler);
            }
          }
        })
        testPlan.put(threadGroup);
      }

    })
  }

  addEnvironments(environments, target) {
    let targetMap = new Map();
    target.forEach(item => {
      if (item.name) {
        targetMap.set(item.name, item.enable);
      }
    });
    let envArray = environments;
    if (!(envArray instanceof Array)) {
      envArray = JSON.parse(environments);
    }
    envArray.forEach(item => {
      let targetItem = targetMap.get(item.name);
      let hasItem = undefined;
      if (targetItem) {
        hasItem = (targetItem.enable === false ? false : true);
      } else {
        hasItem = false;
      }
      if (item.enable != false && item.name && !hasItem) {
        target.push(new KeyValue({name: item.name, value: item.value}));
      }
    })
  }

  addScenarioVariables(threadGroup, scenario) {
    let args = this.filterKV(scenario.variables);
    if (args.length > 0) {
      let name = scenario.name + " Variables";
      threadGroup.put(new Arguments(name, args));
    }
  }

  addRequestVariables(httpSamplerProxy, request, scenario) {
    if (request.useEnvironment && scenario.environment) {
      let config = scenario.environment.config;
      if (!(scenario.environment.config instanceof Object)) {
        config = JSON.parse(scenario.environment.config);
      }
      if (!request.variables) {
        request.variables = [];
      }
      this.addEnvironments(config.commonConfig.variables, request.variables)
    }
    let name = request.name + " Variables";
    if (request.variables) {
      let variables = this.filterKV(request.variables);
      if (variables && variables.length > 0) {
        httpSamplerProxy.put(new Arguments(name, variables));
      }
    }
  }

  addScenarioCookieManager(threadGroup, scenario) {
    if (scenario.enableCookieShare) {
      threadGroup.put(new CookieManager(scenario.name));
    }
  }

  addDNSCacheManager(httpSamplerProxy, environment, useEnv) {
    if (environment && useEnv === true) {
      let commonConfig = environment.config.commonConfig;
      let hosts = commonConfig.hosts;
      if (commonConfig.enableHost && hosts.length > 0) {
        let name = " DNSCacheManager";
        // 强化判断，如果未匹配到合适的host则不开启DNSCache
        let domain = environment.config.httpConfig.domain;
        let validHosts = [];
        hosts.forEach(item => {
          if (item.domain !== undefined && domain !== undefined) {
            let d = item.domain.trim().replace("http://", "").replace("https://", "");
            if (d === domain.trim()) {
              item.domain = d; // 域名去掉协议
              validHosts.push(item);
            }
          }
        });
        if (validHosts.length > 0) {
          httpSamplerProxy.put(new DNSCacheManager(name, validHosts));
        }
      }
    }
  }

  addJDBCDataSources(threadGroup, scenario) {
    let names = new Set();
    let databaseConfigMap = new Map();
    scenario.databaseConfigs.forEach(config => {
      let name = config.name + "JDBCDataSource";
      threadGroup.put(new JDBCDataSource(name, config));
      names.add(name);
      databaseConfigMap.set(config.id, config.name);
    });
    if (scenario.environment) {
      let config = scenario.environment.config;
      if (!(scenario.environment.config instanceof Object)) {
        config = JSON.parse(scenario.environment.config);
      }
      config.databaseConfigs.forEach(config => {
        if (!names.has(config.name)) {
          let name = config.name + "JDBCDataSource";
          threadGroup.put(new JDBCDataSource(name, config));
          databaseConfigMap.set(config.id, config.name);
        }
      });
    }
    scenario.databaseConfigMap = databaseConfigMap;
  }

  addScenarioHeaders(threadGroup, scenario) {
    let headers = this.filterKV(scenario.headers);
    if (headers.length > 0) {
      let name = scenario.name + " Headers";
      threadGroup.put(new HeaderManager(name, headers));
    }
  }

  addRequestHeader(httpSamplerProxy, request, scenario) {
    if (request.useEnvironment && scenario.environment) {
      let config = scenario.environment.config;
      if (!(scenario.environment.config instanceof Object)) {
        config = JSON.parse(scenario.environment.config);
      }
      this.addEnvironments(config.httpConfig.headers, request.headers);
      if (request.doMultipartPost) {
        this.removeContentType(request);
      }
    }
    let name = request.name + " Headers";
    this.addBodyFormat(request);
    let headers = this.filterKV(request.headers);
    if (headers.length > 0) {
      httpSamplerProxy.put(new HeaderManager(name, headers));
    }
  }

  addJSR223PreProcessor(sampler, request) {
    let name = request.name;
    if (request.jsr223PreProcessor && request.jsr223PreProcessor.script) {
      sampler.put(new JSR223PreProcessor(name, request.jsr223PreProcessor));
    }
    if (request.jsr223PostProcessor && request.jsr223PostProcessor.script) {
      sampler.put(new JSR223PostProcessor(name, request.jsr223PostProcessor));
    }
  }

  addConstantsTimer(sampler, request) {
    if (request.timer && request.timer.isValid() && request.timer.enable) {
      sampler.put(new JMXConstantTimer(request.timer.getLabel(), request.timer));
    }
  }

  getController(sampler, request) {
    if (request.controller.isValid() && request.controller.enable) {
      if (request.controller instanceof IfController) {
        let name = request.controller.getLabel();
        let variable = "\"" + request.controller.variable + "\"";
        let operator = request.controller.operator;
        let value = "\"" + request.controller.value + "\"";

        if (operator === "=~" || operator === "!~") {
          value = "\".*" + request.controller.value + ".*\"";
        }

        if (operator === "is empty") {
          variable = "empty(" + variable + ")";
          operator = "";
          value = "";
        }

        if (operator === "is not empty") {
          variable = "!empty(" + variable + ")";
          operator = "";
          value = "";
        }

        let condition = "${__jexl3(" + variable + operator + value + ")}";
        let controller = new JMXIfController(name, {condition: condition});
        controller.put(sampler);
        return controller;
      }
    }
  }

  addBodyFormat(request) {
    let bodyFormat = request.body.format;
    if (!request.body.isKV() && bodyFormat) {
      switch (bodyFormat) {
        case BODY_FORMAT.JSON:
          this.addContentType(request, 'application/json');
          break;
        case BODY_FORMAT.HTML:
          this.addContentType(request, 'text/html');
          break;
        case BODY_FORMAT.XML:
          this.addContentType(request, 'text/xml');
          break;
        default:
          break;
      }
    }
  }

  addContentType(request, type) {
    let hasContentType = false;
    for (let index in request.headers) {
      if (request.headers.hasOwnProperty(index)) {
        if (request.headers[index].name === 'Content-Type' && request.headers[index].enable != false) {
          hasContentType = true;
          break;
        }
      }
    }
    if (!hasContentType) {
      request.headers.push(new KeyValue({name: 'Content-Type', value: type}));
    }
  }

  removeContentType(request) {
    for (let index in request.headers) {
      if (request.headers.hasOwnProperty(index)) {
        if (request.headers[index].name === 'Content-Type' && request.headers[index].enable != false) {
          request.headers.splice(index, 1);
          break;
        }
      }
    }
  }

  addRequestArguments(httpSamplerProxy, request) {
    let args = this.filterKV(request.parameters);
    if (args.length > 0) {
      httpSamplerProxy.add(new HTTPSamplerArguments(args));
    }
  }

  addRequestBody(httpSamplerProxy, request, testId) {
    let body = [];
    if (request.body.isKV()) {
      body = this.filterKV(request.body.kvs);
      this.addRequestBodyFile(httpSamplerProxy, request, testId);
    } else {
      if (request.body.raw) {
        httpSamplerProxy.boolProp('HTTPSampler.postBodyRaw', true);
        body.push({name: '', value: request.body.raw, encode: false, enable: true});
      }
    }

    if (request.method !== 'GET') {
      httpSamplerProxy.add(new HTTPSamplerArguments(body));
    }
  }

  addRequestBodyFile(httpSamplerProxy, request, testId) {
    let files = [];
    let kvs = this.filterKVFile(request.body.kvs);
    kvs.forEach(kv => {
      if ((kv.enable !== false) && kv.files) {
        kv.files.forEach(file => {
          let arg = {};
          arg.name = kv.name;
          arg.value = BODY_FILE_DIR + '/' + testId + '/' + file.id + '_' + file.name;
          arg.contentType = kv.contentType;
          files.push(arg);
        });
      }
    });
    httpSamplerProxy.add(new HTTPsamplerFiles(files));
  }

  addAssertion(httpSamplerProxy, request) {
    let assertions = request.assertions;
    if (assertions.regex.length > 0) {
      assertions.regex.filter(this.filter).forEach(regex => {
        httpSamplerProxy.put(this.getResponseAssertion(regex));
      })
    }

    if (assertions.jsonPath.length > 0) {
      assertions.jsonPath.filter(this.filter).forEach(item => {
        httpSamplerProxy.put(this.getJSONPathAssertion(item));
      })
    }

    if (assertions.xpath2.length > 0) {
      assertions.xpath2.filter(this.filter).forEach(item => {
        httpSamplerProxy.put(this.getXpathAssertion(item));
      })
    }

    if (assertions.jsr223.length > 0) {
      assertions.jsr223.filter(this.filter).forEach(item => {
        httpSamplerProxy.put(this.getJSR223Assertion(item));
      })
    }

    if (assertions.duration.isValid()) {
      let name = "Response In Time: " + assertions.duration.value
      httpSamplerProxy.put(new DurationAssertion(name, assertions.duration.value));
    }
  }

  getJSONPathAssertion(jsonPath) {
    let name = jsonPath.description;
    return new JSONPathAssertion(name, jsonPath);
  }

  getJSR223Assertion(item) {
    let name = item.desc;
    return new JSR223Assertion(name, item);
  }

  getXpathAssertion(item) {
    let name = item.expression;
    return new XPath2Assertion(name, item);
  }

  getResponseAssertion(regex) {
    let name = regex.description;
    let type = JMX_ASSERTION_CONDITION.CONTAINS; // 固定用Match，自己写正则
    let value = regex.expression;
    let assumeSuccess = regex.assumeSuccess;
    switch (regex.subject) {
      case ASSERTION_REGEX_SUBJECT.RESPONSE_CODE:
        return new ResponseCodeAssertion(name, type, value, assumeSuccess);
      case ASSERTION_REGEX_SUBJECT.RESPONSE_DATA:
        return new ResponseDataAssertion(name, type, value, assumeSuccess);
      case ASSERTION_REGEX_SUBJECT.RESPONSE_HEADERS:
        return new ResponseHeadersAssertion(name, type, value, assumeSuccess);
    }
  }

  addRequestExtractor(httpSamplerProxy, request) {
    let extract = request.extract;
    if (extract.regex.length > 0) {
      extract.regex.filter(this.filter).forEach(regex => {
        httpSamplerProxy.put(this.getExtractor(regex));
      })
    }

    if (extract.json.length > 0) {
      extract.json.filter(this.filter).forEach(json => {
        httpSamplerProxy.put(this.getExtractor(json));
      })
    }

    if (extract.xpath.length > 0) {
      extract.xpath.filter(this.filter).forEach(xpath => {
        httpSamplerProxy.put(this.getExtractor(xpath));
      })
    }
  }

  getExtractor(extractCommon) {
    let props = {
      name: extractCommon.variable,
      expression: extractCommon.expression,
      match: extractCommon.multipleMatching ? -1 : undefined
    }
    let testName = props.name
    switch (extractCommon.type) {
      case EXTRACT_TYPE.REGEX:
        testName += " RegexExtractor";
        props.headers = extractCommon.useHeaders; // 对应jMeter body
        props.template = "$1$";
        return new RegexExtractor(testName, props);
      case EXTRACT_TYPE.JSON_PATH:
        testName += " JSONExtractor";
        return new JSONPostProcessor(testName, props);
      case EXTRACT_TYPE.XPATH:
        testName += " XPath2Evaluator";
        return new XPath2Extractor(testName, props);
    }
  }

  filter(config) {
    return config.isValid();
  }

  filterKV(kvs) {
    return kvs.filter(this.filter);
  }

  filterKVFile(kvs) {
    return kvs.filter(kv => {
      return kv.isFile();
    });
  }

  toXML() {
    let xml = '<?xml version="1.0" encoding="UTF-8"?>\n';
    xml += this.jmeterTestPlan.toXML();
    return xml;
  }
}


