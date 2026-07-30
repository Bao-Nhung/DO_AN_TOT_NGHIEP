import { parse as parseSfc } from '@vue/compiler-sfc'
import { baseParse, NodeTypes } from '@vue/compiler-dom'
import { parse as parseJs } from '@babel/parser'
import fs from 'node:fs/promises'
import path from 'node:path'
import process from 'node:process'

const root = path.resolve(import.meta.dirname, '..')
const sourceRoots = [
  path.join(root, 'src'),
  path.resolve(root, '../backend/src/main/java'),
  path.resolve(root, '../database'),
]
const outputPath = path.join(root, 'src/i18n/auto-en.json')
const backendOutputPath = path.resolve(root, '../backend/src/main/resources/i18n/auto-en.json')
const cachePath = path.join(root, 'scripts/.i18n-cache.json')
const vietnamesePattern = /[ÀÁÂÃÈÉÊÌÍÒÓÔÕÙÚĂĐĨŨƠƯẠ-ỹđ]/
const commonVietnamesePattern = /\b(vui lòng|không|đang|đã|chưa|sản phẩm|đơn hàng|khách hàng|nhân viên|thanh toán|đăng nhập|mật khẩu|tìm kiếm|thông báo|xác nhận|hủy|lưu|đóng|thêm|sửa|xóa|giá|màu|kích thước|ngày|tháng|năm|trạng thái|tất cả)\b/i

async function walk(directory) {
  const entries = await fs.readdir(directory, { withFileTypes: true })
  const files = []
  for (const entry of entries) {
    if (entry.name === 'node_modules' || entry.name === 'target' || entry.name === 'dist') continue
    const fullPath = path.join(directory, entry.name)
    if (entry.isDirectory()) files.push(...await walk(fullPath))
    else if (/\.(vue|js|java|sql)$/.test(entry.name)) files.push(fullPath)
  }
  return files
}

function normalize(value) {
  return String(value || '')
    .replace(/\\n/g, ' ')
    .replace(/\s+/g, ' ')
    .trim()
}

function isCandidate(value) {
  const text = normalize(value)
  if (text.length < 2 || text.length > 300) return false
  if (!vietnamesePattern.test(text) && !commonVietnamesePattern.test(text)) return false
  if (/^(https?:|\/api\/|bi-|var\(--|#[0-9a-f]{3,8}$)/i.test(text)) return false
  if (/^[\w.-]+@[\w.-]+\.[a-z]{2,}$/i.test(text)) return false
  return true
}

function collectTemplate(template, values) {
  if (!template) return
  let ast
  try {
    ast = baseParse(template)
  } catch {
    for (const match of template.matchAll(/>([^<>{}]+)</g)) {
      if (isCandidate(match[1])) values.add(normalize(match[1]))
    }
    for (const match of template.matchAll(/\b(?:placeholder|title|aria-label|alt)="([^"]+)"/g)) {
      if (isCandidate(match[1])) values.add(normalize(match[1]))
    }
    return
  }
  const visit = node => {
    if (node.type === NodeTypes.TEXT && isCandidate(node.content)) values.add(normalize(node.content))
    if (node.type === NodeTypes.ELEMENT) {
      for (const prop of node.props || []) {
        if (prop.type === NodeTypes.ATTRIBUTE && prop.value && isCandidate(prop.value.content)) {
          values.add(normalize(prop.value.content))
        }
      }
    }
    for (const child of node.children || []) visit(child)
  }
  visit(ast)
}

function collectJs(source, values) {
  let ast
  try {
    ast = parseJs(source, {
      sourceType: 'module',
      errorRecovery: true,
      plugins: ['jsx', 'importMeta', 'topLevelAwait'],
    })
  } catch {
    return
  }
  const visit = node => {
    if (!node || typeof node !== 'object') return
    if (node.type === 'StringLiteral' && isCandidate(node.value)) {
      values.add(normalize(node.value))
    } else if (node.type === 'TemplateLiteral') {
      const combined = node.quasis
        .map((quasi, index) => `${quasi.value.cooked || ''}${index < node.expressions.length ? `{${index}}` : ''}`)
        .join('')
      if (isCandidate(combined)) values.add(normalize(combined))
      for (const quasi of node.quasis) {
        if (isCandidate(quasi.value.cooked)) values.add(normalize(quasi.value.cooked))
      }
    }
    for (const [key, value] of Object.entries(node)) {
      if (key === 'loc' || key === 'start' || key === 'end') continue
      if (Array.isArray(value)) value.forEach(visit)
      else if (value && typeof value === 'object' && typeof value.type === 'string') visit(value)
    }
  }
  visit(ast)
}

function collectJava(source, values) {
  const stringPattern = /"((?:\\.|[^"\\])*)"/g
  for (const match of source.matchAll(stringPattern)) {
    const value = match[1]
      .replace(/\\"/g, '"')
      .replace(/\\n/g, ' ')
      .replace(/\\t/g, ' ')
    if (isCandidate(value)) values.add(normalize(value))
  }
}

function collectSql(source, values) {
  const stringPattern = /N'((?:''|[^'])*)'/g
  for (const match of source.matchAll(stringPattern)) {
    const value = match[1].replace(/''/g, "'")
    if (isCandidate(value)) values.add(normalize(value))
  }
}

async function translate(text) {
  const placeholders = []
  const protectedText = text.replace(/\{\d+}/g, placeholder => {
    placeholders.push(placeholder)
    return ` ZESTIAPARAM${placeholders.length - 1} `
  })
  const url = new URL('https://translate.googleapis.com/translate_a/single')
  url.searchParams.set('client', 'gtx')
  url.searchParams.set('sl', 'vi')
  url.searchParams.set('tl', 'en')
  url.searchParams.set('dt', 't')
  url.searchParams.set('q', protectedText)
  const response = await fetch(url, { signal: AbortSignal.timeout(15000) })
  if (!response.ok) throw new Error(`Translate HTTP ${response.status}`)
  const data = await response.json()
  let result = (data[0] || []).map(segment => segment[0]).join('')
  placeholders.forEach((placeholder, index) => {
    result = result.replace(new RegExp(`ZESTIAPARAM\\s*${index}`, 'gi'), placeholder)
  })
  return normalize(result)
}

async function main() {
  const values = new Set()
  for (const sourceRoot of sourceRoots) {
    for (const file of await walk(sourceRoot)) {
      const source = await fs.readFile(file, 'utf8')
      if (file.endsWith('.vue')) {
        const { descriptor } = parseSfc(source, { filename: file })
        collectTemplate(descriptor.template?.content, values)
        collectJs(descriptor.script?.content || '', values)
        collectJs(descriptor.scriptSetup?.content || '', values)
      } else if (file.endsWith('.js')) {
        collectJs(source, values)
      } else if (file.endsWith('.sql')) {
        collectSql(source, values)
      } else {
        collectJava(source, values)
      }
    }
  }

  let cache = {}
  try {
    cache = JSON.parse(await fs.readFile(cachePath, 'utf8'))
  } catch {
    // The first run starts with an empty local cache.
  }
  const pending = [...values].filter(value => !cache[value]).sort()
  process.stdout.write(`Found ${values.size} Vietnamese UI strings; translating ${pending.length} new strings.\n`)

  let cursor = 0
  const workers = Array.from({ length: 4 }, async () => {
    while (cursor < pending.length) {
      const index = cursor++
      const source = pending[index]
      try {
        cache[source] = await translate(source)
      } catch (error) {
        process.stderr.write(`Skipped: ${source} (${error.message})\n`)
      }
      if ((index + 1) % 25 === 0) {
        process.stdout.write(`Translated ${index + 1}/${pending.length}\n`)
        await fs.writeFile(cachePath, `${JSON.stringify(cache, null, 2)}\n`)
      }
    }
  })
  await Promise.all(workers)

  const output = Object.fromEntries(
    [...values]
      .filter(value => cache[value])
      .sort((left, right) => left.localeCompare(right, 'vi'))
      .map(value => [value, cache[value]])
  )
  await fs.writeFile(cachePath, `${JSON.stringify(cache, null, 2)}\n`)
  await fs.writeFile(outputPath, `${JSON.stringify(output, null, 2)}\n`)
  await fs.mkdir(path.dirname(backendOutputPath), { recursive: true })
  await fs.writeFile(backendOutputPath, `${JSON.stringify(output, null, 2)}\n`)
  process.stdout.write(`Wrote ${Object.keys(output).length} translations to ${outputPath}\n`)
  process.stdout.write(`Wrote backend translations to ${backendOutputPath}\n`)
}

await main()
