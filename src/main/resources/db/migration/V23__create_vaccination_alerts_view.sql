-- V23: Create vaccination_alerts view for Sanitary & Security Shield
-- Provides an optimized SQL view to query vaccination alerts for active batches
-- Requirements: 6.4, 6.5

CREATE OR REPLACE VIEW vaccination_alerts AS
SELECT
    b.id                                                       AS batch_id,
    b.batch_number,
    b.strain,
    vp.id                                                      AS protocol_id,
    vp.vaccine_name,
    vp.day_of_life,
    (b.arrival_date + (vp.day_of_life * INTERVAL '1 day'))::DATE  AS due_date,
    GREATEST(0, CURRENT_DATE - (b.arrival_date + (vp.day_of_life * INTERVAL '1 day'))::DATE)
                                                               AS days_overdue,
    CASE
        WHEN CURRENT_DATE > (b.arrival_date + (vp.day_of_life * INTERVAL '1 day'))::DATE
        THEN TRUE
        ELSE FALSE
    END                                                        AS is_overdue,
    EXISTS(
        SELECT 1
        FROM   health_records hr
        WHERE  hr.batch_id = b.id
          AND  hr.is_vaccination = TRUE
          AND  LOWER(hr.diagnosis) LIKE '%' || LOWER(vp.vaccine_name) || '%'
    )                                                          AS is_completed
FROM   batches            b
JOIN   vaccination_protocols vp ON b.strain = vp.strain
WHERE  b.status = 'Active'
  AND  (b.arrival_date + (vp.day_of_life * INTERVAL '1 day'))::DATE
           <= CURRENT_DATE + INTERVAL '7 days';

COMMENT ON VIEW vaccination_alerts IS
    'Vaccination alerts for active batches: due within 7 days or overdue, with completion status.';
