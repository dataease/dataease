#!/usr/bin/env bash
# CVE-2026-47890 短期防护：Spring Framework 6.2.x 禁止引入 SSE/流式响应。
# 在升级到 Spring Framework 7.0.9 / Spring Boot 4.x 前，本脚本用于防止
# SseEmitter、ResponseBodyEmitter、StreamingResponseBody 或 text/event-stream
# 进入代码库。

set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"

PATTERN='SseEmitter|ResponseBodyEmitter|StreamingResponseBody|ServerSentEvent|text/event-stream|TEXT_EVENT_STREAM_VALUE'

matches="$(
  grep -RInE "$PATTERN" \
    "$ROOT_DIR/core/core-backend/src/main/java" \
    "$ROOT_DIR"/sdk/*/src/main/java \
    2>/dev/null || true
)"

if [ -n "$matches" ]; then
  echo "Found forbidden SSE/streaming references:" >&2
  echo "$matches" >&2
  exit 1
fi

echo "SSE/streaming check passed."
