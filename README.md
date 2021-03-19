# LAUNCH BLOCKCHAIN
### GENERATE
*exonum-java generate-template --validators-count=4 --supervisor-mode simple  ./common.toml*

- exonum-java generate-config ./common.toml 1 --no-password --peer-address 127.0.0.1:5401
- exonum-java generate-config ./common.toml 2 --no-password --peer-address 127.0.0.1:5402
- exonum-java generate-config ./common.toml 3 --no-password --peer-address 127.0.0.1:5403
- exonum-java generate-config ./common.toml 4 --no-password --peer-address 127.0.0.1:5404

> - exonum-java finalize 1/sec.toml 1/node.toml --public-configs {1,2,3,4}/pub.toml
> - exonum-java finalize 2/sec.toml 2/node.toml --public-configs {1,2,3,4}/pub.toml
> - exonum-java finalize 3/sec.toml 3/node.toml --public-configs {1,2,3,4}/pub.toml
> - exonum-java finalize 4/sec.toml 4/node.toml --public-configs {1,2,3,4}/pub.toml

`export RUST_LOG="${RUST_LOG-error,exonum=info,exonum-java=info,java_bindings=info}"`

### RUN
- exonum-java run --node-config 1/node.toml --artifacts-path 1/artifacts --db-path 1/db --master-key-pass pass --public-api-address 127.0.0.1:3001 --private-api-address 127.0.0.1:3011 --ejb-port 7001 > ./1/node1.log 2>&1 &
- exonum-java run --node-config 2/node.toml --artifacts-path 2/artifacts --db-path 2/db --master-key-pass pass --public-api-address 127.0.0.1:3002 --private-api-address 127.0.0.1:3012 --ejb-port 7002 > ./2/node1.log 2>&1 &
- exonum-java run --node-config 3/node.toml --artifacts-path 3/artifacts --db-path 3/db --master-key-pass pass --public-api-address 127.0.0.1:3003 --private-api-address 127.0.0.1:3013 --ejb-port 7003 > ./3/node1.log 2>&1 &
- exonum-java run --node-config 4/node.toml --artifacts-path 4/artifacts --db-path 4/db --master-key-pass pass --public-api-address 127.0.0.1:3004 --private-api-address 127.0.0.1:3014 --ejb-port 7004 > ./4/node1.log 2>&1 &

# INSPECT BLOCKCHAIN

## LIST PEERS IN CHAIN
curl -s http://127.0.0.1:3011/api/system/v1/info | jq

## LIST CURRENT ARTIFACT & SERVICES
curl -s http://127.0.0.1:3001/api/services/supervisor/services | jq

## GRACDEFULL SHUTDOWN
curl -X POST "http://127.0.0.1:8081/api/system/v1/shutdown"


# INSTALL AND LAUNCH AND DEPLOY SMART-CONTRACT

- python3 -m venv .venv
- source .venv/bin/activate
- pip install exonum-launcher-java-plugins
- python -m exonum_launcher --help
- python -m exonum_launcher -i deploy-start-config.yml

# CHEATSSHEET

> **https://exonum.com/doc/version/1.0.0/get-started/first-java-service/**

http://localhost:8000/api/services/supervisor/config-proposal
http://localhost:8000/api/services/supervisor/consensus-config
http://127.0.0.1:8200/api/explorer/v1/transactions
/api/explorer/v1/transactions?hash=


