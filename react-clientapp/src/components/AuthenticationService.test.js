import React from 'react';
import AuthenticationService from "./AuthenticationService";

const nowPlus1h = new Date(new Date().getTime() + (60*60*1000));


function generateJWT(expDate = new Date('2015-07-01')) {
  const nJwt = require('njwt');
  const secureRandom = require('secure-random');
  const signingKey = secureRandom(256, {type: 'Buffer'}); // Create a highly random byte array of 256 bytes

  const claims = {
    iss: "http://myapp.com/",  // The URL of your service
    sub: "users/user1234",    // The UID of the user in your system
    scope: "self, admins",
    name: "John Doe"
  };

  const jwt = nJwt.create(claims, signingKey);
  jwt.setExpiration(expDate); // A specific date
  return jwt.compact();
}

describe('AuthenticationService test suit', () => {

  beforeEach(() => {
    fetch.resetMocks();
  });

  it('Test login failure',  async (done) => {
    fetch.mockResponseOnce(JSON.stringify({ authenticated: false }));
    const as = new AuthenticationService();
    const spy = jest.spyOn(as, 'setToken');
    as.login('john', 'doe');
    setImmediate(() => {
      try {
        expect(fetch.mock.calls[0][0]).toEqual('/login');
        expect(fetch.mock.calls[0][1].body).toEqual('{"username":"john","password":"doe"}');// a JSon String
        expect(fetch.mock.calls[0][1].headers.Accept).toEqual('application/json');
        expect(fetch.mock.calls[0][1].headers['Content-Type']).toEqual('application/json');
        expect(fetch.mock.calls[0][1].headers.Authorization).toBeUndefined();
        expect(fetch.mock.calls[0][1].method).toEqual('POST');

        expect(spy).not.toHaveBeenCalled();// auth failure
      } catch (e) {
        done.fail(e);
      }
      done();
    });
  });

  it('Test login success',  async (done) => {
    const token = 'gsgdqsdjhgsdjfsdjfgjsdgfjsq';
    fetch.mockResponseOnce(JSON.stringify({ authenticated: true, token: token   }));
    const as = new AuthenticationService();
    const spy = jest.spyOn(as, 'setToken');
    as.login('john', 'doe');
    setImmediate(() => {
      try {
        expect(spy).toHaveBeenCalledWith(token);// auth failure
      } catch (e) {
        done.fail(e);
      }
      done();
    });

  });

  it('Test bearer token is positioned if logged in',  async (done) => {
    const token = 'gsgdqsdjhgsdjfsdjfgjsdgfjsq';
    fetch.mockResponseOnce(JSON.stringify({ data: 'data'}));
    const as = new AuthenticationService();
    const spy = jest.spyOn(as, 'getToken').mockImplementation(() => {
      return token;
    });
    jest.spyOn(as, 'loggedIn').mockImplementation(() => true);
    const result = as.fetch('/test');

    await result;
    setImmediate(() => {
      try {
        expect(spy).toHaveBeenCalled();
        expect(fetch.mock.calls[0][1].headers.Authorization).toEqual('Bearer ' + token);
        expect(fetch.mock.calls[0][1].headers.Accept).toEqual('application/json');
        expect(fetch.mock.calls[0][1].headers['Content-Type']).toEqual('application/json');

      } catch (e) {
        done.fail(e);
      }
      done();
    });

  });

  it('Test fetch reject does not raise exception',  async (done) => {
    fetch.mockReject(new Error('fake error message'));// Might happens
    const as = new AuthenticationService();
    const result = as.fetch('/test');

    let exception;
    try {
      await result;
    } catch (e) {
      exception = e;
    }
    setImmediate(() => {
      try {
        expect(exception).toEqual(new Error('fake error message'));
      } catch (e) {
        done.fail(e);
      }
      done();
    });
  });

  it('Test fetch raise an exception if status 300',  async (done) => {
    fetch.mockResponseOnce(JSON.stringify({ authenticated: true}), { status: 300 });
    const as = new AuthenticationService();
    const result = as.fetch('/test');
    let exception;
    try {
      await result;
    } catch (e) {
      exception = e;
    }
    setImmediate(() => {
      try {
        expect(exception).toEqual(new Error('Multiple Choices'));
      } catch (e) {
        done.fail(e);
      }
      done();
    });
  });

  it('Test fetch raise an exception if status 404',  async (done) => {
    fetch.mockResponseOnce(JSON.stringify({ authenticated: true}), { status: 404 });
    const as = new AuthenticationService();
    const result = as.fetch('/test');
    let exception;
    try {
      await result;
      exception = e;
    } catch (e) {
      exception = e;
    }
    setImmediate(() => {
      try {
        expect(exception).toEqual(new Error('Not Found'));
      } catch (e) {
        done.fail(e);
      }
      done();
    });
  });

  it('Test token expiration',  () => {
    let token = generateJWT();
    const as = new AuthenticationService();
    expect(as.isTokenExpired(token)).toBeTruthy();// not expired for the moment might fail in a few years
    token = generateJWT(nowPlus1h);
    expect(as.isTokenExpired(token)).toBeFalsy();// not expired for the moment might fail in a few years

  });

  it('Test loggedIn',  () => {
    const as = new AuthenticationService();
    jest.spyOn(as, 'getToken').mockImplementation(() => {
      return generateJWT();
    });
    jest.spyOn(as, 'isTokenExpired').mockImplementationOnce(() => false);
    expect(as.getUser().name).toBe('John Doe');
    expect(as.loggedIn()).toBeTruthy();

    jest.spyOn(as, 'isTokenExpired').mockImplementationOnce(() => true);
    expect(as.loggedIn()).toBeFalsy();

    jest.spyOn(as, 'getToken').mockImplementation(() => {
      return undefined;
    });
    expect(as.loggedIn()).toBeFalsy();
  });
});

