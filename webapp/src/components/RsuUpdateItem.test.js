import React from 'react'
import { render, fireEvent, queryByAttribute } from '@testing-library/react'
import RsuUpdateItem from './RsuUpdateItem'
import { replaceChaoticIds } from '../utils/test-utils'

it('snapshot osUpdateAvailable', () => {
  const handleUpdateOS = jest.fn()
  const handleUpdateFW = jest.fn()
  const ip = '1.1.1.1'

  const { container } = render(
    <RsuUpdateItem
      osUpdateAvailable={[ip]}
      fwUpdateAvailable={[]}
      handleUpdateOS={handleUpdateOS}
      handleUpdateFW={handleUpdateFW}
      ip={ip}
    />
  )

  const getById = queryByAttribute.bind(null, 'id')
  fireEvent.click(getById(container, 'updatebtn'))
  expect(handleUpdateOS).toHaveBeenCalledTimes(1)
  expect(handleUpdateOS).toHaveBeenCalledWith(ip)
  expect(handleUpdateFW).not.toHaveBeenCalled()

  expect(replaceChaoticIds(container)).toMatchSnapshot()
})

it('snapshot handleUpdateFW', () => {
  const handleUpdateOS = jest.fn()
  const handleUpdateFW = jest.fn()
  const ip = '1.1.1.1'

  const { container } = render(
    <RsuUpdateItem
      osUpdateAvailable={[]}
      fwUpdateAvailable={[ip]}
      handleUpdateOS={handleUpdateOS}
      handleUpdateFW={handleUpdateFW}
      ip={ip}
    />
  )

  const getById = queryByAttribute.bind(null, 'id')
  fireEvent.click(getById(container, 'updatebtn'))
  expect(handleUpdateFW).toHaveBeenCalledTimes(1)
  expect(handleUpdateFW).toHaveBeenCalledWith(ip)
  expect(handleUpdateOS).not.toHaveBeenCalled()

  expect(replaceChaoticIds(container)).toMatchSnapshot()
})
