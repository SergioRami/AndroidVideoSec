var Speaker = require('speaker');
var stream = require('stream');
var ss = require('socket.io-stream');
var fs = require('fs');
var spawn = require('child_process').spawn;

var speaker = new Speaker({
			  channels: 1,          // 1 channels
			  bitDepth: 16,         // 16-bit samples
			  sampleRate: 44100     // 44,100 Hz sample rate
			}); 

var args = ['-f',
		    'S16_LE',
		    '-c',
		    '1',
		    '-r',
		    '44100',
		    '-D',
		    'plughw:CAMERA'];
												
var arecord = spawn('arecord', args);

module.exports = function(io){
	
	var sockets = {};
	
	io.on('connection', function(socket){
		
		sockets[socket.id] = socket;
		console.log('Total listeners: ', Object.keys(sockets).length);

		console.log('Grabando...');
											
		console.log('Spawning arecord ' + args.join(' '));

		arecord.on('exit', function(err){
			console.log('Error system'+err);
		});
		
		arecord.stderr.on('data', function (data) {
	    	socket.emit('audioserv', data);
	    });

		arecord.stdout.on('data', function (data) {
			socket.emit('audioserv', data);
		});

		socket.on('audio',function(data){
			var bufferStream = new stream.PassThrough();
	        bufferStream.write(new Buffer(data));
			bufferStream.pipe(speaker);
		});

		socket.on('disconnect',function(){
			delete sockets[socket.id];	
		});
	});
};
