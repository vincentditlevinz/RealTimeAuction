import React from 'react';
import {NavMenu} from './NavMenu';
import {shallow} from 'enzyme';
import {Layout} from "./Layout";

const Something = () => null;
const SomethingElse = () => null;


describe('Layout test suit', () => {

  it('Test Layout injected content',  () => {
    const wrapper = shallow(<Layout><Something/><SomethingElse/></Layout>);
    expect(wrapper.exists('NavMenu')).toBe(true);
    expect(wrapper.exists('Something')).toBe(true);
    expect(wrapper.exists('SomethingElse')).toBe(true);
  });

});

