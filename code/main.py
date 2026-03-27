import secrets, json, math, time, re
from argon2 import PasswordHasher
from enum import Enum
from fastapi import FastAPI
from pydantic import BaseModel
from fastapi.responses import HTMLResponse
from fastapi.templating import Jinja2Templates
from fastapi import Request
from fastapi.staticfiles import StaticFiles
from threading import Lock


USER_JSON = json.load(open("../storage_dat/users.json", "r"))
COIN_JSON = json.load(open("../storage_dat/coins.json", "r"))
RESOURCE_JSON = json.load(open("../storage_dat/resources.json", "r"))
CHEQUE_INVOICE_JSON = json.load(open("../storage_dat/orders.json", "r"))
API_KEY_JSON = json.load(open("../storage_dat/api_keys.json", "r"))

ph = PasswordHasher()

ACCOUNT_MAKING_ALLOWANCE = True

ERROR = None






class Types(Enum):
    CHEQUE=1
    INVOICE=2

class Currency(Enum):
    COIN=1
    RESOURCE=2

class StateSaver(Enum):
    USER=1
    COIN=2
    RESOURCE=3
    CHEQUE_INVOICE=4

class MintMethod(Enum):
    RESOURCE_ID=1,
    ITEM_ID=2

class LogType(Enum):
    ERROR="! - [ERROR!] : "
    WARNING="? - [WARN?] : "
    REGLOG="[] - [REGLOG] : "

def logAppend(message: str, logtype: LogType):
    
    with open("../storage_dat/logs.txt", "a") as file:
        file.write(f"{time.strftime("%Y-%m-%d/%H:%M:%S")} - {logtype.value}{message}\n")

def callError(message: str):
    global ERROR
    
    print(f"! - [CALLED ERROR! : \"{message}\"]")
    
    logAppend(message, LogType.ERROR)
    
    
    if (ERROR == None):
        ERROR = f"{message}\n"
    else:
        ERROR += f"{message}\n"
    

def saveState(state: StateSaver):
    try:
        
        if (state == StateSaver.USER):
            
            json.dump(USER_JSON, open("../storage_dat/users.json", "w"), indent=4)
            
        elif (state == StateSaver.COIN):
            
            json.dump(COIN_JSON, open("../storage_dat/coins.json", "w"), indent=4)
            
        elif (state == StateSaver.RESOURCE):
            
            json.dump(RESOURCE_JSON, open("../storage_dat/resources.json", "w"), indent=4)
            
        elif (state == StateSaver.CHEQUE_INVOICE):
            
            json.dump(CHEQUE_INVOICE_JSON, open("../storage_dat/orders.json", "w"), indent=4)
            
    except Exception as e:
        callError("Save_State: " +e)

def rerollIDSafety(JSON_OBJECT, ID_BYTE_LEN):
    try:
        
        ID = secrets.token_hex(ID_BYTE_LEN)
        
        while ID in JSON_OBJECT:
            ID = secrets.token_hex(ID_BYTE_LEN)
            
        return ID.upper()
    
    except Exception as e:
        
        callError("Reroll_ID_Safety: " +e)

def validateUserID(user_id):
    try:
        
        return user_id in USER_JSON["users"]
    
    except Exception as e:
        
        callError("Validate_User_ID: " + str(e))

def validateCred(user_id, password, noToken:bool = False):
    try:
        
        return (user_id in USER_JSON["users"] 
                
                and 
                
                ((password in list(USER_JSON["users"][user_id]["login_tokens"]) 
                    and not noToken) or ph.verify(USER_JSON["users"][user_id]["pass"], password))
                
            )
        
    except Exception as e:
        
        callError("Validate_Cred: " + str(e))


def makeSetUseAdd(JSON_OBJECT, key, add_set_value): 
    try:
        
        if (key in JSON_OBJECT):
            
            JSON_OBJECT[key] += add_set_value
        else:
            
            JSON_OBJECT[key] = add_set_value
            
    except Exception as e:
        
        callError("Make_Set_Use_Add: " + str(e))

def validateUserCurrency(currency, currency_id, user_id):
    try:
        if (validateUserID(user_id)):
            if (currency == Currency.RESOURCE.value):
                
                if (currency_id in USER_JSON["users"][user_id]["resource_balances"]):
                    
                    return True
                
            elif (currency == Currency.COIN.value):
                
                if (currency_id in USER_JSON["users"][user_id]["coin_balances"]):
                    
                    return True
        else:
            callError("Validate_User_Currency: Invalid User ID")
        
        return False
    except Exception as e:
        callError("Validate_User_Currency: " + str(e))



def validateUserCurrencyAmount(currency, currency_id, user_id) -> float:
    try:
        if (validateUserID(user_id)):
            
            if (currency == Currency.RESOURCE.value):
                
                if (currency_id in USER_JSON["users"][user_id]["resource_balances"]):
                    
                    return USER_JSON["users"][user_id]["resource_balances"][currency_id]
                
            elif (currency == Currency.COIN.value):
                
                if (currency_id in USER_JSON["users"][user_id]["coin_balances"]):
                    
                    return USER_JSON["users"][user_id]["coin_balances"][currency_id]
                
            callError("Validate_User_Currency_Amount: User doesnt have currency")
            
            return None
        else:
            
            callError("Validate_User_Currency_Amount: Invalid User ID")
            
            return None
    
    except Exception as e:
        
        callError("Validate_User_Currency_Amount: " + str(e))


def validateCurrency(currency, currency_id):
    try:
        if (currency == Currency.COIN.value):
            
            if (currency_id in COIN_JSON["ids"]):
                
                return True
            
            else:
                
                return False
            
            
        elif (currency == Currency.RESOURCE.value):
            
            if (currency_id in RESOURCE_JSON["ids"]):
                
                return True
            
            else:
                
                return False
            
            
        else:
            
            callError("Validate_Currency: Invalid Currency Type")
            
            return False
        
    except Exception as e:
        callError("Validate_Currency: " + str(e))

def getCoinPrice(coin_id, amount):
    
    try:
        
        if (validateCurrency(Currency.COIN.value, coin_id)):
            
            return (2*amount*COIN_JSON["ids"][coin_id]["backing"])/(2*COIN_JSON["ids"][coin_id]["initial_buy"] - amount)
        
        else:
            
            callError("Get_Coin_Price: Invalid Coin ID")
            
            return None
        
    except Exception as e:
        
        callError("Get_Coin_Price: " + str(e))
        
        return None



def resourceChartUpdate(resource_id):
    try:
        if (validateCurrency(Currency.RESOURCE.value, resource_id)):
            RESOURCE_JSON["ids"][resource_id]["history"].append(
                {
                    "time": time.time(),
                    "amount": RESOURCE_JSON["ids"][resource_id]["minted_amount"]
                }
            )
            return True
        else:
            logAppend(f"Resource_Chart_Update: Invalid Resource ID - {resource_id}", LogType.WARNING)
            return False
    except Exception as e:
        logAppend(f"Resource_Chart_Update: {e}", LogType.WARNING)
        return False


def coinChartUpdate(coin_id):
    try:
        if (validateCurrency(Currency.COIN.value, coin_id)):
            COIN_JSON["ids"][coin_id]["history"].append(
                {
                    "time": time.time(),
                    "backing": COIN_JSON["ids"][coin_id]["backing"],
                    "price": COIN_JSON["ids"][coin_id]["backing"]/COIN_JSON["ids"][coin_id]["initial_buy"]
                }
            )
            return True
        else:
            logAppend(f"Coin_Chart_Update: Invalid Coin ID - {coin_id}", LogType.WARNING)
            return False
    except Exception as e:
        logAppend(f"Coin_Chart_Update: {e}", LogType.WARNING)
        return False

def createAccount(username, password):
    try:
        tempUser = {
            "username": username,
            "pass": ph.hash(password),
            "login_tokens": [],
            "resource_balances": {},
            "coin_balances": {}
        }
        
        
        uid = rerollIDSafety(USER_JSON["users"], 2)
        
        USER_JSON["users"][uid] = tempUser
        
        # / DEBUG /
        #print(USER_JSON["users"])
        
        saveState(StateSaver.USER)
        
        return uid
    except Exception as e:
        callError("Create_Account: " + str(e))
        
        return None

def createLoginToken(user_id, password):
    
    if (validateCred(user_id, password, True)):
        
        tid = rerollIDSafety(USER_JSON["users"][user_id]["login_tokens"], 8)
        
        USER_JSON["users"][user_id]["login_tokens"].append(str(tid))
        
        saveState(StateSaver.USER)
        
        return tid
    else:
        
        callError("Create_Login_Token: Invalid Credentials")
        
        return None



def mint(mint_id, amount: float, mint_method: MintMethod, item_name=None):
    if (amount > 0):
        if (mint_method == MintMethod.ITEM_ID):
            
            if (mint_id in RESOURCE_JSON["items"]):
                
                RESOURCE_JSON["ids"][RESOURCE_JSON["items"][mint_id]]["minted_amount"] += amount
                
                resourceChartUpdate(RESOURCE_JSON["items"][mint_id])

                saveState(StateSaver.RESOURCE)
            else:
                
                iid = rerollIDSafety(RESOURCE_JSON["ids"], 2)
                
                RESOURCE_JSON["items"][mint_id] = iid
                RESOURCE_JSON["ids"][iid] = {
                    "item_id": mint_id,
                    "item_name": item_name,
                    "minted_amount": amount,
                    "history": [
                        {
                            "time": time.time()-1,
                            "amount": amount
                        }
                    ]
                }
                
                resourceChartUpdate(iid)
                
                saveState(StateSaver.RESOURCE)
                
            return RESOURCE_JSON["items"][mint_id]
        
        elif (mint_method == MintMethod.RESOURCE_ID):
            
            if (mint_id in RESOURCE_JSON["ids"]):
                
                RESOURCE_JSON["ids"][mint_id]["minted_amount"] += amount
                
                resourceChartUpdate(mint_id)
                
                saveState(StateSaver.RESOURCE)
            else:
                
                callError("Mint: Resource ID doesnt exist!")
        else:
            
            callError("Mint: Invalid Mint Method!")
    else:
        callError("Mint: Invalid Amount")



def unmint(mint_id, amount: float, mint_method: MintMethod):
    
    if (amount > 0):
        if (mint_method == MintMethod.ITEM_ID):
            
            if (mint_id in RESOURCE_JSON["items"]):
                
                RESOURCE_JSON["ids"][RESOURCE_JSON["items"][mint_id]]["minted_amount"] -= amount
                
                resourceChartUpdate(mint_id)
                
                saveState(StateSaver.RESOURCE)
            else:
                
                callError("Unmint: Item ID Not Found / Item Not Minted Yet!")
        
        elif (mint_method == MintMethod.RESOURCE_ID):
            
            if mint_id in RESOURCE_JSON["ids"]:
                
                RESOURCE_JSON["ids"][mint_id]["minted_amount"] -= amount
                
                resourceChartUpdate(mint_id)
                
                saveState(StateSaver.RESOURCE)
                
            else:
                
                callError("Unmint: Item ID Not Found / Item Not Minted Yet!")
        else:
            
            callError("Unmint: Invalid Mint Method")
    else:
        callError("Unmint: Invalid Amount")



def adjustCoinBacking(coin_id, amount: float):
    
    if (validateCurrency(Currency.COIN.value, coin_id)):
        
        COIN_JSON["ids"][coin_id]["backing"] += amount
        
        coinChartUpdate(coin_id)
        
        saveState(StateSaver.COIN)

def adjustCurrencyAmount(currency, currency_id, amount: float):
    
    if (validateCurrency(currency, currency_id)):
        
        if (currency == Currency.COIN.value):
            
            adjustCoinBacking(currency_id, amount)
            
        elif (currency == Currency.RESOURCE.value):
            
            if (amount < 0):
                
                unmint(currency_id, amount, MintMethod.RESOURCE_ID)
                
            else:
                
                mint(currency_id, amount, MintMethod.RESOURCE_ID)
        else:
            
            callError("Adjust_Currency_Amount: Invalid Currency Type")
    else:
        
        callError("Adjust_Currency_Amount: Invalid Currency")



def depositCurrency(currency, currency_id, amount: float, user_id):
    try:
        if (amount > 0):
            if (validateCurrency(currency, currency_id) and validateUserID(user_id)):
                
                if (currency == Currency.COIN.value):
                    
                    makeSetUseAdd(USER_JSON["users"][user_id]["coin_balances"], currency_id, amount)
                    
                elif (currency == Currency.RESOURCE.value):
                    
                    makeSetUseAdd(USER_JSON["users"][user_id]["resource_balances"], currency_id, amount)
                
                else:
                    
                    callError("Deposit_Currency: Invalid Currency Type")
                    
                    return False
                    
                saveState(StateSaver.USER)
                
                return True
                
            else:
                
                callError("Deposit_Currency: Invalid User and/or Currency")
            
                return False
        else:
                
            callError("Deposit_Currency: Invalid Currency Amount")
        
            return False
    except Exception as e:
        
        callError("Deposit_Currency: " + str(e))
        
        return False



def withdrawCurrency(currency, currency_id, amount: float, user_id, password):
    try:
        if (amount > 0):
            if (validateCurrency(currency, currency_id) and validateCred(user_id, password)):
                
                if (validateUserCurrency(currency, currency_id, user_id) and amount <= validateUserCurrencyAmount(currency, currency_id, user_id)):
                    
                    currency_type = None
                    
                    if (currency == Currency.COIN.value):
                        
                        currency_type = "coin_balances"
                        
                    elif (currency == Currency.RESOURCE.value):
                        
                        currency_type = "resource_balances"
                        
                    else:
                        
                        callError("Withdraw_Currency: Invalid Currency Type")
                        
                        return False
                    
                    USER_JSON["users"][user_id][currency_type][currency_id] -= amount
                    
                    saveState(StateSaver.USER)
                    
                    return True
                
                else:
                    
                    callError("Withdraw_Currency: Insufficent Funds in User")
                    
                    return False
            else:
                
                callError("Withdraw_Currency: Invalid User and/or Currency Credentials")
                
                return False
        else:
                
            callError("Withdraw_Currency: Invalid Currency Amount")
            
            return False
    except Exception as e:
        
        callError("Withdraw_Currency: " + str(e))
        
        return False


def cashCheque(cheque_id, user_id, password):
    try:
        
        if (validateCred(user_id, password)):
            
            if (cheque_id in CHEQUE_INVOICE_JSON["ids"] and CHEQUE_INVOICE_JSON["ids"][cheque_id]["type"] == Types.CHEQUE.value):
                
                if (validateCurrency(CHEQUE_INVOICE_JSON["ids"][cheque_id]["currency_type"], CHEQUE_INVOICE_JSON["ids"][cheque_id]["currency_id"])):
                    
                    if (user_id == CHEQUE_INVOICE_JSON["ids"][cheque_id]["to_user"]):
                        
                        currency_type = None
                        
                        if (CHEQUE_INVOICE_JSON["ids"][cheque_id]["currency_type"] == 1):
                            
                            if (depositCurrency(Currency.COIN.value, CHEQUE_INVOICE_JSON["ids"][cheque_id]["currency_id"], CHEQUE_INVOICE_JSON["ids"][cheque_id]["amount"], user_id)):
                            
                                adjustCoinBacking(CHEQUE_INVOICE_JSON["ids"][cheque_id]["currency_id"], getCoinPrice(CHEQUE_INVOICE_JSON["ids"][cheque_id]["currency_id"], CHEQUE_INVOICE_JSON["ids"][cheque_id]["amount"]))
                            
                                return True
                                
                            else:
                                
                                callError("Cash_Cheque: Currency Coin Deposit Failure")
                                
                                return False
                            
                        elif (CHEQUE_INVOICE_JSON["ids"][cheque_id]["currency_type"] == 2):
                            
                            if (depositCurrency(Currency.RESOURCE.value, CHEQUE_INVOICE_JSON["ids"][cheque_id]["currency_id"], CHEQUE_INVOICE_JSON["ids"][cheque_id]["amount"], user_id)):
                            
                                mint(CHEQUE_INVOICE_JSON["ids"][cheque_id]["currency_id"], CHEQUE_INVOICE_JSON["ids"][cheque_id]["amount"], MintMethod.RESOURCE_ID)
                                
                                return True
                            
                            else:
                                
                                callError("Cash_Cheque: Currency Resource Deposit Failure")
                            
                                return False
                        else:
                            
                            callError("Cash_Cheque: Cheque Currency Type is Invalid")
                            
                            return False
                        
                    else:
                        
                        callError("Cash_Cheque: Cheque Recipient is not the user ID")
                        
                        return False
                    
                else:
                    
                    callError("Cash_Cheque: Invalid Currency Type and/or ID")
                    
                    return False
            else:
                callError("Cash_Cheque: Invalid Cheque ID and/or Order Type")
        else:
            
            callError("Cash_Cheque: Invalid Credentials")
            
            return False
        
    except Exception as e:
        
        callError("Cash_Cheque: " + str(e))
        
        return False

def createCheque(currency, currency_id, amount: float, from_user_id, from_user_pass, to_user_id):
    
    try:
        if (amount > 0):
            if (validateCurrency(currency, currency_id) and validateCred(from_user_id, from_user_pass) and validateUserID(to_user_id)):


                withdrawAttempt = withdrawCurrency(currency, currency_id, amount, from_user_id, from_user_pass)

                if (not withdrawAttempt):
                    
                    callError("Create_Cheque: Withdraw Failed")
                    
                    return None

                if (currency == Currency.COIN.value and withdrawAttempt):
                    
                    adjustCoinBacking(currency_id, getCoinPrice(currency_id, -amount))
                                    
                elif (currency == Currency.RESOURCE.value and withdrawAttempt):
                    
                    unmint(currency_id, amount, MintMethod.RESOURCE_ID)
                                
                else:
                    
                    callError("Create_Cheque: Invalid Currency Type")
                    
                    return None
                
                            
                cheque_id = rerollIDSafety(CHEQUE_INVOICE_JSON["ids"], 4)
                
                CHEQUE_INVOICE_JSON["ids"][cheque_id] = {
                    "type": 1,
                    "to_user": to_user_id,
                    "from_user": from_user_id,
                    "currency_type": currency,
                    "currency_id": currency_id,
                    "amount": amount
                }
                
                saveState(StateSaver.CHEQUE_INVOICE)
                
                return cheque_id
                
            else:
                
                callError("Create_Cheque: Invalid User and/or Currency Credentials")
                
                return None
        else:
            
            callError("Create_Cheque: Invalid Currency Amount")
            
            return None
    except Exception as e:
        
        callError("Create_Cheque: " + str(e))
        
        return None


def createInvoice(currency, currency_id, amount: float, from_user, from_user_pass, to_user):
    
    try:
        
        if (amount > 0):
            if (validateCurrency(currency, currency_id) and validateCred(from_user, from_user_pass) and validateUserID(to_user)):
                    
                invoice_id = rerollIDSafety(CHEQUE_INVOICE_JSON["ids"], 4)
                
                CHEQUE_INVOICE_JSON["ids"][invoice_id] = {
                    "type": 2,
                    "to_user": to_user,
                    "from_user": from_user,
                    "currency_type": currency,
                    "currency_id": currency_id,
                    "amount": amount
                }
                
                saveState(StateSaver.CHEQUE_INVOICE)
                
                return invoice_id
            
            else:
                
                callError("Create_Invoice: Invalid User and/or Currency Credentials")
                
                return None
        else:
                
                callError("Create_Invoice: Invalid Currency Amount")
                
                return None
    except Exception as e:
        
        callError("Create_Invoice: " + str(e))
        
        return None

def pay_invoice(invoice_id, user_id, password):
    
    try:
        
        if (validateCred(user_id, password)):
            
            if (invoice_id in CHEQUE_INVOICE_JSON["ids"]):
                
                invoice = CHEQUE_INVOICE_JSON["ids"][invoice_id]
                
                if (user_id == invoice["to_user"]):
                                        
                    if (withdrawCurrency(invoice["currency_type"], invoice["currency_id"], invoice["amount"], user_id, password)):
                                                
                        if (depositCurrency(invoice["currency_type"], invoice["currency_id"], invoice["amount"], invoice["from_user"])):
                            
                            return True
                        
                        else:
                            
                            callError("Pay_Invoice: Deposit Failure !FATAL ERROR! WITHDRAW NO DEPOSIT")
                            
                            return False
                    else:
                        
                        callError("Pay_Invoice: Withdraw Failure")
                        
                        return False
                else:
                    
                    callError("Pay_Invoice: User not recepient and payer of invoice")
                    
                    return False
                
            else:
                
                callError("Pay_Invoice: Invoice ID Invalid")
                
                return False
        
        else:
            
            callError("Pay_Invoice: Invalid Credentials")
            
            return False
        
    except Exception as e:
        
        callError("Pay_Invoice: " + str(e))
        
        return False
        





def buy_coin(coin_id, amount, user_id, password):
    
    try:
        if (amount > 0):
            if (validateCred(user_id, password) and validateCurrency(Currency.COIN.value, coin_id)):
                
                price = getCoinPrice(coin_id, amount)
                
                if (price == None):
                    
                    callError("Buy_Coin: Coin Price Failure")
                    
                    return False
                
                else: 
                    
                    if (withdrawCurrency(Currency.RESOURCE.value, COIN_JSON["ids"][coin_id]["backing_res_id"], price, user_id, password)):
                        
                        COIN_JSON["ids"][coin_id]["backing"] += price
                        
                        if (depositCurrency(Currency.COIN.value, coin_id, amount, user_id)):
                            
                            unmint(COIN_JSON["ids"][coin_id]["backing_res_id"], price, MintMethod.RESOURCE_ID)
                            
                            saveState(StateSaver.COIN)
                            
                            coinChartUpdate(coin_id)
                            
                            return True
                        
                        else:
                            
                            callError("Buy_Coin: Deposit Failure !FATAL ERROR! WITHDRAW NO DEPOSIT")
                            
                            return False
                    else:
                        
                        callError("Buy_Coin: Withdraw Failure")
                        
                        return False
                    
            else:
                
                callError("Buy_Coin: Invalid User and/or Currency Credentials")
                
                return False
        else:
                
            callError("Buy_Coin: Invalid Amount")
            
            return False
    except Exception as e:
        
        callError("Buy_Coin" + str(e))
        
        return False
        
def sell_coin(coin_id, amount, user_id, password):
    
    try:
        
        if (amount > 0):
            if (validateCred(user_id, password) and validateCurrency(Currency.COIN.value, coin_id)):
                
                price = getCoinPrice(coin_id, -amount)
                
                if (price == None):
                    
                    callError("Sell_Coin: Coin Price Failure")
                    
                    return False
                
                else:
                
                    if (withdrawCurrency(Currency.COIN.value, coin_id, amount, user_id, password)):
                        
                        COIN_JSON["ids"][coin_id]["backing"] += price
                        
                        mint(COIN_JSON["ids"][coin_id]["backing_res_id"], abs(price), MintMethod.RESOURCE_ID)
                        
                        if (depositCurrency(Currency.RESOURCE.value, COIN_JSON["ids"][coin_id]["backing_res_id"], abs(price), user_id)):
                        
                            saveState(StateSaver.COIN)
                            
                            coinChartUpdate(coin_id)
                            
                            return True
                        
                        else:
                            
                            callError("Sell_Coin: Deposit Failure !FATAL ERROR! WITHDRAW NO DEPOSIT")
                            
                            return False
                    else:
                        
                        callError("Sell_Coin: Withdraw Failure")

                        return False
            else:
                
                callError("Sell_Coin: Invalid User Credential and/or Currency")
                
                return False
        else:
                
            callError("Sell_Coin: Invalid Amount")
            
            return False 
    except Exception as e:
        
        callError("Sell_Coin: " + str(e))
        
        return False

def create_coin(backing_resource_id, name, price, initial_buy_amount, user_id, password):
    
    try:
        if (price*initial_buy_amount > 0):
            if (validateCurrency(Currency.RESOURCE.value, backing_resource_id)):
                
                if (re.fullmatch(r'^[A-Za-z]{3,12}$', name) and str(name).upper() not in COIN_JSON["names"]):
                    if (withdrawCurrency(Currency.RESOURCE.value, backing_resource_id, price*initial_buy_amount, user_id, password)):
                        
                        cid = rerollIDSafety(COIN_JSON["ids"], 2)
                        
                        COIN_JSON["ids"][cid] = {
                            "name": name,
                            "backing": price*initial_buy_amount,
                            "creator_id": user_id,
                            "minted_amount": initial_buy_amount,
                            "backing_res_id": backing_resource_id,
                            "initial_buy": initial_buy_amount,
                            "history": [
                                {
                                    "time": time.time(),
                                    "backing": price*initial_buy_amount,
                                    "price": price
                                }
                            ]
                        }
                        
                        COIN_JSON["names"][name] = cid.upper()
                        
                        unmint(COIN_JSON["ids"][cid]["backing_res_id"], price*initial_buy_amount, MintMethod.RESOURCE_ID)
                                                    
                        saveState(StateSaver.COIN)
                    
                        
                        if (depositCurrency(Currency.COIN.value, cid, initial_buy_amount, user_id)):
                            
                            saveState(StateSaver.USER)
                            
                            return cid
                        
                        else:
                            
                            callError("Create_Coin: Deposit Failure !FATAL ERROR! WITHDRAW NO DEPOSIT")
                            
                            return None
                        
                        
                    else:
                        
                        callError("Create_Coin: Withdraw Failure")
                        
                        return None
                else:
                    
                    callError("Create_Coin: Name Already In Use Or Invalid Name")
                        
                    return None
                
            else:
                
                callError("Create_Coin: Invalid Resource")
                
                return None
        else:
            
            callError("Create_Coin: Invalid Price")
        
    except Exception as e:
        
        callError("Create_Coin: " + str(e))

        return None

###     API SIDE
### ------------------
###  NETWORKING SIDE



def _ERROR():
    global ERROR
    tempErr = ERROR
    ERROR = None
    return tempErr


def _CHECK_API_KEY(RequestJSON):
    try:
        if ("api_key" in RequestJSON["dat"]):
            if (RequestJSON["dat"]["api_key"] in API_KEY_JSON["keys"]):
                return True
            else:
                callError("Check_API_Key: API Key Invalid")
                return False
        else:
            callError("Check_API_Key: API Key Field Not Found")
            return False
    except Exception as e:
        callError("Check_API_Key: " + e)
        return False
        
def translateJavaRequests(RequestJSON):
    global ERROR, ACCOUNT_MAKING_ALLOWANCE
    try:
        match RequestJSON["request"]:
            case "mint":
                if (_CHECK_API_KEY(RequestJSON)):
                    resourceId = mint(RequestJSON["dat"]["item_id"], RequestJSON["dat"]["amount"], MintMethod.ITEM_ID, RequestJSON["dat"]["item_name"])
                #print(ERROR)
                if (ERROR == None):
                    return {
                        "response": "accept",
                        "error": "None",
                        "dat": {
                            "resource_id": resourceId
                        }
                    }
                else:
                    return {
                        "response": "deny",
                        "error": _ERROR(),
                        "dat": {}
                    }
            case "unmint":
                if (_CHECK_API_KEY(RequestJSON)):
                    unmint(RequestJSON["dat"]["resource_id"], RequestJSON["dat"]["amount"], MintMethod.RESOURCE_ID)
                #print(ERROR)
                if (ERROR == None):
                    return {
                        "response": "accept",
                        "error": "None",
                        "dat": {}
                    }
                else:
                    return {
                        "response": "deny",
                        "error": _ERROR(),
                        "dat": {}
                    }
            case "deposit":
                if (_CHECK_API_KEY(RequestJSON)):
                    depositCurrency(RequestJSON["dat"]["currency"], RequestJSON["dat"]["currency_id"], RequestJSON["dat"]["amount"], RequestJSON["dat"]["user_id"])
                if (ERROR == None):
                    return {
                        "response": "accept",
                        "error": "None",
                        "dat": {}
                    }
                else:
                    return {
                        "response": "deny",
                        "error": _ERROR(),
                        "dat": {}
                    }
            case "withdraw":
                if (_CHECK_API_KEY(RequestJSON)):
                    withdraw = withdrawCurrency(RequestJSON["dat"]["currency"], RequestJSON["dat"]["currency_id"], RequestJSON["dat"]["amount"], RequestJSON["dat"]["user_id"], RequestJSON["dat"]["password"])
                    name = "-!ERROR!-"
                    if (RequestJSON["dat"]["currency"] == Currency.COIN.value):
                        name = COIN_JSON["ids"][RequestJSON["dat"]["currency_id"]]["name"]
                    elif (RequestJSON["dat"]["currency"] == Currency.RESOURCE.value):
                        name = RESOURCE_JSON["ids"][RequestJSON["dat"]["currency_id"]]["item_id"]
                if (ERROR == None):
                    return {
                        "response": "accept",
                        "error": "None",
                        "dat": {
                            "name": name
                        }
                    }
                else:
                    return {
                        "response": "deny",
                        "error": _ERROR(),
                        "dat": {}
                    }
            case "create_cheque":
                if (_CHECK_API_KEY(RequestJSON)): 
                    cheque_id = createCheque(RequestJSON["dat"]["currency"], RequestJSON["dat"]["currency_id"], RequestJSON["dat"]["amount"],
                                            RequestJSON["dat"]["from_user_id"], RequestJSON["dat"]["from_user_password"], RequestJSON["dat"]["to_user_id"])
                    to_user_name = None
                    from_user_name = None
                    if (validateUserID(RequestJSON["dat"]["to_user_id"]) and validateUserID(RequestJSON["dat"]["from_user_id"])):
                        to_user_name = USER_JSON["users"][RequestJSON["dat"]["to_user_id"]]["username"]
                        from_user_name = USER_JSON["users"][RequestJSON["dat"]["from_user_id"]]["username"]
                    else:
                        callError("Invalid User ID(s)")
                    currency_name = "-!ERROR!-"
                    if (RequestJSON["dat"]["currency"] == Currency.COIN.value):
                        if (validateCurrency(Currency.COIN.value, RequestJSON["dat"]["currency_id"])):
                            currency_name = COIN_JSON["ids"][RequestJSON["dat"]["currency_id"]]["name"]
                        else:
                            callError("Requests: Invalid Currency (coin) ID")
                    elif (RequestJSON["dat"]["currency"] == Currency.RESOURCE.value):
                        if (validateCurrency(Currency.RESOURCE.value, RequestJSON["dat"]["currency_id"])):
                            currency_name = RESOURCE_JSON["ids"][RequestJSON["dat"]["currency_id"]]["item_name"]
                        else:
                            callError("Requests: Invalid Currency (resource) ID")
                    else:
                        callError("Invalid Currency Types. FATAL ERROR! FATAL ERROR! SEND TO MARK! 0x1")
                if (ERROR == None):
                    return {
                        "response": "accept",
                        "error": "None",
                        "dat": {
                            "id": cheque_id,
                            "from_user_name": from_user_name,
                            "to_user_name": to_user_name,
                            "currency_name": currency_name
                        }
                    }
                else:
                    return {
                        "response": "deny",
                        "error": _ERROR(),
                        "dat": {}
                    }
            case "create_invoice":
                if (_CHECK_API_KEY(RequestJSON)):
                    invoice_id = createInvoice(RequestJSON["dat"]["currency"], RequestJSON["dat"]["currency_id"], RequestJSON["dat"]["amount"],
                                            RequestJSON["dat"]["from_user_id"], RequestJSON["dat"]["from_user_password"], RequestJSON["dat"]["recipient_user_id"])
                    to_user_name = None
                    from_user_name = None
                    if (validateUserID(RequestJSON["dat"]["recipient_user_id"]) and validateUserID(RequestJSON["dat"]["from_user_id"])):
                        to_user_name = USER_JSON["users"][RequestJSON["dat"]["recipient_user_id"]]["username"]
                        from_user_name = USER_JSON["users"][RequestJSON["dat"]["from_user_id"]]["username"]
                    else:
                        callError("Invalid User ID(s)")
                    currency_name = "-!ERROR!-"
                    if (RequestJSON["dat"]["currency"] == Currency.COIN.value):
                        if (validateCurrency(Currency.COIN.value, RequestJSON["dat"]["currency_id"])):
                            currency_name = COIN_JSON["ids"][RequestJSON["dat"]["currency_id"]]["name"]
                        else:
                            callError("Requests: Invalid Currency (coin) ID")
                    elif (RequestJSON["dat"]["currency"] == Currency.RESOURCE.value):
                        if (validateCurrency(Currency.RESOURCE.value, RequestJSON["dat"]["currency_id"])):
                            currency_name = RESOURCE_JSON["ids"][RequestJSON["dat"]["currency_id"]]["item_name"]
                        else:
                            callError("Requests: Invalid Currency (resource) ID")
                    else:
                        callError("Invalid Currency Types. FATAL ERROR! FATAL ERROR! SEND TO MARK! 0x2")
                if (ERROR == None):
                    return {
                        "response": "accept",
                        "error": "None",
                        "dat": {
                            "id": invoice_id,
                            "from_user_name": from_user_name,
                            "to_user_name": to_user_name,
                            "currency_name": currency_name
                        }
                    }
                else:
                    return {
                        "response": "deny",
                        "error": _ERROR(),
                        "dat": {}
                    }
            case "cash_cheque":
                if (_CHECK_API_KEY(RequestJSON)):
                    cheque_status = cashCheque(RequestJSON["dat"]["cheque_id"], RequestJSON["dat"]["user_id"], RequestJSON["dat"]["password"])
                    
                    if (not cheque_status):
                        callError("Requests: Cheque Status False")
                    
                if (ERROR == None):
                    return {
                        "response": "accept",
                        "error": "None",
                        "dat": {}
                    }
                else:
                    return {
                        "response": "deny",
                        "error": _ERROR(),
                        "dat": {}
                    }
            case "pay_invoice":
                if (_CHECK_API_KEY(RequestJSON)):
                    invoice_status = pay_invoice(RequestJSON["dat"]["invoice_id"], RequestJSON["dat"]["user_id"], RequestJSON["dat"]["password"])
                    
                    if (not invoice_status):
                        callError("Requests: Invoice Status False")
                    
                if (ERROR == None):
                    return {
                        "response": "accept",
                        "error": "None",
                        "dat": {}
                    }
                else:
                    return {
                        "response": "deny",
                        "error": _ERROR(),
                        "dat": {}
                    }
            
            case "coin_json":
                
                if (ERROR == None):
                    return {
                        "response": "accept",
                        "error": "None",
                        "dat": COIN_JSON
                    }
                else:
                    return {
                        "response": "deny",
                        "error": _ERROR(),
                        "dat": {}
                    }
            
            case "resource_json":
                
                if (ERROR == None):
                    return {
                        "response": "accept",
                        "error": "None",
                        "dat": RESOURCE_JSON
                    }
                else:
                    return {
                        "response": "deny",
                        "error": _ERROR(),
                        "dat": {}
                    }
                    
            case "user_info":
                
                user_info = {}
                user_id = RequestJSON["dat"]["user_id"]
                
                if (validateCred(RequestJSON["dat"]["user_id"], RequestJSON["dat"]["password"])):
                    user_info = {
                        "user_id": user_id,
                        "username": USER_JSON["users"][user_id]["username"],
                        "resource_balances": USER_JSON["users"][user_id]["resource_balances"],
                        "coin_balances": USER_JSON["users"][user_id]["coin_balances"]
                    }
                else:
                    callError("Requests: Invalid User Credentials for User_Info")
                
                if (ERROR == None):
                    return {
                        "response": "accept",
                        "error": "None",
                        "dat": user_info
                    }
                else:
                    return {
                        "response": "deny",
                        "error": _ERROR(),
                        "dat": {}
                    }
                    
            case "coin_info":
                
                coin_info = {}
                coin_id = RequestJSON["dat"]["coin_id"]
                
                if (validateCurrency(Currency.COIN.value, coin_id)):
                    coin_info = COIN_JSON["ids"][coin_id]
                    coin_info["coin_id"] = coin_id
                else:
                    callError("Requests: Invalid Currency Credentials for Coin_Info")
                
                if (ERROR == None):
                    return {
                        "response": "accept",
                        "error": "None",
                        "dat": coin_info
                    }
                else:
                    return {
                        "response": "deny",
                        "error": _ERROR(),
                        "dat": {}
                    }
            
            case "resource_info":
                
                resource_info = {}
                resource_id = RequestJSON["dat"]["resource_id"]
                
                if (validateCurrency(Currency.RESOURCE.value, resource_id)):
                    resource_info = RESOURCE_JSON["ids"][resource_id]
                    resource_info["resource_id"] = resource_id
                else:
                    callError("Requests: Invalid Currency Credentials for Resource_Info")
                
                if (ERROR == None):
                    return {
                        "response": "accept",
                        "error": "None",
                        "dat": resource_info
                    }
                else:
                    return {
                        "response": "deny",
                        "error": _ERROR(),
                        "dat": {}
                    }
            
            case "request_login_token":
                
                token = None
                
                if (validateCred(RequestJSON["dat"]["user_id"], RequestJSON["dat"]["password"], True)):
                    token = createLoginToken(RequestJSON["dat"]["user_id"], RequestJSON["dat"]["password"])
                else:
                    callError("Requests: Invalid Credentials for Login Token Request")
                
                if (ERROR == None):
                    return {
                        "response": "accept",
                        "error": "None",
                        "dat": {
                            "token": token
                        }
                    }
                else:
                    return {
                        "response": "deny",
                        "error": _ERROR(),
                        "dat": {}
                    }
            
            case "make_user_account":
                
                user_id = None
                
                if (ACCOUNT_MAKING_ALLOWANCE):
                    user_id = createAccount(RequestJSON["dat"]["username"], RequestJSON["dat"]["password"])
                else:
                    callError("Requests: Making Accounts is Disabled At the Moment")
                
                if (ERROR == None):
                    return {
                        "response": "accept",
                        "error": "None",
                        "dat": {
                            "user_id": user_id
                        }
                    }
                else:
                    return {
                        "response": "deny",
                        "error": _ERROR(),
                        "dat": {}
                    }
                    
            case "create_coin":
                
                coin_id = None
                
                coin_id = create_coin(name=RequestJSON["dat"]["name"], price=RequestJSON["dat"]["price"], backing_resource_id=RequestJSON["dat"]["backing_resource_id"], initial_buy_amount=RequestJSON["dat"]["initial_mint"], user_id=RequestJSON["dat"]["user_id"], password=RequestJSON["dat"]["password"])
                
                if (coin_id == None):
                    callError("Requests: Coin ID is Null")
                
                if (ERROR == None):
                    return {
                        "response": "accept",
                        "error": "None",
                        "dat": {
                            "coin_id": coin_id
                        }
                    }
                else:
                    return {
                        "response": "deny",
                        "error": _ERROR(),
                        "dat": {}
                    }
            
            case "buy_coin":
                
                buy_coin(RequestJSON["dat"]["coin_id"], RequestJSON["dat"]["amount"], RequestJSON["dat"]["user_id"], RequestJSON["dat"]["password"])
                
                if (ERROR == None):
                    return {
                        "response": "accept",
                        "error": "None",
                        "dat": {}
                    }
                else:
                    return {
                        "response": "deny",
                        "error": _ERROR(),
                        "dat": {}
                    }
            
            case "sell_coin":
                
                sell_coin(RequestJSON["dat"]["coin_id"], RequestJSON["dat"]["amount"], RequestJSON["dat"]["user_id"], RequestJSON["dat"]["password"])
                
                if (ERROR == None):
                    return {
                        "response": "accept",
                        "error": "None",
                        "dat": {}
                    }
                else:
                    return {
                        "response": "deny",
                        "error": _ERROR(),
                        "dat": {}
                    }
            
            
            case _:
                return {
                        "response": "deny",
                        "error": "Invalid Request",
                        "dat": {}
                    }
                
                
                
    except Exception as e:
        callError(f"Request Error: {e}")
        return {
                "response": "deny",
                "error": _ERROR(),
                "dat": {}
            }



templates = Jinja2Templates(directory="../site/templates")
api_lock = Lock()

app = FastAPI()

app.mount("/static", StaticFiles(directory="../site/static"), name="static")

class RequestData(BaseModel):
    request: str
    dat: dict

class ResponseData(BaseModel):
    response:str
    dat: dict
    
unlimitedRequests = [
    
]

@app.post("/api")
def api(data: RequestData):
    api_lock.acquire()
    try:
        global ERROR
        ERROR = None

        jsonify = {"request": data.request, "dat": data.dat}
                
        logAppend("REQUEST RECIEVED: " + str(data), LogType.REGLOG)
        
        response = translateJavaRequests(jsonify)
        
        logAppend("RESPONSE: " + str(response), LogType.REGLOG)
        
        return response
    finally:
        api_lock.release()



@app.get("/", response_class=HTMLResponse)
def index_page(request: Request):
    return templates.TemplateResponse(
        "index.html",
        {"request": request}
    )


@app.get("/login", response_class=HTMLResponse)
def index_page(request: Request):
    return templates.TemplateResponse(
        "login.html",
        {"request": request}
    )

@app.get("/resource", response_class=HTMLResponse)
def index_page(request: Request):
    return templates.TemplateResponse(
        "resource.html",
        {"request": request}
    )

@app.get("/resources", response_class=HTMLResponse)
def index_page(request: Request):
    return templates.TemplateResponse(
        "resources.html",
        {"request": request}
    )


@app.get("/coins", response_class=HTMLResponse)
def index_page(request: Request):
    return templates.TemplateResponse(
        "coins.html",
        {"request": request}
    )

@app.get("/createcoin", response_class=HTMLResponse)
def index_page(request: Request):
    return templates.TemplateResponse(
        "createCoin.html",
        {"request": request}
    )
    
    
@app.get("/coin", response_class=HTMLResponse)
def index_page(request: Request):
    return templates.TemplateResponse(
        "coin.html",
        {"request": request}
    )