package com.example.database;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class Db {
    private final Connection connection;

    public Db(String dbName) {
        try {
            this.connection = DriverManager.getConnection("jdbc:sqlite:" + dbName);
        } catch (SQLException e) {
            throw new RuntimeException("Couldn't create database connection", e);
        }

        init();
    }

    public List<Product> getAll(ProductFilters filters) {
        String subquery = "";
        List<Object> params = new ArrayList<>();
        if (filters != null) {
            List<String> filterStrings = new ArrayList<>();
            if (filters.getName() != null) {
                filterStrings.add("name LIKE %?%");
                params.add(filters.getName());
            }
            if (filters.getPriceFrom() != null) {
                filterStrings.add("price >= ?");
                params.add(filters.getPriceFrom());
            }
            if (filters.getPriceTo() != null) {
                filterStrings.add("price <= ?");
                params.add(filters.getPriceTo());
            }
            if (filters.getRemainderFrom() != null) {
                filterStrings.add("remainder >= ?");
                params.add(filters.getRemainderFrom());
            }
            if (filters.getRemainderTo() != null) {
                filterStrings.add("remainder <= ?");
                params.add(filters.getRemainderTo());
            }
            if (filters.getCategory() != null) {
                filterStrings.add("category LIKE %?%");
                params.add(filters.getCategory());
            }
            subquery = " WHERE " + String.join(" AND ", filterStrings);
        }
        try (PreparedStatement ps = connection.prepareStatement("SELECT * FROM Product" + subquery)) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i+1, params.get(i));
            }
            List<Product> products = new ArrayList<>();
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    products.add(new Product(rs.getInt("id"), rs.getString("name"), rs.getInt("remainder"),
                            rs.getDouble("price"), rs.getString("category")));
                }
            }
            return products;
        } catch (SQLException e) {
            throw new RuntimeException("Couldn't get products", e);
        }
    }

    public Optional<Product> getById(int id) {
        try (PreparedStatement ps = connection.prepareStatement("SELECT * FROM Product WHERE id = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(new Product(rs.getInt("id"), rs.getString("name"), rs.getInt("remainder"),
                            rs.getDouble("price"), rs.getString("category")));
                }
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new RuntimeException("Couldn't get product by id: " + id, e);
        }
    }

    public int insert(Product product) {
        try (PreparedStatement ps = connection.prepareStatement("INSERT INTO Product (name, remainder, price, category) VALUES (?, ?, ?, ?)",
                Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, product.getName());
            ps.setInt(2, product.getRemainder());
            ps.setDouble(3, product.getPrice());
            ps.setString(4, product.getCategory());
            int inserted = ps.executeUpdate();

            if (inserted < 1) {
                throw new RuntimeException("Insert failed");
            }

            ResultSet generatedKeys = ps.getGeneratedKeys();
            return generatedKeys.getInt(1);
        } catch (SQLException e) {
            throw new RuntimeException("Couldn't insert product: " + product, e);
        }
    }

    public boolean update(Product product) {
        try (PreparedStatement ps = connection.prepareStatement(
                "UPDATE Product SET name = ?, remainder = ?, price = ?, category = ? WHERE id = ?")) {
            ps.setString(1, product.getName());
            ps.setInt(2, product.getRemainder());
            ps.setDouble(3, product.getPrice());
            ps.setString(4, product.getCategory());
            ps.setInt(5, product.getId());
            int updated = ps.executeUpdate();

            return updated >= 1;
        } catch (SQLException e) {
            throw new RuntimeException("Couldn't update product: " + product, e);
        }
    }

    public boolean delete(int id) {
        try (PreparedStatement ps = connection.prepareStatement("DELETE FROM Product WHERE id = ?")) {
            ps.setInt(1, id);
            int deleted = ps.executeUpdate();

            return deleted >= 1;
        } catch (SQLException e) {
            throw new RuntimeException("Couldn't delete product with id: " + id, e);
        }
    }


    private void init() {
        try (Statement statement = connection.createStatement()) {
            statement.execute("""
                    CREATE TABLE IF NOT EXISTS Product (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        name VARCHAR(50) NOT NULL,
                        remainder INTEGER NOT NULL,
                        price DECIMAL(13, 2),
                        category VARCHAR(50)
                    )""");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
