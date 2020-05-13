from random import randint
from time import time

class User:
    unull = '0000000000'
    pnull = '0000000000000000000000000000000000000000000000000000000000000000'

    def __init__(self, uname=unull, passwd=pnull, code=randint(10000,99999), lit=False, status="", lat=0.0, lon=0.0, time=time()):
        self.uname      = uname      # 10 digit phone number
        self.passwd     = passwd     # (sha256 . sha256) password
        self.code       = code       # text-code for phone verification, -1 if verified
        self.lit        = lit        # True broadcasts location, False is offline
        self.status     = status     # status message, like "hey what's up doods"
        self.lat        = lat        # latitude
        self.lon        = lon        # longitude coordinates
        self.time       = time       # last time coordinates were updated, seconds from unix epoch
        #friends are contained in friends collection
        #a users friend is at "uname":user.uname
        #it has one other field: "friends":[friend]
        #where a friend = {"fname":0000000000, "name":"some name", lit:True}
        #where lit is whether or not they can see you

    @staticmethod
    def validpasswd(passwd):
        if not (len(passwd) == 64):
            return False
        hex = True
        for char in passwd:
            hex = hex and char in "0123456789abcdef"
        return hex

    @staticmethod
    def validuname(uname):
        if len(uname) > 13 or len(uname) < 10:
            return False
        for c in uname:
            if c not in '1234567890':
                return False
        return True

    @staticmethod
    def newfriend(fname, name, lit):
        return {'fname':fname, 'name':name, 'lit':lit}

    @classmethod
    def fromdict(cls, user):
        """ returns a new user from a dictionary of user fields """
        return cls( \
            uname   = user['uname'],  \
            passwd  = user['passwd'], \
            code    = user['code'],   \
            lit     = user['lit'],    \
            status  = user['status'], \
            lat     = user['lat'],    \
            lon     = user['lon'],    \
            time    = user['time'],   \
        )

    def dictify(self):
        """ returns a dictionary of itself """
        return { \
            'uname'  : self.uname,  \
            'passwd' : self.passwd, \
            'code'   : self.code,   \
            'lit'	 : self.lit,    \
            'status' : self.status, \
            'lat'    : self.lat,    \
            'lon'    : self.lon,    \
            'time'   : self.time,   \
            }
