export const Options = (deleteTitle, deleteMessage, buttons) => {return {
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
  onKeypressEscape: () => {}
}};