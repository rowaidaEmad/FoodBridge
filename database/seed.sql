INSERT INTO event_types(name) VALUES ('Fundraising'),('Workshop'),('Food Distribution') ON CONFLICT(name) DO NOTHING;

-- Remove the earlier default values if nothing uses them yet (databases seeded before the donation module).
DELETE FROM food_categories WHERE name IN ('Grains')
  AND id NOT IN (SELECT category_id FROM food_items) AND id NOT IN (SELECT category_id FROM donation_requests);
DELETE FROM units WHERE name IN ('item','liter')
  AND id NOT IN (SELECT unit_id FROM food_items) AND id NOT IN (SELECT unit_id FROM donation_requests);
DELETE FROM donation_statuses WHERE name IN ('PENDING','ACCEPTED','COMPLETED','CANCELLED')
  AND id NOT IN (SELECT status_id FROM donations);

INSERT INTO food_categories(name) VALUES ('Cooked meals'),('Bakery'),('Dairy'),('Canned'),('Produce') ON CONFLICT(name) DO NOTHING;
INSERT INTO units(name) VALUES ('kg'),('meals'),('boxes'),('liters') ON CONFLICT(name) DO NOTHING;
INSERT INTO donation_statuses(name) VALUES ('Posted'),('Picked up'),('Delivered'),('Expired'),('Cancelled') ON CONFLICT(name) DO NOTHING;
-- Money donations move from Posted to Paid after a successful payment.
INSERT INTO donation_statuses(name) VALUES ('Paid') ON CONFLICT(name) DO NOTHING;
-- Default admin account. Log in with admin@foodbridge.test / secret123. Safe to run again.
INSERT INTO users(name,email,password_hash,role) VALUES ('Admin','admin@foodbridge.test','65536:kY71PDDOCjjipgPEjxKHAw==:zQ9txKrqSLY6rme+TG/YBuaBsHV3nbhUSmu1gmibJfw=','ADMIN') ON CONFLICT(email) DO NOTHING;
INSERT INTO admins(user_id) SELECT id FROM users WHERE email='admin@foodbridge.test' AND role='ADMIN' ON CONFLICT(user_id) DO NOTHING;
