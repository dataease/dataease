import HashTreeElement from "../../hashtree";

export const TYPE = "Configuration";

export default class Configuration extends HashTreeElement {
  constructor(options = {}) {
    super(options);
    this.$type = TYPE;
  }
}
