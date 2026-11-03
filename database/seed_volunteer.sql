-- Demo data for the volunteer module. Run ONCE, after schema.sql and seed.sql.
-- The seeded donor and beneficiary cannot log in (their password hash is a placeholder).
DO $$
DECLARE
  v_donor BIGINT; v_ben BIGINT; v_don BIGINT;
  v_status BIGINT := (SELECT id FROM donation_statuses ORDER BY id LIMIT 1);
  v_cat BIGINT := (SELECT id FROM food_categories ORDER BY id LIMIT 1);
  v_unit BIGINT := (SELECT id FROM units ORDER BY id LIMIT 1);
  i INT;
BEGIN
  INSERT INTO users(name,email,password_hash,role) VALUES ('Demo Donor','donor@demo.local','seed','DONOR') RETURNING id INTO v_donor;
  INSERT INTO donors(user_id,donor_type) VALUES (v_donor,'INDIVIDUAL');
  INSERT INTO users(name,email,password_hash,role) VALUES ('Demo Beneficiary','beneficiary@demo.local','seed','BENEFICIARY') RETURNING id INTO v_ben;
  INSERT INTO beneficiaries(user_id,household_size,needs) VALUES (v_ben,4,'Dry food');
  FOR i IN 1..3 LOOP
    INSERT INTO donations(donor_id,status_id,donation_type) VALUES (v_donor,v_status,'FOOD') RETURNING id INTO v_don;
    INSERT INTO food_donations(donation_id,expiry_date,pickup_window) VALUES (v_don,CURRENT_DATE+7,CASE WHEN i=2 THEN 'weekdays' ELSE 'weekends' END);
    INSERT INTO food_items(food_donation_id,name,quantity,category_id,unit_id) VALUES (v_don,'Rice',10*i,v_cat,v_unit);
    INSERT INTO pickup_tasks(food_donation_id,beneficiary_id,required_skill) VALUES (v_don,v_ben,CASE WHEN i=3 THEN 'Driving' END);
  END LOOP;
  INSERT INTO events(title,description,event_date,location,event_type_id,required_skill) VALUES
    ('Food Drive','Sort and pack donations',NOW()+INTERVAL '7 days','Community Center',(SELECT id FROM event_types WHERE name='Food Distribution'),'Logistics'),
    ('First Aid Workshop','Training session',NOW()+INTERVAL '14 days','Main Office',(SELECT id FROM event_types WHERE name='Workshop'),NULL);
END $$;
