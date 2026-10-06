import { BaseSideService, settingsLib } from '@zeppos/zml/base-side'

function config() {
  return {
    bridgeUrl: String(settingsLib.getItem('bridgeUrl') || '').replace(/\/$/, ''),
    bridgeToken: String(settingsLib.getItem('bridgeToken') || ''),
    scope: String(settingsLib.getItem('scope') || 'today')
  }
}

function parseBody(body) {
  if (typeof body === 'string') {
    try {
      return JSON.parse(body)
    } catch (_) {
      return { raw: body }
    }
  }
  return body || {}
}

async function bridgeRequest(path, options = {}) {
  const { bridgeUrl, bridgeToken } = config()
  if (!bridgeUrl) throw new Error('BRIDGE_NOT_CONFIGURED')

  const response = await fetch({
    url: `${bridgeUrl}${path}`,
    method: options.method || 'GET',
    headers: {
      'Content-Type': 'application/json',
      ...(bridgeToken ? { Authorization: `Bearer ${bridgeToken}` } : {})
    },
    body: options.body ? JSON.stringify(options.body) : undefined
  })

  const body = parseBody(response.body)
  if (body && body.ok === false) {
    throw new Error(body.error || 'BRIDGE_ERROR')
  }
  return body
}

async function getTasks() {
  const { scope } = config()
  const body = await bridgeRequest(`/api/tasks?scope=${encodeURIComponent(scope)}&limit=40`)
  return Array.isArray(body.tasks) ? body.tasks : []
}

AppSideService(
  BaseSideService({
    onInit() {},

    async onRequest(req, res) {
      try {
        if (req.method === 'GET_TASKS') {
          const tasks = await getTasks()
          res(null, { tasks, source: config().scope })
          return
        }

        if (req.method === 'COMPLETE_TASK') {
          const id = encodeURIComponent(req.params.id)
          await bridgeRequest(`/api/tasks/${id}/complete`, { method: 'POST' })
          const tasks = await getTasks()
          res(null, { tasks })
          return
        }

        if (req.method === 'START_TASK') {
          const id = encodeURIComponent(req.params.id)
          await bridgeRequest(`/api/tasks/${id}/start`, { method: 'POST' })
          res(null, { ok: true })
          return
        }

        if (req.method === 'HEALTH') {
          const health = await bridgeRequest('/health')
          res(null, health)
          return
        }

        res('UNKNOWN_METHOD')
      } catch (error) {
        console.log('SP Watch side error', String(error))
        res(String(error))
      }
    },

    onSettingsChange({ key }) {
      if (key === 'bridgeUrl' || key === 'bridgeToken' || key === 'scope') {
        this.call({ type: 'CONFIG_CHANGED' })
      }
    },

    onRun() {},
    onDestroy() {}
  })
)
