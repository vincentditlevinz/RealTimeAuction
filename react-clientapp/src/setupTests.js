import {configure} from 'enzyme';
import Adapter from 'enzyme-adapter-react-16';

import jest_fetch_mock from "jest-fetch-mock";

configure({ adapter: new Adapter() });

global.localStorage = {
  getItem: jest.fn(),
  setItem: jest.fn(),
  clear: jest.fn()
};

global.fetch = jest_fetch_mock;

