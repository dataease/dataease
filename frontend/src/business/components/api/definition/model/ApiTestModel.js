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
  RAW: "Raw",
  WWW_FORM: "WWW_FORM",
  XML: "XML",
  BINARY: "BINARY",
  JSON: "JSON"
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
          }
          else {
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
    this.version = '1.4.0';
    this.id = uuid();
    this.name = undefined;
    this.projectId = undefined;
    this.request = {};
    this.schedule = {};
    this.set(options);
  }

  initOptions(options) {
    options = options || {};
    options.request = options.request || new RequestFactory();
    return options;
  }

  isValid() {
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

export class ScenarioObj extends BaseConfig {
  constructor(options = {}) {
    super();
    this.id = undefined;
    this.name = undefined;
    this.type = "scenario";
    this.hashTree = [];
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

export class ResponseFactory {
  static TYPES = {
    HTTP: "HTTP",
    DUBBO: "DUBBO",
    SQL: "SQL",
    TCP: "TCP",
  }

  constructor(options = {}) {
    options.type = options.type || ResponseFactory.TYPES.HTTP
    switch (options.type) {
      case RequestFactory.TYPES.DUBBO:
        return new DubboRequest(options);
      case RequestFactory.TYPES.SQL:
        return new SqlRequest(options);
      case RequestFactory.TYPES.TCP:
        return new TCPRequest(options);
      default:
        return new HttpResponse(options);
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
    this.rest = [];
    this.authConfig = {verification: "No Auth", isEncrypt: false};
    this.headers = [];
    this.body = new Body(options.body);
    this.environment = options.environment;
    this.useEnvironment = options.useEnvironment;
    this.debugReport = undefined;
    this.doMultipartPost = options.doMultipartPost;
    this.connectTimeout = options.connectTimeout || 10 * 1000;
    this.responseTimeout = options.responseTimeout || 10 * 1000;
    this.followRedirects = options.followRedirects === undefined ? true : options.followRedirects;

    this.sets({parameters: KeyValue, rest: KeyValue, headers: KeyValue}, options);
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


export class Response extends BaseConfig {
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
  }
}


export class HttpResponse extends Response {
  constructor(options) {
    super(ResponseFactory.TYPES.HTTP, options);
    this.headers = [];
    this.body = new Body(options.body);
    this.statusCode = [];
    this.sets({statusCode: KeyValue, headers: KeyValue}, options);
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
    this.parameters = [];

    //设置TCPConfig的属性
    this.set(new TCPConfig(options));
    this.sets({parameters: KeyValue}, options);

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
    this.poolMax = undefined;
    this.timeout = undefined;
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
    this.type = "KeyValue";
    this.raw = undefined;
    this.kvs = [];
    this.binary = [];
    this.set(options);
    this.sets({kvs: KeyValue}, {binary: KeyValue}, options);
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
    return [BODY_TYPE.FORM_DATA, BODY_TYPE.WWW_FORM, BODY_TYPE.BINARY].indexOf(this.type) > 0;
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

export class Assertions extends BaseConfig {
  constructor(options) {
    super();
    this.resourceId = uuid();
    this.type = "Assertions";
    this.text = [];
    this.regex = [];
    this.jsonPath = [];
    this.jsr223 = [];
    this.xpath2 = [];
    this.duration = undefined;
    this.enable = true;
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
    this.scriptLanguage = "beanshell";
    this.set(options);
  }

  isValid() {
    return !!this.script && !!this.scriptLanguage;
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
    this.resourceId = uuid();
    this.active = false;
    this.type = "JSR223Processor";
    this.script = undefined;
    this.scriptLanguage = "beanshell";
    this.enable = true;
    this.hashTree = [];
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
    this.resourceId = uuid();
    this.type = "Extract";
    this.regex = [];
    this.json = [];
    this.xpath = [];
    this.enable = true;
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
    options.resourceId = options.resourceId || uuid();
    options.enable = options.enable === undefined ? true : options.enable;
  }
}

export class IfController extends Controller {
  constructor(options = {}) {
    super(Controller.TYPES.IF_CONTROLLER, options);
    this.type = "IfController";
    this.variable;
    this.operator;
    this.value;
    this.hashTree = [];
    this.set(options);
  }

  isValid() {
    if (!!this.operator && this.operator.indexOf("empty") > 0) {
      return !!this.variable && !!this.operator;
    }
    return !!this.variable && !!this.operator && !!this.value;
  }

  label() {
    if (this.isValid()) {
      let label = this.variable;
      if (this.operator) label += " " + this.operator;
      if (this.value) label += " " + this.value;
      return label;
    }
    return "";
  }
}

export class LoopController extends Controller {
  constructor(options = {}) {
    super("LoopController", options);
    this.type = "LoopController";
    this.active = false;
    this.loopType = "LOOP_COUNT";
    this.countController = {loops: 0, interval: 0, proceed: true, requestResult: {}};
    this.forEachController = {inputVal: "", returnVal: "", interval: 0, requestResult: {}};
    this.whileController = {variable: "", operator: "", value: "", timeout: 0, requestResult: {}};
    this.hashTree = [];
    this.set(options);
  }

  isValid() {
    if (!!this.operator && this.operator.indexOf("empty") > 0) {
      return !!this.variable && !!this.operator;
    }
    return !!this.variable && !!this.operator && !!this.value;
  }

  label() {
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
    options.resourceId = options.resourceId || uuid();
    options.enable = options.enable === undefined ? true : options.enable;
  }
}

export class ConstantTimer extends Timer {
  constructor(options = {}) {
    super(Timer.TYPES.CONSTANT_TIMER, options);
    this.delay;
    this.type = "ConstantTimer";
    this.hashTree = [];
    this.set(options);
  }

  isValid() {
    return this.delay > 0;
  }

  label() {
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
        this.path = this.getPostQueryParameters(request, decodeURIComponent(url.pathname + (request.path ? request.path : '')));
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
  constructor(request) {
    // Request 复制
    let obj = request.clone();
    // 去掉无效的kv
    obj.args = obj.args.filter(arg => {
      return arg.isValid();
    });
    obj.attachmentArgs = obj.attachmentArgs.filter(arg => {
      return arg.isValid();
    });
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
  constructor(request) {
    let obj = request.clone();
    if (request.useEnvironment) {
      return obj;
    }
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
    this.addScenarios(testPlan, test.id, test.request);

    this.jmeterTestPlan = new JMeterTestPlan();
    this.jmeterTestPlan.put(testPlan);
  }

  addScenarios(testPlan, testId, request) {

    let threadGroup = new ThreadGroup(request.name || "");

    if (!request.isValid()) return;
    let sampler;
    if (request instanceof DubboRequest) {
      sampler = new DubboSample(request.name || "", new JMXDubboRequest(request));
    } else if (request instanceof HttpRequest) {
      sampler = new HTTPSamplerProxy(request.name || "", new JMXHttpRequest(request, false));
      this.addRequestHeader(sampler, request);
      this.addRequestArguments(sampler, request);
      this.addRequestBody(sampler, request, testId);
    } else if (request instanceof SqlRequest) {
      sampler = new JDBCSampler(request.name || "", request);
      this.addRequestVariables(sampler, request);
    } else if (request instanceof TCPRequest) {
      sampler = new TCPSampler(request.name || "", new JMXTCPRequest(request));
    }

    this.addDNSCacheManager(sampler, false, request.useEnvironment);

    this.addRequestExtractor(sampler, request);

    this.addRequestAssertion(sampler, request);

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
    testPlan.put(threadGroup);
  }

  addEnvironments(environments, target) {
    let keys = new Set();
    target.forEach(item => {
      keys.add(item.name);
    });
    let envArray = environments;
    if (!(envArray instanceof Array)) {
      envArray = JSON.parse(environments);
    }
    envArray.forEach(item => {
      if (item.name && !keys.has(item.name)) {
        target.push(new KeyValue({name: item.name, value: item.value}));
      }
    })
  }

  addScenarioVariables(threadGroup, scenario) {
    if (scenario.environment) {
      let config = scenario.environment.config;
      if (!(scenario.environment.config instanceof Object)) {
        config = JSON.parse(scenario.environment.config);
      }
      this.addEnvironments(config.commonConfig.variables, scenario.variables)
    }
    let args = this.filterKV(scenario.variables);
    if (args.length > 0) {
      let name = scenario.name + " Variables";
      threadGroup.put(new Arguments(name, args));
    }
  }

  addRequestVariables(httpSamplerProxy, request) {
    let name = request.name + " Variables";
    let variables = this.filterKV(request.variables);
    if (variables && variables.length > 0) {
      httpSamplerProxy.put(new Arguments(name, variables));
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
    if (scenario.environment) {
      let config = scenario.environment.config;
      if (!(scenario.environment.config instanceof Object)) {
        config = JSON.parse(scenario.environment.config);
      }
      this.addEnvironments(config.httpConfig.headers, scenario.headers)
    }
    let headers = this.filterKV(scenario.headers);
    if (headers.length > 0) {
      let name = scenario.name + " Headers";
      threadGroup.put(new HeaderManager(name, headers));
    }
  }

  addRequestHeader(httpSamplerProxy, request) {
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
      sampler.put(new JMXConstantTimer(request.timer.label(), request.timer));
    }
  }

  getController(sampler, request) {
    if (request.controller.isValid() && request.controller.enable) {
      if (request.controller instanceof IfController) {
        let name = request.controller.label();
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
    for (let index in request.headers) {
      if (request.headers.hasOwnProperty(index)) {
        if (request.headers[index].name === 'Content-Type') {
          request.headers.splice(index, 1);
          break;
        }
      }
    }
    request.headers.push(new KeyValue({name: 'Content-Type', value: type}));
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
      httpSamplerProxy.boolProp('HTTPSampler.postBodyRaw', true);
      body.push({name: '', value: request.body.raw, encode: false, enable: true});
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
          files.push(arg);
        });
      }
    });
    httpSamplerProxy.add(new HTTPsamplerFiles(files));
  }

  addRequestAssertion(httpSamplerProxy, request) {
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

    if (assertions.duration.isValid()) {
      let name = "Response In Time: " + assertions.duration.value
      httpSamplerProxy.put(new DurationAssertion(name, assertions.duration.value));
    }
  }

  getJSONPathAssertion(jsonPath) {
    let name = jsonPath.description;
    return new JSONPathAssertion(name, jsonPath);
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


