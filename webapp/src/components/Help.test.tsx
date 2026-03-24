import { render } from '@testing-library/react'
import Help from './Help'
import { replaceChaoticIds } from '../utils/test-utils'
import { MemoryRouter } from 'react-router-dom'

it('should take a snapshot', () => {
  jest.mock('../EnvironmentVars', () => ({
    EnvironmentVars: {
      ENABLE_RSU_FEATURES: true,
      ENABLE_WZDX_FEATURES: true,
      ENABLE_HAAS_FEATURES: true,
    },
  }))

  const { container } = render(
    <MemoryRouter>
      <Help />
    </MemoryRouter>
  )

  expect(replaceChaoticIds(container)).toMatchSnapshot()
})
