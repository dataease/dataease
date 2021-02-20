import Assertion from "../assertion";

const DEFAULT_OPTIONS = {
  options: {
    attributes: {
      guiclass: "JSONPathAssertionGui",
      testclass: "JSONPathAssertion",
      testname: "JSONPath Assertion",
      enabled: "true"
    },
  }
};

export default class JSONPathAssertion extends Assertion {
  constructor(options = DEFAULT_OPTIONS) {
    super(options);

    this.jsonPath = this.initStringProp("JSON_PATH")
    this.jsonValidation = this.initBoolProp("JSONVALIDATION", false)
    this.isRegex = this.initBoolProp("ISREGEX", true)
    this.expectedValue = this.initStringProp("EXPECTED_VALUE")

    this.expectNull = this.initBoolProp("EXPECT_NULL", false)
    this.invert = this.initBoolProp("INVERT", false)

  }
}

export const schema = {
  JSONPathAssertion: JSONPathAssertion
}
