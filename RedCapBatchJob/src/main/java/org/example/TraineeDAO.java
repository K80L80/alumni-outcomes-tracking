package org.example;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.TypedQuery;
import java.time.LocalDate;
import java.util.List;
//Java Persistence Query Language (JPQL) –– Object-Oriented Queries meaning it operates on java objects instead of database directly
//Write JPA translates them into SQL
public class TraineeDAO {
    private EntityManagerFactory emf = Persistence.createEntityManagerFactory("TraineePU");

    //Run this query and give me back a list of Trainee objects.
    public List<Trainee> getFormerTrainees() {
        EntityManager em = emf.createEntityManager();
        String jpqlQuery = "SELECT t FROM Trainee t WHERE t.endDate < CURRENT_DATE";
        TypedQuery<Trainee> query = em.createQuery(jpqlQuery, Trainee.class);
        List<Trainee> formerTrainees = query.getResultList();
        em.close();
        return formerTrainees;
    }

    public void close() {
        emf.close();
    }

    public void insertTrainee(Trainee trainee) {
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        em.persist(trainee);
        em.getTransaction().commit();
        em.close();
    }

    public void createTestTrainees() {
        // Adding sample Trainee records using the insertTrainee method
        insertTrainee(new Trainee("John", "Doe", "john.doe@example.com", LocalDate.of(2023, 11, 1)));
        insertTrainee(new Trainee("Jane", "Smith", "jane.smith@example.com", LocalDate.of(2022, 5, 15)));
        insertTrainee(new Trainee("Alice", "Johnson", "alice.johnson@example.com", LocalDate.of(2024, 1, 10)));
        insertTrainee(new Trainee("Bob", "Brown", "bob.brown@example.com", LocalDate.of(2020, 12, 25)));
    }
}