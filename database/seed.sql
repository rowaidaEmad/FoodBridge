INSERT INTO event_types(name) VALUES ('Fundraising'),('Workshop'),('Food Distribution') ON CONFLICT(name) DO NOTHING;
INSERT INTO food_categories(name) VALUES ('Grains'),('Produce'),('Dairy') ON CONFLICT(name) DO NOTHING;
INSERT INTO units(name) VALUES ('kg'),('item'),('liter') ON CONFLICT(name) DO NOTHING;
INSERT INTO donation_statuses(name) VALUES ('PENDING'),('ACCEPTED'),('COMPLETED'),('CANCELLED') ON CONFLICT(name) DO NOTHING;
