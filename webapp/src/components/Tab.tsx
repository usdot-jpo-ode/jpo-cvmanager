import React from 'react'
import PropTypes from 'prop-types'

interface TabProps {
  activeTab: string
  label: string

  onClick: (label: string) => void
}

const Tab = (props: TabProps) => {
  const { onClick, activeTab, label } = props
  let className = 'tab-list-item'

  if (activeTab === label) {
    className += ' tab-list-active'
  }

  return (
    <li
      className={className}
      tabIndex={0}
      onKeyDown={(e) => {
        if (e.code === 'Space') {
          onClick(label)
        }
      }}
      onClick={() => onClick(label)}
    >
      {label}
    </li>
  )
}

Tab.propTypes = {
  activeTab: PropTypes.string.isRequired,
  label: PropTypes.string.isRequired,
  onClick: PropTypes.func.isRequired,
}

export default Tab
