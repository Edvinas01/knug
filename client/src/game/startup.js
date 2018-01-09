const dummyWorld = {
  size: {
    width: 2000,
    height: 2000,
  },
  entities: {
    players: [{
      controlled: true,
      name: 'James',
      position: {
        x: 1000,
        y: 1000,
      },
      size: {
        width: 64,
        height: 64,
      },
    }, {
      name: 'Jeff',
      position: {
        x: 500,
        y: 500,
      },
      size: {
        width: 128,
        height: 128,
      },
    }],
    polygons: [{
      points: [
        { x: 100, y: 100 },
        { x: 300, y: 100 },
        { x: 200, y: 200 },
        { x: 300, y: 300 },
        { x: 100, y: 300 },
      ],
    }],
  },
};

const useDummyWorld = true;
const socketUrl = `ws://${process.env.REACT_APP_BACKEND_HOST}/game?username=CoolGuy`;

/**
 * Setup and start the game.
 *
 * @param canvasWrapper wrapper element which will hold the game canvas.
 */
const startGame = (canvasWrapper) => {
  /* eslint-disable import/no-extraneous-dependencies, global-require, import/no-unresolved */
  window.PIXI = require('pixi.js');
  window.p2 = require('p2');

  const Phaser = require('phaser');
  const PlayState = require('./core/PlayState').default;
  /* eslint-enable import/no-extraneous-dependencies, global-require, import/no-unresolved */

  const webSocket = new WebSocket(socketUrl);

  let playState = null;

  webSocket.onmessage = (payload) => {
    const data = JSON.parse(payload.data);

    if (playState === null && (data.type === 'setup' || useDummyWorld)) {
      playState = new PlayState({
        worldFixture: useDummyWorld
          ? dummyWorld
          : data,
        webSocket,
      });

      const game = new Phaser.Game('100', '100', Phaser.AUTO, canvasWrapper);
      game.state.add('Play', playState, true);
    } else if (!playState) {
      // No point in spamming errors if engine failed to setup.
      webSocket.close(1000, 'Cannot handle messages while engine is not initialized');
    } else {
      PlayState.handleMessage(data);
    }
  };

  webSocket.onclose = (event) => {
    const { code, reason } = event;
    console.log(`Connection to game server was closed, code: ${code}, reason: "${reason}"`);
  };
};

export default startGame;
