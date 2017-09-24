package io.openapitools.rest.bank.customer;

import com.fasterxml.jackson.jaxrs.json.JacksonJaxbJsonProvider;
import io.openapitools.rest.common.core.diagnostic.ContextInfo;
import io.openapitools.rest.common.core.diagnostic.DiagnosticContext;
import java.io.UnsupportedEncodingException;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@SuppressWarnings("unchecked")
public class CustomerServiceExposureIT {

    private DiagnosticContext dCtx;

    @Before
    public void setupLogToken() {
        dCtx = new DiagnosticContext(new ContextInfo() {
            @Override
            public String getLogToken() {
                return "junit-" + System.currentTimeMillis();
            }

            @Override
            public void setLogToken(String s) {

            }
        });
        dCtx.start();
    }

    @After
    public void removeLogToken() {
        dCtx.stop();
    }

    @Test
    public void testListCustomers() {
        WebTarget target = ClientBuilder.newClient().register(JacksonJaxbJsonProvider.class).target("http://localhost:7001/sample");
        Map<String, Object> response = target.path("customers")
                .request()
                .accept("application/hal+json")
                .header("X-Client-Version", "1.0.0")
                .header("X-Service-Generation", "1")
                .header("X-Log-Token", DiagnosticContext.getLogToken())
                .get(Map.class);

        assertNotNull(response.get("_embedded"));
        Map<String, Object> embedded = (Map<String, Object>) response.get("_embedded");
        assertTrue(((List) embedded.get("customers")).size() >= 3);
    }

    @Test
    public void testGetCustomer() {
        WebTarget target = ClientBuilder.newClient().register(JacksonJaxbJsonProvider.class).target("http://localhost:7001/sample");
        Map<String, Object> response = target.path("customers/9999999999")
                .request()
                .accept("application/hal+json")
                .header("X-Client-Version", "1.0.0")
                .header("X-Service-Generation", "1")
                .header("X-Log-Token", DiagnosticContext.getLogToken())
                .get(Map.class);

        assertEquals("Ole", response.get("firstName"));
        assertEquals("Bent", response.get("middleName"));
        assertEquals("Pedersen", response.get("sirname"));
        assertEquals("9999999999", response.get("number"));
    }

    @Test
    public void testGetCustomerUsingSpecificContentTypeWithParameters() {
        WebTarget target = ClientBuilder.newClient().register(JacksonJaxbJsonProvider.class).target("http://localhost:7001/sample");
        Map<String, Object> response = target.path("customers").path("8888888888")
                .request()
                .accept("application/hal+json;concept=customer;v=1")
                .header("X-Client-Version", "1.0.0")
                .header("X-Service-Generation", "1")
                .header("X-Log-Token", DiagnosticContext.getLogToken())
                .get(Map.class);
        assertEquals("Georg", response.get("firstName"));
        assertEquals("B", response.get("middleName"));
        assertEquals("Jensen", response.get("sirname"));
        assertEquals("8888888888", response.get("number"));
    }

    @Ignore("Ignored because a valid OAuth endpoint is not supplied")
    @Test
    public void testCreateCustomer() throws Exception {

        String accessToken = requestAccessToken("advisor1");
        int customerNo = ThreadLocalRandom.current().nextInt(9999999);
        WebTarget customerServices =
                ClientBuilder.newClient().register(JacksonJaxbJsonProvider.class).target("http://localhost:7001/sample");
        Map<String, String> customerCreate = new ConcurrentHashMap<>();
        customerCreate.put("firstName", "Bo");
        customerCreate.put("middleName", "Hr");
        customerCreate.put("sirname", "Hansen");
        Map<String, Object> response = customerServices.path("customers").path("" + customerNo)
                .request()
                .accept("application/hal+json")
                .header("X-Client-Version", "1.0.0")
                .header("X-Log-Token", DiagnosticContext.getLogToken())
                .header("X-Service-Generation", "1")
                .header("Authorization", "Bearer " + accessToken)
                .put(Entity.entity(customerCreate, MediaType.APPLICATION_JSON_TYPE), Map.class);

        assertEquals("Bo", response.get("firstName"));
        assertEquals("Hr", response.get("middleName"));
        assertEquals("Hansen", response.get("sirname"));
        assertEquals(customerNo, response.get("number"));
    }

    @Ignore("Ignored because a valid OAuth endpoint is not supplied")
    @Test
    public void testEventCollections() throws UnsupportedEncodingException {
        WebTarget target = ClientBuilder.newClient().register(JacksonJaxbJsonProvider.class).target("http://localhost:7001/sample");
        String accessToken = requestAccessToken("tx-system-update");
        Map<String, Object> response = target.path("customer-events")
                .request()
                .accept("application/hal+json")
                .header("X-Client-Version", "1.0.0")
                .header("X-Service-Generation", "1")
                .header("X-Log-Token", DiagnosticContext.getLogToken())
                .header("Authorization", "Bearer " + accessToken)
                .get(Map.class);

        assertNotNull(response);
        assertNotNull(response.get("_links"));
        assertNotNull(response.get("_embedded"));
        Map<String, Object> embedded = (Map<String, Object>) response.get("_embedded");
        assertTrue(((List) embedded.get("events")).size() >= 2);
    }



    private String requestAccessToken(final String username) throws UnsupportedEncodingException {
        WebTarget oauth2Service = ClientBuilder.newClient().register(JacksonJaxbJsonProvider.class).target("http://localhost:7001/security");
        MultivaluedMap<String, String> request = new MultivaluedHashMap<>();
        request.putSingle("grant_type", "client_credentials");
        String credentials = Base64.getEncoder().encodeToString((username + ":passw0rd").getBytes("UTF-8"));
        Map<String, String> oauthResponse = oauth2Service.path("oauth2/token")
                .request(MediaType.APPLICATION_FORM_URLENCODED_TYPE)
                .header("Authorization", "Basic " + credentials)
                .header("X-Log-Token", DiagnosticContext.getLogToken())
                .header("X-Service-Generation", "1")
                .header("X-Client-Version", "1.0.0")
                .post(Entity.form(request), Map.class);
        return oauthResponse.get("access_token");
    }
}
