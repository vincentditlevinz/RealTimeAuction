import React, {Component} from 'react';
import {Button, Table} from 'reactstrap';
import AuthenticationService from './AuthenticationService';
import EventBus from 'vertx3-eventbus-client';
import Rx from 'rx';

const url = window.location.protocol + '//' + window.location.hostname + ':' + 7878 + '/eventbus';
console.log('SockJS listening on:' +  url);


export class OpenAuctions extends Component {
  static displayName = OpenAuctions.name;
  Auth = new AuthenticationService();
  eb;

  constructor(props) {
    super(props);
    this.state = {auctions: [], loading: true};
  }


  componentDidMount() {
    this.registerEventBus(url);
    this.loadData();
  }

  /**
   * Load open auctions from REST API
   */
  loadData() {
    try {
      this.Auth.fetch('api/auctions?closed=false&offset=0&max=20')
        .then(response => response)
        .then(data => {
          this.setState({auctions: data, loading: false});
        })
    } catch (err) {
      this.setState({err, auctions: [], loading: false});
    }
  }

  /**
   * Register the event bus for listening to bids from the server
   */
  registerEventBus(url) {
    this.eb = new EventBus(url);
    this.eb.onopen = () => {
      this.eb.enableReconnect(true);
      Rx.Observable.create((observer) => {
        try {
          this.eb.registerHandler("bids", (err, msg) => {
            observer.next(msg.body);
          })
        } catch (err) {
          observer.error(err);
        }
      }).subscribe(message => {
        const auction = JSON.parse(message);
        const data = this.state.auctions.map(item => {
          return (item.id === auction.id) ? auction : item;
        });
        this.setState({auctions: data});
      }, err => {
        this.setState({err})
      })
    };
  }

  /**
   * perform a bid for the provided auction
   * @param auction provided
   */
  bid = (auction) => {
    const price = auction.price * 1.1;
    const body = JSON.stringify({
      price
    });

    this.Auth.fetch('api/bid/' + auction.id, {
      method: 'PATCH', body: body
    })
      .then(response => {
        response;
      })
      .catch((err) => this.setState({err}))
  };


  /**
   * Render the open auctions table
   * @param auctions
   * @returns {*}
   */
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
          <th>Send an offer</th>
        </tr>
        </thead>
        <tbody>
        {auctions.map(auction =>
          <tr key={auction.id}>
            <td>{auction.product}</td>
            <td>{auction.price}</td>
            <td>{auction.buyer}</td>
            <td>{auction.ending}</td>
            <td><Button name="Bid" onClick={() => this.bid(auction)}>Bid 10% more</Button></td>
          </tr>
        )}
        </tbody>
      </Table>
    );
  }

  /**
   * Renders the component
   * @returns {*}
   */
  render() {
    let contents = this.state.loading
      ? <p><em>Loading...</em></p>
      : this.renderAuctionsTable(this.state.auctions);

    return (
      <div>
        <h1>Auctions open for sale</h1>
        {contents}
      </div>
    );
  }
}
