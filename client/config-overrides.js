const path = require('path');

const phaserModule = path.join(__dirname, '/node_modules/phaser/');

const phaser = path.join(phaserModule, 'build/custom/phaser-split.js');
const pixi = path.join(phaserModule, 'build/custom/pixi.js');
const p2 = path.join(phaserModule, 'build/custom/p2.js');

module.exports = function override(config) {
  config.module.loaders = [{
    test: /pixi.js/,
    loader: 'script'
  }];

  config.resolve.alias = {
    'phaser': phaser,
    'pixi.js': pixi,
    'p2': p2,
  };

  return config;
};