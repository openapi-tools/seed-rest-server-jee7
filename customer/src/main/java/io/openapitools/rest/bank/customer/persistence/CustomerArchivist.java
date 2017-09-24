package io.openapitools.rest.bank.customer.persistence;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceException;
import javax.persistence.TypedQuery;

import dk.nykredit.api.capabilities.Interval;
import io.openapitools.rest.bank.customer.model.Customer;
import io.openapitools.rest.bank.customer.model.Event;
import io.openapitools.rest.common.core.logging.LogDuration;

/**
 * Handles archiving (persistence) tasks for the customer domain model.
 */
@Stateless
@TransactionAttribute(TransactionAttributeType.REQUIRED)
public class CustomerArchivist {

    @PersistenceContext(unitName = "customerPersistenceUnit")
    private EntityManager em;

    @LogDuration(limit = 50)
    public List<Customer> listCustomers() {
        TypedQuery<Customer> q = em.createQuery("select c from Customer c", Customer.class);
        return q.getResultList();
    }

    /**
     * Find customer by its semantic key. Note this will throw {@link NoResultException} which will roll back the
     * transaction if the customer is not found - if this is a problem consider using
     * {@link #findCustomer(String)}.
     */
    @LogDuration(limit = 50)
    public Customer getCustomer(String customerNo) {
        TypedQuery<Customer> q = em.createQuery("select c from Customer c where c.sid=:sid", Customer.class);
        q.setParameter("sid", customerNo);
        return q.getSingleResult();
    }

    /**
     * Find customer by names. Note this will throw {@link NoResultException} which will roll back the
     * transaction if the customer is not found - if this is a problem consider using
     * {@link #findCustomer(String)}.
     */
    @LogDuration(limit = 50)
    public Customer findCustomerByNames(String firstName, String middleName, String sirname) {
        TypedQuery<Customer> q = em.createQuery("select c from Customer c where c.firstName=:firstName and " +
                "c.middleName=:middleName and c.sirname=:sirname", Customer.class);
        q.setParameter("firstName", firstName);
        q.setParameter("middleName", middleName);
        q.setParameter("sirname", sirname);
        return q.getSingleResult();
    }

    @LogDuration(limit = 50)
    public Optional<Customer> findCustomer(String customerNo) {
        try {
            return Optional.of(getCustomer(customerNo));
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }

    @LogDuration(limit = 50)
    public void save(Customer customer) {
        em.persist(customer);
    }

    public List<Event> findEvents(Optional<Interval> withIn) {
        StringBuilder qs = new StringBuilder("select e from Event e");
        if (withIn.isPresent()) {
            qs.append(" and t.lastModifiedTime>:startsAt and t.lastModifiedTime<:endsAt");
        }
        TypedQuery<Event> q = em.createQuery(qs.toString(), Event.class);
        if (withIn.isPresent()) {
            Timestamp ts = Timestamp.from(withIn.get().getStart().toInstant());
            q.setParameter("startsAt", ts);
            Timestamp te = Timestamp.from(withIn.get().getEnd().toInstant());
            q.setParameter("endsAt", te);
        }
        return q.getResultList();
    }

    public List<Event> getEventsForCategory(String category, Optional<Interval> withIn) {
        StringBuilder qs = new StringBuilder("select e from Event e where e.category=:category");
        if (withIn.isPresent()) {
            qs.append(" and t.lastModifiedTime>:startsAt and t.lastModifiedTime<:endsAt");
        }
        TypedQuery<Event> q = em.createQuery(qs.toString(), Event.class);
        q.setParameter("category", category);
        if (withIn.isPresent()) {
            Interval intv = withIn.get();
            q.setParameter("startsAt", Timestamp.from(intv.getStart().toInstant()));
            q.setParameter("endsAt", Timestamp.from(intv.getEnd().toInstant()));
        }
        return q.getResultList();
    }

    public Event getEvent(String category, String id) {
        TypedQuery<Event> q = em.createQuery("select e from Event e where e.category=:category and e.id=:sid", Event.class);
        q.setParameter("category", category);
        q.setParameter("sid", id);
        return q.getResultList().get(0);
    }

    public void save(Event customerChanged) {
        try {
            em.persist(customerChanged);
        } catch (PersistenceException pe) {
            // in the example there is no check for collision in sequence, the check for time and sequence uniqueness.
            // This check must be done in a real life example in order to have choice for that.
            // The sequence is imho less attractive as a chronological order for the events.
            // The sequence is that is dependent on central persistence and thus is not very cloud capable
            // and is going to pose a challenge (taking it to the NP level) in a true distributed setup.
        }
    }

}
