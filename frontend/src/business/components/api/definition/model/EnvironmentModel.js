import {BaseConfig, DatabaseConfig, KeyValue} from "./ApiTestModel";
import {TCPConfig} from "@/business/components/api/test/model/ScenarioModel";

export class Environment extends BaseConfig {
  constructor(options = {}) {

    super();

    this.projectId = undefined;
    this.name = undefined;
    this.id = undefined;
    this.config = undefined;

    this.set(options);
    this.sets({}, options);
  }

  initOptions(options = {}) {
    this.config = new Config(options.config);
    return options;
  }
}

export class Config extends BaseConfig {
  constructor(options = {}) {
    super();
    this.commonConfig = undefined;
    this.httpConfig = undefined;
    this.databaseConfigs = [];
    this.tcpConfig = undefined;

    this.set(options);
    this.sets({databaseConfigs: DatabaseConfig}, options);
  }

  initOptions(options = {}) {
    this.commonConfig = new CommonConfig(options.commonConfig);
    this.httpConfig = new HttpConfig(options.httpConfig);
    options.databaseConfigs = options.databaseConfigs || [];
    options.tcpConfig = new TCPConfig(options.tcpConfig);
    return options;
  }
}

export class CommonConfig extends BaseConfig {
  constructor(options = {}) {
    super();
    this.variables = [];
    this.enableHost = false;
    this.hosts = [];

    this.set(options);
    this.sets({variables: KeyValue, hosts: Host}, options);
  }

  initOptions(options = {}) {
    options.variables = options.variables || [new KeyValue()];
    options.hosts = options.hosts || [];
    return options;
  }
}

export class HttpConfig extends BaseConfig {
  constructor(options = {}) {
    super();

    this.socket = '';
    this.domain = '';
    this.headers = [];
    this.protocol = 'https';
    this.port = '';

    this.set(options);
    this.sets({headers: KeyValue}, options);
  }

  initOptions(options = {}) {
    options.headers = options.headers || [new KeyValue()];
    return options;
  }
}

export class Host extends BaseConfig {
  constructor(options = {}) {
    super();

    this.ip = undefined;
    this.domain = undefined;
    this.status = undefined;
    this.annotation = undefined;
    this.uuid = undefined;

    this.set(options);
  }
}


/* ---------- Functions ------- */

export function compatibleWithEnvironment(environment) {
  //兼容旧版本
  if (!environment.config) {
    let config = new Config();
    if (!(environment.variables instanceof Array)) {
      config.commonConfig.variables = JSON.parse(environment.variables);
    }
    if (environment.hosts && !(environment.hosts instanceof Array)) {
      config.commonConfig.hosts = JSON.parse(environment.hosts);
      config.commonConfig.enableHost = true;
    }
    if (!(environment.headers instanceof Array)) {
      config.httpConfig.headers = JSON.parse(environment.headers);
    }
    config.httpConfig.port = environment.port;
    config.httpConfig.protocol = environment.protocol;
    config.httpConfig.domain = environment.domain;
    config.httpConfig.socket = environment.socket;
    environment.config = JSON.stringify(config);
  }
}

export function parseEnvironment(environment) {
  compatibleWithEnvironment(environment);
  if (!(environment.config instanceof Config)) {
    environment.config = new Config(JSON.parse(environment.config));
  }
}
