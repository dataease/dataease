-- A YAML plugins list replaces the image defaults. Keep those defaults intact.
local plugins = require("apisix.cli.config").plugins

for _, name in ipairs(plugins) do
    if name == "log-rotate" then
        return
    end
end

plugins[#plugins + 1] = "log-rotate"
