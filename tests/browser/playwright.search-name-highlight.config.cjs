module.exports = {
  testDir: "./specs",
  testMatch: "search-name-highlight.spec.cjs",
  workers: 1,
  retries: 0,
  timeout: 30000,
  reporter: "dot",
  outputDir: "./output/search-name-highlight-results",
  use: { channel: "chrome", trace: "retain-on-failure" },
};
