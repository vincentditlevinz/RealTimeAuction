import React, {Component} from 'react';
import {Col, Container, Row, Toast, ToastBody, ToastHeader} from 'reactstrap';

export default class ErrorHandler extends Component {
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
    this.setState({visible: false, errorInfo:undefined});
  };

  render() {
    if (this.state.errorInfo) {
     return (
        <div>
          <Container>
            <Row>
              <Col xs="6" sm="4"></Col>
              <Col xs="6" sm="4"></Col>
              <Col sm="4"><Toast color="primary" isOpen={this.state.visible} >
                <ToastHeader toggle={this.onDismiss}>
                  <b>Problem</b>
                </ToastHeader>
                <ToastBody>
                  {this.state.error && this.state.error.toString()}
                </ToastBody>
              </Toast></Col>
            </Row>

          </Container>
        </div>

      );
    }
    // Render children if there's no error
    return this.props.children;
  }
}
