var Gpio= require('onoff').Gpio;
var gcm = require('node-gcm');
var connex = require('../routes/connex');

module.exports = function(){

	console.log('GPIO Listening');
	button = new Gpio(17, 'in', 'both');

	button.watch(function(err, value) {
	  if (err) exit();
	  if(value == 1){
	  	console.log('Boton pulsado');

	  	var sender = new gcm.Sender('AIzaSyBa2YCpYopWnF4jEMKHDT75VtQgrvQcIkw');

		//var registrationIds = ['eKcIFbSH-_4:APA91bH_kzFsnfqw2Ii5prF7ILbPUjrc-llcchP-LO59rJfBG-QiU85pWYqmh9g8Jz2OW08V_cC0DpNNWSNDLDeO7ApZESqcFLy7T72Tax1ACVABEnlg6DPRmVp8vB18OeRpMJISc5MU'];
		var registrationIds = [];
		var connection = connex.connection();
		var camera = 1;

		  connection.connect();

		  connection.query('SELECT token FROM users WHERE id in (SELECT user FROM users_camera WHERE camera = ?) AND token IS NOT NULL AND token <> "";', camera, function(err,rows){
		    if(err){
		      console.log(err);
		      connection.end();
		    }
		    for (var i = 0 ; i < rows.length ; i++){
		    	registrationIds.push(rows[i].token);
		    }
		    var message = new gcm.Message();
			message.addData('message',"Tienes una llamada entrante"); 

			sender.send(message, registrationIds, 4, function (err,response) {
				if(err) {
					console.error(err);
				} else {
					console.log(response);
				}
			});
		    connection.end(); 
		});
		}
	});

};