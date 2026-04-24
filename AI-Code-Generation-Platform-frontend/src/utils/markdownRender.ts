import MarkdownIt from 'markdown-it'
import DOMPurify from 'dompurify'
import hljs from 'highlight.js/lib/core'
import bash from 'highlight.js/lib/languages/bash'
import css from 'highlight.js/lib/languages/css'
import javascript from 'highlight.js/lib/languages/javascript'
import json from 'highlight.js/lib/languages/json'
import typescript from 'highlight.js/lib/languages/typescript'
import xml from 'highlight.js/lib/languages/xml'

import 'highlight.js/styles/github.css'

hljs.registerLanguage('javascript', javascript)
hljs.registerLanguage('js', javascript)
hljs.registerLanguage('typescript', typescript)
hljs.registerLanguage('ts', typescript)
hljs.registerLanguage('css', css)
hljs.registerLanguage('xml', xml)
hljs.registerLanguage('json', json)
hljs.registerLanguage('bash', bash)
hljs.registerLanguage('sh', bash)

function resolveHighlightLang(raw: string): string | null {
  const name = raw.trim().toLowerCase()
  if (!name) return null
  const map: Record<string, string> = {
    js: 'javascript',
    javascript: 'javascript',
    mjs: 'javascript',
    cjs: 'javascript',
    ts: 'typescript',
    typescript: 'typescript',
    html: 'xml',
    htm: 'xml',
    vue: 'xml',
    svg: 'xml',
    xml: 'xml',
    css: 'css',
    scss: 'css',
    less: 'css',
    json: 'json',
    bash: 'bash',
    sh: 'bash',
    shell: 'bash',
    zsh: 'bash',
  }
  const resolved = map[name] ?? name
  return hljs.getLanguage(resolved) ? resolved : null
}

const md = new MarkdownIt({
  html: true,
  linkify: true,
  breaks: true,
  typographer: true,
})

md.options.highlight = (str: string, langRaw: string): string => {
  const lang = resolveHighlightLang(langRaw || '')
  const rawLabel = (langRaw || lang || 'text').trim().toLowerCase()
  const fenceLabel = md.utils.escapeHtml(rawLabel)
  if (lang) {
    try {
      const { value } = hljs.highlight(str, { language: lang, ignoreIllegals: true })
      return `<div class="md-fence"><pre class="hljs"><code class="language-${fenceLabel}">${value}</code></pre></div>`
    } catch {
      // fall through
    }
  }
  return `<div class="md-fence"><pre class="hljs"><code class="language-${fenceLabel}">${md.utils.escapeHtml(str)}</code></pre></div>`
}

const defaultLinkOpen =
  md.renderer.rules.link_open ??
  ((tokens, idx, options, env, self) => self.renderToken(tokens, idx, options))

md.renderer.rules.link_open = (tokens, idx, options, env, self) => {
  const token = tokens[idx]
  if (!token) {
    return defaultLinkOpen(tokens, idx, options, env, self)
  }
  if (token.attrIndex('target') < 0) {
    token.attrPush(['target', '_blank'])
    token.attrPush(['rel', 'noopener noreferrer'])
  }
  return defaultLinkOpen(tokens, idx, options, env, self)
}

/**
 * 将 AI 回复 Markdown 转为可安全插入页面的 HTML（代码块由 highlight.js 着色）
 */
export function renderAssistantMarkdown(source: string): string {
  const s = source.trim()
  if (!s) return ''
  const dirty = md.render(s)
  return DOMPurify.sanitize(dirty, {
    ADD_ATTR: ['target', 'rel', 'class', 'id', 'href', 'src', 'alt', 'title', 'colspan', 'rowspan'],
    ALLOW_DATA_ATTR: false,
  })
}
