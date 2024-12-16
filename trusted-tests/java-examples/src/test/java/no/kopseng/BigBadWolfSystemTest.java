package no.kopseng;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;

class BigBadWolfSystemTest {

    @Test
    void startProcessingItems() throws InterruptedException {
        final var repository = createRepository();
        final var sut = new BigBadWolfSystem(repository);

        final var items = List.of("item 1", "item 2");
        sut.startProcessingItems(items);

        await().atMost(5, SECONDS).until(() -> repository.findAll().containsAll(items));
    }

    public static BigBadWolfSystem.StringRepository createRepository() {
        return new BigBadWolfSystem.StringRepository() {
            Set<String> db = new HashSet<>();

            @Override
            public void save(String s) {
                db.add(s);
            }

            @Override
            public List<String> findAll() {
                return db.stream().toList();
            }
        };
    }
}