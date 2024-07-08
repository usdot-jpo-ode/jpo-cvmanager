import React from 'react'
import PropTypes from 'prop-types'
import { Link } from 'react-router-dom'
import { useLocation } from 'react-router-dom'

interface TabProps {
  activeTab: string
  label: string
  path: string

  onClick: (label: string) => void
}

const Tab = (props: TabProps) => {
  const { onClick, path, label } = props
  const location = useLocation()

  let className = 'tab-list-item'

  if (location.pathname.includes(path)) {
    className += ' tab-list-active'
  }

  return (
    <Link
      className={className}
      to={path}
      onKeyDown={(e) => {
        if (e.code === 'Space') {
          onClick(label)
        }
      }}
      onClick={() => onClick(label)}
    >
      {label}
    </Link>
  )
}

Tab.propTypes = {
  activeTab: PropTypes.string.isRequired,
  label: PropTypes.string.isRequired,
  onClick: PropTypes.func.isRequired,
}

export default Tab
