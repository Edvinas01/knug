import ReactDOM from 'react-dom';
import React from 'react';

import {
  BrowserRouter,
  Redirect,
  Switch,
  Route,
} from 'react-router-dom';

import App from './components/app/App';
import Game from './components/game/Game';

ReactDOM.render(
  (
    <BrowserRouter>
      <Switch>
        <Route exact path="/" component={App} />
        <Route path="/game" component={Game} />
        <Redirect from="*" to="/" />
      </Switch>
    </BrowserRouter>
  ), document.getElementById('root'),
);
