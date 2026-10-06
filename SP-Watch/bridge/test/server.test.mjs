import test from 'node:test'
import assert from 'node:assert/strict'
import { normalizeTask, sortTasks } from '../server.mjs'

test('normalizeTask keeps only watch-safe fields', () => {
  const projects = new Map([['p1', 'Trabajo']])
  assert.deepEqual(
    normalizeTask({ id: '1', title: 'Preparar clase', projectId: 'p1', isDone: false, notes: 'private' }, projects),
    {
      id: '1',
      title: 'Preparar clase',
      projectId: 'p1',
      project: 'Trabajo',
      isDone: false,
      parentId: null,
      dueDay: null,
      dueWithTime: null,
      plannedAt: null,
      timeEstimate: 0,
      timeSpent: 0
    }
  )
})

test('sortTasks prioritizes scheduled tasks', () => {
  const result = sortTasks([
    { title: 'B', plannedAt: null },
    { title: 'C', plannedAt: 200 },
    { title: 'A', plannedAt: 100 }
  ])
  assert.deepEqual(result.map((x) => x.title), ['A', 'C', 'B'])
})
