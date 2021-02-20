import PostProcessor from "../pre-processor";

const DEFAULT_OPTIONS = {
  options: {
    attributes: {
      guiclass: "TestBeanGUI",
      testclass: "JSR223PreProcessor",
      testname: "JSR223 PreProcessor",
      enabled: "true"
    },
  }
};

export default class JSR223PreProcessor extends PostProcessor {
  constructor(options = DEFAULT_OPTIONS) {
    super(options);
    this.type = "JSR223PreProcessor";
    this.scriptLanguage = "beanshell";
    this.parameters = [];
    this.filename = undefined;
    this.cacheKey = undefined;
    this.enable = true;
    this.script = undefined;
  }
}

export const schema = {
  JSR223PreProcessor: JSR223PreProcessor
}
