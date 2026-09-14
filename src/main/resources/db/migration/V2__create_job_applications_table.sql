CREATE TABLE IF NOT EXISTS job_applications (
    id UUID PRIMARY KEY,
    company_name VARCHAR(255) NOT NULL,
    role_title VARCHAR(255) NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'ADAPTED',
    match_score INT NOT NULL,
    applied_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    adapted_resume_id UUID NULL,
    match_analysis_id UUID NULL,
    notes TEXT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_job_app_adapted_resume
    FOREIGN KEY (adapted_resume_id)
    REFERENCES adapted_resumes(id)
                         ON DELETE SET NULL,
    CONSTRAINT fk_job_app_match_analysis
    FOREIGN KEY (match_analysis_id)
    REFERENCES match_analyses(id)
                         ON DELETE SET NULL,
    CONSTRAINT chk_job_app_status
    CHECK (status IN ('ADAPTED', 'APPLIED', 'INTERVIEWING', 'OFFER', 'REJECTED')),
    CONSTRAINT chk_job_app_match_score
    CHECK (match_score >= 0 AND match_score <= 100)
    );

CREATE INDEX IF NOT EXISTS idx_job_applications_status ON job_applications (status);
CREATE INDEX IF NOT EXISTS idx_job_applications_created_at_desc ON job_applications (created_at DESC);