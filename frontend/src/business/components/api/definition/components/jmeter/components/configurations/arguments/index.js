import Configuration from "../configuration";

const DEFAULT_OPTIONS = {
  options: {
    attributes: {
      guiclass: "ArgumentsPanel",
      testclass: "Arguments",
      testname: "Arguments",
      enabled: "true"
    },
  }
};

export default class Arguments extends Configuration {
  constructor(options = DEFAULT_OPTIONS) {
    super(options);

    this.arguments = [];
  }
}

export const schema = {
  Arguments: Arguments
}
