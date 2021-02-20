import HashTreeElement from "../../hashtree";

export const TYPE = "PreProcessor";

export default class PreProcessor extends HashTreeElement {
  constructor(options = {}) {
    super(options);
    this.$type = TYPE;
  }
}
