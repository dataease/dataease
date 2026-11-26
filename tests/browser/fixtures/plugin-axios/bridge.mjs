export const useEmbedded = () => window.fixture.embedded;
export const useLinkStoreWithOut = () => ({
  getLinkToken: window.fixture.link,
});
export const useCache = () => ({
  wsCache: { get: (key) => window.fixture.cache[key] },
});
export const configHandler = async (config) => {
  window.fixture.contextCalls++;
  if (window.fixture.waitContext)
    await new Promise((resolve) => {
      window.releaseContext = resolve;
    });
  if (window.fixture.token)
    config.headers.set("X-DE-TOKEN", window.fixture.token);
  return config;
};
export const useI18n = () => ({ t: (key) => "translated:" + key });
export const getLocale = () => "zh-CN";
export const ElMessage = {
  error: (message) => window.fixture.messages.push(message),
};
