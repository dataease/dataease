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
local ngx        = ngx
local abs        = math.abs
local ngx_time   = ngx.time
local ngx_re     = require("ngx.re")
local ipairs     = ipairs
local hmac_sha1  = ngx.hmac_sha1
local core       = require("apisix.core")
local hmac       = require("resty.hmac")
local consumer   = require("apisix.consumer")
local ngx_decode_base64 = ngx.decode_base64
local ngx_encode_base64 = ngx.encode_base64
local plugin_name   = "hmac-auth"
local ALLOWED_ALGORITHMS = {"hmac-sha1", "hmac-sha256", "hmac-sha512"}
local resty_sha256 = require("resty.sha256")
local schema_def = require("apisix.schema_def")
local auth_utils = require("apisix.utils.auth")

local schema = {
    type = "object",
    title = "work with route or service object",
    properties = {
        allowed_algorithms = {
            type = "array",
            minItems = 1,
            items = {
                type = "string",
                enum = ALLOWED_ALGORITHMS
            },
            default = ALLOWED_ALGORITHMS,
        },
        clock_skew = {
            type = "integer",
            default = 300,
            minimum = 1
        },
        signed_headers = {
            type = "array",
            items = {
                type = "string",
                minLength = 1,
                maxLength = 50,
            }
        },
        validate_request_body = {
            type = "boolean",
            title = "A boolean value telling the plugin to enable body validation",
            default = false,
        },
        hide_credentials = {type = "boolean", default = false},
        anonymous_consumer = schema_def.anonymous_consumer_schema,
    },
}

local consumer_schema = {
    type = "object",
    title = "work with consumer object",
    properties = {
        key_id = {type = "string", minLength = 1, maxLength = 256},
        secret_key = {type = "string", minLength = 1, maxLength = 256},
    },
    encrypt_fields = {"secret_key"},
    required = {"key_id", "secret_key"},
}

local _M = {
    version = 0.1,
    priority = 2530,
    type = 'auth',
    name = plugin_name,
    schema = schema,
    consumer_schema = consumer_schema
}

local hmac_funcs = {
    ["hmac-sha1"] = function(secret_key, message)
        return hmac_sha1(secret_key, message)
    end,
    ["hmac-sha256"] = function(secret_key, message)
        return hmac:new(secret_key, hmac.ALGOS.SHA256):final(message)
    end,
    ["hmac-sha512"] = function(secret_key, message)
        return hmac:new(secret_key, hmac.ALGOS.SHA512):final(message)
    end,
}


local function array_to_map(arr)
    local map = core.table.new(0, #arr)
    for _, v in ipairs(arr) do
      map[v] = true
    end

    return map
end


function _M.check_schema(conf, schema_type)
    if schema_type == core.schema.TYPE_CONSUMER then
        return core.schema.check(consumer_schema, conf)
    else
        return core.schema.check(schema, conf)
    end
end



local function get_consumer(key_id)
    if not key_id then
        return nil, "missing key_id"
    end

    local cur_consumer, _, err = consumer.find_consumer(plugin_name, "key_id", key_id)
    if not cur_consumer then
        return nil, err or "Invalid key_id"
    end
    return cur_consumer
end


local function generate_signature(ctx, secret_key, params)
    local uri = ctx.var.request_uri
    local request_method = core.request.get_method()

    if uri == "" then
        uri = "/"
    end

    local signing_string_items = {
        params.keyId,
    }

    if params.headers then
        for _, h in ipairs(params.headers) do
            
            local canonical_header = core.request.header(ctx, h)
            --[[ core.log.warn("Processing header: " .. h .. ", canonical_header: " .. (canonical_header or "nil")) ]]
            if not canonical_header then
              if h == "@request-target" then
                local request_target = request_method .. " " .. uri
                core.table.insert(signing_string_items, request_target)
                core.log.info("canonical_header name:", core.json.delay_encode(h))
                core.log.info("canonical_header value: ",
                              core.json.delay_encode(request_target))
              end
            else
              core.table.insert(signing_string_items,
                                h .. ": " .. canonical_header)
              core.log.info("canonical_header name:", core.json.delay_encode(h))
              core.log.info("canonical_header value: ",
                            core.json.delay_encode(canonical_header))
            end
        end
    end

    local signing_string = core.table.concat(signing_string_items, "\n") .. "\n"
    --[[ core.log.warn("signing_string: ", signing_string) ]]
    return hmac_funcs[params.algorithm](secret_key, signing_string)
end


local function sha256(key)
    local hash = resty_sha256:new()
    hash:update(key)
    local digest = hash:final()
    return digest
end


local function validate(ctx, conf, params)
    if not params then
        return nil
    end

    if not params.keyId or not params.signature then
        return nil, "keyId or signature missing"
    end

    if not params.algorithm then
        return nil, "algorithm missing"
    end

    local consumer, err = get_consumer(params.keyId)
    if err then
        return nil, err
    end

    local consumer_conf = consumer.auth_conf
    local found_algorithm = false
    -- check supported algorithm used
    if not conf.allowed_algorithms then
        conf.allowed_algorithms = ALLOWED_ALGORITHMS
    end

    for _, algo in ipairs(conf.allowed_algorithms) do
      if algo == params.algorithm then
        found_algorithm = true
        break
      end
    end

    if not found_algorithm then
        return nil, "Invalid algorithm"
    end

    core.log.info("clock_skew: ", conf.clock_skew)
    if conf.clock_skew and conf.clock_skew > 0 then
        if not params.date then
            return nil, "Date header missing. failed to validate clock skew"
        end

        local time = ngx.parse_http_time(params.date)
        core.log.info("params.date: ", params.date, " time: ", time)
        if not time then
            return nil, "Invalid GMT format time"
        end

        local diff = abs(ngx_time() - time)

        if diff > conf.clock_skew then
            return nil, "Clock skew exceeded"
        end
    end

    -- validate headers
    -- All headers passed in route conf.signed_headers must be used in signing(params.headers)
    if conf.signed_headers and #conf.signed_headers >= 1 then
        if not params.headers then
            return nil, "headers missing"
        end
        local params_headers_map = array_to_map(params.headers)
        if params_headers_map then
            for _, header in ipairs(conf.signed_headers) do
                if not params_headers_map[header] then
                    return nil, [[expected header "]] .. header .. [[" missing in signing]]
                end
            end
        end
    end

    local secret_key          = consumer_conf and consumer_conf.secret_key
    local request_signature   = ngx_decode_base64(params.signature)
    local generated_signature = generate_signature(ctx, secret_key, params)
    if request_signature ~= generated_signature then
        return nil, "Invalid signature"
    end

    local validate_request_body = conf.validate_request_body
    if validate_request_body then
        local digest_header = params.body_digest
        if not digest_header then
            return nil, "Invalid digest"
        end

        local req_body, err = core.request.get_body()
        if err then
            return nil, err
        end

        req_body = req_body or ""
        local digest_created = "SHA-256" .. "=" ..
                ngx_encode_base64(sha256(req_body))
        if digest_created ~= digest_header then
            return nil, "Invalid digest"
        end
    end

    return consumer
end


local function retrieve_hmac_fields(ctx)
    local hmac_params = {}
    local auth_string = core.request.header(ctx, "Authorization")
    if not auth_string then
        return nil, "missing Authorization header"
    end

    if not core.string.has_prefix(auth_string, "Signature") then
        return nil, "Authorization header does not start with 'Signature'"
    end

    local signature_fields = auth_string:sub(10):gmatch('[^,]+')

    for field in signature_fields do
        local key, value = field:match('%s*(%w+)="(.-)"')
        if key and value then
            if key == "keyId" or key == "algorithm" or key == "signature" then
                hmac_params[key] = value

            elseif key == "headers" then
                hmac_params.headers = ngx_re.split(value, " ")
            end
        end
    end

    -- will be required to check clock skew
    if core.request.header(ctx, "Date") then
        hmac_params.date = core.request.header(ctx, "Date")
    elseif core.request.header(ctx, "X-Date") then
        hmac_params.date = core.request.header(ctx, "X-Date")
    end

    if core.request.header(ctx, "Digest") then
        hmac_params.body_digest = core.request.header(ctx, "Digest")
    end

    return hmac_params
end

local function find_consumer(conf, ctx)
    local params,err = retrieve_hmac_fields(ctx)
    if err then
        if not auth_utils.is_running_under_multi_auth(ctx) then
            core.log.warn("client request can't be validated: ", err)
        end
        return nil, nil, "client request can't be validated: " .. err
    end

    local validated_consumer, err = validate(ctx, conf, params)
    if not validated_consumer then
        core.log.warn(err)
        err = "client request can't be validated: " .. (err or "Invalid signature")
        if auth_utils.is_running_under_multi_auth(ctx) then
            return nil, nil, err
        end
        core.log.warn(err)
        return nil, nil, "client request can't be validated"
    end

    local consumers_conf = consumer.consumers_conf(plugin_name)
    return validated_consumer, consumers_conf, err
end


function _M.rewrite(conf, ctx)
    local cur_consumer, consumers_conf, err = find_consumer(conf, ctx)
    if not cur_consumer then
        if not conf.anonymous_consumer then
            return 401, { message = err }
        end
        cur_consumer, consumers_conf, err = consumer.get_anonymous_consumer(conf.anonymous_consumer)
        if not cur_consumer then
            if auth_utils.is_running_under_multi_auth(ctx) then
                return 401, err
            end
            core.log.error(err)
            return 401, { message = "Invalid user authorization" }
        end
    end

    if conf.hide_credentials then
        core.request.set_header("Authorization", nil)
    end

    consumer.attach_consumer(ctx, cur_consumer, consumers_conf)
end


return _M
