import Sampler from "../sampler";

const DEFAULT_OPTIONS = {
  options: {
    attributes: {
      guiclass: "TestBeanGUI",
      testclass: "JDBCSampler",
      testname: "JDBC Request",
      enabled: "true"
    },
  }
};

export default class JDBCSampler extends Sampler {
  constructor(options = DEFAULT_OPTIONS) {
    super(options);
    this.type = "JDBCSampler";
    this.hashTree = [];
    this.variables = [];
    this.environmentId = undefined;
    this.dataSource = undefined;
    this.dataSourceId = undefined;
    this.query = undefined;
    this.queryType = undefined;
    this.queryArguments = undefined;
    this.queryArgumentsTypes = undefined;
    this.queryTimeout = undefined;
    this.resultSetHandler = undefined;
    this.resultSetMaxRows = undefined;
    this.resultVariable = undefined;
    this.variableNames = undefined;
  }
}

export const schema = {
  JDBCSampler: JDBCSampler
}
