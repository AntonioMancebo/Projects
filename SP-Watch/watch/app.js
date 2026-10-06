import { BaseApp } from '@zeppos/zml/base-app'
import { log as Logger } from '@zos/utils'

const logger = Logger.getLogger('sp-watch-app')

App(
  BaseApp({
    globalData: {},
    onCreate() {
      logger.log('SP Watch created')
    },
    onDestroy() {
      logger.log('SP Watch destroyed')
    }
  })
)
