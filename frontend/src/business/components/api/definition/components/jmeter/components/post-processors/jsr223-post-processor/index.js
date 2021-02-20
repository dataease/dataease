import PostProcessor from "../post-processor";

const DEFAULT_OPTIONS = {
  options: {
    attributes: {
      guiclass: "TestBeanGUI",
      testclass: "JSR223PostProcessor",
      testname: "JSR223 PostProcessor",
      enabled: "true"
    },
  }
};

export default class JSR223PostProcessor extends PostProcessor {
  constructor(options = DEFAULT_OPTIONS) {
    super(options);
    this.type = "JSR223PostProcessor";
    this.scriptLanguage = "beanshell";
    this.parameters = [];
    this.filename = undefined;
    this.cacheKey = true;
    this.enable = true;
    this.script = undefined;
  }
}

export const schema = {
  JSR223PostProcessor: JSR223PostProcessor
}
