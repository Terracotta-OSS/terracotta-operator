package org.terracotta.k8s.operator.app.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.fabric8.kubernetes.client.KubernetesClient;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;
import org.springframework.web.context.WebApplicationContext;
import org.terracotta.k8s.operator.shared.TerracottaClusterConfiguration;
import org.terracotta.k8s.operator.app.service.TheService;

import java.util.Map;
import java.util.TreeMap;

@RunWith(SpringRunner.class)
@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)

@Ignore
public class TheControllerTest {

  private MockMvc mockMvc;
  @Autowired
  private WebApplicationContext webApplicationContext;
  @Autowired
  ObjectMapper objectMapper;
  @Autowired
  TheService theService;

//  @MockBean private KubernetesClientFactory kubernetesClientFactory;


  @Before
  public void setUp() {
    KubernetesClient mockClient =  null;
//    when(kubernetesClientFactory.retrieveKubernetesClient()).thenReturn(mockClient);
    this.mockMvc = webAppContextSetup(webApplicationContext).build();
  }

  @Test
  public void simple() throws Exception {

    TerracottaClusterConfiguration value = new TerracottaClusterConfiguration();
    value.setClientReconnectWindow(20);
    value.setDataroots(new TreeMap<String, String>() {{
      put("dataroot1", "EBS");
      put("dataroot2", "local");
    }});

    value.setOffheaps(new TreeMap<String, String>() {{
      put("offheap1", "100MB");
      put("offheap2", "10GB");
    }});


    String s = objectMapper.writeValueAsString(value);

    System.out.println(s);



  }

  @Test
  public void createConfigMapTest() {
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


    Map<String, String> tcConfigs = theService.generateTerracottaConfigs(clusterConfig);
    theService.storeTcConfigsConfigMap(tcConfigs);




  }

  @Test
  public void ingestTerracottaClusterConfiguration() throws Exception {

    //example payload
    String newCluster = "{\n" +
        "  \"offheaps\": {\n" +
        "    \"offheap1\": \"256MB\",\n" +
        "    \"offheap2\": \"100GB\"\n" +
        "  },\n" +
        "  \"dataroots\": {\n" +
        "    \"dataroot1\": \"EBS\",\n" +
        "    \"dataroot2\": \"local\"\n" +
        "  },\n" +
        "  \"stripes\": 2,\n" +
        "  \"serversPerStripe\": 2,\n" +
        "  \"clientReconnectWindowSeconds\": 20\n" +
        "}";


    // let's make sure it looks fine
    mockMvc.perform(post("/api/deployment/MyCluster").content(newCluster).contentType(MediaType.APPLICATION_JSON))
        //.andDo(result -> println(result))
        .andExpect(status().isOk());


  }

}
