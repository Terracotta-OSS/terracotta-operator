# Terracotta Operator

## get started with local environement

    mvn clean install -DskipTests

Then run the main method from your IDE; it should work

### Upgrade plugin versions

    mvn versions:display-dependency-updates versions:display-plugin-updates

## deploying to Kubernetes


### build and publish it
    mvn compile jib:dockerBuild

you can checkout jib options to push to local docker instead :
    https://github.com/GoogleContainerTools/jib

### Required access (if needed)

```
kubectl create rolebinding admin --clusterrole=admin --serviceaccount=default:default --namespace=default
kubectl create rolebinding admin-test --clusterrole=admin --serviceaccount=default:default --namespace=thisisatest
```

### Run and expose the operator

```
kubectl run operator --image-pull-policy='Always' --image=terracotta/terracotta-operator --port=8080
kubectl expose deployment operator --name=operator-port --type=NodePort
```

## Interact with the operator, via Rest

### create a new cluster named `pif` , list it, delete it

When a new cluster is created, we'll persist the tc-configs in a ConfigMap (kubernetes object) named tc-configs
    
    curl --header "Content-Type: application/json" \
      --request POST \
      --data '{"offheaps":{"offheap1":"100MB","offheap2":"10GB"},"dataroots":{"PLATFORM":"local","dataroot1":"EBS","dataroot2":"local"},"stripes":2,"serversPerStripe":2,"clientReconnectWindow":20}' \
      http://localhost:8080/api/cluster/pif

    curl http://localhost:8080/api/cluster/pif
    
    # or list all known clusters (1 MAX for now)
    curl http://localhost:8080/api/cluster

    curl -X DELETE http://localhost:8080/api/cluster/pif


### upload license, list license, delete license

When a license is uploaded, we'll persist it in a ConfigMap (kubernetes object) named license

    curl -X PUT -F 'data=@/Users/adah/Downloads/TerracottaDB101.xml' http://localhost:8080/api/config/license
    
    curl http://localhost:8080/api/config/license
    
    curl -X DELETE http://localhost:8080/api/config/license

### get info about Kubernetes cluster

    curl http://localhost:8080/api/info

### few notes

