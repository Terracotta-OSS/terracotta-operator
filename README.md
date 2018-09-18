# Terracotta Operator

## Required access (if needed)

```
kubectl create rolebinding admin --clusterrole=admin --serviceaccount=default:default --namespace=default
kubectl create rolebinding admin-test --clusterrole=admin --serviceaccount=default:default --namespace=thisisatest
```

## Run and expose the operator

```
kubectl run operator --image-pull-policy='Always' --image=terracotta/terracotta-operator --port=8080
kubectl expose deployment operator --name=operator-port --type=NodePort
```

## Interact with the operator

### create a new deployment named `pif`
    
    curl -X POST http://localhost:8080/pif


### list it

    kubectl get all --namespace=thisisatest

## build and publish it
    mvn compile jib:dockerBuild

you can checkout jib options to push to local docker instead :
    https://github.com/GoogleContainerTools/jib


## Development

### Upgrade plugin versions

    mvn versions:display-dependency-updates versions:display-plugin-updates
