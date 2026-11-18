import { h } from "vue";

// Pure UI boundaries for auxiliary icons and SQL metadata. SQL preview goes through
// a request so each test can assert who started it and which draft was sent.
export const Icon = {
  setup:
    (_, { slots }) =>
    () =>
      h("span", slots.default?.()),
};
export const EmptyBackground = Icon;
export const iconFieldMap = {};
export const isDesktop = () => false;
export const searchVariableApi = async () => ({
  data: window.fixtureSystemVariables || [],
});
export async function getPreviewSql(data) {
  const response = await fetch("/fixture-api/dataset/previewSql", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(data),
  });
  return response.json();
}
