import React from 'react'
import { render, screen, fireEvent, queryByAttribute } from '@testing-library/react'
import ConfigureItem from './ConfigureItem'
import { replaceChaoticIds } from '../utils/test-utils'

it('should take a snapshot', () => {
  const updateRsu = jest.fn()
  const { container } = render(<ConfigureItem indexList={[0]} updateRsu={updateRsu} index={0} ip={''} />)

  expect(replaceChaoticIds(container)).toMatchSnapshot()

  const getById = queryByAttribute.bind(null, 'id')

  fireEvent.click(getById(container, 'selectedconfigitemdiv'))
  expect(updateRsu).toHaveBeenCalledTimes(1)
  expect(updateRsu).toHaveBeenCalledWith(0, true)

  fireEvent.click(getById(container, 'selectedconfigitemdiv'))
  expect(updateRsu).toHaveBeenCalledTimes(2)
  expect(updateRsu).toHaveBeenCalledWith(0, false)
})
