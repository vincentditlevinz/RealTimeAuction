import React from 'react';
import {shallow} from 'enzyme';
import {DashboardAuctions} from "./DashboardAuctions";


describe('DashboardAuctions test suit', () => {

  it('Test DashboardAuctions injected content',  () => {
    const wrapper = shallow(<DashboardAuctions></DashboardAuctions>);
    expect(wrapper.exists('OpenAuctions')).toBe(true);
    expect(wrapper.exists('ClosedAuctions')).toBe(true);
    expect(wrapper.find('ErrorHandler')).toHaveLength(2);
  });

});

