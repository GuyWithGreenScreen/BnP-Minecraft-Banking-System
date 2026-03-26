export async function APICall(request) {

    const response = await fetch("/api", 
        {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify(request)
        }
    );

    const data = await response.json();
    return data;
}

function isValid(input) {
    return /^[a-zA-Z0-9]+$/.test(input);
}

export function callErrorAlert(message) {
    alert.call("! ERROR: " + message);
}

export function isLoggedIn() {
    const user_id = localStorage.getItem("user_id");
    const token = localStorage.getItem("loginToken");

    if (user_id && token) {
        return true;
    }
    return false;
}

export async function getUserInfo(user_id, password) {
    const response = await APICall(
        {
            "request": "user_info",
            "dat": {
                "user_id": user_id,
                "password": password
            }
        }
    );

    return response;
}

export async function getResourceInfo(resource_id) {
    const response = await APICall(
        {
            "request": "resource_info",
            "dat": {
                "resource_id": resource_id
            }
        }
    );

    return response;
}

export async function getResources() {
    const response = await APICall(
        {
            "request": "resource_json",
            "dat": {}
        }
    );

    return response;
}

export async function getCoinInfo(coin_id) {
    const response = await APICall(
        {
            "request": "coin_info",
            "dat": {
                "coin_id": coin_id
            }
        }
    );

    return response;
}
