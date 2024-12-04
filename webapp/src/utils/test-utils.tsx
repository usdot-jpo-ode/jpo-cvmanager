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

import React from 'react'
import { LocalizationProvider } from '@mui/x-date-pickers/LocalizationProvider'
import { AdapterDayjs } from '@mui/x-date-pickers/AdapterDayjs'
import dayjs from 'dayjs'
import utc from 'dayjs/plugin/utc'
import timezone from 'dayjs/plugin/timezone'

// Extend dayjs with timezone and UTC plugins
dayjs.extend(utc)
dayjs.extend(timezone)

// Mock component to set timezone
const MockLocalizationProvider = ({ children }) => {
  dayjs.tz.setDefault('America/Denver')
  return <LocalizationProvider dateAdapter={AdapterDayjs}>{children}</LocalizationProvider>
}

export { replaceChaoticIds, MockLocalizationProvider }
