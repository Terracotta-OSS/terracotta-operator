package org.terracotta.k8s.operator.app.service;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import org.junit.Test;
import org.terracotta.k8s.operator.shared.TerracottaClusterConfiguration;

import java.util.HashSet;
import java.util.Map;
import java.util.TreeMap;

public class TheServiceTest {

  @Test
  public void generateTerracottaConfigsTest() {

    TerracottaClusterConfiguration clusterConfig = new TerracottaClusterConfiguration();
    clusterConfig.setClientReconnectWindow(20);
    clusterConfig.setServersPerStripe(2);

    clusterConfig.setOffheaps(new TreeMap<String, String>() {{
      put("offheap1", "100");
      put("offheap2", "300");
    }});


    TheService theService =  new TheService();
    Map<String, String> tcConfigs = theService.generateTerracottaConfig(clusterConfig);


    assertThat(tcConfigs.keySet(),equalTo(new HashSet<String>() {{
      add("stripe-0.xml");
      add("stripe-1.xml");
    }}));

  }

  @Test
  public void retrieveAmountFromStringTest() {
    TheService theService =  new TheService();
    assertThat(theService.retrieveAmountFromString("100MB"), equalTo(100));
  }
  @Test
  public void retrieveUnitFromStringTest() {
    TheService theService =  new TheService();
    assertThat(theService.retrieveUnitFromString("100MB"), equalTo("MB"));
  }

  @Test
  public void constructTerracottaServerUrlTest() {
    TerracottaClusterConfiguration clusterConfig = new TerracottaClusterConfiguration();
    clusterConfig.setClientReconnectWindow(20);
    clusterConfig.setServersPerStripe(2);

    clusterConfig.setOffheaps(new TreeMap<String, String>() {{
      put("offheap1", "100");
      put("offheap2", "300");
    }});


    TheService theService =  new TheService();
    String terracottaServerUrl = theService.constructTerracottaServerUrl(clusterConfig);
    assertThat(terracottaServerUrl, equalTo("terracotta://terracotta-0-0.stripe-0:9410,terracotta-0-1.stripe-0:9410"));
  }
}
