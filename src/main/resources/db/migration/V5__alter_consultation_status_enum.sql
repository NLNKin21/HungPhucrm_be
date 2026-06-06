ALTER TABLE consultations
    MODIFY COLUMN status ENUM(
        'DA_TIEP_NHAN',
        'DA_LIEN_LAC',
        'CHUA_LIEN_LAC_DUOC',
        'KHONG_CO_NHU_CAU',
        'DANG_BAO_GIA',
        'THANH_CONG',
        'THAT_BAI',
        'DA_CHUYEN_DU_AN'
    ) NOT NULL DEFAULT 'DA_TIEP_NHAN';