import React, {Component} from 'react';
import {Table} from 'reactstrap';
import AuthenticationService from './AuthenticationService';


export class ClosedAuctions extends Component {
  static displayName = ClosedAuctions.name;
  Auth = new AuthenticationService();


  constructor(props) {
    super(props);
    this.state = {auctions: [], loading: true};
    try {
      this.Auth.fetch('api/auctions?closed=true&offset=0&max=20')
        .then(response => response)
        .then(data => {
          this.setState({auctions: data, loading: false});
        })
    } catch (err) {
      this.state = {err, auctions: [], loading: false};
    }
  }


  renderAuctionsTable(auctions) {
    if (this.state.err) throw this.state.err;
    return (
      <Table className='table table-striped'>
        <thead>
        <tr>
          <th>Product</th>
          <th>Price (€)</th>
          <th>Buyer</th>
          <th>End</th>
        </tr>
        </thead>
        <tbody>
        {auctions.map(auction =>
          <tr key={auction.id}>
            <td>{auction.product}</td>
            <td>{auction.price}</td>
            <td>{auction.buyer}</td>
            <td>{auction.ending}</td>
          </tr>
        )}
        </tbody>
      </Table>
    );
  }

  render() {
    let contents = this.state.loading
      ? <p><em>Loading...</em></p>
      : this.renderAuctionsTable(this.state.auctions);

    return (
      <div>
        <h1>Auctions closed</h1>
        {contents}
      </div>
    );
  }
}
