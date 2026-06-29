-- Analysis pipeline fields and product ↔ analysis link

ALTER TABLE analysis_requests
    ADD COLUMN IF NOT EXISTS error_message TEXT,
    ADD COLUMN IF NOT EXISTS detected_title VARCHAR(255),
    ADD COLUMN IF NOT EXISTS confidence DOUBLE PRECISION;

ALTER TABLE products
    ADD COLUMN IF NOT EXISTS analysis_request_id UUID REFERENCES analysis_requests(id) ON DELETE SET NULL;

CREATE INDEX IF NOT EXISTS idx_products_analysis_request_id ON products(analysis_request_id);
