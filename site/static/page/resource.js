import { APICall, isLoggedIn, getUserInfo, getResourceInfo, callErrorAlert } from "../core/main.js";

document.addEventListener("DOMContentLoaded", () => {
    init();
});

function roundTs(number) {
    return Math.round(number * 1000) / 1000;
}

async function init() {
    const params = new URLSearchParams(window.location.search);
    const resourceID = params.get("id");


    if (resourceID) {
        console.log(resourceID);
        
        const resourceInfo = await getResourceInfo(resourceID);

        if (resourceInfo.response == "accept" && resourceInfo.error == "None") {
            const el_itemName = document.getElementById("itemName");
            const el_itemID = document.getElementById("itemID");
            const el_currencyID = document.getElementById("currencyID");
            const el_globalMinted = document.getElementById("globalMinted");
            const el_userBalance = document.getElementById("userBalance");
            const el_label = document.getElementById("topLabel");

            el_itemName.textContent = resourceInfo.dat.item_name;
            el_itemID.textContent = resourceInfo.dat.item_id;
            el_currencyID.textContent = "ID: " + resourceID;
            el_globalMinted.textContent = "Global Minted: " + roundTs(Number(resourceInfo.dat.minted_amount));
            el_label.textContent = `${resourceInfo.dat.item_name} - ${roundTs(Number(resourceInfo.dat.minted_amount))}`;

            console.log(resourceInfo);

            const canvas = document.getElementById("itemChart");
            const ctx = canvas.getContext("2d");

            const history = resourceInfo.dat.history; // your JSON array
            console.log(Chart._adapters._date);
            const labels = history.map(p => p.time);
            const values = history.map(p => p.amount);
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
                
                if (userInfo.response == "accept" && userInfo.error == "None") {
                    if (userInfo.dat.resource_balances[resourceID] == null) {
                        el_userBalance.textContent = "Your Balance: 0 " + resourceInfo.dat.item_name;
                    } else {
                        el_userBalance.textContent = "Your Balance: " + roundTs(Number(userInfo.dat.resource_balances[resourceID]));
                    }
                } else {
                    el_userBalance.textContent = "An error has occured trying to log in. Please send to Mark: " + userInfo.error;
                }

            } else {
                el_userBalance.textContent = "Your Balance: Please Log In";
                document.getElementById("loginButton").classList.remove("hidden");
            }

        } else {
            console.log(resourceInfo.error);
            window.location.replace("/");
        }

    } else {
        window.location.replace("/");
    }
}