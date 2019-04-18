import React from 'react';
import {ClosedAuctions} from './ClosedAuctions';
import {ErrorHandler} from './ErrorHandler';
import {shallow, mount} from 'enzyme';


describe('ClosedAuctions test suit', () => {

  it('Show table after loading async data with fetch function', async (done) => {
    const spy0 = jest.spyOn(ClosedAuctions.prototype, 'componentDidMount');
    const fakePromise = Promise.resolve([{
      id: '12345',
      product: 'My super product',
      price: 120.00,
      buyer: 'John Doe',
      ending: '2019-04-17T07:56:40.432Z'
    },
      {
        id: '67890',
        product: 'My other product',
        price: 100.01,
        buyer: 'Jane Doe',
        ending: '2019-04-17T07:56:40.432Z'
      }]);


    const spy = jest.spyOn(ClosedAuctions.prototype, 'fetch').mockImplementation(() => {
      return fakePromise;
    });
    const wrapper = shallow(<ClosedAuctions/>);
    expect(spy0).toHaveBeenCalled();
    expect(spy).toHaveBeenCalled();
    await fakePromise;
    setImmediate(() => {
      try {
        wrapper.update();
        const table = wrapper.find('Table');
        expect(table).toHaveLength(1);
        const rows = wrapper.find('tbody tr');
        expect(rows).toHaveLength(2);

        const firstRowColumns = rows.first().find('td').map(column => column.text());
        expect(firstRowColumns).toHaveLength(4);
        expect(firstRowColumns[0]).toBe('My super product');
        expect(firstRowColumns[1]).toBe("120");// because of text()
        expect(firstRowColumns[2]).toBe('John Doe');
        expect(firstRowColumns[3]).toBe('2019-04-17T07:56:40.432Z');

        const secondRowColumns = rows.last().find('td').map(column => column.text());
        expect(firstRowColumns).toHaveLength(4);
        expect(secondRowColumns[0]).toBe('My other product');
        expect(secondRowColumns[1]).toBe("100.01");// because of text()
        expect(secondRowColumns[2]).toBe('Jane Doe');
        expect(secondRowColumns[3]).toBe('2019-04-17T07:56:40.432Z');

      } catch (e) {
        done.fail(e);
      }
      done();
    });


  });

  it('Show error message in case of pb',  () => {
    expect.assertions(3);
    const spy = jest.spyOn(ClosedAuctions.prototype, 'fetch').mockImplementation(() => {
      throw new Error('test');
    });
    expect(spy).toThrow(new Error('test'));
    const wrapper = mount(<ErrorHandler><ClosedAuctions/></ErrorHandler>);
    expect(wrapper.find('ToastBody').text()).toEqual('Error: test');
    expect(wrapper.exists('Something')).toBe(false);// Something should not be rendered
  });
});

