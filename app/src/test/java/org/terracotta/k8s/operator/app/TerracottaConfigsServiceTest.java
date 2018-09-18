package org.terracotta.k8s.operator.app;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.*;
import org.junit.Test;
import org.terracotta.k8s.operator.app.model.TerracottaClusterConfiguration;

import java.util.HashSet;
import java.util.Map;
import java.util.TreeMap;

public class TerracottaConfigsServiceTest {

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


    TerracottaConfigsService terracottaConfigsService =  new TerracottaConfigsService();
    Map<String, String> tcConfigs = terracottaConfigsService.generateTerracottaConfigs(clusterConfig);


    assertThat(tcConfigs.keySet(),equalTo(new HashSet<String>() {{
      add("terracotta-stripe-0.xml");
      add("terracotta-stripe-1.xml");
    }}));


  }


}