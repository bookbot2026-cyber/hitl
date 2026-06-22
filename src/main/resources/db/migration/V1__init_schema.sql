-- Create reviews table
CREATE TABLE reviews (
    id UUID PRIMARY KEY,
    transaction_date DATE,
    type VARCHAR(50) CHECK (type IN ('INCOME', 'EXPENSE')),
    category VARCHAR(100),
    amount NUMERIC(12, 2),
    person VARCHAR(255),
    splits JSONB DEFAULT '[]'::jsonb,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING_REVIEW' CHECK (status IN ('PENDING_REVIEW', 'ACCEPTED', 'REJECTED')),
    is_duplicate BOOLEAN DEFAULT FALSE,
    duplicate_of_id UUID REFERENCES reviews(id) ON DELETE SET NULL,
    media_url VARCHAR(1024),
    media_type VARCHAR(50) CHECK (media_type IN ('IMAGE', 'AUDIO', 'TEXT')),
    raw_ocr_or_transcript TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Create transactions table (production ledger)
CREATE TABLE transactions (
    id UUID PRIMARY KEY,
    transaction_date DATE,
    type VARCHAR(50) CHECK (type IN ('INCOME', 'EXPENSE')),
    category VARCHAR(100),
    amount NUMERIC(12, 2),
    person VARCHAR(255),
    splits JSONB DEFAULT '[]'::jsonb,
    media_url VARCHAR(1024),
    media_type VARCHAR(50) CHECK (media_type IN ('IMAGE', 'AUDIO', 'TEXT')),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);
