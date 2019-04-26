import decode from 'jwt-decode';

export default class AuthenticationService {

  // Initializing important variables

  /**
   * Log a user in
   * @param username
   * @param password
   * @returns {*}
   */
  login = (username, password) => {

    // Get a token from api server using the fetch api
    return this.fetch(`/login`, {
      method: 'POST',
      body: JSON.stringify({
        username,
        password
      })
    }).then(res => {
      if(res.authenticated === true) {
        this.setToken(res.token) // Setting the token in localStorage
      } else {
        console.log("Authentication failure")
      }
      return Promise.resolve(res);
    })
  };


  /**
   * Is the user logged in
   * @returns {boolean}
   */
  loggedIn = () => {
    // Checks if there is a saved token and it's still valid
    const token = this.getToken(); // Getting token from localstorage
    return !!token && !this.isTokenExpired(token)
  };

  /**
   * Is token expired ?
   * @param token the token
   * @returns {boolean}
   */
  isTokenExpired = (token) => {
    try {
      const decoded = decode(token);
      return decoded.exp < Date.now() / 1000;
    }
    catch (err) {
      return false;
    }
  };

  /**
   * Store the token in the local storage
   * @param idToken
   */
  setToken = (idToken) => {
    // Saves user token to localStorage
    localStorage.setItem('id_token', idToken)
  };

  /**
   * Retrieves the token from the local storage
   * @returns {string}
   */
  getToken = () => {
    // Retrieves the user token from localStorage
    return localStorage.getItem('id_token')
  };

  /**
   * Log out the user
   * @param redirect
   */
  logout = (redirect) => {
    // Clear user token and profile data from localStorage
    localStorage.removeItem('id_token');
    redirect();
  };

  /**
   * Retrieves the user from the token
   * @returns {any}
   */
  getUser = () => {
    // Using jwt-decode npm package to decode the token
    return decode(this.getToken());
  };

  /**
   * Decor the calls to API to secure it.
   * @param url
   * @param options
   * @returns {Promise<any>}
   */
  fetch = (url, options) => {
    // performs api calls sending the required authentication headers
    const headers = {
      'Accept': 'application/json',
      'Content-Type': 'application/json'
    };
    // Setting Authorization header
    // Authorization: Bearer xxxxxxx.xxxxxxxx.xxxxxx
    if (this.loggedIn()) {
      headers['Authorization'] = 'Bearer ' + this.getToken()
    }

    return fetch(url, {
      headers,
      ...options
    })
      .then(this._checkStatus)
      .then(response => response)
      .catch((error)  => {
        console.error('Pb with fetch function: ' + error.message);
        throw error;
      });
  };

  /**
   * Handle exceptions
   * @param response
   * @returns {*|Promise<any>}
   * @private
   */
  _checkStatus = (response) => {
    // raises an error in case response status is not a success
    if (response.ok && response.status >= 200 && response.status < 300) { // Success status lies between 200 to 300
      return response.json()
    } else {
      const error = new Error(response.statusText);
      error.response = response;
      throw error
    }
  }
}
