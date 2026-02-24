-- V1__initial_schema.sql
-- Script de Criação Inicial do MVP - SEAGRI (PostgreSQL)

-- 1. organization
CREATE TABLE organization (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    created_by VARCHAR(64) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    status VARCHAR(32) NOT NULL DEFAULT 'ACTIVE'
);
CREATE INDEX idx_org_created_by ON organization(created_by);

-- 2. unit
CREATE TABLE unit (
    id UUID PRIMARY KEY,
    organization_id UUID NOT NULL,
    name VARCHAR(255) NOT NULL,
    geo_type VARCHAR(16) NOT NULL, -- RADIUS | POLYGON
    geo_center_lat DOUBLE PRECISION,
    geo_center_lng DOUBLE PRECISION,
    geo_radius_meters INTEGER,
    geo_polygon JSONB,
    created_at TIMESTAMP NOT NULL,
    FOREIGN KEY (organization_id) REFERENCES organization(id)
);
CREATE INDEX idx_unit_org ON unit(organization_id);

-- 3. membership
CREATE TABLE membership (
    id UUID PRIMARY KEY,
    organization_id UUID NOT NULL,
    user_id VARCHAR(64) NOT NULL, -- Keycloak sub
    role VARCHAR(32) NOT NULL,
    status VARCHAR(32) NOT NULL DEFAULT 'ACTIVE',
    joined_at TIMESTAMP NOT NULL,
    FOREIGN KEY (organization_id) REFERENCES organization(id)
);
CREATE UNIQUE INDEX uq_membership_user_org ON membership(user_id, organization_id);

-- 4. invitation
CREATE TABLE invitation (
    id UUID PRIMARY KEY,
    organization_id UUID NOT NULL,
    code VARCHAR(32) NOT NULL,
    expiration TIMESTAMP NOT NULL,
    created_by VARCHAR(64) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    used BOOLEAN NOT NULL DEFAULT FALSE,
    FOREIGN KEY (organization_id) REFERENCES organization(id)
);
CREATE UNIQUE INDEX uq_invitation_code ON invitation(code);

-- 5. biometric_profile
CREATE TABLE biometric_profile (
    id UUID PRIMARY KEY,
    user_id VARCHAR(64) NOT NULL,
    template BYTEA NOT NULL,
    template_version VARCHAR(32) NOT NULL,
    enrolled_at TIMESTAMP NOT NULL
);

-- 6. attendance_record
CREATE TABLE attendance_record (
    id UUID PRIMARY KEY,
    organization_id UUID NOT NULL,
    unit_id UUID NOT NULL,
    user_id VARCHAR(64) NOT NULL,
    latitude DOUBLE PRECISION NOT NULL,
    longitude DOUBLE PRECISION NOT NULL,
    gps_accuracy DOUBLE PRECISION NOT NULL,
    biometric_score DOUBLE PRECISION,
    biometric_valid BOOLEAN,
    device_id VARCHAR(128) NOT NULL,
    registration_mode VARCHAR(16) NOT NULL, -- SELF | SHARED_DEVICE
    intermediary_user_id VARCHAR(64),
    risk_score DOUBLE PRECISION,
    risk_flags JSONB,
    technical_status VARCHAR(32) NOT NULL DEFAULT 'RECEIVED',
    administrative_status VARCHAR(32) NOT NULL DEFAULT 'PENDING',
    captured_at TIMESTAMP NOT NULL,
    server_received_at TIMESTAMP NOT NULL,
    FOREIGN KEY (organization_id) REFERENCES organization(id),
    FOREIGN KEY (unit_id) REFERENCES unit(id)
);
CREATE INDEX idx_attendance_org_user ON attendance_record(organization_id, user_id);
CREATE INDEX idx_attendance_org_time ON attendance_record(organization_id, server_received_at);
CREATE INDEX idx_attendance_unit_time ON attendance_record(unit_id, server_received_at);

-- 7. device
CREATE TABLE device (
    id UUID PRIMARY KEY,
    device_id VARCHAR(128) NOT NULL,
    user_id VARCHAR(64),
    first_seen TIMESTAMP NOT NULL,
    last_seen TIMESTAMP NOT NULL,
    trust_level VARCHAR(16) NOT NULL,
    platform VARCHAR(32),
    UNIQUE(device_id)
);

-- 8. audit_log
CREATE TABLE audit_log (
    id UUID PRIMARY KEY,
    organization_id UUID,
    actor_user_id VARCHAR(64),
    action VARCHAR(128) NOT NULL,
    entity_type VARCHAR(64),
    entity_id UUID,
    decision VARCHAR(16),
    metadata JSONB,
    created_at TIMESTAMP NOT NULL
);
CREATE INDEX idx_audit_org_time ON audit_log(organization_id, created_at);

-- 9. consent
CREATE TABLE consent (
    id UUID PRIMARY KEY,
    user_id VARCHAR(64) NOT NULL,
    organization_id UUID NOT NULL,
    consent_type VARCHAR(64) NOT NULL,
    granted_at TIMESTAMP NOT NULL,
    revoked_at TIMESTAMP,
    FOREIGN KEY (organization_id) REFERENCES organization(id)
);

-- 10. relationship (Modelo ReBAC)
CREATE TABLE relationship (
    id UUID PRIMARY KEY,
    organization_id UUID NOT NULL,
    subject_id VARCHAR(64) NOT NULL,
    object_id VARCHAR(64) NOT NULL,
    relationship_type VARCHAR(64) NOT NULL,
    validity_start TIMESTAMP,
    validity_end TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (organization_id) REFERENCES organization(id)
);
CREATE INDEX idx_relationship_org_subject ON relationship(organization_id, subject_id);
CREATE INDEX idx_relationship_org_object ON relationship(organization_id, object_id);

-- 11. user_permissions_override (Exceções Granulares ABAC/PBAC)
CREATE TABLE user_permissions_override (
    id UUID PRIMARY KEY,
    organization_id UUID NOT NULL,
    user_id VARCHAR(64) NOT NULL,
    resource VARCHAR(128) NOT NULL,
    scope VARCHAR(64) NOT NULL,
    condition_json JSONB,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (organization_id) REFERENCES organization(id)
);
CREATE INDEX idx_permissions_org_user ON user_permissions_override(organization_id, user_id);
