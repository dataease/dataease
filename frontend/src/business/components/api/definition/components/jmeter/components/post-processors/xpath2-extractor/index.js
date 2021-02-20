import PostProcessor from "../post-processor";

const DEFAULT_OPTIONS = {
  options: {
    attributes: {
      guiclass: "XPath2ExtractorGui",
      testclass: "XPath2Extractor",
      testname: "XPath Extractor",
      enabled: "true"
    },
  }
};

export default class XPath2Extractor extends PostProcessor {
  constructor(options = DEFAULT_OPTIONS) {
    super(options);
    this.scope = this.initStringProp("Sample.scope")
    this.variable = this.initStringProp("Scope.variable")
    this.refName = this.initStringProp("XPathExtractor2.refname")
    this.xpathQuery = this.initStringProp("XPathExtractor2.xpathQuery")
    this.matchNumber = this.initStringProp("XPathExtractor2.match_number", 0)
    this.default = this.initStringProp("XPathExtractor2.default")
    this.namespaces = this.initStringProp("XPathExtractor2.namespaces")
    this.fragment = this.initBoolProp("XPathExtractor2.fragment")
  }
}

export const schema = {
  XPath2Extractor: XPath2Extractor
}

