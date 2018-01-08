import { State, Point, Physics, Camera } from 'phaser';

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
