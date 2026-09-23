import { Disposable, ICommandService } from '@univerjs/core'
import {
  AddHyperLinkCommand,
  AddRichHyperLinkCommand,
  UpdateHyperLinkCommand,
  UpdateRichHyperLinkCommand
} from '@univerjs/sheets-hyper-link'

const ADD_COMMANDS = new Set([AddHyperLinkCommand.id, AddRichHyperLinkCommand.id])
const UPDATE_COMMANDS = new Set([UpdateHyperLinkCommand.id, UpdateRichHyperLinkCommand.id])

/** Restore application routes shortened by Univer's same-page URL serializer. */
export const restoreApplicationHyperLink = (payload: string, currentUrl: string): string => {
  if (!payload.startsWith('#/')) return payload
  const url = new URL(currentUrl)
  // The serializer only shortens links without a search string.
  url.search = ''
  url.hash = payload
  return url.href
}

export class DataEaseHyperLinkController extends Disposable {
  constructor(@ICommandService commandService: ICommandService) {
    super()
    this.disposeWithMe(
      commandService.beforeCommandExecuted(command => {
        if (!ADD_COMMANDS.has(command.id) && !UPDATE_COMMANDS.has(command.id)) return
        const params = command.params as {
          link?: { payload?: string }
          payload?: { payload?: string }
        }
        const content = ADD_COMMANDS.has(command.id) ? params?.link : params?.payload
        if (typeof content?.payload === 'string') {
          content.payload = restoreApplicationHyperLink(content.payload, window.location.href)
        }
      })
    )
  }
}
