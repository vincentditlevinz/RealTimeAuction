import React from 'react';
import {NavMenu} from './NavMenu';
import {shallow, mount} from 'enzyme';
import {NavLink} from "reactstrap";
import {Home} from "./Home";
import {Route} from "react-router-dom";
import { MemoryRouter } from 'react-router-dom';

const Something = () => null;
const SomethingElse = () => null;


describe('NavMenu test suit', () => {

  it('Test NavMenu default content',  () => {
    const wrapper = shallow(<NavMenu/>);
    expect(wrapper.exists('Navbar')).toBe(true);
    expect(wrapper.exists('NavbarBrand')).toBe(true);
    expect(wrapper.exists('NavbarToggler')).toBe(true);
    expect(wrapper.exists('Collapse')).toBe(true);
    expect(wrapper.find('NavItem')).toHaveLength(2);
    const navLinks = wrapper.find(NavLink);
    expect(navLinks).toHaveLength(2);
    expect(navLinks.first().prop('to')).toEqual('/');
    expect(navLinks.first().prop('children')).toEqual('Home');
    expect(navLinks.last().prop('to')).toEqual('/auctions');
    expect(navLinks.last().prop('children')).toEqual('Auctions');

  });

  it('Test NavMenu routing',  () => {
    const wrapper = mount(<MemoryRouter><div><NavMenu/><Route exact path='/' component={Something} /><Route exact path='/auctions' component={SomethingElse} /></div></MemoryRouter>);
    const navLinks = wrapper.find(NavLink);
    expect(navLinks.first().html()).toEqual('<a class="text-dark nav-link" href="/">Home</a>');
    expect(navLinks.last().html()).toEqual('<a class="text-dark nav-link" href="/auctions">Auctions</a>');
  });

  it('Test NavMenu toggle action',  () => {
    const wrapper = shallow(<NavMenu/>);
    expect(wrapper.state('collapsed')).toBeTruthy();
    wrapper.find('NavbarToggler').simulate('click');
    expect(wrapper.state('collapsed')).toBeFalsy();
    wrapper.find('NavbarToggler').simulate('click');
    expect(wrapper.state('collapsed')).toBeTruthy();
  });
});

