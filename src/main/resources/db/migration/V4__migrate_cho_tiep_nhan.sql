UPDATE consultations
SET status      = 'DA_TIEP_NHAN',
    accepted_at = COALESCE(accepted_at, created_at)
WHERE status = 'CHO_TIEP_NHAN';