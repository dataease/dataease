import Sampler from "../sampler";
import {ConfigCenter, ConsumerAndService, RegistryCenter} from "../../../../../model/ApiTestModel";

const DEFAULT_OPTIONS = {
  options: {
    attributes: {
      guiclass: "DubboSampleGui",
      testclass: "DubboSampler",
      testname: "DubboSampler",
      enabled: "true"
    },
  }
};
export default class DubboSampler extends Sampler {
  static PROTOCOLS = {
    DUBBO: "dubbo://",
    RMI: "rmi://",
  }
  constructor(options = DEFAULT_OPTIONS) {
    super(options);
    this.type = "DubboSampler";
    this.hashTree = [];
    this.protocol = options.protocol || DubboSampler.PROTOCOLS.DUBBO;
    this.interface = options.interface;
    this.method = options.method;
    this.configCenter = new ConfigCenter(options.configCenter);
    this.registryCenter = new RegistryCenter(options.registryCenter);
    this.consumerAndService = new ConsumerAndService(options.consumerAndService);
    this.args = [];
    this.attachmentArgs = [];
    this.dubboConfig = undefined;
    this.debugReport = undefined;
  }
}

export const schema = {
  DubboSampler: DubboSampler
}
