-- Add nullable repository owner and name to projects
ALTER TABLE projects ADD COLUMN repo_owner VARCHAR(255);
ALTER TABLE projects ADD COLUMN repo_name VARCHAR(255);
