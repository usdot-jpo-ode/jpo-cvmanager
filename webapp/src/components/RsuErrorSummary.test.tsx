import React from 'react'
import { render } from '@testing-library/react'
import RsuErrorSummary from './RsuErrorSummary'
import { replaceChaoticIds } from '../utils/test-utils'

it('should take a snapshot', () => {
  const { container } = render(
    <RsuErrorSummary
      rsu={'string'}
      online_status={''}
      scms_status={''}
      hidden={false}
      setHidden={function (): void {
        return null
      }}
    />
  )

  expect(replaceChaoticIds(container)).toMatchSnapshot()
})
