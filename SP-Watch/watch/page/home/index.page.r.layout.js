import * as hmUI from '@zos/ui'
import { getDeviceInfo } from '@zos/device'
import { px } from '@zos/utils'

export const { width: W, height: H } = getDeviceInfo()

export const HEADER = {
  x: px(56),
  y: px(30),
  w: W - px(112),
  h: px(44),
  text: 'SUPER PRODUCTIVITY',
  color: 0xffffff,
  text_size: px(28),
  align_h: hmUI.align.CENTER_H,
  align_v: hmUI.align.CENTER_V
}

export const STATUS = {
  x: px(58),
  y: px(78),
  w: W - px(116),
  h: px(28),
  text: 'Conectando…',
  color: 0x9ca3af,
  text_size: px(20),
  align_h: hmUI.align.CENTER_H,
  align_v: hmUI.align.CENTER_V
}

export const ROW_Y = [112, 184, 256, 328].map(px)
export const ROW_H = px(62)

export const TASK_TEXT = {
  x: px(48),
  w: px(330),
  h: ROW_H,
  color: 0xffffff,
  text_size: px(24),
  align_h: hmUI.align.LEFT,
  align_v: hmUI.align.CENTER_V,
  text_style: hmUI.text_style.ELLIPSIS
}

export const DONE_BUTTON = {
  x: px(382),
  w: px(54),
  h: px(54),
  radius: px(27),
  normal_color: 0x263238,
  press_color: 0x455a64,
  color: 0xffffff,
  text_size: px(26),
  text: '✓'
}

export const NAV_BUTTON = {
  y: px(404),
  w: px(74),
  h: px(48),
  radius: px(24),
  normal_color: 0x202124,
  press_color: 0x3c4043,
  color: 0xffffff,
  text_size: px(24)
}
