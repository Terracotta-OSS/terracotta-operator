package org.terracotta.k8s.operator.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.terracotta.k8s.operator.shared.ServerStatus;
import org.terracotta.k8s.operator.shared.ServerStatusResponse;

/**
 * @author Henri Tremblay
 */
@SpringBootApplication
@RestController
@RequestMapping("/api")
public class Main {

  @GetMapping("/status")
  @ResponseBody
  public ServerStatusResponse status() {
    return new ServerStatusResponse(ServerStatus.OK);
  }

  public static void main(String[] args) {
    SpringApplication.run(Main.class, args);
  }

}
