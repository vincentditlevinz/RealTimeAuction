import React from 'react';
import {shallow} from 'enzyme';
import {Login} from "./Login";


describe('Login test suit', () => {

  it('Test Login content',  () => {
    const wrapper = shallow(<Login location={'/'} />);// location attribute is
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

});

