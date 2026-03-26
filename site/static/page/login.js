import { APICall } from "../core/main.js";

document.addEventListener("DOMContentLoaded", () => {
    const login = document.getElementById("loginForm");
    const create = document.getElementById("makeAccountForm");

    login.addEventListener("submit", handleLogin);
    create.addEventListener("submit", handleCreate);
    init();
});

function isValid(input) {
    return /^[A-Za-z0-9_]{3,16}$/.test(input);
}

function callError(message) {
    const el = document.getElementById("error");
    el.innerText = "Error: " + message;
    el.classList.remove("hidden");
}

async function handleLogin(event) {
    event.preventDefault();

    const user_id = document.getElementById("userid").value.toUpperCase();
    const password = document.getElementById("loginPassword").value;

    console.log(JSON.stringify(user_id))

    if (user_id && password) {
        if (isValid(user_id)) {
            const response = await APICall(
                {
                    "request": "request_login_token",
                    "dat": {
                        "user_id": user_id,
                        "password": password
                    }
                }
            );

            if (response.response == "accept" && response.error == "None") {
                localStorage.setItem("user_id", user_id);
                localStorage.setItem("loginToken", response.dat.token);
                window.location.replace("/");
            } else {
                callError(response.error);
            }

        } else {
            callError("Invalid Username (NO space or special characters)");
        }
    } else {
        callError("Please Enter a Username and Password");
    }
    console.log(user_id, password);
}

async function handleCreate(event) {
    event.preventDefault();

    const username = document.getElementById("username").value;
    const password = document.getElementById("createPassword").value;

    if (username && password) {
        if (isValid(username)) {
            const response = await APICall(
                {
                    "request": "make_user_account",
                    "dat": {
                        "username": username,
                        "password": password
                    }
                }
            );

            if (response.response == "accept" && response.error == "None") {
                const response2 = await APICall(
                    {
                        "request": "request_login_token",
                        "dat": {
                            "user_id": response.dat.user_id,
                            "password": password
                        }
                    }
                );

                if (response2.response == "accept" && response2.error == "None") {

                    
                    localStorage.setItem("user_id", response.dat.user_id);
                    
                    localStorage.setItem("loginToken", response2.dat.token);
                    window.location.replace("/");
                } else {
                    callError(response2.error);
                }
            } else {
                callError(response.error);
            }
        } else {
            callError("Invalid Username (NO space or special characters)");
        }
    } else {
        callError("Please Enter a Username and Password")
    }

    console.log(username);
}

function init() {
    if (localStorage.getItem("loginToken") && localStorage.getItem("user_id")) {
        window.location.replace("/");
    }
}