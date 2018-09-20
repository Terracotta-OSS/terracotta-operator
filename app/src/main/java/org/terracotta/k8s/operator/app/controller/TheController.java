package org.terracotta.k8s.operator.app.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.terracotta.k8s.operator.app.NotFoundException;
import org.terracotta.k8s.operator.app.TerracottaOperatorException;
import org.terracotta.k8s.operator.shared.ServerStatus;
import org.terracotta.k8s.operator.shared.ServerStatusResponse;
import org.terracotta.k8s.operator.shared.TerracottaClusterConfiguration;
import org.terracotta.k8s.operator.app.service.TheService;

import java.util.Base64;
import java.util.Map;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api")
public class TheController {

  private static final Logger log = LoggerFactory.getLogger(TheController.class);

  @Autowired
  private TheService theService;

  @GetMapping(value="/status")
  @ResponseBody
  public ServerStatusResponse status() {
    return new ServerStatusResponse(ServerStatus.OK);
  }

  @PutMapping(value = "/config/license")
  @ResponseBody
  public ResponseEntity<String> createLicense(@RequestBody String licenseFile) {
    theService.storeLicenseConfigMap(new String(Base64.getDecoder().decode(licenseFile)));
    return ResponseEntity.ok("License stored\n");
  }

  @GetMapping(value = "/config/license")
  @ResponseBody
  public ResponseEntity<String> readLicense() {
    String license = theService.readLicenseConfigMap();
    if (license != null) {
      return ResponseEntity.ok(license);
    } else {
      return ResponseEntity.badRequest().body("License not found\n");
    }
  }

  @DeleteMapping(value = "/config/license")
  @ResponseBody
  public ResponseEntity<String> deleteLicense() {
    boolean deleted = theService.deleteConfigMap("license");
    if (deleted) {
      return ResponseEntity.ok("Deleted the license\n");
    } else {
      return ResponseEntity.badRequest().body("License not found\n");
    }
  }

  @PostMapping(value = "/cluster/{clusterName}", consumes = {MediaType.APPLICATION_JSON_VALUE})
  @ResponseBody
  public ResponseEntity<String> createDeployment(@PathVariable("clusterName") String clusterName, @RequestBody TerracottaClusterConfiguration terracottaClusterConfiguration) {

    // check the license exists
    theService.checkLicense();

    // create the tc configs
    Map<String, String> tcConfigs = theService.generateTerracottaConfigs(terracottaClusterConfiguration);

    // store them in a configmap
    theService.storeTcConfigsConfigMap(tcConfigs);

    // store the terracottaClusterConfiguration in a configMap
    theService.storeTerracottaClusterConfigurationConfigMap(clusterName, terracottaClusterConfiguration);

    // create the Terracotta Statefulsets
    theService.createCluster(terracottaClusterConfiguration);

    // configure the cluster with the cluster tool
    if (!theService.configureCluster(clusterName, terracottaClusterConfiguration)) {
      theService.deleteCluster(clusterName, terracottaClusterConfiguration);
      throw new TerracottaOperatorException("Impossible to create the cluster");
    }

    return ResponseEntity.ok(theService.constructTerracottaServerUrl(terracottaClusterConfiguration));

  }

  @GetMapping("/cluster/{clusterName}")
  @ResponseBody
  public TerracottaClusterConfiguration listDeployment(@PathVariable("clusterName") String clusterName) {
    return theService.retrieveTerracottaClusterConfigurationConfigMap(clusterName);
  }

  @DeleteMapping("/cluster/{clusterName}")
  @ResponseBody
  public void deleteDeployment(@PathVariable("clusterName") String clusterName) {

    // store the terracottaClusterConfiguration in a configMap
    TerracottaClusterConfiguration terracottaClusterConfiguration = theService.retrieveTerracottaClusterConfigurationConfigMap(clusterName);
    if (terracottaClusterConfiguration != null) {
      theService.deleteCluster(clusterName, terracottaClusterConfiguration);
    } else {
      throw new NotFoundException();
    }


  }

}
