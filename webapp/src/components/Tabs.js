import React, { useState } from 'react'
import PropTypes from 'prop-types'
import Tab from './Tab'
import { selectAuthLoginData } from '../generalSlices/userSlice'
import { useSelector } from 'react-redux'

const Tabs = (props) => {
  const { children } = props
  const [activeTab, setActiveTab] = useState(children[0].props.label)
  const authLoginData = useSelector(selectAuthLoginData)

  const onClickTabItem = (tab) => {
    if (authLoginData) {
      setActiveTab(tab)
    }
  }

  return (
    <div className="tabs">
      <ol className="tab-list">
        {children.map((child) => {
          const { label } = child.props
          if (label !== undefined) {
            return <Tab activeTab={activeTab} key={label} label={label} onClick={onClickTabItem} />
          } else {
            return null
          }
        })}
      </ol>
      <div className="tab-content">
        {children.map((child) => {
          if (child.props.label !== activeTab) return undefined
          return child.props.children
        })}
      </div>
    </div>
  )
}

Tabs.propTypes = {
  children: PropTypes.array.isRequired,
}

export default Tabs
