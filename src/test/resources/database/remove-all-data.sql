DELETE FROM books_categories;
DELETE FROM categories;
DELETE FROM cart_items;
DELETE FROM order_items;
DELETE FROM books;

ALTER TABLE books_categories AUTO_INCREMENT = 1;
ALTER TABLE categories AUTO_INCREMENT = 1;
ALTER TABLE cart_items AUTO_INCREMENT = 1;
ALTER TABLE order_items AUTO_INCREMENT = 1;
ALTER TABLE books AUTO_INCREMENT = 1;
