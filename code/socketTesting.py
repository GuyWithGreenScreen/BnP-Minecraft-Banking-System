from fastapi import FastAPI
from pydantic import BaseModel

app = FastAPI()

class RequestData(BaseModel):
    request: str
    dat: dict

class ResponseData(BaseModel):
    response:str
    dat: dict

@app.post("/api")
def api(data: RequestData):
    return {"request": f"python received: {data.request}",
            "dat": {"ligma": data.dat["balls"]}}