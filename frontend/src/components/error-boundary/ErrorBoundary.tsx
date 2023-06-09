import React, { PropsWithChildren } from "react"

interface IHasErrorState {
  hasError: boolean
}

export default class ErrorBoundary extends React.Component<PropsWithChildren, IHasErrorState> {

  constructor(props: PropsWithChildren) {
    super(props)
    this.state = { hasError: false }
  }

  static getDerivedStateFromError(error: any) {
    return { hasError: true }
  }

  render() {
    if (this.state.hasError) {
      return <h1>ErrorBoundary: an error occurred</h1>
    } else {
      return this.props.children
    }
  }
}