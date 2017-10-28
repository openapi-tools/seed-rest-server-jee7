package io.openapitools.sample.rest.common.rs;

import java.time.Instant;
import java.util.Date;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.EntityTag;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;

import io.openapitools.sample.rest.common.persistence.jpa.AbstractAuditable;

/**
 * Builder to build {@link Response} instances based on assumptions on the returned concept.
 *
 * @param <E> Entity type that this builder expects
 * @param <R> Representation type that this builder expects
 */
public class EntityResponseBuilder<E, R> {

    /**
     * Hal+json media type string.
     */
    public static final String APPLICATION_HAL_JSON = "application/hal+json";

    /**
     * Hal+json media type.
     */
    public static final MediaType APPLICATION_HAL_JSON_TYPE = MediaType.valueOf(APPLICATION_HAL_JSON);
    private final E entity;
    private final Function<E, R> mapper;

    private String name;
    private String version;
    private Integer maxAge;
    private String logToken;

    /**
     * the rate limits headers values as a number of requests left - default value for not set is "-1"
     */
    private String rateLimitTime2Reset = "-1";
    /**
     * the rate limits reset headers value denotes the time left to a reset of the rate limit in milliseconds - not set value is "-1"
     */
    private String rateLimit = "-1";
    /**
     * the rate limits 24h headers values is the number of requests for 24h - default value for not set is "-1"
     */
    private String rateLimit24h = "-1";
    /**
     * the rate limits remaining header values as a number of requests left - default value for not set is "-1"
     */
    private String rateLimitRemaining = "-1";

    public EntityResponseBuilder(E entity, Function<E, R> mapper) {
        this(entity, mapper, "");
    }

    /**
     * Construct new builder giving the entity and a mapper able to map the entity to a concrete representation. If the
     * given entity is an implementation of {@link AbstractAuditable} the last modified time from this will be use in
     * the <code>last-modified</code> header.
     */
    public EntityResponseBuilder(E entity, Function<E, R> mapper, String token) {
        this.entity = entity;
        this.mapper = mapper;
        this.logToken = (token != null && !"".equals(token.trim())) ? token : UUID.randomUUID().toString();
    }

    /**
     * transforms content parameters delivered as "concept" and "v" e.g.
     * <p>
     * "concept", "Account"
     * "v", "1.0.0"
     * <p>
     * into:
     * <p>
     * application/hal+json;concept=account;v=1.0.0
     * <p>
     */
    public static MediaType getMediaType(Map<String, String> parameters) {
        return new MediaType("application", "hal+json", parameters);
    }

    /**
     * Sets the concept name used in the <code>concept</code> content type parameter.
     */
    public EntityResponseBuilder<E, R> name(String name) {
        this.name = name;
        return this;
    }

    /**
     * Sets the concept version used in the <code>v</code> content type parameter.
     */
    public EntityResponseBuilder<E, R> version(String version) {
        this.version = version;
        return this;
    }

    /**
     * Sets the max age in seconds.
     */
    public EntityResponseBuilder<E, R> maxAge(int maxAge) {
        this.maxAge = maxAge;
        return this;
    }

    /**
     * Sets the rate limit calls per minute.
     */
    public EntityResponseBuilder<E, R> rateLimitPerMinute(long number) {
        this.rateLimit = Long.toString(number);
        return this;
    }

    /**
     * Sets the rate limit calls per 24 hours.
     */
    public EntityResponseBuilder<E, R> rateLimitPer24h(long number) {
        this.rateLimit24h = Long.toString(number);
        return this;
    }

    /**
     * Sets the rate limit reset time in milliseconds.
     */
    public EntityResponseBuilder<E, R> rateLimitReset(long time2Reset) {
        this.rateLimitTime2Reset = Long.toString(time2Reset);
        return this;
    }

    /**
     * Sets the rate limit reset time in milliseconds.
     */
    public EntityResponseBuilder<E, R> rateLimitRemaining(long number) {
        this.rateLimitRemaining = Long.toString(number);
        return this;
    }

    /**
     * Build a response given a concrete request. If the request contain an <code>if-modified-since</code> or
     * <code>if-none-match</code> header this will be checked against the entity given to the builder returning
     * a response with status not modified if appropriate.
     */
    public Response build(Request req) {
        EntityTag eTag = new EntityTag(Integer.toString(entity.hashCode()));
        Date lastModified = entity instanceof AbstractAuditable ? ((AbstractAuditable) entity).getLastModifiedTime() : Date.from(Instant.now());
        Response.ResponseBuilder notModifiedBuilder = req.evaluatePreconditions(lastModified, eTag);
        if (notModifiedBuilder != null) {
            return notModifiedBuilder.build();
        }

        Map<String, String> parameters = new ConcurrentHashMap<>();
        if (name != null) {
            parameters.put("concept", name);
        }
        if (version != null) {
            parameters.put("v", version);
        }
        MediaType type = getMediaType(parameters);

        Response.ResponseBuilder b = Response.ok(mapper.apply(entity))
            .type(type)
            .tag(eTag)
            .lastModified(lastModified)
            .header("X-Log-Token", logToken)
            .header("X-RateLimit-Limit", rateLimit)
            .header("X-RateLimit-Limit-24h", rateLimit24h)
            .header("X-RateLimit-Remaining", rateLimitRemaining)
            .header("X-RateLimit-Reset", rateLimitTime2Reset);

        if (maxAge != null) {
            CacheControl cc = new CacheControl();
            cc.setMaxAge(maxAge);
            b.cacheControl(cc).expires(Date.from(Instant.now().plusSeconds(maxAge)));
        }
        return b.build();
    }

}
