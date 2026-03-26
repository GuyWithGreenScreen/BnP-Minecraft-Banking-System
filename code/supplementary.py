import json, secrets
from argon2 import PasswordHasher
ph = PasswordHasher()



USER_JSON = json.load(open("../storage_dat/users.json", "r"))
API_KEY_JSON = json.load(open("../storage_dat/api_keys.json", "r"))

def rerollIDSafety(JSON_OBJECT, ID_BYTE_LEN):
    
    ID = secrets.token_hex(ID_BYTE_LEN)
    
    while ID in JSON_OBJECT:
        ID = secrets.token_hex(ID_BYTE_LEN)
        
    return ID.upper()


def createKey():
    
    ID = rerollIDSafety(API_KEY_JSON["keys"], 8)
    
    API_KEY_JSON["keys"].append(ID)
    
    json.dump(API_KEY_JSON, open("../storage_dat/api_keys.json", "w"))
    
def resetPassword(user_id, new_password):

        
        USER_JSON["users"][user_id]["pass"] = ph.hash(new_password)
        
        json.dump(USER_JSON, open("../storage_dat/users.json", "w"))


resetPassword("02AC", "fish")