#!/usr/bin/env bash
# 统一 PR 创建入口：直接走 GitHub REST API，避开 gh pr create 的 GraphQL 接口。
# 用法：scripts/create-pr.sh --title "fix(scope): ..." --body-file /tmp/pr.md \
#         [--base dev-v3] [--repo owner/name] [--no-push] [--draft] [--dry-run]

set -euo pipefail

base="dev-v3"
repo="dataease/dataease"
title=""
body_file=""
push_branch=1
draft=0
dry_run=0

usage() {
  cat <<'USAGE'
用法: scripts/create-pr.sh --title <标题> --body-file <文件> [选项]

选项:
  --base <分支>      目标分支，默认 dev-v3
  --repo <owner/name> PR 所在仓库，默认 dataease/dataease
  --no-push          不执行 git push，假定当前分支已推送
  --draft            创建草稿 PR
  --dry-run          只打印将执行的 gh api 命令
  -h, --help         显示帮助
USAGE
}

die() {
  echo "$1" >&2
  exit 1
}

while [ $# -gt 0 ]; do
  case "$1" in
    --base) base="${2:-}"; shift 2 ;;
    --repo) repo="${2:-}"; shift 2 ;;
    --title) title="${2:-}"; shift 2 ;;
    --body-file) body_file="${2:-}"; shift 2 ;;
    --no-push) push_branch=0; shift ;;
    --draft) draft=1; shift ;;
    --dry-run) dry_run=1; shift ;;
    -h|--help) usage; exit 0 ;;
    *) die "未知参数: $1（使用 --help 查看用法）" ;;
  esac
done

[ -n "$title" ] || die "缺少 --title"
[ -n "$body_file" ] || die "缺少 --body-file"
[ -f "$body_file" ] || die "--body-file 不存在: $body_file"
[ -n "$base" ] || die "--base 不能为空"
[ -n "$repo" ] || die "--repo 不能为空"

branch="$(git branch --show-current)"
[ -n "$branch" ] || die "当前处于 detached HEAD，无法创建 PR"
case "$branch" in
  dev-v3|main) die "拒绝直接从 $branch 创建 PR，请使用任务分支" ;;
esac

if [ "$push_branch" -eq 1 ]; then
  git push -u origin "$branch"
fi

args=(--method POST "repos/$repo/pulls" -f "base=$base" -f "head=$branch" -f "title=$title" -f "body=$(cat "$body_file")")
if [ "$draft" -eq 1 ]; then
  args+=(-F draft=true)
fi

if [ "$dry_run" -eq 1 ]; then
  printf 'gh api'
  printf ' %q' "${args[@]}"
  printf '\n'
  exit 0
fi

url="$(gh api "${args[@]}" --jq '.html_url')"
echo "PR created: $url"
