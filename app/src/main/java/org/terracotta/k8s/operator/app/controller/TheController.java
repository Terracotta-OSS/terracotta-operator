package org.terracotta.k8s.operator.app.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.terracotta.k8s.operator.app.NotFoundException;
import org.terracotta.k8s.operator.app.service.TheService;
import org.terracotta.k8s.operator.shared.ServerStatus;
import org.terracotta.k8s.operator.shared.ServerStatusResponse;
import org.terracotta.k8s.operator.shared.TerracottaClusterConfiguration;

import java.util.Map;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api")
public class TheController {

  private static final Logger log = LoggerFactory.getLogger(TheController.class);

  @Autowired
  private TheService theService;

  @Value("${application.namespace}")
  String namespace;

  @GetMapping(value="/status")
  @ResponseBody
  public ServerStatusResponse status() {
    return new ServerStatusResponse(ServerStatus.OK);
  }

  @PostMapping(value = "/cluster", consumes = {MediaType.APPLICATION_JSON_VALUE})
  @ResponseBody
  public ResponseEntity<String> createDeployment(@RequestBody TerracottaClusterConfiguration terracottaClusterConfiguration) {

    // create the tc configs
    Map<String, String> tcConfigs = theService.generateTerracottaConfig(terracottaClusterConfiguration);

    // store them in a configmap
    theService.storeTcConfigConfigMap(tcConfigs);

    // store the terracottaClusterConfiguration in a configMap
    theService.storeTerracottaClusterConfigurationConfigMap(terracottaClusterConfiguration);

    // create the Terracotta Statefulsets
    theService.createCluster(terracottaClusterConfiguration);

    return ResponseEntity.ok(theService.constructTerracottaServerUrl(terracottaClusterConfiguration));

  }

  @GetMapping("/cluster")
  @ResponseBody
  public TerracottaClusterConfiguration listDeployment() {
    return theService.retrieveTerracottaClusterConfigurationConfigMap();
  }

  @DeleteMapping("/cluster")
  @ResponseBody
  public void deleteDeployment() {

    // store the terracottaClusterConfiguration in a configMap
    TerracottaClusterConfiguration terracottaClusterConfiguration = theService.retrieveTerracottaClusterConfigurationConfigMap();
    if (terracottaClusterConfiguration != null) {
      theService.deleteCluster();
    } else {
      throw new NotFoundException();
    }


  }

}
