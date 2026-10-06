import * as hmUI from '@zos/ui'
import { BasePage } from '@zeppos/zml/base-page'
import { LocalStorage } from '@zos/storage'
import { log as Logger } from '@zos/utils'
import {
  HEADER,
  STATUS,
  ROW_Y,
  ROW_H,
  TASK_TEXT,
  DONE_BUTTON,
  NAV_BUTTON
} from 'zosLoader:./index.page.[pf].layout.js'

const logger = Logger.getLogger('sp-watch-home')
const PAGE_SIZE = 4
const storage = new LocalStorage('sp-watch-cache')

Page(
  BasePage({
    state: {
      tasks: storage.getItem('tasks', []),
      page: 0,
      statusWidget: null,
      titleWidgets: [],
      doneButtons: [],
      pageWidget: null,
      busy: false
    },

    build() {
      hmUI.createWidget(hmUI.widget.TEXT, HEADER)
      this.state.statusWidget = hmUI.createWidget(hmUI.widget.TEXT, STATUS)

      for (let i = 0; i < PAGE_SIZE; i += 1) {
        const y = ROW_Y[i]
        this.state.titleWidgets.push(
          hmUI.createWidget(hmUI.widget.TEXT, {
            ...TASK_TEXT,
            y,
            text: ''
          })
        )
        this.state.doneButtons.push(
          hmUI.createWidget(hmUI.widget.BUTTON, {
            ...DONE_BUTTON,
            y: y + 4,
            click_func: () => this.completeVisibleTask(i),
            longpress_func: () => this.startVisibleTask(i)
          })
        )
      }

      hmUI.createWidget(hmUI.widget.BUTTON, {
        ...NAV_BUTTON,
        x: 88,
        text: '‹',
        click_func: () => this.prevPage()
      })

      hmUI.createWidget(hmUI.widget.BUTTON, {
        ...NAV_BUTTON,
        x: 203,
        text: '↻',
        click_func: () => this.loadTasks(true)
      })

      hmUI.createWidget(hmUI.widget.BUTTON, {
        ...NAV_BUTTON,
        x: 318,
        text: '›',
        click_func: () => this.nextPage()
      })

      this.state.pageWidget = hmUI.createWidget(hmUI.widget.TEXT, {
        x: 170,
        y: 454,
        w: 140,
        h: 22,
        text: '',
        color: 0x8a8f98,
        text_size: 17,
        align_h: hmUI.align.CENTER_H
      })

      this.renderTasks()
      this.loadTasks(false)
    },

    setStatus(text, color = 0x9ca3af) {
      if (!this.state.statusWidget) return
      this.state.statusWidget.setProperty(hmUI.prop.MORE, {
        ...STATUS,
        text,
        color
      })
    },

    loadTasks(showToast = false) {
      if (this.state.busy) return
      this.state.busy = true
      this.setStatus('Sincronizando…')

      this.request({ method: 'GET_TASKS' })
        .then(({ tasks = [], source = 'bridge' }) => {
          this.state.tasks = tasks
          storage.setItem('tasks', tasks)
          const maxPage = Math.max(0, Math.ceil(tasks.length / PAGE_SIZE) - 1)
          this.state.page = Math.min(this.state.page, maxPage)
          this.renderTasks()
          this.setStatus(`${tasks.length} pendientes · ${source}`, 0x7dd3fc)
          if (showToast) hmUI.showToast({ text: 'Tareas actualizadas' })
        })
        .catch((error) => {
          logger.error('GET_TASKS failed', error)
          this.renderTasks()
          const cached = this.state.tasks.length
          this.setStatus(cached ? `Sin conexión · ${cached} en caché` : 'Configura el bridge', 0xfca5a5)
          if (showToast) hmUI.showToast({ text: 'No se pudo sincronizar' })
        })
        .then(() => {
          this.state.busy = false
        })
    },

    visibleTask(index) {
      return this.state.tasks[this.state.page * PAGE_SIZE + index]
    },

    completeVisibleTask(index) {
      if (this.state.busy) return
      const task = this.visibleTask(index)
      if (!task) return
      this.state.busy = true
      this.setStatus('Completando…')

      this.request({
        method: 'COMPLETE_TASK',
        params: { id: task.id }
      })
        .then(({ tasks = [] }) => {
          this.state.tasks = tasks
          storage.setItem('tasks', tasks)
          const maxPage = Math.max(0, Math.ceil(tasks.length / PAGE_SIZE) - 1)
          this.state.page = Math.min(this.state.page, maxPage)
          this.renderTasks()
          this.setStatus(`${tasks.length} pendientes`, 0x86efac)
          hmUI.showToast({ text: '✓ Tarea completada' })
        })
        .catch(() => {
          this.setStatus('Error al completar', 0xfca5a5)
          hmUI.showToast({ text: 'No se pudo completar' })
        })
        .then(() => {
          this.state.busy = false
        })
    },

    startVisibleTask(index) {
      if (this.state.busy) return
      const task = this.visibleTask(index)
      if (!task) return
      this.state.busy = true
      this.setStatus('Iniciando tarea…')

      this.request({
        method: 'START_TASK',
        params: { id: task.id }
      })
        .then(() => {
          this.setStatus('Tarea iniciada', 0x86efac)
          hmUI.showToast({ text: '▶ Tarea iniciada' })
        })
        .catch(() => {
          this.setStatus('Error al iniciar', 0xfca5a5)
        })
        .then(() => {
          this.state.busy = false
        })
    },

    prevPage() {
      if (this.state.page > 0) {
        this.state.page -= 1
        this.renderTasks()
      }
    },

    nextPage() {
      const maxPage = Math.max(0, Math.ceil(this.state.tasks.length / PAGE_SIZE) - 1)
      if (this.state.page < maxPage) {
        this.state.page += 1
        this.renderTasks()
      }
    },

    renderTasks() {
      const totalPages = Math.max(1, Math.ceil(this.state.tasks.length / PAGE_SIZE))
      for (let i = 0; i < PAGE_SIZE; i += 1) {
        const task = this.visibleTask(i)
        const title = task ? task.title : ''
        this.state.titleWidgets[i] &&
          this.state.titleWidgets[i].setProperty(hmUI.prop.MORE, {
            ...TASK_TEXT,
            y: ROW_Y[i],
            text: title
          })
        this.state.doneButtons[i] &&
          this.state.doneButtons[i].setProperty(hmUI.prop.VISIBLE, Boolean(task))
      }
      if (this.state.pageWidget) {
        this.state.pageWidget.setProperty(hmUI.prop.TEXT, `${this.state.page + 1}/${totalPages}`)
      }
    }
  })
)
