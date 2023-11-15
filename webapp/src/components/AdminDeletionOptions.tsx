import React from 'react'

export const Options = (
  deleteTitle: string,
  deleteMessage: string,
  buttons: {
    label: string
    onClick: () => void
  }[]
) => {
  return {
    title: deleteTitle,
    message: deleteMessage,
    buttons: buttons,
    childrenElement: () => <div />,
    closeOnEscape: true,
    closeOnClickOutside: true,
    keyCodeForClose: [8, 32],
    willUnmount: () => {},
    afterClose: () => {},
    onClickOutside: () => {},
    onKeypressEscape: () => {},
  }
}
