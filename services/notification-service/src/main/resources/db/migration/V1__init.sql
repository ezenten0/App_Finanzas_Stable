CREATE TABLE notifications (
    id BIGSERIAL PRIMARY KEY,
    recipient VARCHAR(120) NOT NULL,
    channel VARCHAR(20) NOT NULL,
    subject VARCHAR(140) NOT NULL,
    body VARCHAR(500) NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL
);
