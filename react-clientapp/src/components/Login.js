import React, {Component} from "react";
import {Button, Form, FormGroup, Input, Label} from 'reactstrap';
import {Redirect} from 'react-router-dom'
/* We want to import our 'AuthenticationService' component in order to send a login request */
import AuthenticationService from './AuthenticationService';


export class Login extends Component {
  static displayName = Login.name;

  /* In order to utilize our authentication methods within the AuthService class, we want to instantiate a new object */
  Auth = new AuthenticationService();

  state = {
    username: "",
    password: "",
    redirectToReferrer: false
  };

  /* Fired off every time the use enters something into the input fields */
  handleChange = (e) => {
    this.setState(
      {
        [e.target.name]: e.target.value
      }
    )
  };

  validateForm() {
    return this.state.username.length > 0 && this.state.password.length > 0;
  }

  handleFormSubmit = (e) => {
    e.preventDefault();
    /* Here is where all the login logic will go. Upon clicking the login button, we would like to utilize a login method that will send our entered credentials over to the server for verification. Once verified, it should store your token and send you to the protected route. */
    this.Auth.login(this.state.username, this.state.password)
      .then(() => {
        this.setState(() => ({
          redirectToReferrer: true
        }))
      })
      .catch(err => {
        this.setState({err});
      })
  };

  render() {
    if (this.state.err) throw this.state.err;
    const {from} = this.props.location.state || {from: {pathname: '/'}};
    const {redirectToReferrer} = this.state;

    if (redirectToReferrer === true) {
      return <Redirect to={from} />
    }
    return (
      <React.Fragment>
        <Form inline onSubmit={this.handleFormSubmit}>
          <FormGroup className="mb-2 mr-sm-2 mb-sm-0">
            <Label for="exampleEmail" className="mr-sm-2">Email</Label>
            <Input type="text" name="username" id="username" value={this.state.username}
                   onChange={this.handleChange} />
          </FormGroup>
          <FormGroup className="mb-2 mr-sm-2 mb-sm-0">
            <Label for="password" className="mr-sm-2">Password</Label>
            <Input type="password" name="password" id="password" value={this.state.password}
                   onChange={this.handleChange}/>
          </FormGroup>
          <Button disabled={!this.validateForm()} type="submit">Submit</Button>
        </Form>

      </React.Fragment>
    );
  }

}
