const editor = require("./playwright.dataset-editor.config.cjs");
const url =
  "http://127.0.0.1:" + (process.env.DATASET_SQL_ASSISTANT_PORT || 14277);
module.exports = {
  ...editor,
  testMatch: "dataset-sql-assistant*.spec.cjs",
  outputDir: "./output/dataset-sql-assistant",
  use: { ...editor.use, baseURL: url },
  webServer: {
    ...editor.webServer,
    url,
    env: { DATASET_SQL_ASSISTANT_FIXTURE: "1" },
  },
};
