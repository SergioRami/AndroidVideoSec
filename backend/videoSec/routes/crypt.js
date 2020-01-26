var bcrypt = require('bcrypt');

exports.cryptPass = function(password,callback){
	bcrypt.genSalt(10, function(err,salt){
		if(err){
			return callback(error);
		}
		bcrypt.hash(password, salt, function(err,hash){
			if(err){
				return callback(err);
			}
			return callback(null, hash);
		});
	});
}

exports.matchPass = function(password, passToMatch,callback){
	bcrypt.compare(password,passToMatch,function(err,same){
		if(err){
			return callback(err);
		}
		return callback(null,same);
	})
}