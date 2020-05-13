#!/usr/bin/python3.5

import os
import json
from time import time
from flask import Flask, request, send_file, send_from_directory, render_template, redirect
from flask_pymongo import PyMongo
from twilio.rest import Client

from user import User
from hashlib import sha256

def passhash(sha):
    return sha256(sha.encode('utf8')).hexdigest()

app = Flask(__name__)

twilio_sid = 'scrubbed'
twilio_key = 'scrubbed'
twilio_uname = 'scrubbed'
client = Client(twilio_sid, twilio_key)

STATIC_FOLDER = './static/' #'/var/www/itlit/itlit/static/'
IMAGES_FOLDER = './images/' #'/var/www/itlit/itlit/images/'
CONTACT_FOLDER = './inquiries/' #'/var/www/itlit/itlit/inquiries/'
FAVICON = IMAGES_FOLDER + 'favicon.png'
UPLOAD_FOLDER = '/var/www/itlit/itlit/images/selfies'
ALLOWED_EXTENSIONS = set(['png', 'jpeg'])

app.config['MONGO_DBNAME']  = 'litdb'
app.config['MONGO_URI']     = 'mongodb://127.0.0.1:27017/litdb'
app.config['UPLOAD_FOLDER'] = UPLOAD_FOLDER
app.config['MAX_CONTENT_LENGTH'] = 8 * 1024 * 1024 #8mb

mongo = PyMongo(app)

def errorify(msg):
    return json.dumps({'error': msg})

def errorless(jsob):
    """ authorize returns None if credentials check out """
    if not ('uname' in jsob and 'passwd' in jsob):
        return 'Phone number or password missing'
    uname = jsob['uname']
    passwd = jsob['passwd']
    if uname == '' or passwd == '':
        return 'Phone number and password cannot be empty'
    if not (User.validuname(uname)):
        return 'Invalid phone number'
    if not (User.validpasswd(passwd)):
        return 'Invalid password'
    user = mongo.db.users.find_one( {'uname':uname} )
    if user is None:
        return 'Account not registered'
    if not (user['passwd'] == passhash(passwd)):
        return 'Incorrect password'
    if not (user['code'] == -1):
        return 'Account not verified'
    return None

@app.route('/')
@app.route('/index')
@app.route('/index.html')
def index():
    return render_template('index.html')

@app.route('/litcoin')
def litcoin():
    return render_template('litcoin.html')

@app.route('/privacy')
def privacy():
    return render_template('privacy.html')

@app.route('/contact', methods=['GET', 'POST'])
def contact():
    if request.method == 'GET':
        return render_template('contact.html')
    elif request.method == 'POST':
        cl = request.content_length
        if cl is not None and cl > 1024:
            return 'max content size is 1kb'
        name = request.form['name']
        email = request.form['email']
        body = request.form['body']
        msg = name + '\n' + email + '\n\n' + body
        f = open(CONTACT_FOLDER + str(time()) + '.txt', 'w')
        f.write(msg)
        f.close()
        return redirect('/contact')

@app.route('/marketing')
def marketing():
    return redirect('contact')

@app.route('/static/<dest>')
def getstatic(dest):
    return send_from_directory(STATIC_FOLDER, dest)

@app.route('/favicon.png')
def favicon():
    return send_file(FAVICON)

@app.route('/images/<path:img>')
def getimage(img):
    return send_from_directory(IMAGES_FOLDER, img)

@app.route('/login', methods=['POST'])
def login():
    err = errorless(request.json)
    if not (err is None):
        return errorify(err)
    uname = request.json['uname']
    users = mongo.db.users

    users.update_one( {'uname':uname}, {'$set': {'lit': False, 'lat': 0.0, 'lon': 0.0}} )
    return json.dumps({})

@app.route('/logout', methods=['POST'])
def logout():
    err = errorless(request.json)
    if err is not None:
        return errorify(err)
    uname = request.json['uname']
    users = mongo.db.users

    users.update_one( {'uname':uname}, {'$set': {'lit': False, 'lat': 0.0, 'lon': 0.0}} )
    return json.dumps({})

@app.route('/register', methods=['POST'])
def register():
    jsob = request.json
    if not ('uname' in jsob and 'passwd' in jsob):
        return errorify('Username or password missing')
    uname = jsob['uname']
    passwd = jsob['passwd']
    if uname == '' or passwd == '':
        return errorify('Phone number and password cannot be empty')
    if not User.validuname(uname):
        return errorify('Invalid phone number')
    if not User.validpasswd(passwd):
        return errorify('Invalid password')
    users = mongo.db.users
    frens = mongo.db.friends

    user = users.find_one( {'uname':uname} )
    if not (user is None):
        if user['code'] == -1:
            return errorify('Phone number already registered')
        users.delete_one({'uname':uname})
    user = User(uname=uname, passwd=passhash(passwd))
    users.insert_one(user.dictify())
    frens.insert_one({'uname':uname, 'friends':[]})
    #print("code: " + str(user.code), file=open('/home/ubuntu/code.txt','w'))
    try:
        client.api.account.messages.create(to=uname,
                                           from_=twilio_uname, 
                                           body='ItLit code: ' + str(user.code))
    except:
        return errorify('Invalid phone number')
    return json.dumps({})

@app.route('/activate', methods=['POST'])
def activate():
    err = errorless(request.json)
    if not (err is None):
        if not (err == 'Account not verified'):
            return errorify(err)
    uname = request.json['uname']
    users = mongo.db.users

    if not ('code' in request.json):
        return errorify('Code missing')
    code = request.json['code']
    user = users.find_one({'uname':uname})
    if not (user['code'] == code):
        return errorify('Incorrect code, please try again') # NO EXCEPTIONS
    users.update_one({'uname':uname}, {'$set': {'code':-1}})
    return json.dumps({})

@app.route('/light', methods=['POST'])
def light():
    err = errorless(request.json)
    if not (err is None):
        return errorify(err)
    uname = request.json['uname']
    users = mongo.db.users

    if not ('lit' in request.json):
        return json.dumps({'lit': users.find_one({'uname':uname})['lit']})
    lit = request.json['lit']
    users.update_one( {'uname':uname}, {'$set': {'lit':lit, 'time': time()}} )
    return json.dumps({})

@app.route('/statusget', methods=['POST'])
def statusget():
    err = errorless(request.json)
    if not (err is None):
        return errorify(err)
    uname = request.json['uname']
    users = mongo.db.users
    
    status = users.find_one({'uname':uname})['status']
    return json.dumps({'status':status})

@app.route('/status', methods=['POST'])
def status():
    err = errorless(request.json)
    if not (err is None):
        return errorify(err)
    uname = request.json['uname']
    users = mongo.db.users

    if not('status' in request.json):
        return errorify('Status is missing')
    status = request.json['status']
    if len(status) > 140:
        return errorify('Status exceeds 140 characters')
    users.update_one( {'uname':uname}, {'$set': {'status':status} } )
    return json.dumps({})

@app.route('/move', methods=['POST'])
def move():
    err = errorless(request.json)
    if not (err is None):
        return errorify(err)
    uname = request.json['uname']
    users = mongo.db.users
    jsob = request.json

    if not ('lat' in jsob and 'lon' in jsob):
        return errorify('Latitude or longitude missing')
    lat = jsob['lat']
    lon = jsob['lon']
    users.update_one( {'uname':uname}, {'$set': {'lit': True, 'lat':lat, 'lon':lon, 'time':time()} } )
    return json.dumps({})

@app.route('/getfriends', methods=['POST'])
def getfriends():
    err = errorless(request.json)
    if not (err is None):
        return errorify(err)
    uname = request.json['uname']
    frens = mongo.db.friends

    friends = frens.find_one({'uname':uname})['friends']
    return json.dumps({'friends':friends})

@app.route('/setfriends', methods=['POST'])
def setfriends():
    err = errorless(request.json)
    if not (err is None):
        return errorify(err)
    uname = request.json['uname']
    frens = mongo.db.friends

    if not ('friends' in request.json):
        return errorify('No friends were submitted')
    friends = request.json['friends']
    frens.update_one( {'uname':uname}, {'$set': {'friends':friends} } )
    return json.dumps({})

def allexcept(fname, friends):
    newfriends = []
    for friend in friends:
        if not (fname == friend['fname']):
            newfriends.append(friend)
    return newfriends

@app.route('/delfriend', methods=['POST'])
def delfriend():
    err = errorless(request.json)
    if not (err is None):
        return errorify(err)
    uname = request.json['uname']
    frens = mongo.db.friends

    if not ('friend' in request.json):
        return errorify('No friend to delete')
    friend = request.json['friend']
    friends = allexcept(friend['fname'], frens.find_one({'uname':uname})['friends'])
    frens.update_one({'uname':uname}, {'$set': {'friends':friends}})
    return json.dumps({})

@app.route('/setfriend', methods=['POST'])
def setfriend():
    err = errorless(request.json)
    if not (err is None):
        return errorify(err)
    uname = request.json['uname']
    frens = mongo.db.friends

    if not ('friend' in request.json):
        return errorify('No friend was submitted')
    friend = request.json['friend']
    if not ('fname' in friend):
        return errorify('Friend must include a phone number')
    name = ''
    lit = False
    if ('name' in friend):
        name = friend['name']
    if ('lit' in friend):
        lit = friend['lit']
    friend = User.newfriend(friend['fname'], name, lit)
    friends = allexcept(friend['fname'], frens.find_one({'uname':uname})['friends'])
    friends.append(friend)
    frens.update_one( {'uname':uname}, {'$set': {'friends':friends} } )
    return json.dumps({})

@app.route('/getlit', methods=['POST'])
def getlit():
    err = errorless(request.json)
    if not (err is None):
        return errorify(err)
    uname = request.json['uname']
    users = mongo.db.users
    frens = mongo.db.friends

    friends = []
    user = users.find_one({'uname':uname})
    if user['lit']:
        friends.append( { \
            'fname'  : uname,          \
            'name'   : 'you',          \
            'status' : user['status'], \
            'lat'    : user['lat'],    \
            'lon'    : user['lon']     \
        })
    user = frens.find_one({'uname':uname})
    for ufriend in user['friends']:
        fname = ufriend['fname']
        ufren = users.find_one({'uname':fname})
        if ufren is not None:
            if ufren['lit']:
                ffren = frens.find_one({'uname':fname})
                if ffren is not None:
                    for ffriend in ffren['friends']:
                        if ffriend['fname'] == uname and ffriend['lit']:
                            friends.append( { \
                                'fname'  : fname,           \
                                'name'   : ufriend['name'], \
                                'status' : ufren['status'], \
                                'lat'    : ufren['lat'],    \
                                'lon'    : ufren['lon']     \
                            })
    return json.dumps( {'friends':friends} )

def sendpic(uname):
    return send_file(app.config['UPLOAD_FOLDER'] + '/' + uname + '.png')

@app.route('/getpic', methods=['POST'])
def getpic():
    nullpic = sendpic('icon')
    err = errorless(request.json)
    if not (err is None):
        return nullpic
    uname = request.json['uname']
    frens = mongo.db.friends

    fname = request.json['fname']
    if fname is None or fname == '':
        return nullpic # No picture specified to get
    friend = frens.find_one({'uname':fname})
    if friend is None:
        return nullpic # Cannot get nonexistent user's pic
    friends = friend['friends']
    mutual = False
    for f in friends:
        if f['fname'] == uname:
            mutual = True
    mutual = mutual or (fname == uname)
    if not mutual:
        return nullpic # Permission denied
    if not os.path.isfile(os.path.join(UPLOAD_FOLDER, fname + '.png')):
        return nullpic # File not found
    return sendpic(fname) # Return friend's image

def allowed_filename(filename):
    return '.' in filename and \
        filename.rsplit('.',1)[1] in ALLOWED_EXTENSIONS

@app.route('/setpic', methods=['POST'])
def setpic():
    uname = request.form['uname']
    passwd = request.form['passwd']
    if uname is None or passwd is None:
        return errorify('User or password missing')
    jsob = {'uname':uname, 'passwd':passwd}
    err = errorless(jsob)
    if not (err is None):
        return errorify(err)

    file = request.files['file']
    if (file is None):
        return errorify('Missing file')
    if not os.path.exists(app.config['UPLOAD_FOLDER']):
        os.makedirs(app.config['UPLOAD_FOLDER'])
    fname = os.path.join(app.config['UPLOAD_FOLDER'], uname + '.png')
    if os.path.isfile(fname):
        os.remove(fname)
    file.save(fname)
    os.chmod(fname, 0o777)
    return json.dumps({})

if __name__ == '__main__':
    app.secret_key = 'scrubbed'
    app.run(host='0.0.0.0')
    os.system('./gc/gc.py')
