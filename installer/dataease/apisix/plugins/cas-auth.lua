--
---- Licensed to the Apache Software Foundation (ASF) under one or more
---- contributor license agreements.  See the NOTICE file distributed with
---- this work for additional information regarding copyright ownership.
---- The ASF licenses this file to You under the Apache License, Version 2.0
---- (the "License"); you may not use this file except in compliance with
---- the License.  You may obtain a copy of the License at
----
----     http://www.apache.org/licenses/LICENSE-2.0
----
---- Unless required by applicable law or agreed to in writing, software
---- distributed under the License is distributed on an "AS IS" BASIS,
---- WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
---- See the License for the specific language governing permissions and
---- limitations under the License.
----
local core = require("apisix.core")
local http = require("resty.http")
local ngx = ngx
local ngx_re_match = ngx.re.match
local CAS_REQUEST_URI = "CAS_REQUEST_URI"
local COOKIE_NAME = "CAS_SESSION"
local COOKIE_PARAMS = "; Path=/; HttpOnly"
local SESSION_LIFETIME = 3600
local STORE_NAME = "cas_sessions"

local store = ngx.shared[STORE_NAME]


local plugin_name = "cas-auth"
local schema = {
    type = "object",
    properties = {
        idp_uri = {type = "string"},
        cas_callback_uri = {type = "string"},
        logout_uri = {type = "string"},
        cas_callback_domain={type="string"}
    },
    required = {
        "idp_uri", "cas_callback_uri", "logout_uri"
    }
}

local _M = {
    version = 0.1,
    priority = 2597,
    name = plugin_name,
    schema = schema
}

function _M.check_schema(conf)
    return core.schema.check(schema, conf)
end

local function uri_without_ticket(conf, ctx)
    if conf.cas_callback_domain == nil then
        return ctx.var.scheme .. "://" .. ctx.var.host .. ":" ..
            ctx.var.server_port .. conf.cas_callback_uri
    else
        return conf.cas_callback_domain .. conf.cas_callback_uri
    end
end

local function get_session_id(ctx)
    return ctx.var["cookie_" .. COOKIE_NAME]
end

local function set_our_cookie(name, val)
    core.response.add_header("Set-Cookie", name .. "=" .. val .. COOKIE_PARAMS)
end

local function first_access(conf, ctx)
    local login_uri = conf.idp_uri .. "/login?" ..
        ngx.encode_args({ service = uri_without_ticket(conf, ctx) })
    core.log.info("first access: ", login_uri,
        ", cookie: ", ctx.var.http_cookie, ", request_uri: ", ctx.var.request_uri)
    set_our_cookie(CAS_REQUEST_URI, ctx.var.request_uri)
    core.response.set_header("Location", login_uri)
    return ngx.HTTP_MOVED_TEMPORARILY
end

local function with_session_id(conf, ctx, session_id)
    -- does the cookie exist in our store?
    local user = store:get(session_id);
    core.log.info("ticket=", session_id, ", user=", user)
    if user == nil then
        set_our_cookie(COOKIE_NAME, "deleted; Max-Age=0")
        return first_access(conf, ctx)
    else
        -- refresh the TTL
        store:set(session_id, user, SESSION_LIFETIME)
	core.request.set_header("X-CAS-USER", user)
    end
end

local function set_store_and_cookie(session_id, user)
    -- place cookie into cookie store
    local success, err, forcible = store:add(session_id, user, SESSION_LIFETIME)
    if success then
        if forcible then
            core.log.info("CAS cookie store is out of memory")
        end
        set_our_cookie(COOKIE_NAME, session_id)
    else
        if err == "no memory" then
            core.log.emerg("CAS cookie store is out of memory")
        elseif err == "exists" then
            core.log.error("Same CAS ticket validated twice, this should never happen!")
        else
            core.log.error("CAS cookie store: ", err)
        end
    end
    return success
end

local function validate(conf, ctx, ticket)
    -- send a request to CAS to validate the ticket
    local httpc = http.new()
    local res, err = httpc:request_uri(conf.idp_uri ..
        "/serviceValidate",
        { query = { ticket = ticket, service = uri_without_ticket(conf, ctx) } ,ssl_verify = false})

    if res and res.status == ngx.HTTP_OK and res.body ~= nil then
        if core.string.find(res.body, "<cas:authenticationSuccess>") then
            local m = ngx_re_match(res.body, "<cas:user>(.*?)</cas:user>", "jo");
            if m then
                return m[1]
            end
        else
            core.log.info("CAS serviceValidate failed: ", res.body)
        end
    else
        core.log.error("validate ticket failed: status=", (res and res.status),
            ", has_body=", (res and res.body ~= nil or false), ", err=", err)
    end
    return nil
end

local function validate_with_cas(conf, ctx, ticket)
    local user = validate(conf, ctx, ticket)
    if user and set_store_and_cookie(ticket, user) then
        local request_uri = ctx.var["cookie_" .. CAS_REQUEST_URI]
        set_our_cookie(CAS_REQUEST_URI, "deleted; Max-Age=0")
        core.log.info("ticket: ", ticket,
            ", cookie: ", ctx.var.http_cookie, ", request_uri: ", request_uri, ", user=", user)
        core.response.set_header("Location", request_uri)
        return ngx.HTTP_MOVED_TEMPORARILY
    else
        return ngx.HTTP_UNAUTHORIZED, {message = "invalid ticket"}
    end
end

local function logout(conf, ctx)
    local session_id = get_session_id(ctx)
    if session_id == nil then
        return ngx.HTTP_UNAUTHORIZED
    end

    core.log.info("logout: ticket=", session_id, ", cookie=", ctx.var.http_cookie)
    store:delete(session_id)
    set_our_cookie(COOKIE_NAME, "deleted; Max-Age=0")

    core.response.set_header("Location", conf.idp_uri .. "/logout")
    return ngx.HTTP_MOVED_TEMPORARILY
end

function _M.access(conf, ctx)
    local method = core.request.get_method()
    local uri = ctx.var.uri

    if method == "GET" and uri == conf.logout_uri then
        return logout(conf, ctx)
    end

    if method == "POST" and uri == conf.cas_callback_uri then
        local data = core.request.get_body()
        local ticket = data:match("<samlp:SessionIndex>(.*)</samlp:SessionIndex>")
        if ticket == nil then
            return ngx.HTTP_BAD_REQUEST,
                {message = "invalid logout request from IdP, no ticket"}
        end
        core.log.info("Back-channel logout (SLO) from IdP: LogoutRequest: ", data)
        local session_id = ticket
        local user = store:get(session_id);
        if user then
            store:delete(session_id)
            core.log.info("SLO: user=", user, ", tocket=", ticket)
        end
    else
        local session_id = get_session_id(ctx)
        if session_id ~= nil then
            return with_session_id(conf, ctx, session_id)
        end

        local ticket = ctx.var.arg_ticket
        if ticket ~= nil and uri == conf.cas_callback_uri then
            return validate_with_cas(conf, ctx, ticket)
        else
            return first_access(conf, ctx)
        end
    end
end

return _M
