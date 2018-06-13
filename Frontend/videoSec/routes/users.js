var express = require('express');
var http = require('http');
var mysql = require('mysql');
var nodemailer = require('nodemailer');
var connex = require('./connex');
var crypt = require('./crypt');
var spawn = require('child_process').spawn;
var fs = require('fs');

var router = express.Router();

var lengthRandomString = 8;
var rString = '0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ';

function randomString() {
    var result = '';
    for (var i = lengthRandomString; i > 0; --i) 
      result += rString[Math.round(Math.random() * (rString.length - 1))];
    return result;
}

function getUserID(req,res,next){
  var email = req.body.email;
  var connection = connex.connection();

  connection.connect();

  connection.query('SELECT id FROM users where email = ?', email, function(err,rows){
    if(err){
      connection.end();
      return next(err);
    }
    if(rows.length == 1){
      res.locals.user = rows[0].id;
      connection.end();
      return next();
    } else {
      next(new Error('401'));
    }
  });
}

/* POST login. */
router.post('/login', function(req, res, next) {
	var email = req.body.email;
	var password = req.body.password;
  var token = req.body.token;
  var connection = connex.connection();
  connection.connect();

  connection.query('SELECT id, password, valid FROM users WHERE email = ?;', email, function(err,rows){
    if(err){
      throw err;
      connection.end();
    }
    if(rows.length !=1){
      res.sendStatus(403);
      return;
      connection.end();
    }
    if(rows[0].valid == 0){
      res.sendStatus(401);
      return;
      connection.end();
    }
    var user = rows[0].id;
    connection.query('SELECT count(1) FROM users_camera WHERE user = ?;', user, function(err,row){
      if(err){
        throw err;
        connection.end();
      }
      if(row[0] == 0){
        res.sendStatus(404);
        return;
        connection.end();
      }
        crypt.matchPass(password,rows[0].password,function(error,same){
        if(err){
          throw err;
          connection.end();
        }
        if(same){
          connection.query('UPDATE users SET token = ? WHERE ID = ?', [token, user], function(err,rows){
            if(err){
              console.log(err);
              res.sendStatus(500);
              connection.end();
            }
            res.sendStatus(200);
            connection.end(); 
          });
        }else{
          res.sendStatus(403);
          return;
          connection.end();
        }
      });
    });  
  });  
});

/*POST code*/
router.post('/code', getUserID, function(req, res, next) {
  var user = res.locals.user;
  var email = req.body.email;
  var code = req.body.code;
  var connection = connex.connection();

  connection.connect();

  connection.query('SELECT code FROM users_code WHERE user = ? AND expiration_date >= NOW();', user, function(err,rows){
    if(err){
      throw err;
      connection.end();
    }
    if(rows.length != 1){
      res.sendStatus(403);
      connection.end();
      return;
    }
    if(rows[0].code == code){
      connection.query('UPDATE users SET valid = 1 WHERE email = ?;', email, function(err,rows){
        if(err){
          throw err;
          connection.end();
        }
        connection.query('DELETE FROM users_code WHERE user = ?;', user, function(err,rows){
        if(err){
          throw err;
          connection.end();
        }
        res.sendStatus(200);
        connection.end();
        });
      });
    }
  });
});

/*POST bidi*/
router.post('/bidi', getUserID, function(req, res, next) {
  var bidi = req.body.bidi;
  var user = res.locals.user;
  var connection = connex.connection();

  connection.connect();

  connection.query('INSERT INTO users_camera(user,camera) (SELECT ?, id FROM camera where camera = ?);', [user, bidi], function(err,rows){
    if(err){
      console.log(err);
      res.sendStatus(404);
      connection.end();
    }
    res.sendStatus(200);
    connection.end(); 
  });
});

/*PUT New Code*/
router.put('/newcode', getUserID, function(req,res,next){
  var user = res.locals.user;
  var email = req.body.email;
  var connection = connex.connection();

  connection.connect();

  var expiration_date = new Date();
  expiration_date.setDate(expiration_date.getDate() + 1);

  var code = randomString();

  connection.query('UPDATE users_code SET code = ?, expiration_date =? WHERE user =?;', [code, expiration_date, user],function(err, result){
    if(err){
      throw err;
    }
      var transporter = connex.transport();
      var mailOptions = {
          from: 'VideoSec App  <no-reply@gmail.com>', 
          to: email, 
          subject: 'Nueva código', 
          text: '', 
          html: '<b>Hola '+'<br>Has solicitado un nuevo código!'+'<br>Para terminar, inserta este codigo: <br><br>'
          + code +'<br> Un saludo y gracias por utilizar la app!</b>' 
      };

      transporter.sendMail(mailOptions, function(error, info){
          if(error){
              return console.log(error);
          }
          console.log('Message sent: ' + info.response);

      });

    console.log(result);
    res.sendStatus(200);
    return;
  });
  connection.end();
});

/* POST Sign up */
router.post('/signup', function(req,res,next){
  var username = req.body.username;
  var email = req.body.email;
  var password = req.body.password;
  var token = req.body.token;
  var connection = connex.connection();
  crypt.cryptPass(password,function(err,hash){
    if(err){
      throw err;
    }
    password = hash;
    connection.connect();

    connection.query('INSERT INTO users (email,name,password,token) VALUES (?,?,?,?);', [email, username, password, token],function(err, result){
      if(err){
        if(err.code == 'ER_DUP_ENTRY'){
  		    res.sendStatus(409);
  		    return;
          connection.end();
        }
        throw err;
        connection.end();
      }
      var expiration_date = new Date();
      expiration_date.setDate(expiration_date.getDate() + 1);

      var code = randomString();

      var user = result.insertId;
                                    
      connection.query('INSERT INTO users_code (user, code, expiration_date) VALUES (?,?,?);', [user, code, expiration_date],function(err, result){
        if(err){
          throw err;
          connection.end();
        }
        
        var transporter = connex.transport();
        var mailOptions = {
            from: 'VideoSec App  <no-reply@gmail.com>', 
            to: email, 
            subject: 'Nueva cuenta', 
            text: '', 
            html: '<b>Hola ' + username +'<br>Bienvenido a la app de videoSec!'+'<br>Para terminar, inserta este codigo: <br><br>'
            + code +'<br> Un saludo y gracias por utilizar la app!</b>' 
        };

        transporter.sendMail(mailOptions, function(error, info){
            if(error){
                return console.log(error);
                connection.end();
            }
            console.log('Message sent: ' + info.response);

        });

      console.log(result);
         res.sendStatus(200);
      return;
      connection.end();
      });
    });
  });
});

/* POST Streaming */
router.post('/streaming', getUserID , function(req,res,next){
  var user = res.locals.user;
  var id = req.body.id;
  var boundary = "BoundaryString";

  var connection = connex.connection();

  connection.connect();

  connection.query('SELECT port FROM camera c INNER JOIN users_camera uc ON c.id = uc.camera where uc.user = ? AND c.id = ?', [user, id], function(err, result){
    if(err){
      throw err;
      connection.end();
    }
    console.log(result[0].port);
    var options = {
      host:   'localhost',
      port:   result[0].port,
      path:   '/',
      method: 'GET',
      headers: req.headers
    };

    var proxyReq = http.request(options, function(proxyRes) {

          res.setHeader('Content-Type', 'multipart/x-mixed-replace;boundary="' + boundary + '"');
          res.setHeader('Connection', 'close');
          res.setHeader('Pragma', 'no-cache');
          res.setHeader('Cache-Control', 'no-cache, private');
          res.setHeader('Expires', 0);
          res.setHeader('Max-Age', 0);

      proxyRes.on('data', function(chunk){
        res.write(chunk);
      });

      proxyRes.on('close', function(){
        res.writeHead(proxyRes.statusCode);
        res.end();
      });

    }).on('error', function(e) {
      console.log(e.message);
      res.writeHead(500);
      res.end();
    });

    proxyReq.end();
  });
});

/*POST cameras*/
router.post('/cameras', getUserID, function(req, res, next) {
  var user = res.locals.user;
  var connection = connex.connection();

  connection.connect();

  connection.query('SELECT id,name FROM camera c INNER JOIN users_camera uc ON c.id = uc.camera WHERE uc.user = ?;', user, function(err,rows){
    if(err){
      console.log(err);
      res.sendStatus(404);
      connection.end();
    }
    res.send(JSON.stringify(rows));
    connection.end(); 
  });
});

module.exports = router;