import { APICall } from "../core/main.js";


document.addEventListener("DOMContentLoaded", () => {
    init();
});


async function init() {




    const grid2 = document.querySelector(".coinGrid");
    const baseItem2 = grid2.querySelector(".coinGridItem");
    
    const response = await APICall(
        {
            "request": "coin_json",
            "dat": {}
        }
    );

    const resourceJSON = await APICall(
        {
            "request": "resource_json",
            "dat": {}
        }
    );

    if (
        (response.error == "None" && response.response == "accept") && 
        (resourceJSON.error == "None" && resourceJSON.response == "accept")) {
            
        
        console.log(response.dat.ids)


        
        for (const [key, value] of Object.entries(response.dat.ids)) {
            if (value <= 0) {
                continue;
            }
            const clone = baseItem2.cloneNode(true);



            const backedItemName = resourceJSON.dat.ids[value.backing_res_id].item_name;
            const backedItemID = resourceJSON.dat.ids[value.backing_res_id].item_id;


            clone.querySelector(".coinLink").href = `/coin?id=${key}`;
            clone.querySelector(".coinName").textContent = "$" + value.name;
            clone.querySelector(".backingItemID").textContent = `Backed by: ${backedItemID} (${value.backing_res_id})`;
            clone.querySelector(".coinID").textContent = "Coin ID: " + key;
            clone.querySelector(".coinPrice").textContent = `Price: ${Math.round(value.backing/value.initial_buy * 1000) / 1000} ${backedItemName}`;
            clone.classList.remove("hidden");
            //clone.querySelector(".gridLink").href = `/coin?id=${key}`;

            grid2.appendChild(clone);
            const canvas = clone.querySelector(".coinChart");
            const ctx = canvas.getContext("2d");

            const history = value.history; // your JSON array
            
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
    }

}