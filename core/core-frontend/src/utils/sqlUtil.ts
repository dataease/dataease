// 去除 SQL 注释，规则与后端 SqlUtil.removeSqlComments 保持一致：
// 保留字符串/标识符/dollar-quoted string 内的注释符号，仅移除真正的 --、#、/* */ 注释。
export const removeSqlComments = (sql: string): string => {
  if (!sql) {
    return sql
  }

  let result = ''
  let i = 0
  while (i < sql.length) {
    const c = sql.charAt(i)

    const dollarEnd = dollarQuoteEnd(sql, i)
    if (dollarEnd > i) {
      result += sql.substring(i, dollarEnd)
      i = dollarEnd
      continue
    }

    if (c === "'") {
      let j = i + 1
      while (j < sql.length) {
        const sc = sql.charAt(j)
        if (sc === "'") {
          if (j + 1 < sql.length && sql.charAt(j + 1) === "'") {
            j += 2
            continue
          }
          j++
          break
        }
        j++
      }
      result += sql.substring(i, j)
      i = j
      continue
    }

    if (c === '"') {
      let j = i + 1
      while (j < sql.length) {
        const sc = sql.charAt(j)
        if (sc === '"') {
          if (j + 1 < sql.length && sql.charAt(j + 1) === '"') {
            j += 2
            continue
          }
          j++
          break
        }
        j++
      }
      result += sql.substring(i, j)
      i = j
      continue
    }

    if (c === '`') {
      let j = i + 1
      while (j < sql.length && sql.charAt(j) !== '`') {
        j++
      }
      if (j < sql.length) {
        j++
      }
      result += sql.substring(i, j)
      i = j
      continue
    }

    // 单行注释 --，保留换行符避免与下一行粘连
    if (c === '-' && i + 1 < sql.length && sql.charAt(i + 1) === '-') {
      let j = i + 2
      while (j < sql.length && sql.charAt(j) !== '\n' && sql.charAt(j) !== '\r') {
        j++
      }
      if (j < sql.length) {
        result += sql.charAt(j)
        j++
      }
      i = j
      continue
    }

    // 单行注释 #（兼容 MySQL），但保留 PostgreSQL 的 #>/#>>/#- JSON 运算符
    if (c === '#') {
      const isIdentifier =
        (i > 0 && isJavaIdentifierPart(sql.charAt(i - 1))) ||
        (i + 1 < sql.length && isJavaIdentifierPart(sql.charAt(i + 1)))
      const postgresOperator =
        i + 1 < sql.length && (sql.charAt(i + 1) === '>' || sql.charAt(i + 1) === '-')
      if (!isIdentifier && !postgresOperator) {
        let j = i + 1
        while (j < sql.length && sql.charAt(j) !== '\n' && sql.charAt(j) !== '\r') {
          j++
        }
        if (j < sql.length) {
          result += sql.charAt(j)
          j++
        }
        i = j
        continue
      }
    }

    // 多行注释
    if (c === '/' && i + 1 < sql.length && sql.charAt(i + 1) === '*') {
      const end = sql.indexOf('*/', i + 2)
      i = end >= 0 ? end + 2 : sql.length
      continue
    }

    result += c
    i++
  }

  return result
}

const isJavaIdentifierPart = (ch: string): boolean => {
  return /[\w$]/.test(ch)
}

const dollarQuoteEnd = (sql: string, start: number): number => {
  if (sql.charAt(start) !== '$' || start + 1 >= sql.length) {
    return -1
  }
  // 仅在 token 边界识别 dollar quote，避免把 a$$ 这类标识符当成字符串起始
  if (start > 0 && /[\w$]/.test(sql.charAt(start - 1))) {
    return -1
  }
  const next = start + 1
  if (sql.charAt(next) === '$') {
    const end = sql.indexOf('$$', next + 1)
    return end >= 0 ? end + 2 : sql.length
  }
  if (/[A-Za-z_]/.test(sql.charAt(next))) {
    let tagEnd = next
    while (tagEnd < sql.length && /[A-Za-z0-9_]/.test(sql.charAt(tagEnd))) {
      tagEnd++
    }
    if (tagEnd < sql.length && sql.charAt(tagEnd) === '$') {
      const tag = sql.substring(start, tagEnd + 1)
      const end = sql.indexOf(tag, tagEnd + 1)
      return end >= 0 ? end + tag.length : sql.length
    }
  }
  return -1
}
