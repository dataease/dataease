import Sampler from "../sampler";

const DEFAULT_OPTIONS = {
  options: {
    attributes: {
      guiclass: "TCPSamplerGui",
      testclass: "TCPSampler",
      testname: "TCPSampler",
      enabled: "true"
    },
  }
};

export default class TCPSampler extends Sampler {
  static CLASSES = ["TCPClientImpl", "BinaryTCPClientImpl", "LengthPrefixedBinaryTCPClientImpl"]

  constructor(options = DEFAULT_OPTIONS) {
    super(options);
    this.type = "TCPSampler";
    this.classname = options.classname || TCPSampler.CLASSES[0];
    this.server = options.server;
    this.port = options.port;
    this.ctimeout = options.ctimeout; // Connect
    this.timeout = options.timeout; // Response

    this.reUseConnection = options.reUseConnection === undefined ? true : options.reUseConnection;
    this.nodelay = options.nodelay === undefined ? false : options.nodelay;
    this.closeConnection = options.closeConnection === undefined ? false : options.closeConnection;
    this.soLinger = options.soLinger;
    this.eolByte = options.eolByte;

    this.username = options.username;
    this.password = options.password;

    this.parameters = [];
    this.hashTree = [];
  }
}

export const schema = {
  TCPSampler: TCPSampler
}
