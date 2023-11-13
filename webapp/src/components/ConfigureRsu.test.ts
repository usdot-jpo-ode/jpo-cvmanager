import React from 'react'
import { render } from '@testing-library/react'
import ConfigureRsu from './ConfigureRsu'
import { Provider } from 'react-redux'
import { setupStore } from '../store'
import { replaceChaoticIds } from '../utils/test-utils'

describe('ConfigureRsu component', () => {
  it('should take a single RSU snapshot', () => {
    const { container } = render(
      <Provider
        store={setupStore({
          rsu: {
            value: {
              selectedRsu: {
                properties: {
                  primary_route: 'primary_route',
                  milepost: 111,
                  ipv4_address: '1.1.1.1',
                },
              },
            },
          },
        })}
      >
        <ConfigureRsu />
      </Provider>
    )

    expect(replaceChaoticIds(container)).toMatchSnapshot()
  })

  it('should take a multi-RSU snapshot', () => {
    const { container } = render(
      <Provider
        store={setupStore({
          config: {
            value: {
              configList: ['1.1.1.1', '2.2.2.2', '3.3.3.3'],
            },
          },
        })}
      >
        <ConfigureRsu />
      </Provider>
    )

    expect(replaceChaoticIds(container)).toMatchSnapshot()
  })
})
