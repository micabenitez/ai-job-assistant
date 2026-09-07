CREATE TABLE resumes (
    id UUID PRIMARY KEY,
    original_filename VARCHAR(255) NOT NULL,
    raw_text TEXT NOT NULL,
    structured_data TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE job_offers (
    id UUID PRIMARY KEY,
    title VARCHAR(255),
    company VARCHAR(255),
    raw_text TEXT NOT NULL,
    structured_requirements TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE match_analyses (
    id UUID PRIMARY KEY,
    resume_id UUID NOT NULL,
    job_offer_id UUID NOT NULL,
    match_score INT,
    compatibility_level VARCHAR(50),
    analysis_result TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_match_analyses_resume FOREIGN KEY (resume_id) REFERENCES resumes(id) ON DELETE CASCADE,
    CONSTRAINT fk_match_analyses_job_offer FOREIGN KEY (job_offer_id) REFERENCES job_offers(id) ON DELETE CASCADE
);

CREATE TABLE adapted_resumes (
    id UUID PRIMARY KEY,
    match_analysis_id UUID NOT NULL UNIQUE,
    adapted_content TEXT NOT NULL,
    adaptation_notes TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_adapted_resumes_analysis FOREIGN KEY (match_analysis_id) REFERENCES match_analyses(id) ON DELETE CASCADE
);

CREATE INDEX idx_match_analyses_resume_id ON match_analyses(resume_id);
CREATE INDEX idx_match_analyses_job_offer_id ON match_analyses(job_offer_id);