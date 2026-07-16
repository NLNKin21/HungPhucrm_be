ALTER TABLE projects
    MODIFY COLUMN elevator_type
        ENUM('GIA_DINH','KINH','HOMELIFT') NULL;

ALTER TABLE projects
    MODIFY COLUMN project_type
        ENUM('CAI_TAO','XAY_MOI') NULL;