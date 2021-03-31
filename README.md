### GENERATE CONFIG FOR BLOCKCHAIN

*exonum-java generate-template --validators-count=4 --supervisor-mode simple  ./common.toml*

- exonum-java generate-config ./common.toml 1 --no-password --peer-address 127.0.0.1:5401
- exonum-java generate-config ./common.toml 2 --no-password --peer-address 127.0.0.1:5402
- exonum-java generate-config ./common.toml 3 --no-password --peer-address 127.0.0.1:5403
- exonum-java generate-config ./common.toml 4 --no-password --peer-address 127.0.0.1:5404

> - exonum-java finalize 1/sec.toml 1/node.toml --public-configs {1,2,3,4}/pub.toml
> - exonum-java finalize 2/sec.toml 2/node.toml --public-configs {1,2,3,4}/pub.toml
> - exonum-java finalize 3/sec.toml 3/node.toml --public-configs {1,2,3,4}/pub.toml
> - exonum-java finalize 4/sec.toml 4/node.toml --public-configs {1,2,3,4}/pub.toml


###### PREPARE THE RUNTIME FOR DEPLOY SMART CONTRACTS
- python3 -m venv .venv
- source .venv/bin/activate
- pip install exonum-launcher-java-plugins
- python -m exonum_launcher --help

### LAUNCH BLOCKCHAIN

`export RUST_LOG="${RUST_LOG-error,exonum=info,exonum-java=info,java_bindings=info}"`

exonum-java run --node-config 1/node.toml --artifacts-path 1/artifacts --db-path 1/db --master-key-pass pass --public-api-address 127.0.0.1:3001 --private-api-address 127.0.0.1:3011 --ejb-port 7001 > ./1/node1.log 2>&1 &

exonum-java run --node-config 2/node.toml --artifacts-path 2/artifacts --db-path 2/db --master-key-pass pass --public-api-address 127.0.0.1:3002 --private-api-address 127.0.0.1:3012 --ejb-port 7002 > ./2/node1.log 2>&1 &

exonum-java run --node-config 3/node.toml --artifacts-path 3/artifacts --db-path 3/db --master-key-pass pass --public-api-address 127.0.0.1:3003 --private-api-address 127.0.0.1:3013 --ejb-port 7003 > ./3/node1.log 2>&1 &

exonum-java run --node-config 4/node.toml --artifacts-path 4/artifacts --db-path 4/db --master-key-pass pass --public-api-address 127.0.0.1:3004 --private-api-address 127.0.0.1:3014 --ejb-port 7004 > ./4/node1.log 2>&1 &


###### BUILD JAR & DEPLOY

- maven clean
- maven package
- add this jar (car-registry-service-1.0.0-artifact.jar) to folders called artifacts
- python -m exonum_launcher -i deploy-start-config.yml (this file is placed in config blockchain directory)

# INSPECT BLOCKCHAIN

## LIST PEERS IN CHAIN
curl -s http://127.0.0.1:3011/api/system/v1/info | jq

## LIST CURRENT ARTIFACT & SERVICES
curl -s http://127.0.0.1:3001/api/services/supervisor/services | jq

## GRACDEFULL SHUTDOWN
curl -X POST "http://127.0.0.1:8081/api/system/v1/shutdown"



# CLIENT

- add-vehicle -a -n=test-car "My car1" "VW" "Polo" "USER1"
- find-vehicle -n=test-car "My car1"
- find-vehicle-get -n=test-car "My car1"
- find-vehicle-post -n=test-car "My car1"

# CHEATSSHEET

> **https://exonum.com/doc/version/1.0.0/get-started/first-java-service/**

- https://github.com/exonum/exonum-java-binding/tree/8db5cec21ba542aa0c3bb70a6b3811c42fd17251/exonum-java-binding/cryptocurrency-demo/frontend

http://localhost:8000/api/services/supervisor/config-proposal
http://localhost:8000/api/services/supervisor/consensus-config
http://127.0.0.1:8200/api/explorer/v1/transactions
/api/explorer/v1/transactions?hash=

> https://exonum.com/doc/version/1.0.0/architecture/transactions/
