import HashTreeElement from "../../hashtree";

const DEFAULT_OPTIONS = {
  options: {
    attributes: {
      guiclass: "AuthPanel",
      testclass: "AuthManager",
      testname: "AuthManager",
      enabled: "true"
    },
  }
};

export default class AuthManager extends HashTreeElement {
  constructor(options = DEFAULT_OPTIONS) {
    super(options);
    this.type = "AuthManager";
    this.username = undefined;
    this.password = undefined;
    this.url = undefined;
    this.realm = undefined;
    this.verification = "No Auth";
    this.mechanism = "BASIC_DIGEST";
    this.encrypt = false;
    this.environment = undefined;
  }
}
export const schema = {
  AuthManager: AuthManager
}
