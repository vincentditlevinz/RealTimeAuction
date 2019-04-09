import React, {Component} from 'react';
import {OpenAuctions} from "./OpenAuctions";
import {ClosedAuctions} from "./ClosedAuctions";
import {ErrorHandler} from "./ErrorHandler";

export class DashboardAuctions extends Component {
  static displayName = DashboardAuctions.name;

  render() {
    return (
      <div>
        <ErrorHandler>
          <OpenAuctions/>
        </ErrorHandler>
        <br/>
        <ErrorHandler>
          <ClosedAuctions/>
        </ErrorHandler>
      </div>
    );
  }
}
