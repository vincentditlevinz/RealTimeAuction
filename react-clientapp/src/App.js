import React, { Component } from 'react';
import { Button,} from 'reactstrap';
import {
  Route,
  Redirect,
  withRouter
} from 'react-router-dom'
import { Layout } from './components/Layout';
import { Home } from './components/Home';
import { FetchData } from './components/FetchData';
import { Counter } from './components/Counter';
import { Login } from './components/Login';
import AuthenticationService from "./components/AuthenticationService";

const Auth = new AuthenticationService();

const PrivateRoute = ({ component: Component, ...rest }) => (
  <Route {...rest} render={(props) => (
    Auth.loggedIn() === true
      ? <Component {...props} />
      : <Redirect to={{
        pathname: '/login',
        state: { from: props.location }
      }} />
  )} />
)

const AuthButton = withRouter(({ history }) => (
  Auth.loggedIn() ? (
    <p>
      <b>Welcome {Auth.getUser().sub} ! &nbsp;</b><Button onClick={() => {
      Auth.logout(() => history.push('/'))
    }}>Sign out</Button>
    </p>
  ) : (
    <p><b>You are not logged in.</b></p>
  )
))

class App extends Component {

  render () {
    return (
      <Layout>
        <AuthButton/>
        <Route exact path='/' component={Home} />
        <Route path='/counter' component={Counter} />
        <Route path="/login" component={Login}/>
        <PrivateRoute path='/fetch-data' component={FetchData} />
      </Layout>
    );
  }
}

export default App;
