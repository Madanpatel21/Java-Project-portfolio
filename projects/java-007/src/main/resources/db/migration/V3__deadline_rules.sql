-- Court-calendar deadline rules (days offset from the trigger date; negative = before)
INSERT INTO deadline_rules (id, event_type, jurisdiction, days_offset, active) VALUES
('00000000-0000-0000-0000-000000000701','RESPONSE_DUE','DEFAULT', 21, TRUE),
('00000000-0000-0000-0000-000000000702','DISCOVERY','DEFAULT', 60, TRUE),
('00000000-0000-0000-0000-000000000703','APPEAL','DEFAULT', -7, TRUE),
('00000000-0000-0000-0000-000000000704','HEARING','DEFAULT', 90, TRUE);
