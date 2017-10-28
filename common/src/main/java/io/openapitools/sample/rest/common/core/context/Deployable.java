package io.openapitools.sample.rest.common.core.context;

import java.io.Serializable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * Represents one deployable within an application.
 */
public class Deployable implements Serializable {

    public static final String CONFIG_APPLICATION_NAME = "application.name";

    private static final long serialVersionUID = 1L;
    private static final Pattern REGEX_VERSION = Pattern.compile("v?(\\d+)\\.(\\d+)\\.(\\d+)(-([\\w\\.]+))?(\\+([\\w\\.]+))?");


    /**
     * @serial Deployable name
     */
    private final String name;

    /**
     * @serial Deployable version identifier as according to the <a href="http://semver.org">semver</a> standard.
     */
    private final String version;

    /**
     * Create new deployable.
     *
     * @param name        Name uniquely identifying the deployable within the application
     * @param version     Version identification string according to the <a href="http://semver.org">semver</a>
     *                    standard, e.g., 2.1.0-SNAPSHOT<br>
     *                    The version string may optionally start with "v", e.g., v2.1.0-SNAPSHOT
     */
    public Deployable(String name, String version) {

        if (name == null || name.length() == 0) {
            throw new IllegalArgumentException("'name' cannot be null or empty");
        }

        if (version == null || version.length() == 0) {
            throw new IllegalArgumentException("'version' cannot be null or empty");
        }
        Matcher m = REGEX_VERSION.matcher(version);
        if (!m.matches()) {
            throw new IllegalArgumentException("'version' format invalid [" + version + "] must follow semver.org specification");
        }
        this.name = name;
        this.version = version;
    }



    public String getName() {
        return name;
    }

    public String getVersion() {
        return version;
    }

    public int getVersionMajor() {
        Matcher m = REGEX_VERSION.matcher(version);
        if (m.matches()) {
            return Integer.parseInt(m.group(1));
        }
        throw new IllegalStateException("The deployable contained an invalid version string [" + version + "]");
    }

    public int getVersionMinor() {
        Matcher m = REGEX_VERSION.matcher(version);
        if (m.matches()) {
            return Integer.parseInt(m.group(2));
        }
        throw new IllegalStateException("The deployable contained an invalid version string [" + version + "]");
    }

    public int getVersionPatch() {
        Matcher m = REGEX_VERSION.matcher(version);
        if (m.matches()) {
            return Integer.parseInt(m.group(3));
        }
        throw new IllegalStateException("The deployable contained an invalid version string [" + version + "]");
    }

    public String getVersionPreRelease() {
        Matcher m = REGEX_VERSION.matcher(version);
        if (m.matches()) {
            return m.group(5);
        }
        throw new IllegalStateException("The deployable contained an invalid version string [" + version + "]");
    }

    public String getVersionBuild() {
        Matcher m = REGEX_VERSION.matcher(version);
        if (m.matches()) {
            return m.group(7);
        }
        throw new IllegalStateException("The deployable contained an invalid version string [" + version + "]");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Deployable that = (Deployable) o;
        return new EqualsBuilder()
                .append(name, that.name)
                .append(version, that.version)
                .build();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(name)
                .append(version)
                .build();
    }

    @Override
    public String toString() {
        return name + "-" + version;
    }
}
