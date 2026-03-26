import { APICall, isLoggedIn, getUserInfo, getCoinInfo, callErrorAlert, getResourceInfo } from "../core/main.js";

let el_error;
let coinInfo;

document.addEventListener("DOMContentLoaded", () => {
    const submit = document.getElementById("purchaseSellForm");

    const amount = document.getElementById("amount");

    amount.addEventListener("input", updateCost);


    el_error = document.getElementById("error");

    submit.addEventListener("submit", purchaseSell);
    
    init();
});

function roundTs(number) {
    return Math.round(number * 1000) / 1000;
}

function getCost(amount) {
    if (coinInfo) {
        return (2*amount*coinInfo.dat.backing)/(2*coinInfo.dat.initial_buy - amount);
    }
    return NaN;
}

function updateCost() {
    const input = Number(document.getElementById("amount").value);
    const cost = document.getElementById("cost");

    if (coinInfo) {
        cost.textContent = `Cost: ${getCost(input)}`;
    }

}

async function purchaseSell(event) {
    event.preventDefault();
    const params = new URLSearchParams(window.location.search);
    const coinID = params.get("id");
    const input = Number(document.getElementById("amount").value);
    const user_id = localStorage.getItem("user_id");
    const token = localStorage.getItem("loginToken");

    if (Number.isNaN(input)) {
        el_error.textContent = "Error: Invalid Amount";
        el_error.classList.remove("hidden");
        return;
    }

    if (event.submitter.id == "buyButton") {
        const response = await APICall(
        {
            "request": "buy_coin",
            "dat": {
                "coin_id": coinID,
                "amount": input,
                "user_id": user_id,
                "password": token
            }
        });

        if (response.response == "accept" && response.error == "None") {
            location.reload();
        } else {
            el_error.textContent = "Error: " + response.error;
            el_error.classList.remove("hidden");
        }
    
    } else if (event.submitter.id == "sellButton") {
        const response = await APICall(
        {
            "request": "sell_coin",
            "dat": {
                "coin_id": coinID,
                "amount": input,
                "user_id": user_id,
                "password": token
            }
        });

        if (response.response == "accept" && response.error == "None") {
            location.reload();
        } else {
            el_error.textContent = "Error: " + response.error;
            el_error.classList.remove("hidden");
        }
    } else {
        el_error.textContent = "Error: Unknown Button Press";
        el_error.classList.remove("hidden");
    }

}


async function init() {
    const params = new URLSearchParams(window.location.search);
    const coinID = params.get("id");



    if (coinID) {
        console.log(coinID);
        
        coinInfo = await getCoinInfo(coinID);
        const backingResourceInfo = await getResourceInfo(coinInfo.dat.backing_res_id);

        if ((coinInfo.response == "accept" && coinInfo.error == "None")
            && (backingResourceInfo.response == "accept" && backingResourceInfo.error == "None")) {
            const coinName = document.getElementById("coinName");
            const el_coinID = document.getElementById("coinID");
            const backedBy = document.getElementById("backedBy");
            const globalMinted = document.getElementById("globalMinted");
            const el_price = document.getElementById("price");
            const userBalance = document.getElementById("userBalance");
            const el_label = document.getElementById("topLabel");
            const backing_link = document.getElementById("backingElement");
            const price = Math.round(coinInfo.dat.backing/coinInfo.dat.initial_buy * 1000) / 1000;

            const el_resourceBalance = document.getElementById("resourceBalance");


            coinName.textContent = "$" + coinInfo.dat.name;
            el_coinID.textContent = "Coin ID: " + coinID;
            backedBy.textContent = `Backing Resource: ${backingResourceInfo.dat.item_name} (${coinInfo.dat.backing_res_id})`;
            globalMinted.textContent = "Global Minted: " + coinInfo.dat.minted_amount;
            el_label.textContent = `$${coinInfo.dat.name} - ${price} ${backingResourceInfo.dat.item_name}`;
            el_price.textContent = `Price: ${price} ${backingResourceInfo.dat.item_name}`;
            backing_link.href = `/resource?id=${coinInfo.dat.backing_res_id}`;

            console.log(coinInfo);

            const canvas = document.getElementById("itemChart");
            const ctx = canvas.getContext("2d");

            const history = coinInfo.dat.history; // your JSON array
            console.log(Chart._adapters._date);
            const labels = history.map(p => p.time);
            const values = history.map(p => p.price);
            labels.push(Date.now()/1000);
            values.push(values[values.length - 1]);
            const timestamps = labels;
            new Chart(ctx, {
                type: "line",
                data: {
                    datasets: [{
                        data: timestamps.map((t, i) => ({
                            x: t*1000,
                            y: values[i]
                        })),
                        stepped: "before",
                        borderWidth: 2,
                        pointRadius: 0,
                        pointHitRadius: 15,
                        tension: 0
                    }]
                },
                options: {
                    animation: false,
                    responsive: false,

                    interaction: {
                        mode: "nearest",
                        intersect: false,
                        axis: "x"
                    },

                    plugins: {
                        legend: { display: false },
                        tooltip: {
                            mode: "nearest",
                            intersect: false
                        }
                    },

                    scales: {
                        x: {
                            type: "time",
                            time: {
                                tooltipFormat: "yyyy-MM-dd HH:mm"
                            }
                        },
                        y: {
                            position: "right"
                        }
                    }
                }
            });

            if (isLoggedIn()) {
                const userInfo = await getUserInfo(localStorage.getItem("user_id"), localStorage.getItem("loginToken"));
                el_resourceBalance.classList.remove("hidden");

                
                if (userInfo.response == "accept" && userInfo.error == "None") {
                    if (userInfo.dat.coin_balances[coinID] == null) {
                        userBalance.textContent = "0" + " $" + coinInfo.dat.name;
                    } else {
                        userBalance.textContent = userInfo.dat.coin_balances[coinID] + " $" + coinInfo.dat.name;
                    }
                    document.getElementById("optionButtons").classList.remove("hidden");
                    document.getElementById("purchaseSellForm").classList.remove("hidden");
                } else {
                    userBalance.textContent = "An error has occured trying to log in. Please send to Mark: " + userInfo.error;
                }
                
                if (coinInfo.dat.backing_res_id in userInfo.dat.resource_balances) {
                    el_resourceBalance.textContent = `${roundTs(userInfo.dat.resource_balances[coinInfo.dat.backing_res_id])} ${backingResourceInfo.dat.item_name}`;
                } else {
                    el_resourceBalance.textContent = `0 ${backingResourceInfo.dat.item_name}`;
                }

            } else {
                userBalance.textContent = "Please Log In to See Balance and Buy/Sell";
                document.getElementById("loginButton").classList.remove("hidden");
            }

        } else {
            console.log(coinInfo.error);
            window.location.replace("/");
        }

    } else {
        window.location.replace("/");
    }
}