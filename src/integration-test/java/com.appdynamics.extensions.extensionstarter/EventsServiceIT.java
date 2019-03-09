package com.appdynamics.extensions.extensionstarter;

import com.appdynamics.extensions.eventsservice.EventsServiceDataManager;
import com.appdynamics.extensions.extensionstarter.events.ExtensionStarterEventsManager;
import com.appdynamics.extensions.http.Http4ClientBuilder;
import com.appdynamics.extensions.logging.ExtensionsLoggerFactory;
import com.appdynamics.extensions.yml.YmlReader;
import org.apache.http.HttpHost;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;

import java.io.File;
import java.util.Map;

import static com.appdynamics.extensions.eventsservice.utils.Constants.*;

public class EventsServiceIT {
    private static final Logger logger = ExtensionsLoggerFactory.getLogger(EventsServiceIT.class);

    private CloseableHttpClient httpClient;
    private HttpHost httpHost;
    private String globalAccountName, eventsApiKey;
    private ExtensionStarterEventsManager eventsManager;
    private CloseableHttpResponse httpResponse;

    @Before
    public void setup() {
        Map<String, ?> eventsServiceParameters = (Map) YmlReader.readFromFile(new File("src/integration-test/resources/conf/config_ci.yml"))
                .get("eventsServiceParameters");
        String eventsServiceHost = (String) eventsServiceParameters.get("host");
        int eventsServicePort = (Integer) eventsServiceParameters.get("port");
        globalAccountName = (String) eventsServiceParameters.get("globalAccountName");
        eventsApiKey = (String) eventsServiceParameters.get("eventsApiKey");
        boolean useSSL = (Boolean) eventsServiceParameters.get("useSSL");
        httpClient = Http4ClientBuilder.getBuilder(eventsServiceParameters).build();
        httpHost = new HttpHost(eventsServiceHost, eventsServicePort, useSSL ? "https" : "http");
        eventsManager = new ExtensionStarterEventsManager(new EventsServiceDataManager(eventsServiceParameters));
    }

    @After
    public void teardown() {
        if (httpResponse != null) {
            try {
                httpResponse.close();
            } catch (Exception ex) {
                logger.error("Error encountered while closing the HTTP response", ex);
            }
        }
    }

    @Test
    public void testWhetherSchemaIsCreated() throws Exception {
        eventsManager.deleteSchema();
        eventsManager.createSchema();
        CloseableHttpResponse httpResponse = fetchSchemaFromEventsService();
        Assert.assertEquals(200, httpResponse.getStatusLine().getStatusCode());
    }

    @Test
    public void testWhetherSchemaIsDeleted() throws Exception {
        eventsManager.deleteSchema();
        CloseableHttpResponse httpResponse = fetchSchemaFromEventsService();
        Assert.assertEquals(404, httpResponse.getStatusLine().getStatusCode());

    }

    @Test
    public void testWhetherSchemaIsUpdated() throws Exception {
        eventsManager.deleteSchema();
        eventsManager.createSchema();
        eventsManager.updateSchema();
        String responseBody = EntityUtils.toString(fetchSchemaFromEventsService().getEntity(), "UTF-8");
        Assert.assertTrue(responseBody.contains("appName"));
    }

    @Test
    public void testWhetherEventsArePublished() throws Exception {
        eventsManager.deleteSchema();
        eventsManager.createSchema();
        eventsManager.updateSchema();
        eventsManager.publishEvents();
        Assert.assertTrue(!eventsManager.queryEvents().equals(""));
    }

    private CloseableHttpResponse fetchSchemaFromEventsService() throws Exception {
        HttpGet httpGet = new HttpGet(httpHost.toURI() + SCHEMA_PATH + "BTDSchema");
        httpGet.setHeader(ACCOUNT_NAME_HEADER, globalAccountName);
        httpGet.setHeader(API_KEY_HEADER, eventsApiKey);
        httpGet.setHeader(ACCEPT_HEADER, ACCEPTED_CONTENT_TYPE);
        return httpClient.execute(httpGet);
    }
}