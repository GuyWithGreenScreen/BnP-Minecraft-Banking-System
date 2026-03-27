# BLOCK AND PAUL'S MINECRAFT BANKING SYSTEM

## DISCLAIMER

This project was made with the following assumption in mind:
- This was only going to be used with my friends and privately

This means that this project was by no means meant to be deployed large scale, nor made for it.

Not only that but due to unfortunate circumstances regarding the Minecraft server this project was made for, I stopped development half way through as the server was no longer happening.

This means that unfortunately I did not stress test and poke holes in my security as thoroughly as I wanted to, nor have I finished all the features.

These features include:
- A player run exchange for anything to anything
- Larger ID lengths for coins *(4 HEX values translates to around 65K Coins, unacceptable, especially considering you must efficently find new and unused values, making the cutoff much smaller, or I can make an incremental ID system instead of a random one to mitigate the problem of collisions)*
- Complete overhaul of price logic *(This is so greatly broken as of now, coin price calculations are not anywhere close to real economical ways of calculating price)*
- Better storage of data (Not sure if JSONs are the way to go everywhere)
- Possibly other language (I might eye Java as the language of choice for the backend)
- More security on session/account tokens (Not secure enough as it is)
- Atomic operations (To ensure that mid op crashes aren't catastrophic)
- Asyncronous operation (Yes, I had to make it sequential for my error checking to work, again not an issue for its intended scope (a few friends), but an issue for large deployments)

**ALL TRAFFIC IS HTTP, PLEASE ENSURE YOUR SETUP IS EITHER FULLY PRIVATE OR RUNS THROUGH SOMETHING LIKE A CLOUDFLARE TUNNEL!**

## ABOUT

This mod/webapp combo adds basic banking and "crypto"coin creation to Minecraft.

This is a full stack project that involves the NeoForge mod backend written in Java, the website logic/backend written in Python, and the frontend written in JS.

To use, you must install the necessary libraries for Python that the [code/main.py](main.py) uses, then you can run it using [code/start_server.sh](start_server.sh), or just by starting a uvicorn server yourself.

The Minecraft server must have the NeoForge mod installed, as well as, the config file of the mod must contain an API key that you generate using the [code/supplementary](supplementary.py) program, simply by calling the 
`createKey()` function at the end of the file.

The mod config file must also contain the location of the API.

## OTHER INFO

### TO READ ABOUT HOW THE .JSON FILES ARE STRUCTURED AND HOW THE NETWORKING AND BACKEND WORKS [features.md](CLICK HERE)

### TO READ ABOUT EACH SITE PAGE [site/features.md](CLICK HERE)
