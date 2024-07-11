--
-- Licensed to the Apache Software Foundation (ASF) under one or more
-- contributor license agreements.  See the NOTICE file distributed with
-- this work for additional information regarding copyright ownership.
-- The ASF licenses this file to You under the Apache License, Version 2.0
-- (the "License"); you may not use this file except in compliance with
-- the License.  You may obtain a copy of the License at
--
--     http://www.apache.org/licenses/LICENSE-2.0
--
-- Unless required by applicable law or agreed to in writing, software
-- distributed under the License is distributed on an "AS IS" BASIS,
-- WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
-- See the License for the specific language governing permissions and
-- limitations under the License.
--

local core    = require("apisix.core")
local ngx_re  = require("ngx.re")
local openidc = require("resty.openidc")
local random  = require("resty.random")
local string  = string
local ngx     = ngx

local ngx_encode_base64 = ngx.encode_base64

local plugin_name = "openid-connect"


local schema = {
    type = "object",
    properties = {
        client_id = {type = "string"},
        client_secret = {type = "string"},
        discovery = {type = "string"},
        scope = {
            type = "string",
            default = "openid",
        },
        ssl_verify = {
            type = "boolean",
            default = false,
        },
        timeout = {
            type = "integer",
            minimum = 1,
            default = 3,
            description = "timeout in seconds",
        },
        introspection_endpoint = {
            type = "string"
        },
        introspection_endpoint_auth_method = {
            type = "string",
            default = "client_secret_basic"
        },
        bearer_only = {
            type = "boolean",
            default = false,
        },
        session = {
            type = "object",
            properties = {
                secret = {
                    type = "string",
                    description = "the key used for the encrypt and HMAC calculation",
                    minLength = 16,
                },
            },
            required = {"secret"},
            additionalProperties = false,
        },
        realm = {
            type = "string",
            default = "",
        },
        logout_path = {
            type = "string",
            default = "/logout",
        },
        redirect_uri = {
            type = "string",
            description = "use ngx.var.request_uri if not configured"
        },
        post_logout_redirect_uri = {
            type = "string",
            description = "the URI will be redirect when request logout_path",
        },
        unauth_action = {
            type = "string",
            default = "auth",
            enum = {"auth", "deny", "pass"},
            description = "The action performed when client is not authorized. Use auth to " ..
                "redirect user to identity provider, deny to respond with 401 Unauthorized, and " ..
                "pass to allow the request regardless."
        },
        public_key = {type = "string"},
        token_signing_alg_values_expected = {type = "string"},
        use_pkce = {
            description = "when set to true the PKEC(Proof Key for Code Exchange) will be used.",
            type = "boolean",
            default = false
        },
        set_access_token_header = {
            description = "Whether the access token should be added as a header to the request " ..
                "for downstream",
            type = "boolean",
            default = true
        },
        access_token_in_authorization_header = {
            description = "Whether the access token should be added in the Authorization " ..
                "header as opposed to the X-Access-Token header.",
            type = "boolean",
            default = false
        },
        set_id_token_header = {
            description = "Whether the ID token should be added in the X-ID-Token header to " ..
                "the request for downstream.",
            type = "boolean",
            default = true
        },
        set_userinfo_header = {
            description = "Whether the user info token should be added in the X-Userinfo " ..
                "header to the request for downstream.",
            type = "boolean",
            default = true
        },
        set_refresh_token_header = {
            description = "Whether the refresh token should be added in the X-Refresh-Token " ..
                "header to the request for downstream.",
            type = "boolean",
            default = false
        },
        proxy_opts = {
            description = "HTTP proxy server be used to access identity server.",
            type = "object",
            properties = {
                http_proxy = {
                    type = "string",
                    description = "HTTP proxy like: http://proxy-server:80.",
                },
                https_proxy = {
                    type = "string",
                    description = "HTTPS proxy like: http://proxy-server:80.",
                },
                http_proxy_authorization = {
                    type = "string",
                    description = "Basic [base64 username:password].",
                },
                https_proxy_authorization = {
                    type = "string",
                    description = "Basic [base64 username:password].",
                },
                no_proxy = {
                    type = "string",
                    description = "Comma separated list of hosts that should not be proxied.",
                }
            },
        }
    },
    encrypt_fields = {"client_secret"},
    required = {"client_id", "client_secret", "discovery"}
}


local _M = {
    version = 0.2,
    priority = 2599,
    name = plugin_name,
    schema = schema,
}


function _M.check_schema(conf)
    if conf.ssl_verify == "no" then
        -- we used to set 'ssl_verify' to "no"
        conf.ssl_verify = false
    end

    if not conf.bearer_only and not conf.session then
        core.log.warn("when bearer_only = false, " ..
                       "you'd better complete the session configuration manually")
        conf.session = {
            -- generate a secret when bearer_only = false and no secret is configured
            secret = ngx_encode_base64(random.bytes(32, true) or random.bytes(32))
        }
    end

    local ok, err = core.schema.check(schema, conf)
    if not ok then
        return false, err
    end

    return true
end


local function get_bearer_access_token(ctx)
    -- Get Authorization header, maybe.
    local auth_header = core.request.header(ctx, "Authorization")
    if not auth_header then
        -- No Authorization header, get X-Access-Token header, maybe.
        local access_token_header = core.request.header(ctx, "X-Access-Token")
        if not access_token_header then
            -- No X-Access-Token header neither.
            return false, nil, nil
        end

        -- Return extracted header value.
        return true, access_token_header, nil
    end

    -- Check format of Authorization header.
    local res, err = ngx_re.split(auth_header, " ", nil, nil, 2)

    if not res then
        -- No result was returned.
        return false, nil, err
    elseif #res < 2 then
        -- Header doesn't split into enough tokens.
        return false, nil, "Invalid Authorization header format."
    end

    if string.lower(res[1]) == "bearer" then
        -- Return extracted token.
        return true, res[2], nil
    end

    return false, nil, nil
end


local function introspect(ctx, conf)
    -- Extract token, maybe.
    local has_token, token, err = get_bearer_access_token(ctx)

    if err then
        return ngx.HTTP_BAD_REQUEST, err, nil, nil
    end

    if not has_token then
        -- Could not find token.

        if conf.bearer_only then
            -- Token strictly required in request.
            ngx.header["WWW-Authenticate"] = 'Bearer realm="' .. conf.realm .. '"'
            return ngx.HTTP_UNAUTHORIZED, "No bearer token found in request.", nil, nil
        else
            -- Return empty result.
            return nil, nil, nil, nil
        end
    end

    -- If we get here, token was found in request.

    if conf.public_key or conf.use_jwks then
        -- Validate token against public key or jwks document of the oidc provider.
        -- TODO: In the called method, the openidc module will try to extract
        --  the token by itself again -- from a request header or session cookie.
        --  It is inefficient that we also need to extract it (just from headers)
        --  so we can add it in the configured header. Find a way to use openidc
        --  module's internal methods to extract the token.
        local res, err = openidc.bearer_jwt_verify(conf)

        if err then
            -- Error while validating or token invalid.
            ngx.header["WWW-Authenticate"] = 'Bearer realm="' .. conf.realm ..
                '", error="invalid_token", error_description="' .. err .. '"'
            return ngx.HTTP_UNAUTHORIZED, err, nil, nil
        end

        -- Token successfully validated.
        local method = (conf.public_key and "public_key") or (conf.use_jwks and "jwks")
        core.log.debug("token validate successfully by ", method)
        return res, err, token, res
    else
        -- Validate token against introspection endpoint.
        -- TODO: Same as above for public key validation.
        local res, err = openidc.introspect(conf)

        if err then
            ngx.header["WWW-Authenticate"] = 'Bearer realm="' .. conf.realm ..
                '", error="invalid_token", error_description="' .. err .. '"'
            return ngx.HTTP_UNAUTHORIZED, err, nil, nil
        end

        -- Token successfully validated and response from the introspection
        -- endpoint contains the userinfo.
        core.log.debug("token validate successfully by introspection")
        return res, err, token, res
    end
end


local function add_access_token_header(ctx, conf, token)
    if token then
        -- Add Authorization or X-Access-Token header, respectively, if not already set.
        if conf.set_access_token_header then
            if conf.access_token_in_authorization_header then
                if not core.request.header(ctx, "Authorization") then
                    -- Add Authorization header.
                    core.request.set_header(ctx, "Authorization", "Bearer " .. token)
                end
            else
                if not core.request.header(ctx, "X-Access-Token") then
                    -- Add X-Access-Token header.
                    core.request.set_header(ctx, "X-Access-Token", token)
                end
            end
        end
    end
end


function _M.rewrite(plugin_conf, ctx)
    local conf = core.table.clone(plugin_conf)

    -- Previously, we multiply conf.timeout before storing it in etcd.
    -- If the timeout is too large, we should not multiply it again.
    if not (conf.timeout >= 1000 and conf.timeout % 1000 == 0) then
        conf.timeout = conf.timeout * 1000
    end

    if not conf.redirect_uri then
        conf.redirect_uri = ctx.var.request_uri
    end

    if not conf.ssl_verify then
        -- openidc use "no" to disable ssl verification
        conf.ssl_verify = "no"
    end

    local response, err, session, _

    if conf.bearer_only or conf.introspection_endpoint or conf.public_key then
        -- An introspection endpoint or a public key has been configured. Try to
        -- validate the access token from the request, if it is present in a
        -- request header. Otherwise, return a nil response. See below for
        -- handling of the case where the access token is stored in a session cookie.
        local access_token, userinfo
        response, err, access_token, userinfo = introspect(ctx, conf)

        if err then
            -- Error while validating token or invalid token.
            core.log.error("OIDC introspection failed: ", err)
            return response
        end

        if response then
            -- Add configured access token header, maybe.
            add_access_token_header(ctx, conf, access_token)

            if userinfo and conf.set_userinfo_header then
                -- Set X-Userinfo header to introspection endpoint response.
                core.request.set_header(ctx, "X-Userinfo",
                    ngx_encode_base64(core.json.encode(userinfo)))
            end
        end
    end

    if not response then
        -- Either token validation via introspection endpoint or public key is
        -- not configured, and/or token could not be extracted from the request.

        local unauth_action = conf.unauth_action
        if unauth_action ~= "auth" then
            unauth_action = "deny"
        end

        -- Authenticate the request. This will validate the access token if it
        -- is stored in a session cookie, and also renew the token if required.
        -- If no token can be extracted, the response will redirect to the ID
        -- provider's authorization endpoint to initiate the Relying Party flow.
        -- This code path also handles when the ID provider then redirects to
        -- the configured redirect URI after successful authentication.
        response, err, _, session  = openidc.authenticate(conf, nil, unauth_action, conf.session)

        if err then
            if err == "unauthorized request" then
                if conf.unauth_action == "pass" then
                    return nil
                end
                return 401
            end
            core.log.error("OIDC authentication failed: ", err)
            return 500
        end

        if response then
            -- If the openidc module has returned a response, it may contain,
            -- respectively, the access token, the ID token, the refresh token,
            -- and the userinfo.
            -- Add respective headers to the request, if so configured.

            -- Add configured access token header, maybe.
            add_access_token_header(ctx, conf, response.access_token)

            -- Add X-ID-Token header, maybe.
            if response.id_token and conf.set_id_token_header then
                local token = core.json.encode(response.id_token)
                core.request.set_header(ctx, "X-ID-Token", ngx.encode_base64(token))
            end

            -- Add X-Userinfo header, maybe.
            if response.user and conf.set_userinfo_header then
                core.request.set_header(ctx, "X-Userinfo",
                    ngx_encode_base64(core.json.encode(response.user)))
            end

            -- Add X-Refresh-Token header, maybe.
            if session.data.refresh_token and conf.set_refresh_token_header then
                core.request.set_header(ctx, "X-Refresh-Token", session.data.refresh_token)
            end
        end
    end
end


return _M
