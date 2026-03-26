# FEATURES

## USERS CAN:
```
1. Make Account
2. Mint Almost Any Resources
3. Unmint Resources
4. Deposit Minted Resources
5. Withdrawl Minted Resources
6. Make Coins
7. Buy Coins
8. Sell Coins
```

## COINS:
```
CREATION:
How Much Coins To Mint: _____
Price Of Coin: _____
Initial Buy Amount: _____

Then math is:
Price = Backing / Initial Buy Amount


FEATURES:
Have to be backed by a resource
```

## API HANDLES:
```
STORAGE:
-USER JSON FILE
-COIN JSON FILE
-RESOURCE JSON FILE
-ORDER/CHEQUE/INVOICE FILE

COMMANDS:
createAccount(username, pass)

createLoginToken(username, pass)

validateCred(user_id, password)

mint(item_id, amount)

unmint(item_id, amount)

depositCurrency(currency(coin/resource), currency_id, amount, user_id)

cashCheque(cheque_id, user_id)

withdraw(currency(coin/resource), currency_id, amount, user_id, password)

createCheque(currency(coin/resource), currency_id, amount, from_user_id, from_user_pass, to_user_id)

createInvoice(currency(coin/resource), currency_id, amount, payee, payer)

pay_invoice(invoice_id, user_id, password)

create_coin(backing_resource_id, mint_amount, price, initial_buy_amount)

buy_coin(coin_id, amount, user_id, password)

sell_coin(coin_id, amount, user_id, password)

```

## JAVA MOD COMMANDS:
```
/mint [amount] - Mints the amount of whatever the player is holding

/unmint [amount] - Unmints the amount of minted resource the player is 
holding

/deposit [amount] [user_id] - Deposits the amount of whatever currency the player is holding to the user_id account

/withdraw [coin/resource] [currency_id] [amount] [user_id] [password] - Withdraws the amount of the currency

/create_cheque [coin/resource] [currency_id] [amount] [from_user_id] [from_user_password] [to_user_id]

/create_invoice [coin/resource] [currency_id] [amount] [from_user_id] [from_user_password] [recipient_user_id]

/cash_cheque [user_id] [password] - Deposits the cheque the user is holding to the user_id account

/pay_invoice [user_id] [password] - Pays the currently held invoice
```

## JSONS

### COIN JSON:
```
"ids": {
    "XXXX": {
        "name": "shit-coin",
        "backing": 6767,
        "backing_res_id": "XXXX",
        "creator_id": "XXXX",
        "minted_amount": 67,
        "initial_buy": 69,
        "history": [
            {
                "time": SINCE_EPOCH,
                "backing": 69,
                "price": 32
            },
            ...
        ]
    }, ....
}
```

### USER JSON:
```
"users": {
    "XXXX": {
        "username": "pikasour67",
        "pass": "argon$ojgeid$ffhehfe",
        "login_tokens": [],
        "resource_balances": {
            "XXXX": 696969,
            ...
        },
        "coin_balances": {
            "XXXX": 676767,
            ...
        }
    }, ...
}
```

### RESOURCE JSON:
```
"ids": {
    "XXXX": {
        "item_id": "minecraft:iron_ingot",
        "item_name": "Iron Ingot",
        "minted_amount": 6969,
        "history": [
            {
                "time": SINCE_EPOCH,
                "amount": 67
            },
            ...
        ]
    }, ....
},
"items": {
    "minecraft:iron_ingot": XXXX,
    ...
}
```


### CHEQUE/INVOICE JSON:
```
"ids": {
    "XXXXXXXX": {
        "type": 1, #CHEQUE(1)/INVOICE(2)
        "to_user": "XXXX", # Payer
        "from_user": "XXXX", # Payee
        "currency_type": 1, #COIN(1)/RESOURCE(2),
        "currency_id": "XXXX"
        "amount": 67
    }, ...
}
```

---
## NETWORKING REQUESTS

All IDs are temporary on both sides and get cleared once request is complete.
IDs are for syncing only.

### REQUESTS
"Error in Transaction, please send to Mark: "


### CREATE ACOUNT
> REQUEST
```
{
    "request": "create_account",
    "dat": {
        "username": "sus",
        "password": "sussy"
    }
}
```

> RESPONSE
```
{
    "response": "accept" / "deny",
    "error": "None" / "X",
    "dat": {
        "user_id": "XXXX"
    }
}
```

### CREATE LOGIN TOKEN
> REQUEST
```
{
    "request": "create_token",
    "dat": {
        "username": "sus",
        "password": "sussy"
    }
}
```

> RESPONSE
```
{
    "response": "accept" / "deny",
    "error": "None" / "X",
    "dat": {
        "token_id": "XXXX"
    }
}
```

### REQUEST COIN JSON
> REQUEST
```
{
    "request": "coin_json",
    "dat": {}
}
```

> RESPONSE
```
{
    "response": "accept" / "deny",
    "error": "None" / "X",
    "dat": {
        {
            "ids": {
                ...
            }
            ### COIN JSON
        }
    }
}
```

### REQUEST RESOURCE JSON
> REQUEST
```
{
    "request": "resource_json",
    "dat": {}
}
```

> RESPONSE
```
{
    "response": "accept" / "deny",
    "error": "None" / "X",
    "dat": {
        {
            "ids": {
                ...
            },
            "items": {
                ...
            }
            ### RESOURCE JSON
        }
    }
}
```

### MINT
> REQUEST
```
{
    "request": "mint",
    "dat": {
        "amount": 67,
        "item_id": "minecraft:iron_ingot",
        "item_name": "Iron Ingot"
    }
}
```
> RESPONSE
```
{
    "response": "accept" / "deny",
    "error": "None" / "X",
    "dat": {
        "resource_id": "XXXX"
    }
}
```

### UNMINT
> REQUEST
```
{
    "request": "unmint",
    "dat": {
        "amount": 67,
        "resource_id": "XXXX"
    }
}
```
> RESPONSE
```
{
    "response": "accept" / "deny",
    "error": "None" / "X",
    "dat": {}
}
```

### DEPOSIT CURRENCY
> REQUEST
```
{
    "request": "deposit",
    "dat": {
        "currency": 1, # 1 = COIN 2 = RESOURCE
        "amount": 67,
        "currency_id": "XXXX",
        "user_id": "XXXX"
    }
}
```
> RESPONSE
```
{
    "response": "accept" / "deny",
    "error": "None" / "X",
    "dat": {}
}
```

### WITHDRAW CURRENCY
> REQUEST
```
{
    "request": "withdraw",
    "dat": {
        "currency": 1, # 1 = COIN 2 = RESOURCE
        "amount": 67,
        "currency_id": "XXXX",
        "user_id": "XXXX",
        "password": "X"
    }
}
```

> RESPONSE
```
{
    "response": "accept" / "deny",
    "error": "None" / "X",
    "dat": {
        "name": "minecraft:iron_ingot" / "cockaine" # name or item_id
    }
}
```

### CREATE CHEQUE
> REQUEST
```
{
    "request": "create_cheque",
    "dat": {
        "currency": 1, # 1 = COIN 2 = RESOURCE
        "currency_id": "XXXX",
        "amount": 67,
        "from_user_id": "XXXX",
        "from_user_password": "X",
        "to_user_id": "XXXX"
    }
}
```

> RESPONSE
```
{
    "response": "accept" / "deny",
    "error": "None" / "X",
    "dat": {
        "id": "XXXXXXXX",
        "from_user_name": "X",
        "to_user_name": "X",
        "currency_name": "minecraft:iron_ingot" / "$Cockcaine"
    }
}
```

### CREATE INVOICE
> REQUEST
```
{
    "request": "create_invoice",
    "dat": {
        "currency": 1, # 1 = COIN 2 = RESOURCE
        "currency_id": "XXXX",
        "amount": 67,
        "from_user_id": "XXXX",
        "from_user_password": "X",
        "recipient_user_id": "XXXX"
    }
}
```

> RESPONSE
```
{
    "response": "accept" / "deny",
    "error": "None" / "X",
    "dat": {
        "id": "XXXXXXXX",
        "from_user_name": "X",
        "to_user_name": "X",
        "currency_name": "minecraft:iron_ingot" / "$Cockcaine"
    }
}
```

### CASH CHEQUE
> REQUEST
```
{
    "request": "cash_cheque",
    "dat": {
        "cheque_id": "XXXXXXXX",
        "user_id": "XXXX",
        "password": "X"
    }
}
```

> RESPONSE
```
{
    "response": "accept" / "deny",
    "error": "None" / "X",
    "dat": {}
}
```

### PAY INVOICE
> REQUEST
```
{
    "request": "pay_invoice",
    "dat": {
        "invoice_id": "XXXXXXXX",
        "user_id": "XXXX",
        "password": "X"
    }
}
```

> RESPONSE
```
{
    "response": "accept" / "deny",
    "error": "None" / "X",
    "dat": {}
}
```

### REQUEST USER INFO
> REQUEST
```
{
    "request": "user_info",
    "dat": {
        "user_id": "XXXX",
        "password": "X"
    }
}
```

> RESPONSE
```
{
    "response": "accept" / "deny",
    "error": "None" / "X",
    "dat": {
        "user_id": "XXXX",
        "username": "pikasour67",
        "resource_balances": {
            "XXXX": 696969,
            ...
        },
        "coin_balances": {
            "XXXX": 676767,
            ...
        }
    }
}
```

### REQUEST COIN INFO
> REQUEST
```
{
    "request": "coin_info",
    "dat": {
        "coin_id": "XXXX"
    }
}
```

> RESPONSE
```
{
    "response": "accept" / "deny",
    "error": "None" / "X",
    "dat": {
        "name": "shit-coin",
        "backing": 6767,
        "backing_res_id": "XXXX",
        "creator_id": "XXXX",
        "minted_amount": 67,
        "initial_buy": 69,
        "history": [
            {
                "time": SINCE_EPOCH,
                "backing": 69,
                "price": 32
            },
            ...
        ]
    }
}
```

### REQUEST RESOURCE INFO
> REQUEST
```
{
    "request": "resource_info",
    "dat": {
        "resource_id": "XXXX"
    }
}
```

> RESPONSE
```
{
    "response": "accept" / "deny",
    "error": "None" / "X",
    "dat": {
        "item_id": "minecraft:iron_ingot",
        "item_name": "Iron Ingot",
        "minted_amount": 6969,
        "history": [
            {
                "time": SINCE_EPOCH,
                "amount": 67
            },
            ...
        ]
    }
}
```

### REQUEST USER LOGIN TOKEN
> REQUEST
```
{
    "request": "request_login_token",
    "dat": {
        "user_id": "XXXX",
        "password": "X"
    }
}
```

> RESPONSE
```
{
    "response": "accept" / "deny",
    "error": "None" / "X",
    "dat": {
        "token": "XXXXXXXXXXXXXXXX"
    }
}
```

### MAKE USER ACCOUNT
> REQUEST
```
{
    "request": "make_user_account",
    "dat": {
        "username": "X",
        "password": "X"
    }
}
```

> RESPONSE
```
{
    "response": "accept" / "deny",
    "error": "None" / "X",
    "dat": {
        "user_id": "XXXX"
    }
}
```

### CREATE COIN
> REQUEST
```
{
    "request": "create_coin",
    "dat": {
        "name": "X",
        "price": 67,
        "backing_resource_id": "XXXX",
        "initial_mint": 69,
        "user_id": "X",
        "password": "X"
    }
}
```

> RESPONSE
```
{
    "response": "accept" / "deny",
    "error": "None" / "X",
    "dat": {
        "coin_id": "XXXX"
    }
}
```

### BUY COIN
> REQUEST
```
{
    "request": "buy_coin",
    "dat": {
        "coin_id": BEEF,
        "amount": 67,
        "user_id": XXXX,
        "password": X
    }
}
```

> RESPONSE
```
{
    "response": "accept" / "deny",
    "error": "None" / "X",
    "dat": {}
}
```

### SELL COIN
> REQUEST
```
{
    "request": "sell_coin",
    "dat": {
        "coin_id": BEEF,
        "amount": 67,
        "user_id": XXXX,
        "password": X
    }
}
```

> RESPONSE
```
{
    "response": "accept" / "deny",
    "error": "None" / "X",
    "dat": {}
}
```