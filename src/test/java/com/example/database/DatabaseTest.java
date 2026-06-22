package com.example.database;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class DatabaseTest {
    private Db db;
    @BeforeEach
    void setupDatabase() {
        db = new Db("test.db");
    }

    @AfterEach
    void cleanup() {
        db.deleteAll();
    }

    @Test
    void testInsert() {
        int countBefore = db.count();
        Product product = new Product("Сир", 10, 80.99, "Молочні продукти");
        int generatedId = db.insert(product);
        int countAfter = db.count();

        assertTrue(generatedId > 0);
        assertEquals(countBefore + 1, countAfter);

        Optional<Product> retrievedOptional = db.getById(generatedId);
        assertTrue(retrievedOptional.isPresent());

        Product retrievedProduct = retrievedOptional.get();

        assertEquals("Сир", retrievedProduct.getName());
        assertEquals(10, retrievedProduct.getRemainder());
        assertEquals(80.99, retrievedProduct.getPrice());
        assertEquals("Молочні продукти", retrievedProduct.getCategory());

    }
    @Test
    void testGetByIdEmpty() {
        Optional<Product> result = db.getById(999);
        assertTrue(result.isEmpty());
    }

    @Test
    void testUpdateProducts() {
        int id = db.insert(new Product("123", 5, 50.0, "987"));
        Product productToUpdate = new Product(id, "new 123", 15, 65.0, "new 987");

        boolean isUpdated = db.update(productToUpdate);

        assertTrue(isUpdated);
        Optional<Product> retrievedOptional = db.getById(id);
        assertTrue(retrievedOptional.isPresent());

        Product retrievedProduct = retrievedOptional.get();

        assertEquals("new 123", retrievedProduct.getName());
        assertEquals(15, retrievedProduct.getRemainder());
        assertEquals(65.0, retrievedProduct.getPrice());
    }

    @Test
    void testUpdateNonExistentProduct() {
        Product fakeProduct = new Product(9999, "fake", 1, 1.0, "fake");
        boolean isUpdated = db.update(fakeProduct);
        assertFalse(isUpdated);
    }

    @Test
    void testDelete() {
        int id = db.insert(new Product("1", 1, 1.0, "1"));
        assertEquals(1, db.count());

        boolean isDeleted = db.delete(id);

        assertTrue(isDeleted);
        assertEquals(0, db.count());
        assertTrue(db.getById(id).isEmpty());
    }

    @Nested
    class FilterTests {

        @BeforeEach
        void fillSampleData() {
            db.insert(new Product("milk", 15, 34.99, "milk products"));
            db.insert(new Product("cheese", 8, 300.0, "milk products"));
            db.insert(new Product("white bread", 20, 20.00, "bread"));
            db.insert(new Product("black bread", 40, 25.00, "bread"));
        }

        @Test
        void testGetAllNoFilters() {
            ProductFilters emptyFilters = new ProductFilters();
            List<Product> results = db.getAll(emptyFilters);
            assertEquals(4, results.size());
        }

        @Test
        void testFilterByName() {
            ProductFilters filters = new ProductFilters();
            filters.setName("bread");

            List<Product> results = db.getAll(filters);

            assertEquals(2, results.size());
            assertTrue("white bread".equals(results.get(0).getName()) || "white bread".equals(results.get(1).getName()));
            assertTrue("black bread".equals(results.get(0).getName()) || "black bread".equals(results.get(1).getName()));
        }

        @Test
        void testFilterByPriceRange() {
            ProductFilters filters = new ProductFilters();
            filters.setPriceFrom(25.00);
            filters.setPriceTo(35.00);

            List<Product> results = db.getAll(filters);

            assertEquals(2, results.size());
            assertTrue("black bread".equals(results.get(0).getName()) || "black bread".equals(results.get(1).getName()));
            assertTrue("milk".equals(results.get(0).getName()) || "milk".equals(results.get(1).getName()));
        }

        @Test
        void testFilterByCategory() {
            ProductFilters filters = new ProductFilters();
            filters.setCategory("bread");

            List<Product> results = db.getAll(filters);

            assertEquals(2, results.size());
            assertTrue("white bread".equals(results.get(0).getName()) || "white bread".equals(results.get(1).getName()));
            assertTrue("black bread".equals(results.get(0).getName()) || "black bread".equals(results.get(1).getName()));
        }

        @Test
        void testFiltersCombined() {
            ProductFilters filters = new ProductFilters();
            filters.setCategory("bread");
            filters.setPriceTo(22.00);

            List<Product> results = db.getAll(filters);

            assertEquals(1, results.size());
            assertEquals("white bread", results.getFirst().getName());
        }
    }

    @Test
    void testPagination() {
        Product product = new Product("milk", 15, 34.99, "milk products");

        for (int i = 0; i < 101; i++) {
            db.insert(product);
        }
        assertEquals(101, db.count());

        ProductFilters filters = new ProductFilters();
        List<Product> first_page = db.getAll(filters);
        assertEquals(50, first_page.size());

        filters.setPage(3);
        List<Product> third_page = db.getAll(filters);
        assertEquals(1, third_page.size());
    }
}
