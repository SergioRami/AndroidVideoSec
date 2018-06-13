var mysql = require('mysql');
var nodemailer = require('nodemailer');

exports.connection = function() {
	var result = 	mysql.createConnection({
			  		host : 'localhost',
			  		user : 'sergiorami',
			  		password : '',
			  		database : 'videoSec'
			  	});
	return result;
}

exports.transport = function() {
	var result = nodemailer.createTransport({
        service: 'Gmail',
        auth: {
            user: 'pipaletilla@gmail.com',
            pass: ''
        }
      });
	return result;
}