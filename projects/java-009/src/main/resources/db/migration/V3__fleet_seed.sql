-- Seed reference maintenance plans (compliance + routine service kits).
INSERT INTO maintenance_plans (id, code, name, applies_to_category, interval_type, interval_value, compliance_required, active, created_at) VALUES
  ('plan-oil',    'OIL-CHANGE',      'Engine Oil & Filter Service', 'ANY',  'ODOMETER', 10000, FALSE, TRUE, CURRENT_TIMESTAMP),
  ('plan-brake',  'BRAKE-INSPECT',   'Brake System Inspection',     'ANY',  'ODOMETER', 30000, TRUE,  TRUE, CURRENT_TIMESTAMP),
  ('plan-dot',    'DOT-ANNUAL',      'DOT Annual Safety Inspection','TRUCK','CALENDAR', 365,   TRUE,  TRUE, CURRENT_TIMESTAMP),
  ('plan-tire',   'TIRE-ROTATION',   'Tire Rotation & Balance',     'ANY',  'ODOMETER', 8000,  FALSE, TRUE, CURRENT_TIMESTAMP);

INSERT INTO plan_items (id, plan_id, part_code, part_name, quantity, estimated_cost) VALUES
  ('pi-001', 'plan-oil',   'OIL-5W30',  'Engine Oil 5W-30 (5L)',   1,  42.50),
  ('pi-002', 'plan-oil',   'FILT-OIL',  'Oil Filter',              1,  12.75),
  ('pi-003', 'plan-brake', 'PAD-FRONT', 'Front Brake Pad Set',     1,  95.00),
  ('pi-004', 'plan-brake', 'SENS-ABS',  'ABS Sensor',              2,  38.00),
  ('pi-005', 'plan-tire',  'WHEEL-WGT', 'Wheel Balance Weights',   4,   6.25);

INSERT INTO parts (id, part_code, name, quantity_on_hand, reserved_qty, reorder_point, unit_cost) VALUES
  ('pt-001', 'OIL-5W30',  'Engine Oil 5W-30 (5L)', 40, 0, 10, 35.00),
  ('pt-002', 'FILT-OIL',  'Oil Filter',            60, 0, 15,  9.50),
  ('pt-003', 'PAD-FRONT', 'Front Brake Pad Set',    6, 0,  4, 80.00),
  ('pt-004', 'SENS-ABS',  'ABS Sensor',             8, 0,  4, 30.00),
  ('pt-005', 'WHEEL-WGT', 'Wheel Balance Weights', 50, 0, 20,  4.75);
