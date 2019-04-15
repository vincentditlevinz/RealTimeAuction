import React from 'react';
import {mount} from 'enzyme';
import ErrorHandler from './ErrorHandler';

const Something = () => null;

function simulateError() {
  const wrapper = mount(
    <ErrorHandler>
      <Something/>
    </ErrorHandler>
  );

  const error = new Error('test');
  wrapper.find(ErrorHandler).simulateError(error);
  return wrapper;
}

describe('ErrorHandler test suit', () => {

  it('Calls componentDidCatch in case of an error', () => {
    const spy = jest.spyOn(ErrorHandler.prototype, 'componentDidCatch');
    simulateError();
    expect(spy).toHaveBeenCalled();
    spy.mockReset();
    spy.mockRestore();

  });

  it('Does not call componentDidCatch if no error', () => {
    const spy = jest.spyOn(ErrorHandler.prototype, 'componentDidCatch');
    mount(
      <ErrorHandler>
        <Something />
      </ErrorHandler>
    );

    expect(spy).not.toHaveBeenCalled();
    spy.mockReset();
    spy.mockRestore();

  });

  it('Should display an ErrorMessage if wrapped component throws', () => {
    const wrapper = simulateError();
    expect(wrapper.find('ToastBody').text()).toEqual('Error: test');
    expect(wrapper.exists('Something')).toBe(false);// Something should not be rendered
  });

  it('Clicking on ToastHeader should dismiss the error message', () => {
    const wrapper = simulateError();
    wrapper.instance().onDismiss();// simulate click on toggle (we expect the ToastHeader component to work as expected)
    wrapper.update();
    expect(wrapper.exists('Toast')).toBe(false);// component Toast disappeared
    expect(wrapper.exists('Something')).toBe(true);// Something should be rendered
  })
});

