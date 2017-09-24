package io.openapitools.rest.bank.account;

import static org.junit.Assert.*;

import java.io.UnsupportedEncodingException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import com.fasterxml.jackson.jaxrs.json.JacksonJaxbJsonProvider;
import io.openapitools.rest.common.core.diagnostic.ContextInfo;
import io.openapitools.rest.common.core.diagnostic.DiagnosticContext;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

@SuppressWarnings("unchecked")
public class AccountServiceExposureIT {

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
    public void testListAccounts() {
        WebTarget target = ClientBuilder.newClient().register(JacksonJaxbJsonProvider.class).target("http://localhost:7001/sample");
        Map<String, Object> response = target.path("accounts")
                .request()
                .accept("application/hal+json")
                .header("X-Client-Version", "1.0.0")
                .header("X-Service-Generation", "1")
                .header("X-Log-Token", DiagnosticContext.getLogToken())
                .get(Map.class);

        assertNotNull(response.get("_embedded"));
        Map<String, Object> embedded = (Map<String, Object>) response.get("_embedded");
        assertTrue(((List) embedded.get("accounts")).size() >= 2);
    }

    @Test
    public void testGetAccount() {
        WebTarget target = ClientBuilder.newClient().register(JacksonJaxbJsonProvider.class).target("http://localhost:7001/sample");
        Map<String, Object> response = target.path("accounts/5479-1234567")
                .request()
                .accept("application/hal+json")
                .header("X-Client-Version", "1.0.0")
                .header("X-Service-Generation", "1")
                .header("X-Log-Token", DiagnosticContext.getLogToken())
                .get(Map.class);

        assertEquals("5479", response.get("regNo"));
        assertEquals("1234567", response.get("accountNo"));
        assertEquals("Checking account", response.get("name"));
        assertNotNull(response.get("_links"));
        assertNotNull(response.get("_embedded"));
    }

    @Test
    public void testGetAccountUsingSpecificContentTypeWithParameters() {
        WebTarget target = ClientBuilder.newClient().register(JacksonJaxbJsonProvider.class).target("http://localhost:7001/sample");
        Map<String, Object> response = target.path("accounts").path("5479-1234567")
                .request()
                .accept("application/hal+json;concept=account;v=1")
                .header("X-Client-Version", "1.0.0")
                .header("X-Service-Generation", "1")
                .header("X-Log-Token", DiagnosticContext.getLogToken())
                .get(Map.class);
        assertEquals("5479", response.get("regNo"));
        assertEquals("1234567", response.get("accountNo"));
        assertEquals("Checking account", response.get("name"));
        assertNotNull(response.get("_links"));
        assertNull(response.get("_embedded"));
    }

    @Ignore("Ignored because a valid OAuth endpoint is not supplied")
    @Test
    public void testCreateAccount() throws Exception {

        String accessToken = requestAccessToken("advisor1");
        int accountNo = ThreadLocalRandom.current().nextInt(9999999);
        WebTarget bankServices =
                ClientBuilder.newClient().register(JacksonJaxbJsonProvider.class).target("http://localhost:7001/sample");
        Map<String, String> accountCreate = new ConcurrentHashMap<>();
        accountCreate.put("regNo", "5479");
        accountCreate.put("accountNo", Integer.toString(accountNo));
        accountCreate.put("name", "Savings account");
        Map<String, Object> response = bankServices.path("accounts").path("5479-" + accountNo)
                .request()
                .accept("application/hal+json")
                .header("X-Client-Version", "1.0.0")
                .header("X-Log-Token", DiagnosticContext.getLogToken())
                .header("X-Service-Generation", "1")
                .header("Authorization", "Bearer " + accessToken)
                .put(Entity.entity(accountCreate, MediaType.APPLICATION_JSON_TYPE), Map.class);

        assertEquals("5479", response.get("regNo"));
        assertEquals(Integer.toString(accountNo), response.get("accountNo"));
        assertEquals("Savings account", response.get("name"));
    }


    @Ignore("Ignored because a valid OAuth endpoint is not supplied")
    @Test
    public void testCreateAccountAccessDenied() throws Exception {
        String accessToken = requestAccessToken("customer1");

        WebTarget bankServices =
                ClientBuilder.newClient().register(JacksonJaxbJsonProvider.class).target("http://localhost:7001/sample");
        Map<String, String> accountCreate = new ConcurrentHashMap<>();
        accountCreate.put("regNo", "5479");
        accountCreate.put("accountNo", "5555555");
        accountCreate.put("name", "Checking account");
        Response response = bankServices.path("accounts").path("5479-5555555")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .accept("application/hal+json")
                .header("X-Client-Version", "1.0.0")
                .header("X-Service-Generation", "1")
                .header("X-Log-Token", DiagnosticContext.getLogToken())
                .header("Authorization", "Bearer " + accessToken)
                .put(Entity.entity(accountCreate, MediaType.APPLICATION_JSON_TYPE), Response.class);
        assertEquals(Response.Status.UNAUTHORIZED.getStatusCode(), response.getStatus());
    }

    @Ignore("Ignored because a valid OAuth endpoint is not supplied")
    @Test
    public void testUpdateAccount() throws Exception {
        String accessToken = requestAccessToken("advisor1");

        WebTarget bankServices =
                ClientBuilder.newClient().register(JacksonJaxbJsonProvider.class).target("http://localhost:7001/sample");
        Map<String, String> accountCreate = new ConcurrentHashMap<>();
        accountCreate.put("regNo", "5479");
        accountCreate.put("accountNo", "1234567");
        accountCreate.put("name", "new account name");
        Map<String, Object> response = bankServices.path("accounts").path("5479-1234567")
                .request()
                .accept("application/hal+json")
                .header("X-Client-Version", "1.0.0")
                .header("X-Service-Generation", "1")
                .header("X-Log-Token", DiagnosticContext.getLogToken())
                .header("Authorization", "Bearer " + accessToken)
                .put(Entity.entity(accountCreate, MediaType.APPLICATION_JSON_TYPE), Map.class);

        assertEquals("5479", response.get("regNo"));
        assertEquals("1234567", response.get("accountNo"));
        assertEquals("new account name", response.get("name"));
        accountCreate = new ConcurrentHashMap<>();
        accountCreate.put("regNo", "5479");
        accountCreate.put("accountNo", "1234567");
        accountCreate.put("name", "Checking account");
        response = bankServices.path("accounts").path("5479-" + "1234567")
                .request()
                .accept("application/hal+json")
                .header("X-Client-Version", "1.0.0")
                .header("X-Service-Generation", "1")
                .header("X-Log-Token", DiagnosticContext.getLogToken())
                .header("Authorization", "Bearer " + accessToken)
                .put(Entity.entity(accountCreate, MediaType.APPLICATION_JSON_TYPE), Map.class);

        assertEquals("5479", response.get("regNo"));
        assertEquals("1234567", response.get("accountNo"));
        assertEquals("Checking account", response.get("name"));
    }

    @Test
    public void testListTransactions() {
        WebTarget target = ClientBuilder.newClient().register(JacksonJaxbJsonProvider.class).target("http://localhost:7001/sample");
        Map<String, Object> response = target.path("accounts").path("5479-1234567").path("transactions")
                .queryParam("elements", "1|3")
                .queryParam("sort", "amount::+")
                .queryParam("interval", "from::-14d|to::now")
                .request()
                .accept("application/hal+json")
                .header("X-Client-Version", "1.0.0")
                .header("X-Service-Generation", "1")
                .header("X-Log-Token", DiagnosticContext.getLogToken())
                .get(Map.class);

        assertNotNull(response.get("_links"));
        assertNotNull(response.get("_embedded"));
        Map<String, Object> embedded = (Map<String, Object>) response.get("_embedded");
        assertTrue(((List) embedded.get("transactions")).size() == 0);

        response = target.path("accounts").path("5479-1234567").path("transactions")
                .queryParam("sort", "amount::+")
                .request()
                .accept("application/hal+json")
                .header("X-Client-Version", "1.0.0")
                .header("X-Service-Generation", "1")
                .header("X-Log-Token", DiagnosticContext.getLogToken())
                .get(Map.class);

        assertNotNull(response.get("_links"));
        assertNotNull(response.get("_embedded"));
        embedded = (Map<String, Object>) response.get("_embedded");
        assertTrue(((List) embedded.get("transactions")).size() >= 6);

        response = target.path("accounts").path("5479-1234567").path("transactions")
                .queryParam("elements", "1|3")
                .queryParam("sort", "amount::+")
                .request()
                .accept("application/hal+json")
                .header("X-Client-Version", "1.0.0")
                .header("X-Service-Generation", "1")
                .header("X-Log-Token", DiagnosticContext.getLogToken())
                .get(Map.class);

        assertNotNull(response.get("_links"));
        assertNotNull(response.get("_embedded"));
        embedded = (Map<String, Object>) response.get("_embedded");
        assertEquals(3, ((List) embedded.get("transactions")).size());
    }


    @Ignore("Ignored because a valid OAuth endpoint is not supplied")
    @Test
    public void testAddTransactions() throws UnsupportedEncodingException {
        WebTarget target = ClientBuilder.newClient().register(JacksonJaxbJsonProvider.class).target("http://localhost:7001/sample");
        String accessToken = requestAccessToken("tx-system-update");
        Map<String, Object> response = target.path("accounts").path("5479-1234567").path("transactions")
                .request()
                .accept("application/hal+json")
                .header("X-Client-Version", "1.0.0")
                .header("X-Service-Generation", "1")
                .header("X-Log-Token", DiagnosticContext.getLogToken())
                .get(Map.class);

        Map<String, Object> embedded = (Map<String, Object>) response.get("_embedded");
        int count = ((List) embedded.get("transactions")).size();

        Map<String, String> txCreate = new ConcurrentHashMap<>();
        txCreate.put("amount", "100.00");
        txCreate.put("description", "a 100 added to the account for blåbærgrød");
        response = target.path("accounts").path("5479-1234567").path("transactions")
                .path("one-long-technical-id-show")
                .request()
                .accept("application/hal+json")
                .header("X-Client-Version", "1.0.0")
                .header("X-Service-Generation", "1")
                .header("X-Log-Token", DiagnosticContext.getLogToken())
                .header("Authorization", "Bearer " + accessToken)
                .put(Entity.entity(txCreate, MediaType.APPLICATION_JSON_TYPE), Map.class);

        assertNotNull(response.get("_links"));

        response = target.path("accounts").path("5479-1234567").path("transactions")
                .queryParam("sort", "amount::+")
                .request()
                .accept("application/hal+json")
                .header("X-Client-Version", "1.0.0")
                .header("X-Service-Generation", "1")
                .header("X-Log-Token", DiagnosticContext.getLogToken())
                .get(Map.class);

        assertNotNull(response.get("_links"));
        assertNotNull(response.get("_embedded"));
        embedded = (Map<String, Object>) response.get("_embedded");
        assertEquals(count + 1, ((List) embedded.get("transactions")).size());

        txCreate = new ConcurrentHashMap<>();
        txCreate.put("amount", "1000,01"); //wrong format
        txCreate.put("description", "millennium just passed");
        try {
            response = target.path("accounts").path("5479-1234567").path("transactions")
                    .path("one-long-technical-id-show-too")
                    .request()
                    .accept("application/hal+json")
                    .header("X-Client-Version", "1.0.0")
                    .header("X-Service-Generation", "1")
                    .header("X-Log-Token", DiagnosticContext.getLogToken())
                    .header("Authorization", "Bearer " + accessToken)
                    .put(Entity.entity(txCreate, MediaType.APPLICATION_JSON_TYPE), Map.class);
            fail("Should not be able to handle amounts formatted with , it only supports .");
        } catch (BadRequestException bre) {
            //expected
        }

        txCreate = new ConcurrentHashMap<>();
        txCreate.put("amount", "1000.1");
        txCreate.put("description", "millennium just passed");
        try {
            response = target.path("accounts").path("5479-1234567").path("transactions")
                    .path("one-long-technical-id-show-too")
                    .request()
                    .accept("application/hal+json")
                    .header("X-Client-Version", "1.0.0")
                    .header("X-Service-Generation", "1")
                    .header("X-Log-Token", DiagnosticContext.getLogToken())
                    .header("Authorization", "Bearer " + accessToken)
                    .put(Entity.entity(txCreate, MediaType.APPLICATION_JSON_TYPE), Map.class);
            fail("Should not be able to handle amounts formatted without 2 decimals");
        } catch (BadRequestException bre) {
            //expected
        }
    }

    @Ignore("Ignored because a valid OAuth endpoint is not supplied")
    @Test
    public void testEventCollections() throws UnsupportedEncodingException {
        WebTarget target = ClientBuilder.newClient().register(JacksonJaxbJsonProvider.class).target("http://localhost:7001/sample");
        String accessToken = requestAccessToken("tx-system-update");
        Map<String, Object> response = target.path("account-events") //.path("5479-1234567")
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
        assertTrue(((List) embedded.get("events")).size() >= 8);

        response = target.path("account-events").path("5479-1234567")
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
        embedded = (Map<String, Object>) response.get("_embedded");
        assertTrue(((List) embedded.get("events")).size() >= 6);
    }

    @Test
    public void testTransactionCollections() {
        WebTarget target = ClientBuilder.newClient().register(JacksonJaxbJsonProvider.class).target("http://localhost:7001/sample");
        Map<String, Object> response = target.path("accounts").path("5479-1234567").path("transactions")
                .queryParam("sort", "amount::+")
                .queryParam("interval", "from::-14d|to::now")
                .request()
                .accept("application/hal+json")
                .header("X-Client-Version", "1.0.0")
                .header("X-Service-Generation", "1")
                .header("X-Log-Token", DiagnosticContext.getLogToken())
                .get(Map.class);

        assertNotNull(response.get("_links"));
        assertNotNull(response.get("_embedded"));
        Map<String, Object> embedded = (Map<String, Object>) response.get("_embedded");
        assertEquals(0, ((List) embedded.get("transactions")).size());

        ZonedDateTime zd = ZonedDateTime.of(2016, 1, 4, 8, 5, 10, 0, ZoneId.of("UTC"));

        response = target.path("accounts").path("5479-1234567").path("transactions")
                .queryParam("sort", "amount::+")
                .queryParam("interval", "from::" + zd.toInstant().toEpochMilli() + "|to::now")
                .request()
                .accept("application/hal+json")
                .header("X-Client-Version", "1.0.0")
                .header("X-Service-Generation", "1")
                .header("X-Log-Token", DiagnosticContext.getLogToken())
                .get(Map.class);

        assertNotNull(response.get("_links"));
        assertNotNull(response.get("_embedded"));
        embedded = (Map<String, Object>) response.get("_embedded");
        assertEquals(6, ((List) embedded.get("transactions")).size());

        zd = ZonedDateTime.of(2016, 1, 6, 8, 5, 10, 0, ZoneId.of("UTC"));

        response = target.path("accounts").path("5479-1234567").path("transactions")
                .queryParam("sort", "amount::+")
                .queryParam("interval", "from::" + zd.toInstant().toEpochMilli() + "|to::now")
                .request()
                .accept("application/hal+json")
                .header("X-Client-Version", "1.0.0")
                .header("X-Service-Generation", "1")
                .header("X-Log-Token", DiagnosticContext.getLogToken())
                .get(Map.class);

        assertNotNull(response.get("_links"));
        assertNotNull(response.get("_embedded"));
        embedded = (Map<String, Object>) response.get("_embedded");

        assertEquals(0, ((List) embedded.get("transactions")).size());
        zd = ZonedDateTime.of(2016, 1, 5, 8, 5, 10, 0, ZoneId.of("UTC"));

        response = target.path("accounts").path("5479-1234567").path("transactions")
                .queryParam("sort", "amount::+")
                .queryParam("interval", "from::" + zd.toInstant().toEpochMilli() + "|to::now")
                .request()
                .accept("application/hal+json")
                .header("X-Client-Version", "1.0.0")
                .header("X-Service-Generation", "1")
                .header("X-Log-Token", DiagnosticContext.getLogToken())
                .get(Map.class);

        assertNotNull(response.get("_links"));
        assertNotNull(response.get("_embedded"));
        embedded = (Map<String, Object>) response.get("_embedded");
        assertEquals(4, ((List) embedded.get("transactions")).size());
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
