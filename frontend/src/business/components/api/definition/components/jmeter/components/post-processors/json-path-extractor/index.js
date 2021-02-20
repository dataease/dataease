import PostProcessor from "../post-processor";

const DEFAULT_OPTIONS = {
  options: {
    attributes: {
      guiclass: "JSONPostProcessorGui",
      testclass: "JSONPostProcessor",
      testname: "JSONPostProcessor",
      enabled: "true"
    },
  }
};

export default class JSONPostProcessor extends PostProcessor {
  constructor(options = DEFAULT_OPTIONS) {
    super(options);
    this.scope = this.initStringProp("Sample.scope")
    this.variable = this.initStringProp("Scope.variable")
    this.referenceNames = this.initStringProp("JSONPostProcessor.referenceNames")
    this.jsonPathExprs = this.initStringProp("JSONPostProcessor.jsonPathExprs")
    this.matchNumber = this.initStringProp("JSONPostProcessor.match_number")
    this.defaultValues = this.initStringProp("JSONPostProcessor.defaultValues")
    this.computeConcat = this.initBoolProp("JSONPostProcessor.compute_concat")
  }
}

export const schema = {
  JSONPostProcessor: JSONPostProcessor
}

