import React, {Component} from 'react';
import {OpenAuctions} from "./OpenAuctions";
import {ClosedAuctions} from "./ClosedAuctions";

export class DashboardAuctions extends Component {
  static displayName = DashboardAuctions.name;

  render() {
    return (
      <div>
        <OpenAuctions/>
        <br/>
        <ClosedAuctions/>
      </div>
    );
  }
}
