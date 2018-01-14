import { State, Point, Camera, Math } from 'phaser';

/**
 * Create a mask for provided array of polygons.
 */
const createPolygonMask = (game, polygons) => {
  const outline = game.add.graphics(0, 0);
  const mask = game.add.graphics(0, 0);

  outline.lineStyle(4, 0x000000);
  mask.beginFill(0xffffff);

  polygons.forEach((polygon) => {
    const firstPoint = polygon.points[0];
    const points = [...polygon.points, ...[firstPoint]].map(point => new Point(
      point.x,
      point.y,
    ));

    outline.drawPolygon(points);
    mask.drawPolygon(points);

    const sprite = game.add.tileSprite(
      0,
      0,
      game.world.width,
      game.world.height,
      'paper',
    );

    sprite.mask = mask;
  });

  outline.endFill();
  mask.endFill();
};

/**
 * Add a new player with a sprite.
 */
const addPlayer = (game, state) => {
  const { position } = state;
  const player = game.add.sprite(
    position.x,
    position.y,
    'borker',
  );

  const name = game.add.text(0, 0, state.name, {
    font: '24px Arial',
    fill: '#ffffff',
    align: 'center',
  });

  name.setShadow(0, 0, 'rgba(0,0,0,1)', 5);
  name.anchor.set(0.5);

  player.width = state.size.width;
  player.height = state.size.height;
  player.anchor.setTo(0.5, 0.5);
  player.userData = {
    name,
    id: state.id,
    desiredPos: {
      x: position.x,
      y: position.y,
    },
    desiredRot: 0,
  };

  return player;
};

/**
 * Main game state.
 */
class PlayState extends State {
  constructor(props) {
    super();

    this.initialState = props.initialState;
    this.webSocket = props.webSocket;
    this.players = {};
  }

  preload() {
    this.game.load.image('background', 'assets/images/grid.png');
    this.game.load.image('borker', 'assets/images/borker.png');
    this.game.load.image('player', 'assets/images/arrow.png');
    this.game.load.image('paper', 'assets/images/paper.png');
  }

  create() {
    const { entities, world } = this.initialState;
    const { players } = entities;

    // Setup world.
    this.game.world.setBounds(
      0,
      0,
      world.size.width,
      world.size.height,
    );

    this.game.add.tileSprite(
      0,
      0,
      world.size.width,
      world.size.height,
      'background',
    );

    // Setup players.
    this.players = players.reduce((res, state) => {
      const player = addPlayer(this.game, state);

      if (state.controlled) {
        this.game.camera.follow(player, Camera.FOLLOW_LOCKON, 0.1, 0.1);
        this.player = player;
      }

      res[state.id] = player;
      return res;
    }, {});

    Object.keys(this.players).forEach((id) => {
      this.players[id].userData.name.bringToTop();
    });

    // Setup static polygons.
    createPolygonMask(this.game, entities.polygons);

    // Setup controls.
    this.controls = {
      cursors: this.game.input.keyboard.createCursorKeys(),
    };
  }

  update() {
    const { cursors } = this.controls;

    Object.keys(this.players).forEach((id) => {
      const player = this.players[id];
      const { desiredPos, desiredRot, name } = player.userData;

      // Sync with server state.
      // https://gafferongames.com/post/snapshot_interpolation/
      player.rotation = Math.rotateToAngle(player.rotation, desiredRot, 0.5);
      player.x = Math.linearInterpolation([player.x, desiredPos.x], 0.5);
      player.y = Math.linearInterpolation([player.y, desiredPos.y], 0.5);

      // Make names follow players.
      name.x = player.x;
      name.y = player.y - name.height - (player.height / 2);
    });

    this.webSocket.send(JSON.stringify({
      type: 'input',
      up: cursors.up.isDown,
      down: cursors.down.isDown,
      left: cursors.left.isDown,
      right: cursors.right.isDown,
    }));
  }

  render() {
    this.game.debug.pointer(this.game.input.activePointer);
  }

  /**
   * Handle parsed event message.
   */
  handleMessage(message) {
    if (message.type === 'debug') {
      console.log(message);
    } else if (message.type === 'state') {
      message.players.forEach((remote) => {
        const existing = this.players[remote.id];
        if (existing) {
          const { rotation, position } = remote;
          existing.userData = {
            ...existing.userData,
            desiredPos: {
              x: position.x,
              y: this.game.world.height - position.y,
            },
            desiredRot: -rotation,
          };
        }
      });
    } else if (message.type === 'connect') {
      this.players[message.id] = addPlayer(this.game, message);
    } else if (message.type === 'disconnect') {
      const connected = this.players[message.id];
      if (connected) {
        connected.destroy();
        connected.userData.name.destroy();
        delete this.players[message.id];
      }
    }
  }
}

export default PlayState;
