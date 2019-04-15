import React from 'react';
import ReactDOM from 'react-dom';
import {MemoryRouter} from 'react-router-dom';
import {shallow} from 'enzyme';
import App from './App';


it('Renders app without crashing (shallow test)', () => {
  shallow(<App />);
});

it('Renders app without crashing', () => {
  const div = document.createElement('div');
  ReactDOM.render(
    <MemoryRouter>
      <App />
    </MemoryRouter>, div);
});
