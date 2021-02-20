import Sampler from "../sampler";
import {Body} from "../../../../../model/ApiTestModel";

const DEFAULT_OPTIONS = {
  options: {
    attributes: {
      guiclass: "HttpTestSampleGui",
      testclass: "HTTPSamplerProxy",
      testname: "HTTPSamplerProxy",
      enabled: "true"
    },
  }
};
export default class HTTPSamplerProxy extends Sampler {
  constructor(options = DEFAULT_OPTIONS) {
    super(options);
    this.type = "HTTPSamplerProxy";
    this.protocol = "HTTP";
    this.domain = undefined;
    this.port = undefined;
    this.method = undefined;
    this.path = undefined;
    this.contentEncoding = undefined;

    this.autoRedirects = false;
    this.followRedirects = true;
    this.useKeepalive = true;
    this.postBodyRaw = undefined;
    this.doMultipartPost = false;
    this.browserCompatibleMultipart = undefined;
    this.embeddedUrlRe = undefined;
    this.connectTimeout = 6000;
    this.responseTimeout = 6000;
    // 初始化主体对象
    this.body = new Body();
    this.arguments = [];
    this.rest = [];
    this.files = [];
    this.headers = [];
    this.hashTree = [];
  }
}

export const schema = {
  HTTPSamplerProxy: HTTPSamplerProxy
}

