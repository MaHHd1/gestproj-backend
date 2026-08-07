ALTER TABLE projects ADD COLUMN deployment_host VARCHAR(255);
ALTER TABLE projects ADD COLUMN deployment_container VARCHAR(255);

ALTER TABLE deployments ADD COLUMN workflow_name VARCHAR(255);
ALTER TABLE deployments ADD COLUMN workflow_run_id VARCHAR(100);
ALTER TABLE deployments ADD COLUMN workflow_url VARCHAR(1000);
ALTER TABLE deployments ADD COLUMN deployment_target VARCHAR(255);
ALTER TABLE deployments ADD COLUMN docker_status VARCHAR(50);
ALTER TABLE deployments ADD COLUMN docker_details TEXT;

CREATE INDEX idx_deployments_project_started_at ON deployments(project_id, started_at DESC);
