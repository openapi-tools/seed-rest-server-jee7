package dk.sample.rest.bank.account.persistence;

import java.sql.Timestamp;
import java.util.ArrayList;
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

import dk.sample.rest.bank.account.model.Account;
import dk.sample.rest.bank.account.model.Event;
import dk.sample.rest.bank.account.model.ReconciledTransaction;
import dk.sample.rest.bank.account.model.Transaction;
import dk.sample.rest.common.core.logging.LogDuration;
import io.openapitools.api.capabilities.Element;
import io.openapitools.api.capabilities.Interval;
import io.openapitools.api.capabilities.Sort;

/**
 * Handles archiving (persistence) tasks for the account domain model.
 */
@Stateless
@TransactionAttribute(TransactionAttributeType.REQUIRED)
public class AccountArchivist {
    private static final int TX_DEFAULTSIZE = 5;
    private static final int TX_MAXSIZE = 500;
    @PersistenceContext(unitName = "accountPersistenceUnit")
    private EntityManager em;

    @LogDuration(limit = 50)
    public List<Account> listAccounts() {
        TypedQuery<Account> q = em.createQuery("select a from Account a", Account.class);
        return q.getResultList();
    }

    /**
     * Find account by its primary key. Note this will throw {@link NoResultException} which will roll back the
     * transaction if the account is not found - if this is a problem consider using {@link #findAccount(String, String)}.
     */
    @LogDuration(limit = 50)
    public Account getAccount(String regNo, String accountNo) {
        TypedQuery<Account> q = em.createQuery("select a from Account a where a.regNo=:regNo and a.accountNo=:accountNo", Account.class);
        q.setParameter("regNo", regNo);
        q.setParameter("accountNo", accountNo);
        return q.getSingleResult();
    }

    @LogDuration(limit = 50)
    public Optional<Account> findAccount(String regNo, String accountNo) {
        try {
            return Optional.of(getAccount(regNo, accountNo));
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }

    @LogDuration(limit = 50)
    public void save(Account account) {
        em.persist(account);
    }

    @LogDuration(limit = 50)
    public Transaction getTransaction(String regNo, String accountNo, String id) {
        TypedQuery<Transaction> q = em.createQuery("select t from Transaction t " +
                "where t.account.regNo=:regNo and t.account.accountNo=:accountNo and t.id=:id", Transaction.class);
        q.setParameter("regNo", regNo);
        q.setParameter("accountNo", accountNo);
        q.setParameter("id", id);
        return q.getSingleResult();
    }

    @LogDuration(limit = 50)
    public Transaction findTransaction(String regNo, String accountNo, String id) {
        TypedQuery<Transaction> q = em.createQuery("select t from Transaction t " +
                "where t.account.regNo=:regNo and t.account.accountNo=:accountNo and t.id=:id", Transaction.class);
        q.setParameter("regNo", regNo);
        q.setParameter("accountNo", accountNo);
        q.setParameter("id", id);
        return q.getResultList().get(0);
    }

    @LogDuration(limit = 50)
    public ReconciledTransaction getReconciledTransaction(String regNo, String accountNo, String id) {
        TypedQuery<ReconciledTransaction> q = em.createQuery("select rt from ReconciledTransaction rt " +
                "where rt.account.regNo=:regNo and rt.account.accountNo=:accountNo and rt.id=:id", ReconciledTransaction.class);
        q.setParameter("regNo", regNo);
        q.setParameter("accountNo", accountNo);
        q.setParameter("id", id);
        return q.getSingleResult();
    }

    @LogDuration(limit = 50)
    public void save(ReconciledTransaction rt) {
        em.persist(rt);
    }

    /**
     * getting a set of transactions filtered according to the APi capabilities
     *
     * @param elementSet the number of element that is wished returned
     * @param withIn     the interval which the returned items needs to stay within
     * @param sortAs     attribute to sort by, default is time and supports sorting by amount
     */
    public List<Transaction> getTransactions(String regNo, String accountNo, Optional<Element> elementSet, Optional<Interval> withIn,
            List<Sort> sortAs) {
        StringBuilder qs = new StringBuilder("select t from Transaction t where t.account.regNo=:regNo and t.account.accountNo=:accountNo");
        if (withIn.isPresent()) {
            qs.append(" and t.lastModifiedTime>:startsAt and t.lastModifiedTime<:endsAt");
        }
        sortAs.stream()
                .filter(sort -> "amount".equals(sort.getAttribute()))
                .findAny()
                .ifPresent(sort -> qs.append(" order by t.").append(sort.getAttribute()).append(" ").append(sort.getDirection()));
        TypedQuery<Transaction> q = em.createQuery(qs.toString(), Transaction.class);
        q.setParameter("regNo", regNo);
        q.setParameter("accountNo", accountNo);
        if (withIn.isPresent()) {
            Timestamp ts = Timestamp.from(withIn.get().getStart().toInstant());
            q.setParameter("startsAt", ts);
            Timestamp te = Timestamp.from(withIn.get().getEnd().toInstant());
            q.setParameter("endsAt", te);
        }
        List<Transaction> txs = q.setMaxResults(TX_MAXSIZE).getResultList();
        if ((TX_DEFAULTSIZE < txs.size() && (elementSet.isPresent()))) {
            return reduceElements(elementSet.get(), txs);
        }
        return txs;

    }

    /**
     * this merely shows that the persistence does not have to support the complete API Capability set
     * sometimes the use of these capabilities will cause the query to be designed in order to deliver results
     * in a good and efficient way to the users of the API.
     */
    private List<Transaction> reduceElements(Element elementSet, List<Transaction> txs) {
        int elements = elementSet.getEnd() - elementSet.getStart() + 1;
        List<Transaction> txr = new ArrayList<>(elements);
        int no = 0;
        for (Transaction tx : txs) {
            no++;
            if ((no >= elementSet.getStart()) && (no <= elementSet.getEnd())) {
                txr.add(tx);
            }
        }
        return txr;

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

    public void save(Event newTX) {
        try {
            em.persist(newTX);
        } catch (PersistenceException pe) {
            // in the example there is no check for collision in sequence, the check for time and sequence uniqueness.
            // This check must be done in a real life example in order to have choice for that.
            // The sequence is imho less attractive as a chronological order for the events.
            // The sequence is that is dependent on central persistence and thus is not very cloud capable
            // and is going to pose a challenge (taking it to the NP level) in a true distributed setup.
        }
    }

}
