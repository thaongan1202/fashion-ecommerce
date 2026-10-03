-- Reviews previously approved without Admin moderation must be reviewed again.
UPDATE reviews
SET status = 'PENDING'
WHERE status = 'APPROVED';

INSERT INTO users (full_name, email, password_hash, role, status)
VALUES ('Demo Admin', 'demo.admin@fashion.local', 'demo-session-only', 'ADMIN', 'ACTIVE')
ON CONFLICT (lower(email)) DO NOTHING;
