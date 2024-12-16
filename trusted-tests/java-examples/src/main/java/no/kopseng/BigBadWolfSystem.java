package no.kopseng;

import java.util.List;

public class BigBadWolfSystem {
    private final StringRepository repo;

    interface StringRepository {
        void save(String s);

        List<String> findAll();
    }

    public BigBadWolfSystem(StringRepository repo) {
        this.repo = repo;
    }

    public void startProcessingItems(List<String> items) throws InterruptedException {
        for (var item : items) {
            doExpensiveComputation();
            repo.save(item);
        }

        System.out.println("Success");
    }

    private static void doExpensiveComputation() throws InterruptedException {
        Thread.sleep((long) (Math.random() * 1_000));
    }
}
