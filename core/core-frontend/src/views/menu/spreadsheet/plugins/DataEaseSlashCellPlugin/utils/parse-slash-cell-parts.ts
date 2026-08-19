export function parseSlashCellParts(value: unknown): string[] {
  if (value === undefined || value === null) {
    return ['']
  }

  const text = String(value)
  const parts: string[] = []
  let current = ''
  let escaping = false

  for (const char of text) {
    if (escaping) {
      if (char === ',' || char === '\\') {
        current += char
      } else {
        current += `\\${char}`
      }
      escaping = false
      continue
    }

    if (char === '\\') {
      escaping = true
      continue
    }

    if (char === ',') {
      parts.push(current)
      current = ''
      continue
    }

    current += char
  }

  if (escaping) {
    current += '\\'
  }

  parts.push(current)
  return parts
}
