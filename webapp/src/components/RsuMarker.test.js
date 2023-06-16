import React from 'react'
import { render } from '@testing-library/react'
import RsuMarker from './RsuMarker'
import { replaceChaoticIds } from '../utils/test-utils'

it('snapshot online online', () => {
  const { container } = render(<RsuMarker displayType="online" onlineStatus="online" />)
  expect(replaceChaoticIds(container)).toMatchSnapshot()
})

it('snapshot online unstable', () => {
  const { container } = render(<RsuMarker displayType="online" onlineStatus="unstable" />)
  expect(replaceChaoticIds(container)).toMatchSnapshot()
})

it('snapshot online offline', () => {
  const { container } = render(<RsuMarker displayType="online" onlineStatus="offline" />)
  expect(replaceChaoticIds(container)).toMatchSnapshot()
})

it('snapshot online other', () => {
  const { container } = render(<RsuMarker displayType="online" onlineStatus="other" />)
  expect(replaceChaoticIds(container)).toMatchSnapshot()
})

it('snapshot scms 1', () => {
  const { container } = render(<RsuMarker displayType="scms" scmsStatus="1" />)
  expect(replaceChaoticIds(container)).toMatchSnapshot()
})

it('snapshot scms 0', () => {
  const { container } = render(<RsuMarker displayType="online" scmsStatus="0" />)
  expect(replaceChaoticIds(container)).toMatchSnapshot()
})

it('snapshot scms other', () => {
  const { container } = render(<RsuMarker displayType="online" scmsStatus="other" />)
  expect(replaceChaoticIds(container)).toMatchSnapshot()
})

it('snapshot other', () => {
  const { container } = render(<RsuMarker displayType="other" />)
  expect(replaceChaoticIds(container)).toMatchSnapshot()
})
