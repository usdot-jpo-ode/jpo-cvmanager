/**
 * This function searches for the every react-aria SSR ids in a given HTMLElement node and replace every attribute values with a static id
 *
 * This can be usefull when you're trying to generate a snapshot of components using react-aria under the hood
 *
 * @ex :
 * ```
 * const { container } = render(<Component />);
 *
 * replaceReactAriaIds(container);
 * ```
 *
 * @param container The HTMLElement node to search for SSR ids
 */
function replaceChaoticIds(container) {
  const props = [
    {
      selector: "class",
      updateFunc: (val) =>
        val.replace(
          /css-[0-9a-z]{6}-MuiTableCell-root-MuiTablePagination-root/g,
          "css-mocked-MuiTableCell-root-MuiTablePagination-root"
        ),
    },
  ];

  container.querySelectorAll("td").forEach((item) => {
    props.forEach((prop) => {
      if (item.getAttribute(prop.selector)) {
        item.setAttribute(prop.selector, prop.updateFunc(item.getAttribute(prop.selector)));
      }
    });
  });
  container.querySelectorAll("input[aria-invalid]").forEach((item) => {
    delete item["aria-invalid"];
  });
  return container;
}

export { replaceChaoticIds };
