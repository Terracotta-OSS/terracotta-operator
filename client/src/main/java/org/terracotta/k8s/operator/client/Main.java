package org.terracotta.k8s.operator.client;

import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.terracotta.k8s.operator.shared.ClusterInfo;
import org.terracotta.k8s.operator.shared.ServerStatusResponse;
import org.terracotta.k8s.operator.shared.WorkerNode;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * @author Henri Tremblay
 */
public class Main {

    private static String server;

    private static final RestTemplate template = initRestTemplate();

    private static final ObjectMapper mapper = createObjectMapper();

    private static ObjectMapper createObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        mapper.findAndRegisterModules();
        return mapper;
    }

    private static ObjectMapper getObjectMapper() {
        return mapper;
    }

    private static RestTemplate initRestTemplate() {
        return new RestTemplate();
    }

    public static void main(String[] args) throws IOException {
        System.out.println("Welcome to Terracotta admin client. What do you want to do?");

        while(true) {
            try {
                System.out.println();
                System.out.println("1- Select server (do this first)");
                System.out.println("2- Get cluster info");
                // Add more choices here
                System.out.println("x- Exit");

                String choice = readChoice();

                switch(choice) {
                    case "1":
                        selectServer();
                        break;
                    case "2":
                        getClusterInfo();
                    case "x":
                        return;
                    default:
                        System.out.println("Invalid choice, try again");
                }
            } catch(Exception e) {
                e.printStackTrace(System.out);
            }
        }
    }

    private static void getClusterInfo() {
      ClusterInfo clusterInfo = template.getForObject(server + "/api/info", ClusterInfo.class);
      for (int i = 0; i < clusterInfo.getWorkerNodes().size(); i++) {
        WorkerNode node = clusterInfo.getWorkerNodes().get(i);
        System.out.println("Node " + i);
        System.out.println("  Memory: " + node.getAvailableMemory());
        System.out.println("  CPU:    " + node.getCpuNumber());
        System.out.println("  Labels: " + node.getLabels());
        System.out.println("  Pods:   " + node.getPodsCurrentlyRunning());
      }
    }

    private static void selectServer() throws IOException {
        while(true) {
            server = readServer();
            try {
                ServerStatusResponse status = template.getForObject(server + "/api/status", ServerStatusResponse.class);
                System.out.println(status);
                break;
            } catch(RestClientException e) {
                e.printStackTrace();
            }
        }
    }

    private static String readChoice() throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        return br.readLine();
    }

    private static String readString(String choice) throws IOException {
        System.out.println();
        System.out.println(choice);
        return readChoice();
    }

    private static String readServer() throws IOException {
        System.out.println();
        System.out.println("From which server?");
        return readChoice();
    }
}
