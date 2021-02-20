const INDENT = '  '; // 缩进2空格

export class Element {
  constructor(name, attributes, value) {
    this.indent = '';
    this.name = name;  // 标签名
    this.attributes = attributes || {}; // 属性
    this.value = undefined; // 基础类型的内容
    this.elements = []; // 子节点

    if (value instanceof Element) {
      this.elements.push(value);
    } else {
      this.value = value;
    }
  }

  set(value) {
    this.elements = [];
    this.value = value;
  }

  add(element) {
    if (element instanceof Element) {
      this.value = undefined;
      this.elements.push(element);
      return element;
    }
  }

  getDefault(value, defaultValue) {
    return value === undefined ? defaultValue : value;
  }

  commonValue(tag, name, value, defaultValue) {
    let v = this.getDefault(value, defaultValue);
    return this.add(new Element(tag, {name: name}, v));
  }

  boolProp(name, value, defaultValue) {
    return this.commonValue('boolProp', name, value, defaultValue);
  }

  intProp(name, value, defaultValue) {
    return this.commonValue('intProp', name, value, defaultValue);
  }

  longProp(name, value, defaultValue) {
    return this.commonValue('longProp', name, value, defaultValue);
  }

  stringProp(name, value, defaultValue) {
    return this.commonValue('stringProp', name, value, defaultValue);
  }

  collectionProp(name) {
    return this.commonValue('collectionProp', name);
  }

  elementProp(name, elementType) {
    return this.add(new Element('elementProp', {name: name, elementType: elementType}));
  }

  isEmptyValue() {
    return this.value === undefined || this.value === '';
  }

  isEmptyElement() {
    return this.elements.length === 0;
  }

  isEmpty() {
    return this.isEmptyValue() && this.isEmptyElement();
  }

  replace(str) {
    if (!str || !(typeof str === 'string')) return str;
    return str.replace(/&/g, "&amp;").replace(/</g, "&lt;").replace(/>/g, "&gt;").replace(/'/g, "&apos;").replace(/"/g, "&quot;");
  }

  toXML(indent) {
    if (indent) {
      this.indent = indent;
    }

    let str = this.start();
    str += this.content();
    str += this.end();
    return str;
  }

  start() {
    let str = this.indent + '<' + this.replace(this.name);
    for (let key in this.attributes) {
      if (this.attributes.hasOwnProperty(key)) {
        str += ' ' + this.replace(key) + '="' + this.replace(this.attributes[key]) + '"';
      }
    }
    if (this.isEmpty()) {
      str += '/>';
    } else {
      str += '>';
    }
    return str;
  }

  content() {
    if (!this.isEmptyValue()) {
      return this.replace(this.value);
    }

    let str = '';
    let parent = this;
    if (this.elements.length > 0) {
      str += '\n';
      this.elements.forEach(e => {
        e.indent += parent.indent + INDENT;
        str += e.toXML();
      });
    }
    return str;
  }

  end() {
    if (this.isEmpty()) {
      return '\n';
    }
    let str = '</' + this.replace(this.name) + '>\n';
    if (!this.isEmptyValue()) {
      return str;
    }
    if (!this.isEmptyElement()) {
      return this.indent + str;
    }
  }
}

// HashTree, 只能添加TestElement的子元素，没有基础类型内容
export class HashTree extends Element {
  constructor() {
    super('hashTree');
  }

  add(te) {
    if (te instanceof TestElement) {
      super.add(te);
    }
  }
}

// TestElement包含2部分，Element 和 HashTree
export class TestElement extends Element {
  constructor(name, attributes, value) {
    // Element, 只能添加Element
    super(name, attributes, value);
    // HashTree, 只能添加TestElement
    this.hashTree = new HashTree();
  }

  put(te) {
    this.hashTree.add(te);
  }

  toXML() {
    let str = super.toXML();
    str += this.hashTree.toXML(this.indent);
    return str;
  }
}

export class DefaultTestElement extends TestElement {
  constructor(tag, guiclass, testclass, testname, enabled) {
    super(tag, {
      guiclass: guiclass,
      testclass: testclass,
      testname: testname === undefined ? tag + ' Name' : testname,
      enabled: enabled || true
    });
  }
}

export class TestPlan extends DefaultTestElement {
  constructor(testName, props) {
    super('TestPlan', 'TestPlanGui', 'TestPlan', testName);

    props = props || {};
    this.boolProp("TestPlan.functional_mode", props.mode, false);
    this.boolProp("TestPlan.serialize_threadgroups", props.stg, true);
    this.boolProp("TestPlan.tearDown_on_shutdown", props.tos, true);
    this.stringProp("TestPlan.comments", props.comments);
    this.stringProp("TestPlan.user_define_classpath", props.classpath);
    this.add(new ElementArguments(props.args, "TestPlan.user_defined_variables", "User Defined Variables"));
  }
}

export class ThreadGroup extends DefaultTestElement {
  constructor(testName, props) {
    super('ThreadGroup', 'ThreadGroupGui', 'ThreadGroup', testName);

    props = props || {};
    this.intProp("ThreadGroup.num_threads", props.threads, 1);
    this.intProp("ThreadGroup.ramp_time", props.ramp, 1);
    this.longProp("ThreadGroup.delay", props.delay, 0);
    this.longProp("ThreadGroup.duration", props.delay, 0);
    this.stringProp("ThreadGroup.on_sample_error", props.error, "continue");
    this.boolProp("ThreadGroup.scheduler", props.scheduler, false);

    let loopAttrs = {
      name: "ThreadGroup.main_controller",
      elementType: "LoopController",
      guiclass: "LoopControlPanel",
      testclass: "LoopController",
      testname: "Loop Controller",
      enabled: "true"
    };
    let loopProps = props.loopProps || {};
    let loopController = this.add(new Element('elementProp', loopAttrs));
    loopController.boolProp('LoopController.continue_forever', loopProps.continue, false);
    loopController.stringProp('LoopController.loops', loopProps.loops, 1);
  }
}

export class DubboSample extends DefaultTestElement {
  constructor(testName, request = {}) {
    super('io.github.ningyu.jmeter.plugin.dubbo.sample.DubboSample',
      'io.github.ningyu.jmeter.plugin.dubbo.gui.DubboSampleGui',
      'io.github.ningyu.jmeter.plugin.dubbo.sample.DubboSample', testName);
    this.request = request;

    this.stringProp("FIELD_DUBBO_CONFIG_CENTER_PROTOCOL", this.request.configCenter.protocol);
    this.stringProp("FIELD_DUBBO_CONFIG_CENTER_GROUP", this.request.configCenter.group);
    this.stringProp("FIELD_DUBBO_CONFIG_CENTER_NAMESPACE", this.request.configCenter.namespace);
    this.stringProp("FIELD_DUBBO_CONFIG_CENTER_USER_NAME", this.request.configCenter.username);
    this.stringProp("FIELD_DUBBO_CONFIG_CENTER_PASSWORD", this.request.configCenter.password);
    this.stringProp("FIELD_DUBBO_CONFIG_CENTER_ADDRESS", this.request.configCenter.address);
    this.stringProp("FIELD_DUBBO_CONFIG_CENTER_TIMEOUT", this.request.configCenter.timeout);

    this.stringProp("FIELD_DUBBO_REGISTRY_PROTOCOL", this.request.registryCenter.protocol);
    this.stringProp("FIELD_DUBBO_REGISTRY_GROUP", this.request.registryCenter.group);
    this.stringProp("FIELD_DUBBO_REGISTRY_USER_NAME", this.request.registryCenter.username);
    this.stringProp("FIELD_DUBBO_REGISTRY_PASSWORD", this.request.registryCenter.password);
    this.stringProp("FIELD_DUBBO_ADDRESS", this.request.registryCenter.address);
    this.stringProp("FIELD_DUBBO_REGISTRY_TIMEOUT", this.request.registryCenter.timeout);

    this.stringProp("FIELD_DUBBO_TIMEOUT", this.request.consumerAndService.timeout);
    this.stringProp("FIELD_DUBBO_VERSION", this.request.consumerAndService.version);
    this.stringProp("FIELD_DUBBO_RETRIES", this.request.consumerAndService.retries);
    this.stringProp("FIELD_DUBBO_GROUP", this.request.consumerAndService.group);
    this.stringProp("FIELD_DUBBO_CONNECTIONS", this.request.consumerAndService.connections);
    this.stringProp("FIELD_DUBBO_LOADBALANCE", this.request.consumerAndService.loadBalance);
    this.stringProp("FIELD_DUBBO_ASYNC", this.request.consumerAndService.async);
    this.stringProp("FIELD_DUBBO_CLUSTER", this.request.consumerAndService.cluster);

    this.stringProp("FIELD_DUBBO_RPC_PROTOCOL", this.request.protocol);
    this.stringProp("FIELD_DUBBO_INTERFACE", this.request.interface);
    this.stringProp("FIELD_DUBBO_METHOD", this.request.method);

    this.intProp("FIELD_DUBBO_METHOD_ARGS_SIZE", this.request.args.length);
    this.intProp("FIELD_DUBBO_ATTACHMENT_ARGS_SIZE", this.request.attachmentArgs.length);
    this.request.args.forEach((arg, i) => {
      if (!!arg.name || !!arg.value) {
        let index = i + 1;
        this.stringProp("FIELD_DUBBO_METHOD_ARGS_PARAM_TYPE" + index, arg.name);
        this.stringProp("FIELD_DUBBO_METHOD_ARGS_PARAM_VALUE" + index, arg.value);
      }
    })
    this.request.attachmentArgs.forEach((arg, i) => {
      if (!!arg.name || !!arg.value) {
        let index = i + 1;
        this.stringProp("FIELD_DUBBO_ATTACHMENT_ARGS_KEY" + index, arg.name);
        this.stringProp("FIELD_DUBBO_ATTACHMENT_ARGS_VALUE" + index, arg.value);
      }
    })
  }
}

export class JDBCSampler extends DefaultTestElement {
  constructor(testName, request = {}) {
    super('JDBCSampler', 'TestBeanGUI', 'JDBCSampler', testName);

    this.stringProp("dataSource", request.dataSource);
    this.stringProp("query", request.query);
    this.stringProp("queryTimeout", request.queryTimeout);
    this.stringProp("resultVariable", request.resultVariable);
    this.stringProp("variableNames", request.variableNames);
    this.stringProp("queryArguments");
    this.stringProp("queryArgumentsTypes");
    this.stringProp("resultSetMaxRows");
    this.stringProp("resultSetHandler", 'Store as String');
    this.stringProp("queryType", 'Callable Statement');
  }
}

export class TCPSampler extends DefaultTestElement {
  constructor(testName, request = {}) {
    super('TCPSampler', 'TCPSamplerGui', 'TCPSampler', testName);

    this.stringProp("TCPSampler.classname", request.classname);
    this.stringProp("TCPSampler.server", request.server);
    this.stringProp("TCPSampler.port", request.port);
    this.stringProp("TCPSampler.ctimeout", request.ctimeout);
    this.stringProp("TCPSampler.timeout", request.timeout);
    this.boolProp("TCPSampler.reUseConnection", request.reUseConnection);
    this.boolProp("TCPSampler.nodelay", request.nodelay);
    this.boolProp("TCPSampler.closeConnection", request.closeConnection);
    this.stringProp("TCPSampler.soLinger", request.soLinger);
    this.stringProp("TCPSampler.EolByte", request.eolByte);
    this.stringProp("TCPSampler.request", request.request);
    this.stringProp("ConfigTestElement.username", request.username);
    this.stringProp("ConfigTestElement.password", request.password);
  }
}

export class HTTPSamplerProxy extends DefaultTestElement {
  constructor(testName, options = {}) {
    super('HTTPSamplerProxy', 'HttpTestSampleGui', 'HTTPSamplerProxy', testName);

    this.stringProp("HTTPSampler.domain", options.domain);
    this.stringProp("HTTPSampler.protocol", options.protocol);
    this.stringProp("HTTPSampler.path", options.path);

    this.stringProp("HTTPSampler.method", options.method);
    this.stringProp("HTTPSampler.contentEncoding", options.encoding, "UTF-8");
    if (!options.port) {
      this.stringProp("HTTPSampler.port", "");
    } else {
      this.stringProp("HTTPSampler.port", options.port);
    }
    if (options.connectTimeout) {
      this.stringProp('HTTPSampler.connect_timeout', options.connectTimeout);
    }
    if (options.responseTimeout) {
      this.stringProp('HTTPSampler.response_timeout', options.responseTimeout);
    }
    if (options.followRedirects) {
      this.boolProp('HTTPSampler.follow_redirects', options.followRedirects, true);
    }

    this.boolProp("HTTPSampler.use_keepalive", options.keepalive, true);
    this.boolProp("HTTPSampler.DO_MULTIPART_POST", options.doMultipartPost, false);
  }
}

// 这是一个Element
export class HTTPSamplerArguments extends Element {
  constructor(args) {
    super('elementProp', {
      name: "HTTPsampler.Arguments", // s必须小写
      elementType: "Arguments",
      guiclass: "HTTPArgumentsPanel",
      testclass: "Arguments",
      enabled: "true"
    });

    this.args = args || [];

    let collectionProp = this.collectionProp('Arguments.arguments');
    this.args.forEach(arg => {
      if (arg.enable === true || arg.enable === undefined) { // 非禁用的条件加入执行
        let elementProp = collectionProp.elementProp(arg.name, 'HTTPArgument');
        elementProp.boolProp('HTTPArgument.always_encode', arg.encode, true);
        elementProp.boolProp('HTTPArgument.use_equals', arg.equals, true);
        if (arg.name) {
          elementProp.stringProp('Argument.name', arg.name);
        }
        elementProp.stringProp('Argument.value', arg.value);
        elementProp.stringProp('Argument.metadata', arg.metadata || "=");
        if (arg.contentType) {
          elementProp.stringProp('HTTPArgument.content_type', arg.contentType, "");
        }
      }
    });
  }
}

export class HTTPsamplerFiles extends Element {
  constructor(args) {
    super('elementProp', {
      name: "HTTPsampler.Files",
      elementType: "HTTPFileArgs",
    });

    this.args = args || {};

    let collectionProp = this.collectionProp('HTTPFileArgs.files');
    this.args.forEach(arg => {
      let elementProp = collectionProp.elementProp(arg.value, 'HTTPFileArg');
      elementProp.stringProp('File.path', arg.value);
      elementProp.stringProp('File.paramname', arg.name);
      elementProp.stringProp('File.mimetype', arg.contentType || "application/octet-stream");
    });
  }
}

export class CookieManager extends DefaultTestElement {
  constructor(testName) {
    super('CookieManager', 'CookiePanel', 'CookieManager', testName);
    this.collectionProp('CookieManager.cookies');
    this.boolProp('CookieManager.clearEachIteration', false, false);
    this.boolProp('CookieManager.controlledByThreadGroup', false, false);
  }
}

export class DurationAssertion extends DefaultTestElement {
  constructor(testName, duration) {
    super('DurationAssertion', 'DurationAssertionGui', 'DurationAssertion', testName);
    this.duration = duration || 0;
    this.stringProp('DurationAssertion.duration', this.duration);
  }
}

export class ResponseAssertion extends DefaultTestElement {
  constructor(testName, assertion) {
    super('ResponseAssertion', 'AssertionGui', 'ResponseAssertion', testName);
    this.assertion = assertion || {};

    this.stringProp('Assertion.test_field', this.assertion.field);
    this.boolProp('Assertion.assume_success', this.assertion.assumeSuccess);
    this.intProp('Assertion.test_type', this.assertion.type);
    this.stringProp('Assertion.custom_message', this.assertion.message);

    let collectionProp = this.collectionProp('Asserion.test_strings');
    let random = Math.floor(Math.random() * 10000);
    collectionProp.stringProp(random, this.assertion.value);
  }
}

export class JSONPathAssertion extends DefaultTestElement {
  constructor(testName, jsonPath) {
    super('JSONPathAssertion', 'JSONPathAssertionGui', 'JSONPathAssertion', testName);
    this.jsonPath = jsonPath || {};

    this.stringProp('JSON_PATH', this.jsonPath.expression);
    this.stringProp('EXPECTED_VALUE', this.jsonPath.expect);
    this.boolProp('JSONVALIDATION', true);
    this.boolProp('EXPECT_NULL', false);
    this.boolProp('INVERT', false);
    this.boolProp('ISREGEX', true);
  }
}

export class ResponseCodeAssertion extends ResponseAssertion {
  constructor(testName, type, value, assumeSuccess, message) {
    let assertion = {
      field: 'Assertion.response_code',
      type: type,
      value: value,
      assumeSuccess: assumeSuccess,
      message: message,
    }
    super(testName, assertion)
  }
}

export class ResponseDataAssertion extends ResponseAssertion {
  constructor(testName, type, value, assumeSuccess, message) {
    let assertion = {
      field: 'Assertion.response_data',
      type: type,
      value: value,
      assumeSuccess: assumeSuccess,
      message: message,
    }
    super(testName, assertion)
  }
}

export class ResponseHeadersAssertion extends ResponseAssertion {
  constructor(testName, type, value, assumeSuccess, message) {
    let assertion = {
      field: 'Assertion.response_headers',
      type: type,
      value: value,
      assumeSuccess: assumeSuccess,
      message: message,
    }
    super(testName, assertion)
  }
}

export class BeanShellProcessor extends DefaultTestElement {
  constructor(tag, guiclass, testclass, testname, processor) {
    super(tag, guiclass, testclass, testname);
    this.processor = processor || {};
    this.boolProp('resetInterpreter', false);
    this.stringProp('parameters');
    this.stringProp('filename');
    this.stringProp('script', processor.script);
  }
}

export class JSR223Processor extends DefaultTestElement {
  constructor(tag, guiclass, testclass, testname, processor) {
    super(tag, guiclass, testclass, testname);
    this.processor = processor || {};
    this.stringProp('cacheKey', 'true');
    this.stringProp('filename');
    this.stringProp('parameters');
    this.stringProp('script', this.processor.script);
    this.stringProp('scriptLanguage', this.processor.language);
  }
}

export class JSR223PreProcessor extends JSR223Processor {
  constructor(testName, processor) {
    super('JSR223PreProcessor', 'TestBeanGUI', 'JSR223PreProcessor', testName, processor)
  }
}

export class JSR223PostProcessor extends JSR223Processor {
  constructor(testName, processor) {
    super('JSR223PostProcessor', 'TestBeanGUI', 'JSR223PostProcessor', testName, processor)
  }
}

export class BeanShellPreProcessor extends BeanShellProcessor {
  constructor(testName, processor) {
    super('BeanShellPreProcessor', 'TestBeanGUI', 'BeanShellPreProcessor', testName, processor)
  }
}

export class BeanShellPostProcessor extends BeanShellProcessor {
  constructor(testName, processor) {
    super('BeanShellPostProcessor', 'TestBeanGUI', 'BeanShellPostProcessor', testName, processor)
  }
}

export class IfController extends DefaultTestElement {
  constructor(testName, controller = {}) {
    super('IfController', 'IfControllerPanel', 'IfController', testName);

    this.stringProp('IfController.comments', controller.comments);
    this.stringProp('IfController.condition', controller.condition);
    this.boolProp('IfController.evaluateAll', controller.evaluateAll, false);
    this.boolProp('IfController.useExpression', controller.useExpression, true);
  }
}

export class ConstantTimer extends DefaultTestElement {
  constructor(testName, timer = {}) {
    super('ConstantTimer', 'ConstantTimerGui', 'ConstantTimer', testName);

    this.stringProp('ConstantTimer.delay', timer.delay);
  }
}

export class HeaderManager extends DefaultTestElement {
  constructor(testName, headers) {
    super('HeaderManager', 'HeaderPanel', 'HeaderManager', testName);
    this.headers = headers || [];

    let collectionProp = this.collectionProp('HeaderManager.headers');
    this.headers.forEach(header => {
      if (header.enable === true || header.enable === undefined) {
        let elementProp = collectionProp.elementProp('', 'Header');
        elementProp.stringProp('Header.name', header.name);
        elementProp.stringProp('Header.value', header.value);
      }
    });
  }
}

export class DNSCacheManager extends DefaultTestElement {
  constructor(testName, hosts) {
    super('DNSCacheManager', 'DNSCachePanel', 'DNSCacheManager', testName);
    let collectionPropServers = this.collectionProp('DNSCacheManager.servers');
    let collectionPropHosts = this.collectionProp('DNSCacheManager.hosts');

    hosts.forEach(host => {
      let elementProp = collectionPropHosts.elementProp(host.domain, 'StaticHost');
      elementProp.stringProp('StaticHost.Name', host.domain);
      elementProp.stringProp('StaticHost.Address', host.ip);
    });

    let boolProp = this.boolProp('DNSCacheManager.isCustomResolver', true);
  }
}

export class JDBCDataSource extends DefaultTestElement {
  constructor(testName, datasource) {
    super('JDBCDataSource', 'TestBeanGUI', 'JDBCDataSource', testName);

    this.boolProp('autocommit', true);
    this.boolProp('keepAlive', true);
    this.boolProp('preinit', false);
    this.stringProp('dataSource', datasource.name);
    this.stringProp('dbUrl', datasource.dbUrl);
    this.stringProp('driver', datasource.driver);
    this.stringProp('username', datasource.username);
    this.stringProp('password', datasource.password);
    this.stringProp('poolMax', datasource.poolMax);
    this.stringProp('timeout', datasource.timeout);
    this.stringProp('connectionAge', '5000');
    this.stringProp('trimInterval', '60000');
    this.stringProp('transactionIsolation', 'DEFAULT');
    this.stringProp('checkQuery');
    this.stringProp('initQuery');
    this.stringProp('connectionProperties');
  }
}

export class Arguments extends DefaultTestElement {
  constructor(testName, args) {
    super('Arguments', 'ArgumentsPanel', 'Arguments', testName);
    this.args = args || [];

    let collectionProp = this.collectionProp('Arguments.arguments');

    this.args.forEach(arg => {
      if (arg.enable === true || arg.enable === undefined) { // 非禁用的条件加入执行
        let elementProp = collectionProp.elementProp(arg.name, 'Argument');
        elementProp.stringProp('Argument.name', arg.name);
        elementProp.stringProp('Argument.value', arg.value);
        elementProp.stringProp('Argument.desc', arg.desc);
        elementProp.stringProp('Argument.metadata', arg.metadata, "=");
      }
    });
  }
}

export class ElementArguments extends Element {
  constructor(args, name, testName) {
    super('elementProp', {
      name: name || "arguments",
      elementType: "Arguments",
      guiclass: "ArgumentsPanel",
      testclass: "Arguments",
      testname: testName || "",
      enabled: "true"
    });

    let collectionProp = this.collectionProp('Arguments.arguments');
    if (args) {
      args.forEach(arg => {
        if (arg.enable === true || arg.enable === undefined) { // 非禁用的条件加入执行
          let elementProp = collectionProp.elementProp(arg.name, 'Argument');
          elementProp.stringProp('Argument.name', arg.name);
          elementProp.stringProp('Argument.value', arg.value);
          elementProp.stringProp('Argument.metadata', arg.metadata, "=");
        }
      });
    }
  }
}

export class RegexExtractor extends DefaultTestElement {
  constructor(testName, props) {
    super('RegexExtractor', 'RegexExtractorGui', 'RegexExtractor', testName);
    this.props = props || {}
    this.stringProp('RegexExtractor.useHeaders', props.headers);
    this.stringProp('RegexExtractor.refname', props.name);
    this.stringProp('RegexExtractor.regex', props.expression);
    this.stringProp('RegexExtractor.template', props.template);
    this.stringProp('RegexExtractor.default', props.default);
    this.stringProp('RegexExtractor.match_number', props.match);
  }
}

export class JSONPostProcessor extends DefaultTestElement {
  constructor(testName, props) {
    super('JSONPostProcessor', 'JSONPostProcessorGui', 'JSONPostProcessor', testName);
    this.props = props || {}
    this.stringProp('JSONPostProcessor.referenceNames', props.name);
    this.stringProp('JSONPostProcessor.jsonPathExprs', props.expression);
    this.stringProp('JSONPostProcessor.match_numbers', props.match);
  }
}

export class XPath2Extractor extends DefaultTestElement {
  constructor(testName, props) {
    super('XPath2Extractor', 'XPath2ExtractorGui', 'XPath2Extractor', testName);
    this.props = props || {}
    this.stringProp('XPathExtractor2.default', props.default);
    this.stringProp('XPathExtractor2.refname', props.name);
    this.stringProp('XPathExtractor2.xpathQuery', props.expression);
    this.stringProp('XPathExtractor2.namespaces', props.namespaces);
    this.stringProp('XPathExtractor2.matchNumber', props.match);
  }
}
