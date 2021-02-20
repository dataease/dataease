import Element from "./element";

export class Prop extends Element {
  constructor(options = {}) {
    super(options);
    if (options.attributes) {
      this.key = options.attributes.name;
    }
  }
}

/**
 * int, long, bool, string
 */
export class BasicProp extends Prop {
  constructor(options = {}) {
    super(options);
    if (options.elements) {
      this.value = options.elements[0].text;
    }
  }

  toJson() {
    if (this.value !== undefined && this.value !== "") {
      let json = super.toJson();
      json.elements = [{
        "type": "text",
        "text": "" + this.value
      }]
      return json;
    }
  }
}

/**
 * value:数值
 */
export class IntProp extends BasicProp {
  name = "intProp";

  constructor(options = {}) {
    super(options);
    this.value = options.elements ? parseInt(options.elements[0].text) : undefined;
  }
}

/**
 * value:数值
 */
export class LongProp extends BasicProp {
  name = "longProp";

  constructor(options = {}) {
    super(options);
    this.value = options.elements ? parseInt(options.elements[0].text) : undefined;
  }
}

/**
 * value:布尔
 */
class BoolProp extends BasicProp {
  name = "boolProp";

  constructor(options = {}) {
    super(options);
    this.value = options.elements ? options.elements[0].text === 'true' : undefined;
  }
}

/**
 * value:字符串
 */
export class StringProp extends BasicProp {
  name = "stringProp";

  constructor(options = {}) {
    super(options);
    this.value = options.elements ? options.elements[0].text : undefined;
  }
}


/**
 * value: name/value 对象
 */
export class ObjProp extends Prop {
  constructor(options = {}) {
    super(options);
    if (options.elements) {
      this.value = {};
      options.elements.forEach(e => {
        if (e.name === "name") {
          this.key = e.elements[0].text;
          this.value.name = new BasicProp(e)
        } else {
          this.value.value = new ObjValue(e);
        }
      })
    }
  }

  toJson() {
    let json = super.toJson();
    if (this.value !== undefined) {
      json.elements = [];
      if (this.value.name) json.elements.push(this.value.name.toJson());
      if (this.value.value) json.elements.push(this.value.value.toJson());
    }
    return json;
  }
}

export class ObjValue extends Element {
  constructor(options = {}) {
    super(options);
    if (options.elements) {
      this.value = {}
      options.elements.forEach(e => {
        this.value[e.name] = new BasicProp(e);
      })
    }
  }

  toJson() {
    let json = super.toJson();
    if (this.value !== undefined) {
      json.elements = [];
      Object.keys(this.value).forEach(v => {
        json.elements.push(this.value[v].toJson());
      })
    }
    return json;
  }
}

/**
 * elements:数组
 */
export class CollectionProp extends Prop {
  constructor(options = {}) {
    super(options);
    this.elements = [];
    if (options.elements) {
      options.elements.forEach(e => {
        let prop;
        switch (e.name) {
          case "intProp":
            prop = new IntProp(e);
            break;
          case "longProp":
            prop = new LongProp(e);
            break;
          case "boolProp":
            prop = new BoolProp(e);
            break;
          case "stringProp":
            prop = new StringProp(e);
            break;
          case "elementProp":
            prop = new ElementProp(e);
            break;
        }
        this.add(prop);
      })
    }
  }

  add(prop) {
    if (prop instanceof Prop) {
      this.elements.push(prop);
    } else {
      console.error("prop is not Prop");
    }
  }

  clear() {
    this.elements = [];
  }

  forEach(func) {
    this.elements.forEach(func);
  }

  toJson() {
    let json = super.toJson();
    if (this.elements && this.elements.length > 0) {
      json.elements = [];
      this.elements.forEach(v => {
        let element = v.toJson();
        if (element !== undefined) {
          json.elements.push(element);
        }
      })
    }
    return json;
  }
}

/**
 * elements: Map
 */
export class ElementProp extends Prop {
  constructor(options = {}) {
    super(options);
    this.elements = {};
    if (options.elements) {
      this.elements = getProps(options.elements);
    }
  }

  initIntProp(name, defaultValue) {
    if (this.elements[name] === undefined) {
      this.elements[name] = intProp(name, defaultValue);
    }
    return this.elements[name];
  }

  initLongProp(name, defaultValue) {
    if (this.elements[name] === undefined) {
      this.elements[name] = longProp(name, defaultValue);
    }
    return this.elements[name];
  }

  initBoolProp(name, defaultValue) {
    if (this.elements[name] === undefined) {
      this.elements[name] = boolProp(name, defaultValue);
    }
    return this.elements[name];
  }

  initStringProp(name, defaultValue) {
    if (this.elements[name] === undefined) {
      this.elements[name] = stringProp(name, defaultValue);
    }
    return this.elements[name];
  }

  initCollectionProp(name) {
    if (this.elements[name] === undefined) {
      this.elements[name] = collectionProp(name);
    }
    return this.elements[name];
  }

  add(prop) {
    if (prop instanceof Prop) {
      this.elements[prop.key] = prop;
    } else {
      console.error("prop is not Prop");
    }
  }

  remove(key) {
    delete this.elements[key];
  }

  toJson() {
    let json = super.toJson();
    let keys = Object.keys(this.elements);
    if (keys.length > 0) {
      json.elements = [];
      keys.forEach(key => {
        let element = this.elements[key].toJson();
        if (element !== undefined) {
          json.elements.push(element);
        }
      });
    }
    return json;
  }
}

export const getProps = function (elements) {
  let props = {};
  if (elements) {
    elements.forEach(e => {
      let type = e.name;
      let name;
      if (e.attributes && e.attributes.name) {
        name = e.attributes.name;
      }
      switch (type) {
        case "intProp":
          props[name] = new IntProp(e);
          break;
        case "longProp":
          props[name] = new LongProp(e);
          break;
        case "boolProp":
          props[name] = new BoolProp(e);
          break;
        case "stringProp":
          props[name] = new StringProp(e);
          break;
        case "elementProp":
          props[name] = new ElementProp(e);
          break;
        case "collectionProp":
          props[name] = new CollectionProp(e);
          break;
        case "objProp":
          // const obj = new ObjProp(e);
          // props[obj.key] = obj;
          props[name] = new ObjProp(e);
          break;
      }
    });
  }
  return props;
}

export const basicProp = function (type, name, value) {
  let options = {
    type: "element",
    attributes: {
      name: name
    }
  }
  if (value !== undefined) {
    options.elements = [
      {
        type: "text",
        text: "" + value
      }
    ]
  }
  return new type(options);
}

export const intProp = function (name, value) {
  return basicProp(IntProp, name, value)
}

export const longProp = function (name, value) {
  return basicProp(LongProp, name, value)
}

export const boolProp = function (name, value) {
  return basicProp(BoolProp, name, value)
}

export const stringProp = function (name, value) {
  return basicProp(StringProp, name, value)
}

export const collectionProp = function (name) {
  let options = {
    "type": "element",
    "name": "collectionProp",
    "attributes": {
      "name": name
    },
    elements: []
  };
  return new CollectionProp(options);
}

export const elementProp = function (name, elementType, attributes) {
  let options = {
    "type": "element",
    "name": "elementProp",
    "attributes": {
      "name": name,
      "elementType": elementType,
      ...attributes
    },
    elements: []
  };
  return new ElementProp(options);
}


