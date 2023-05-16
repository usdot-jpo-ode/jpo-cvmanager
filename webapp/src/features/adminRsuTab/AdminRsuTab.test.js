import React from 'react'
import { render, fireEvent, screen } from '@testing-library/react'
import AdminRsuTab from './AdminRsuTab'
import { Provider } from 'react-redux'
import { setupStore } from '../../store'
import { replaceChaoticIds } from '../../utils/test-utils'

it('snapshot add', () => {
  const { container } = render(
    <Provider store={setupStore({ adminRsuTab: { loading: false, value: { activeDiv: 'rsu_table' } } })}>
      <AdminRsuTab />
    </Provider>
  )

  fireEvent.click(screen.queryByTitle('Add RSU'))

  expect(replaceChaoticIds(container)).toMatchSnapshot()
})

it('snapshot refresh', () => {
  const { container } = render(
    <Provider store={setupStore({ adminRsuTab: { loading: false, value: { activeDiv: 'rsu_table' } } })}>
      <AdminRsuTab />
    </Provider>
  )

  fireEvent.click(screen.queryByTitle('Refresh RSUs'))

  expect(replaceChaoticIds(container)).toMatchSnapshot()
})
