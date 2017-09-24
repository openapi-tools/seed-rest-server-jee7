package io.openapitools.rest.bank.customer.model;

import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import io.openapitools.rest.common.persistence.jpa.AbstractAuditable;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * Very basic modelling of customer concept to show the basic use of JPA for persistence handling.
 */
@Entity
@Table(name = "CUSTOMER")
public class Customer extends AbstractAuditable {
    /**
     * TID - the technical unique identifier for instance, i.e., primary key. This should NEVER EVER be
     * exposed out side the service since it is a key very internal to this service.
     */
    @Id
    @Column(name = "TID", length = 36, nullable = false, columnDefinition = "CHAR(36)")
    private String tId;

    @Column(name = "SID", length = 10, nullable = false, columnDefinition = "CHAR(10)")
    private String sid;

    @Column(name = "FIRSTNAME", length = 60, nullable = false)
    private String firstName;

    @Column(name = "MIDDLENAME", length = 60, nullable = false)
    private String middleName;

    @Column(name = "SIRNAME", length = 60, nullable = false)
    private String sirname;


    protected Customer() {
        // Required by JPA
    }

    public Customer(String firstName, String middleName, String sirname) {
        this.firstName = firstName;
        this.middleName = middleName;
        this.sirname = sirname;
        tId = UUID.randomUUID().toString();
        sid = String.valueOf(tId.hashCode() > 0 ? tId.hashCode() : tId.hashCode() * -1);
    }

    Customer(String firstName, String middleName, String sirname, String sid) {
        this.firstName = firstName;
        this.middleName = middleName;
        this.sirname = sirname;
        tId = UUID.randomUUID().toString();
        this.sid = sid;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getMiddleName() {
        return middleName;
    }

    public String getSirname() {
        return sirname;
    }

    public void setSirname(String sirname) {
        this.sirname = sirname;
    }

    public String getSid() {
        return sid;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
            .append("firstName", firstName)
            .append("middleName", middleName)
                .append("sirname", sirname)
                .append("sid", sid)
            .toString();
    }

}
