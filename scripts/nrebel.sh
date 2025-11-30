#!/bin/bash
if [ -f ".nrepl-port" ]; then
    port=$(cat .nrepl-port | tr -d '\n')
else
    echo "No .nrepl-port file found. Please enter the REPL port:"
    read port
fi

lein trampoline run -m rebel-readline.nrepl.main --port $port