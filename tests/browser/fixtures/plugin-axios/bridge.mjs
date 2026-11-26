export const useEmbedded = () => window.fixture.embedded;
export const useLinkStoreWithOut = () => ({
  getLinkToken: window.fixture.link,
});
export const configHandler = async (config) => {
  if (window.fixture.waitContext)
    await new Promise((resolve) => {
      window.releaseContext = resolve;
    });
  if (window.fixture.token)
    config.headers.set("X-DE-TOKEN", window.fixture.token);
  return config;
};
// Synthetic public contract only; no private signer implementation or credentials.
export const securityConfig = async (config, uri) => {
  window.fixture.signatures.push(uri);
  if (window.fixture.enabled) {
    config.headers.set("Authorization", "Signature synthetic-test");
    config.headers.set("X-Date", "synthetic-date");
  }
};
