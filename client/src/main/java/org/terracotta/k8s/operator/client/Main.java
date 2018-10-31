package org.terracotta.k8s.operator.client;

import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.terracotta.k8s.operator.shared.ClusterInfo;
import org.terracotta.k8s.operator.shared.ServerStatusResponse;
import org.terracotta.k8s.operator.shared.TerracottaClusterConfiguration;
import org.terracotta.k8s.operator.shared.WorkerNode;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;

/**
 * @author Henri Tremblay
 */
public class Main {

  private static String server;

  private static final RestTemplate template = initRestTemplate();

  private static ObjectMapper createObjectMapper() {
    ObjectMapper mapper = new ObjectMapper();
    mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    mapper.findAndRegisterModules();
    return mapper;
  }

  private static RestTemplate initRestTemplate() {
    return new RestTemplate();
  }

  public static void main(String[] args) {
    System.out.println("Welcome to Terracotta admin client. What do you want to do?");

    while (true) {
      try {
        System.out.println();
        System.out.println("1- Select server (do this first)");
        System.out.println("2- Get cluster info");
        System.out.println("3- Create deployment");
        System.out.println("4- Delete deployment");
        System.out.println("5- Add license");
        System.out.println("6- Delete license");
        System.out.println("7- Read license");
        // Add more choices here
        System.out.println("x- Exit");

        String choice = readChoice();

        switch (choice) {
          case "1":
            selectServer();
            break;
          case "2":
            getClusterInfo();
            break;
          case "3":
            createDeployment();
            break;
          case "4":
            deleteDeployment();
            break;
          case "5":
            createLicense();
            break;
          case "6":
            deleteLicense();
            break;
          case "7":
            readLicense();
            break;
          case "x":
            return;
          default:
            System.out.println("Invalid choice, try again");
        }
      } catch (Exception e) {
        e.printStackTrace(System.out);
      }
    }
  }

  private static void readLicense() {
    try {
      ResponseEntity<String> response = template.getForEntity(server + "/api/config/license", String.class);
      System.out.println(response.getBody());
    } catch(HttpClientErrorException e) {
      System.out.println(e.getResponseBodyAsString());
    }
  }

  private static void deleteLicense() {
    template.delete(server + "/api/config/license");
  }

  private static void createLicense() throws IOException {
    String filePath = readString("Enter path to licence file:");
    byte[] content = Files.readAllBytes(Paths.get(filePath));
    template.put(server + "/api/config/license", Base64.getEncoder().encodeToString(content));
  }

  private static void deleteDeployment() throws IOException {
    String clusterName = readString("Enter cluster name to delete:");
    template.delete(server + "/api/cluster/{clusterName}", clusterName);
  }

  private static void createDeployment() throws IOException {
    String clusterName = readString("Enter cluster name to create:");
    TerracottaClusterConfiguration conf = new TerracottaClusterConfiguration();

    while(true) {
      if(!readBoolean("Do you want to add offheap?")) {
        break;
      }
      String name = readString("Offheap name: ");
      String size = readString("Offheap size (suffix B, KB, MB, GB): ");
      conf.getOffheaps().put(name, size);
    }

    int serversPerStripe = readInteger("How many servers : ");
    int clientReconnectWindow = readInteger("What is the client reconnect window (in seconds): ");

    conf.setServersPerStripe(serversPerStripe);
    conf.setClientReconnectWindow(clientReconnectWindow);

    ResponseEntity<String> response = template.postForEntity(server + "/api/cluster/{clusterName}", conf, String.class, clusterName);
    System.out.println("TMC URL: " + response.getBody());
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
    while (true) {
      server = readServer();
      try {
        ServerStatusResponse status = template.getForObject(server + "/api/status", ServerStatusResponse.class);
        System.out.println(status);
        break;
      } catch (RestClientException e) {
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

  private static boolean readBoolean(String choice) throws IOException {
    while(true) {
      System.out.println();
      System.out.println(choice + " (Y/n)");
      String value = readChoice();
      switch (value) {
        // default value, Y being uppercase
        case "":
        case "Y":
          return true;
        case "n":
          return false;
        default:
          System.out.println("Invalid value: " + value);
      }
    }
  }

  private static Integer readInteger(String choice) throws IOException {
    while(true) {
      System.out.println();
      System.out.println(choice);
      String value = readChoice();
      try {
        return Integer.valueOf(value);
      } catch (NumberFormatException e) {
        System.out.println("Invalid value: " + value);
      }
    }
  }

  private static String readServer() throws IOException {
    System.out.println();
    System.out.println("From which server?");
    return readChoice();
  }
}
