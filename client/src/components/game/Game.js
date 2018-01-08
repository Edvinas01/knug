import React, { Component } from 'react';

import startGame from '../../game/startup';

class Game extends Component {
  constructor(props) {
    super(props);

    this.canvasWrapper = null;
  }

  componentDidMount() {
    startGame(this.canvasWrapper);
  }

  render() {
    return (
      <div ref={(element) => { this.canvasWrapper = element; }} />
    );
  }
}

export default Game;
