import { APICall, isLoggedIn } from "../core/main.js";

let user_account;
let resource_json;
let sufficentFunds = false;

function roundTs(number) {
    return Math.round(number * 1000) / 1000;
}

document.addEventListener("DOMContentLoaded", async () => {

    const user_id = localStorage.getItem("user_id");
    const token = localStorage.getItem("loginToken");

    const priceField = document.getElementById("coin_price");
    const mintAmountField = document.getElementById("mint_amount");
    const backingResourceField = document.getElementById("backing_resource_select");

    const submit = document.getElementById("createCoinForm");

    submit.addEventListener("submit", handleCreate);


    priceField.addEventListener("input", update);
    mintAmountField.addEventListener("input", update);
    backingResourceField.addEventListener("input", update);
    update();

    if (!isLoggedIn()) {
        window.location.replace("/");
    }

    user_account = await APICall(
        {
            "request": "user_info",
            "dat": {
                "user_id": user_id,
                "password": token
            }
        }
    );

    resource_json = await APICall(
        {
            "request": "resource_json",
            "dat": {}
        }
    );

    const form = document.getElementById("createCoinForm");

    form.addEventListener("submit", () => {
        console.log("E");
    });

    init();
});

function update() { 
    const totalCost = document.getElementById("totalCost"); 
    const priceField = document.getElementById("coin_price"); 
    const backingResourceField = document.getElementById("backing_resource_select");
    const mintAmountField = document.getElementById("mint_amount"); 
    const logField = document.getElementById("log");
    const costToCreate = Math.round(priceField.value*mintAmountField.value*1000)/1000;
    const submitButton = document.getElementById("submitButton");

    if (costToCreate <= 0) {
        totalCost.innerText = "Cannot Create This Coin";
        logField.innerText = "Invalid Values For Coin";
        logField.classList.remove("hidden"); 
        submitButton.classList.add("hidden");
    } else {
        if (costToCreate > user_account.dat.resource_balances[backingResourceField.value]) {
            totalCost.innerText = `${costToCreate} ${resource_json.dat.ids[backingResourceField.value].item_name}`;
            logField.innerText = "INSUFFICENT FUNDS";
            logField.classList.remove("hidden"); 
            submitButton.classList.add("hidden");
            sufficentFunds = false;
        } else {
            totalCost.innerText = `${costToCreate} ${resource_json.dat.ids[backingResourceField.value].item_name}`;
            logField.innerText = "Logger";
            logField.classList.add("hidden");
            submitButton.classList.remove("hidden");
            sufficentFunds = true; 
        }
    }
}

async function init() {
    
    const backing_resource_select = document.getElementById("backing_resource_select");


    if (user_account.response == "accept" && user_account.error == "None") {

        for (const [key, value] of Object.entries(user_account.dat.resource_balances)) {
            if (value <= 0) {
                continue;
            }


            const resource = resource_json.dat.ids[key];
            console.log(resource);

            const option = document.createElement("option");
            option.value = key;
            option.textContent = `${resource.item_name} (${resource.item_id}) - BALANCE: ${roundTs(Number(user_account.dat.resource_balances[key]))}`;

            backing_resource_select.appendChild(option);


        }


    }

}

async function handleCreate(event) {
    event.preventDefault();

    console.log(sufficentFunds);

    if (sufficentFunds) {
        const priceField = document.getElementById("coin_price"); 
        const backingResourceField = document.getElementById("backing_resource_select");
        const mintAmountField = document.getElementById("mint_amount"); 
        const nameField = document.getElementById("coin_name");
        const user_id = localStorage.getItem("user_id");
        const token = localStorage.getItem("loginToken");

        const response = await APICall(
            {
                "request": "create_coin",
                "dat": {
                    "name": nameField.value,
                    "price": parseInt(priceField.value, 10),
                    "backing_resource_id": backingResourceField.value,
                    "initial_mint": parseInt(mintAmountField.value, 10),
                    "user_id": user_id,
                    "password": token
                }
            }
        );

        console.log(response);

        if (response.response == "accept" && response.error == "None") {
            window.location.replace(`/coin?id=${response.dat.coin_id}`);
        } else {
            const logField = document.getElementById("log");
            logField.innerText = `Error: ${response.error}`;
            logField.classList.remove("hidden"); 
        }
    } else {
        const logField = document.getElementById("log");
        logField.innerText = `INSUFFICENT FUNDS FOR CREATION`;
        logField.classList.remove("hidden"); 
    }
}