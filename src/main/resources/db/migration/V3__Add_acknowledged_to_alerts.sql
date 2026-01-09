-- Add acknowledged column to alerts table
ALTER TABLE alerts ADD COLUMN IF NOT EXISTS acknowledged BOOLEAN NOT NULL DEFAULT false;

