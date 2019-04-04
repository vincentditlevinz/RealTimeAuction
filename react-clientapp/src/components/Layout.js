import React, {Component} from 'react';
import {Container} from 'reactstrap';
import {NavMenu} from './NavMenu';
import {ErrorHandler} from "./ErrorHandler";

export class Layout extends Component {
  static displayName = Layout.name;

  render () {
    return (
      <div>
        <NavMenu />
        <ErrorHandler>
          <Container>
            {this.props.children}
          </Container>
        </ErrorHandler>
      </div>
    );
  }
}
