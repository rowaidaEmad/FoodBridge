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
INSERT INTO areas(city,district) VALUES ('Cairo','Nasr City'),('Cairo','Maadi'),('Giza','Dokki'),('Alexandria','Smouha') ON CONFLICT(city,district) DO NOTHING;
