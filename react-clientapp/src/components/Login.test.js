import React from 'react';
import {mount, shallow} from 'enzyme';
import {Login} from "./Login";
import {MemoryRouter, Redirect} from 'react-router-dom';
import {ErrorHandler} from "./ErrorHandler";

function fillLoginForm(usr = 'jdoe', pwd = 'pwd') {
  const wrapper = mount(<Login/>);
  const username = wrapper.find('#username input');
  username.instance().value = usr;
  username.simulate('change');
  const password = wrapper.find('#password input');
  password.instance().value = pwd;
  password.simulate('change');
  return wrapper;
}

function fillLoginFormWithErrorHandling() {
  const wrapper = mount(<ErrorHandler><Login/></ErrorHandler>);
  const username = wrapper.find('#username input');
  username.instance().value = 'jdoe';
  username.simulate('change');
  const password = wrapper.find('#password input');
  password.instance().value = 'pwd';
  password.simulate('change');
  return wrapper;
}

describe('Login test suit', () => {

  it('Test Login component content',  () => {
    const wrapper = shallow(<Login/>);
    expect(wrapper.exists('Form')).toBe(true);
    const formGroup = wrapper.find('FormGroup');
    expect(formGroup).toHaveLength(2);
    expect(formGroup.first().find('Label').prop('for')).toBe('username');
    expect(formGroup.first().find('Label').prop('children')).toBe('User name');
    expect(formGroup.first().find('Input').prop('name')).toBe('username');
    expect(formGroup.first().find('Input').prop('id')).toBe('username');
    expect(formGroup.first().find('Input').prop('type')).toBe('text');
    expect(formGroup.first().find('Input').prop('value')).toBe('');

    expect(formGroup.last().find('Label').prop('for')).toBe('password');
    expect(formGroup.last().find('Label').prop('children')).toBe('Password');
    expect(formGroup.last().find('Input').prop('name')).toBe('password');
    expect(formGroup.last().find('Input').prop('id')).toBe('password');
    expect(formGroup.last().find('Input').prop('type')).toBe('password');
    expect(formGroup.last().find('Input').prop('value')).toBe('');

    const button = wrapper.find('Button');
    expect(button.prop('type')).toBe('submit');
    expect(button.prop('children')).toBe('Submit');
    expect(button.prop('disabled')).toBeTruthy();
  });

  it('Test input binding',  () => {
    const wrapper = fillLoginForm();
    expect(wrapper.state('username')).toEqual('jdoe');
    expect(wrapper.state('password')).toEqual('pwd');

  });

  it('Test login OK',  async (done) => {
    const wrapper = fillLoginForm();

    const fakePromise = Promise.resolve({});
    const spy = jest.spyOn(wrapper.instance(), 'authenticate').mockImplementation(() => {
      return fakePromise;
    });

    const mock = jest.spyOn(wrapper.instance(), 'redirectTo').mockImplementation((from) => {
      console.log("In redirect");
      return <MemoryRouter><Redirect to={from}/></MemoryRouter>;
    });
    wrapper.find('Button').simulate('submit');

    await fakePromise;
    setImmediate(() => {
      try {
        expect(spy).toHaveBeenCalledWith('jdoe','pwd');
        expect(mock).toHaveBeenCalled();
      } catch (e) {
        done.fail(e);
      }
      done();
    });
  });

  it('Test predefined location', () => {
    const wrapper = mount(<Login location={{state: {from: '/testPage'}}}/>);
    expect(wrapper.prop('location').state.from).toEqual("/testPage");
  });

  it('Test login failure',  async(done) => {
    const wrapper = fillLoginFormWithErrorHandling();

    const spy = jest.spyOn(Login.prototype, 'authenticate').mockImplementation(() => Promise.reject(new Error("test")));
    const mock = jest.spyOn(Login.prototype, 'redirectTo').mockImplementation((from) => {
      return <MemoryRouter><Redirect to={from}/></MemoryRouter>;
    });
    const failureSpy = jest.spyOn(Login.prototype, 'onFailure');
    wrapper.find('Button').simulate('submit');

    await spy;
    setImmediate(() => {
      try {
        expect(spy).toHaveBeenCalledWith('jdoe','pwd');
        expect(mock).not.toHaveBeenCalled();
        expect(failureSpy).toHaveBeenCalledWith(new Error("test"));
      } catch (e) {
        done.fail(e);
      }
      done();
    });
  });

  it('Test credential validation',  () => {
    let wrapper = fillLoginForm('');
    expect(wrapper.find('Button').prop('disabled')).toBeTruthy();
    wrapper = fillLoginForm('usr', '');
    expect(wrapper.find('Button').prop('disabled')).toBeTruthy();
    wrapper = fillLoginForm('usr', 'pwd');
    expect(wrapper.find('Button').prop('disabled')).toBeFalsy();
  });

});

