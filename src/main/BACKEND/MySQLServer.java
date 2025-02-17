package main.BACKEND;

import static spark.Spark.*;
import java.sql.*;
import java.util.*;
import java.util.Date;

import com.google.gson.Gson;

public class MySQLServer {
    private static final String DB_URL = "jdbc:mysql://localhost:4545/dbproduct";
    private static final String USER = "root";
    private static final String PASSWORD = "admin";

    static class Product {
        public int sno;
        public String txid;
        public String store;
        public String productid;
        public String title;
        public double price;
        public Date added_at;
        public Date last_updated;
    }

    static class Category {
        public int sno;
        public String txid;
        public String category_id;
        public String category;
        public String pid;
        public String affid1;
    }

    static class Transaction {
        public int sno;
        public String txid;
        public double sales;
        public double commission;
        public Date order_date;
        public String status;
    }

    public static void main(String[] args) {
        port(8080); // Spark Server Port

        enableCORS("*", "*", "*"); // Allow frontend to access API

        // Fetch all products (with optional sorting)
        get("/api/product", (req, res) -> {
            res.type("application/json");
            List<Product> products = getAllProducts();

            String sortBy = req.queryParams("sortBy"); // "title", "price", etc.
            String order = req.queryParams("order");   // "asc" or "desc"

            if (sortBy != null) {
                switch (sortBy.toLowerCase()) {
                    case "title":
                        products.sort(Comparator.comparing(p -> p.title.toLowerCase()));
                        break;
                    case "price":
                        products.sort(Comparator.comparingDouble(p -> p.price));
                        break;
                    case "store":
                        products.sort(Comparator.comparing(p -> p.store.toLowerCase()));
                        break;
                }
                if ("desc".equalsIgnoreCase(order)) {
                    Collections.reverse(products);
                }
            }
            return new Gson().toJson(products);
        });

        // Fetch all categories
        get("/api/category", (req, res) -> {
            res.type("application/json");
            return new Gson().toJson(getAllCategories());
        });

        // Fetch all transactions
        get("/api/transaction", (req, res) -> {
            res.type("application/json");
            return new Gson().toJson(getAllTransactions());
        });

        // Filtering API (by store & txid)
        get("/api/product/filter", (req, res) -> {
            res.type("application/json");

            String titleParam = req.queryParams("title");
            String txidParam  = req.queryParams("txid");

            // If both are empty, return all
            if ((titleParam == null || titleParam.isBlank()) &&
                    (txidParam == null  || txidParam.isBlank())) {
                return new Gson().toJson(getAllProducts());
            }

            // Build a dynamic query
            StringBuilder sql = new StringBuilder("SELECT * FROM product WHERE 1=1");
            List<Object> params = new ArrayList<>();

            // Filter by title (partial match)
            if (titleParam != null && !titleParam.isBlank()) {
                sql.append(" AND LOWER(title) LIKE ?");
                params.add("%" + titleParam.toLowerCase() + "%");
            }

            // Filter by txid (exact match)
            if (txidParam != null && !txidParam.isBlank()) {
                sql.append(" AND txid = ?");
                params.add(txidParam);
            }

            List<Product> results = new ArrayList<>();

            try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASSWORD);
                 PreparedStatement ps = conn.prepareStatement(sql.toString())) {

                // Set parameter values
                for (int i = 0; i < params.size(); i++) {
                    ps.setObject(i + 1, params.get(i));
                }

                // Execute and map results
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        Product p = new Product();
                        p.sno       = rs.getInt("sno");
                        p.txid      = rs.getString("txid");
                        p.store     = rs.getString("store");
                        p.productid = rs.getString("productid");
                        p.title     = rs.getString("title");
                        p.price     = rs.getDouble("price");
                        // fill other fields if needed (added_at, last_updated, etc.)
                        results.add(p);
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }

            return new Gson().toJson(results);
        });


        get("/api/category/filter", (req, res) -> {
            res.type("application/json");

            // Optional query parameters
            String categoryParam = req.queryParams("category"); // partial match
            String txidParam     = req.queryParams("txid");     // exact match

            // If both are blank, return all categories
            if ((categoryParam == null || categoryParam.isBlank()) &&
                    (txidParam == null  || txidParam.isBlank())) {
                return new Gson().toJson(getAllCategories());
            }

            // Build dynamic query
            StringBuilder sql = new StringBuilder("SELECT * FROM category WHERE 1=1");
            List<Object> params = new ArrayList<>();

            // If categoryParam present -> partial match on category column
            if (categoryParam != null && !categoryParam.isBlank()) {
                sql.append(" AND LOWER(category) LIKE ?");
                params.add("%" + categoryParam.toLowerCase() + "%");
            }
            // If txidParam present -> exact match on txid column
            if (txidParam != null && !txidParam.isBlank()) {
                sql.append(" AND txid = ?");
                params.add(txidParam);
            }

            List<Category> results = new ArrayList<>();

            try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASSWORD);
                 PreparedStatement ps = conn.prepareStatement(sql.toString())) {

                // Fill in placeholders
                for (int i = 0; i < params.size(); i++) {
                    ps.setObject(i + 1, params.get(i));
                }

                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        Category c = new Category();
                        c.sno           = rs.getInt("sno");
                        c.txid          = rs.getString("txid");
                        c.category_id   = rs.getString("category_id");
                        c.category      = rs.getString("category");
                        c.pid           = rs.getString("pid");
                        c.affid1        = rs.getString("affid1");
                        results.add(c);
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }

            return new Gson().toJson(results);
        });

        get("/api/transaction/filter", (req, res) -> {
            res.type("application/json");

            // Optional query parameters
            String statusParam = req.queryParams("status");  // partial match
            String txidParam   = req.queryParams("txid");    // exact match

            // If both blank, return all transactions
            if ((statusParam == null || statusParam.isBlank()) &&
                    (txidParam == null  || txidParam.isBlank())) {
                return new Gson().toJson(getAllTransactions());
            }

            // Build dynamic query
            StringBuilder sql = new StringBuilder("SELECT * FROM transaction WHERE 1=1");
            List<Object> params = new ArrayList<>();

            // If statusParam present -> partial match on status
            if (statusParam != null && !statusParam.isBlank()) {
                sql.append(" AND LOWER(status) LIKE ?");
                params.add("%" + statusParam.toLowerCase() + "%");
            }
            // If txidParam present -> exact match on txid
            if (txidParam != null && !txidParam.isBlank()) {
                sql.append(" AND txid = ?");
                params.add(txidParam);
            }

            List<Transaction> results = new ArrayList<>();

            try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASSWORD);
                 PreparedStatement ps = conn.prepareStatement(sql.toString())) {

                // Fill in placeholders
                for (int i = 0; i < params.size(); i++) {
                    ps.setObject(i + 1, params.get(i));
                }

                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        Transaction t = new Transaction();
                        t.sno         = rs.getInt("sno");
                        t.txid        = rs.getString("txid");
                        t.sales       = rs.getDouble("sales");
                        t.commission  = rs.getDouble("commission");
                        t.order_date  = rs.getDate("order_date");
                        t.status      = rs.getString("status");
                        results.add(t);
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }

            return new Gson().toJson(results);
        });





        System.out.println("Spark server is running on http://localhost:8080 ...");
    }

    private static void enableCORS(final String origin, final String methods, final String headers) {
        options("/*", (request, response) -> {
            String accessControlRequestHeaders = request.headers("Access-Control-Request-Headers");
            if (accessControlRequestHeaders != null) {
                response.header("Access-Control-Allow-Headers", accessControlRequestHeaders);
            }
            String accessControlRequestMethod = request.headers("Access-Control-Request-Method");
            if (accessControlRequestMethod != null) {
                response.header("Access-Control-Allow-Methods", accessControlRequestMethod);
            }
            return "OK";
        });

        before((request, response) -> {
            response.header("Access-Control-Allow-Origin", origin);
            response.header("Access-Control-Request-Method", methods);
            response.header("Access-Control-Allow-Headers", headers);
        });
    }

    // Retrieve all products
    private static List<Product> getAllProducts() {
        List<Product> list = new ArrayList<>();
        String sql = "SELECT sno, txid, store, productid, title, price, added_at, last_updated FROM product";

        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASSWORD);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                list.add(mapProduct(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // Retrieve all categories
    private static List<Category> getAllCategories() {
        List<Category> list = new ArrayList<>();
        String sql = "SELECT sno, txid, category_id, category, pid, affid1 FROM category";

        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASSWORD);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Category c = new Category();
                c.sno = rs.getInt("sno");
                c.txid = rs.getString("txid");
                c.category_id = rs.getString("category_id");
                c.category = rs.getString("category");
                c.pid = rs.getString("pid");
                c.affid1 = rs.getString("affid1");
                list.add(c);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // Retrieve all transactions
    private static List<Transaction> getAllTransactions() {
        List<Transaction> list = new ArrayList<>();
        String sql = "SELECT sno, txid, sales, commission, order_date, status FROM transaction";

        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASSWORD);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Transaction t = new Transaction();
                t.sno = rs.getInt("sno");
                t.txid = rs.getString("txid");
                t.sales = rs.getDouble("sales");
                t.commission = rs.getDouble("commission");
                t.order_date = rs.getDate("order_date");
                t.status = rs.getString("status");
                list.add(t);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // Filtering products based on store & txid
    private static List<Product> getFilteredProducts(String store, String txid) {
        List<Product> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT * FROM product WHERE 1=1");
        List<Object> params = new ArrayList<>();

        if (store != null && !store.isBlank()) {
            sql.append(" AND store = ?");
            params.add(store);
        }
        if (txid != null && !txid.isBlank()) {
            sql.append(" AND txid = ?");
            params.add(txid);
        }

        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASSWORD);
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapProduct(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // Utility method to map ResultSet to Product object
    private static Product mapProduct(ResultSet rs) throws SQLException {
        Product p = new Product();
        p.sno = rs.getInt("sno");
        p.txid = rs.getString("txid");
        p.store = rs.getString("store");
        p.productid = rs.getString("productid");
        p.title = rs.getString("title");
        p.price = rs.getDouble("price");
        p.added_at = rs.getDate("added_at");
        p.last_updated = rs.getDate("last_updated");
        return p;
    }
}
