import decode from 'jwt-decode';

export default class AuthenticationService {
    
    // Initializing important variables

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


    loggedIn = () => {
        // Checks if there is a saved token and it's still valid
      const token = this.getToken(); // Getting token from localstorage
        return !!token && !this.isTokenExpired(token)
    };

    isTokenExpired = (token) => {
        try {
            const decoded = decode(token);
            return decoded.exp < Date.now() / 1000;
        }
        catch (err) {
            return false;
        }
    };

    setToken = (idToken) => {
        // Saves user token to localStorage
        localStorage.setItem('id_token', idToken)
    };

    getToken = () => {
        // Retrieves the user token from localStorage
        return localStorage.getItem('id_token')
    };

    logout = (redirect) => {
        // Clear user token and profile data from localStorage
        localStorage.removeItem('id_token');
        redirect();
    };

    getUser = () => {
        // Using jwt-decode npm package to decode the token
      return decode(this.getToken());
    };

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
    };

    _checkStatus = (response) => {
        // raises an error in case response status is not a success
        if (response.status >= 200 && response.status < 300) { // Success status lies between 200 to 300
            return response.json()
        } else {
          const error = new Error(response.status);
          error.response = response;
            throw error
        }
    }
}
