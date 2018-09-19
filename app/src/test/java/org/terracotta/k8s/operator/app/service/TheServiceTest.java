package org.terracotta.k8s.operator.app.service;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import org.junit.Test;
import org.terracotta.k8s.operator.app.model.TerracottaClusterConfiguration;

import java.util.HashSet;
import java.util.Map;
import java.util.TreeMap;

public class TheServiceTest {

  @Test
  public void generateTerracottaConfigsTest() {

    TerracottaClusterConfiguration clusterConfig = new TerracottaClusterConfiguration();
    clusterConfig.setClientReconnectWindow(20);
    clusterConfig.setServersPerStripe(2);
    clusterConfig.setStripes(2);
    clusterConfig.setDataroots(new TreeMap<String, String>() {{
      put("dataroot1", "EBS");
      put("dataroot2", "local");
    }});

    clusterConfig.setOffheaps(new TreeMap<String, String>() {{
      put("offheap1", "100");
      put("offheap2", "300");
    }});


    TheService theService =  new TheService();
    Map<String, String> tcConfigs = theService.generateTerracottaConfigs(clusterConfig);


    assertThat(tcConfigs.keySet(),equalTo(new HashSet<String>() {{
      add("stripe-0.xml");
      add("stripe-1.xml");
    }}));


  }


}