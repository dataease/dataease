import {uuid} from "@/common/js/utils";

/**
 * options: JXM转换的JSON对象(xml2json)
 */
export default class Element {
  constructor(options = {}) {
    this.id = uuid();
    this.type = options.type || "element";
    this.name = options.name

    if (options.attributes) {
      this.attributes = options.attributes;
      if (options.attributes.testname) {
        this.label = options.attributes.testname;
      }
      if (options.attributes.testclass && this.name === undefined) {
        this.name = options.attributes.testclass;
      }
      if (options.attributes.enabled) {
        this.enabled = options.attributes.enabled === "true";
      }
    }
  }

  toJson() {
    let json = {};
    if (this.type) {
      json.type = this.type;
    }
    if (this.name) {
      json.name = this.name;
    }
    if (this.attributes) {
      json.attributes = this.attributes;
      if (this.label !== undefined) {
        json.attributes.testname = this.label;
      }
      if (this.enabled !== undefined) {
        json.attributes.enabled = this.enabled + "";
      }
    }
    return json;
  }
}





