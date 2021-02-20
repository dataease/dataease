import PostProcessor from "../post-processor";

const DEFAULT_OPTIONS = {
  options: {
    attributes: {
      guiclass: "RegexExtractorGui",
      testclass: "RegexExtractor",
      testname: "RegexExtractor",
      enabled: "true"
    },
  }
};

export default class RegexExtractor extends PostProcessor {
  constructor(options = DEFAULT_OPTIONS) {
    super(options);
    this.scope = this.initStringProp("Sample.scope")
    this.variable = this.initStringProp("Scope.variable")
    this.useHeaders = this.initStringProp("RegexExtractor.useHeaders", "false")
    this.refName = this.initStringProp("RegexExtractor.refname")
    this.regex = this.initStringProp("RegexExtractor.regex")
    this.template = this.initStringProp("RegexExtractor.template")
    this.matchNumber = this.initStringProp("RegexExtractor.match_number")
    this.default = this.initStringProp("RegexExtractor.default")
    this.defaultEmpty = this.initBoolProp("RegexExtractor.default_empty_value")
  }
}

export const schema = {
  RegexExtractor: RegexExtractor
}

