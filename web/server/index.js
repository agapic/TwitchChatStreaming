/* eslint no-unused-vars:0 */
/* eslint new-cap:0 */
/* eslint consistent-return:0 */
const express = require('express');
const logger = require('./logger');

const argv = require('minimist')(process.argv.slice(2));
const setup = require('./middlewares/frontendMiddleware');
// const isDev = process.env.NODE_ENV !== 'production';
// const ngrok = (isDev && process.env.ENABLE_TUNNEL) || argv.tunnel ? require('ngrok') : false;
const resolve = require('path').resolve;
const app = express();
const http = require('http').Server(app);
let io = require('socket.io')(http);
const port = argv.port || process.env.PORT || 3002;
// If you need a backend, e.g. an API, add your custom backend-specific middleware here
// app.use('/api', myApi);
const redis = require('redis');
const redisClient = redis.createClient();
// In production we need to pass these values in instead of relying on webpack
setup(app, {
  outputPath: resolve(process.cwd(), 'build'),
  publicPath: '/',
});

function channelsByMessageCount(socket) {
  const init = [];

  redisClient.zrevrange('messageCounts', 0, -1, 'withscores', function (err, keys) {
    let remaining = keys.length;
    for (let i = 0; i < keys.length; i += 2) {
      const splitKeys = keys[i].split(':');
      const gameId = splitKeys[2];
      const channelName = splitKeys[1];
      /* eslint no-loop-func:0 */
      (function (k) {
        redisClient.hget(`game:${gameId}`, 'name', function (error, key) {
          if (error) {
            logger.error(`Failed to get game: ${gameId}`);
          }

          init.push({ name: channelName, game: key.replace('+', ' '), message_count: keys[k + 1] });
          remaining -= 2;

          if (remaining === 0) {
            socket.emit('channelCounts', init);
          }
        });
      }(i));
    }
  });
}

function wordCounts(socket) {
  const init = [];

  redisClient.zrevrange('wordCounts', 0, -1, 'withscores', function (err, keys) {
    for (let i = 0; i < keys.length; i += 2) {
      const word = keys[i].split(/:(.+)/)[1];
      init.push({ word, count: keys[i + 1] });
    }

    socket.emit('wordCounts', init);
  });
}

io = io.on('connection', function (socket) {
  console.log('a user connected');
  channelsByMessageCount(socket);
  wordCounts(socket);
  const subscribe = redis.createClient();
  subscribe.subscribe('channelCounts', 'wordCounts');

  subscribe.on('message', function (pubChannel) {
    if (pubChannel === 'channelCounts') {
      channelsByMessageCount(socket);
    }

    if (pubChannel === 'wordCounts') {
      wordCounts(socket);
    }
  });

  socket.on('message', function (msg) {
    console.log('Message', msg);
  });

  socket.on('disconnect', function () {
    console.log('user disconnected');
    subscribe.quit();
  });
});

http.listen(port, function (err) {
  if (err) {
    return logger.error(err.message);
  }

  // Connect to ngrok in dev mode
  // if (ngrok) {
  //   ngrok.connect(port, (innerErr, url) => {
  //     if (innerErr) {
  //       return logger.error(innerErr);
  //     }

  //     logger.appStarted(port, url);
  //   });
  // } else {
  //   logger.appStarted(port);
  // }
});

