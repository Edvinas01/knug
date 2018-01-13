import { State, Point, Physics, Camera } from 'phaser';

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
const addPlayer = (game, fixture) => {
  const player = game.add.sprite(
    fixture.position.x,
    fixture.position.y,
    'borker',
  );

  const name = game.add.text(0, 0, fixture.name, {
    font: '24px Arial',
    fill: '#ffffff',
    align: 'center',
  });

  name.setShadow(0, 0, 'rgba(0,0,0,1)', 5);
  name.anchor.set(0.5);

  player.width = fixture.size.width;
  player.height = fixture.size.height;
  player.anchor = new Point(0.5, 0.5);
  player.userData = {
    name,
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

    this.game.physics.startSystem(Physics.P2JS);

    // Setup players.
    this.players = players.map((playerState) => {
      const player = addPlayer(this.game, playerState);
      this.game.physics.p2.enable(player);

      if (playerState.controlled) {
        this.game.camera.follow(player, Camera.FOLLOW_LOCKON, 0.1, 0.1);
        this.player = player;
      }
      return player;
    });

    this.players.forEach(player => player.userData.name.bringToTop());

    // Setup static polygons.
    createPolygonMask(this.game, entities.polygons);

    // Setup controls.
    this.controls = {
      cursors: this.game.input.keyboard.createCursorKeys(),
    };
  }

  update() {
    const { cursors } = this.controls;
    const { body } = this.player;

    // Make names follow players.
    this.players.forEach((player) => {
      const { name } = player.userData;
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

    if (cursors.up.isDown) {
      body.moveUp(300);
    }
    if (cursors.down.isDown) {
      body.moveDown(300);
    }
    if (cursors.left.isDown) {
      body.moveLeft(300);
    }
    if (cursors.right.isDown) {
      body.moveRight(300);
    }
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
      const { players } = message;
      console.log(players);
    }
  }
}

export default PlayState;
