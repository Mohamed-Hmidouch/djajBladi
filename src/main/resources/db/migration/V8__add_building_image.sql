ALTER TABLE buildings ADD COLUMN image_url VARCHAR(512);
COMMENT ON COLUMN buildings.image_url IS 'URL or path to building image';
