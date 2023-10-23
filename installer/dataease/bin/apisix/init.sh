#1.create upstream core_api
curl http://127.0.0.1:9180/apisix/admin/upstreams/1 -X PUT -H "X-API-KEY: $DE_APISIX_KEY" -d '
{
  "nodes": [
    {
      "host": "dataease",
      "port": 8100,
      "weight": 1
    }
  ],
  "timeout": {
    "connect": 6,
    "send": 10,
    "read": 60
  },
  "type": "roundrobin",
  "scheme": "http",
  "pass_host": "pass",
  "name": "core_api",
  "desc": "dataease core service",
  "keepalive_pool": {
    "idle_timeout": 60,
    "requests": 1000,
    "size": 320
  }
}'

#2.create forward auth route
curl http://127.0.0.1:9180/apisix/admin/routes -X POST -H "X-API-KEY: $DE_APISIX_KEY" -d '
{  
  "uri": "/de2api/apisix/check",
  "name": "auth_check",
  "methods": [
    "GET",
    "POST",
    "PUT",
    "DELETE",
    "PATCH",
    "HEAD",
    "OPTIONS",
    "CONNECT",
    "TRACE",
    "PURGE"
  ],
  "upstream_id": "1",
  "status": 1
}'

#3.create auth_service
curl http://127.0.0.1:9180/apisix/admin/services/10 -X PUT -H "X-API-KEY: $DE_APISIX_KEY" -d '
{
  "name": "auth_service",
  "desc": "dataease auth service",
  "upstream_id": "1",
  "plugins": {
    "forward-auth": {
      "_meta": {
        "disable": false
      },
      "client_headers": [
        "Location",
        "DE-GATEWAY-FLAG",
        "DE-FORBIDDEN-FLAG"
      ],
      "request_headers": [
        "X-DE-TOKEN",
        "X-DE-LINK-TOKEN",
        "X-EMBEDDED-TOKEN",
        "Content-Type"
      ],
      "request_method": "POST",
      "upstream_headers": [
        "X-User-ID",
        "X-DE-REFRESH-TOKEN",
        "X-DE-TOKEN"
      ],
      "uri": "http://127.0.0.1:9080/de2api/apisix/check"
    }
  }
}'

#4.create static resource route
curl http://127.0.0.1:9180/apisix/admin/routes -X POST -H "X-API-KEY: $DE_APISIX_KEY" -d '
{
  "uris": [
    "/js/*",
    "/assets/*",
    "/vite.svg"
  ],
  "name": "global_static",
  "priority": 1,
  "methods": [
    "GET",
    "POST",
    "PUT",
    "DELETE",
    "PATCH",
    "HEAD",
    "OPTIONS",
    "CONNECT",
    "TRACE",
    "PURGE"
  ],
  "upstream_id": "1",
  "status": 1
}'

#5.create default index route
curl http://127.0.0.1:9180/apisix/admin/routes -X POST -H "X-API-KEY: $DE_APISIX_KEY" -d '
{
  "uri": "/*",
  "name": "default_index",
  "priority": 100,
  "methods": [
    "GET",
    "POST",
    "PUT",
    "DELETE",
    "PATCH",
    "HEAD",
    "OPTIONS",
    "CONNECT",
    "TRACE",
    "PURGE"
  ],
  "vars": [
    [
      "http_out_auth_platform",
      "!",
      "IN",
      [
        "ldap",
        "oidc",
        "cas"
      ]
    ]
  ],
  "upstream_id": "1",
  "status": 1
}'

#6.create default api route
curl http://127.0.0.1:9180/apisix/admin/routes -X POST -H "X-API-KEY: $DE_APISIX_KEY" -d '
{
  "uri": "/de2api/*",
  "name": "default_api",
  "priority": 6,
  "methods": [
    "GET",
    "POST",
    "PUT",
    "DELETE",
    "PATCH",
    "HEAD",
    "OPTIONS",
    "CONNECT",
    "TRACE",
    "PURGE"
  ],
  "vars": [
    [
      "http_out_auth_platform",
      "!",
      "IN",
      [
        "ldap",
        "oidc",
        "cas"
      ]
    ]
  ],
  "service_id": "10",
  "status": 1
}'