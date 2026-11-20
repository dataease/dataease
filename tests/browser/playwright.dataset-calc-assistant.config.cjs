const port = process.env.DATASET_CALC_ASSISTANT_PORT || "14278";
const url = "http://127.0.0.1:" + port;
const sql = require("./playwright.dataset-sql-assistant.config.cjs");
module.exports = {
  ...sql,
  use: { ...sql.use, baseURL: url },
  webServer: {
    ...sql.webServer,
    url,
    env: { ...sql.webServer.env, DATASET_CALC_ASSISTANT_FIXTURE: "1" },
  },
  testMatch: "dataset-calc-assistant*.spec.cjs",
  outputDir: "./output/dataset-calc-assistant",
};
