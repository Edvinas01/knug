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
 * Main game state.
 */
class PlayState extends State {
  constructor(props) {
    super();

    this.worldFixture = props.worldFixture;
    this.webSocket = props.webSocket;
  }

  preload() {
    this.game.load.image('background', 'assets/images/grid.png');
    this.game.load.image('borker', 'assets/images/borker.png');
    this.game.load.image('player', 'assets/images/arrow.png');
    this.game.load.image('paper', 'assets/images/paper.png');
  }

  create() {
    const { entities, size: worldSize } = this.worldFixture;
    const { player: playerFixture } = entities;

    // Setup world.
    this.game.world.setBounds(
      0,
      0,
      worldSize.width,
      worldSize.height,
    );

    this.game.add.tileSprite(
      0,
      0,
      worldSize.width,
      worldSize.height,
      'background',
    );

    this.game.physics.startSystem(Physics.P2JS);

    // Setup player.
    const player = this.game.add.sprite(
      playerFixture.position.x,
      playerFixture.position.y,
      'borker',
    );

    player.width = playerFixture.size.width;
    player.height = playerFixture.size.height;
    player.anchor = new Point(0.5, 0.5);

    this.game.physics.p2.enable(player);
    this.game.camera.follow(player, Camera.FOLLOW_LOCKON, 0.1, 0.1);

    this.player = player;

    // Setup other world entities.
    createPolygonMask(this.game, entities.polygons);

    // Setup controls.
    this.controls = {
      cursors: this.game.input.keyboard.createCursorKeys(),
    };
  }

  update() {
    const { cursors } = this.controls;
    const { body } = this.player;

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
  static handleMessage(message) {
    if (message.type === 'debug') {
      console.log(message);
    }
  }
}

export default PlayState;
