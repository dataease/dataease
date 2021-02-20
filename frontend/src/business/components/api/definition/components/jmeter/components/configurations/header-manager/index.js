import Configuration from "../configuration";

const DEFAULT_OPTIONS = {
  options: {
    attributes: {
      guiclass: "HeaderPanel",
      testclass: "HeaderManager",
      testname: "HeaderManager",
      enabled: "true"
    },
  }
};

export default class HeaderManager extends Configuration {
  constructor(options = DEFAULT_OPTIONS) {
    super(options);
    this.type = "HeaderManager";
    this.headers = [];
  }
}

export const schema = {
  HeaderManager: HeaderManager
}
