UPDATE comments
SET modified_at = modifired_at
WHERE modified_at IS NULL
  AND modifired_at IS NOT NULL;

ALTER TABLE comments DROP COLUMN modifired_at;