import React, {Component} from 'react';
import {Button,} from 'reactstrap';
import {Redirect, Route, withRouter} from 'react-router-dom'
import {Layout} from './components/Layout';
import {Home} from './components/Home';
import {Login} from './components/Login';
import AuthenticationService from "./components/AuthenticationService";
import {DashboardAuctions} from "./components/DashboardAuctions";

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
        <Route path="/login" component={Login}/>
        <PrivateRoute path='/auctions' component={DashboardAuctions}/>
      </Layout>
    );
  }
}

export default App;
