package main.BACKEND;


import java.io.File;
import java.io.FileInputStream;
import java.sql.*;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class ExcelToMySQL {
    private static final String DB_URL = "jdbc:mysql://localhost:4545/dbproduct";
    private static final String USER = "root";
    private static final String PASSWORD = "admin";

    public static void main(String[] args) {
        String excelFilePath = "C:\\Users\\DELL\\IdeaProjects\\mypractice\\seleniumsessions\\src\\test\\java\\testdata\\product_category_transaction_data.xlsx";

        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASSWORD);
             FileInputStream fis = new FileInputStream(new File(excelFilePath));
             Workbook workbook = new XSSFWorkbook(fis)) {

            clearTables(conn);

            processSheet(conn, workbook.getSheet("Product Table"),
                    "INSERT INTO product (sno, txid, store, productid, title, price, added_at, last_updated) VALUES (?, ?, ?, ?, ?, ?, ?, ?)", 8);

            processSheet(conn, workbook.getSheet("Category Table"),
                    "INSERT INTO category (sno, txid, category_id, category, pid, affid1) VALUES (?, ?, ?, ?, ?, ?)", 6);

            processSheet(conn, workbook.getSheet("Transaction Table"),
                    "INSERT INTO transaction (sno, txid, sales, commission, order_date, status) VALUES (?, ?, ?, ?, ?, ?)", 6);

            System.out.println("Data inserted successfully!");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void clearTables(Connection conn) throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("DELETE FROM product");
            stmt.executeUpdate("DELETE FROM category");
            stmt.executeUpdate("DELETE FROM transaction");
            System.out.println("Tables cleared before inserting new data.");
        }
    }

    private static void processSheet(Connection conn, Sheet sheet, String insertSql, int expectedColumns) throws SQLException {
        if (sheet == null) {
            System.out.println("Sheet is null. Skipping.");
            return;
        }

        try (PreparedStatement psInsert = conn.prepareStatement(insertSql)) {
            for (Row row : sheet) {
                if (row.getRowNum() == 0) continue;

                for (int i = 0; i < expectedColumns; i++) {
                    setPreparedStatementValue(psInsert, i + 1, row.getCell(i));
                }
                psInsert.executeUpdate();
            }
        }
    }

    private static void setPreparedStatementValue(PreparedStatement ps, int index, Cell cell) throws SQLException {
        if (cell == null) {
            ps.setNull(index, Types.NULL);
            return;
        }
        switch (cell.getCellType()) {
            case STRING -> ps.setString(index, cell.getStringCellValue());
            case NUMERIC -> {
                if (DateUtil.isCellDateFormatted(cell)) {
                    ps.setDate(index, new java.sql.Date(cell.getDateCellValue().getTime()));
                } else {
                    ps.setDouble(index, cell.getNumericCellValue());
                }
            }
            case BOOLEAN -> ps.setBoolean(index, cell.getBooleanCellValue());
            case FORMULA -> ps.setString(index, cell.getCellFormula());
            default -> ps.setNull(index, Types.NULL);
        }
    }
}
