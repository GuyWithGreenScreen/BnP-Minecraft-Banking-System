import { APICall } from "../core/main.js";


document.addEventListener("DOMContentLoaded", () => {
    init();
});
function roundTs(number) {
    return Math.round(number * 1000) / 1000;
}

function callLogin() {
    const el_user_id = document.getElementById("userid");
    const el_username = document.getElementById("username");
    const el_login_button = document.getElementById("loginButton");

    el_username.textContent = "Please Log In";
    el_user_id.textContent = "USER ID: NULL";
    el_login_button.classList.remove("hidden");
}

async function init() {
    const user_id = localStorage.getItem("user_id");
    const token = localStorage.getItem("loginToken");

    console.log("E");

    if (user_id && token) {
        const response = await APICall(
            {
                "request": "user_info",
                "dat": {
                    "user_id": user_id,
                    "password": token
                }
            }
        );

        console.log(response);

        const resourceJSON = await APICall(
            {
                "request": "resource_json",
                "dat": {}
            }
        );
        const coinJSON = await APICall(
            {
                "request": "coin_json",
                "dat": {}
            }
        );
        if (
            (response.error == "None" && response.response == "accept") && 
            (resourceJSON.error == "None" && resourceJSON.response == "accept") && 
            (coinJSON.error == "None" && coinJSON.response == "accept")
            ) {
            console.log("sus");

            const el_user_id = document.getElementById("userid");
            const el_username = document.getElementById("username");
            const el_portfolio = document.getElementById("portfolio");
            await new Promise(r => requestAnimationFrame(r));
            el_user_id.textContent = "USER ID: " + response.dat.user_id;
            el_username.textContent = response.dat.username;
            el_portfolio.classList.remove("hidden");


            const grid = document.querySelector(".resourceGrid");
            const baseItem = grid.querySelector(".gridItem");
            console.log(response.dat.resource_balances)

            
            for (const [key, value] of Object.entries(response.dat.resource_balances)) {
                if (value <= 0) {
                    continue;
                }
                const clone = baseItem.cloneNode(true);


                clone.querySelector(".itemName").textContent = resourceJSON.dat.ids[key].item_name;
                clone.querySelector(".itemID").textContent = resourceJSON.dat.ids[key].item_id;
                clone.querySelector(".currencyID").textContent = "ID: " + key;
                clone.querySelector(".globalMinted").textContent = "Global Minted: " + roundTs(Number(resourceJSON.dat.ids[key].minted_amount));
                clone.querySelector(".itemBalance").textContent = "Balance: " + roundTs(Number(value));
                clone.querySelector(".gridLink").href = `/resource?id=${key}`;

                grid.appendChild(clone);
                const canvas = clone.querySelector(".itemChart");
                const ctx = canvas.getContext("2d");

                const history = resourceJSON.dat.ids[key].history; // your JSON array
                
                console.log(key, history)

                const labels = history.map(p => p.time);
                const values = history.map(p => p.amount);
                labels.push(Date.now()/1000);
                values.push(values[values.length - 1]);


                new Chart(ctx, {
                    type: "line",
                    data: {
                        labels,
                        datasets: [{
                            data: values,
                            borderWidth: 2,
                            pointRadius: 0,
                            tension: 0.3
                        }]
                    },
                    options: {
                        responsive: false,
                        maintainAspectRatio: false,
                        animation: false,
                        plugins: {
                            legend: { display: false }
                        },
                        scales: {
                            x: { display: false },
                            y: { display: false }
                        }
                    }
                });

            }

            baseItem.remove();

            const grid2 = document.querySelector(".coinGrid");
            const baseItem2 = grid2.querySelector(".coinGridItem");
            console.log(response.dat.coin_balances)

            
            for (const [key, value] of Object.entries(response.dat.coin_balances)) {
                if (value <= 0) {
                    continue;
                }
                const clone = baseItem2.cloneNode(true);

                console.log(resourceJSON.dat.ids);
                console.log(coinJSON.dat.ids[key]);
                const backedItemName = resourceJSON.dat.ids[coinJSON.dat.ids[key].backing_res_id].item_name;
                const backedItemID = resourceJSON.dat.ids[coinJSON.dat.ids[key].backing_res_id].item_id;


                clone.querySelector(".coinLink").href = `/coin?id=${key}`;
                clone.querySelector(".coinName").textContent = "$" + coinJSON.dat.ids[key].name;
                clone.querySelector(".backingItemID").textContent = `Backed by: ${backedItemID} (${coinJSON.dat.ids[key].backing_res_id})`;
                clone.querySelector(".coinID").textContent = "Coin ID: " + key;
                clone.querySelector(".coinPrice").textContent = `Price: ${Math.round(coinJSON.dat.ids[key].backing/coinJSON.dat.ids[key].initial_buy * 1000) / 1000} ${backedItemName}`;
                //clone.querySelector(".gridLink").href = `/coin?id=${key}`;

                grid2.appendChild(clone);
                const canvas = clone.querySelector(".coinChart");
                const ctx = canvas.getContext("2d");

                const history = coinJSON.dat.ids[key].history; // your JSON array
                
                console.log(key, history)

                const labels = history.map(p => p.time);
                const values = history.map(p => p.price);
                labels.push(Date.now()/1000);
                values.push(values[values.length - 1]);

                new Chart(ctx, {
                    type: "line",
                    data: {
                        labels,
                        datasets: [{
                            data: values,
                            borderWidth: 2,
                            pointRadius: 0,
                            tension: 0.3
                        }]
                    },
                    options: {
                        responsive: false,
                        maintainAspectRatio: false,
                        animation: false,
                        plugins: {
                            legend: { display: false }
                        },
                        scales: {
                            x: { display: false },
                            y: { display: false }
                        }
                    }
                });

            }

            baseItem2.remove();


        } else {
            callLogin();
        }
    } else {
        callLogin();
    }
}