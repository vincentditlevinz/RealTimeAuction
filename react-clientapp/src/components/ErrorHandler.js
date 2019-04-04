import React, {Component} from 'react';
import {Alert} from 'reactstrap';

export class ErrorHandler extends Component {
  constructor(props) {
    super(props);
    this.state = {error: null, errorInfo: null, visible: false};
  }

  componentDidCatch(error, errorInfo) {
    this.setState({
      error: error,
      errorInfo: errorInfo,
      visible: true
    });
  }

  onDismiss = () => {
    this.setState({visible: false});
  };

  render() {
    if (this.state.errorInfo) {
      return (
        <div>
          <Alert color="danger" isOpen={this.state.visible} toggle={this.onDismiss}>
            {this.state.error && this.state.error.toString()}
          </Alert>
        </div>
      );
    }
    // Render children if there's no error
    return this.props.children;
  }
}
