
if [ -z "$1" ] || [ -z "$2" ]; then
    echo "Usage: $0 <host addr> <port>"
    exit 1
fi


python -m uvicorn main:app --host $1 --port $2 --workers 1

