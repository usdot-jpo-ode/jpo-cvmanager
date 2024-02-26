import React, { Component } from 'react'
import PropTypes from 'prop-types'
import Tab from './Tab'

interface TabsProps {
  children: {
    props: {
      label: string
      children: React.ReactNode
    }
  }[]
}
interface TabsState {
  activeTab: string
}

class Tabs extends Component<TabsProps, TabsState> {
  static propTypes = {
    children: PropTypes.instanceOf(Array).isRequired,
  }

  constructor(props: TabsProps) {
    super(props)

    this.state = {
      activeTab: props.children[0].props.label,
    }
  }

  onClickTabItem = (tab: string) => {
    this.setState({ activeTab: tab })
  }

  render() {
    const {
      onClickTabItem,
      props: { children },
      state: { activeTab },
    } = this

    return (
      <div className="tabs">
        <ol className="tab-list">
          {children.map((child) => {
            const label = child?.props?.label
            if (label !== undefined) {
              return <Tab activeTab={activeTab} key={label} label={label} onClick={onClickTabItem} />
            } else {
              return null
            }
          })}
        </ol>
        <div className="tab-content">
          {children.map((child) => {
            if (child?.props?.label !== activeTab) return undefined
            return child.props.children
          })}
        </div>
      </div>
    )
  }
}

export default Tabs
