# Terracotta Operator

## get started with local environement

    mvn clean install

Then run the main method from your IDE or run with maven spring-boot:run

### Upgrade plugin versions

    mvn versions:display-dependency-updates versions:display-plugin-updates

## deploying to Kubernetes


### build and publish it
 
    mvn -f app/pom.xml jib:dockerBuild

    docker push terracotta/terracotta-operator

### Required access (if needed)
If you encounter :

```
Message: Forbidden!Configured service account doesn't have access. Service account may have been revoked. namespaces "thisisatest" is forbidden: User "system:serviceaccount:default:default" cannot get namespaces in the namespace "thisisatest".

```

Then apply this file :

```
 kubectl apply -f sample-yaml-files/fabric8-rbac.yaml
```

### Run and expose the operator

If on Minikube :
```
kubectl run terracotta-operator --image-pull-policy='Always' --image=terracotta/terracotta-operator --port=8080
kubectl expose deployment terracotta-operator --name=terracotta-operator-port --type=NodePort
minikube service terracotta-operator-port
```

If somewhere else : 
```
kubectl run terracotta-operator --image-pull-policy='Always' --image=terracotta/terracotta-operator --port=8080
kubectl expose deployment terracotta-operator --name=operator-port --type=NodePort
```


## Interact with the operator, via Rest

### create a new cluster, list it, delete it

To create a new cluster:
    
    curl --header "Content-Type: application/json" \
      --request POST \
      --data '{"offheaps":{"offheap1":"100MB","offheap2":"1GB"},"serversPerStripe":2,"clientReconnectWindow":20}' \
      http://localhost:8080/api/cluster

Check the status of the cluster: 

    curl http://localhost:8080/api/cluster
    
Delete the cluster: 

    curl -X DELETE http://localhost:8080/api/cluster


## Interact with the operator, via kubectl and CRDs

    kubectl --namespace=thisisatest  apply -f sample-yaml-files/1server-cluster.yaml
    
and delete : 

    kubectl --namespace=thisisatest  delete -f sample-yaml-files/1server-cluster.yaml


### few notes

super slow to delete a statefulset...


