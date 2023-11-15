/**
 * This function searches for the every somewhat random/chaotic class name and property that cause snapshot tests to be inconsistent.
 * This current list includes MUI classes (css-*) and aria-invalid attributes.
 *
 * @ex :
 * ```
 * const { container } = render(<Component />);
 *
 * replaceChaoticIds(container);
 * ```
 *
 * @param container The HTMLElement node to search for SSR ids
 */
function replaceChaoticIds(container: HTMLElement) {
  const props = [
    {
      selector: 'class',
      updateFunc: (val: string) => val.replace(/css-[0-9a-z]*?-/g, 'css-mocked-'),
    },
  ]

  container.querySelectorAll('*').forEach((item) => {
    props.forEach((prop) => {
      if (item.getAttribute(prop.selector)) {
        item.setAttribute(prop.selector, prop.updateFunc(item.getAttribute(prop.selector)))
      }
    })
  })
  container.querySelectorAll('input[aria-invalid]').forEach((item) => {
    item.removeAttribute('aria-invalid')
  })
  return container
}

export { replaceChaoticIds }
