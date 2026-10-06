import http from 'node:http'
import { URL } from 'node:url'

const HOST = process.env.HOST || '0.0.0.0'
const PORT = Number(process.env.PORT || 8787)
const SP_BASE_URL = (process.env.SP_BASE_URL || 'http://127.0.0.1:3876').replace(/\/$/, '')
const SP_API_TOKEN = process.env.SP_API_TOKEN || ''
const WATCH_TOKEN = process.env.WATCH_TOKEN || ''

export function normalizeTask(task, projects = new Map()) {
  return {
    id: String(task.id),
    title: String(task.title || 'Sin título'),
    projectId: task.projectId || null,
    project: projects.get(task.projectId) || '',
    isDone: Boolean(task.isDone),
    parentId: task.parentId || null,
    dueDay: task.dueDay || null,
    dueWithTime: task.dueWithTime || null,
    plannedAt: task.plannedAt || null,
    timeEstimate: Number(task.timeEstimate || 0),
    timeSpent: Number(task.timeSpent || 0)
  }
}

export function sortTasks(tasks) {
  return [...tasks].sort((a, b) => {
    const aa = Number(a.plannedAt || a.dueWithTime || 0)
    const bb = Number(b.plannedAt || b.dueWithTime || 0)
    if (aa && bb && aa !== bb) return aa - bb
    if (aa && !bb) return -1
    if (!aa && bb) return 1
    return a.title.localeCompare(b.title, 'es')
  })
}

function sendJson(res, status, data) {
  const body = JSON.stringify(data)
  res.writeHead(status, {
    'Content-Type': 'application/json; charset=utf-8',
    'Content-Length': Buffer.byteLength(body),
    'Cache-Control': 'no-store'
  })
  res.end(body)
}

function authorized(req) {
  if (!WATCH_TOKEN) return true
  return req.headers.authorization === `Bearer ${WATCH_TOKEN}`
}

async function spFetch(path, options = {}) {
  const headers = {
    Accept: 'application/json',
    ...(options.body ? { 'Content-Type': 'application/json' } : {}),
    ...(SP_API_TOKEN ? { Authorization: `Bearer ${SP_API_TOKEN}` } : {})
  }

  const response = await fetch(`${SP_BASE_URL}${path}`, {
    method: options.method || 'GET',
    headers,
    body: options.body ? JSON.stringify(options.body) : undefined
  })

  const text = await response.text()
  let parsed
  try {
    parsed = text ? JSON.parse(text) : {}
  } catch {
    parsed = { ok: false, error: { message: text || `HTTP ${response.status}` } }
  }

  if (!response.ok || parsed.ok === false) {
    const message = parsed?.error?.message || `Super Productivity HTTP ${response.status}`
    const err = new Error(message)
    err.status = response.status || 502
    throw err
  }
  return parsed.data
}

async function projectMap() {
  const projects = await spFetch('/projects')
  return new Map((Array.isArray(projects) ? projects : []).map((p) => [p.id, p.title]))
}

async function listTasks(scope = 'today', limit = 40) {
  const query = scope === 'all' ? '/tasks?includeDone=false' : '/tasks?tagId=TODAY&includeDone=false'
  const [tasks, projects] = await Promise.all([spFetch(query), projectMap()])
  const normalized = (Array.isArray(tasks) ? tasks : [])
    .filter((task) => !task.isDone)
    .map((task) => normalizeTask(task, projects))
  return sortTasks(normalized).slice(0, Math.max(1, Math.min(limit, 100)))
}

export async function handler(req, res) {
  const url = new URL(req.url, `http://${req.headers.host || 'localhost'}`)

  if (url.pathname === '/health') {
    try {
      const health = await spFetch('/health')
      sendJson(res, 200, { ok: true, bridge: 'up', superProductivity: health })
    } catch (error) {
      sendJson(res, 503, { ok: false, bridge: 'up', error: error.message })
    }
    return
  }

  if (!authorized(req)) {
    sendJson(res, 401, { ok: false, error: 'UNAUTHORIZED' })
    return
  }

  if (req.method === 'GET' && url.pathname === '/api/tasks') {
    const scope = url.searchParams.get('scope') === 'all' ? 'all' : 'today'
    const limit = Number(url.searchParams.get('limit') || 40)
    const tasks = await listTasks(scope, Number.isFinite(limit) ? limit : 40)
    sendJson(res, 200, { ok: true, scope, tasks })
    return
  }

  const completeMatch = url.pathname.match(/^\/api\/tasks\/([^/]+)\/complete$/)
  if (req.method === 'POST' && completeMatch) {
    const id = decodeURIComponent(completeMatch[1])
    await spFetch(`/tasks/${encodeURIComponent(id)}`, {
      method: 'PATCH',
      body: { isDone: true }
    })
    sendJson(res, 200, { ok: true, id })
    return
  }

  const startMatch = url.pathname.match(/^\/api\/tasks\/([^/]+)\/start$/)
  if (req.method === 'POST' && startMatch) {
    const id = decodeURIComponent(startMatch[1])
    await spFetch(`/tasks/${encodeURIComponent(id)}/start`, { method: 'POST' })
    sendJson(res, 200, { ok: true, id })
    return
  }

  sendJson(res, 404, { ok: false, error: 'NOT_FOUND' })
}

export function createServer() {
  return http.createServer((req, res) => {
    Promise.resolve(handler(req, res)).catch((error) => {
      console.error('[sp-watch-bridge]', error)
      sendJson(res, error.status && error.status >= 400 ? error.status : 500, {
        ok: false,
        error: error.message || 'INTERNAL_ERROR'
      })
    })
  })
}

if (process.argv[1] && process.argv[1].endsWith('server.mjs')) {
  createServer().listen(PORT, HOST, () => {
    console.log(`SP Watch bridge listening on http://${HOST}:${PORT}`)
    console.log(`Super Productivity API: ${SP_BASE_URL}`)
    console.log(`Watch auth: ${WATCH_TOKEN ? 'enabled' : 'disabled (set WATCH_TOKEN)'}`)
  })
}
