import React from 'react'
import { render } from '@testing-library/react'
import RsuMapView from './RsuMapView'
import { Provider } from 'react-redux'
import { setupStore } from '../store'
import { replaceChaoticIds } from '../utils/test-utils'

it('should take a snapshot', () => {
  const initialState = {
    rsu: {
      value: {
        selectedRsu: {
          properties: {
            ipv4_address: '1.1.1.1',
          },
          geometry: { coordinates: [0, 1] },
        },
        selectedSrm: [{ long: 1, lat: 1 }],
        srmSsmList: [
          { ip: '1.1.1.1', type: 'srmTx' },
          { ip: '1.1.1.1', type: 'other' },
        ],
        rsuMapData: {
          features: [
            {
              properties: {
                ingressPath: 'true',
                egressPath: 'true',
              },
            },
          ],
        },
      },
    },
  }
  const { container } = render(
    <Provider store={setupStore(initialState)}>
      <RsuMapView />
    </Provider>
  )

  expect(replaceChaoticIds(container)).toMatchSnapshot()
})
