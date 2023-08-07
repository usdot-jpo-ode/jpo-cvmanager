import React from 'react'
import { render } from '@testing-library/react'
import ContactSupportMenu from './ContactSupportMenu'
import { replaceChaoticIds } from '../utils/test-utils'

it('should take a snapshot', () => {
    const { container } = render(<ContactSupportMenu />)
    
    expect(replaceChaoticIds(container)).toMatchSnapshot()
})