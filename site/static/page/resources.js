import { APICall } from "../core/main.js";


document.addEventListener("DOMContentLoaded", () => {
    init();
});

function roundTs(number) {
    return Math.round(number * 1000) / 1000;
}

async function init() {

    const grid = document.querySelector(".resourceGrid");
    const baseItem = grid.querySelector(".gridItem");

    const response = await APICall(
        {
            "request": "resource_json",
            "dat": {}
        }
    );

    if (response.error == "None" && response.response == "accept") {
            
        for (const [key, value] of Object.entries(response.dat.ids)) {
            if (value <= 0) {
                continue;
            }
            const clone = baseItem.cloneNode(true);

            clone.querySelector(".itemName").textContent = value.item_name;
            clone.querySelector(".itemID").textContent = value.item_id;
            clone.querySelector(".currencyID").textContent = "ID: " + key;
            clone.querySelector(".globalMinted").textContent = "Global Minted: " + roundTs(Number(value.minted_amount));
            clone.querySelector(".gridLink").href = `/resource?id=${key}`;
            clone.classList.remove("hidden");

            grid.appendChild(clone);
            const canvas = clone.querySelector(".itemChart");
            const ctx = canvas.getContext("2d");

            const history = value.history; // your JSON array
            
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
    }



}