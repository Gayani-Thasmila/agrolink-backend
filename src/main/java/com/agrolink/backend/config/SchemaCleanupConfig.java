package com.agrolink.backend.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;

@Configuration
public class SchemaCleanupConfig {

    private static final Logger log = LoggerFactory.getLogger(SchemaCleanupConfig.class);

    @Bean
    public ApplicationRunner cleanupLegacySchema(JdbcTemplate jdbcTemplate) {
        return args -> {
            dropLegacyOrderUserForeignKey(jdbcTemplate);
            cleanupLegacyOrderItemsSchema(jdbcTemplate);
        };
    }

    private void dropLegacyOrderUserForeignKey(JdbcTemplate jdbcTemplate) {
        String constraintName = jdbcTemplate.query(
                """
                SELECT CONSTRAINT_NAME
                FROM information_schema.KEY_COLUMN_USAGE
                WHERE TABLE_SCHEMA = DATABASE()
                  AND TABLE_NAME = 'orders'
                  AND COLUMN_NAME = 'user_id'
                  AND REFERENCED_TABLE_NAME = 'users'
                """,
                rs -> rs.next() ? rs.getString("CONSTRAINT_NAME") : null
        );

        if (constraintName == null || constraintName.isBlank()) {
            return;
        }

        jdbcTemplate.execute("ALTER TABLE orders DROP FOREIGN KEY " + constraintName);
        log.info("Dropped legacy foreign key {} from orders.user_id to users.user_id", constraintName);
    }

    private void cleanupLegacyOrderItemsSchema(JdbcTemplate jdbcTemplate) {
        if (!tableExists(jdbcTemplate, "order_items")) {
            return;
        }

        if (columnExists(jdbcTemplate, "order_items", "products_id")) {
            int migratedRows = jdbcTemplate.update(
                    """
                    UPDATE order_items
                    SET product_id = products_id
                    WHERE product_id IS NULL
                      AND products_id IS NOT NULL
                    """
            );
            log.info("Backfilled {} legacy order_items rows from products_id into product_id", migratedRows);

            for (String foreignKeyName : findForeignKeys(jdbcTemplate, "order_items", "products_id")) {
                jdbcTemplate.execute("ALTER TABLE order_items DROP FOREIGN KEY " + foreignKeyName);
                log.info("Dropped legacy foreign key {} from order_items.products_id", foreignKeyName);
            }

            for (String indexName : findIndexes(jdbcTemplate, "order_items", "products_id")) {
                jdbcTemplate.execute("ALTER TABLE order_items DROP INDEX " + indexName);
                log.info("Dropped legacy index {} from order_items.products_id", indexName);
            }

            jdbcTemplate.execute("ALTER TABLE order_items DROP COLUMN products_id");
            log.info("Dropped legacy order_items.products_id column");
        }

        for (String foreignKeyName : findForeignKeys(jdbcTemplate, "order_items", "product_id", "products")) {
            jdbcTemplate.execute("ALTER TABLE order_items DROP FOREIGN KEY " + foreignKeyName);
            log.info("Dropped legacy foreign key {} from order_items.product_id to products.product_id", foreignKeyName);
        }

        Integer autoIncrementFlag = jdbcTemplate.queryForObject(
                """
                SELECT CASE WHEN EXTRA LIKE '%auto_increment%' THEN 1 ELSE 0 END
                FROM information_schema.COLUMNS
                WHERE TABLE_SCHEMA = DATABASE()
                  AND TABLE_NAME = 'order_items'
                  AND COLUMN_NAME = 'order_item_id'
                """,
                Integer.class
        );
        if (autoIncrementFlag != null && autoIncrementFlag == 0) {
            jdbcTemplate.execute("ALTER TABLE order_items MODIFY order_item_id INT NOT NULL AUTO_INCREMENT");
            log.info("Enabled AUTO_INCREMENT for order_items.order_item_id");
        }
    }

    private boolean tableExists(JdbcTemplate jdbcTemplate, String tableName) {
        Integer count = jdbcTemplate.queryForObject(
                """
                SELECT COUNT(*)
                FROM information_schema.TABLES
                WHERE TABLE_SCHEMA = DATABASE()
                  AND TABLE_NAME = ?
                """,
                Integer.class,
                tableName
        );
        return count != null && count > 0;
    }

    private boolean columnExists(JdbcTemplate jdbcTemplate, String tableName, String columnName) {
        Integer count = jdbcTemplate.queryForObject(
                """
                SELECT COUNT(*)
                FROM information_schema.COLUMNS
                WHERE TABLE_SCHEMA = DATABASE()
                  AND TABLE_NAME = ?
                  AND COLUMN_NAME = ?
                """,
                Integer.class,
                tableName,
                columnName
        );
        return count != null && count > 0;
    }

    private List<String> findForeignKeys(JdbcTemplate jdbcTemplate, String tableName, String columnName) {
        return findForeignKeys(jdbcTemplate, tableName, columnName, null);
    }

    private List<String> findForeignKeys(JdbcTemplate jdbcTemplate, String tableName, String columnName, String referencedTableName) {
        return jdbcTemplate.query(
                """
                SELECT CONSTRAINT_NAME
                FROM information_schema.KEY_COLUMN_USAGE
                WHERE TABLE_SCHEMA = DATABASE()
                  AND TABLE_NAME = ?
                  AND COLUMN_NAME = ?
                  AND REFERENCED_TABLE_NAME IS NOT NULL
                  AND (? IS NULL OR REFERENCED_TABLE_NAME = ?)
                """,
                (rs, rowNum) -> rs.getString("CONSTRAINT_NAME"),
                tableName,
                columnName,
                referencedTableName,
                referencedTableName
        );
    }

    private List<String> findIndexes(JdbcTemplate jdbcTemplate, String tableName, String columnName) {
        return jdbcTemplate.query(
                """
                SELECT DISTINCT INDEX_NAME
                FROM information_schema.STATISTICS
                WHERE TABLE_SCHEMA = DATABASE()
                  AND TABLE_NAME = ?
                  AND COLUMN_NAME = ?
                  AND INDEX_NAME <> 'PRIMARY'
                """,
                (rs, rowNum) -> rs.getString("INDEX_NAME"),
                tableName,
                columnName
        );
    }
}
