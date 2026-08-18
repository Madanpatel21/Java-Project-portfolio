-- =====================================================================
-- JAVA-211 — catalog seed: antimicrobials (WHO DDD-aligned) + guidelines v1
-- =====================================================================

INSERT INTO antimicrobial_drugs (id, code, name, drug_class, spectrum, ddd_grams, iv_available, po_available, restricted, coverage_tags, iv_cost_per_day, po_cost_per_day) VALUES
('00000000-0000-0000-0000-000000000101','CEFTRIAXONE','Ceftriaxone','CEPHALOSPORIN','MEDIUM',2.0,TRUE,TRUE,FALSE,'GRAM_NEG,GRAM_POS',12.00,6.00),
('00000000-0000-0000-0000-000000000102','CEFAZOLIN','Cefazolin','CEPHALOSPORIN','NARROW',3.0,TRUE,TRUE,FALSE,'GRAM_POS,SURGICAL_PROPHYLAXIS',9.00,4.00),
('00000000-0000-0000-0000-000000000103','MEROPENEM','Meropenem','CARBAPENEM','BROAD',3.0,TRUE,FALSE,TRUE,'GRAM_NEG,GRAM_POS,ANAEROBE,PSEUDOMONAS,ESBL',60.00,NULL),
('00000000-0000-0000-0000-000000000104','PIPERACILLIN_TAZOBACTAM','Piperacillin-Tazobactam','PENICILLIN_BLI','BROAD',14.0,TRUE,FALSE,FALSE,'GRAM_NEG,GRAM_POS,ANAEROBE,PSEUDOMONAS',45.00,NULL),
('00000000-0000-0000-0000-000000000105','VANCOMYCIN','Vancomycin','GLYCOPEPTIDE','MEDIUM',2.0,TRUE,FALSE,TRUE,'GRAM_POS,MRSA',55.00,NULL),
('00000000-0000-0000-0000-000000000106','METRONIDAZOLE','Metronidazole','NITROIMIDAZOLE','NARROW',1.5,TRUE,TRUE,FALSE,'ANAEROBE,PROTOZOA',8.00,2.00),
('00000000-0000-0000-0000-000000000107','CIPROFLOXACIN','Ciprofloxacin','FLUOROQUINOLONE','MEDIUM',1.0,TRUE,TRUE,FALSE,'GRAM_NEG,PSEUDOMONAS',14.00,5.00),
('00000000-0000-0000-0000-000000000108','AZITHROMYCIN','Azithromycin','MACROLIDE','NARROW',0.5,FALSE,TRUE,FALSE,'ATYPICALS,GRAM_POS',NULL,4.00),
('00000000-0000-0000-0000-000000000109','AMOXICILLIN_CLAVULANATE','Amoxicillin-Clavulanate','PENICILLIN_BLI','MEDIUM',1.5,TRUE,TRUE,FALSE,'GRAM_POS,GRAM_NEG,ANAEROBE',10.00,3.00),
('00000000-0000-0000-0000-000000000110','LINEZOLID','Linezolid','OXAZOLIDINONE','MEDIUM',1.2,TRUE,TRUE,TRUE,'GRAM_POS,MRSA,VRE',110.00,90.00),
('00000000-0000-0000-0000-000000000111','FLUCONAZOLE','Fluconazole','AZOLE','NARROW',0.2,TRUE,TRUE,FALSE,'FUNGAL',20.00,8.00),
('00000000-0000-0000-0000-000000000112','DAPTOMYCIN','Daptomycin','LIPOPEPTIDE','MEDIUM',0.28,TRUE,FALSE,TRUE,'GRAM_POS,MRSA,VRE',180.00,NULL),
('00000000-0000-0000-0000-000000000113','COLISTIN','Colistin','POLYMYXIN','BROAD',0.3,TRUE,FALSE,TRUE,'GRAM_NEG,CRE,ESBL',140.00,NULL);

-- Guidelines v1: stewardship rule set
INSERT INTO stewardship_guidelines (id, name, version_no, status, effective_from, created_by, rules_json)
VALUES ('00000000-0000-0000-0000-000000000201','Hospital Stewardship Guideline Set',1,'ACTIVE',CURRENT_TIMESTAMP,'system',
'[' ||
' {"type":"MAX_DURATION","params":{"defaultDays":7,"perIndication":[{"indication":"COMMUNITY_PNEUMONIA","days":5},{"indication":"URINARY_TRACT","days":7},{"indication":"SURGICAL_PROPHYLAXIS","days":1}]}},' ||
' {"type":"IV_TO_PO_ELIGIBILITY","params":{"afebrileHours":48,"requiresGiFunction":true,"requiresClinicalImprovement":true}},' ||
' {"type":"RENAL_ADJUSTMENT","params":{"drugs":[{"code":"PIPERACILLIN_TAZOBACTAM","thresholdCrCl":40,"advice":"Extend interval to Q12H below CrCl 40 mL/min"},{"code":"VANCOMYCIN","thresholdCrCl":50,"advice":"Switch to dosing by trough levels below CrCl 50 mL/min"},{"code":"MEROPENEM","thresholdCrCl":50,"advice":"Extend interval to Q12H below CrCl 50 mL/min"},{"code":"CIPROFLOXACIN","thresholdCrCl":30,"advice":"Reduce dose by 50% below CrCl 30 mL/min"}]}},' ||
' {"type":"REVIEW_TRIGGER","params":{"empiricHours":48,"targetedDays":5}}' ||
']');
