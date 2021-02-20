export const ELEMENTS = new Map([
  ['ALL', ["scenario", "HTTPSamplerProxy", "DubboSampler", "JDBCSampler", "TCPSampler", "OT_IMPORT", "IfController", "LoopController", "ConstantTimer", "JSR223Processor", "CustomizeReq"]],
  ['scenario', ["HTTPSamplerProxy", "DubboSampler", "JDBCSampler", "TCPSampler", "CASE", "OT_IMPORT", "IfController", "ConstantTimer", "JSR223Processor", "CustomizeReq"]],
  ['HTTPSamplerProxy', ["ConstantTimer", "JSR223PreProcessor", "JSR223PostProcessor", "Assertions", "Extract"]],
  ['DubboSampler', ["ConstantTimer", "JSR223PreProcessor", "JSR223PostProcessor", "Assertions", "Extract"]],
  ['JDBCSampler', ["ConstantTimer", "JSR223PreProcessor", "JSR223PostProcessor", "Assertions", "Extract"]],
  ['TCPSampler', ["ConstantTimer", "JSR223PreProcessor", "JSR223PostProcessor", "Assertions", "Extract"]],
  ['OT_IMPORT', ["ConstantTimer", "JSR223PreProcessor", "JSR223PostProcessor", "Assertions", "Extract"]],
  ['IfController', ["IfController", "HTTPSamplerProxy", "DubboSampler", "JDBCSampler", "TCPSampler", "OT_IMPORT", "ConstantTimer", "JSR223Processor", "JSR223PreProcessor", "JSR223PostProcessor", "Assertions", "Extract", "CustomizeReq"]],
  ['LoopController', ["IfController", "HTTPSamplerProxy", "DubboSampler", "JDBCSampler", "TCPSampler", "OT_IMPORT", "ConstantTimer", "JSR223Processor", "JSR223PreProcessor", "JSR223PostProcessor", "Assertions", "Extract", "CustomizeReq"]],
  ['ConstantTimer', []],
  ['JSR223Processor', ["ConstantTimer", "JSR223PreProcessor", "JSR223PostProcessor", "Assertions", "Extract"]],
  ['JSR223PreProcessor', []],
  ['JSR223PostProcessor', []],
  ['Assertions', []],
  ['Extract', []],
  ['CustomizeReq', ["ConstantTimer", "JSR223PreProcessor", "JSR223PostProcessor", "Assertions", "Extract"]],
])

export const ELEMENT_TYPE = {
  scenario: "scenario",
  HTTPSamplerProxy: "HTTPSamplerProxy",
  OT_IMPORT: "OT_IMPORT",
  IfController: "IfController",
  ConstantTimer: "ConstantTimer",
  JSR223Processor: "JSR223Processor",
  JSR223PreProcessor: "JSR223PreProcessor",
  JSR223PostProcessor: "JSR223PostProcessor",
  Assertions: "Assertions",
  Extract: "Extract",
  CustomizeReq: "CustomizeReq",
  LoopController: "LoopController"
}

